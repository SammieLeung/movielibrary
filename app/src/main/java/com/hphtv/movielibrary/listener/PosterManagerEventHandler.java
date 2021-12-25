package com.hphtv.movielibrary.listener;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.hphtv.movielibrary.activity.AppBaseActivity;
import com.hphtv.movielibrary.data.Constants;

/**
 * author: Sam Leung
 * date:  2021/12/10
 */
public class PosterManagerEventHandler {
    public AppBaseActivity mAppBaseActivity;
    public boolean isPickerOpening = false;

    public PosterManagerEventHandler(AppBaseActivity appBaseActivity) {
        mAppBaseActivity = appBaseActivity;
    }

    public void openShortcutPicker() {
        if (!isPickerOpening) {
            synchronized (PosterManagerEventHandler.class) {
                if (!isPickerOpening) {
                    isPickerOpening = true;
                    Intent picker_intent = new Intent(Constants.ACTION_FILE_PICKER);
                    mAppBaseActivity.startActivityForResult(picker_intent);
                }
            }
        }
    }

    public void pickerClose(){
        isPickerOpening=false;
    }


}
