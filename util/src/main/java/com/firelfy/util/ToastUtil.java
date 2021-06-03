package com.firelfy.util;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ToastUtil {
    public static final String TAG = ToastUtil.class.getSimpleName();
    private Context mContext;
    private Handler mHandler;
    private List<Toast> mToastArrayList = new ArrayList<>();
    private ToastDisplayTimeMonitor mTimeMonitor;

    public static ToastUtil newInstance(Context context) {
        return new ToastUtil(context);
    }

    private ToastUtil(Context context) {
        mContext = context;
        mHandler = new Handler(mContext.getMainLooper());
    }

    public void toast(String msg) {
        toast(msg, Toast.LENGTH_SHORT);
    }

    public void toast(String msg, int peroid) {
        Log.v(TAG, "toast start");
        mHandler.removeCallbacks(mTimeMonitor);
        for (Toast t : mToastArrayList) {
            t.cancel();
        }
        mToastArrayList.clear();
        if (!TextUtils.isEmpty(msg)) {
            Toast toast = Toast.makeText(mContext,
                    msg,
                    peroid);
            toast.show();

            mToastArrayList.add(toast);
            mTimeMonitor = new ToastDisplayTimeMonitor();
            mHandler.postDelayed(mTimeMonitor, 2000);
        }
        Log.v(TAG, "toast end");

    }


    private class ToastDisplayTimeMonitor implements Runnable {
        @Override
        public void run() {
            for (Toast t : mToastArrayList) {
                t.cancel();
            }
            mToastArrayList.clear();
        }
    }
}
