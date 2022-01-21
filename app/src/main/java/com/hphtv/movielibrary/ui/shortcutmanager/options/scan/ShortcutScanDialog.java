package com.hphtv.movielibrary.ui.shortcutmanager.options.scan;


import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.databinding.ShortcutScanDialogLayoutBinding;
import com.hphtv.movielibrary.ui.BaseDialogFragment2;
import com.hphtv.movielibrary.ui.shortcutmanager.options.DialogAction;
import com.hphtv.movielibrary.ui.shortcutmanager.options.ShortcutOptionsViewModel;
import com.hphtv.movielibrary.ui.shortcutmanager.options.scan.sub.ShortcutNameEditDialog;
import com.hphtv.movielibrary.ui.shortcutmanager.options.scan.sub.ShortcutScanItemSelectFragment;

import org.jetbrains.annotations.NotNull;

/**
 * 索引菜单
 * <p>
 * author: Sam Leung
 * date:  2021/12/30
 */
public class ShortcutScanDialog extends BaseDialogFragment2<ShortcutOptionsViewModel, ShortcutScanDialogLayoutBinding> implements DialogAction {
    public static final String TAG = ShortcutScanDialog.class.getSimpleName();
    public static final String KEY_SHORTCUT = "shortcut";
    private ShortcutScanItemSelectFragment mTypeFragment, mAccessFragment;
    private volatile boolean confirmFlag=false;
    public static ShortcutScanDialog newInstance() {
        Bundle args = new Bundle();
        ShortcutScanDialog fragment = new ShortcutScanDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel.loadShortcutData();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding.setDialogTitle(mViewModel.getShortcut().firendlyName);
        mBinding.setTypeItem(mViewModel.getTypeItem());
        mBinding.setAccessItem(mViewModel.getAccessItem());
        mBinding.setNameItem(mViewModel.getNameItem());
        mBinding.setShowSub(mViewModel.getShowSubDialogFlag());
        mBinding.shortcutTypeItem.viewOptions.setOnClickListener(this::showShortcutType);
        mBinding.shortcutAccessItem.viewOptions.setOnClickListener(this::showAccessPermission);
        mBinding.shortcutNameItem.viewOptions.setOnClickListener(this::showEditName);
        mBinding.btnScan.setOnClickListener(this::confirmScan);
    }

    @Override
    protected ShortcutOptionsViewModel createViewModel() {
        return mViewModel=new ViewModelProvider(getActivity()).get(ShortcutOptionsViewModel.class);
    }

    @Override
    public void onStart() {
        super.onStart();
        mTypeFragment = ShortcutScanItemSelectFragment.newInstance(this);
        mTypeFragment.setItem(mViewModel.getTypeItem());
        mAccessFragment = ShortcutScanItemSelectFragment.newInstance(this);
        mAccessFragment.setItem(mViewModel.getAccessItem());
    }

    /**
     * 显示影片类型选择
     *
     * @param v
     */
    public void showShortcutType(View v) {
        Log.d(TAG, "showShortcutType: ");
        if (getChildFragmentManager().findFragmentByTag("type") == null) {
            mViewModel.getShowSubDialogFlag().set(true);
            getChildFragmentManager().beginTransaction().replace(R.id.view_sub_dialog, mTypeFragment, "type").commit();
        } else {
            boolean flag = !mViewModel.getShowSubDialogFlag().get();
            mViewModel.getShowSubDialogFlag().set(flag);
            getChildFragmentManager().beginTransaction().remove(mTypeFragment).commit();
        }
    }

    /**
     * 显示影片分级选择
     *
     * @param v
     */
    public void showAccessPermission(View v) {
        if (getChildFragmentManager().findFragmentByTag("access") == null) {
            mViewModel.getShowSubDialogFlag().set(true);
            getChildFragmentManager().beginTransaction().replace(R.id.view_sub_dialog, mAccessFragment, "access").commit();
        } else {
            boolean flag = !mViewModel.getShowSubDialogFlag().get();
            mViewModel.getShowSubDialogFlag().set(flag);
            getChildFragmentManager().beginTransaction().remove(mAccessFragment).commit();
        }
    }

    /**
     * 显示自定义索引名称输入框
     *
     * @param v
     */
    public void showEditName(View v) {
        mViewModel.getShowSubDialogFlag().set(false);
        if (getChildFragmentManager().findFragmentByTag("access") != null) {
            getChildFragmentManager().beginTransaction().remove(mAccessFragment).commit();
        }
        if (getChildFragmentManager().findFragmentByTag("type") != null) {
            getChildFragmentManager().beginTransaction().remove(mAccessFragment).commit();
        }
        ShortcutNameEditDialog.newInstance(this).show(getChildFragmentManager(), ShortcutNameEditDialog.class.getSimpleName());
        hide();
    }

    public void confirmScan(View v) {
        if(confirmFlag==false) {
            synchronized (this) {
                confirmFlag=true;
                mViewModel.saveShortcut();
                dismiss();
            }
        }
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

}
