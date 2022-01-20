package com.hphtv.movielibrary.ui.view;

import android.content.Context;
import androidx.drawerlayout.widget.DrawerLayout;
import android.util.AttributeSet;

/**
 * Created by tchip on 18-1-5.
 */

public class CustomDrawerLayout extends DrawerLayout {
    public CustomDrawerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(
                MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(
                MeasureSpec.getSize(heightMeasureSpec), MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}