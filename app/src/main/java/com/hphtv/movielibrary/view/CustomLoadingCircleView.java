package com.hphtv.movielibrary.view;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.util.DensityUtil;

/**
 * Created by tchip on 17-10-14.
 */

public class CustomLoadingCircleView extends FrameLayout {

    private Context context;
    private ImageView imageView;
    private LayoutInflater inflater;
    private int[] colors = {0xff55a51c};
    private MaterialProgressDrawable mProgress;

    public CustomLoadingCircleView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.prepearLoading();
        this.setVisibility(View.GONE);
    }

    public CustomLoadingCircleView(Context context) {
        this(context, null);
    }


    public void prepearLoading() {
        if(imageView==null) {
            imageView = (ImageView) inflater.inflate(R.layout.circle_loading_view, null);
            addView(imageView);
            LayoutParams lp = new LayoutParams(DensityUtil.dip2px(context,50), DensityUtil.dip2px(context,50), Gravity.CENTER);
            imageView.setLayoutParams(lp);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                imageView.setElevation(DensityUtil.dip2px(context,8));
            }
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
        }
    }

    public void startLoading() {
        this.setVisibility(View.VISIBLE);
        mProgress.start();
    }

    public void stopLoading() {
        mProgress.stop();
        this.setVisibility(View.GONE);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        Log.v("Loading","Loading");
        return false;
    }
}
