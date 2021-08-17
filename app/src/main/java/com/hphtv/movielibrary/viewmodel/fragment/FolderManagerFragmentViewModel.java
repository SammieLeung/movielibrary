package com.hphtv.movielibrary.viewmodel.fragment;

import android.app.Application;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.hphtv.movielibrary.data.ConstData;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.DeviceDao;
import com.hphtv.movielibrary.roomdb.dao.ScanDirectoryDao;
import com.hphtv.movielibrary.roomdb.entity.Device;
import com.hphtv.movielibrary.roomdb.entity.ScanDirectory;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;
import com.station.kit.util.Base64Helper;
import com.station.kit.util.LogUtil;
import com.station.kit.util.SharePreferencesTools;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * author: Sam Leung
 * date:  2021/8/24
 */
public class FolderManagerFragmentViewModel extends AndroidViewModel {
    private ScanDirectoryDao mScanDirectoryDao;
    private DeviceDao mDeviceDao;
    private boolean passwordhasBeenSet = false;

    public FolderManagerFragmentViewModel(@NonNull @NotNull Application application) {
        super(application);
        MovieLibraryRoomDatabase roomDatabase = MovieLibraryRoomDatabase.getDatabase(application);
        mScanDirectoryDao = roomDatabase.getScanDirectoryDao();
        mDeviceDao = roomDatabase.getDeviceDao();
        String password= SharePreferencesTools.getInstance(application).readProperty(ConstData.SharePreferenceKeys.PASSWORD,"");
        passwordhasBeenSet = TextUtils.isEmpty(password) ? false : true;
    }

    public void loadScanDirectory(Callback callback) {
        Observable.just("")
                .subscribeOn(Schedulers.io())
                .map(s -> {
                    List<ScanDirectory> scanDirectoryList = mScanDirectoryDao.queryAllNotHiddenScanDirectories();
                    return scanDirectoryList;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(scanDirectoryList -> {
                    if (callback != null)
                        callback.refreshScanDirectoryList(scanDirectoryList);
                });
    }

    public void loadHiddenScanDirectory(Callback callback) {
        Observable.just("")
                .subscribeOn(Schedulers.io())
                .map(s -> {
                    List<ScanDirectory> scanDirectoryList = mScanDirectoryDao.queryAllHiddenScanDirectories();
                    return scanDirectoryList;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(scanDirectoryList -> {
                    if (callback != null)
                        callback.refreshHiddenScanDirectoryList(scanDirectoryList);
                });
    }

    public void addScanDirectoryByUri(Uri uri, Callback callback) {
        Observable.just(uri)
                .subscribeOn(Schedulers.io())
                .map(uri1 -> {
//                        String deviceTypeStr = uri.getPathSegments().get(0);//device Api str

                    String base64Id = uri1.getPathSegments().get(1);//dir id
                    String path = Base64Helper.decode(base64Id);

//                        int type;
//                        if (deviceTypeStr.equals(ConstData.DeviceType.STR_LOCAL)) {
//                            if (StorageHelper.isMountUsb(getApplication(), path)) {
//                                type = ConstData.DeviceType.DEVICE_TYPE_USB;
//                            } else if (StorageHelper.isMountSdCard(getApplication(), path)) {
//                                type = ConstData.DeviceType.DEVICE_TYPE_SDCARDS;
//                            } else if (StorageHelper.isMountHardDisk(getApplication(), path)) {
//                                type = ConstData.DeviceType.DEVICE_TYPE_HARD_DISK;
//                            } else if (StorageHelper.isMountPcie(getApplication(), path)) {
//                                type = ConstData.DeviceType.DEVICE_TYPE_PCIE;
//                            } else {
//                                type = ConstData.DeviceType.DEVICE_TYPE_INTERNAL_STORAGE;
//                            }
//                        } else if (deviceTypeStr.equals(ConstData.DeviceType.STR_DLNA)) {
//                            type=ConstData.DeviceType.DEVICE_TYPE_DLNA;
//                        } else if (deviceTypeStr.equals(ConstData.DeviceType.STR_SAMBA)) {
//                            type=ConstData.DeviceType.DEVICE_TYPE_SMB;
//                        }

                    for (Device device : mDeviceDao.qureyAll()) {
                        if (path.contains(device.path)) {
                            LogUtil.v("id " + device.id + " localpath:" + device.path);
                            ScanDirectory scanDirectory = mScanDirectoryDao.queryScanDirectoryByPath(path);
                            if (scanDirectory != null) {
                                scanDirectory.isUserAdd = true;
                                scanDirectory.isHidden = false;
                                mScanDirectoryDao.updateScanDirectory(scanDirectory);
                            } else {
                                scanDirectory = new ScanDirectory(path, device.path);
                                scanDirectory.isUserAdd = true;
                                mScanDirectoryDao.insertScanDirectories(scanDirectory);
                            }
                        }
                    }
                    List<ScanDirectory> scanDirectoryList = mScanDirectoryDao.queryAllNotHiddenScanDirectories();
                    return scanDirectoryList;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(scanDirectoryList -> {
                    if (callback != null)
                        callback.refreshScanDirectoryList(scanDirectoryList);
                });

    }

    public void deleteDirecotryByPath(String path, Callback callback) {
        Observable.just(path)
                .subscribeOn(Schedulers.io())
                .doOnNext(_path -> {
                    mScanDirectoryDao.deleteScanDirectory(_path);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(_path1 -> {
                    loadHiddenScanDirectory(callback);
                    loadScanDirectory(callback);
                });
    }

    public void moveToHidden(String path, Callback callback) {
        Observable.just(path)
                .subscribeOn(Schedulers.io())
                .doOnNext(_path -> {
                    mScanDirectoryDao.updateScanDirectoryHiddenState(_path,true);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                   loadScanDirectory(callback);
                   loadHiddenScanDirectory(callback);
                });

    }

    public void moveToPublic(String path,Callback callback){
        Observable.just(path)
                .subscribeOn(Schedulers.io())
                .doOnNext(_path -> {
                    mScanDirectoryDao.updateScanDirectoryHiddenState(_path,false);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                    loadScanDirectory(callback);
                    loadHiddenScanDirectory(callback);
                });
    }

    public boolean isPasswordHasBeenSet() {
        return passwordhasBeenSet;
    }

    public interface Callback {
        void refreshScanDirectoryList(List<ScanDirectory> scanDirectoryList);
        void refreshHiddenScanDirectoryList(List<ScanDirectory> scanDirectoryList);
    }
}
