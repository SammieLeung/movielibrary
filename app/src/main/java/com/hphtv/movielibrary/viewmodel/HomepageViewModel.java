package com.hphtv.movielibrary.viewmodel;

import android.app.Application;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.DeviceDao;
import com.hphtv.movielibrary.roomdb.dao.GenreDao;
import com.hphtv.movielibrary.roomdb.dao.MovieDao;
import com.hphtv.movielibrary.roomdb.dao.VideoFileDao;
import com.hphtv.movielibrary.roomdb.entity.Device;
import com.hphtv.movielibrary.roomdb.entity.MovieDataView;
import com.hphtv.movielibrary.roomdb.entity.MovieWrapper;
import com.hphtv.movielibrary.roomdb.entity.VideoFile;
import com.hphtv.movielibrary.service.MovieScanService2;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
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
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    //筛选条件数据
    private List<String> mConditionGenres = new ArrayList<>();// 电影类型数据
    private List<String> mConditionYears = new ArrayList<>();

    private List<Device> mConditionDevices = new ArrayList<>();


    public List<VideoFile> mAllNotScannedVideoFiles;

    public List<MovieWrapper> mMovieWrappers;

    private Device mDevice;
    private String mYear;
    private String mGenre;
    private int mSortType;
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
    public void prepareSortDeviceConditions(Callback callback) {
        Observable.just(GET_NOT_SCAN_VIDEOFILES)
                .observeOn(Schedulers.from(mSingleThreadPool))
                .doOnNext(s -> {
                    switch (s) {
                        case GET_NOT_SCAN_VIDEOFILES:
                            mConditionDevices = mDeviceDao.qureyAll();
                            break;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<String>() {
                    @Override
                    public void onAction(String s) {
                      callback.runOnUIThread();
                    }
                });
    }


    /**
     * 3.读取其他条件
     * 4.扫描结束后调用/不需要扫描调用
     *
     * @return
     */
    public void prepareConditions(Callback callback) {
        Observable.just(PREPARE_CONDITIONS)
                .subscribeOn(Schedulers.from(mSingleThreadPool))
                .doOnNext(s -> {
                    mConditionDevices = mDeviceDao.qureyAll();
                    mConditionGenres = mGenreDao.queryAllGenres();
                    mConditionYears = mMovieDao.qureyYearsGroup();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<String>() {
                    @Override
                    public void onAction(String s) {
                        switch (s) {
                            case HomepageViewModel.PREPARE_CONDITIONS:
                                callback.runOnUIThread();
                                break;
                        }
                    }
                });
    }


    public void prepareMovies(Device device, String year, String genre, int sortType, boolean isDesc, Callback callback) {
        mDevice = device;
        mYear = year;
        mGenre = genre;
        mSortType = sortType;
        this.isDesc = isDesc;
        prepareMovies(callback);
    }

    public void prepareMovies(Device device, String year, String genre, Callback callback) {
        mDevice = device;
        mYear = year;
        mGenre = genre;
        prepareMovies(callback);
    }

    public void prepareMovies(int sortType, boolean isDesc, Callback callback) {
        mSortType = sortType;
        this.isDesc = isDesc;
        prepareMovies(callback);
    }

    public void prepareMovies(Callback callback) {
        Observable.just(GET_HOMEPAGE_MOVIE)
                .subscribeOn(Schedulers.from(mSingleThreadPool))
                .map(s -> {
                    String device_id = null;
                    String year = null;
                    String genre = null;
                    if (mDevice != null)
                        device_id = mDevice.id;
                    if (!TextUtils.isEmpty(mYear))
                        year = mYear;
                    if (!TextUtils.isEmpty(mGenre))
                        genre = mGenre;

                    int sortType = mSortType;
                    List<MovieDataView> list = mMovieDao.queryMoiveDataView(device_id, year, genre, sortType, isDesc);
                    return list;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<MovieDataView>>() {
                    @Override
                    public void onAction(List<MovieDataView> movieDataViews) {
                        callback.runOnUIThread(movieDataViews);
                    }
                });
    }


    public void clearNotScannedVideoFiles() {
        if (mAllNotScannedVideoFiles != null)
            mAllNotScannedVideoFiles.clear();
    }

    public static final String GET_NOT_SCAN_VIDEOFILES = "get_not_scan_videofiles";
    public static final String PREPARE_CONDITIONS = "prepare_conditions";

    public static final String GET_NOT_SCANNED_VIDEOFILE = "event2";
    public static final String GET_HOMEPAGE_MOVIE = "event4";


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
        void runOnUIThread(Object... args);
    }
}
