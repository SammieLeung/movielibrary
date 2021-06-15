package com.hphtv.movielibrary.view;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import androidx.annotation.NonNull;

import com.firelfy.util.DensityUtil;
import com.firelfy.util.SharePreferencesTools;
import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.data.ConstData;
import com.hphtv.movielibrary.roomdb.entity.Device;

public class CategoryView extends LinearLayout implements
        OnCheckedChangeListener, OnTouchListener {


    public static final String DESC = "↓";
    public static final String ASC = "↑";
    private List<Device> mDevices;
    private List<String> mYears;
    private List<String> mGenres;
    private List<String> mSortFilters;
    private boolean isDesc = false;

    private int mCurDevPos = -1;
    private int mCurYearPos = -1;
    private int mCurGenrePos = -1;
    private int mCurSortPos = 0;

    private RadioGroup mSortRadioGroup;
    private UIHandler mUIHandler = new UIHandler();


    public CategoryView(Context context) {
        this(context, null);
    }

    public CategoryView(Context context, AttributeSet attrs) {
        super(context, attrs);
        prepareSortFilters();
    }

    public CategoryView setDevices(List<Device> devices) {
        mDevices = devices;
        return this;
    }

    public CategoryView setYears(List<String> years) {
        mYears = years;
        return this;
    }

    public CategoryView setGenres(List<String> genres) {
        mGenres = genres;
        return this;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setUpRestButton();
    }

    public boolean isDesc() {
        return isDesc;
    }

    public Device getDevice() {
        if (mCurDevPos == -1)
            return null;
        else
            return mDevices.get(mCurDevPos);
    }

    public String getYear() {
        if (mCurYearPos == -1)
            return "";
        else
            return mYears.get(mCurYearPos);
    }

    public String getGenre() {
        if (mCurGenrePos == -1)
            return "";
        else
            return mGenres.get(mCurGenrePos);
    }

    public String getSortType() {
        return mSortFilters.get(mCurSortPos);
    }

    public int getSortTypePos() {
        return mCurSortPos;
    }

    private void prepareCheckPosition() {
        String devicePath = SharePreferencesTools.getInstance(getContext()).readProperty(ConstData.SharePreferenceKeys.DEVICE, "");
        String year = SharePreferencesTools.getInstance(getContext()).readProperty(ConstData.SharePreferenceKeys.YEAR, "");
        String genre = SharePreferencesTools.getInstance(getContext()).readProperty(ConstData.SharePreferenceKeys.GENRE, "");
        String sort_type = SharePreferencesTools.getInstance(getContext()).readProperty(ConstData.SharePreferenceKeys.SORTTYPE, getResources().getString(R.string.order_name));
        boolean isDesc = SharePreferencesTools.getInstance(getContext()).readProperty(ConstData.SharePreferenceKeys.SORT_BY_DESC, false);
        int i = 0;
        for (Device device : mDevices) {
            if (devicePath.equalsIgnoreCase(device.localPath)) {
                mCurDevPos = i;
            }//TODO network 设备考虑
            i++;
        }
        mCurYearPos = mYears.indexOf(year);
        mCurGenrePos = mGenres.indexOf(genre);
        mCurSortPos = mSortFilters.indexOf(sort_type);
        this.isDesc = isDesc;
    }

    private void prepareSortFilters() {
        mSortFilters = new ArrayList<>();
        mSortFilters.add(getResources().getString(R.string.order_name));
        mSortFilters.add(getResources().getString(R.string.order_score));
        mSortFilters.add(getResources().getString(R.string.order_type));
        mSortFilters.add(getResources().getString(R.string.order_year));
        mSortFilters.add(getResources().getString(R.string.order_addtime));
    }

    public void create() {
//        prepareCheckPosition();
        mUIHandler.post(() -> {
            addConditionAt(mDevices, mCurDevPos, 1, obj -> ((Device) obj).name);
            addConditionAt(mYears, mCurYearPos, 2, null);
            addConditionAt(mGenres, mCurGenrePos, 3, null);
            addSort();
            addOrder();
        });

    }


    private void setUpRestButton() {
        View view = getChildAt(0);
        Button button = (Button) view.findViewById(R.id.btn_reset);
        button.setOnClickListener(v -> {
            for (int i = 1; i < getChildCount(); i++) {
                View view1 = getChildAt(i);
                if (view1.getId() == R.id.sort_group) {
                    continue;
                }
                RadioGroup radioGroup = (RadioGroup) view1.findViewById(R.id.container);
                if (radioGroup != null)
                    radioGroup.check(radioGroup.getChildAt(0).getId());
            }
        });
    }


    private void addConditionAt(List<?> list, int checkPos, int pos, ConditionNameGetter nameGetter) {
        View view = getChildAt(pos);
        RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.container);
        radioGroup.setTag(pos);
        radioGroup.removeAllViews();
        RadioButton bt = newRadioButton(getResources().getString(R.string.tx_all));
        radioGroup.addView(bt);
        // 默认选中
        if (checkPos == -1) {
            bt.setChecked(true);
        }
        if (list.size() > 0) {
            // 全部
            for (Object object : list) {
                int i = list.indexOf(object);
                String name = "";
                if (nameGetter == null)
                    name = (String) object;
                else
                    name = nameGetter.getName(object);
                bt = newRadioButton(name);// 实例化新的RadioButton
                radioGroup.addView(bt);
                if (i == checkPos) {
                    bt.setChecked(true);
                }
            }
            // 为当前RadioGroup设置监听器
            radioGroup.setOnCheckedChangeListener(this);
        }
    }


    private void addSort() {
        // 加载布局
        View view = getChildAt(4);
        View line = view.findViewById(R.id.line);
        line.setVisibility(View.GONE);
        mSortRadioGroup = (RadioGroup) view.findViewById(R.id.container);
        mSortRadioGroup.removeAllViews();
        RadioButton bt;

        if (mSortFilters != null && mSortFilters.size() > 0) {
            // 全部

            for (String sortFilter : mSortFilters) {
                int i = mSortFilters.indexOf(sortFilter);
                bt = newRadioButton(sortFilter);// 实例化新的RadioButton
                bt.setOnTouchListener(this);// 为每个radiobutton设置监听器
                mSortRadioGroup.addView(bt);
                if (i == mCurSortPos)
                  bt.setChecked(true);
            }
        }
    }


    /**
     * 添加排序方式
     */
    private void addOrder() {
        if (mSortRadioGroup != null) {
            ((RadioButton) mSortRadioGroup.getChildAt(mCurSortPos)).append(isDesc ? DESC : ASC);
        }
    }

    /**
     * 更新排序方式--需要UI线程执行
     */
    private void updateOrder() {
        SharePreferencesTools.getInstance(getContext()).saveProperty(ConstData.SharePreferenceKeys.SORTTYPE, mSortFilters.get(mCurSortPos));
        SharePreferencesTools.getInstance(getContext()).saveProperty(ConstData.SharePreferenceKeys.SORT_BY_DESC, isDesc);
        mUIHandler.post(() -> {
            for (int i = 0; i < mSortRadioGroup.getChildCount(); i++) {
                RadioButton radioButton = (RadioButton) mSortRadioGroup.getChildAt(i);
                radioButton.setText(mSortFilters.get(i));
            }
            addOrder();
        });

    }

    /**
     * 创建RadioButton
     */
    @SuppressLint("NewApi")
    private RadioButton newRadioButton(String text) {
        RadioButton button = new RadioButton(getContext());
//        int widthpx = 168;
        int heightpx = DensityUtil.dip2px(getContext(), 42);
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


    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (mListener != null) {
            int index = (int) group.getTag();
            View view = group.findViewById(checkedId);
            int checkPos = group.indexOfChild(view) - 1;

            switch (index) {
                case 1:
                    mCurDevPos = checkPos;
                    break;
                case 2:
                    mCurYearPos = checkPos;
                    break;
                case 3:
                    mCurGenrePos = checkPos;
                    break;
            }
            mListener.onConditionChange(getDevice(), getYear(), getGenre());
        }
    }

    /**
     * 指定监听器
     */
    public void setOnClickCategoryListener(OnClickCategoryListener l) {
        mListener = l;
    }

    private OnClickCategoryListener mListener;

    public void resetDevicePos() {
        mCurDevPos=-1;
    }

    /**
     * 回调接口
     */
    public interface OnClickCategoryListener {
        /**
         * 排序条件改变
         *
         * @param sortType
         * @param isDesc
         */
        public void onSortChange(int sortType, boolean isDesc);

        public void onConditionChange(Device device, String year, String genre);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (mListener != null) {
                int pos = mSortRadioGroup.indexOfChild(v);
                if (mCurSortPos == pos)
                    isDesc = !isDesc;
                else
                    mCurSortPos = pos;
                mListener.onSortChange(mCurSortPos, isDesc);
                updateOrder();
            }
        }
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (mListener != null) {
            if ((event.getKeyCode() == KeyEvent.KEYCODE_ENTER || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER)) {
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    View view = getFocusedChild();
                    if (view != null) {
                        int index = mSortRadioGroup.indexOfChild(view);
                        if (index != -1) {
                            if (mCurSortPos == index)
                                isDesc = !isDesc;
                            else
                                mCurSortPos = index;
                            mListener.onSortChange(mCurSortPos, isDesc);
                            updateOrder();
                        }
                    }
                }
            }
        }
        return super.dispatchKeyEvent(event);
    }

    public interface ConditionNameGetter {
        public String getName(Object obj);
    }

    public class UIHandler extends Handler {

        public UIHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void dispatchMessage(@NonNull Message msg) {
            super.dispatchMessage(msg);
        }
    }

}
