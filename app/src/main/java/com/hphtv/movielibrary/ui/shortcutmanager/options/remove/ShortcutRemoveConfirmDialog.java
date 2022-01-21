package com.hphtv.movielibrary.ui.shortcutmanager.options.remove;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.ui.common.ConfirmDialog;
import com.hphtv.movielibrary.ui.shortcutmanager.options.ShortcutOptionsViewModel;

import org.jetbrains.annotations.NotNull;

/**
 * author: Sam Leung
 * date:  2022/1/8
 */
public class ShortcutRemoveConfirmDialog extends ConfirmDialog<ShortcutOptionsViewModel> {
    public static ShortcutRemoveConfirmDialog newInstance() {

        Bundle args = new Bundle();

        ShortcutRemoveConfirmDialog fragment = new ShortcutRemoveConfirmDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected ShortcutOptionsViewModel createViewModel() {
        return mViewModel = new ViewModelProvider(getActivity()).get(ShortcutOptionsViewModel.class);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding.setDialogTitle(getString(R.string.shortcut_remove_dialog_content, mViewModel.getShortcut().firendlyName));
        mBinding.btnConfirm.setOnClickListener(this::confirm);
        mBinding.btnCancel.setOnClickListener(this::cancel);
    }

    @Override
    public void confirm(View v) {
        mViewModel.removeShortcut(mViewModel.getShortcut());
        dismiss();
    }

    @Override
    public void cancel(View v) {
        dismiss();
    }

}
