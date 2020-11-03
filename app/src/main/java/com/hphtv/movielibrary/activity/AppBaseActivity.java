package com.hphtv.movielibrary.activity;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;

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
    protected DeviceMonitorService mDeviceMonitorService;
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
                DeviceMonitorService.MonitorBinder binder = (DeviceMonitorService.MonitorBinder) service;
                mDeviceMonitorService = binder.getService();
                mDeviceMonitorService.setOnDevcieChangeListener(list -> OnDeviceChange(list));
                OnDeviceMonitorServiceConnect(mDeviceMonitorService);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mDeviceMonitorService =null;
            }
        };
    }

    @Override
    protected void onPause() {
        super.onPause();
        unBindServices();
    }

    @Override
    protected void onResume() {
        super.onResume();
        attachServices();
    }



    public abstract void OnDeviceChange(List<Device> deviceList);
    public abstract void OnDeviceMonitorServiceConnect(DeviceMonitorService service);


    private void attachServices() {
        Intent intent = new Intent(this, DeviceMonitorService.class);
        bindService(intent, mServiceConnection, Service.BIND_AUTO_CREATE);
    }

    private void unBindServices() {
        unbindService(mServiceConnection);
    }

    public MovieApplication getApp(){
        return MovieApplication.getInstance();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
