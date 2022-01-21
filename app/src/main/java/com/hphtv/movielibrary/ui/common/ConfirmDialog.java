package com.hphtv.movielibrary.ui.common;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;

import com.hphtv.movielibrary.databinding.CommonConfirmDialogLayoutBinding;
import com.hphtv.movielibrary.databinding.CommonEditextDialogLayoutBinding;
import com.hphtv.movielibrary.ui.BaseDialogFragment2;

import org.jetbrains.annotations.NotNull;

/**
 * author: Sam Leung
 * date:  2022/1/5
 */
public abstract class ConfirmDialog<VM extends AndroidViewModel> extends BaseDialogFragment2<VM, CommonConfirmDialogLayoutBinding> {

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding.btnConfirm.setOnClickListener(this::confirm);
        mBinding.btnCancel.setOnClickListener(this::cancel);
    }

    public abstract void confirm(View v);
    public abstract void cancel(View v);
}
