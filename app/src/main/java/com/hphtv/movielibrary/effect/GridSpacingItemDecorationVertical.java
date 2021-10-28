package com.hphtv.movielibrary.effect;

import android.content.Context;
import android.graphics.Rect;
import androidx.annotation.DimenRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;

/**
 * Created by tchip on 17-12-19.
 */

public class GridSpacingItemDecorationVertical extends RecyclerView.ItemDecoration {

    private int mItemOffset;
    private int mCloum;
    private Context context;

    public GridSpacingItemDecorationVertical(int itemOffset, int cloum) {
        mItemOffset = itemOffset;
        mCloum = cloum;
    }


    public GridSpacingItemDecorationVertical(@NonNull Context context, @DimenRes int itemOffsetId, int mCloum) {
        this(context.getResources().getDimensionPixelSize(itemOffsetId), mCloum);
        this.context=context;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {
       if(mCloum!=-1){
           int pos=parent.indexOfChild(view);
           if(pos%mCloum==0){
               outRect.set(0,0,mItemOffset,mItemOffset);
           }else if(pos%mCloum==(mCloum-1)){
               outRect.set(mItemOffset,0,0,mItemOffset);
           }else{
               outRect.set(mItemOffset/2,0,mItemOffset,mItemOffset);
           }
       }
    }
}