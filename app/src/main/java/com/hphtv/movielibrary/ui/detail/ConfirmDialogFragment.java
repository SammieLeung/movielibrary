package com.hphtv.movielibrary.ui.detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;

import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.databinding.ComponentBaseDialogFragmentBinding;

import org.jetbrains.annotations.NotNull;

/**
 * Created by tchip on 17-11-10.
 */

public class ConfirmDialogFragment extends DialogFragment implements View.OnClickListener {

    private ComponentBaseDialogFragmentBinding mBinding;
    private String mMessage;

    public static ConfirmDialogFragment newInstance() {

        Bundle args = new Bundle();

        ConfirmDialogFragment fragment = new ConfirmDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.component_base_dialog_fragment, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding.dialogMsg.setText(mMessage);
        mBinding.btnCancel.setOnClickListener(this);
        mBinding.btnOk.setOnClickListener(this);
    }


    //    @Override
//    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        Dialog dialog = new Dialog(context);
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        dialog.setContentView(view);
//        dialog.getWindow().setLayout(DensityUtil.dip2px(context,394), RelativeLayout.LayoutParams.WRAP_CONTENT);
//        return dialog;
//    }


//    public ConfirmDialogFragment setPositiveButton(String text, OnPositiveListener listener, int which, boolean isFocus) {
//        mPlistener = listener;
//        Button button = null;
//        switch (which) {
//            case BTN_POSITIVE:
//                button = (Button) view.findViewById(R.id.btn_cancel);
//                break;
//            case BTN_NAGETIVE:
//                button = (Button) view.findViewById(R.id.btn_ok);
//                break;
//        }
//        button.setText(text);
//        button.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dismiss();
//                if (mPlistener != null) {
//                    mPlistener.OnPositivePress((Button) v);
//                }
//            }
//        });
//        if (isFocus)
//            button.requestFocus();
//        return this;
//    }

//    public ConfirmDialogFragment setNegativeButton(String text, OnNagetiveListener listener, int which, boolean isFocus) {
//        mNlistener = listener;
//        Button button = null;
//        switch (which) {
//            case BTN_POSITIVE:
//                button = (Button) view.findViewById(R.id.btn_cancel);
//                break;
//            case BTN_NAGETIVE:
//                button = (Button) view.findViewById(R.id.btn_ok);
//                break;
//        }
//
//        button.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (mNlistener != null) {
//                    mNlistener.OnNagetivePress((Button) v);
//                }
//                dismiss();
//            }
//        });
//        button.setText(text);
//        if (isFocus)
//            button.requestFocus();
//        return this;
//    }

    public ConfirmDialogFragment setMessage(String title) {
        mMessage=title;
        return this;
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ok:
                if (mOnClickListener != null)
                    mOnClickListener.OK();
                break;
            case R.id.btn_cancel:
                if (mOnClickListener != null)
                    mOnClickListener.Cancel();
                break;
        }
        dismiss();
    }

    public interface OnClickListener {
        public void OK();
        public void Cancel();
    }

    private OnClickListener mOnClickListener;
}
