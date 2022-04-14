package com.hphtv.movielibrary.service.Thread;

import android.content.Intent;
import android.text.TextUtils;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.hphtv.movielibrary.roomdb.entity.Device;
import com.hphtv.movielibrary.service.DeviceMonitorService;
import com.station.kit.util.LogUtil;
import com.station.kit.util.StorageHelper;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.DeviceDao;
import com.hphtv.movielibrary.roomdb.dao.VideoFileDao;


import java.io.File;
import java.util.List;

/**
 * @author lxp
 * @date 18-12-13
 * 设备初始化检测线程。
 */
public class DeviceInitThread extends Thread {
    public static final String TAG = DeviceInitThread.class.getSimpleName();
    private DeviceMonitorService mDeviceMonitorService;
    private DeviceDao mDeviceDao;
    private VideoFileDao mVideoFileDao;
    private MovieLibraryRoomDatabase mMovieLibraryRoomDatabase;

    public DeviceInitThread(DeviceMonitorService monitorService) {
        mDeviceMonitorService = monitorService;
        mMovieLibraryRoomDatabase = MovieLibraryRoomDatabase.getDatabase(mDeviceMonitorService);
        mDeviceDao = mMovieLibraryRoomDatabase.getDeviceDao();
        mVideoFileDao = mMovieLibraryRoomDatabase.getVideoFileDao();
    }

    /**
     * 获取本地设备后，启动设备挂载线程处理。
     */
    @Override
    public void run() {
        super.run();
        //清除数据库设备,
        mDeviceDao.deleteAll();

        LogUtil.v(TAG, "=============>挂载设备:");

        String internelStorage = StorageHelper.getFlashStoragePath(mDeviceMonitorService);
        List<String> allCardPaths = StorageHelper.getSdCardPaths(mDeviceMonitorService);
        List<String> allUsbPaths = StorageHelper.getUSBPaths(mDeviceMonitorService);
        List<String> allPciePaths = StorageHelper.getPciePaths(mDeviceMonitorService);
        List<String> allHardDiskPaths = StorageHelper.getHardDiskPaths(mDeviceMonitorService);

        //扫描sd卡
        if (allCardPaths != null && allCardPaths.size() > 0) {
            for (String path : allCardPaths) {
                String deviceName = path.substring(path.lastIndexOf("/") + 1);
                int type = Constants.DeviceType.DEVICE_TYPE_SDCARDS;
                saveDevice(path,type,deviceName);
            }
        }

        //扫描USB设备
        if (allUsbPaths != null && allUsbPaths.size() > 0) {
            for (String path : allUsbPaths) {
                String deviceName = path.substring(path.lastIndexOf("/") + 1);
                int type = Constants.DeviceType.DEVICE_TYPE_USB;
                saveDevice(path,type,deviceName);
            }
        }

        //扫描PCIE设备
        if (allPciePaths != null && allPciePaths.size() > 0) {
            for (String path : allPciePaths) {
                String deviceName = path.substring(path.lastIndexOf("/") + 1);
                int type = Constants.DeviceType.DEVICE_TYPE_PCIE;
                saveDevice(path,type,deviceName);
            }
        }

        //扫描Hard Disk设备
        if (allHardDiskPaths != null && allHardDiskPaths.size() > 0) {
            for (String path : allHardDiskPaths) {
                String deviceName = path.substring(path.lastIndexOf("/") + 1);
                int type = Constants.DeviceType.DEVICE_TYPE_HARD_DISK;
                saveDevice(path,type,deviceName);
            }
        }

        if (!TextUtils.isEmpty(internelStorage)) {
            //扫描内置存储
            String deviceName = internelStorage.substring(internelStorage.lastIndexOf("/") + 1);//TODO 验证Android 11上的可行性
            int type = Constants.DeviceType.DEVICE_TYPE_INTERNAL_STORAGE;
            saveDevice(internelStorage,type,deviceName);
        }

        Intent intent=new Intent();
        intent.setAction(Constants.ACTION.DEVICE_INIT);
        LocalBroadcastManager.getInstance(mDeviceMonitorService).sendBroadcast(intent);
    }

    private void saveDevice(String mountPath, int deviceType, String name) {
        Device device = new Device();
        File file = new File(mountPath);
        if (!file.exists())//不是本地挂载设备返回null
            return;
        device.path = mountPath;
        device.name = name;

        //获取设备具体类型
        if (deviceType == Constants.DeviceType.DEVICE_TYPE_LOCAL) {
            if (StorageHelper.isMountUsb(mDeviceMonitorService, mountPath)) {
                device.type = Constants.DeviceType.DEVICE_TYPE_USB;
            } else if (StorageHelper.isMountSdCard(mDeviceMonitorService, mountPath)) {
                device.type = Constants.DeviceType.DEVICE_TYPE_SDCARDS;
            } else if (StorageHelper.isMountHardDisk(mDeviceMonitorService, mountPath)) {
                device.type = Constants.DeviceType.DEVICE_TYPE_HARD_DISK;
            } else if (StorageHelper.isMountPcie(mDeviceMonitorService, mountPath)) {
                device.type = Constants.DeviceType.DEVICE_TYPE_PCIE;
            } else {
                device.type = Constants.DeviceType.DEVICE_TYPE_INTERNAL_STORAGE;
            }
        } else {
            device.type = deviceType;
        }
        mDeviceDao.insertDevice(device);
    }

}
