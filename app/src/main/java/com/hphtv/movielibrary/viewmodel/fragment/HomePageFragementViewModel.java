package com.hphtv.movielibrary.viewmodel.fragment;

import android.app.Application;
import android.telecom.Call;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.MovieDao;
import com.hphtv.movielibrary.roomdb.entity.Device;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.hphtv.movielibrary.util.ScraperSourceTools;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * author: Sam Leung
 * date:  2021/6/28
 */
public class HomePageFragementViewModel extends AndroidViewModel {
    private Device mDevice;
    private String mYear;
    private String mGenre;
    private int mSortType;
    private boolean isDesc = false;

    private ExecutorService mSingleThreadPool;

    private MovieDao mMovieDao;

    public HomePageFragementViewModel(@NonNull @NotNull Application application) {
        super(application);
        init();
    }

    private void init() {
        mSingleThreadPool = Executors.newSingleThreadExecutor();
        initDao();
    }

    /**
     * 初始化Dao类
     */
    private void initDao() {
        MovieLibraryRoomDatabase movieLibraryRoomDatabase = MovieLibraryRoomDatabase.getDatabase(getApplication());
        mMovieDao = movieLibraryRoomDatabase.getMovieDao();
    }


    public void prepareMovies(Device device, String year, String genre, int sortType, boolean isDesc, Callback callback) {
        mDevice = device;
        mYear = year;
        mGenre = genre;
        mSortType = sortType;
        this.isDesc = isDesc;
        prepareMovies(callback);
    }

    public void prepareMovies(Callback callback) {
        Observable.just("")
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
                    List<MovieDataView> list = mMovieDao.queryMovieDataView(device_id, year, genre, sortType, ScraperSourceTools.getSource(), isDesc);
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

    public void getMovieDataView(String movie_id, Callback callback) {
        Observable.just(movie_id)
                .map(mid -> {
                    MovieDataView movieDataView = mMovieDao.queryMovieDataViewByMovieId(mid, ScraperSourceTools.getSource());
                    return movieDataView;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<MovieDataView>() {
                    @Override
                    public void onAction(MovieDataView view) {
                        if (callback != null)
                            callback.runOnUIThread(view);
                    }
                });
    }

    public interface Callback {
        void runOnUIThread(Object... args);
    }
}
