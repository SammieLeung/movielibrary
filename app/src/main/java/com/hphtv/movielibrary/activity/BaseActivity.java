package com.hphtv.movielibrary.activity;


import androidx.appcompat.app.AppCompatActivity;

import com.station.kit.util.LogUtil;
import com.hphtv.movielibrary.fragment.dialog.LoadingDialogFragment;

/**
 * author: Sam Leung
 * date:  2021/6/11
 */
public class BaseActivity extends AppCompatActivity {
    public final String TAG = this.getClass().getSimpleName();
    LoadingDialogFragment mLoadingDialogFragment;
    public void startLoading() {
        LogUtil.v(TAG,"startLoading");
        if(mLoadingDialogFragment==null){
            mLoadingDialogFragment=new LoadingDialogFragment();
            mLoadingDialogFragment.show(getSupportFragmentManager(),TAG);
        }

    }

    public void stopLoading() {
        if(mLoadingDialogFragment!=null){
            LogUtil.v(TAG, "stopLoading");
            mLoadingDialogFragment.dismiss();
            mLoadingDialogFragment=null;
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        stopLoading();
    }
}
