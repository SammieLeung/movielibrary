package com.hphtv.movielibrary.effect;

import android.graphics.Rect;

import androidx.recyclerview.widget.RecyclerView;

import android.view.View;

/**
 * Created by tchip on 17-12-19.
 */

public class GridSpacingItemDecorationVertical extends RecyclerView.ItemDecoration {

    private int mRowSpacing;
    private int mColumnSpacing;
    private int mSpanCount;

    public GridSpacingItemDecorationVertical(int rowSpacing, int columnSpacing, int spanCount) {
        mRowSpacing = rowSpacing;
        mColumnSpacing = columnSpacing;
        mSpanCount = spanCount;
    }


    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {
        if (mSpanCount != -1) {
            int pos = parent.getChildAdapterPosition(view);
            if (pos % mSpanCount == 0) {
                outRect.set(mColumnSpacing / 2, mRowSpacing/2, mColumnSpacing / 2, mRowSpacing/2);
            } else if (pos % mSpanCount == (mSpanCount - 1)) {
                outRect.set(mColumnSpacing / 2, mRowSpacing/2, mColumnSpacing / 2, mRowSpacing/2);
            } else {
                outRect.set(mColumnSpacing / 2, mRowSpacing/2, mColumnSpacing / 2, mRowSpacing/2);
            }
        }
    }
}
