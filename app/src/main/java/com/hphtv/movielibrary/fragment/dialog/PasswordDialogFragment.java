package com.hphtv.movielibrary.fragment.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import com.firelfy.util.DensityUtil;
import com.firelfy.util.LogUtil;
import com.hphtv.movielibrary.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tchip on 18-3-20.
 */

public class PasswordDialogFragment extends DialogFragment {
    public static final String TAG = PasswordDialogFragment.class.getSimpleName();
    private List<EditText> editTextList;
    private List<String> hintList;
    private View view;
    private Button btn_yes;
    private Button btn_no;
    private LinearLayout edit_group;
    private TextView tips;
    private TextView tv_title;
    private String title;
    private List<String> errorMsg;
    private Context context;

    public static PasswordDialogFragment newInstance(Context context) {
        PasswordDialogFragment fragment = new PasswordDialogFragment();
        fragment.context = context;
        return fragment;
    }

    public PasswordDialogFragment addPswEditText(String hint) {
        if (hintList == null) {
            hintList = new ArrayList<>();
            editTextList = new ArrayList<>();
        }
        hintList.add(hint);
        return this;
    }

    public PasswordDialogFragment addTips(String tips) {
        if (errorMsg == null) {
            errorMsg = new ArrayList<>();
        }
        errorMsg.add(tips);
        return this;
    }

    public PasswordDialogFragment setTitle(String title) {
        this.title = title;
        return this;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.component_password_dialog_fragment, null);
        btn_yes = (Button) view.findViewById(R.id.btn_yes);
        btn_no = (Button) view.findViewById(R.id.btn_no);
        edit_group = (LinearLayout) view.findViewById(R.id.edit_group);
        tv_title = (TextView) view.findViewById(R.id.title);
        tv_title.setText(title);
        tips = (TextView) view.findViewById(R.id.tips);
        for (int i = 0; i < hintList.size(); i++) {
            EditText editText = (EditText) inflater.inflate(R.layout.psw_edittext_item, edit_group, false);
            editText.setHint(hintList.get(i));
            edit_group.addView(editText);
            editTextList.add(editText);
        }
        btn_yes.setOnClickListener(mOnClickListener);
        btn_no.setOnClickListener(mOnClickListener);
        Dialog dialog = new Dialog(getActivity(), R.style.DialogStyle_transparent);
        dialog.setContentView(view);
        dialog.getWindow().setLayout(DensityUtil.dip2px(context,394), RelativeLayout.LayoutParams.WRAP_CONTENT);
        return dialog;
    }


    public void showTips(int i) {
        if (errorMsg != null && i < errorMsg.size()) {
            tips.setText(errorMsg.get(i));
        }
    }

    public void hideTips() {
        try {
            tips.setText(null);
        } catch (Exception e) {

        }

    }

    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_yes:
                    listener.onConfirm(editTextList);
                    break;
                case R.id.btn_no:
                    listener.onCancle();
                    dismiss();
                    break;
            }
        }
    };

    public interface OnClickListener {
        public void onConfirm(List<EditText> editTextList);

        public void onCancle();
    }

    private OnClickListener listener;

    public void setOnClickListener(OnClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void dismiss() {
        LogUtil.v(TAG, "dismiss()");
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        super.dismiss();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        LogUtil.v(TAG, "onCancle()");
        listener.onCancle();
        super.onCancel(dialog);
    }


    public PasswordDialogFragment InputPassword() {

        String title = context.getResources().getString(R.string.psw_input);
        String et_hint1 = context.getResources().getString(R.string.psw_hints_input);

        String tips_1 = context.getResources().getString(R.string.psw_tips_psw_len);
        String tips_2 = context.getResources().getString(R.string.psw_tips_psw_err);
        return this.setTitle(title).addTips(tips_1).addTips(tips_2).addPswEditText(et_hint1);
    }

    public PasswordDialogFragment ChangePassword() {

        String title = context.getResources().getString(R.string.psw_change);
        String et_hint1 = context.getResources().getString(R.string.psw_hints_change_1);
        String et_hint2 = context.getResources().getString(R.string.psw_hints_change_2);
        String et_hint3 = context.getResources().getString(R.string.psw_hints_change_3);
        String tips_1 = context.getResources().getString(R.string.psw_tips_psw_len);
        String tips_2 = context.getResources().getString(R.string.psw_tips_psw_unmatch);
        String tips_3 = context.getResources().getString(R.string.psw_tips_psw_err);
        return this.setTitle(title).addTips(tips_1).addTips(tips_2).addTips(tips_3).addPswEditText(et_hint1).addPswEditText(et_hint2).addPswEditText(et_hint3);
    }


    public PasswordDialogFragment SetPassword() {

        String title = context.getResources().getString(R.string.psw_set);
        String et_hint1 = context.getResources().getString(R.string.psw_hints_set_1);
        String et_hint2 = context.getResources().getString(R.string.psw_hints_set_2);
        String tips_1 = context.getResources().getString(R.string.psw_tips_psw_len);
        String tips_2 = context.getResources().getString(R.string.psw_tips_psw_unmatch);
        return this.setTitle(title).addTips(tips_1).addTips(tips_2).addPswEditText(et_hint1).addPswEditText(et_hint2);
    }
}
