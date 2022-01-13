package com.hphtv.movielibrary.ui.shortcutmanager;

import android.content.Intent;

import com.hphtv.movielibrary.ui.AppBaseActivity;
import com.hphtv.movielibrary.data.Constants;

/**
 * 设备管理页
 * author: Sam Leung
 * date:  2021/12/10
 */
public class ShortcutManagerEventHandler {
    public AppBaseActivity mAppBaseActivity;
    public boolean isPickerOpening = false;

    public ShortcutManagerEventHandler(AppBaseActivity appBaseActivity) {
        mAppBaseActivity = appBaseActivity;
    }

    /**
     * 打开文件选择器
     */
    public void openShortcutPicker() {
        if (!isPickerOpening) {
            synchronized (ShortcutManagerEventHandler.class) {
                if (!isPickerOpening) {
                    isPickerOpening = true;
                    Intent picker_intent = new Intent(Constants.ACTION_FILE_PICKER);
                    mAppBaseActivity.startActivityForResult(picker_intent);
                }
            }
        }
    }

    /**
     * 文件管理器启动flag
     */
    public void pickerClose(){
        isPickerOpening=false;
    }


}
