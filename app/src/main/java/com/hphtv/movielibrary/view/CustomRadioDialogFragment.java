package com.hphtv.movielibrary.view;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.hphtv.movielibrary.sqlite.bean.VideoFile;
import com.hphtv.movielibrary.R;

import java.util.ArrayList;

/**
 * Created by tchip on 17-11-23.
 */

public class CustomRadioDialogFragment extends DialogFragment {
    public static final String TAG = CustomRadioDialogFragment.class.getSimpleName();
    DialogSetting mDialogSetting;
    public int focusId;
    RadioGroup radioGroup;
    LinearLayout llGroup;
    private TextView title;
    private Button confirm;
    private String[] textData = new String[2];
    private int mType;
    public static final int TYPE_RADIO = 0;
    public static final int TYPE_CHECKBOX = 1;
    public ScrollView sv_1, sv_2;

    public CustomRadioDialogFragment() {

    }

    public static CustomRadioDialogFragment newInstance(DialogSetting dialogSetting, int type) {

        CustomRadioDialogFragment fragment = new CustomRadioDialogFragment();
        fragment.mType = type;
        fragment.mDialogSetting = dialogSetting;
        return fragment;
    }

    public static CustomRadioDialogFragment newInstance(DialogSetting dialogSetting) {
        return newInstance(dialogSetting, TYPE_RADIO);
    }

    @Override
    public void onResume() {
        Log.v(TAG, "onResume()");
        if (radioGroup != null && focusId != -1) {
            View view = radioGroup.findViewById(focusId);
            if (view != null) {
                view.requestFocus();
            }
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.v(TAG, "onPause()");
        if (radioGroup != null) {
            focusId = radioGroup.getCheckedRadioButtonId();
        }
        super.onPause();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.component_my_radio_dialog_fragment, null);
        llGroup = (LinearLayout) view.findViewById(R.id.ll_paths_group);
        radioGroup = (RadioGroup) view.findViewById(R.id.rb_paths_group);
        sv_1 = (ScrollView) view.findViewById(R.id.sv_1);
        sv_2 = (ScrollView) view.findViewById(R.id.sv_2);
        title = (TextView) view.findViewById(R.id.title);
        confirm = (Button) view.findViewById(R.id.btn_play);
        Button playBtn = (Button) view.findViewById(R.id.btn_play);

        VideoFile[] files = mDialogSetting.getVideoFiles();
        if (textData[0] != null)
            title.setText(textData[0]);

        if (textData[1] != null)
            confirm.setText(textData[1]);

        if (mType == TYPE_RADIO) {
            sv_2.setVisibility(View.GONE);
            sv_1.setVisibility(View.VISIBLE);
            if (files != null && files.length > 0) {
                for (int i = 0; i < files.length; i++) {
                    VideoFile file = files[i];
                    final RadioButton button = newRadioButton(file);
                    button.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            if (((RadioButton) v).isChecked()) {
                                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                                    LogUtil.v(TAG, "onTouch run 1");
                                    mDialogSetting.doItemSelect(v.getTag());
                                }
                            }
                            return false;
                        }
                    });
                    button.setOnKeyListener(new View.OnKeyListener() {
                        @Override
                        public boolean onKey(View v, int keyCode, KeyEvent event) {
                            if (((RadioButton) v).isChecked()) {
                                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                                    if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                                        mDialogSetting.doItemSelect(v.getTag());
                                    }
                                }
                            }
                            return false;
                        }
                    });
                    radioGroup.addView(button);
                }
                radioGroup.check(radioGroup.getChildAt(0).getId());
            }
            playBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int id = radioGroup.getCheckedRadioButtonId();
                    if (id != -1) {
                        VideoFile file = (VideoFile) radioGroup.findViewById(id).getTag();
                        mDialogSetting.doPositiveClick(file);
                    }

                }
            });


        } else {
            sv_1.setVisibility(View.GONE);
            sv_2.setVisibility(View.VISIBLE);
            if (files != null && files.length > 0) {
                for (int i = 0; i < files.length; i++) {
                    VideoFile file = files[i];
                    final CheckBox button = newCheckbutton(file);
                    llGroup.addView(button);
                }

            }
            playBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int count = llGroup.getChildCount();
                    ArrayList<VideoFile> videoFiles = new ArrayList<>();
                    for (int i = 0; i < count; i++) {
                        CheckBox button = (CheckBox) llGroup.getChildAt(i);
                        if (button.isChecked()) {
                            videoFiles.add((VideoFile) button.getTag());

                        }
                    }
                    mDialogSetting.doPositiveClick(videoFiles);
                }
            });

        }

        Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(view);
        return dialog;

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
//        String path=file.getUri();
//        Pattern p = Pattern.compile(".+/(.+)$");
//        Matcher m = p.matcher(path);
//        if (m.find()) {
//            button.setText(m.group(1));
//        } else {
//            button.setText(path);
//        }
        button.setText(file.getFilename());
        button.setTag(file);

        return button;
    }

    @SuppressLint("NewApi")
    public CheckBox newCheckbutton(VideoFile file) {

        CheckBox button = new CheckBox(getContext());
        int widthpx = DensityUtil.dip2px(getContext(), 80);
        int heightpx = DensityUtil.dip2px(getContext(), 42);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, heightpx);


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
//        String path=file.getUri();
//        Pattern p = Pattern.compile(".+/(.+)$");
//        Matcher m = p.matcher(path);
//        if (m.find()) {
//            button.setText(m.group(1));
//        } else {
//            button.setText(path);
//        }
        button.setText(file.getFilename());
        button.setTag(file);

        return button;
    }

    public interface DialogSetting {
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

        /**
         * 数据
         *
         * @return
         */
        public VideoFile[] getVideoFiles();

    }

    public void setTitle(String title) {
        this.textData[0] = title;
    }

    public void setConfirmText(String text) {
        this.textData[1] = text;
    }

}
