package com.hphtv.movielibrary.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.DeviceDao;
import com.hphtv.movielibrary.roomdb.dao.ScanDirectoryDao;
import com.hphtv.movielibrary.roomdb.dao.VideoFileDao;
import com.hphtv.movielibrary.roomdb.entity.VideoFile;
import com.hphtv.movielibrary.service.Thread.DeviceInitThread;
import com.hphtv.movielibrary.service.Thread.DeviceMountThread;
import com.hphtv.movielibrary.service.Thread.FileScanThread;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;
import com.station.kit.util.LogUtil;
import com.hphtv.movielibrary.data.Constants;

import org.eclipse.jetty.util.ConcurrentHashSet;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * @author lxp
 * @date 19-3-26
 */
public class DeviceMonitorService extends Service {
    public static final String TAG = DeviceMonitorService.class.getSimpleName();
    private MonitorBinder mBinder;
    private StorageManager mStorageManager;
    private MovieScanService mMovieScanService;
    private ConcurrentHashSet<String> mPosterPairingDevice = new ConcurrentHashSet<>();
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

    private DeviceDao mDeviceDao;
    private VideoFileDao mVideoFileDao;
    private ScanDirectoryDao mScanDirectoryDao;
    private int mScanFlag = 0;


    /**
     * 处理Android 11设备挂载/卸载回调
     */
    Object mStorageVolumeCallback;

    /**
     * 初始化工作
     */
    @Override
    public void onCreate() {
        LogUtil.v(TAG, "OnCreate");
        super.onCreate();
        initThreadPools();
        initDb();
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

    /**
     * 初始化数据库类
     */
    private void initDb() {
        MovieLibraryRoomDatabase movieLibraryRoomDatabase = MovieLibraryRoomDatabase.getDatabase(getApplication());
        mDeviceDao = movieLibraryRoomDatabase.getDeviceDao();
        mVideoFileDao = movieLibraryRoomDatabase.getVideoFileDao();
        mScanDirectoryDao = movieLibraryRoomDatabase.getScanDirectoryDao();
    }

    //初始化线程池
    private void initThreadPools() {
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
                        executeOnMountThread(volume.getUuid(), Constants.DeviceType.DEVICE_TYPE_LOCAL, volume.getDirectory().getPath(), false, "", state);
                    } else {
                        //拔出U盘的时候volume.getDirectory()返回的是null，所以被迫使用反射获取mPath的值
                        try {
                            Class clazz = volume.getClass();
                            Field field_mPath = clazz.getDeclaredField("mPath");
                            field_mPath.setAccessible(true);
                            File fpath = (File) field_mPath.get(volume);
                            String path = fpath.toString();
                            executeOnMountThread(volume.getUuid(), Constants.DeviceType.DEVICE_TYPE_LOCAL, path, false, "", state);
                        } catch (NoSuchFieldException | IllegalAccessException e) {
                            e.printStackTrace();
                        }
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
            registerReceiver(mDeviceMountReceiver, newFilter);
        }
        IntentFilter localFilter = new IntentFilter();
        localFilter.addAction(Constants.BroadCastMsg.DEVICE_UP);
        localFilter.addAction(Constants.BroadCastMsg.DEVICE_DOWN);
        localFilter.addAction(Constants.BroadCastMsg.RESCAN_DEVICE);
        localFilter.addAction(Constants.BroadCastMsg.POSTER_PAIRING);
        localFilter.addAction(Constants.BroadCastMsg.POSTER_PAIRING_FOR_NETWORK_URI);
        LocalBroadcastManager.getInstance(this).registerReceiver(mDeviceMountReceiver, localFilter);
    }

    private void unRegisterReceivers() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            mStorageManager.unregisterStorageVolumeCallback((StorageManager.StorageVolumeCallback) mStorageVolumeCallback);
        } else {
            unregisterReceiver(mDeviceMountReceiver);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mDeviceMountReceiver);
        }
    }

    /**
     * 接受设备挂载广播
     */
    private BroadcastReceiver mDeviceMountReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            LogUtil.v(DeviceMonitorService.class.getSimpleName(), "mDeviceMountReceiver action:" + action);
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
                        executeOnMountThread(storageVolume.getUuid(), Constants.DeviceType.DEVICE_TYPE_LOCAL, path, false, "", state);
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
                //本地广播
                case Constants.BroadCastMsg.DEVICE_UP:
