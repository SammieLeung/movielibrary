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
    public static boolean isPickerOpening=false;
    public static void openShortcutPicker(AppBaseActivity activity){
        if (!isPickerOpening) {
            synchronized (PosterManagerEventHandler.class) {
                if (!isPickerOpening) {
                    isPickerOpening = true;
                    Intent picker_intent = new Intent(Constants.ACTION_FILE_PICKER);
                    activity.startActivityForResult(picker_intent);
                }
            }
        }
    }

}
