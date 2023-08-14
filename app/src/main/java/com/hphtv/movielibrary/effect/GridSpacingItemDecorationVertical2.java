package com.hphtv.movielibrary.effect;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by tchip on 17-12-19.
 */

public class GridSpacingItemDecorationVertical2 extends RecyclerView.ItemDecoration {
    public static final String TAG = GridSpacingItemDecorationVertical2.class.getSimpleName();
    private int mFirstRowSpacing;
    private int mEdgeSpacing;
    private int mRowSpacing;
    private int mColumnSpacing;
    private int mSpanCount;
    private int mItemWidth;

    public GridSpacingItemDecorationVertical2(int itemWidth, int firstRowSpacing, int edgeSpacing, int rowSpacing, int columnSpacing, int spanCount) {
        mItemWidth = itemWidth;
        mFirstRowSpacing = firstRowSpacing;
        mEdgeSpacing = edgeSpacing;
        mRowSpacing = rowSpacing;
        mColumnSpacing = columnSpacing;
        mSpanCount = spanCount;
    }


    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {
        if (mItemWidth != 0 && mSpanCount != -1) {
            int pos = parent.getChildAdapterPosition(view);

            int rowPos = pos % mSpanCount;
            int topRowSpacing = 0;
            if (mFirstRowSpacing != -1 && pos / mSpanCount == 0) {
                topRowSpacing = mFirstRowSpacing;
            }
            if (rowPos == 0) {
                outRect.set(mEdgeSpacing, topRowSpacing, mColumnSpacing / 2 + 1, mRowSpacing);
            } else if (rowPos == mSpanCount - 1) {
                outRect.set(mColumnSpacing / 2 - 1, topRowSpacing, mEdgeSpacing, mRowSpacing);
            } else {
                outRect.set(mColumnSpacing / 2 + 1, topRowSpacing, mColumnSpacing / 2 - 1, mRowSpacing);
            }


        }
    }

}
