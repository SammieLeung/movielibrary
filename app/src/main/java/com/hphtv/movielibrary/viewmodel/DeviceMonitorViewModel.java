package com.hphtv.movielibrary.viewmodel;

import android.app.Application;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.firelfy.util.LogUtil;
import com.firelfy.util.StorageHelper;
import com.hphtv.movielibrary.data.ConstData;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.DeviceDao;
import com.hphtv.movielibrary.roomdb.dao.VideoFileDao;
import com.hphtv.movielibrary.roomdb.entity.Device;
import com.hphtv.movielibrary.roomdb.entity.VideoFile;
import com.hphtv.movielibrary.service.MovieScanService2;
import com.hphtv.movielibrary.service.Thread.DeviceInitThread;
import com.hphtv.movielibrary.service.Thread.FileScanThread;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * author: Sam Leung
 * date:  2021/6/11
 */
public class DeviceMonitorViewModel extends AndroidViewModel {
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
    private int mScanFlag = 0;

    public DeviceMonitorViewModel(@NonNull @NotNull Application application) {
        super(application);
        initThreadPools();
        initDb();
    }

    /**
     * 初始化数据库类
     */
    private void initDb() {
        MovieLibraryRoomDatabase movieLibraryRoomDatabase = MovieLibraryRoomDatabase.getDatabase(getApplication());
        mDeviceDao = movieLibraryRoomDatabase.getDeviceDao();
        mVideoFileDao = movieLibraryRoomDatabase.getVideoFileDao();
    }

