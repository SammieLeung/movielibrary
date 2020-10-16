package com.hphtv.movielibrary;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.util.Log;
import android.webkit.WebView;

import com.hphtv.movielibrary.service.MovieScanService;
import com.hphtv.movielibrary.util.DoubanMovieSearchHelper;
import com.hphtv.movielibrary.util.MovieSharedPreferences;
import com.umeng.analytics.MobclickAgent;

import java.util.LinkedList;
import java.util.List;

public class MovieApplication extends Application {
    public static final boolean DEBUG = true;
    public static final String TAG =MovieApplication.class.getSimpleName();
    private boolean isShowEncrypted = false;
    private DoubanMovieSearchHelper helper;
    private WebView webview;
    private String cachePath;
    private static final String APP_CACHE_DIRNAME = "/webcache";
    private List<Activity> activitys = new LinkedList<Activity>();

    @Override
    public void onCreate() {
        super.onCreate();
        webview = new WebView(MovieApplication.this);
        MovieSharedPreferences.getInstance().setContext(MovieApplication.this);
        cachePath = getFilesDir().getAbsolutePath()
                + APP_CACHE_DIRNAME;
        helper = DoubanMovieSearchHelper.getInstance();
        helper.setContext(MovieApplication.this);
        helper.setmWebview(webview);
        //开启数据缓存
        helper.initWebView(true, cachePath);
//        Intent service = new Intent(MovieApplication.this, MovieScanService.class);
//        startService(service);
        //友盟统计
        MobclickAgent.setScenarioType(this, MobclickAgent.EScenarioType.E_UM_NORMAL);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    public DoubanMovieSearchHelper getSearchHelper() {
        return helper;
    }

    // 添加Activity到容器中
    public void addActivity(Activity activity) {
        if (activitys != null && activitys.size() > 0) {
            if (!activitys.contains(activity)) {
                activitys.add(activity);
            }
        } else {
            activitys.add(activity);
        }
    }

    public void removeActivity(Activity activity) {
        if (activitys != null && activitys.size() > 0) {
            if (activitys.contains(activity)) {
                activitys.remove(activity);
            }
        }
    }



    // 遍历所有Activity并finish
    public void exit() {
        if (activitys != null && activitys.size() > 0) {
            for (Activity activity : activitys) {
                activity.finish();
            }
        }
        System.exit(0);
    }

    public void moveToBack(Activity context) {
        context.moveTaskToBack(true);
    }

    public boolean isShowEncrypted() {
        return isShowEncrypted;
    }

    public void setShowEncrypted(boolean showEncrypted) {
        isShowEncrypted = showEncrypted;
    }
}
