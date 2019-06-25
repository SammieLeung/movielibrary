package com.hphtv.movielibrary.view;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.hphtv.movielibrary.sqlite.bean.Device;
import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.sqlite.bean.Directory;
import com.hphtv.movielibrary.util.DensityUtil;

public class CategoryView extends LinearLayout implements
        OnCheckedChangeListener, OnTouchListener {
    public static final int RADIOGROUP_SORT_ORDER = 1;
    public static final int RADIOGROUP_SORT_CONDITION = 2;
    public static final int RADIOGROUP_SORT_SPECIAL_DEVICE=3;
    public static final int RADIOGROUP_SORT_SPECIAL_DIR=4;

    private LayoutInflater inflater;

    public CategoryView(Context context) {
        this(context, null);
    }

    public CategoryView(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflater = LayoutInflater.from(context);
    }

    public void addRestButton() {
        View view = getChildAt(0);
        Button button = (Button) view.findViewById(R.id.btn_reset);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 1; i < getChildCount(); i++) {
                    View view = getChildAt(i);
                    if(view.getId()==R.id.sort_group){
                        continue;
                    }
                    RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.container);
                    if (radioGroup != null)
                        radioGroup.check(radioGroup.getChildAt(0).getId());
                }
            }
        });

    }

    /**
     * @param list
     * @param title
     * @param checkPos radio group 选中项
     * @param pos      device 所在pos位置
     */
    public void addConditionForDeviceAt(List<Device> list, String title, int checkPos, int pos) {
        // 加载布局
        View view = getChildAt(pos);
        RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.container);
        radioGroup.setTag(RADIOGROUP_SORT_SPECIAL_DEVICE);// 为radioGroup添加标记
        radioGroup.removeAllViews();
            RadioButton bt = newRadioButton(getResources().getString(R.string.tx_all));
            radioGroup.addView(bt);
            // 默认选中
            if (checkPos == -1) {
                radioGroup.check(bt.getId());
            }

            if (list.size() > 0) {
                // 全部
                for (Device device : list) {
                    int i = list.indexOf(device);
                    bt = newRadioButton(device.getName());// 实例化新的RadioButton
                    radioGroup.addView(bt);
                    if (i == checkPos) {
                        radioGroup.check(bt.getId());
                    }
                }
                // 为当前RadioGroup设置监听器
                radioGroup.setOnCheckedChangeListener(this);
            }
    }

    public void addConditionForDirectoryAt(List<Directory> list, String title, int checkPos, int pos)
    {
// 加载布局
        View view = getChildAt(pos);
        RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.container);
        radioGroup.setTag(RADIOGROUP_SORT_SPECIAL_DIR);// 为radioGroup添加标记
        radioGroup.removeAllViews();
            RadioButton bt = newRadioButton(getResources().getString(R.string.tx_all));
            radioGroup.addView(bt);
            // 默认选中
            if (checkPos == -1) {
                radioGroup.check(bt.getId());
            }
            if (list.size() > 0) {
                // 全部
                for (Directory directory : list) {
                    int i = list.indexOf(directory);
                    bt = newRadioButton(directory.getName());// 实例化新的RadioButton
                    radioGroup.addView(bt);
                    if (i == checkPos) {
                        radioGroup.check(bt.getId());
                    }
                }
                // 为当前RadioGroup设置监听器
                radioGroup.setOnCheckedChangeListener(this);
            }
    }



    public void addConditionAt(List<String> list, String title, int checkPos, int pos) {
        View view = getChildAt(pos);
        RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.container);
        radioGroup.setTag(RADIOGROUP_SORT_CONDITION);// 为radioGroup添加标记
        radioGroup.removeAllViews();
        RadioButton bt = newRadioButton(getResources().getString(R.string.tx_all));
        radioGroup.addView(bt);
        // 默认选中
        if (checkPos == -1) {
            radioGroup.check(bt.getId());
        }
        if (list.size() > 0) {
            // 全部
            for (String str : list) {
                int i = list.indexOf(str);
                bt = newRadioButton(str);// 实例化新的RadioButton
                radioGroup.addView(bt);
                if (i == checkPos) {
                    radioGroup.check(bt.getId());
                }
            }
            // 为当前RadioGroup设置监听器
            radioGroup.setOnCheckedChangeListener(this);
        }
    }


    public void addOrderAt(List<String> list, int checkpos, int pos) {
        // 加载布局
        View view = getChildAt(pos);
        View line = view.findViewById(R.id.line);
        line.setVisibility(View.GONE);
        RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.container);
        radioGroup.setTag(RADIOGROUP_SORT_ORDER);
        RadioButton bt;

        if (list.size() > 0) {
            // 全部
            for (String str : list) {
                int i = list.indexOf(str);
                bt = newRadioButton(str);// 实例化新的RadioButton
                bt.setOnTouchListener(this);// 为每个radiobutton设置监听器
                radioGroup.addView(bt);
                if (i == checkpos)
                    radioGroup.check(bt.getId());
            }
        }
    }


