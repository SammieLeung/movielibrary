package com.hphtv.movielibrary.ui.shortcutmanager;

import android.app.Application;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.DeviceDao;
import com.hphtv.movielibrary.roomdb.dao.ScanDirectoryDao;
import com.hphtv.movielibrary.roomdb.dao.ShortcutDao;
import com.hphtv.movielibrary.roomdb.entity.Device;
import com.hphtv.movielibrary.roomdb.entity.Shortcut;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;
import com.station.kit.util.Base64Helper;
import com.station.kit.util.LogUtil;
import com.station.kit.util.SharePreferencesTools;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * author: Sam Leung
 * date:  2021/8/24
 */
public class ShortcutManagerViewModel extends AndroidViewModel {
    public static final String TAG = ShortcutManagerViewModel.class.getSimpleName();
    private ScanDirectoryDao mScanDirectoryDao;
    private DeviceDao mDeviceDao;
    private ShortcutDao mShortcutDao;
    private boolean passwordhasBeenSet = false;

    public ShortcutManagerViewModel(@NonNull @NotNull Application application) {
        super(application);
        MovieLibraryRoomDatabase roomDatabase = MovieLibraryRoomDatabase.getDatabase(application);
        mScanDirectoryDao = roomDatabase.getScanDirectoryDao();
        mDeviceDao = roomDatabase.getDeviceDao();
        mShortcutDao = roomDatabase.getShortcutDao();
        String password = SharePreferencesTools.getInstance(application).readProperty(Constants.SharePreferenceKeys.PASSWORD, "");
        passwordhasBeenSet = TextUtils.isEmpty(password) ? false : true;
    }

    public void loadShortcuts(Callback callback) {
        Observable.just("")
                .subscribeOn(Schedulers.io())
                .map(s -> {
                    List<Shortcut> shortcutList = mShortcutDao.queryAllShortcuts();
                    return shortcutList;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<Shortcut>>() {
                    @Override
                    public void onAction(List<Shortcut> shortcutList) {
                        if (callback != null)
                            callback.refreshShortcutList(shortcutList);
                    }
                });
    }

//    public void loadHiddenScanDirectory(Callback callback) {
//        Observable.just("")
//                .subscribeOn(Schedulers.io())
//                .map(s -> {
//                    List<ScanDirectory> scanDirectoryList = mScanDirectoryDao.queryAllHiddenScanDirectories();
//                    return scanDirectoryList;
//                })
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(scanDirectoryList -> {
//                    if (callback != null)
//                        callback.refreshHiddenScanDirectoryList(scanDirectoryList);
//                });
//    }

    public void addShortcut(Uri uri, Callback callback) {
        Observable.just(uri)
                .subscribeOn(Schedulers.io())
                .map(queryUri -> {
                    String deviceTypeStr = uri.getPathSegments().get(0);//device Api str
                    String base64Id = queryUri.getPathSegments().get(1);//dir id
                    String path = Base64Helper.decode(base64Id);
                    switch (deviceTypeStr) {
                        case "local":
                            break;
                        case "dlna":
                            break;
                    }
                    if (deviceTypeStr.equals("local")) {
                        for (Device device : mDeviceDao.qureyAll()) {
                            if (path.contains(device.path)) {
                                LogUtil.v("id " + device.id + " localpath:" + device.path);
                                Shortcut shortcut = mShortcutDao.queryShortcutByUri(path);
                                if (shortcut == null) {
                                    shortcut = new Shortcut(path, device.type, null, null,null);
                                    shortcut.devicePath=device.path;
                                    mShortcutDao.insertShortcut(shortcut);
                                }
                                return shortcut;
                            }
                        }
                    } else if (deviceTypeStr.equals("dlna") || deviceTypeStr.equals("samba")) {

                        Shortcut shortcut = mShortcutDao.queryShortcutByUri(path);
                        if (shortcut == null) {
                            if (deviceTypeStr.equals("samba")) {
                                shortcut = new Shortcut(path, Constants.DeviceType.DEVICE_TYPE_SMB, null, null,queryUri.toString());
                                mShortcutDao.insertShortcut(shortcut);
                            } else {
                                shortcut = new Shortcut(path, Constants.DeviceType.DEVICE_TYPE_DLNA, null, null,queryUri.toString());
                                mShortcutDao.insertShortcut(shortcut);
                            }
                        }
                        return shortcut;
                    }
                    return new Shortcut("", 0, null, null,"");
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(shortcut -> {
                    if (callback != null)
                        callback.addShortcut(shortcut);
                });

    }

    public void removeShortcut(Shortcut shortcut, Callback callback) {
        Observable.just(shortcut)
                .subscribeOn(Schedulers.io())
                .doOnNext(_shortcut -> {
                    mShortcutDao.delete(_shortcut);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    loadShortcuts(callback);
                });
    }

//    public void moveToHidden(String path, Callback callback) {
//        Observable.just(path)
//                .subscribeOn(Schedulers.io())
//                .doOnNext(_path -> {
//                    mScanDirectoryDao.updateScanDirectoryHiddenState(_path, true);
//                })
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(s -> {
//                    loadShortcuts(callback);
//                    loadHiddenScanDirectory(callback);
//                });
//
//    }
//
//    public void moveToPublic(String path, Callback callback) {
//        Observable.just(path)
//                .subscribeOn(Schedulers.io())
//                .doOnNext(_path -> {
//                    mScanDirectoryDao.updateScanDirectoryHiddenState(_path, false);
//                })
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(s -> {
//                    loadShortcuts(callback);
//                    loadHiddenScanDirectory(callback);
//                });
//    }

//    public boolean isPasswordHasBeenSet() {
//        return passwordhasBeenSet;
//    }

    public interface Callback {
        void refreshShortcutList(List<Shortcut> shortcutList);

        void addShortcut(Shortcut shortcut);

    }
}
