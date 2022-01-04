package com.hphtv.movielibrary.ui.shortcutmanager.options;


import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hphtv.movielibrary.databinding.ShortcutOptionsDialogLayoutBinding;
import com.hphtv.movielibrary.roomdb.entity.Shortcut;
import com.hphtv.movielibrary.ui.BaseDialogFragment2;

import org.jetbrains.annotations.NotNull;

/**
 * 索引菜单
 *
 * author: Sam Leung
 * date:  2021/12/30
 */
public class ShortcutOptionsDialog extends BaseDialogFragment2<ShortcutOptionsViewModel, ShortcutOptionsDialogLayoutBinding> {
    public static final String KEY_SHORTCUT = "shortcut";

    public static ShortcutOptionsDialog newInstance(Shortcut shortcut) {
        Bundle args = new Bundle();
        args.putSerializable(KEY_SHORTCUT, shortcut);
        ShortcutOptionsDialog fragment = new ShortcutOptionsDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel.setShortcut((Shortcut) getArguments().getSerializable(KEY_SHORTCUT));
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding.setDialogTitle(mViewModel.getShortcut().firendlyName);
        mBinding.setTypeItem(mViewModel.getTypeItem());
        mBinding.setAccessItem(mViewModel.getAccessItem());
        mBinding.setNameItem(mViewModel.getNameItem());
        mBinding.shortcutTypeItem.viewOptions.setOnClickListener(mViewModel::showShortcutType);
        mBinding.shortcutAccessItem.viewOptions.setOnClickListener(mViewModel::showAccessPermission);
        mBinding.shortcutNameItem.viewOptions.setOnClickListener(mViewModel::showEditName);

    }
}
