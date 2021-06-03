package com.firelfy.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.util.Map;

/**
 * Created by Luis on 2018/1/30.
 */
public class SharePreferencesTools {

    private String CONFIG_FILE_NAME = "StationService";
    private static SharePreferencesTools PREFERENCE;

    private SharedPreferences mSharedPreferences;

    private SharePreferencesTools(Context context) {
        CONFIG_FILE_NAME=context.getPackageName();
        mSharedPreferences = context.getSharedPreferences(CONFIG_FILE_NAME, Context.MODE_PRIVATE);
    }

    public static SharePreferencesTools getInstance(Context context) {
        if(PREFERENCE==null){
            synchronized(SharePreferencesTools.class){
                if (PREFERENCE == null)
                    PREFERENCE = new SharePreferencesTools(context);
            }
        }
        return PREFERENCE;
    }


    public synchronized void saveProperty(String key, String value) {
        Editor editor = mSharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public synchronized void saveProperty(String key, boolean value) {
        Editor editor = mSharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public synchronized void saveProperty(String key, int value) {
        Editor editor = mSharedPreferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public synchronized void saveProperty(String key, float value) {
        Editor editor = mSharedPreferences.edit();
        editor.putFloat(key, value);
        editor.apply();
    }

    public synchronized void saveProperty(String key, long value) {
        Editor editor = mSharedPreferences.edit();
        editor.putLong(key, value);
        editor.apply();
    }

    public synchronized String readProperty(String key, String defvalue) {
        return mSharedPreferences.getString(key, defvalue);
    }

    public synchronized boolean readProperty(String key, boolean defValue) {
        return mSharedPreferences.getBoolean(key, defValue);
    }

    public synchronized int readProperty(String key, int defValue) {
        return mSharedPreferences.getInt(key, defValue);
    }

    public synchronized float readProperty(String key, float defValue) {
        return mSharedPreferences.getFloat(key, defValue);
    }

    public synchronized long readProperty(String key, long defValue) {
        return mSharedPreferences.getLong(key, defValue);
    }

    public synchronized Map<String, ?> readProperty() {
        return mSharedPreferences.getAll();
    }

    public synchronized void removeData(String key) {
        Editor editor = mSharedPreferences.edit();
        editor.remove(key);
        editor.apply();
    }

    public synchronized void clearData() {
        Editor editor = mSharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

}
