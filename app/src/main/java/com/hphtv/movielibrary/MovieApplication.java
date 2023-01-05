package com.hphtv.movielibrary;

import android.app.Application;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.archos.filecorelibrary.filecorelibrary.jcifs.JcifsUtils;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.stetho.Stetho;
import com.firefly.filepicker.utils.SambaAuthHelper;
import com.hphtv.movielibrary.data.AuthHelper;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.device.DeviceBindStateApiService;
import com.hphtv.movielibrary.util.ScraperSourceTools;
import com.hphtv.movielibrary.util.rxjava.RxJavaGcManager;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;
import com.umeng.analytics.MobclickAgent;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MovieApplication extends Application {
    public static final boolean DEBUG = true;
    public static final String TAG = MovieApplication.class.getSimpleName();
    private boolean isShowEncrypted = false;
    public static boolean hasNetworkConnection = false;
    private static MovieApplication sMovieApplication;
    private ConnectivityManager mConnectivityManager;
    private int mDeviceBindState = -1;

    private ConnectivityManager.NetworkCallback mNetworkCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(@NonNull Network network) {
            super.onAvailable(network);
            synchronized (sMovieApplication) {
                hasNetworkConnection = true;
                mDeviceBindState = -1;
            }
            ObservableSource<String> getCNAuthToken = observer -> {
                if (TextUtils.isEmpty(AuthHelper.sTokenCN)) {
                    AuthHelper.requestTokenCN();
                }
                observer.onNext(AuthHelper.sTokenCN);
                observer.onComplete();
            };

            ObservableSource<String> getEnAuthToken = observer -> {
                if (TextUtils.isEmpty(AuthHelper.sTokenEN)) {
                    AuthHelper.requestTokenEN();
                }
                observer.onNext(AuthHelper.sTokenEN);
                observer.onComplete();
            };

            Observable.zip(getCNAuthToken, getEnAuthToken, (cn, en) -> ScraperSourceTools.getSource()).subscribeOn(Schedulers.newThread())
                    .subscribe(new SimpleObserver<String>() {
                        @Override
                        public void onAction(String source) {
                            Intent intent = new Intent();
                            intent.setAction(Constants.ACTION_NETWORK_AVAILABLE);
                            LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(intent);
                        }
                    });

        }

        @Override
        public void onLost(@NonNull Network network) {
            super.onLost(network);
            synchronized (sMovieApplication) {
                hasNetworkConnection = false;
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        sMovieApplication = this;
        init();
        registerNetworkCallback();
    }

    public void registerNetworkCallback() {
        if (mConnectivityManager == null)
            mConnectivityManager = getSystemService(ConnectivityManager.class);
        mConnectivityManager.registerDefaultNetworkCallback(mNetworkCallback);
    }

    public void unregisterNetworkCallback() {
        if (mConnectivityManager != null) {
            mConnectivityManager.unregisterNetworkCallback(mNetworkCallback);
            mConnectivityManager = null;
        }
    }

    private void init() {

        Observable.just("")
                .observeOn(Schedulers.newThread())
                .subscribe(new SimpleObserver<String>() {
                    @Override
                    public void onAction(String s) {
                        //动图库
                        Fresco.initialize(getApplicationContext());
                        //初始化stetho
                        Stetho.initializeWithDefaults(getBaseContext());
                        //友盟统计
                        MobclickAgent.setScenarioType(sMovieApplication, MobclickAgent.EScenarioType.E_UM_NORMAL);
                        JcifsUtils.getInstance(MovieApplication.this);
                        SambaAuthHelper.getInstance().init(MovieApplication.this);

                        //设备TOKEN
//                        AuthHelper.init();
                    }
                });
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        RxJavaGcManager.getInstance().clearDisposable();
        unregisterNetworkCallback();
    }


    public boolean isShowEncrypted() {
        return isShowEncrypted;
    }

    public void setShowEncrypted(boolean showEncrypted) {
        isShowEncrypted = showEncrypted;
    }

    public static MovieApplication getInstance() {
        return sMovieApplication;
    }

    public boolean isDeviceBound() {
        return isDeviceBound(false);
    }

    public boolean isDeviceBound(boolean forceRresh) {
        if (mDeviceBindState == -1 || forceRresh)
            mDeviceBindState = DeviceBindStateApiService.getBindState();
        return mDeviceBindState == 1;
    }

}
