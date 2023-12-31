package com.hphtv.movielibrary.ui.shortcutmanager;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.activity.result.ActivityResult;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.adapter.FolderItemAdapter;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.databinding.FLayoutFolderBinding;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.ShortcutDao;
import com.hphtv.movielibrary.roomdb.entity.Shortcut;
import com.hphtv.movielibrary.scraper.service.OnlineDBApiService;
import com.hphtv.movielibrary.service.MovieScanService;
import com.hphtv.movielibrary.ui.AppBaseActivity;
import com.hphtv.movielibrary.ui.shortcutmanager.options.ShortcutOptionsDialog;
import com.hphtv.movielibrary.ui.shortcutmanager.options.ShortcutOptionsViewModel;
import com.hphtv.movielibrary.ui.shortcutmanager.options.scan.ShortcutScanDialog;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;
import com.station.kit.util.LogUtil;
import com.station.kit.util.ToastUtil;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;

/**
 * 设备管理页
 * author: Sam Leung
 * date:  2021/12/10
 */
public class ShortcutManagerActivity extends AppBaseActivity<ShortcutManagerViewModel, FLayoutFolderBinding> {
    private FolderItemAdapter mFolderItemAdapter;
    private ShortcutManagerEventHandler mShortcutManagerEventHandler;
    private ShortcutOptionsViewModel mOptionsViewModel;


    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //创建ShortcutOptionsViewModel
        createShortcutOptionsViewModel();
        mShortcutManagerEventHandler = new ShortcutManagerEventHandler(this);
        mBinding.setPosterHandler(mShortcutManagerEventHandler);
        //返回按钮
        mBinding.btnExit.setOnClickListener(v -> finish());
        //文件选择器初始化
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mBinding.rvAddedFolders.setLayoutManager(linearLayoutManager);
        //索引列表适配器
        mFolderItemAdapter = new FolderItemAdapter(this);
        mFolderItemAdapter.setOnClickListener(mFolderItemClickListener);
        mBinding.rvAddedFolders.setAdapter(mFolderItemAdapter);
    }

    private void createShortcutOptionsViewModel() {
        mOptionsViewModel = new ViewModelProvider(this).get(ShortcutOptionsViewModel.class);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mBinding.setIsEmtpy(true);
        bindService();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService();
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceivers();

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceivers();
    }


    @Override
    protected void onActivityResultCallback(ActivityResult result) {
        super.onActivityResultCallback(result);
        if (result.getResultCode() == RESULT_OK) {
            final Uri uri = result.getData().getData();
            mViewModel.addShortcut(uri)
                    .subscribe(new SimpleObserver<Shortcut>() {

                        @Override
                        public void onAction(Shortcut shortcut) {
                            addShortcut(shortcut);
                            mBinding.setIsEmtpy(false);
                        }

                        @Override
                        public void onError(Throwable e) {
                            super.onError(e);
                            if(e instanceof ShortcutManagerViewModel.DeviceNotFoundException) {
                                ToastUtil.newInstance(getBaseContext()).toast(getString(R.string.errtips_device_not_found));
                                Intent intent = new Intent();
                                intent.setAction(Constants.ACTION.DEVICE_RE_INIT);
                                LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(intent);
                            }
                        }
                    });
            setResult(RESULT_OK);
        }
        mShortcutManagerEventHandler.pickerClose();
    }

    private void registerReceivers() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.ACTION.SHORTCUT_INFO_UPDATE);
        intentFilter.addAction(Constants.ACTION.SHORTCUT_REMOVE);
        intentFilter.addAction(Constants.ACTION.SHORTCUT_SCRAP_START);
        intentFilter.addAction(Constants.ACTION.SHORTCUT_SCRAP_STOP);
        intentFilter.addAction(Constants.ACTION.MATCHED_MOVIE);
        intentFilter.addAction(Constants.ACTION.MATCHED_MOVIE_FAILED);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, intentFilter);
    }

    private void unregisterReceivers() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
    }

    private void bindService() {
        Intent service = new Intent(getBaseContext(), MovieScanService.class);
        bindService(service, mServiceConnection, BIND_AUTO_CREATE);
    }

    private void unbindService() {
        unbindService(mServiceConnection);
    }

    private void addShortcut(Shortcut shortcut) {
        startLoading();
        LogUtil.w("addShortcut "+shortcut.name);
        mFolderItemAdapter.add(shortcut);
        new Thread(() -> {
            ShortcutDao shortcutDao = MovieLibraryRoomDatabase.getDatabase(getBaseContext()).getShortcutDao();
            List<Shortcut> shortcutList = shortcutDao.queryAllConnectShortcuts();
            OnlineDBApiService.notifyShortcuts(shortcutList, Constants.Scraper.TMDB);
            OnlineDBApiService.notifyShortcuts(shortcutList, Constants.Scraper.TMDB_EN);
        }).start();
        mOptionsViewModel.setShortcut(shortcut);
        Observable.timer(300, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<Long>() {
                    @Override
                    public void onAction(Long aLong) {
                        stopLoading();
                        ShortcutScanDialog dialog = ShortcutScanDialog.newInstance(true);
                        dialog.show(getSupportFragmentManager(), "");
                        mBinding.rvAddedFolders.smoothScrollToPosition(0);
                    }
                });
    }

    FolderItemAdapter.OnClickListener mFolderItemClickListener = item -> {
        Shortcut shortcut = item.item;
        mOptionsViewModel.setShortcut(shortcut);
        ShortcutOptionsDialog dialog = ShortcutOptionsDialog.newInstance();
        dialog.show(getSupportFragmentManager(), "");
    };

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Shortcut shortcut;
            switch (action) {
                case Constants.ACTION.SHORTCUT_SCRAP_START:
                case Constants.ACTION.SHORTCUT_SCRAP_STOP:
                case Constants.ACTION.MATCHED_MOVIE:
                case Constants.ACTION.MATCHED_MOVIE_FAILED:
                    shortcut = (Shortcut) intent.getSerializableExtra(Constants.Extras.SHORTCUT);
                    mFolderItemAdapter.updateShortcut(shortcut);
                    break;
                case Constants.ACTION.SHORTCUT_INFO_UPDATE:
                    shortcut = (Shortcut) intent.getSerializableExtra(Constants.Extras.SHORTCUT);
                    mFolderItemAdapter.updateShortcut(shortcut);
                    ShortcutManagerActivity.this.setResult(RESULT_OK);
                    break;
                case Constants.ACTION.SHORTCUT_REMOVE:
                    shortcut = (Shortcut) intent.getSerializableExtra(Constants.Extras.SHORTCUT);
                    mViewModel.removeShortcut(shortcut)
                            .subscribe(new SimpleObserver<Shortcut>() {
                                @Override
                                public void onAction(Shortcut shortcut) {
                                    mFolderItemAdapter.removeShortcut(shortcut);
                                    if(mFolderItemAdapter.getItemCount()==0)
                                        mBinding.setIsEmtpy(true);
                                    ShortcutManagerActivity.this.setResult(RESULT_OK);
                                }
                            });
                    break;
            }
        }
    };

    ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mViewModel.loadShortcuts()
                    .subscribe(new SimpleObserver<List<Shortcut>>() {
                        @Override
                        public void onAction(List<Shortcut> shortcuts) {
                            setResult(RESULT_OK);
                            if(shortcuts.size()>0)
                                mBinding.setIsEmtpy(false);
                            mFolderItemAdapter.addAllShortcuts(shortcuts);
                            HashSet<Shortcut> shortcutHashSet = ((MovieScanService.ScanBinder) service).getService().getShortcutHashSet();
                            for (Shortcut shortcut : shortcutHashSet) {
                                mFolderItemAdapter.updateShortcut(shortcut);
                            }
                        }
                    });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
}
