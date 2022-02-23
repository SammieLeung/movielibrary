package com.hphtv.movielibrary.effect;

import android.graphics.Rect;

import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;

import com.station.kit.util.DensityUtil;

/**
 * Created by tchip on 17-12-19.
 */

public class GridSpacingItemDecorationVertical extends RecyclerView.ItemDecoration {
    public static final String TAG = GridSpacingItemDecorationVertical.class.getSimpleName();
    private int mFirstRowSpacing;
    private int mEdageSpacing;
    private int mRowSpacing;
    private int mColumnSpacing;
    private int mSpanCount;
    private int mItemWidth;

    public GridSpacingItemDecorationVertical(int itemWidth, int firstRowSpacing, int edageSpacing, int rowSpacing, int columnSpacing, int spanCount) {
        mItemWidth = itemWidth;
        mFirstRowSpacing = firstRowSpacing;
        mEdageSpacing = edageSpacing;
        mRowSpacing = rowSpacing;
        mColumnSpacing = columnSpacing;
        mSpanCount = spanCount;
    }


    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {
        if (mItemWidth != 0 && mSpanCount != -1) {
            int width = parent.getWidth();
            int eachWidth=Math.round(1.0f * width / mSpanCount);
            int eachColumnSpacing =eachWidth - mItemWidth;
            int pos = parent.getChildAdapterPosition(view);

            int tmpPos = pos % mSpanCount;
            int lastLeftColumn = 0;
            int lastRightColumn = 0;
            for (int i = 0; i < mSpanCount; i++) {
                if(mEdageSpacing!=-1) {
                    if (i == 0) {
                        lastLeftColumn = mEdageSpacing;
                        lastRightColumn= eachWidth-mEdageSpacing-mItemWidth;
                    } else if (i == mSpanCount - 1) {
                        lastLeftColumn= eachColumnSpacing-lastRightColumn;
                        lastRightColumn=mEdageSpacing;
                    } else {
                        lastLeftColumn=mColumnSpacing-lastRightColumn;
                        lastRightColumn=eachColumnSpacing-lastLeftColumn;
                    }
                    if(i==tmpPos){
                        break;
                    }
                }
            }
            //设置第一行顶部间隙+第一行item
            if (mFirstRowSpacing != -1 && pos / mSpanCount == 0) {
                    outRect.set(lastLeftColumn, mFirstRowSpacing, lastRightColumn, mRowSpacing / 2);
            } else {
                outRect.set(lastLeftColumn, mRowSpacing / 2, lastRightColumn, mRowSpacing / 2);
            }


//            //设置第一行顶部间隙+第一行item
//            if (mFirstRowSpacing != -1 && pos / mSpanCount == 0) {
//                if (pos % mSpanCount == 0) {
//                    outRect.set(mEdageSpacing, mFirstRowSpacing, mColumnSpacing / 2, mRowSpacing / 2);
//                } else if (pos % mSpanCount == (mSpanCount - 1)) {
//                    outRect.set(mColumnSpacing / 2, mFirstRowSpacing, mEdageSpacing, mRowSpacing / 2);
//                }
//                else {
//                    outRect.set(mColumnSpacing / 2, mFirstRowSpacing, mColumnSpacing / 2, mRowSpacing / 2);
//                }
//            } else {
//                if (pos % mSpanCount == 0) {
//                    outRect.set(mEdageSpacing, mRowSpacing/2, mColumnSpacing / 2, mRowSpacing / 2);
//                } else if (pos % mSpanCount == (mSpanCount - 1)) {
//                    outRect.set(mColumnSpacing / 2, mRowSpacing / 2, mEdageSpacing , mRowSpacing / 2);
//                }
//                else {
//                    outRect.set(mColumnSpacing / 2, mRowSpacing / 2, mColumnSpacing / 2,mRowSpacing / 2);
//                }
//            }


        }
    }
}
