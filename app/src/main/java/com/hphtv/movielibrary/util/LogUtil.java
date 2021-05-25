package com.hphtv.movielibrary.util;

import android.util.Log;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author lxp
 * @date 20-7-16
 */
public class LogUtil {
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({VERBOSE, DEBUG, INFO, WARN, ERROR})
    public @interface Level {
    }


    private static boolean _DEBUG = true;

    private static String TAG = "STATION-LOG";
    private static final int MSG_MAX_LENGTH = 4 * 1024;

    public static final int VERBOSE = Log.VERBOSE;
    public static final int DEBUG = Log.DEBUG;
    public static final int INFO = Log.INFO;
    public static final int WARN = Log.WARN;
    public static final int ERROR = Log.ERROR;
    private static int LEVEL = VERBOSE;//filter

    public static void setTAG(String tag) {
        TAG = tag;
    }

    public static void v(String msg) {
        v(TAG, msg);
    }

    public static void v(String msg, Throwable tr) {
        v(TAG, msg, tr);
    }

    public static void v(String tag, String msg) {
        v(tag, msg, null);
    }

    public static void v(String tag, String msg, Throwable tr) {
        printLog(tag, msg, VERBOSE, tr);
    }

    public static void d(String msg) {
        d(TAG, msg);
    }

    public static void d(String msg, Throwable tr) {
        d(TAG, msg, tr);
    }

    public static void d(String tag, String msg) {
        d(tag, msg, null);
    }

    public static void d(String tag, String msg, Throwable tr) {
        printLog(tag, msg, DEBUG, tr);
    }

    public static void i(String msg) {
        i(TAG, msg);
    }

    public static void i(String msg, Throwable tr) {
        i(TAG, msg, tr);
    }

    public static void i(String tag, String msg) {
        i(tag, msg, null);
    }

    public static void i(String tag, String msg, Throwable tr) {
        printLog(tag, msg, INFO, tr);
    }

    public static void w(String msg) {
        w(TAG, msg);
    }

    public static void w(String msg, Throwable tr) {
        w(TAG, msg, tr);
    }

    public static void w(String tag, String msg) {
        w(tag, msg, null);
    }

    public static void w(String tag, String msg, Throwable tr) {
        printLog(tag, msg, WARN, tr);
    }


    public static void e(String msg) {
        e(TAG, msg);
    }

    public static void e(String msg, Throwable tr) {
        e(TAG, msg, tr);
    }

    public static void e(String tag, String msg) {
        e(tag, msg, null);
    }

    public static void e(String tag, String msg, Throwable tr) {
        printLog(tag, msg, ERROR, tr);
    }

    public static void printSpendTimes(long t) {
        LogUtil.e((System.currentTimeMillis() - t) + "ms");
    }

    private static void printLog(String tag, String msg, int level, Throwable exception) {
        if (_DEBUG) {
            int stringLen = msg.length();
            if (stringLen > MSG_MAX_LENGTH) {
                int count = (int) Math.ceil(stringLen / MSG_MAX_LENGTH);
                for (int i = 0; i < count; i++) {
                    String message;
                    if ((i + 1) * MSG_MAX_LENGTH > stringLen) {
                        message = msg.substring(i * MSG_MAX_LENGTH, stringLen);
                    } else {
                        message = msg.substring(i * MSG_MAX_LENGTH, (i + 1) * MSG_MAX_LENGTH);
                    }
                    if (level >= LEVEL)
                        switch (level) {
                            case VERBOSE:
                                if (exception == null)
                                    Log.v(tag, message);
                                else
                                    Log.v(tag, message, exception);
                                break;
                            case DEBUG:
                                if (exception == null)
                                    Log.d(tag, message);
                                else
                                    Log.d(tag, msg, exception);
                                break;
                            case INFO:
                                if (exception == null)
                                    Log.i(tag, message);
                                else
                                    Log.i(tag, msg, exception);
                                break;
                            case WARN:
                                if (exception == null)
                                    Log.w(tag, message);
                                else
                                    Log.w(tag, msg, exception);
                                break;
                            case ERROR:
                                if (exception == null)
                                    Log.e(tag, message);
                                else
                                    Log.e(tag, msg, exception);
                                break;
                        }
                }
            } else {
                if (level >= LEVEL)
                    switch (level) {
                        case VERBOSE:
                            if (exception == null)
                                Log.v(tag, msg);
                            else
                                Log.v(tag, msg, exception);
                            break;
                        case DEBUG:
                            if (exception == null)
                                Log.d(tag, msg);
                            else
                                Log.d(tag, msg, exception);
                            break;
                        case INFO:
                            if (exception == null)
                                Log.i(tag, msg);
                            else
                                Log.i(tag, msg, exception);
                            break;
                        case WARN:
                            if (exception == null)
                                Log.w(tag, msg);
                            else
                                Log.w(tag, msg, exception);
                            break;
                        case ERROR:
                            if (exception == null)
                                Log.e(tag, msg);
                            else
                                Log.e(tag, msg, exception);
                            break;
                    }
            }
        }
    }

    public static void setLevel(@Level int level) {
        LEVEL = level;
    }

    public static void enableLogcat(boolean enable) {
        _DEBUG = enable;
    }
}
