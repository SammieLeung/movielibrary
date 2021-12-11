package com.hphtv.movielibrary.listener;

import android.content.Context;
import android.content.Intent;

import com.hphtv.movielibrary.data.Constants;

/**
 * author: Sam Leung
 * date:  2021/12/10
 */
public class PosterManagerEventHandler {
    private boolean isPickerOpening=false;
    public void openShortcutPicker(Context context){
        if (!isPickerOpening) {
            synchronized (this) {
                if (!isPickerOpening) {
                    isPickerOpening = true;
                    Intent picker_intent = new Intent(Constants.ACTION_FILE_PICKER);
                    context.startActivityForResult(picker_intent);
                }
            }
        }
    }
}
