package com.hphtv.movielibrary.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.storage.StorageVolume;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.hphtv.movielibrary.MovieApplication;
import com.hphtv.movielibrary.data.ConstData;
import com.hphtv.movielibrary.service.Thread.DeviceCheckThread;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @author lxp
 * @date 19-3-26
 */
public class DeviceMonitorService extends Service {
    public static final String TAG = DeviceMonitorService.class.getSimpleName();
    private MonitorBinder mBinder;


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

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter newFilter = new IntentFilter();
        newFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        newFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        newFilter.addDataScheme("file");
        registerReceiver(mountReceiver, newFilter);
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
        unregisterReceiver(mountReceiver);
    }

    /**
     * 接受设备挂载广播
     */
    BroadcastReceiver mountReceiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
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


    public Bundle getDeviceMountedBundle(String deviceName, int deviceType, String localPath, boolean isFromNetwork, String networkPath, int mountState) {
        Bundle bundle = new Bundle();
        if (deviceName == null)
            deviceName = "";
        if (localPath == null)
            localPath = "";
        if (networkPath == null)
            networkPath = "";
        bundle.putString(ConstData.DeviceMountMsg.DEVICE_NAME, deviceName);
        bundle.putInt(ConstData.DeviceMountMsg.MOUNT_DEVICE_STATE, mountState);
        bundle.putInt(ConstData.DeviceMountMsg.MOUNT_TYPE, deviceType);
        bundle.putString(ConstData.DeviceMountMsg.DEVICE_NETWORKPATH, networkPath);
        bundle.putBoolean(ConstData.DeviceMountMsg.IS_FROM_NETWORK, isFromNetwork);
        bundle.putString(ConstData.DeviceMountMsg.DEVICE_MOUNT_PATH, localPath);
        return bundle;
    }

    public interface OnDeviceChange {
         void OnDeviceChange(List list);
    }

    private OnDeviceChange mDeviceChangeListener;

    public void setOnDevcieChangeListener(OnDeviceChange listener) {
        mDeviceChangeListener = listener;
    }
}
