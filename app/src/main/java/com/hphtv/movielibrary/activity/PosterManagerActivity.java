package com.hphtv.movielibrary.activity;

import android.app.Activity;
import android.net.Uri;

import androidx.activity.result.ActivityResult;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.hphtv.movielibrary.adapter.FolderItemAdapter;
import com.hphtv.movielibrary.databinding.FLayoutFolderBinding;
import com.hphtv.movielibrary.fragment.dialog.PasswordDialogFragment;
import com.hphtv.movielibrary.listener.PosterManagerEventHandler;
import com.hphtv.movielibrary.roomdb.entity.ScanDirectory;
import com.hphtv.movielibrary.roomdb.entity.Shortcut;
import com.hphtv.movielibrary.viewmodel.fragment.FolderManagerFragmentViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * author: Sam Leung
 * date:  2021/12/10
 */
public class PosterManagerActivity extends AppBaseActivity<FolderManagerFragmentViewModel, FLayoutFolderBinding> {
    private FolderItemAdapter mFolderItemAdapter;
    private List<ScanDirectory> mHiddenScanDirectoryList = new ArrayList<>();

//    private FolderItemAdapter.OnClickListener mFolderItemClickListener = new FolderItemAdapter.OnClickListener() {
//        @Override
//        public void onClick(ScanDirectory scanDirectory) {
//
//        }
//
//        @Override
//        public void delete(String path) {
//            mViewModel.deleteDirecotryByPath(path, mCallback);
//           setResult(Activity.RESULT_OK);
//        }
//
//        @Override
//        public void move(String path) {
//            mViewModel.moveToHidden(path, mCallback);
//            setResult(Activity.RESULT_OK);
//
//        }
//
//        @Override
//        public void rescan(String path) {
//
//        }
//
//        @Override
//        public void addClick() {
////            if (!isPickerOpening) {
////                synchronized (this) {
////                    if (!isPickerOpening) {
////                        isPickerOpening = true;
////                        Intent picker_intent = new Intent(Constants.ACTION_FILE_PICKER);
////                        startActivityForResult(picker_intent);
////                    }
////                }
////            }
//        }
//    };


    @Override
    protected void onCreate() {

        mBinding.btnExit.setOnClickListener(v -> {
            finish();
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        LinearLayoutManager hiddenLinearLayoutManager = new LinearLayoutManager(this);
        hiddenLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mBinding.rvAddedFolders.setLayoutManager(linearLayoutManager);
        mFolderItemAdapter = new FolderItemAdapter(this);
//        mFolderItemAdapter.setOnClickListener(mFolderItemClickListener);
        mBinding.rvAddedFolders.setAdapter(mFolderItemAdapter);
    }

    @Override
    protected void onActivityResultCallback(ActivityResult result) {
        super.onActivityResultCallback(result);
        PosterManagerEventHandler.isPickerOpening = false;

        if (result.getResultCode() == RESULT_OK) {
            final Uri uri = result.getData().getData();
            mViewModel.addScanDirectoryByUri(uri, mCallback);
            setResult(Activity.RESULT_OK);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        notifyUpdate();
    }

    private void showEnterPasswordDialog() {
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
        passwordDialogFragment.show(getSupportFragmentManager(), "");
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
