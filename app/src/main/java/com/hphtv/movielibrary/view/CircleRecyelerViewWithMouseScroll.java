package com.hphtv.movielibrary.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.InputDevice;
import android.view.MotionEvent;

import me.khrystal.library.widget.CircleRecyclerView;

/**
 * Created by tchip on 18-5-2.
 */

public class CircleRecyelerViewWithMouseScroll extends CircleRecyclerView {

    private float mVerticalScrollFactor = 20.f;
    private static final String TAG = "RViewWithMouseScroll";

    public CircleRecyelerViewWithMouseScroll(Context context) {
        super(context);
    }

    public CircleRecyelerViewWithMouseScroll(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CircleRecyelerViewWithMouseScroll(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
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
