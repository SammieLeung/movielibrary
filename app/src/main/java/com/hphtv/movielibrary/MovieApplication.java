package com.hphtv.movielibrary;

import android.app.Application;
import android.content.Intent;

import com.hphtv.movielibrary.service.DeviceMonitorService;
import com.hphtv.movielibrary.util.rxjava.RxJavaGcManager;
import com.umeng.analytics.MobclickAgent;

public class MovieApplication extends Application {
    public static final boolean DEBUG = true;
    public static final String TAG = MovieApplication.class.getSimpleName();
    private boolean isShowEncrypted = false;
    private static MovieApplication sMovieApplication;
    @Override
    public void onCreate() {
        super.onCreate();
        sMovieApplication =this;
        init();
    }

    private void init(){
        //友盟统计
        MobclickAgent.setScenarioType(sMovieApplication, MobclickAgent.EScenarioType.E_UM_NORMAL);
        Intent service=new Intent(sMovieApplication, DeviceMonitorService.class);
        startService(service);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        RxJavaGcManager.getInstance().clearDisposable();
    }


    public boolean isShowEncrypted() {
        return isShowEncrypted;
    }

    public void setShowEncrypted(boolean showEncrypted) {
        isShowEncrypted = showEncrypted;
    }

    public static MovieApplication getInstance(){
        return sMovieApplication;
    }
}
