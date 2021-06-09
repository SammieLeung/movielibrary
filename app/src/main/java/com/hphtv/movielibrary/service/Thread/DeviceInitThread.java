package com.hphtv.movielibrary.service.Thread;

import com.firelfy.util.LogUtil;
import com.firelfy.util.StorageList;
import com.hphtv.movielibrary.data.ConstData;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.DeviceDao;
import com.hphtv.movielibrary.roomdb.dao.VideoFileDao;
import com.hphtv.movielibrary.service.DeviceMonitorService;


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

    public DeviceInitThread(DeviceMonitorService service) {
        mDeviceMonitorService =service;
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
        //清除数据库设备
        mDeviceDao.deleteAll();
//        String[] paths=StorageList.getInstance().getVolumnPaths();
//        if(paths!=null){
//            for(String path:paths){
//                LogUtil.v("StorageList path="+path);
//                LogUtil.v("mountState "+StorageList.getInstance().isVolumeMounted(path));
//            }
//        }
        LogUtil.v(TAG, "=============>挂载设备:");
        String internelStorage = StorageList.getInstance().getFlashStoragePath();
        LogUtil.v("internelStorage = "+internelStorage);
        List<String> allCardPaths = StorageList.getInstance().getSdCardPaths();
        List<String> allUsbPaths = StorageList.getInstance().getUSBPaths();

        //扫描sd卡
        if (allCardPaths != null && allCardPaths.size() > 0) {
            for (String path : allCardPaths) {
                String deviceName = path.substring(path.lastIndexOf("/") + 1);
                int type = ConstData.DeviceType.DEVICE_TYPE_SDCARDS;
                int state = ConstData.DeviceMountState.MOUNTED;
                mDeviceMonitorService.executeOnMountThread(deviceName, type, path, false, "", state);
            }
        }
        //扫描USB设备
        if (allUsbPaths != null && allUsbPaths.size() > 0) {
            for (String path : allUsbPaths) {
                String deviceName = path.substring(path.lastIndexOf("/") + 1);
                int type = ConstData.DeviceType.DEVICE_TYPE_USB;
                int state = ConstData.DeviceMountState.MOUNTED;
                mDeviceMonitorService.executeOnMountThread(deviceName, type, path, false, "", state);
            }
        }
        //扫描内置存储
        String deviceName = internelStorage.substring(internelStorage.lastIndexOf("/") + 1);//TODO 验证Android 11上的可行性
        int type = ConstData.DeviceType.DEVICE_TYPE_INTERNAL_STORAGE;
        int state = ConstData.DeviceMountState.MOUNTED;
        mDeviceMonitorService.executeOnMountThread(deviceName, type, internelStorage, false, "", state);
    }
}
