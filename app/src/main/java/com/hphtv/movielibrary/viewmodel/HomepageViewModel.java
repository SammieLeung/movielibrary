package com.hphtv.movielibrary.viewmodel;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.firelfy.util.LogUtil;
import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.activity.HomePageActivity;
import com.hphtv.movielibrary.data.ConstData;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.DeviceDao;
import com.hphtv.movielibrary.roomdb.dao.GenreDao;
import com.hphtv.movielibrary.roomdb.dao.MovieDao;
import com.hphtv.movielibrary.roomdb.dao.VideoFileDao;
import com.hphtv.movielibrary.roomdb.entity.Device;
import com.hphtv.movielibrary.roomdb.entity.VideoFile;
import com.hphtv.movielibrary.service.MovieScanService2;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * author: Sam Leung
 * date:  2021/6/1
 */
public class HomepageViewModel extends AndroidViewModel {
    private DeviceDao mDeviceDao;
    private MovieDao mMovieDao;
    private GenreDao mGenreDao;
    private VideoFileDao mVideoFileDao;
    private ExecutorService mSingleThreadPool;
    private CompositeDisposable mCompositeDisposable=new CompositeDisposable();

    //筛选条件数据
    private List<String> mConditionGenres = new ArrayList<>();// 电影类型数据
    private List<String> mConditionYears = new ArrayList<>();
    ;
    private List<Device> mConditionDevices = new ArrayList<>();
    ;//

    public List<VideoFile> mAllNotScannedVideoFiles;

    private boolean isDesc = false;

    public HomepageViewModel(@NonNull @NotNull Application application) {
        super(application);
        init();
    }

    /**
     * 初始化
     */
    private void init() {
        mSingleThreadPool = Executors.newSingleThreadExecutor();

        initDao();
    }

    /**
     * 初始化Dao类
     */
    private void initDao() {
        MovieLibraryRoomDatabase movieLibraryRoomDatabase = MovieLibraryRoomDatabase.getDatabase(getApplication());
        mDeviceDao = movieLibraryRoomDatabase.getDeviceDao();
        mMovieDao = movieLibraryRoomDatabase.getMovieDao();
        mGenreDao = movieLibraryRoomDatabase.getGenreDao();
        mVideoFileDao = movieLibraryRoomDatabase.getVideoFileDao();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        mCompositeDisposable.clear();
    }

    /**
     * 1.加载筛选数据 设备、排序方式
     * 2.获取所有未扫描设备
     *
     * @return
     */
    public void prepareSortDeviceConditions(MovieScanService2 scanService, Callback callback) {
        Observable.just(GET_BASE_CONDITIONS)
                .observeOn(Schedulers.from(mSingleThreadPool))
                .doOnNext(s -> {
                    switch (s) {
                        case GET_BASE_CONDITIONS:
                            mConditionDevices = mDeviceDao.qureyAll();
                            mAllNotScannedVideoFiles = getNotScannedFiles();
                            break;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<String>() {
                    @Override
                    public void onAction(String s) {
                        if (mAllNotScannedVideoFiles != null && mAllNotScannedVideoFiles.size() > 0)
                            HomepageViewModel.this.startScrap(scanService, mAllNotScannedVideoFiles);
                        else
                            HomepageViewModel.this.prepareOtherConditions(callback);
                    }
                });
    }

    public void prepareSortDeviceConditions(MovieScanService2 scanService, String mountPath, Callback callback) {
        Observable.just(mountPath)
                .subscribeOn(Schedulers.from(mSingleThreadPool))
                .observeOn(Schedulers.from(mSingleThreadPool))
                .map(s -> {
                    Device device = mDeviceDao.querybyMountPath(mountPath);
                    boolean isContain = false;
                    if (device != null) {
                        for (int i = 0; i < mConditionDevices.size(); i++) {
                            Device dev = mConditionDevices.get(i);
                            if (dev.localPath.equalsIgnoreCase(device.localPath)) {
                                mConditionDevices.add(i, device);
                                mConditionDevices.remove(dev);
                                isContain = true;
                                break;
                            }
                        }
                        if (!isContain)
                            mConditionDevices.add(device);
                    }
                    return device;
                })
                .map(device -> {
                    List<VideoFile> videoFiles = getNotScannedFiles(device);
                    return videoFiles;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<VideoFile>>() {
                    @Override
                    public void onAction(List<VideoFile> videoFiles) {
                        if (videoFiles != null && videoFiles.size() > 0)
                            HomepageViewModel.this.startScrap(scanService, videoFiles);
                        else
                            HomepageViewModel.this.prepareOtherConditions(callback);
                    }
                });
    }

    /**
     * 3.读取其他条件
     * 4.扫描结束后调用/不需要扫描调用
     *
     * @return
     */
    public void prepareOtherConditions(Callback callback) {
        Observable.just(GET_MOVIE_CONDITION)
                .subscribeOn(Schedulers.from(mSingleThreadPool))
                .doOnNext(s -> {
                    mConditionGenres = mGenreDao.queryAllGenres();
                    mConditionYears = mMovieDao.qureyYearsGroup();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<String>() {
                    @Override
                    public void onAction(String s) {
                        switch (s) {
                            case HomepageViewModel.GET_MOVIE_CONDITION:
                                callback.runOnUIThread();
                                break;
                        }
                    }
                });
    }

    /**
     * 开始对未匹配文件进行海报匹配
     *
     * @param service
     * @param videoFileList
     */
    public void startScrap(MovieScanService2 service, List<VideoFile> videoFileList) {
        if (videoFileList.size() > 0) {
            service.start(videoFileList);
        }
    }

    public void clearNotScannedVideoFiles() {
        if (mAllNotScannedVideoFiles != null)
            mAllNotScannedVideoFiles.clear();
    }

    public static final String GET_BASE_CONDITIONS = "event1";
    public static final String GET_NOT_SCANNED_VIDEOFILE = "event2";
    public static final String GET_MOVIE_CONDITION = "event3";
    public static final String EVENT4 = "event4";


    /**
     * 获取所以未扫描的文件
     */
    private List<VideoFile> getNotScannedFiles() {
        String[] deviceIds = new String[mConditionDevices.size()];
        for (int i = 0; i < mConditionDevices.size(); i++) {
            deviceIds[i] = mConditionDevices.get(i).id;
        }
        List<VideoFile> mountedDeviceFiles = mVideoFileDao.queryAllNotScanedByIds(deviceIds);
        return mountedDeviceFiles;
    }

    /**
     * 获取所以未扫描的文件
     */
    private List<VideoFile> getNotScannedFiles(Device device) {
        List<VideoFile> mountedDeviceFiles = mVideoFileDao.queryAllNotScanedByIds(device.id);
        return mountedDeviceFiles;
    }

    public void startScrap(MovieScanService2 service) {
        if (mAllNotScannedVideoFiles != null && mAllNotScannedVideoFiles.size() > 0) {
            service.start(mAllNotScannedVideoFiles);
        }
    }

    public List<String> getConditionGenres() {
        return mConditionGenres;
    }

    public List<String> getConditionYears() {
        return mConditionYears;
    }

    public List<Device> getConditionDevices() {
        return mConditionDevices;
    }

    public interface Callback {
        void runOnUIThread();
    }
}
