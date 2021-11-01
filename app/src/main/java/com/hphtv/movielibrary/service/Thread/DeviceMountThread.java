package com.hphtv.movielibrary.service.Thread;

import android.content.Context;
import android.content.Intent;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.DeviceDao;
import com.hphtv.movielibrary.roomdb.entity.Device;
import com.station.kit.util.LogUtil;
import com.station.kit.util.StorageHelper;

import java.io.File;

/**
 * author: Sam Leung
 * date:  2021/10/29
 */
public class DeviceMountThread extends Thread {
    private String mDeviceName;
    private int mDeviceType;
    private String mMountPath;
    private boolean isNetwork;
    private String mNetworkPath;
    private int mMountState;
    private DeviceDao mDeviceDao;
    private Context mContext;

    public DeviceMountThread(Context context, String deviceName, int deviceType, String mountPath, boolean isFromNetwork, String networkPath, int mountState) {
        mContext=context;
        mDeviceName = deviceName;
        mDeviceType = deviceType;
        mMountPath = mountPath;
        isNetwork = isFromNetwork;
        mNetworkPath = networkPath;
        mMountState = mountState;
        mDeviceDao = MovieLibraryRoomDatabase.getDatabase(mContext).getDeviceDao();
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
        broadIntent.putExtra(Constants.Extras.DEVICE_MOUNT_PATH, mMountPath);
        broadIntent.putExtra(Constants.Extras.MOUNT_TYPE, mDeviceType);
        broadIntent.putExtra(Constants.Extras.DEVICE_STATE, mMountState);
        broadIntent.putExtra(Constants.Extras.IS_FROM_NETWORK, false);//TODO hard code
        //设备状态为mounted则将设备信息入库，并扫描设备文件。
        Device device_db = mDeviceDao.querybyMountPath(mMountPath);
        if (device_db != null) {
            mDeviceDao.deleteDevices(device_db);//删除设备
        }
        if (mMountState == Constants.DeviceMountState.MOUNTED) {
            Device device = buildDeviceFromPath(mMountPath, mNetworkPath, mDeviceType, mDeviceName);//生成一个基础设备
            if (device == null)
                return;
            //设备入库
            mDeviceDao.insertDevices(device);
            //保存设备id和设备路径的关联信息
            Constants.devicePathIDs.put(mMountPath, device.id);//

            broadIntent.setAction(Constants.BroadCastMsg.DEVICE_UP);
            broadIntent.putExtra(Constants.Extras.DEVICE_ID, device.id);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(broadIntent);
        } else {
            Constants.devicePathIDs.remove(mMountPath);
            broadIntent.setAction(Constants.BroadCastMsg.DEVICE_DOWN);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(broadIntent);
        }
        LogUtil.v("mount " + mDeviceName + " finish");

    }

    /**
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
        device.path = mountPath;
        device.name = name;

        //获取设备具体类型
        if (deviceType == Constants.DeviceType.DEVICE_TYPE_LOCAL) {
            if (StorageHelper.isMountUsb(mContext, mountPath)) {
                device.type = Constants.DeviceType.DEVICE_TYPE_USB;
            } else if (StorageHelper.isMountSdCard(mContext, mountPath)) {
                device.type = Constants.DeviceType.DEVICE_TYPE_SDCARDS;
            } else if (StorageHelper.isMountHardDisk(mContext, mountPath)) {
                device.type = Constants.DeviceType.DEVICE_TYPE_HARD_DISK;
            } else if (StorageHelper.isMountPcie(mContext, mountPath)) {
                device.type = Constants.DeviceType.DEVICE_TYPE_PCIE;
            } else {
                device.type = Constants.DeviceType.DEVICE_TYPE_INTERNAL_STORAGE;
            }
        } else {
            device.type = deviceType;
        }
        return device;
    }
}