//                    String mountPath = intent.getStringExtra(Constants.Extras.DEVICE_MOUNT_PATH);
//                    LogUtil.v("currentPath=" + mountPath);
                    executeOnFileScanThread();
                    break;
                case Constants.BroadCastMsg.DEVICE_DOWN:
                    break;
                case Constants.BroadCastMsg.RESCAN_DEVICE:
                    reScanDevices();
                    break;
                case Constants.BroadCastMsg.POSTER_PAIRING:
                    startScanWithNotScannedFiles();
                    break;
                case Constants.BroadCastMsg.POSTER_PAIRING_FOR_NETWORK_URI:
                    String query_uri = intent.getStringExtra(Constants.Extras.QUERY_URI);
                    String network_dirpath = intent.getStringExtra(Constants.Extras.NETWORK_DIR_PATH);
                    startScanNetworkFiles(query_uri, network_dirpath);
                    break;
            }
        }
    };

    ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mMovieScanService = ((MovieScanService.ScanBinder) service).getService();
            scanDevices();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    /**
     * 扫描本地所有挂载设备并扫描文件
     */
    public void scanDevices() {
        if (mScanFlag == 0) {
            synchronized (DeviceMonitorService.this) {
                if (mScanFlag == 0) {
                    mScanFlag = 1;
                    mDeviceInitExecutor.execute(new DeviceInitThread(this));
                }
            }
        }
    }

    public void reScanDevices() {
        mScanFlag = 0;
        scanDevices();
    }

    /**
     * 设备挂载处理线程
     *
     * @param deviceName
     * @param deviceType
     * @param localPath
     * @param isFromNetwork
     * @param networkPath
     * @param mountState
     */
    public void executeOnMountThread(String deviceName, int deviceType, String localPath, boolean isFromNetwork, String networkPath, int mountState) {
        mDeviceMountExecutor.execute(new DeviceMountThread(this, deviceName, deviceType, localPath, isFromNetwork, networkPath, mountState));
    }

    public void executeOnFileScanThread() {
        mFileScanExecutor.execute(new FileScanThread(this));
    }

    public void startScanWithNotScannedFiles() {
        Observable.just("")
                .subscribeOn(Schedulers.from(mSingleThreadPool))
                .observeOn(Schedulers.from(mSingleThreadPool))
                .map(arg -> {
                    List<VideoFile> videoFiles = getNotScannedFiles();
                    return videoFiles;
                })
                .onErrorReturn(throwable -> {
                    throwable.printStackTrace();
                    return new ArrayList<VideoFile>();
                })
                .subscribe(new SimpleObserver<List<VideoFile>>() {
                    @Override
                    public void onAction(List<VideoFile> videoFiles) {
                        if (videoFiles != null && videoFiles.size() > 0) {
                            if (mMovieScanService != null)
                                mMovieScanService.addToPairingQueue(videoFiles);
                        } else {    //TODO mPosterPairingDevice处理
                            LocalBroadcastManager.getInstance(getApplication()).sendBroadcast(new Intent(Constants.BroadCastMsg.MOVIE_SCRAP_FINISH));
                        }
                    }
                });
    }


    public void startScanNetworkFiles(String query_dir, String networkPath) {
        Observable.just(query_dir)
                .subscribeOn(Schedulers.from(mSingleThreadPool))
                .observeOn(Schedulers.from(mSingleThreadPool))
                .map(uri -> {
                    List<VideoFile> videoFileList = new ArrayList<>();
                    Cursor cursor = getContentResolver().query(Uri.parse(uri), null, null, null, null);
                    if (cursor != null) {
                        while (cursor.moveToNext()) {
//                            Log.w(TAG, "startScanNetworkFiles:document_id "+  cursor.getString(cursor.getColumnIndex("document_id")) );
//                            Log.w(TAG, "startScanNetworkFiles:mime_type "+  cursor.getString(cursor.getColumnIndex("mime_type")) );
//                            Log.w(TAG, "startScanNetworkFiles:_display_name "+  cursor.getString(cursor.getColumnIndex("_display_name")) );
//                            Log.w(TAG, "startScanNetworkFiles:last_modified "+  cursor.getString(cursor.getColumnIndex("last_modified")) );
//                            Log.w(TAG, "startScanNetworkFiles:_size "+  cursor.getString(cursor.getColumnIndex("_size")) );
//                            Log.w(TAG, "startScanNetworkFiles:path "+  cursor.getString(cursor.getColumnIndex("path")) );
//                            Log.w(TAG, "startScanNetworkFiles:file_source "+  cursor.getString(cursor.getColumnIndex("file_source")) );
//                            VideoFile videoFile=new VideoFile();
//                            videoFile.path= cursor.getString(cursor.getColumnIndex("path"));
//                            videoFile.filename=cursor.getString(cursor.getColumnIndex("_display_name"));
//                            videoFile.dirPath=networkPath;
                            String path = cursor.getString(cursor.getColumnIndex("path"));
                            String filename = cursor.getString(cursor.getColumnIndex("_display_name"));
                            String dirPath = networkPath;

                            VideoFile videoFile = mVideoFileDao.queryByPath(path);
                            if (videoFile != null) {
                                if (videoFile.isScanned == 0)
                                    videoFileList.add(videoFile);
                            } else {
                                videoFile = new VideoFile();
                                videoFile.path = cursor.getString(cursor.getColumnIndex("path"));
                                videoFile.filename = filename;
                                videoFile.dirPath = dirPath;
                                long vid = mVideoFileDao.insertOrIgnore(videoFile);
                                videoFile.vid = vid;
                                videoFileList.add(videoFile);
                            }

                        }

                    }
                    return videoFileList;
                })
                .onErrorReturn(throwable -> new ArrayList<>())
                .subscribe(new SimpleObserver<List<VideoFile>>() {
                    @Override
                    public void onAction(List<VideoFile> videoFiles) {
                        if (videoFiles != null && videoFiles.size() > 0) {
                            if (mMovieScanService != null)
                                mMovieScanService.addToPairingQueue(videoFiles);
                        } else {    //TODO mPosterPairingDevice处理
                            LocalBroadcastManager.getInstance(getApplication()).sendBroadcast(new Intent(Constants.BroadCastMsg.MOVIE_SCRAP_FINISH));
                        }
                    }
                });
    }

    /**
     * 获取所以未扫描的文件
     */
    private List<VideoFile> getNotScannedFiles() {
        List<VideoFile> mountedDeviceFiles = mVideoFileDao.queryAllNotScanedVideoFiles();
        return mountedDeviceFiles;
    }


    public class MonitorBinder extends Binder {
        public DeviceMonitorService getService() {
            return DeviceMonitorService.this;
        }
    }
}
