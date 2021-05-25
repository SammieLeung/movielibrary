package com.hphtv.movielibrary.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.storage.StorageVolume;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.hphtv.movielibrary.MovieApplication;
import com.hphtv.movielibrary.data.ConstData;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.DeviceDao;
import com.hphtv.movielibrary.roomdb.dao.VideoFileDao;
import com.hphtv.movielibrary.roomdb.entity.Device;
import com.hphtv.movielibrary.service.Thread.DeviceCheckThread;
import com.hphtv.movielibrary.service.Thread.DeviceInitThread;
import com.hphtv.movielibrary.service.Thread.FileScanThread;
import com.hphtv.movielibrary.util.LogUtil;
import com.hphtv.movielibrary.util.StorageList;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author lxp
 * @date 19-3-26
 */
public class DeviceMonitorService extends Service {
    public static final String TAG = DeviceMonitorService.class.getSimpleName();
    private MonitorBinder mBinder;
    /**
     * 单线程池服务，设备挂载，卸载线程
     */
    private ExecutorService mDeviceMountExecutor;
    /**
     * 单线程池服务，设备初始化检测服务
     */
    private ExecutorService mDeviceInitExecutor;
    private ExecutorService mFileScanExecutor;

    private DeviceDao mDeviceDao;
    private VideoFileDao mVideoFileDao;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if (mBinder == null)
            mBinder = new MonitorBinder();
        return mBinder;
    }

    public class MonitorBinder extends Binder {
        public DeviceMonitorService getService() {
            return DeviceMonitorService.this;
        }
    }

    /**
     * 1.监听设备挂载广播
     * 2.初始化线程池
     * 3.扫描全盘
     */
    @Override
    public void onCreate() {
        super.onCreate();
        initDb();
        bindRegisterReceivers();
        initThreadPools();
        scanDevices();
    }

    private void initDb() {
        MovieLibraryRoomDatabase movieLibraryRoomDatabase = MovieLibraryRoomDatabase.getDatabase(this);
        mDeviceDao = movieLibraryRoomDatabase.getDeviceDao();
        mVideoFileDao = movieLibraryRoomDatabase.getVideoFileDao();
    }

    /**
     * 注册广播
     */
    private void bindRegisterReceivers() {
        IntentFilter newFilter = new IntentFilter();
        newFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        newFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        newFilter.addDataScheme("file");
        registerReceiver(mDeviceMountReceiver, newFilter);
    }

    //初始化线程池
    private void initThreadPools() {
        mDeviceMountExecutor = Executors.newSingleThreadExecutor();
        mFileScanExecutor = new ThreadPoolExecutor(5, 5, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
        mDeviceInitExecutor = Executors.newSingleThreadExecutor();
    }

    //扫描全盘
    private void scanDevices() {
        mDeviceInitExecutor.execute(new DeviceInitThread(this));
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

    /**
     * 搜索设备
     */
    public void checkDevices() {
        int isEncrypted;
        if (((MovieApplication) getApplication()).isShowEncrypted()) {
            isEncrypted = ConstData.EncryptState.ENCRYPTED;
        } else {
            isEncrypted = ConstData.EncryptState.UNENCRYPTED;
        }
        DeviceCheckThread deviceCheckThread = new DeviceCheckThread(this, list -> {
            if (mDeviceChangeListener != null)
                mDeviceChangeListener.OnDeviceChange(list);
        }, isEncrypted);
        deviceCheckThread.execute();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mDeviceMountReceiver);
    }

    /**
     * 接受设备挂载广播
     */
    BroadcastReceiver mDeviceMountReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(TAG, intent.getAction());
            switch (intent.getAction()) {
                case Intent.ACTION_MEDIA_MOUNTED:
                case Intent.ACTION_MEDIA_UNMOUNTED:
                    StorageVolume storageVolume = intent.getParcelableExtra(StorageVolume.EXTRA_STORAGE_VOLUME);
                    Log.v(TAG, "========storageVolume=========");

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        String state_txt = storageVolume.getState();
                        int state = state_txt.equals(ConstData.DeviceMountState.MOUNTED_TEXT) ? ConstData.DeviceMountState.MOUNTED : ConstData.DeviceMountState.UNMOUNTED;


                        try {
                            Class clazz = storageVolume.getClass();
                            Method meth = clazz.getDeclaredMethod("getPath");
                            meth.setAccessible(true);
                            String filepath = (String) meth.invoke(storageVolume);
//                            if (deviceChangeListener != null)
//                                deviceChangeListener.OnDeviceChange(storageVolume.getUuid(), ConstData.DeviceType.DEVICE_TYPE_LOCAL, filepath, false, "", state);
                            checkDevices();
                        } catch (NoSuchMethodException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                    break;

            }
        }
    };

    /**
     * Todo 完善文件搜索
     * 设备挂载处理。
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
            List<Device> deviceList = mDeviceDao.querybyMountPath(mMountPath);
            if (deviceList != null && deviceList.size() > 0) {
                for (Device device : deviceList) {
                    mVideoFileDao.deleteByDeviceId(device.id);
                    mDeviceDao.deleteDevices(device);//删除设备
                }
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
//
//
//                //启动文件扫描线程。
                mFileScanExecutor.execute(new FileScanThread(DeviceMonitorService.this, device));
            } else {
                broadIntent.setAction(ConstData.BroadCastMsg.DEVICE_DOWN);
            }

            LocalBroadcastManager.getInstance(DeviceMonitorService.this).sendBroadcast(broadIntent);
        }


        private Device buildDeviceFromPath(String mountPath, String netWorkPath, int deviceType, String name) {
            Device device = new Device();
            File file = new File(mountPath);
            if (!file.exists())//不是本地挂载设备返回null
                return null;
            device.localPath = mountPath;
            device.name = name;
            device.networkPath = netWorkPath;

            if (deviceType == ConstData.DeviceType.DEVICE_TYPE_LOCAL) {
                if (StorageList.getInstance().isMountUsb(mountPath)) {
                    device.type = ConstData.DeviceType.DEVICE_TYPE_USB;
                } else if (StorageList.getInstance().isMountSdCard(mountPath)) {
                    device.type = (ConstData.DeviceType.DEVICE_TYPE_SDCARDS);
                } else {
                    device.type = (ConstData.DeviceType.DEVICE_TYPE_INTERNAL_STORAGE);
                }
            } else {
                device.type = (deviceType);
            }


            return device;
        }
    }


    public interface OnDeviceChange {
        void OnDeviceChange(List list);
    }

    private OnDeviceChange mDeviceChangeListener;

    public void setOnDevcieChangeListener(OnDeviceChange listener) {
        mDeviceChangeListener = listener;
    }
}