    //初始化线程池
    private void initThreadPools() {
        mDeviceMountExecutor = Executors.newSingleThreadExecutor();
        mFileScanExecutor = new ThreadPoolExecutor(5, 5, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
        mDeviceInitExecutor = Executors.newSingleThreadExecutor();
        mSingleThreadPool = Executors.newSingleThreadExecutor();
    }

    /**
     * 扫描本地所有挂载设备并扫描文件
     */
    public void scanDevices() {
        if (mScanFlag == 0) {
            synchronized (DeviceMonitorViewModel.this) {
                if (mScanFlag == 0) {
                    mScanFlag = 1;
                    mDeviceInitExecutor.execute(new DeviceInitThread(this));
                }
            }
        }
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
        mDeviceMountExecutor.execute(new DeviceMountThread(deviceName, deviceType, localPath, isFromNetwork, networkPath, mountState));
    }

    public void startScanWithNotScannedFiles(MovieScanService2 scanService, String mountPath) {
        Observable.just(mountPath)
                .subscribeOn(Schedulers.from(mSingleThreadPool))
                .observeOn(Schedulers.from(mSingleThreadPool))
                .map(mount_path -> {
                    Device device = mDeviceDao.querybyMountPath(mount_path);
                    if(device!=null) {
                        List<VideoFile> videoFiles = getNotScannedFiles(device);
                        return videoFiles;
                    }
                    return new ArrayList<VideoFile>();
                })
                .subscribe(new SimpleObserver<List<VideoFile>>() {
                    @Override
                    public void onAction(List<VideoFile> videoFiles) {
                        if (videoFiles != null && videoFiles.size() > 0)
                            scanService.start(videoFiles);
                        else
                            LocalBroadcastManager.getInstance(getApplication()).sendBroadcast(new Intent(ConstData.BroadCastMsg.MOVIE_SCRAP_FINISH));
                    }
                });
    }

    /**
     * 获取所以未扫描的文件
     */
    private List<VideoFile> getNotScannedFiles(Device device) {
        List<VideoFile> mountedDeviceFiles = mVideoFileDao.queryAllNotScanedByIds(device.id);
        return mountedDeviceFiles;
    }

    /**
     * 设备挂载/卸载处理线程。
     */
    private class DeviceMountThread extends Thread {
        private String mDeviceName;
        private int mDeviceType;
        private String mMountPath;
        private boolean isNetwork;
        private String mNetworkPath;
        private int mMountState;

        private DeviceMountThread(String deviceName, int deviceType, String mountPath, boolean isFromNetwork, String networkPath, int mountState) {
            mDeviceName = deviceName;
            mDeviceType = deviceType;
            mMountPath = mountPath;
            isNetwork = isFromNetwork;
            mNetworkPath = networkPath;
            mMountState = mountState;
        }

        @Override
        public void run() {
            super.run();
            LogUtil.v("dump device======>:"
                    + "mountState:" + mMountState
                    + " mountPath:" + mMountPath
                    + " name:" + mDeviceName
                    + " isNetwork:" + isNetwork
                    + " networkPath:" + mNetworkPath
                    + " deviceType:" + mDeviceType);

            Intent broadIntent = new Intent();
            broadIntent.putExtra(ConstData.DeviceMountMsg.DEVICE_MOUNT_PATH, mMountPath);
            broadIntent.putExtra(ConstData.DeviceMountMsg.MOUNT_TYPE, mDeviceType);
            broadIntent.putExtra(ConstData.DeviceMountMsg.MOUNT_DEVICE_STATE, mMountState);
            broadIntent.putExtra(ConstData.DeviceMountMsg.IS_FROM_NETWORK, false);//TODO hard code
            //设备状态为mounted则将设备信息入库，并扫描设备文件。
            Device device_db = mDeviceDao.querybyMountPath(mMountPath);
            if (device_db != null) {
                mDeviceDao.deleteDevices(device_db);//删除设备
            }
            if (mMountState == ConstData.DeviceMountState.MOUNTED) {
                Device device = buildDeviceFromPath(mMountPath, mNetworkPath, mDeviceType, mDeviceName);//生成一个基础设备
                if (device == null)
                    return;
                //设备入库
                mDeviceDao.insertDevices(device);
                //保存设备id和设备路径的关联信息
                ConstData.devicePathIDs.put(mMountPath, device.id);//

                broadIntent.setAction(ConstData.BroadCastMsg.DEVICE_UP);
                broadIntent.putExtra(ConstData.DeviceMountMsg.DEVICE_ID, device.id);
                //启动文件扫描线程。
                Future<Boolean> scan = mFileScanExecutor.submit(new FileScanThread(getApplication(), device));
                try {
                    if (scan.get()) {
                        LocalBroadcastManager.getInstance(getApplication()).sendBroadcast(broadIntent);
                    }
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                ConstData.devicePathIDs.remove(mMountPath);
                broadIntent.setAction(ConstData.BroadCastMsg.DEVICE_DOWN);
                LocalBroadcastManager.getInstance(getApplication()).sendBroadcast(broadIntent);
            }

        }

        /**
         *
         * @param mountPath
         * @param netWorkPath
         * @param deviceType
         * @param name
         * @return
         */
        private Device buildDeviceFromPath(String mountPath, String netWorkPath, int deviceType, String name) {
            Device device = new Device();
            File file = new File(mountPath);
            if (!file.exists())//不是本地挂载设备返回null
                return null;
            device.localPath = mountPath;
            device.name = name;
            device.networkPath = netWorkPath;

            //获取设备具体类型
            if (deviceType == ConstData.DeviceType.DEVICE_TYPE_LOCAL) {
                if (StorageHelper.isMountUsb(getApplication(), mountPath)) {
                    device.type = ConstData.DeviceType.DEVICE_TYPE_USB;
                } else if (StorageHelper.isMountSdCard(getApplication(), mountPath)) {
                    device.type = ConstData.DeviceType.DEVICE_TYPE_SDCARDS;
                } else if (StorageHelper.isMountHardDisk(getApplication(), mountPath)) {
                    device.type = ConstData.DeviceType.DEVICE_TYPE_HARD_DISK;
                } else if (StorageHelper.isMountPcie(getApplication(), mountPath)) {
                    device.type = ConstData.DeviceType.DEVICE_TYPE_PCIE;
                } else {
                    device.type = ConstData.DeviceType.DEVICE_TYPE_INTERNAL_STORAGE;
                }
            } else {
                device.type = deviceType;
            }
            return device;
        }
    }
}
