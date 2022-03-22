package com.hphtv.movielibrary.ui.homepage;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.DeviceDao;
import com.hphtv.movielibrary.roomdb.dao.GenreDao;
import com.hphtv.movielibrary.roomdb.dao.MovieDao;
import com.hphtv.movielibrary.roomdb.entity.Device;
import com.hphtv.movielibrary.util.ScraperSourceTools;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * author: Sam Leung
 * date:  2021/6/1
 */
public class HomepageViewModel extends AndroidViewModel {
    private DeviceDao mDeviceDao;
    private MovieDao mMovieDao;
    private GenreDao mGenreDao;
    private ExecutorService mSingleThreadPool;

    //筛选条件数据
    private List<String> mConditionGenres = new ArrayList<>();// 电影类型数据
    private List<String> mConditionYears = new ArrayList<>();

    private List<Device> mConditionDevices = new ArrayList<>();

    private MutableLiveData<Integer> mCurrentFragmentPos = new MutableLiveData<>();

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
                    mConditionGenres = mGenreDao.queryGenresBySource(ScraperSourceTools.getSource());
                    mConditionYears = mMovieDao.queryYearsGroup();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<String>() {
                    @Override
                    public void onAction(String s) {
                        switch (s) {
                            case HomepageViewModel.PREPARE_CONDITIONS:
                                if (callback != null)
                                    callback.runOnUIThread();
                                break;
                        }
                    }
                });
    }


    public static final String GET_NOT_SCAN_VIDEOFILES = "get_not_scan_videofiles";
    public static final String PREPARE_CONDITIONS = "prepare_conditions";


    public List<String> getConditionGenres() {
        return mConditionGenres;
    }

    public List<String> getConditionYears() {
        return mConditionYears;
    }

    public List<Device> getConditionDevices() {
        return mConditionDevices;
    }

    public MutableLiveData<Integer> getCurrentFragmentPos() {
        return mCurrentFragmentPos;
    }

    public interface Callback {
        void runOnUIThread(Object... args);
    }
}
