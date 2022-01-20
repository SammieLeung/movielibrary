package com.hphtv.movielibrary.effect;

import android.content.Context;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.station.kit.util.DensityUtil;
import com.station.kit.util.LogUtil;

import org.jetbrains.annotations.NotNull;

/**
 * author: Sam Leung
 * date:  2022/1/17
 */
public class SpacingItemDecoration extends RecyclerView.ItemDecoration {
    private int mSpecialSpace;
    private int mSpace;

    public static final String TAG = SpacingItemDecoration.class.getSimpleName();

    public SpacingItemDecoration(int specialSpace,int normalSpace) {
        mSpecialSpace=specialSpace;
        mSpace=normalSpace;
    }


    @Override
    public void getItemOffsets(@NonNull @NotNull Rect outRect, @NonNull @NotNull View view, @NonNull @NotNull RecyclerView parent, @NonNull @NotNull RecyclerView.State state) {
        if (view.getTag() != null && view.getTag() instanceof Integer) {
            int pos = (int) view.getTag();
            if (pos == 0) {
                outRect.left = mSpecialSpace;
                outRect.right = mSpace;
                outRect.top = mSpace;
                outRect.bottom = mSpace;
            } else if (pos == state.getItemCount() - 1) {
                outRect.left = mSpace;
                outRect.right = mSpecialSpace;
                outRect.top = mSpace;
                outRect.bottom = mSpace;
            } else {
                outRect.left = mSpace;
                outRect.right = mSpace;
                outRect.top = mSpace;
                outRect.bottom = mSpace;
            }
        } else {
            outRect.left = mSpace;
            outRect.right = mSpace;
            outRect.top = mSpace;
            outRect.bottom = mSpace;
        }

    }
}
