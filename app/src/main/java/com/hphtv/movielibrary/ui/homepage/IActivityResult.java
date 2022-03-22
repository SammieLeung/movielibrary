package com.hphtv.movielibrary.ui.homepage;

import android.content.Intent;

import androidx.activity.result.ActivityResult;

/**
 * author: Sam Leung
 * date:  2022/2/21
 */
public interface IActivityResult {
    void onActivityResult(ActivityResult result);
    void forceRefresh();
}
