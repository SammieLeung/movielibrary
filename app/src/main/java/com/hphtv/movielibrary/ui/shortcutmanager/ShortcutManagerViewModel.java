package com.hphtv.movielibrary.ui.shortcutmanager;

import android.app.Application;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.DeviceDao;
import com.hphtv.movielibrary.roomdb.dao.MovieVideofileCrossRefDao;
import com.hphtv.movielibrary.roomdb.dao.ScanDirectoryDao;
import com.hphtv.movielibrary.roomdb.dao.ShortcutDao;
import com.hphtv.movielibrary.roomdb.dao.VideoFileDao;
import com.hphtv.movielibrary.roomdb.entity.Device;
import com.hphtv.movielibrary.roomdb.entity.Shortcut;
import com.hphtv.movielibrary.roomdb.entity.VideoFile;
import com.station.kit.util.Base64Helper;
import com.station.kit.util.LogUtil;
import com.station.kit.util.SharePreferencesTools;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * 设备管理页
 * author: Sam Leung
 * date:  2021/8/24
 */
public class ShortcutManagerViewModel extends AndroidViewModel {
    public static final String TAG = ShortcutManagerViewModel.class.getSimpleName();
    private DeviceDao mDeviceDao;
    private ShortcutDao mShortcutDao;
    private MovieVideofileCrossRefDao mMovieVideofileCrossRefDao;
    private VideoFileDao mVideoFileDao;
    private List<Shortcut> mShortcutList;


    public ShortcutManagerViewModel(@NonNull @NotNull Application application) {
        super(application);
        MovieLibraryRoomDatabase roomDatabase = MovieLibraryRoomDatabase.getDatabase(application);
        mDeviceDao = roomDatabase.getDeviceDao();
        mShortcutDao = roomDatabase.getShortcutDao();
        mMovieVideofileCrossRefDao=roomDatabase.getMovieVideofileCrossRefDao();
        mVideoFileDao=roomDatabase.getVideoFileDao();
    }

    /**
     * 读取所有索引
     */
    public Observable<List<Shortcut>> loadShortcuts() {
        return Observable.just("")
                .subscribeOn(Schedulers.io())
                .map(s -> {
                    mShortcutList = mShortcutDao.queryAllShortcuts();
                    for(Shortcut shortcut:mShortcutList){
                        int fileCount=mShortcutDao.queryTotalFiles(shortcut.uri);
                        int matchedCount=mShortcutDao.queryMatchedFiles(shortcut.uri);
                        shortcut.fileCount=fileCount;
                        shortcut.posterCount=matchedCount;
                        mShortcutDao.updateShortcut(shortcut);
                    }
                    return mShortcutList;
                })
                .observeOn(AndroidSchedulers.mainThread());
    }


    /**
     * 添加索引
     *
     * @param uri
     */
    public Observable<Shortcut> addShortcut(Uri uri) {
        return Observable.just(uri)
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
                                    shortcut = new Shortcut(path, device.type, null, null, path);
                                    shortcut.devicePath = device.path;
                                    long id = mShortcutDao.insertShortcut(shortcut);
                                    shortcut.shortcutId = id;
                                }
                                return shortcut;
                            }
                        }
                    } else if (deviceTypeStr.equals("dlna") || deviceTypeStr.equals("samba")) {

                        Shortcut shortcut = mShortcutDao.queryShortcutByUri(path);
                        if (shortcut == null) {
                            if (deviceTypeStr.equals("samba")) {
                                shortcut = new Shortcut(path, Constants.DeviceType.DEVICE_TYPE_SMB, null, null, queryUri.toString());
                                long id = mShortcutDao.insertShortcut(shortcut);
                                shortcut.shortcutId = id;
                            } else {
                                shortcut = new Shortcut(path, Constants.DeviceType.DEVICE_TYPE_DLNA, null, null, queryUri.toString());
                                long id = mShortcutDao.insertShortcut(shortcut);
                                shortcut.shortcutId = id;
                            }
                        }
                        return shortcut;
                    }
                    return new Shortcut("", 0, null, null, "");
                })
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 移除索引
     *
     * @param shortcut
     */
    public Observable<Shortcut> removeShortcut(Shortcut shortcut) {
        return Observable.just(shortcut)
                .subscribeOn(Schedulers.io())
                .map(_shortcut -> {
                    mShortcutDao.delete(_shortcut);
                    List<VideoFile> videoFileList=mVideoFileDao.queryVideoFilesOnShortcut(_shortcut.uri);
                    List<String> paths=new ArrayList<>();
                    for(VideoFile videoFile:videoFileList){
                        paths.add(videoFile.path);
                    }
                    mVideoFileDao.deleteVideoFilesOnShortcut(_shortcut.uri);
                    mMovieVideofileCrossRefDao.deleteByPaths(paths);

                    return _shortcut;
                })
                .observeOn(AndroidSchedulers.mainThread());
    }

}
