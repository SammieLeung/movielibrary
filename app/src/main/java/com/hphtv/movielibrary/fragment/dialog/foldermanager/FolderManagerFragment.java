package com.hphtv.movielibrary.fragment.dialog.foldermanager;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.adapter.FolderItemAdapter;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.databinding.FLayoutFolderBinding;
import com.hphtv.movielibrary.fragment.dialog.BaseDialogFragment;
import com.hphtv.movielibrary.fragment.dialog.PasswordDialogFragment;
import com.hphtv.movielibrary.roomdb.entity.ScanDirectory;
import com.hphtv.movielibrary.roomdb.entity.Shortcut;
import com.hphtv.movielibrary.viewmodel.fragment.FolderManagerFragmentViewModel;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.hphtv.movielibrary.fragment.FileManagerFragment.REQUEST_FOLDER_DIALOG;

/**
 * author: Sam Leung
 * date:  2021/8/24
 */
public class FolderManagerFragment extends BaseDialogFragment<FolderManagerFragmentViewModel, FLayoutFolderBinding> {
    private FolderItemAdapter mFolderItemAdapter;
//    private FolderItemAdapter mHiddenFolderItemApdapter;
    private List<ScanDirectory> mHiddenScanDirectoryList = new ArrayList<>();
    private boolean isPickerOpening = false;
    private boolean isNeedRefresh=false;

    private FolderItemAdapter.OnClickListener mFolderItemClickListener =new FolderItemAdapter.OnClickListener() {
        @Override
        public void onClick(ScanDirectory scanDirectory) {

        }

        @Override
        public void delete(String path) {
            mViewModel.deleteDirecotryByPath(path, mCallback);
            isNeedRefresh=true;
        }

        @Override
        public void move(String path) {
            mViewModel.moveToHidden(path, mCallback);
            isNeedRefresh=true;
        }

        @Override
        public void rescan(String path) {

        }

        @Override
        public void addClick() {
            if (!isPickerOpening) {
                synchronized (this) {
                    if (!isPickerOpening) {
                        isPickerOpening = true;
                        Intent picker_intent = new Intent(Constants.ACTION_FILE_PICKER);
                        startActivityForResult(picker_intent);
                    }
                }
            }
        }
    };

    public static FolderManagerFragment newInstance() {

        Bundle args = new Bundle();
        FolderManagerFragment fragment = new FolderManagerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void onViewCreated() {
        mBinding.btnShowHidden.setOnClickListener(v -> {
            showEnterPasswordDialog();
        });

        mBinding.btnExit.setOnClickListener(v -> {
           dismiss();
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        LinearLayoutManager hiddenLinearLayoutManager = new LinearLayoutManager(getContext());
        hiddenLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mBinding.rvAddedFolders.setLayoutManager(linearLayoutManager);
        mBinding.rvHiddenFolder.setLayoutManager(hiddenLinearLayoutManager);
        mFolderItemAdapter = new FolderItemAdapter(getContext());
        mFolderItemAdapter.setOnClickListener(mFolderItemClickListener);
//        mHiddenFolderItemApdapter = new FolderItemAdapter(getContext(), mHiddenScanDirectoryList);
//        mHiddenFolderItemApdapter.setTypeFolder(FolderItemAdapter.TYPE_HIDDEN_FOLDER);
//        mHiddenFolderItemApdapter.setOnClickListener(new FolderItemAdapter.OnClickListener() {
//            @Override
//            public void addClick() {
//
//            }
//
//            @Override
//            public void onClick(ScanDirectory scanDirectory) {
//
//            }
//
//            @Override
//            public void delete(String path) {
//                mViewModel.deleteDirecotryByPath(path, mCallback);
//                isNeedRefresh=true;
//            }
//
//            @Override
//            public void move(String path) {
//                mViewModel.moveToPublic(path, mCallback);
//                isNeedRefresh=true;
//            }
//
//            @Override
//            public void rescan(String path) {
//
//            }
//        });
        mBinding.rvAddedFolders.setAdapter(mFolderItemAdapter);
//        mBinding.rvHiddenFolder.setAdapter(mHiddenFolderItemApdapter);
    }


    @Override
    protected int getLayoutId() {
        return R.layout.f_layout_folder;
    }

    @Override
    protected void onActivityResultCallback(ActivityResult result) {
        super.onActivityResultCallback(result);
        isPickerOpening = false;

        if (result.getResultCode() == RESULT_OK) {
            final Uri uri = result.getData().getData();
            mViewModel.addScanDirectoryByUri(uri, mCallback);
            isNeedRefresh=true;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        notifyUpdate();
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog=getDialog();
        if(dialog!=null){
            dialog.getWindow().setLayout(getResources().getDimensionPixelOffset(R.dimen.folder_dialog_contentbox_w),getResources().getDimensionPixelOffset(R.dimen.folder_dialog_contentbox_h));
        }
    }

    @Override
    public void onDismiss(@NonNull @NotNull DialogInterface dialog) {
        super.onDismiss(dialog);
        Bundle bundle=new Bundle();
        bundle.putBoolean("refresh",isNeedRefresh);
        getParentFragmentManager().setFragmentResult(REQUEST_FOLDER_DIALOG,bundle);
    }

    private void showEnterPasswordDialog(){
        PasswordDialogFragment passwordDialogFragment = PasswordDialogFragment.newInstance().newEnterPassword();
        passwordDialogFragment.setOnClickListener(new PasswordDialogFragment.OnClickListener() {
            @Override
            public void isVaildPassword() {
                mBinding.setShowHidden(true);
            }

            @Override
            public void updatePassword(String text) {
                showEnterPasswordDialog();
            }

            @Override
            public void onCancel() {

            }
        });
        passwordDialogFragment.show(getChildFragmentManager(), "");
    }

    public void notifyUpdate() {
        mViewModel.loadScanDirectory(mCallback);
        mViewModel.loadHiddenScanDirectory(mCallback);
    }

    private FolderManagerFragmentViewModel.Callback mCallback = new FolderManagerFragmentViewModel.Callback() {
        @Override
        public void refreshScanDirectoryList(List<ScanDirectory> scanDirectoryList) {
            mFolderItemAdapter.addAllScanDirecotry(scanDirectoryList);
        }

        @Override
        public void refreshHiddenScanDirectoryList(List<ScanDirectory> scanDirectoryList) {
//            mHiddenFolderItemApdapter.addAll(scanDirectoryList);
        }

        @Override
        public void refreshShortcutList(List<Shortcut> shortcutList) {
            mFolderItemAdapter.addAllShortcuts(shortcutList);
        }

        @Override
        public void addShortcut(Shortcut shortcut) {
            mFolderItemAdapter.add(shortcut);
        }

        @Override
        public void addScanDirectory(ScanDirectory scanDirectory) {
            mFolderItemAdapter.add(scanDirectory);

        }

    };
}
