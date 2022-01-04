package com.hphtv.movielibrary.ui.shortcutmanager.options;

import android.app.Application;
import android.view.View;

import androidx.annotation.NonNull;

import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.roomdb.entity.Shortcut;
import com.hphtv.movielibrary.ui.BaseAndroidViewModel;
import com.hphtv.movielibrary.ui.shortcutmanager.bean.ShortcutOptionsItem;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * author: Sam Leung
 * date:  2021/12/30
 */
public class ShortcutOptionsViewModel extends BaseAndroidViewModel {
    private ShortcutOptionsItem mNameItem, mAccessItem, mTypeItem;
    private Shortcut mShortcut;

    public ShortcutOptionsViewModel(@NonNull @NotNull Application application) {
        super(application);
        new Thread(() -> loadData()).start();
    }

    private void loadData() {
        List<String> shortcutTypesList = new ArrayList<>();
        shortcutTypesList.add(getString(R.string.shortcut_type_auto));
        shortcutTypesList.add(getString(R.string.shortcut_type_movie));
        shortcutTypesList.add(getString(R.string.shortcut_type_tv));
        mTypeItem = new ShortcutOptionsItem(getString(R.string.shortcut_scan_dialog_item_shortcut_type), shortcutTypesList);
        List<String> accessList = new ArrayList<>();
        accessList.add(getString(R.string.shortcut_access_all_ages));
        accessList.add(getString(R.string.shortcut_access_adult_only));
        mAccessItem = new ShortcutOptionsItem(getString(R.string.shortcut_scan_dialog_item_shortcut_access), accessList);
        List<String> nameList = new ArrayList<>();
        nameList.add(getString(R.string.shortcut_name_default));
        mNameItem = new ShortcutOptionsItem(getString(R.string.shortcut_scan_dialog_item_shortcut_name), nameList);
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

    public  void showShortcutType(View v){

    }

    public  void showAccessPermission(View v){

    }

    public  void showEditName(View v){

    }
}
