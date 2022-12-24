package com.hphtv.movielibrary.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.AndroidViewModel;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.hphtv.movielibrary.MovieApplication;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.util.ServiceStatusHelper;
import com.station.kit.util.LogUtil;
import com.station.kit.view.mvvm.activity.BaseInflateActivity;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author lxp
 * @date 19-3-26
 */
public abstract class AppBaseActivity<VM extends AndroidViewModel, VDB extends ViewDataBinding> extends BaseInflateActivity<VM, VDB> implements IRemoteRefresh {

    private static Handler mHanlder = new Handler(Looper.getMainLooper());
    public final String TAG = this.getClass().getSimpleName();
    LoadingDialogFragment mLoadingDialogFragment;
    private ActivityResultLauncher mActivityResultLauncher;

    private AtomicInteger mAtomicLoading = new AtomicInteger();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityResultContracts.StartActivityForResult startActivityForResult = new ActivityResultContracts.StartActivityForResult();
        mActivityResultLauncher = registerForActivityResult(startActivityForResult, result -> {
            Log.v(AppBaseActivity.this.getClass().getSimpleName(), "onActivityResult resultCode=" + result.getResultCode());
            onActivityResultCallback(result);
        });
    }

    protected void onActivityResultCallback(ActivityResult result) {

    }

    public void startActivityForResult(Intent intent) {
        mActivityResultLauncher.launch(intent);
    }


    @Override
    protected void onPause() {
        super.onPause();
        mHanlder.removeCallbacksAndMessages(null);
        ServiceStatusHelper.pauseView();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mHanlder.postDelayed(() -> ServiceStatusHelper.resumeView(), 100);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.ACTION.MOVIE_SCRAP_START);
        intentFilter.addAction(Constants.ACTION.MOVIE_SCRAP_STOP_AND_REFRESH);
        intentFilter.addAction(Constants.ACTION_FAVORITE_MOVIE_CHANGE_NOTIFY);
        intentFilter.addAction(Constants.ACTION_APP_UPDATE_MOVIE);
        intentFilter.addAction(Constants.ACTION_APP_REMOVE_MOVIE);

        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, intentFilter);
    }

    public MovieApplication getApp() {
        return MovieApplication.getInstance();
    }

    public void startLoading() {
        int i = mAtomicLoading.incrementAndGet();
        LogUtil.v(TAG, "startLoading " + i);
        if (mLoadingDialogFragment == null) {
            mLoadingDialogFragment = new LoadingDialogFragment();
            mLoadingDialogFragment.show(getSupportFragmentManager(), TAG);
        }
    }

    public void stopLoading() {
        if (mAtomicLoading.decrementAndGet() <= 0) {
            if (mLoadingDialogFragment != null) {
                LogUtil.v(TAG, "stopLoading");
                mLoadingDialogFragment.dismiss();
                mLoadingDialogFragment = null;
                mAtomicLoading.set(0);
            }
        }
    }

    public void forceStopLoading() {
        if (mLoadingDialogFragment != null) {
            LogUtil.v(TAG, "stopLoading");
            mLoadingDialogFragment.dismiss();
            mLoadingDialogFragment = null;
            mAtomicLoading.set(0);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        stopLoading();
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case Constants.ACTION.MOVIE_SCRAP_START:
                    break;
                case Constants.ACTION.MOVIE_SCRAP_STOP_AND_REFRESH:
                    movieScrapeFinish();
                    break;
                case Constants.ACTION_FAVORITE_MOVIE_CHANGE_NOTIFY:
                    remoteUpdateFavoriteNotify(
                            intent.getStringExtra("movie_id"),
                            intent.getStringExtra("type"),
                            intent.getBooleanExtra("is_favorite", false));
                    break;
                case Constants.ACTION_APP_UPDATE_MOVIE:
                    remoteUpdateMovieNotify(
                            intent.getLongExtra("new", -1),
                            intent.getLongExtra("old", -1));
                    break;
                case Constants.ACTION_APP_REMOVE_MOVIE:
                    remoteRemoveMovieNotify(
                            intent.getStringExtra("movie_id"),
                            intent.getStringExtra("type"));
                    break;

            }
        }
    };

    protected void movieScrapeFinish() {
    }

    ;

    @Override
    public void remoteUpdateFavoriteNotify(String movie_id, String type, boolean isFavorite) {

    }

    @Override
    public void remoteUpdateMovieNotify(long o_id, long n_id) {

    }

    @Override
    public void remoteRemoveMovieNotify(String movie_id, String type) {

    }
}
