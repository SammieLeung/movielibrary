package com.hphtv.movielibrary.ui.view;

import android.view.View;

/**
 * author: Sam Leung
 * date:  2021/8/27
 */
public class AnimateWrapper {
    private View mView;
    public AnimateWrapper(View target){
        mView=target;
    }

    public int getWidth(){
        return mView.getLayoutParams().width;
    }

    public void setWidth(int width){
        mView.getLayoutParams().width=width;
        mView.requestLayout();
    }

    public int getHeight(){
        return mView.getLayoutParams().height;
    }

    public void setHeight(int height){
        mView.getLayoutParams().height=height;
        mView.requestLayout();
    }
}
