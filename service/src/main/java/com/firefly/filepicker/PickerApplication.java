package com.firefly.filepicker;

import android.app.Application;

import jcifs.Config;

/**
 * Created by rany on 18-4-3.
 */

public class PickerApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Config.registerSmbURLHandler();

        if (BuildConfig.DEBUG) {
            System.setProperty("jcifs.util.loglevel", "3");
        } else {
            System.setProperty("jcifs.util.loglevel", "0");
        }
    }
}
