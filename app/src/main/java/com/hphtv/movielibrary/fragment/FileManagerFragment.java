package com.hphtv.movielibrary.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.activity.HomePageActivity;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.databinding.FLayoutSettingBinding;
import com.hphtv.movielibrary.fragment.dialog.FolderManagerFragment;
import com.hphtv.movielibrary.fragment.dialog.PasswordDialogFragment;
import com.hphtv.movielibrary.viewmodel.HomepageViewModel;
import com.hphtv.movielibrary.viewmodel.fragment.FileManagerFragmentViewModel;

/**
 * Created by tchip on 17-12-4.
 */

public class FileManagerFragment extends BaseFragment<FileManagerFragmentViewModel, FLayoutSettingBinding> {
    public static final String TAG = FileManagerFragment.class.getSimpleName();
    public static final String REQUEST_FOLDER_DIALOG = "request_folder_dialog";


    public static FileManagerFragment newInstance(int pos) {
        Bundle args = new Bundle();
        args.putInt(Constants.IntentKey.KEY_CUR_FRAGMENT, pos);
        FileManagerFragment fragment = new FileManagerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void onViewCreated() {
        mBinding.viewFolderManager.setOnClickListener(v -> {
            FolderManagerFragment fragment = FolderManagerFragment.newInstance();
            getChildFragmentManager().setFragmentResultListener(REQUEST_FOLDER_DIALOG, this, (requestKey, result) -> {
                boolean needRefresh = result.getBoolean("refresh");
                if (needRefresh) {
                    Intent intent = new Intent();
                    intent.setAction(Constants.BroadCastMsg.RESCAN_DEVICE);
                    LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
                    ((HomepageViewModel) getAppBaseActivity().getViewModel()).getCurrentFragmentPos().postValue(HomePageActivity.HOME_PAGE_FRAGMENT);
                }
            });
            fragment.show(getChildFragmentManager(), "");
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

}
