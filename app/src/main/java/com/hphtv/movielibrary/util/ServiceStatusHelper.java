package com.hphtv.movielibrary.util;

import android.content.Context;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.databinding.FloatDialogBinding;
import com.station.kit.util.DensityUtil;
import com.station.kit.util.LogUtil;

import static android.content.Context.WINDOW_SERVICE;

/**
 * author: Sam Leung
 * date:  2022/1/6
 */
public class ServiceStatusHelper {
    private static volatile FloatDialogBinding sFloatDialogBinding;
    private static boolean isTmpRemove = false;

    public static synchronized void addView(String text, Context context) {
        if (sFloatDialogBinding == null) {
            sFloatDialogBinding = FloatDialogBinding.inflate(LayoutInflater.from(context));
            sFloatDialogBinding.setText(text);
            WindowManager wm = (WindowManager) context.getSystemService(WINDOW_SERVICE);
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            layoutParams.format = PixelFormat.RGBA_8888;
            layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
            layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;

            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            layoutParams.gravity= Gravity.BOTTOM|Gravity.END;

            wm.addView(sFloatDialogBinding.getRoot(), layoutParams);
        }
    }

    public static synchronized void removeView(Context context) {
        if (sFloatDialogBinding != null) {
            WindowManager wm = (WindowManager) context.getSystemService(WINDOW_SERVICE);
            wm.removeView(sFloatDialogBinding.getRoot());
            sFloatDialogBinding = null;
        }
    }

    public static synchronized void pauseView() {
        if (sFloatDialogBinding != null)
            sFloatDialogBinding.getRoot().setVisibility(View.GONE);
    }

    public static synchronized void resumeView() {
        if (sFloatDialogBinding!=null) {
            sFloatDialogBinding.getRoot().setVisibility(View.VISIBLE);
        }
    }

}
