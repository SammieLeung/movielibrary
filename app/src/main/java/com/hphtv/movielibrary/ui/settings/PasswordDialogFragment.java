package com.hphtv.movielibrary.ui.settings;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.data.Config;
import com.hphtv.movielibrary.databinding.CommonPasswordDialogLayoutBinding;
import com.hphtv.movielibrary.ui.BaseDialogFragment2;

import org.jetbrains.annotations.NotNull;

/**
 * Re-Created by tchip on 22-3-21.
 */

public class PasswordDialogFragment extends BaseDialogFragment2<PasswordDialogFragmentViewModel, CommonPasswordDialogLayoutBinding> {
    public String mDialogTitle;

    public static PasswordDialogFragment newInstance() {
        PasswordDialogFragment fragment = new PasswordDialogFragment();
        return fragment;
    }

    @Override
    protected PasswordDialogFragmentViewModel createViewModel() {
        return null;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindDatas();
        initView();
    }

    private void initView() {
        mBinding.btnCancel.setOnClickListener(this::cancel);
        mBinding.btnConfirm.setOnClickListener(this::confirm);
    }

    private void bindDatas() {
        if (Config.getChildModePassword().equals(Config.DEAULT_PASSWORD)) {
            mBinding.setInputHint(getString(R.string.hint_enter_psw));
        } else {
            mBinding.setInputHint(getString(R.string.hint_enter_psw_2));
        }
        mBinding.setDialogTitle(TextUtils.isEmpty(mDialogTitle) ? getString(R.string.title_input_psw) : mDialogTitle);
        mBinding.setViewmodel(mViewModel);
    }

    public void setDialogTitle(String dialogTitle) {
        mDialogTitle = dialogTitle;
    }

    public CommonPasswordDialogLayoutBinding getBinding() {
        return mBinding;
    }

    public void setViewModel(PasswordDialogFragmentViewModel viewModel) {
        mViewModel = viewModel;
    }

    public void errorTips() {
        mBinding.etPsw.setError(getString(R.string.errtips_psw_error));
    }

    public void confirm(View v) {
        if (mViewModel.checkPassword()) {
            if (mOnConfirmListener != null)
                mOnConfirmListener.onConfirm();
            dismiss();
        } else {
            errorTips();
        }
    }

    public void cancel(View v) {
        dismiss();
    }

    private OnConfirmListener mOnConfirmListener;

    public interface OnConfirmListener {
        void onConfirm();
    }

    public void setOnConfirmListener(OnConfirmListener confirmListener) {
        mOnConfirmListener = confirmListener;
    }
}
