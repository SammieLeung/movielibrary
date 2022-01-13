package com.hphtv.movielibrary.ui.shortcutmanager;

import android.app.Activity;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.activity.result.ActivityResult;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.hphtv.movielibrary.adapter.FolderItemAdapter;
import com.hphtv.movielibrary.databinding.FLayoutFolderBinding;
import com.hphtv.movielibrary.ui.AppBaseActivity;
import com.hphtv.movielibrary.roomdb.entity.Shortcut;
import com.hphtv.movielibrary.ui.settings.PasswordDialogFragment;
import com.hphtv.movielibrary.ui.shortcutmanager.options.ShortcutOptionsDialog;
import com.hphtv.movielibrary.ui.shortcutmanager.options.ShortcutOptionsViewModel;
import com.hphtv.movielibrary.ui.shortcutmanager.options.scan.ShortcutScanDialog;

import java.util.List;

/**
 * 设备管理页
 * author: Sam Leung
 * date:  2021/12/10
 */
public class ShortcutManagerActivity extends AppBaseActivity<ShortcutManagerViewModel, FLayoutFolderBinding> {
    public final static int MSG_NOTIFY_UPDATE=1;
    private FolderItemAdapter mFolderItemAdapter;
    private ShortcutManagerEventHandler mShortcutManagerEventHandler;
    private ShortcutOptionsViewModel mOptionsViewModel;
    private final Handler mUIHanlder = new Handler(Looper.getMainLooper()) {
        @Override
        public void dispatchMessage(@NonNull Message msg) {
            super.dispatchMessage(msg);
            switch (msg.what) {
                case MSG_NOTIFY_UPDATE:
                    notifyUpdate();
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + msg.what);
            }
        }
    };

    private FolderItemAdapter.OnClickListener mFolderItemClickListener = item -> {
        Shortcut shortcut = item.item;
        mOptionsViewModel.setShortcut(shortcut);
        ShortcutOptionsDialog dialog=ShortcutOptionsDialog.newInstance();
        dialog.show(getSupportFragmentManager(),"");
    };

    @Override
    protected void onCreate() {
        mOptionsViewModel=new ViewModelProvider(this).get(ShortcutOptionsViewModel.class);
        mOptionsViewModel.setShortcutManagerViewModel(mViewModel);
        mOptionsViewModel.setUICallback(mCallback);
        mShortcutManagerEventHandler=new ShortcutManagerEventHandler(this);
        mBinding.btnExit.setOnClickListener(v -> {
            finish();
        });
        mBinding.setPosterHandler(mShortcutManagerEventHandler);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        LinearLayoutManager hiddenLinearLayoutManager = new LinearLayoutManager(this);
        hiddenLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mBinding.rvAddedFolders.setLayoutManager(linearLayoutManager);
        mFolderItemAdapter = new FolderItemAdapter(this);
        mFolderItemAdapter.setOnClickListener(mFolderItemClickListener);
        mBinding.rvAddedFolders.setAdapter(mFolderItemAdapter);
    }

    @Override
    protected void onActivityResultCallback(ActivityResult result) {
        super.onActivityResultCallback(result);
        mShortcutManagerEventHandler.pickerClose();
        if (result.getResultCode() == RESULT_OK) {

            final Uri uri = result.getData().getData();
            mViewModel.addShortcut(uri, mCallback);
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
        mViewModel.loadShortcuts(mCallback);
    }

    private ShortcutManagerViewModel.Callback mCallback = new ShortcutManagerViewModel.Callback() {

        @Override
        public void refreshShortcutList(List<Shortcut> shortcutList) {
            mFolderItemAdapter.addAllShortcuts(shortcutList);
        }

        @Override
        public void addShortcut(Shortcut shortcut) {
            mFolderItemAdapter.add(shortcut);
            mOptionsViewModel.setShortcut(shortcut);
            ShortcutScanDialog dialog= ShortcutScanDialog.newInstance();
            dialog.show(getSupportFragmentManager(),"");
        }
    };
}
