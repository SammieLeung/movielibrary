package com.hphtv.movielibrary.ui.shortcutmanager.options.scan.sub;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.ui.common.EditDialog;
import com.hphtv.movielibrary.ui.shortcutmanager.options.DialogAction;
import com.hphtv.movielibrary.ui.shortcutmanager.options.ShortcutOptionsViewModel;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;

/**
 * author: Sam Leung
 * date:  2022/1/6
 */
public class ShortcutNameEditDialog extends EditDialog<ShortcutOptionsViewModel> {
    private WeakReference<DialogAction> mReference;
    private String mText;

    public static ShortcutNameEditDialog newInstance(DialogAction dialog) {

        Bundle args = new Bundle();
        ShortcutNameEditDialog fragment = new ShortcutNameEditDialog();
        fragment.mReference = new WeakReference<>(dialog);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected ShortcutOptionsViewModel createViewModel() {
        return  mViewModel=new ViewModelProvider(getActivity()).get(ShortcutOptionsViewModel.class);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mText=mViewModel.getNameItem().getSubTitle();
        mBinding.btnConfirm.setOnClickListener(this::confirm);
        mBinding.btnCancel.setOnClickListener(this::cancel);
        mBinding.setDialogTitle(getString(R.string.shortcut_name_editdialog_title));
        mBinding.setInputHint(getString(R.string.shortcut_name_editdialog_hint));
        mBinding.setInputText(mText);
    }


    private void confirm(View v){
        mViewModel.getNameItem().setSubTitle(mBinding.getInputText());
        dismiss();
    }

    private void cancel(View v){
        dismiss();
    }

    @Override
    public void onDismiss(@NonNull @NotNull DialogInterface dialog) {
        super.onDismiss(dialog);
        mReference.get().show();
    }
}
