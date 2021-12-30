package com.hphtv.movielibrary.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatButton;

import com.hphtv.movielibrary.R;


/**
 * Created by tchip on 18-4-27.
 */

public class DrawTopButton extends AppCompatButton {
    private int drawableSize;
    private STATE_COLLECTION currentState=STATE_COLLECTION.FALSE;

    public enum STATE_COLLECTION {
        TRUE, FALSE
    }

    private static final int[] FAVORITE_STATE = {R.attr.isFavorite};

    public DrawTopButton(Context context) {
        super(context);
    }

    public DrawTopButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.DrawTopButton);
        drawableSize = a.getDimensionPixelSize(R.styleable.DrawTopButton_dtbDrawableTopSize, 50);
        Drawable drawableTop = a.getDrawable(R.styleable.DrawTopButton_dtbDrawableTop);
        a.recycle();
        setCompoundDrawablesWithIntrinsicBounds(null, drawableTop, null, null);
    }

    public DrawTopButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setCompoundDrawablesWithIntrinsicBounds(Drawable left, Drawable top, Drawable right, Drawable bottom) {
        super.setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom);

        if (top != null) {
            top.setBounds(0, 0, drawableSize, drawableSize);
        }
        setCompoundDrawables(left, top, right, bottom);
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        int[] stateSets = super.onCreateDrawableState(extraSpace + 1);
        if (currentState == null) {
            currentState = STATE_COLLECTION.FALSE;
        }
        switch (currentState) {
            case TRUE:
                mergeDrawableStates(stateSets, FAVORITE_STATE);
                break;
        }
        return stateSets;
    }

    public void setFavoriteState(boolean isFavorite){
        if(isFavorite){
            setState(STATE_COLLECTION.TRUE);
        }else{
            setState(STATE_COLLECTION.FALSE);
        }
    }

    private void setState(STATE_COLLECTION currentState) {
        this.currentState = currentState;
        refreshDrawableState();
    }



    public STATE_COLLECTION getState(){
        return this.currentState;
    }

    public void toggle(){
        if(this.currentState==STATE_COLLECTION.FALSE){
            this.currentState=STATE_COLLECTION.TRUE;
        }else{
            this.currentState=STATE_COLLECTION.FALSE;
        }
    }

}
