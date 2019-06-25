package com.hphtv.movielibrary.view;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.util.DensityUtil;

/**
 * Created by tchip on 17-10-14.
 */

public class CustomLoadingCircleViewFragment extends DialogFragment {

    private Context context;
    private ImageView imageView;
    private LayoutInflater inflater;
    private int[] colors = {0xff55a51c};
    private MaterialProgressDrawable mProgress;
//
//    public CustomLoadingCircleViewFragment(@NonNull Context context, @Nullable AttributeSet attrs) {
//        super(context, attrs);
//        this.context = context;
//        this.inflater = LayoutInflater.from(context);
//        this.prepearLoading();
//        this.setVisibility(View.GONE);
//    }
//
//    public CustomLoadingCircleViewFragment(Context context) {
//        this(context, null);
//    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        imageView = (ImageView) getActivity().getLayoutInflater().inflate(R.layout.circle_loading_view, null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            imageView.setElevation(DensityUtil.dip2px(context, 8));
        }

        int width_dp = 50;
        Dialog dialog = new Dialog(getActivity(), R.style.DialogStyle_transparent);
        dialog.getWindow().setLayout(DensityUtil.dip2px(context, width_dp), DensityUtil.dip2px(context, width_dp));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(imageView);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if(keyCode==KeyEvent.KEYCODE_BACK){
                    return true;
                }
                return false;
            }
        });
        mProgress = new MaterialProgressDrawable(
                context, imageView);
        mProgress.setBackgroundColor(0xFFFAFAFA);
        mProgress.setColorSchemeColors(colors);
        mProgress.updateSizes(MaterialProgressDrawable.LARGE);
        mProgress.setAlpha(255);
        mProgress.setStartEndTrim(0f, 0.8f);
        mProgress.setArrowScale(1f); // 0~1之间
        mProgress.setProgressRotation(1);
        imageView.setImageDrawable(mProgress);
        startLoading();
        return dialog;

    }


    public void startLoading() {
        mProgress.start();
    }

    public void stopLoading() {
        mProgress.stop();
    }

}
