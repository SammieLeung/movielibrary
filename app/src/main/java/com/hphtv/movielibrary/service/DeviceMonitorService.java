package com.hphtv.movielibrary.service;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.firefly.videonameparser.MovieNameInfo;
import com.firefly.videonameparser.VideoNameParser;
import com.firefly.videonameparser.VideoNameParser2;
import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.data.AuthHelper;
import com.hphtv.movielibrary.data.Config;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.ShortcutDao;
import com.hphtv.movielibrary.roomdb.dao.VideoFileDao;
import com.hphtv.movielibrary.roomdb.entity.Shortcut;
import com.hphtv.movielibrary.roomdb.entity.VideoFile;
import com.hphtv.movielibrary.scraper.service.OnlineDBApiService;
import com.hphtv.movielibrary.service.Thread.DeviceInitThread;
import com.hphtv.movielibrary.service.Thread.DeviceMountThread;
import com.hphtv.movielibrary.service.Thread.LocalFileScanHelper;
import com.hphtv.movielibrary.util.ServiceStatusHelper;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;
import com.station.kit.util.AppUtils;
import com.station.kit.util.LogUtil;
import com.hphtv.movielibrary.data.Constants;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * @author lxp
 * @date 19-3-26
 */
public class DeviceMonitorService extends Service {
    public static final String TAG = DeviceMonitorService.class.getSimpleName();
    private MonitorBinder mBinder;
    private MovieScanService mMovieScanService;
    /**
     * 单线程池服务，设备挂载，卸载线程
     */
    private ExecutorService mDeviceMountExecutor;
    /**
     * 单线程池服务，设备初始化检测服务
     */
    private ExecutorService mDeviceInitExecutor;
    private ExecutorService mFileScanExecutor;
    private ExecutorService mSingleThreadPool;

    private VideoFileDao mVideoFileDao;
    private ShortcutDao mShortcutDao;
    private AtomicBoolean isScanning = new AtomicBoolean(false);
    private Handler mHandler = new Handler();
    private Runnable mMountedTask, mUnmountedTask;

    /**
     * 处理Android 11设备挂载/卸载回调
     */
    private Object mStorageVolumeCallback;
    private StorageManager mStorageManager;


    private BroadcastReceiver mMediaMountReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case Intent.ACTION_MEDIA_MOUNTED:
                case Intent.ACTION_MEDIA_UNMOUNTED:
                    StorageVolume storageVolume = intent.getParcelableExtra(StorageVolume.EXTRA_STORAGE_VOLUME);
                    String state_txt = storageVolume.getState();
                    int state = state_txt.equals(Constants.DeviceMountState.MOUNTED_TEXT) ? Constants.DeviceMountState.MOUNTED : Constants.DeviceMountState.UNMOUNTED;
                    //Android 11以上
                    try {
                        Class clazz = storageVolume.getClass();
                        Method meth = clazz.getDeclaredMethod("getPath");
                        meth.setAccessible(true);
                        String path = (String) meth.invoke(storageVolume);
                        executeOnMountThread(storageVolume.getUuid(), Constants.DeviceType.DEVICE_TYPE_LOCAL, path, state);
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };

