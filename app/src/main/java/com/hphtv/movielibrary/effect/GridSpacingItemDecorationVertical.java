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
            /*计算实际间隔
                sample:
                指定边距35(最左和最右列距)
                item固定宽度307
                相邻item列距30
                |35 264 8|22 264 21|9 264 35|
            */
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
            //首行
            if (mFirstRowSpacing != -1 && pos / mSpanCount == 0) {
                    outRect.set(lastLeftColumn, mFirstRowSpacing, lastRightColumn, mRowSpacing / 2);
            } else {
                outRect.set(lastLeftColumn, mRowSpacing / 2, lastRightColumn, mRowSpacing / 2);
            }
        }
    }

}
