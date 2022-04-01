package com.hphtv.movielibrary.ui.homepage;

import android.content.Intent;

import androidx.activity.result.ActivityResult;

import com.hphtv.movielibrary.ui.AppBaseActivity;

/**
 * author: Sam Leung
 * date:  2022/2/21
 */
public interface IActivityResult {
    void startActivityForResult(Intent data);
    //强制刷新
    void forceRefresh();
}
