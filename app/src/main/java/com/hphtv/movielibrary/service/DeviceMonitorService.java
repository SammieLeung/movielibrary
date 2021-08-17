package com.hphtv.movielibrary.service;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.station.kit.util.LogUtil;
import com.hphtv.movielibrary.data.ConstData;
import com.hphtv.movielibrary.viewmodel.DeviceMonitorViewModel;
import com.station.kit.util.StorageHelper;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author lxp
 * @date 19-3-26
 */
public class DeviceMonitorService extends Service {
    public static final String TAG = DeviceMonitorService.class.getSimpleName();
    private MonitorBinder mBinder;
    private DeviceMonitorViewModel mDeviceMonitorViewModel;
    private StorageManager mStorageManager;
    private MovieScanService2 mMovieScanService;
    /**
     * 处理Android 11设备挂载/卸载回调
     */
    Object mStorageVolumeCallback;

    /**
     * 1.监听设备挂载广播
     * 2.初始化线程池
     * 3.扫描全盘
     */
    @Override
    public void onCreate() {
        LogUtil.v(TAG, "OnCreate");
        super.onCreate();
        mDeviceMonitorViewModel = new ViewModelProvider.AndroidViewModelFactory(getApplication()).create(DeviceMonitorViewModel.class);
        bindRegisterReceivers();
        bindServices();

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
    public void onDestroy() {
        LogUtil.v(TAG, "onDestroy");
        super.onDestroy();
        unRegisterReceivers();
        unbindServices();
    }

    private void bindServices() {
        Intent intent = new Intent();
        intent.setClass(this, MovieScanService2.class);
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
                    int state = state_txt.equals(ConstData.DeviceMountState.MOUNTED_TEXT) ? ConstData.DeviceMountState.MOUNTED : ConstData.DeviceMountState.UNMOUNTED;
                    if (state == ConstData.DeviceMountState.MOUNTED) {
                        mDeviceMonitorViewModel.executeOnMountThread(volume.getUuid(), ConstData.DeviceType.DEVICE_TYPE_LOCAL, volume.getDirectory().getPath(), false, "", state);
                    } else {
                        //拔出U盘的时候volume.getDirectory()返回的是null，所以被迫使用反射获取mPath的值
                        try {
                            Class clazz = volume.getClass();
                            Field field_mPath = clazz.getDeclaredField("mPath");
                            field_mPath.setAccessible(true);
                            File fpath = (File) field_mPath.get(volume);
                            String path = fpath.toString();
                            mDeviceMonitorViewModel.executeOnMountThread(volume.getUuid(), ConstData.DeviceType.DEVICE_TYPE_LOCAL, path, false, "", state);
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
        localFilter.addAction(ConstData.BroadCastMsg.DEVICE_UP);
        localFilter.addAction(ConstData.BroadCastMsg.DEVICE_DOWN);
        localFilter.addAction(ConstData.BroadCastMsg.RESCAN_DEVICE);
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


    public DeviceMonitorViewModel getDeviceMonitorViewModel() {
        return mDeviceMonitorViewModel;
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
                    int state = state_txt.equals(ConstData.DeviceMountState.MOUNTED_TEXT) ? ConstData.DeviceMountState.MOUNTED : ConstData.DeviceMountState.UNMOUNTED;
                    //Android 11以上
                    try {
                        Class clazz = storageVolume.getClass();
                        Method meth = clazz.getDeclaredMethod("getPath");
                        meth.setAccessible(true);
                        String path = (String) meth.invoke(storageVolume);
                        mDeviceMonitorViewModel.executeOnMountThread(storageVolume.getUuid(), ConstData.DeviceType.DEVICE_TYPE_LOCAL, path, false, "", state);
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
                case ConstData.BroadCastMsg.DEVICE_UP:
                    String mountPath = intent.getStringExtra(ConstData.DeviceMountMsg.DEVICE_MOUNT_PATH);
                    LogUtil.v("currentPath=" + mountPath);
                    mDeviceMonitorViewModel.startScanWithNotScannedFiles(mMovieScanService, mountPath);
                    break;
                case ConstData.BroadCastMsg.DEVICE_DOWN:
                    break;
                case ConstData.BroadCastMsg.RESCAN_DEVICE:
                  mDeviceMonitorViewModel.reScanDevices();
                    break;
            }
        }
    };

    ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mMovieScanService = ((MovieScanService2.ScanBinder) service).getService();
            mDeviceMonitorViewModel.scanDevices();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    public class MonitorBinder extends Binder {
        public DeviceMonitorService getService() {
            return DeviceMonitorService.this;
        }
    }

}
