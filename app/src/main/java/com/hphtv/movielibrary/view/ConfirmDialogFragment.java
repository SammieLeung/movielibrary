package com.hphtv.movielibrary.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.util.DensityUtil;

/**
 * Created by tchip on 17-11-10.
 */

public class ConfirmDialogFragment extends DialogFragment {

    public static final int BTN_POSITIVE = 0;
    public static final int BTN_NAGETIVE = 1;
    private LayoutInflater inflate;
    private Context context;
    private View view;

    public ConfirmDialogFragment() {

    }

    @SuppressLint("ValidFragment")
    public ConfirmDialogFragment(Context context) {
        this.context = context;
        view = ((Activity) context).getLayoutInflater().inflate(R.layout.component_base_dialog_fragment,null,false);
        setPositiveButton(null);
        setNegativeButton(null);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        this.context = getActivity();
        super.onCreate(savedInstanceState);

    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(view);
        dialog.getWindow().setLayout(DensityUtil.dip2px(context,394), RelativeLayout.LayoutParams.WRAP_CONTENT);
        return dialog;
    }

    public ConfirmDialogFragment setPositiveButton(String text, OnPositiveListener listener, int which, boolean isFocus) {
        mPlistener = listener;
        Button button = null;
        switch (which) {
            case BTN_POSITIVE:
                button = (Button) view.findViewById(R.id.positive_btn);
                break;
            case BTN_NAGETIVE:
                button = (Button) view.findViewById(R.id.nagetive_btn);
                break;
        }
        button.setText(text);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (mPlistener != null) {
                    mPlistener.OnPositivePress((Button) v);
                }
            }
        });
        if (isFocus)
            button.requestFocus();
        return this;
    }

    public ConfirmDialogFragment setNegativeButton(String text, OnNagetiveListener listener, int which, boolean isFocus) {
        mNlistener = listener;
        Button button = null;
        switch (which) {
            case BTN_POSITIVE:
                button = (Button) view.findViewById(R.id.positive_btn);
                break;
            case BTN_NAGETIVE:
                button = (Button) view.findViewById(R.id.nagetive_btn);
                break;
        }

        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mNlistener != null) {
                    mNlistener.OnNagetivePress((Button) v);
                }
                dismiss();
            }
        });
        button.setText(text);
        if (isFocus)
            button.requestFocus();
        return this;
    }


    public ConfirmDialogFragment setPositiveButton(String string, OnPositiveListener listener, boolean isFocus) {
        return setPositiveButton(string, listener, BTN_POSITIVE, isFocus);
    }

    public ConfirmDialogFragment setPositiveButton(OnPositiveListener listener, boolean isFocus) {
        return setPositiveButton(context.getResources().getString(R.string.btn_yes), listener, isFocus);

    }

    public ConfirmDialogFragment setPositiveButton(OnPositiveListener listener) {
        return setPositiveButton(listener, false);

    }

    public ConfirmDialogFragment setNegativeButton(String string, OnNagetiveListener listener, boolean isFocus) {
        return setNegativeButton(string, listener, BTN_NAGETIVE, isFocus);
    }

    public ConfirmDialogFragment setNegativeButton(OnNagetiveListener listener, boolean isFocus) {
        return setNegativeButton(context.getResources().getString(R.string.btn_no), listener, isFocus);
    }

    public ConfirmDialogFragment setNegativeButton(OnNagetiveListener listener) {
        return setNegativeButton(listener, false);
    }

    public ConfirmDialogFragment setMessage(String title) {
        TextView dialog_msg = (TextView) view.findViewById(R.id.dialog_msg);
        dialog_msg.setText(title);
        return this;
    }

    public interface OnPositiveListener {
        public void OnPositivePress(Button button);
    }

    private OnPositiveListener mPlistener;

    public interface OnNagetiveListener {
        public void OnNagetivePress(Button button);
    }

    private OnNagetiveListener mNlistener;
}
