package com.hphtv.movielibrary.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.hphtv.movielibrary.MovieApplication;
import com.hphtv.movielibrary.fragment.dialog.LoadingDialogFragment;
import com.station.kit.util.LogUtil;
import com.station.kit.view.activity.BaseInflateActivity;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author lxp
 * @date 19-3-26
 */
public abstract class AppBaseActivity<VM extends AndroidViewModel, VDB extends ViewDataBinding> extends BaseInflateActivity<VM, VDB> {

    public final String TAG = this.getClass().getSimpleName();
    LoadingDialogFragment mLoadingDialogFragment;
    private ActivityResultLauncher mActivityResultLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        processLogic();
        init();
        ActivityResultContracts.StartActivityForResult startActivityForResult = new ActivityResultContracts.StartActivityForResult();
        mActivityResultLauncher = registerForActivityResult(startActivityForResult, result -> {
            Log.v(AppBaseActivity.this.getClass().getSimpleName(), "onActivityResult resultCode=" + result.getResultCode());
            onActivityResultCallback(result);
        });
    }

    /**
     * 处理onCreate()
     */
    protected abstract void processLogic();

    protected void onActivityResultCallback(ActivityResult result) {

    }

    ;

    public void startActivityForResult(Intent intent) {
        mActivityResultLauncher.launch(intent);
    }

    /**
     * 初始化
     */
    private void init() {

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public MovieApplication getApp() {
        return MovieApplication.getInstance();
    }

    public void startLoading() {
        LogUtil.v(TAG, "startLoading");
        if (mLoadingDialogFragment == null) {
            mLoadingDialogFragment = new LoadingDialogFragment();
            mLoadingDialogFragment.show(getSupportFragmentManager(), TAG);
        }

    }

    public void stopLoading() {
        if (mLoadingDialogFragment != null) {
            LogUtil.v(TAG, "stopLoading");
            mLoadingDialogFragment.dismiss();
            mLoadingDialogFragment = null;
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        stopLoading();
    }
}
