package com.hphtv.movielibrary.ui.shortcutmanager;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import androidx.activity.result.ActivityResult;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.hphtv.movielibrary.adapter.FolderItemAdapter;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.databinding.FLayoutFolderBinding;
import com.hphtv.movielibrary.ui.AppBaseActivity;
import com.hphtv.movielibrary.roomdb.entity.Shortcut;
import com.hphtv.movielibrary.ui.settings.PasswordDialogFragment;

import java.util.List;

/**
 * author: Sam Leung
 * date:  2021/12/10
 */
public class ShortcutManagerActivity extends AppBaseActivity<ShortcutManagerViewModel, FLayoutFolderBinding> {
    private FolderItemAdapter mFolderItemAdapter;

    private FolderItemAdapter.OnClickListener mFolderItemClickListener = item -> {
        Shortcut shortcut = item.item;
        ShortcutOptionsDialog dialog=ShortcutOptionsDialog.newInstance();
        dialog.show(getSupportFragmentManager(),"");
//        if (shortcut.devcieType > 5) {
//            Intent intent = new Intent();
//            intent.setAction(Constants.BroadCastMsg.POSTER_PAIRING_FOR_NETWORK_URI);
//            intent.putExtra(Constants.Extras.QUERY_URI, shortcut.queryUri);
//            intent.putExtra(Constants.Extras.NETWORK_DIR_PATH,shortcut.uri);
//            LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(intent);
//        }
    };

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
        mFolderItemAdapter.setOnClickListener(mFolderItemClickListener);
        mBinding.rvAddedFolders.setAdapter(mFolderItemAdapter);
    }

    @Override
    protected void onActivityResultCallback(ActivityResult result) {
        super.onActivityResultCallback(result);
        mFolderItemAdapter.pickerClose();
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
        }
    };
}
