package com.hphtv.movielibrary.service.Thread;

import android.text.TextUtils;

import com.station.kit.util.LogUtil;
import com.station.kit.util.StorageHelper;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.DeviceDao;
import com.hphtv.movielibrary.roomdb.dao.VideoFileDao;
import com.hphtv.movielibrary.viewmodel.DeviceMonitorViewModel;


import java.util.List;

/**
 * @author lxp
 * @date 18-12-13
 * 设备初始化检测线程。
 */
public class DeviceInitThread extends Thread {
    public static final String TAG = DeviceInitThread.class.getSimpleName();
    private DeviceMonitorViewModel mDeviceMonitorViewModel;
    private DeviceDao mDeviceDao;
    private VideoFileDao mVideoFileDao;
    private MovieLibraryRoomDatabase mMovieLibraryRoomDatabase;

    public DeviceInitThread(DeviceMonitorViewModel viewModel) {
        mDeviceMonitorViewModel = viewModel;
        mMovieLibraryRoomDatabase = MovieLibraryRoomDatabase.getDatabase(viewModel.getApplication());
        mDeviceDao = mMovieLibraryRoomDatabase.getDeviceDao();
        mVideoFileDao = mMovieLibraryRoomDatabase.getVideoFileDao();
    }

    /**
     * 获取本地设备后，启动设备挂载线程处理。
     */
    @Override
    public void run() {
        super.run();
        //清除数据库设备
        mDeviceDao.deleteAll();
        LogUtil.v(TAG, "=============>挂载设备:");

        String internelStorage = StorageHelper.getFlashStoragePath(mDeviceMonitorViewModel.getApplication());
        List<String> allCardPaths = StorageHelper.getSdCardPaths(mDeviceMonitorViewModel.getApplication());
        List<String> allUsbPaths = StorageHelper.getUSBPaths(mDeviceMonitorViewModel.getApplication());
        List<String> allPciePaths = StorageHelper.getPciePaths(mDeviceMonitorViewModel.getApplication());
        List<String> allHardDiskPaths = StorageHelper.getHardDiskPaths(mDeviceMonitorViewModel.getApplication());

        //扫描sd卡
        if (allCardPaths != null && allCardPaths.size() > 0) {
            for (String path : allCardPaths) {
                String deviceName = path.substring(path.lastIndexOf("/") + 1);
                int type = Constants.DeviceType.DEVICE_TYPE_SDCARDS;
                int state = Constants.DeviceMountState.MOUNTED;
                mDeviceMonitorViewModel.executeOnMountThread(deviceName, type, path, false, "", state);
            }
        }

        //扫描USB设备
        if (allUsbPaths != null && allUsbPaths.size() > 0) {
            for (String path : allUsbPaths) {
                String deviceName = path.substring(path.lastIndexOf("/") + 1);
                int type = Constants.DeviceType.DEVICE_TYPE_USB;
                int state = Constants.DeviceMountState.MOUNTED;
                mDeviceMonitorViewModel.executeOnMountThread(deviceName, type, path, false, "", state);
            }
        }

        //扫描PCIE设备
        if (allPciePaths != null && allPciePaths.size() > 0) {
            for (String path : allPciePaths) {
                String deviceName = path.substring(path.lastIndexOf("/") + 1);
                int type = Constants.DeviceType.DEVICE_TYPE_PCIE;
                int state = Constants.DeviceMountState.MOUNTED;
                mDeviceMonitorViewModel.executeOnMountThread(deviceName, type, path, false, "", state);
            }
        }

        //扫描Hard Disk设备
        if (allHardDiskPaths != null && allHardDiskPaths.size() > 0) {
            for (String path : allHardDiskPaths) {
                String deviceName = path.substring(path.lastIndexOf("/") + 1);
                int type = Constants.DeviceType.DEVICE_TYPE_HARD_DISK;
                int state = Constants.DeviceMountState.MOUNTED;
                mDeviceMonitorViewModel.executeOnMountThread(deviceName, type, path, false, "", state);
            }
        }

        if (!TextUtils.isEmpty(internelStorage)) {
            //扫描内置存储
            String deviceName = internelStorage.substring(internelStorage.lastIndexOf("/") + 1);//TODO 验证Android 11上的可行性
            int type = Constants.DeviceType.DEVICE_TYPE_INTERNAL_STORAGE;
            int state = Constants.DeviceMountState.MOUNTED;
            mDeviceMonitorViewModel.executeOnMountThread(deviceName, type, internelStorage, false, "", state);
        }

        int ret = mVideoFileDao.deleteOutdated(System.currentTimeMillis());
        LogUtil.v(TAG, "delete outdated videofile " + ret);
    }
}
