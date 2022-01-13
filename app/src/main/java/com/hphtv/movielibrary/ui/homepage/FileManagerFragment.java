package com.hphtv.movielibrary.ui.homepage;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.ui.BaseFragment;
import com.hphtv.movielibrary.ui.shortcutmanager.ShortcutManagerActivity;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.databinding.FLayoutSettingBinding;
import com.hphtv.movielibrary.ui.settings.PasswordDialogFragment;

/**
 * Created by tchip on 17-12-4.
 */

public class FileManagerFragment extends BaseFragment<FileManagerFragmentViewModel, FLayoutSettingBinding> {
    public static final String TAG = FileManagerFragment.class.getSimpleName();
    public static final String REQUEST_FOLDER_DIALOG = "request_folder_dialog";


    public static FileManagerFragment newInstance(int pos) {
        Bundle args = new Bundle();
        args.putInt(Constants.Extras.CURRENT_FRAGMENT, pos);
        FileManagerFragment fragment = new FileManagerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void onViewCreated() {
        mBinding.viewFolderManager.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ShortcutManagerActivity.class);
            startActivityForResult(intent);
        });
        mBinding.viewChangePassword.setOnClickListener(v -> {
            PasswordDialogFragment passwordDialogFragment = PasswordDialogFragment.newInstance().newChangePassword();
            passwordDialogFragment.show(getChildFragmentManager(), "");
        });
        mBinding.tvVersion.setText(mViewModel.getVersion());
    }

    @Override
    protected int getLayoutId() {
        return R.layout.f_layout_setting;
    }

    @Override
    protected void onActivityResultCallback(ActivityResult result) {
        super.onActivityResultCallback(result);
        if (result.getResultCode() == Activity.RESULT_OK) {
            Intent intent = new Intent();
            intent.setAction(Constants.BroadCastMsg.RESCAN_ALL);
            LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
            ((HomepageViewModel) getAppBaseActivity().getViewModel()).getCurrentFragmentPos().postValue(HomePageActivity.HOME_PAGE_FRAGMENT);
        }
    }
}
