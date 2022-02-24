package com.hphtv.movielibrary.effect;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.recyclerview.widget.GridLayoutManager;

import com.hphtv.movielibrary.listener.VisibleItemListener;

/**
 * author: Sam Leung
 * date:  2022/2/24
 */
public class FilterGridLayoutManager extends GridLayoutManager implements VisibleItemListener {
    public FilterGridLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public FilterGridLayoutManager(Context context, int spanCount) {
        super(context, spanCount);
    }

    public FilterGridLayoutManager(Context context, int spanCount, int orientation, boolean reverseLayout) {
        super(context, spanCount, orientation, reverseLayout);
    }

    @Override
    public void getFirstVisibleItem(View v) {
        if(mVisibleItemListener!=null)
            mVisibleItemListener.getFirstVisibleItem(v);
    }

    public VisibleItemListener mVisibleItemListener;

    public void setVisibleItemListener(VisibleItemListener visibleItemListener) {
        mVisibleItemListener = visibleItemListener;
    }
}
