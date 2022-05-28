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
public class LinearLayoutItemDecoration extends RecyclerView.ItemDecoration {
    private int mSpecialSpace;
    private int mLRspace;
    private int mTBspace;

    public static final String TAG = LinearLayoutItemDecoration.class.getSimpleName();

    public LinearLayoutItemDecoration(int specialSpace, int lrspace, int tbspace) {
        mSpecialSpace=specialSpace;
        mLRspace =lrspace;
        mTBspace=tbspace;
    }


    @Override
    public void getItemOffsets(@NonNull @NotNull Rect outRect, @NonNull @NotNull View view, @NonNull @NotNull RecyclerView parent, @NonNull @NotNull RecyclerView.State state) {
        if (view.getTag() != null && view.getTag() instanceof Integer) {
            int pos = (int) view.getTag();
            if (pos == 0) {
                outRect.left = mSpecialSpace;
                outRect.right = mLRspace;
                outRect.top = mTBspace;
                outRect.bottom = mTBspace;
            } else {
                outRect.left = mLRspace;
                outRect.right = mLRspace;
                outRect.top = mTBspace;
                outRect.bottom = mTBspace;
            }
        } else {
            outRect.left = mLRspace;
            outRect.right = mLRspace;
            outRect.top = mTBspace;
            outRect.bottom = mTBspace;
        }

    }
}