    /**
     * 接受设备挂载广播
     */
    private BroadcastReceiver mDeviceMountReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            LogUtil.v(DeviceMonitorService.class.getSimpleName(), "mDeviceMountReceiver action:" + action);
            switch (action) {
                case Constants.ACTION.DEVICE_MOUNTED:
                    if (intent.hasExtra(Constants.Extras.DEVICE_MOUNT_PATH)) {
                        String mountPath = intent.getStringExtra(Constants.Extras.DEVICE_MOUNT_PATH);
                        Observable.just(mountPath)
                                .observeOn(Schedulers.from(mFileScanExecutor))
                                .subscribe(new SimpleObserver<String>() {
                                    @Override
                                    public void onAction(String device_path) {
                                        if (Config.isAutoSearch().get()) {
                                            startScanLocalDevices(device_path);
                                        }
                                    }
                                });
                    }
                    break;
                case Constants.ACTION.DEVICE_UNMOUNTED:
                    break;
                case Constants.ACTION.DEVICE_INIT:
                    new Thread(() -> {
                        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                            List<Shortcut> shortcutList = mShortcutDao.queryAllConnectShortcuts();
                            OnlineDBApiService.notifyShortcuts(shortcutList, Constants.Scraper.TMDB);
                            OnlineDBApiService.notifyShortcuts(shortcutList, Constants.Scraper.TMDB_EN);
                        }
                    }).start();
                case Constants.ACTION.RESCAN_ALL_FILES:
                    if (Config.isAutoSearch().get()) {
                        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                            startScanAllLocalUnScannedFiles();
                    }
                    break;
                case Constants.ACTION.ADD_LOCAL_SHORTCUT:
                    if (intent.hasExtra(Constants.Extras.QUERY_SHORTCUT)) {
                        Shortcut shortcut = (Shortcut) intent.getSerializableExtra(Constants.Extras.QUERY_SHORTCUT);
                        boolean isQuickMode = intent.getBooleanExtra(Constants.Extras.QUICKMODE, true);
                        startScanLocalShortcut(shortcut, isQuickMode);
                    }
                    break;
                case Constants.ACTION.ADD_NETWORK_SHORTCUT:
                    if (intent.getSerializableExtra(Constants.Extras.QUERY_SHORTCUT) != null) {
                        Shortcut shortcut = (Shortcut) intent.getSerializableExtra(Constants.Extras.QUERY_SHORTCUT);
                        boolean isQuickMode = intent.getBooleanExtra(Constants.Extras.QUICKMODE, true);
                        startScanNetworkFiles(shortcut, isQuickMode);
                    }
                    break;
                case Constants.ACTION.MOVIE_SCRAP_START:
                    if (!AppUtils.isAppInBackground(getBaseContext()))
                        ServiceStatusHelper.addView(getString(R.string.scanning_in_background), getBaseContext());
                    break;
                case Constants.ACTION.MOVIE_SCRAP_STOP:
                case Constants.ACTION.MOVIE_SCRAP_STOP_AND_REFRESH:
                    isScanning.set(false);
                    ServiceStatusHelper.removeView(context);
                    break;
            }
        }
    };

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mMovieScanService = ((MovieScanService.ScanBinder) service).getService();
            new Thread(() -> {
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    AuthHelper.init();
                    scanDevices();
                }
            }).start();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    /**
     * 绑定扫描服务
     * 绑定监听器
     *
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtil.v(TAG, "onStartCommand flags:" + flags + " startId:" + startId);
        bindRegisterReceivers();
        bindServices();
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        LogUtil.v(TAG, "onBind");
        if (mBinder == null)
            mBinder = new MonitorBinder();
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        LogUtil.v(TAG, "onDestroy");
        super.onDestroy();
        unRegisterReceivers();
        unbindServices();
    }

    //初始化
    private void init() {
        MovieLibraryRoomDatabase movieLibraryRoomDatabase = MovieLibraryRoomDatabase.getDatabase(getApplication());
        mVideoFileDao = movieLibraryRoomDatabase.getVideoFileDao();
        mShortcutDao = movieLibraryRoomDatabase.getShortcutDao();
        mDeviceMountExecutor = Executors.newSingleThreadExecutor();
        mFileScanExecutor = new ThreadPoolExecutor(5, 5, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
        mDeviceInitExecutor = Executors.newSingleThreadExecutor();
        mSingleThreadPool = Executors.newSingleThreadExecutor();
    }

    private void bindServices() {
        Intent intent = new Intent();
        intent.setClass(this, MovieScanService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void unbindServices() {
        unbindService(mServiceConnection);
    }


    /**
     * 注册广播
     */
    private void bindRegisterReceivers() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            mStorageVolumeCallback = new StorageManager.StorageVolumeCallback() {
                @Override
                public void onStateChanged(@NonNull StorageVolume volume) {
                    super.onStateChanged(volume);
                    String state_txt = volume.getState();
                    int state = state_txt.equals(Constants.DeviceMountState.MOUNTED_TEXT) ? Constants.DeviceMountState.MOUNTED : Constants.DeviceMountState.UNMOUNTED;

                    if (state == Constants.DeviceMountState.MOUNTED) {
                        mHandler.removeCallbacks(mMountedTask);
                        mMountedTask = new MountedRunnable(volume, state);
                        mHandler.postDelayed(mMountedTask, 500);
                    } else {
                        //拔出U盘的时候volume.getDirectory()返回的是null，所以被迫使用反射获取mPath的值
                        mHandler.removeCallbacks(mUnmountedTask);
                        mUnmountedTask = new UnMountRunnable(volume, state);
                        mHandler.postDelayed(mUnmountedTask, 500);
                    }
                    LogUtil.v("onStateChanged name " + volume.getMediaStoreVolumeName() + " " + volume.getState());
                }
            };
            mStorageManager = (StorageManager) getApplication().getSystemService(Context.STORAGE_SERVICE);
            mStorageManager.registerStorageVolumeCallback(getMainExecutor(), (StorageManager.StorageVolumeCallback) mStorageVolumeCallback);
        } else {
            IntentFilter newFilter = new IntentFilter();
            newFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
            newFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
            newFilter.addDataScheme("file");
            registerReceiver(mMediaMountReceiver, newFilter);
        }
        IntentFilter localFilter = new IntentFilter();
        localFilter.addAction(Constants.ACTION.DEVICE_INIT);
        localFilter.addAction(Constants.ACTION.DEVICE_MOUNTED);
        localFilter.addAction(Constants.ACTION.DEVICE_UNMOUNTED);
        localFilter.addAction(Constants.ACTION.RESCAN_ALL_FILES);
        localFilter.addAction(Constants.ACTION.ADD_LOCAL_SHORTCUT);
        localFilter.addAction(Constants.ACTION.ADD_NETWORK_SHORTCUT);
        localFilter.addAction(Constants.ACTION.MOVIE_SCRAP_START);
        localFilter.addAction(Constants.ACTION.MOVIE_SCRAP_STOP);
        localFilter.addAction(Constants.ACTION.MOVIE_SCRAP_STOP_AND_REFRESH);

        LocalBroadcastManager.getInstance(this).registerReceiver(mDeviceMountReceiver, localFilter);
    }

    private void unRegisterReceivers() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            mStorageManager.unregisterStorageVolumeCallback((StorageManager.StorageVolumeCallback) mStorageVolumeCallback);
        } else {
            unregisterReceiver(mMediaMountReceiver);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mDeviceMountReceiver);
        }
    }

    /**
     * 扫描本地所有挂载设备并扫描文件
     */
    public void scanDevices() {
        mDeviceInitExecutor.execute(new DeviceInitThread(this));
    }


    /**
     * 设备挂载处理线程
     *
     * @param deviceName
     * @param deviceType
     * @param localPath
     * @param mountState
     */
    public void executeOnMountThread(String deviceName, int deviceType, String localPath, int mountState) {
        mDeviceMountExecutor.execute(new DeviceMountThread(this, deviceName, deviceType, localPath, mountState));
    }

    private void startScanLocalDevices(String devicePath) {
        Observable.create((ObservableOnSubscribe<Object[]>) emitter -> {
            List<Shortcut> shortcutList = LocalFileScanHelper.scanDevice(getApplication(), devicePath);
            for (Shortcut shortcut : shortcutList) {
                List<VideoFile> videoFileList = mVideoFileDao.queryUnScannedVideoFiles(shortcut.uri);
                Object[] data = new Object[2];
                data[0] = shortcut;
                data[1] = videoFileList;
                emitter.onNext(data);
            }
            emitter.onComplete();
        }).subscribeOn(Schedulers.newThread())
                .onErrorReturn(throwable -> {
                    throwable.printStackTrace();
                    return new Object[0];
                })//TODO 切换成主线程?
                .subscribe(new SimpleObserver<Object[]>() {
                    @Override
                    public void onAction(Object[] data) {
                        if (data.length == 2) {
                            Shortcut shortcut = (Shortcut) data[0];
                            List<VideoFile> videoFileList = (List<VideoFile>) data[1];
                            if (videoFileList != null && videoFileList.size() > 0) {
                                if (mMovieScanService != null)
                                    mMovieScanService.scanVideo(shortcut, videoFileList);
                            } else {    //TODO mPosterPairingDevice处理
                                LocalBroadcastManager.getInstance(DeviceMonitorService.this).sendBroadcast(new Intent(Constants.ACTION.MOVIE_SCRAP_STOP));
                                shortcut.isScanned = 1;
                                mShortcutDao.updateShortcut(shortcut);
                                Intent intent = new Intent();
                                intent.setAction(Constants.ACTION.SHORTCUT_SCRAP_STOP);
                                intent.putExtra(Constants.Extras.SHORTCUT, shortcut);
                                LocalBroadcastManager.getInstance(DeviceMonitorService.this).sendBroadcast(intent);
                            }
                        } else {
                            LocalBroadcastManager.getInstance(getApplication()).sendBroadcast(new Intent(Constants.ACTION.MOVIE_SCRAP_STOP));
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        LocalBroadcastManager.getInstance(getApplication()).sendBroadcast(new Intent(Constants.ACTION.MOVIE_SCRAP_STOP));
                    }

                    @Override
                    public void onSubscribe(Disposable d) {
                        super.onSubscribe(d);
                        LocalBroadcastManager.getInstance(getApplication()).sendBroadcast(new Intent(Constants.ACTION.MOVIE_SCRAP_START));
                    }


                });
    }

    private void startScanLocalShortcut(Shortcut shortcut, boolean isQuick) {
        Observable.just(shortcut)
                .observeOn(Schedulers.newThread())
                .map(shortcut1 -> {
                    LocalFileScanHelper.searchShortcut(getApplication(), shortcut1);
                    List<VideoFile> videoFileList;
                    if (isQuick)
                        videoFileList = mVideoFileDao.queryUnScannedVideoFiles(shortcut1.uri);
                    else {
                        videoFileList = mVideoFileDao.queryVideoFilesOnShortcut(shortcut1.uri);
                        shortcut1.posterCount = 0;
                        mShortcutDao.updateShortcut(shortcut1);
                    }
                    Object[] data = new Object[2];
                    data[0] = shortcut1;
                    data[1] = videoFileList;
                    return data;
                })
                .onErrorReturn(throwable -> {
                    throwable.printStackTrace();
                    return new Object[0];
                })//TODO 切换成主线程?
                .subscribe(new SimpleObserver<Object[]>() {
                    @Override
                    public void onAction(Object[] data) {
                        if (data.length == 2) {
                            Shortcut shortcut = (Shortcut) data[0];
                            List<VideoFile> videoFileList = (List<VideoFile>) data[1];
                            if (videoFileList != null && videoFileList.size() > 0) {
                                if (mMovieScanService != null)
                                    mMovieScanService.scanVideo(shortcut, videoFileList);
                            } else {    //TODO mPosterPairingDevice处理
                                LocalBroadcastManager.getInstance(DeviceMonitorService.this).sendBroadcast(new Intent(Constants.ACTION.MOVIE_SCRAP_STOP));
                                shortcut.isScanned = 1;
                                mShortcutDao.updateShortcut(shortcut);
                                Intent intent = new Intent();
                                intent.setAction(Constants.ACTION.SHORTCUT_SCRAP_STOP);
                                intent.putExtra(Constants.Extras.SHORTCUT, shortcut);
                                LocalBroadcastManager.getInstance(DeviceMonitorService.this).sendBroadcast(intent);
                            }
                        } else {
                            LocalBroadcastManager.getInstance(getApplication()).sendBroadcast(new Intent(Constants.ACTION.MOVIE_SCRAP_STOP));
                        }
                    }

                    @Override
                    public void onSubscribe(Disposable d) {
                        super.onSubscribe(d);
                        LocalBroadcastManager.getInstance(getApplication()).sendBroadcast(new Intent(Constants.ACTION.MOVIE_SCRAP_START));
                    }
                });
    }


    private void startScanNetworkFiles(Shortcut shortcut, boolean isQuick) {
        Observable.just(shortcut)
                .subscribeOn(Schedulers.newThread())
                .map(shortcut1 -> {
                    List<VideoFile> videoFileList = new ArrayList<>();
                    String queryUri = shortcut1.queryUri;
                    String networkPath = shortcut1.uri;
                    Cursor cursor = getContentResolver().query(Uri.parse(queryUri), null, null, null, null);
                    if (cursor != null) {
                        int fileCount = cursor.getCount();
                        if (!isQuick)
                            shortcut1.posterCount = 0;
                        shortcut1.fileCount = fileCount;
                        mShortcutDao.updateShortcut(shortcut1);
                        while (cursor.moveToNext()) {
                            String path = cursor.getString(cursor.getColumnIndex("path"));
                            String filename = cursor.getString(cursor.getColumnIndex("_display_name"));
                            String dirPath = networkPath;

                            VideoFile videoFile = mVideoFileDao.queryByPath(path);
                            if (videoFile != null) {
                                if (isQuick) {
                                    if (videoFile.isScanned == 0)
                                        videoFileList.add(videoFile);
                                } else {
                                    videoFileList.add(videoFile);
                                }
                            } else {
                                videoFile = new VideoFile();
                                videoFile.path = cursor.getString(cursor.getColumnIndex("path"));
                                videoFile.filename = filename;
                                videoFile.dirPath = dirPath;
                                videoFile.addTime = System.currentTimeMillis();
                                long vid = mVideoFileDao.insertOrIgnore(videoFile);
                                videoFile.vid = vid;
                                videoFileList.add(videoFile);
                            }
                        }
                    }
                    LogUtil.v("startScanNetworkFiles [" + shortcut1.friendlyName + "] filecount:" + videoFileList.size());
                    Object[] data = new Object[2];
                    data[0] = shortcut1;
                    data[1] = videoFileList;
                    return data;
                })
                .onErrorReturn(throwable -> new Object[0])
                .subscribe(new SimpleObserver<Object[]>() {
                    @Override
                    public void onAction(Object[] data) {
                        if (data.length == 2) {
                            Shortcut shortcut = (Shortcut) data[0];
                            List<VideoFile> videoFileList = (List<VideoFile>) data[1];
                            if (videoFileList != null && videoFileList.size() > 0) {
                                if (mMovieScanService != null)
                                    mMovieScanService.scanVideo(shortcut, videoFileList);
                            } else {    //TODO mPosterPairingDevice处理
                                LocalBroadcastManager.getInstance(getApplication()).sendBroadcast(new Intent(Constants.ACTION.MOVIE_SCRAP_STOP));
                                ServiceStatusHelper.removeView(DeviceMonitorService.this);
                            }
                        }
                    }

                    @Override
                    public void onSubscribe(Disposable d) {
                        super.onSubscribe(d);
                        if (!AppUtils.isAppInBackground(getBaseContext()))
                            ServiceStatusHelper.addView(getString(R.string.scanning_in_background), DeviceMonitorService.this);
                    }
                });
    }

    private void startScanAllLocalUnScannedFiles() {
        if (!isScanning.get())
            Observable.just("")
                    .subscribeOn(Schedulers.newThread())
                    .map(arg -> {
                        LocalFileScanHelper.searchAllLocalShortcuts(getBaseContext());
                        List<VideoFile> unScannedFiles = getUnScannedLocalFiles();
                        HashMap<String, List<VideoFile>> map = new HashMap<>();
                        for (VideoFile videoFile : unScannedFiles) {
                            if (map.get(videoFile.dirPath) == null) {
                                map.put(videoFile.dirPath, new ArrayList<>());
                            }
                            map.get(videoFile.dirPath).add(videoFile);
                        }
                        Set<String> shortcutUris = map.keySet();
                        Iterator<String> urisIterator = shortcutUris.iterator();
                        List<Object[]> dataList = new ArrayList<>();
                        while (urisIterator.hasNext()) {
                            String uri = urisIterator.next();
                            Shortcut shortcut = mShortcutDao.queryShortcutByUri(uri);
                            if (shortcut != null) {
                                Object[] data = new Object[2];
                                data[0] = shortcut;
                                data[1] = map.get(uri);
                                dataList.add(data);
                            }
                        }
                        return dataList;

                    })
                    .onErrorReturn(throwable -> {
                        throwable.printStackTrace();
                        return new ArrayList<>();
                    })
                    .subscribe(new SimpleObserver<List<Object[]>>() {
                        @Override
                        public void onAction(List<Object[]> dataList) {
                            if (dataList.size() > 0) {
                                Log.w(Thread.currentThread().getName(), "StartScanAllUnScannedVideos=>");
                                for (Object[] objArr : dataList) {
                                    if (objArr != null && objArr.length == 2) {
                                        Shortcut shortcut = (Shortcut) objArr[0];
                                        List<VideoFile> videoFileList = (List<VideoFile>) objArr[1];
                                        if (videoFileList.size() > 0) {
                                            Log.w(Thread.currentThread().getName(), shortcut.friendlyName);
                                            if (mMovieScanService != null)
                                                mMovieScanService.scanVideo(shortcut, videoFileList);
                                        } else {
                                            LocalBroadcastManager.getInstance(getApplication()).sendBroadcast(new Intent(Constants.ACTION.MOVIE_SCRAP_STOP));
                                        }
                                    }
                                }
                            } else {
                                LocalBroadcastManager.getInstance(getApplication()).sendBroadcast(new Intent(Constants.ACTION.MOVIE_SCRAP_STOP));
                            }
                        }

                        @Override
                        public void onSubscribe(Disposable d) {
                            super.onSubscribe(d);
                            isScanning.set(true);
                            LocalBroadcastManager.getInstance(getApplication()).sendBroadcast(new Intent(Constants.ACTION.MOVIE_SCRAP_START));

                        }
                    });
    }

    /**
     * 获取所以未扫描的文件
     */
    private List<VideoFile> getUnScannedLocalFiles() {
        return mVideoFileDao.queryAllLocalUnScannedVideoFiles();
    }

    public class MonitorBinder extends Binder {
        public DeviceMonitorService getService() {
            return DeviceMonitorService.this;
        }
    }

    public class MountedRunnable implements Runnable {
        private StorageVolume mStorageVolume;
        private int mState;

        public MountedRunnable(StorageVolume volume, int state) {
            mStorageVolume = volume;
            mState = state;

        }

        @Override
        public void run() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                executeOnMountThread(mStorageVolume.getUuid(), Constants.DeviceType.DEVICE_TYPE_LOCAL, mStorageVolume.getDirectory().getPath(), mState);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MountedRunnable that = (MountedRunnable) o;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                return Objects.equals(mStorageVolume.getDirectory().getPath(), that.mStorageVolume.getDirectory().getPath());
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(mStorageVolume);
        }
    }

    public class UnMountRunnable implements Runnable {
        private StorageVolume mStorageVolume;
        private int mState;
        private String mPath;

        public UnMountRunnable(StorageVolume volume, int state) {
            mStorageVolume = volume;
            mState = state;
            try {
                Class clazz = mStorageVolume.getClass();
                Field field_mPath = clazz.getDeclaredField("mPath");
                field_mPath.setAccessible(true);
                File fpath = (File) field_mPath.get(mStorageVolume);
                mPath = fpath.toString();
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void run() {
            executeOnMountThread(mStorageVolume.getUuid(), Constants.DeviceType.DEVICE_TYPE_LOCAL, mPath, mState);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            UnMountRunnable that = (UnMountRunnable) o;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                return Objects.equals(mPath, that.mPath);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(mStorageVolume);
        }
    }
}
