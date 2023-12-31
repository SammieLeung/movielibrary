package com.hphtv.movielibrary.ui.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hphtv.movielibrary.R;
import com.station.kit.util.LogUtil;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * author: Sam Leung
 * date:  2022/5/7
 */
public class CollapseButton extends androidx.appcompat.widget.AppCompatButton implements View.OnFocusChangeListener {
    public static final String TAG = CollapseButton.class.getSimpleName();

    public String mPresetText = null;
    public AtomicBoolean mAtomicMeasure = new AtomicBoolean(false);
    private int mMinWidth = -1;
    private int mMaxWidth = -1;

    public CollapseButton(Context context) {
        this(context, null);
    }

    public CollapseButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CollapseButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setGravity(Gravity.CENTER);
        mPresetText = getText().toString();
        setText(null);
        setCompoundDrawablePadding(0);
        setOnFocusChangeListener(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mAtomicMeasure.set(true);
    }

    public void showPresetText() {
        if(mMinWidth<0)
            mMinWidth=getMeasuredWidth();
        int measureWidth = mMinWidth;

        int drawPadding = getResources().getDimensionPixelSize(R.dimen.common_circle_btn_drawable_padding);
        int minPadding = getResources().getDimensionPixelSize(R.dimen.common_circle_btn_min_padding);
        int normalPadding = getResources().getDimensionPixelSize(R.dimen.common_circle_btn_padding);
        if (measureWidth > 0) {
            int textWidth = (int) this.getPaint().measureText(mPresetText);
            int targetWidth = measureWidth + textWidth + drawPadding - 2 * minPadding + 2 * normalPadding;
            if (mMaxWidth < 0)
                mMaxWidth = targetWidth;
            Log.e(TAG, "hidePresetText: measureWidth=" + measureWidth + " targetWidth=" + targetWidth);

            ObjectAnimator objectAnimator = ObjectAnimator.ofInt(this, "wrapperWidth", measureWidth, targetWidth).setDuration(200);
            objectAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    setTextColor(getResources().getColorStateList(R.color.circle_btn_color_list, null));
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    setCompoundDrawablePadding(drawPadding);
                    setText(mPresetText);
                    setPadding(normalPadding, getPaddingTop(), normalPadding, getPaddingBottom());
                }
            });
            objectAnimator.start();

        } else if (!mAtomicMeasure.get()) {
            setText(mPresetText);
            setCompoundDrawablePadding(drawPadding);
            setPadding(normalPadding, getPaddingTop(), normalPadding, getPaddingBottom());
        }
    }

    public void hidePresetText() {
        int measureWidth = mMaxWidth < 0 ? getMeasuredWidth() : mMaxWidth;
        int drawPadding = getResources().getDimensionPixelSize(R.dimen.common_circle_btn_drawable_padding);
        int minPadding = getResources().getDimensionPixelSize(R.dimen.common_circle_btn_min_padding);
        int normalPadding = getResources().getDimensionPixelSize(R.dimen.common_circle_btn_padding);
        if (measureWidth > 0) {
            int textWidth = (int) this.getPaint().measureText(mPresetText);

            int targetWidth = measureWidth - textWidth - drawPadding - 2 * normalPadding + 2 * minPadding;
            setWrapperWidth(measureWidth);

            Log.e(TAG, "hidePresetText: measureWidth=" + measureWidth + " targetWidth=" + targetWidth);
            ObjectAnimator objectAnimator = ObjectAnimator.ofInt(this, "wrapperWidth", measureWidth, targetWidth).setDuration(200);
            objectAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    setCompoundDrawablePadding(0);
                    setText(null);
                    setPadding(minPadding, getPaddingTop(), minPadding, getPaddingBottom());
                }
            });
            objectAnimator.start();
            ObjectAnimator.ofInt(this, "textColor", getResources().getColor(android.R.color.transparent, null)).setDuration(200).start();
        } else if (!mAtomicMeasure.get()) {
            setText(null);
            setCompoundDrawablePadding(0);
            setPadding(minPadding, getPaddingTop(), minPadding, getPaddingBottom());
        }
    }

    public void setWrapperWidth(int width) {
        getLayoutParams().width = width;
        requestLayout();
    }

    public void setWrapperHeight(int height) {
        getLayoutParams().height = height;
        requestLayout();
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            if (v instanceof CollapseButton)
                this.showPresetText();
        } else {
            if (v instanceof CollapseButton)
                this.hidePresetText();
        }
    }
}
