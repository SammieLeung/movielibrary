package com.hphtv.movielibrary;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;

import com.hphtv.movielibrary.service.DeviceMonitorService;
import com.hphtv.movielibrary.util.rxjava.RxJavaGcManager;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;
import com.umeng.analytics.MobclickAgent;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;

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
