package com.hphtv.movielibrary.activity;

import androidx.appcompat.app.AppCompatActivity;

import com.firelfy.util.LogUtil;
import com.hphtv.movielibrary.fragment.dialog.LoadingDialogFragment;

/**
 * author: Sam Leung
 * date:  2021/6/11
 */
public class BaseActivity extends AppCompatActivity {
    public final String TAG = this.getClass().getSimpleName();
    LoadingDialogFragment mLoadingDialogFragment;
    protected void startLoading() {
        LogUtil.v(TAG,"startLoading");
        if(mLoadingDialogFragment==null){
            mLoadingDialogFragment=new LoadingDialogFragment();
            mLoadingDialogFragment.show(getSupportFragmentManager(),TAG);
        }

    }

    protected void stopLoading() {
        LogUtil.v(TAG, "stopLoading");
        if(mLoadingDialogFragment!=null){
            mLoadingDialogFragment.dismiss();
            mLoadingDialogFragment=null;
        }
    }

}