//    public void addSingleItem(List<Device> list, String title, int checkPos) {
//
//        View view = findViewById(R.id.device_group);
//
//        RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.container);
//        radioGroup.setTag(RADIOGROUP_SORT_CONDITION);// 为radioGroup添加标记
//        radioGroup.removeAllViews();
//        RadioButton bt = newRadioButton(getResources().getString(R.string.tx_all));
//        radioGroup.addView(bt);
//
//        if (checkPos == -1)
//            // 默认选中
//            bt.setChecked(true);
//
//        if (list != null && list.size() > 0) {
//            // 全部
//            for (int i = 0; i < list.size(); i++) {
//
//                String str = (String) list.get(i).getName();
//                bt = newRadioButtonForDevice(str);// 实例化新的RadioButton
//                radioGroup.addView(bt);
//                if (i == checkPos) {
//                    radioGroup.check(bt.getId());
//                }
//            }
//            // 为当前RadioGroup设置监听器
//            radioGroup.setOnCheckedChangeListener(this);
//        }
//    }

    /**
     * 创建RadioButton
     */
    @SuppressLint("NewApi")
    private RadioButton newRadioButton(String text) {
        RadioButton button = new RadioButton(getContext());
//        int widthpx = 168;
        int heightpx = DensityUtil.dip2px(getContext(),42);
        RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(
                RadioGroup.LayoutParams.MATCH_PARENT, heightpx);


        // 设置内外边距
        button.setLayoutParams(params);
        button.setTextSize(18f);
        button.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        button.setSingleLine(true);
        button.setMarqueeRepeatLimit(3);
        // 设置背景
        button.setBackgroundResource(R.drawable.selector_category_bg);
        // 去掉左侧默认的圆点
        button.setButtonDrawable(null);
        button.setCompoundDrawablePadding(-25);
        Drawable drawable = getResources().getDrawable(R.drawable.ic_radiobutton);
        drawable.setBounds(-20, 0, 5, 25);
        button.setCompoundDrawables(button.getCompoundDrawables()[0], button.getCompoundDrawables()[0], drawable, button.getCompoundDrawables()[0]);
        // 设置不同状态下文字颜色，通过ColorStateList，对应的selector放在res/color文件目录中，否则没有效果
        button.setTextColor(getResources().getColorStateList(
                R.color.selector_category_text, null));
        button.setGravity(Gravity.CENTER);
        button.setText(text);

        return button;
    }

    /**
     * 创建RadioButton为设备
     */
    @SuppressLint("NewApi")
    private RadioButton newRadioButtonForDevice(String text) {
        RadioButton button = new RadioButton(getContext());
        int heightpx = DensityUtil.dip2px(getContext(),42);
        RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(
                RadioGroup.LayoutParams.MATCH_PARENT, heightpx);


        // 设置内外边距
        button.setLayoutParams(params);
        button.setGravity(Gravity.CENTER);
//        button.setPadding(4, 4, 4, 4);
        button.setTextSize(18f);
        button.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        button.setSingleLine(true);
        button.setMarqueeRepeatLimit(3);

        // 设置背景
        button.setBackgroundResource(R.drawable.selector_category_bg);
        // 去掉左侧默认的圆点
        button.setButtonDrawable(android.R.color.transparent);
        Drawable drawable = getResources().getDrawable(R.drawable.ic_radiobutton);
        drawable.setBounds(-20, 0, 5, 25);
        button.setCompoundDrawables(button.getCompoundDrawables()[0], button.getCompoundDrawables()[0], drawable, button.getCompoundDrawables()[0]);

        // 设置不同状态下文字颜色，通过ColorStateList，对应的selector放在res/color文件目录中，否则没有效果
        button.setTextColor(getResources().getColorStateList(
                R.color.selector_category_text, null));
        button.setGravity(Gravity.CENTER);
        button.setText(text);

        return button;
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (mListener != null) {
            Log.v("CategoryView",checkedId +" trigger check action");
            mListener.click(group, checkedId);
        }
    }

    /**
     * 指定监听器
     */
    public void setOnClickCategoryListener(OnClickCategoryListener l) {
        mListener = l;
    }

    private OnClickCategoryListener mListener;

    /**
     * 回调接口
     */
    public interface OnClickCategoryListener {
        /**
         * 点击事件发生
         */
        public void click(RadioGroup group, int checkedId);
        public void onKeyPress(KeyEvent event,CategoryView view);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            Log.v("lxp", "onTouch");
            if (mListener != null) {
                RadioGroup group = (RadioGroup) v.getParent();
                mListener.click(group, v.getId());
            }
        }
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (mListener != null) {
            mListener.onKeyPress(event,this);
        }
        return super.dispatchKeyEvent(event);
    }

}
