package com.hphtv.movielibrary.ui.shortcutmanager.options;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.hphtv.movielibrary.databinding.ShortcutOptionDialogLayoutBinding;
import com.hphtv.movielibrary.ui.BaseDialogFragment2;
import com.hphtv.movielibrary.ui.shortcutmanager.options.remove.ShortcutRemoveConfirmDialog;
import com.hphtv.movielibrary.ui.shortcutmanager.options.scan.ShortcutScanDialog;

import org.jetbrains.annotations.NotNull;

/**
 * author: Sam Leung
 * date:  2022/1/8
 */
public class ShortcutOptionsDialog extends BaseDialogFragment2<ShortcutOptionsViewModel, ShortcutOptionDialogLayoutBinding> implements DialogAction{
    public static ShortcutOptionsDialog newInstance() {

        Bundle args = new Bundle();

        ShortcutOptionsDialog fragment = new ShortcutOptionsDialog();
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    protected ShortcutOptionsViewModel createViewModel() {
        return mViewModel=new ViewModelProvider(getActivity()).get(ShortcutOptionsViewModel.class);
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding.btnRemove.setOnClickListener(this::removeShortcut);
        mBinding.btnScan.setOnClickListener(this::showScanDialog);
        mBinding.btnClose.setOnClickListener(this::dismiss);
        mBinding.setDialogTitle(mViewModel.getShortcut().firendlyName);
    }

    @Override
    public void show() {
        getDialog().show();
    }

    @Override
    public void hide() {
        getDialog().hide();
    }

    @Override
    public ViewModelStoreOwner getOwner() {
        return this;
    }

    private void showScanDialog(View v){
        ShortcutScanDialog dialog= ShortcutScanDialog.newInstance();
        dialog.show(getActivity().getSupportFragmentManager(),"");
        dismiss();
    }

    private void removeShortcut(View v){
        ShortcutRemoveConfirmDialog dialog=ShortcutRemoveConfirmDialog.newInstance();
        dialog.show(getActivity().getSupportFragmentManager(),"");
        dismiss();
    }

    private void dismiss(View v){
        dismiss();
    }
}
