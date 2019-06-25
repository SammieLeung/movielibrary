package com.hphtv.movielibrary.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;

public class RecyclerViewWithMouseScroll extends RecyclerView {
    private float mVerticalScrollFactor = 20.f;
    private static final String TAG = "RViewWithMouseScroll";

    public RecyclerViewWithMouseScroll(Context context) {
        super(context);
    }

    public RecyclerViewWithMouseScroll(Context context,
                                       @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public RecyclerViewWithMouseScroll(Context context,
                                       @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {// 鼠标滑轮滑动事件
        if ((event.getSource() & InputDevice.SOURCE_CLASS_POINTER) != 0) {
            if (event.getAction() == MotionEvent.ACTION_SCROLL
                    && getScrollState() == SCROLL_STATE_IDLE) {// 鼠标滑轮事件执行&&RecyclerView不是真正滑动
                final float vscroll = event
                        .getAxisValue(MotionEvent.AXIS_VSCROLL);// 获取轴线距离
                if (vscroll != 0) {
                    final int delta = -1
                            * (int) (vscroll * mVerticalScrollFactor);
                    Log.v(TAG, "onGenericMotionEvent()==>");
                    if (ViewCompat
                            .canScrollVertically(this, delta > 0 ? 1 : -1)) {
                        scrollBy(0, delta);
                        Log.v(TAG, "canScrollVertically");
                        return true;

                    } else if (ViewCompat.canScrollHorizontally(this, delta > 0 ? 1 : -1)) {
                        Log.v(TAG, "canScrollHorizontally");
                        scrollBy(delta * 2, 0);
                        return true;
                    }
                }
            }
        }
        return super.onGenericMotionEvent(event);
    }


}
