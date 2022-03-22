package com.hphtv.movielibrary.ui.shortcutmanager;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import androidx.activity.result.ActivityResult;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.hphtv.movielibrary.adapter.FolderItemAdapter;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.databinding.FLayoutFolderBinding;
import com.hphtv.movielibrary.service.MovieScanService;
import com.hphtv.movielibrary.ui.AppBaseActivity;
import com.hphtv.movielibrary.roomdb.entity.Shortcut;
import com.hphtv.movielibrary.ui.settings.PasswordDialogFragment;
import com.hphtv.movielibrary.ui.shortcutmanager.options.ShortcutOptionsDialog;
import com.hphtv.movielibrary.ui.shortcutmanager.options.ShortcutOptionsViewModel;
import com.hphtv.movielibrary.ui.shortcutmanager.options.scan.ShortcutScanDialog;

import java.util.HashSet;
import java.util.List;

/**
 * 设备管理页
 * author: Sam Leung
 * date:  2021/12/10
 */
public class ShortcutManagerActivity extends AppBaseActivity<ShortcutManagerViewModel, FLayoutFolderBinding> {
    public final static int MSG_NOTIFY_UPDATE = 1;
    private FolderItemAdapter mFolderItemAdapter;
    private ShortcutManagerEventHandler mShortcutManagerEventHandler;
    private ShortcutOptionsViewModel mOptionsViewModel;
    private final Handler mUIHanlder = new Handler(Looper.getMainLooper()) {
        @Override
        public void dispatchMessage(@NonNull Message msg) {
            super.dispatchMessage(msg);
            switch (msg.what) {
                case MSG_NOTIFY_UPDATE:
                    loadShortcuts();
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + msg.what);
            }
        }
    };

    private FolderItemAdapter.OnClickListener mFolderItemClickListener = item -> {
        Shortcut shortcut = item.item;
        mOptionsViewModel.setShortcut(shortcut);
        ShortcutOptionsDialog dialog = ShortcutOptionsDialog.newInstance();
        dialog.show(getSupportFragmentManager(), "");
    };

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mOptionsViewModel = new ViewModelProvider(this).get(ShortcutOptionsViewModel.class);
        mOptionsViewModel.setShortcutManagerViewModel(mViewModel);
        mOptionsViewModel.setUICallback(mCallback);

        mShortcutManagerEventHandler = new ShortcutManagerEventHandler(this);
        //返回按钮
        mBinding.btnExit.setOnClickListener(v -> {
            finish();
        });
        //文件选择器
        mBinding.setPosterHandler(mShortcutManagerEventHandler);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        LinearLayoutManager hiddenLinearLayoutManager = new LinearLayoutManager(this);
        hiddenLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mBinding.rvAddedFolders.setLayoutManager(linearLayoutManager);
        //索引列表适配器
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
            setResult(RESULT_OK);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceivers();
        bindService();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceivers();
        unbindService();
    }

    private void registerReceivers() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.BroadCastMsg.SHORTCUT_SCRAP_START);
        intentFilter.addAction(Constants.BroadCastMsg.SHORTCUT_SCRAP_STOP);
        intentFilter.addAction(Constants.BroadCastMsg.MATCHED_MOVIE);
        intentFilter.addAction(Constants.BroadCastMsg.MATCHED_MOVIE_FAILED);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, intentFilter);
    }

    private void unregisterReceivers() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
    }

    private void bindService(){
        Intent service=new Intent(getBaseContext(), MovieScanService.class);
        bindService(service,mServiceConnection,BIND_AUTO_CREATE);
    }

    private void unbindService(){
        unbindService(mServiceConnection);
    }

    public void loadShortcuts() {
        mViewModel.loadShortcuts(mCallback);
    }

    private ShortcutManagerViewModel.Callback mCallback = new ShortcutManagerViewModel.Callback() {

        @Override
        public void refreshShortcutList(List<Shortcut> shortcutList) {
            mFolderItemAdapter.addAllShortcuts(shortcutList);
            setResult(RESULT_OK);
        }

        @Override
        public void addShortcut(Shortcut shortcut) {
            mFolderItemAdapter.add(shortcut);
            mOptionsViewModel.setShortcut(shortcut);
            ShortcutScanDialog dialog = ShortcutScanDialog.newInstance();
            dialog.show(getSupportFragmentManager(), "");
        }
    };

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Shortcut shortcut;
            switch (action) {
                case Constants.BroadCastMsg.SHORTCUT_SCRAP_START:
                case Constants.BroadCastMsg.SHORTCUT_SCRAP_STOP:
                case Constants.BroadCastMsg.MATCHED_MOVIE:
                case Constants.BroadCastMsg.MATCHED_MOVIE_FAILED:
                    shortcut = (Shortcut) intent.getSerializableExtra(Constants.Extras.SHORTCUT);
                    mFolderItemAdapter.updateShortcut(shortcut);
                    break;
            }
        }
    };

    ServiceConnection mServiceConnection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mViewModel.loadShortcuts(new ShortcutManagerViewModel.Callback() {
                @Override
                public void refreshShortcutList(List<Shortcut> shortcutList) {
                    setResult(RESULT_OK);
                    mFolderItemAdapter.addAllShortcuts(shortcutList);
                    HashSet<Shortcut> shortcutHashSet=((MovieScanService.ScanBinder)service).getService().getShortcutHashSet();
                    for(Shortcut shortcut:shortcutHashSet){
                        mFolderItemAdapter.updateShortcut(shortcut);
                    }
                }

                @Override
                public void addShortcut(Shortcut shortcut) {
                    mFolderItemAdapter.add(shortcut);
                    mOptionsViewModel.setShortcut(shortcut);
                    ShortcutScanDialog dialog = ShortcutScanDialog.newInstance();
                    dialog.show(getSupportFragmentManager(), "");
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
}
