package com.hphtv.movielibrary.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.MovieDao;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.hphtv.movielibrary.util.PinyinParseAndMatchTools;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * author: Sam Leung
 * date:  2021/8/19
 */
public class MovieSearchViewModel extends AndroidViewModel {

    private MovieDao mMovieDao;

    private List<MovieDataView> mMovieDataViewList;

    public MovieSearchViewModel(@NonNull @NotNull Application application) {
        super(application);
        MovieLibraryRoomDatabase database = MovieLibraryRoomDatabase.getDatabase(application);
        mMovieDao = database.getMovieDao();
    }

    public void init() {
        Observable.just("")
                .observeOn(Schedulers.io())
                .subscribe(s -> mMovieDataViewList = mMovieDao.queryAllMovieDataView());
    }

    public void search(String keyword, Callback<MovieDataView> callback) {
        Observable.just(keyword)
                .map(s -> {
                    return PinyinParseAndMatchTools.getInstance().match(mMovieDataViewList, keyword);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(dataViewList -> {
                    if (callback != null)
                        callback.runOnUIThread(dataViewList);
                });
    }

    public interface Callback<T> {
        void runOnUIThread(List<T> data);
    }
}
