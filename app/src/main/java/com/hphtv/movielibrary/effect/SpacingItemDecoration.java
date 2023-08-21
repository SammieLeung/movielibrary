package com.hphtv.movielibrary.effect;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

/**
 * author: Sam Leung
 * date:  2022/1/17
 */
public class SpacingItemDecoration extends RecyclerView.ItemDecoration {
    private int mEdgeSpace;
    private int mLeftSpace;
    private int mRightSpace;
    private int mTopSpace;
    private int mBottomSpace;

    public static final String TAG = SpacingItemDecoration.class.getSimpleName();

    public SpacingItemDecoration(int specialSpace,int lrspace,int tbspace) {
        mEdgeSpace=specialSpace;
        mLeftSpace =lrspace;
        mRightSpace=lrspace;
        mTopSpace=tbspace;
        mBottomSpace=tbspace;
    }

    public SpacingItemDecoration(int edgeSpace,int leftSpace,int rightSpace,int topSpace,int bottomSpace) {
        mEdgeSpace=edgeSpace;
        mLeftSpace =leftSpace;
        mRightSpace=rightSpace;
        mTopSpace=topSpace;
        mBottomSpace=bottomSpace;
    }


    @Override
    public void getItemOffsets(@NonNull @NotNull Rect outRect, @NonNull @NotNull View view, @NonNull @NotNull RecyclerView parent, @NonNull @NotNull RecyclerView.State state) {
        if (view.getTag() != null && view.getTag() instanceof Integer) {
            int pos = (int) view.getTag();
            if (pos == 0) {
                outRect.left = mEdgeSpace;
                outRect.right = mLeftSpace;
                outRect.top = mTopSpace;
                outRect.bottom = mBottomSpace;
            } else if (pos == state.getItemCount() - 1) {
                outRect.left = mLeftSpace;
                outRect.right = mEdgeSpace;
                outRect.top = mTopSpace;
                outRect.bottom = mBottomSpace;
            } else {
                outRect.left = mLeftSpace;
                outRect.right = mRightSpace;
                outRect.top = mTopSpace;
                outRect.bottom = mBottomSpace;
            }
        } else {
            outRect.left = mLeftSpace;
            outRect.right = mRightSpace;
            outRect.top = mTopSpace;
            outRect.bottom = mBottomSpace;
        }

    }
}
