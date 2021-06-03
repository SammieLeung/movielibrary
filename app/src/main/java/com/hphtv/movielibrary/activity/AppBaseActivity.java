package com.hphtv.movielibrary.activity;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.hphtv.movielibrary.MovieApplication;
import com.hphtv.movielibrary.roomdb.entity.Device;
import com.hphtv.movielibrary.service.DeviceMonitorService;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * @author lxp
 * @date 19-3-26
 */
public abstract class AppBaseActivity<VM extends AndroidViewModel, VDB extends ViewDataBinding> extends AppCompatActivity {
    public static final String TAG = AppBaseActivity.class.getSimpleName();
    protected VDB mBinding;
    protected VM mViewModel;
    protected DeviceMonitorService mDeviceMonitorService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, getContentViewId());
        mBinding.setLifecycleOwner(this);
        createViewModel();
        processLogic();
        init();
    }

    /**
     * 返回layout
     *
     * @return
     */
    protected abstract int getContentViewId();

    /**
     * 处理onCreate()
     */
    protected abstract void processLogic();

    /**
     * 创建ViewModel
     */
    private void createViewModel() {
        if (mViewModel == null) {
            Class modelClass;
            Type type = getClass().getGenericSuperclass();
            if (type instanceof ParameterizedType) {
                modelClass = (Class) ((ParameterizedType) type).getActualTypeArguments()[0];
            } else {
                //如果没有指定泛型参数，则默认使用BaseViewModel
                modelClass = AndroidViewModel.class;
            }
            mViewModel = (VM) new ViewModelProvider(this).get(modelClass);
        }
    }

    /**
     * 初始化
     */
    private void init() {

    }

    @Override
    protected void onPause() {
        super.onPause();
//        unBindServices();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        attachServices();
    }



    private void attachServices() {
        Intent intent = new Intent(this, DeviceMonitorService.class);
        bindService(intent, mServiceConnection, Service.BIND_AUTO_CREATE);
    }

    private void unBindServices() {
        unbindService(mServiceConnection);
    }

    public MovieApplication getApp() {
        return MovieApplication.getInstance();
    }

    ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DeviceMonitorService.MonitorBinder binder = (DeviceMonitorService.MonitorBinder) service;
            mDeviceMonitorService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mDeviceMonitorService = null;
        }
    };

}
