package com.hphtv.movielibrary.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hphtv.movielibrary.R;

/**
 * Created by tchip on 17-12-21.
 */

public class FloatKeyboard extends RelativeLayout {
    private Context context;
    public static final String TAG = "FloatKeyboard";
    private TextView tv_center, tv_left, tv_top, tv_right, tv_bottom;
    private boolean isShowed = false;
    private View triggerView;
    private FloatKeyboard mFloatKeyboard;
    private int mFloatKeyboardWidth = 0;
    private int mFloatKeyboardOffset = 0;

    public FloatKeyboard(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        this.setFocusable(true);
        mFloatKeyboard = this;
        LayoutInflater.from(context).inflate(R.layout.view_float_keyboard, this, true);
        tv_center = (TextView) findViewById(R.id.fk_center);
        tv_left = (TextView) findViewById(R.id.fk1);
        tv_top = (TextView) findViewById(R.id.fk2);
        tv_right = (TextView) findViewById(R.id.fk3);
        tv_bottom = (TextView) findViewById(R.id.fk4);

        tv_center.setOnClickListener(onClickListener);
        tv_left.setOnClickListener(onClickListener);
        tv_top.setOnClickListener(onClickListener);
        tv_right.setOnClickListener(onClickListener);
        tv_bottom.setOnClickListener(onClickListener);

        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.FloatKeyboard);
        if (attributes != null) {
            mFloatKeyboardWidth = attributes.getDimensionPixelSize(R.styleable.FloatKeyboard_offsetDis, 120);
            Log.v(TAG, " mFloatKeyboardWidth =" + mFloatKeyboardWidth);
        }
        attributes.recycle();
    }

    public FloatKeyboard(Context context) {
        this(context, null);
    }

    public void setDatas(String[] datas) {
        tv_bottom.setText(null);
        tv_right.setText(null);
        tv_top.setText(null);
        tv_left.setText(null);
        tv_center.setText(null);
        if (datas.length > 0) {
            if (tv_top != null) {
                tv_top.setText(datas[0]);
            }
            if (datas.length > 1) {
                if (tv_left != null) {
                    tv_left.setText(datas[1]);
                }
                if (datas.length > 2) {
                    if (tv_center != null) {
                        tv_center.setText(datas[2]);
                    }
                    if (datas.length > 3) {
                        if (tv_right != null) {
                            tv_right.setText(datas[3]);
                        }
                        if (datas.length > 4) {
                            if (tv_bottom != null) {
                                tv_bottom.setText(datas[4]);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void show(View tView, final int offset_left, final int offset_top) {
        this.triggerView = tView;

        int tW = triggerView.getMeasuredWidth() / 2;
        int tL = triggerView.getLeft() + tW;
        int tT = triggerView.getTop() + tW;
        int hw = 0;
        if (mFloatKeyboardWidth != 0)
            hw = mFloatKeyboardWidth / 2;
        int x = tL - hw + offset_left;
        int y = tT - hw + offset_top;
        mFloatKeyboard.setX(x);
        mFloatKeyboard.setY(y);
        Log.v(TAG, "floatkeyboard x=" + x + ",y=" + y);


        isShowed = true;
        this.setVisibility(View.VISIBLE);
        this.requestFocus();
    }


    public void hide() {
        isShowed = false;
        if (triggerView != null) {
            triggerView.requestFocus();
        }

        this.setVisibility(View.GONE);
    }

    public boolean isShowed() {
        return isShowed;
    }


    public String getTopValue() {
        if (tv_center.getText() != null) {
            return tv_top.getText().toString();
        }
        return null;
    }

    public String getLeftValue() {
        if (tv_center.getText() != null) {
            return tv_left.getText().toString();
        }
        return null;
    }


    public String getRightValue() {
        if (tv_center.getText() != null) {
            return tv_right.getText().toString();
        }
        return null;
    }


    public String getBottomValue() {
        if (tv_center.getText() != null) {
            return tv_bottom.getText().toString();
        }
        return null;
    }


    public String getCenterValue() {
        if (tv_center.getText() != null) {
            return tv_center.getText().toString();
        }
        return null;
    }

    OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (buttonClickListener != null)
                switch (v.getId()) {
                    case R.id.fk_center:
                        buttonClickListener.OnButtonClick(getCenterValue());
                        break;
                    case R.id.fk1:
                        buttonClickListener.OnButtonClick(getLeftValue());
                        break;
                    case R.id.fk2:
                        buttonClickListener.OnButtonClick(getTopValue());
                        break;
                    case R.id.fk3:
                        buttonClickListener.OnButtonClick(getRightValue());
                        break;
                    case R.id.fk4:
                        buttonClickListener.OnButtonClick(getBottomValue());
                        break;

                }
        }
    };

    public interface OnButtonClickListener {
        public void OnButtonClick(String data);
    }

    private OnButtonClickListener buttonClickListener;

    public void setOnButtonClickListener(OnButtonClickListener listener) {
        this.buttonClickListener = listener;
    }

}
