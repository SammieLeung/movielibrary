package com.hphtv.movielibrary.util;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import java.util.Locale;

/**
 * Created by tchip on 18-8-17.
 */

public class LanguageUtil {


    private static final String LAST_LANGUAGE = "lastLanguage";

    /**
     * 当改变系统语言时,重启App
     *
     * @param activity
     * @param homeActivityCls 主activity
     * @return
     */
    public static boolean isLanguageChanged(Activity activity, Class<?> homeActivityCls) {
        Locale locale = Locale.getDefault();
        if (locale == null) {
            return false;
        }
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);
        String localeStr = sp.getString(LAST_LANGUAGE, "");
        String curLocaleStr = getLocaleString(locale);
        if (TextUtils.isEmpty(localeStr)) {
            sp.edit().putString(LAST_LANGUAGE, curLocaleStr).commit();
            return false;
        } else {
            if (localeStr.equals(curLocaleStr)) {
                return false;
            } else {
                sp.edit().putString(LAST_LANGUAGE, curLocaleStr).commit();
                restartApp(activity, homeActivityCls);
                return true;
            }
        }
    }

    private static String getLocaleString(Locale locale) {
        if (locale == null) {
            return "";
        } else {
            return locale.getCountry() + locale.getLanguage();
        }
    }

    public static void restartApp(Activity activity, Class<?> homeClass) {
        Intent intent = new Intent(activity, homeClass);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        // 杀掉进程
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

}
