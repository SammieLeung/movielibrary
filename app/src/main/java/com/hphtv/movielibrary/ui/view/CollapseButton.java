package com.hphtv.movielibrary.ui.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hphtv.movielibrary.R;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * author: Sam Leung
 * date:  2022/5/7
 */
public class CollapseButton extends androidx.appcompat.widget.AppCompatButton implements View.OnFocusChangeListener {
    public static final String TAG = CollapseButton.class.getSimpleName();

    public String mPresetText = null;
    public AtomicBoolean mAtomicMeasure = new AtomicBoolean(false);

    public CollapseButton(Context context) {
        this(context, null);
    }

    public CollapseButton(Context context, AttributeSet attrs) {
        this(context, attrs,0);
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
        int measureWidth = getMeasuredWidth();
        int drawPadding=getResources().getDimensionPixelSize(R.dimen.common_circle_btn_padding);

        if (measureWidth > 0) {
            int textWidth = (int) this.getPaint().measureText(mPresetText);
            int targetWidth = measureWidth + textWidth+drawPadding;
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
                }
            });
            objectAnimator.start();

        } else if (!mAtomicMeasure.get()) {
            setText(mPresetText);
            setCompoundDrawablePadding(drawPadding);
        }
    }

    public void hidePresetText() {
        int measureWidth = getMeasuredWidth();
        if (measureWidth > 0) {
            int textWidth = (int) this.getPaint().measureText(mPresetText);
            int drawPadding=getResources().getDimensionPixelSize(R.dimen.common_circle_btn_padding);
            int targetWidth = measureWidth - textWidth-drawPadding;
            setWrapperWidth(measureWidth);
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
                }
            });
            objectAnimator.start();
            ObjectAnimator.ofInt(this, "textColor", getResources().getColor(android.R.color.transparent, null)).setDuration(300).start();
        } else if (!mAtomicMeasure.get()) {
            setText(null);
            setCompoundDrawablePadding(0);
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
