package com.hphtv.movielibrary.ui.shortcutmanager.options;

import android.app.Application;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.ShortcutDao;
import com.hphtv.movielibrary.roomdb.entity.Shortcut;
import com.hphtv.movielibrary.ui.BaseAndroidViewModel;
import com.hphtv.movielibrary.ui.shortcutmanager.ShortcutManagerViewModel;
import com.hphtv.movielibrary.ui.shortcutmanager.bean.ShortcutOptionsItem;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * author: Sam Leung
 * date:  2021/12/30
 */
public class ShortcutOptionsViewModel extends BaseAndroidViewModel {
    private ShortcutDao mShortcutDao;
    private ShortcutOptionsItem mNameItem, mAccessItem, mTypeItem;
    private Shortcut mShortcut;
    private List<String> mFolderTypeList = new ArrayList<>(), mAccessList = new ArrayList<>();
    private ObservableBoolean mShowSubDialogFlag = new ObservableBoolean();
    private WeakReference<ShortcutManagerViewModel> mShortcutManagerViewModelWeakReference;
    private ShortcutManagerViewModel.Callback mCallback;

    public ShortcutOptionsViewModel(@NonNull @NotNull Application application) {
        super(application);
        mShortcutDao = MovieLibraryRoomDatabase.getDatabase(application).getShortcutDao();
        loadData();
    }


    private void loadData() {
        mFolderTypeList.add(getString(R.string.shortcut_type_auto));
        mFolderTypeList.add(getString(R.string.shortcut_type_movie));
        mFolderTypeList.add(getString(R.string.shortcut_type_tv));
        mTypeItem = new ShortcutOptionsItem(getString(R.string.shortcut_scan_dialog_item_shortcut_type), mFolderTypeList);
        mAccessList.add(getString(R.string.shortcut_access_all_ages));
        mAccessList.add(getString(R.string.shortcut_access_adult_only));
        mAccessItem = new ShortcutOptionsItem(getString(R.string.shortcut_scan_dialog_item_shortcut_access), mAccessList);
        mNameItem = new ShortcutOptionsItem(getString(R.string.shortcut_scan_dialog_item_shortcut_name), getString(R.string.shortcut_name_default));
    }

    public Shortcut getShortcut() {
        return mShortcut;
    }

    public void setShortcut(Shortcut shortcut) {
        mShortcut = shortcut;
    }

    public ShortcutOptionsItem getNameItem() {
        return mNameItem;
    }

    public void setNameItem(ShortcutOptionsItem nameItem) {
        mNameItem = nameItem;
    }

    public ShortcutOptionsItem getAccessItem() {
        return mAccessItem;
    }

    public void setAccessItem(ShortcutOptionsItem accessItem) {
        mAccessItem = accessItem;
    }

    public ShortcutOptionsItem getTypeItem() {
        return mTypeItem;
    }

    public void setTypeItem(ShortcutOptionsItem typeItem) {
        mTypeItem = typeItem;
    }

    public ObservableBoolean getShowSubDialogFlag() {
        return mShowSubDialogFlag;
    }

    /**
     * 读取shortcut信息
     */
    public void loadShortcutData() {
        mNameItem.setSubTitle(mShortcut.firendlyName);

        switch (mShortcut.access) {
            case ALL_AGE:
                mAccessItem.setPos(0);
                break;
            case ADULT:
                mAccessItem.setPos(1);
                break;
        }
        switch (mShortcut.folderType) {
            case movie:
                mTypeItem.setPos(1);
                break;
            case tv:
                mTypeItem.setPos(2);
                break;
            default:
                mTypeItem.setPos(0);
                break;
        }
    }


    /**
     * 保存索引修改
     */
    public void saveShortcut() {
        Observable.create((ObservableOnSubscribe<Shortcut>) emitter -> {
            mShortcut.firendlyName = mNameItem.getSubTitle();
            switch (mTypeItem.getPos()) {
                case 0:
                    mShortcut.folderType = Constants.SearchType.auto;
                    break;
                case 1:
                    mShortcut.folderType = Constants.SearchType.movie;
                    break;
                case 2:
                    mShortcut.folderType = Constants.SearchType.tv;
                    break;
            }
            switch (mAccessItem.getPos()) {
                case 0:
                    mShortcut.access = Constants.AccessPermission.ALL_AGE;
                    break;
                case 1:
                    mShortcut.access = Constants.AccessPermission.ADULT;
                    break;
            }
            emitter.onNext(mShortcut);
            emitter.onComplete();
        })
                .subscribeOn(Schedulers.newThread())
                .doOnNext(shortcut -> {
                    mShortcutDao.updateShortcut(mShortcut);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<Shortcut>() {
                    @Override
                    public void onAction(Shortcut shortcut) {
                        if(shortcut.devcieType>Constants.DeviceType.DEVICE_TYPE_HARD_DISK) {
                            Intent intent = new Intent();
                            intent.setAction(Constants.BroadCastMsg.POSTER_PAIRING_FOR_NETWORK_URI);
                            intent.putExtra(Constants.Extras.QUERY_SHORTCUT,mShortcut);
                            LocalBroadcastManager.getInstance(getApplication()).sendBroadcast(intent);
                        }else{
                            Intent intent=new Intent();
                            intent.setAction(Constants.BroadCastMsg.POSTER_PAIRING);
                            intent.putExtra(Constants.Extras.QUERY_SHORTCUT,mShortcut);
                            LocalBroadcastManager.getInstance(getApplication()).sendBroadcast(intent);
                        }
                        if(mShortcutManagerViewModelWeakReference.get()!=null){
                            mShortcutManagerViewModelWeakReference.get().loadShortcuts(mCallback);
                        }
                    }
                });

    }

    public void removeShortcut(Shortcut shortcut){
        if(mShortcutManagerViewModelWeakReference.get()!=null){
            mShortcutManagerViewModelWeakReference.get().removeShortcut(shortcut,mCallback);
        }
    }

    public void setShortcutManagerViewModel(ShortcutManagerViewModel viewModel) {
        mShortcutManagerViewModelWeakReference = new WeakReference<>(viewModel);
    }

    public void setUICallback( ShortcutManagerViewModel.Callback callback){
        mCallback=callback;
    }
}