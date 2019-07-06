package com.hphtv.movielibrary.activity;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.hphtv.movielibrary.MovieApplication;
import com.hphtv.movielibrary.service.DeviceMonitorService;
import com.hphtv.movielibrary.sqlite.bean.Device;

import java.util.List;

/**
 * @author lxp
 * @date 19-3-26
 */
public abstract class AppBaseActivity extends Activity {
    public static final String TAG = AppBaseActivity.class.getSimpleName();
    protected DeviceMonitorService mService;
    private AppBaseActivity mContext;
    private ServiceConnection mServiceConnection;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    /**
     * 初始化
     */
    private void init() {
        mContext=this;
        mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.v(TAG,"onServiceConneted "+name );
                DeviceMonitorService.MonitorBinder binder = (DeviceMonitorService.MonitorBinder) service;
                mService = binder.getService();
                mService.setOnDevcieChangeListener(new DeviceMonitorService.OnDeviceChange() {
                    @Override
                    public void OnDeviceChange(List list) {
                        mContext.OnDeviceChange(mContext, list);
                    }
                });
                OnDeviceMonitorServiceConnect(mService);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mService=null;
            }
        };
    }

    @Override
    protected void onPause() {
        super.onPause();
//        LocalBroadcastManager.getInstance(AppBaseActivity.this).unregisterReceiver(mDeviceReceiver);
        unBindServices();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        IntentFilter deviceListIntentFilter = new IntentFilter();
//        deviceListIntentFilter.addAction(ConstData.BoardCastMsg.ACTION_DEVICE_LIST_REFRESH);
//        LocalBroadcastManager.getInstance(AppBaseActivity.this).registerReceiver(mDeviceReceiver, deviceListIntentFilter);
        attachServices();
    }



    public abstract void OnDeviceChange(Context context,List<Device> deviceList);
    public abstract void OnDeviceMonitorServiceConnect(DeviceMonitorService service);

//    private class DeviceListReceiver extends BroadcastReceiver {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            Log.v(TAG, "LocalBroadCastMsg intent.getAction() " + intent.getAction());
//           List<Device> deviceList = (List<Device>) intent.getSerializableExtra(ConstData.MapKey.KEY_DEVICE_LIST);
//            OnDeviceChange(context, deviceList);
//        }
//    }

//    private DeviceListReceiver mDeviceReceiver = new DeviceListReceiver();

    private void attachServices() {
        Log.v(TAG,"attachServices");
        Intent intent = new Intent(this, DeviceMonitorService.class);
        bindService(intent, mServiceConnection, Service.BIND_AUTO_CREATE);
    }

    private void unBindServices() {
        Log.v(TAG,"unBindServices");
        unbindService(mServiceConnection);
    }

    public MovieApplication getApp(){
        return (MovieApplication) getApplication();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
