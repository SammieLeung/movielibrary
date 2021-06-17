package com.hphtv.movielibrary.view;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;

import com.firelfy.util.DensityUtil;
import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.databinding.ComponentMyRadioDialogFragmentBinding;
import com.hphtv.movielibrary.roomdb.entity.VideoFile;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

/**
 * Created by tchip on 17-11-23.
 */

public class CustomRadioDialogFragment extends DialogFragment {
    public static final String TAG = CustomRadioDialogFragment.class.getSimpleName();
    public static final String VIDEO_LIST = "videolist";
    public static final String TITLE = "title";
    public static final String CONIFRM_TEXT = "confirm_text";
    public int focusId;
    private String mTitle;
    private String mConfirmText;

    private ComponentMyRadioDialogFragmentBinding mBinding;

    public List<VideoFile> mVideoFileList;

    public CustomRadioDialogFragment() {

    }

    public static CustomRadioDialogFragment newInstance(String title, String confirmText, List<VideoFile> videoFiles) {
        CustomRadioDialogFragment fragment = new CustomRadioDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(VIDEO_LIST, (Serializable) videoFiles);
        bundle.putSerializable(TITLE, title);
        bundle.putSerializable(CONIFRM_TEXT, confirmText);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static CustomRadioDialogFragment newInstance(List<VideoFile> videoFiles) {
        CustomRadioDialogFragment fragment = new CustomRadioDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(VIDEO_LIST, (Serializable) videoFiles);
        fragment.setArguments(bundle);
        return fragment;
    }



    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.component_my_radio_dialog_fragment, container, false);
        mVideoFileList = (List<VideoFile>) getArguments().getSerializable(VIDEO_LIST);
        mVideoFileList.sort((o1, o2) -> o1.filename.compareTo(o2.filename));
        mTitle = getArguments().getString(TITLE);
        mConfirmText = getArguments().getString(CONIFRM_TEXT);
        initView();
        return mBinding.getRoot();
    }

    private void initView() {
        for (int i = 0; i < mVideoFileList.size(); i++) {
            VideoFile file = mVideoFileList.get(i);
            final RadioButton button = newRadioButton(file);
            button.setOnTouchListener((v, event) -> {
                if (((RadioButton) v).isChecked()) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        if (mOnClickListener != null)
                            mOnClickListener.doItemSelect(v.getTag());
                    }
                }
                return false;
            });
            button.setOnKeyListener((v, keyCode, event) -> {
                if (((RadioButton) v).isChecked()) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                            if (mOnClickListener != null)
                                mOnClickListener.doItemSelect(v.getTag());
                        }
                    }
                }
                return false;
            });
            mBinding.rbPathsGroup.addView(button);
        }
        mBinding.rbPathsGroup.check(mBinding.rbPathsGroup.getChildAt(0).getId());
        mBinding.btnPlay.setOnClickListener(v -> {
            int id = mBinding.rbPathsGroup.getCheckedRadioButtonId();
            if (id != -1) {
                VideoFile file = (VideoFile) mBinding.rbPathsGroup.findViewById(id).getTag();
                if (mOnClickListener != null)
                    mOnClickListener.doPositiveClick(file);
            }

        });
        if (!TextUtils.isEmpty(mConfirmText))
            mBinding.btnPlay.setText(mConfirmText);
        if (!TextUtils.isEmpty(mTitle))
            mBinding.title.setText(mTitle);
    }


    @SuppressLint("NewApi")
    public RadioButton newRadioButton(VideoFile file) {

        RadioButton button = new RadioButton(getContext());
        int widthpx = DensityUtil.dip2px(getContext(), 80);
        int heightpx = DensityUtil.dip2px(getContext(), 42);
        RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(
                RadioGroup.LayoutParams.MATCH_PARENT, heightpx);


        // 设置内外边距
        params.leftMargin = 2;
        params.rightMargin = 2;
        button.setLayoutParams(params);
//        button.setPadding(4, 4, 4, 4);
        button.setMinWidth(widthpx);
        button.setTextSize(17f);
        button.setSingleLine(true);
        button.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        // 设置背景
        button.setBackgroundResource(R.drawable.selector_category_bg);
        // 去掉左侧默认的圆点
        button.setButtonDrawable(android.R.color.transparent);

        // 设置不同状态下文字颜色，通过ColorStateList，对应的selector放在res/color文件目录中，否则没有效果
        button.setTextColor(getResources().getColorStateList(
                R.color.selector_category_text, null));
        button.setGravity(Gravity.CENTER);
        button.setText(file.filename);
        button.setTag(file);

        return button;
    }


    public OnClickListener mOnClickListener;

    public interface OnClickListener {
        /**
         * 点击按钮时触发
         *
         * @param obj
         */
        public void doPositiveClick(Object obj);

        /**
         * 点击已选项目时触发
         *
         * @param obj
         */
        public void doItemSelect(Object obj);


    }

    public void setOnClickListener(OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
    }
}
