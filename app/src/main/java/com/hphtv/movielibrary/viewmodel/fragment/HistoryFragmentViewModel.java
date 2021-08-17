package com.hphtv.movielibrary.viewmodel.fragment;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.MovieDao;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * author: Sam Leung
 * date:  2021/8/21
 */
public class HistoryFragmentViewModel extends AndroidViewModel {
    private MovieDao mMovieDao;

    public HistoryFragmentViewModel(@NonNull @NotNull Application application) {
        super(application);
        MovieLibraryRoomDatabase database = MovieLibraryRoomDatabase.getDatabase(application);
        mMovieDao = database.getMovieDao();
    }

    public void prepareHistory(Callback callback) {
        Observable.just("")
                .map(s -> {
                    List<MovieDataView> movieDataViewList = mMovieDao.queryHistoryMovieDataView();
                    return movieDataViewList;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(dataViewList -> {
                    if (callback != null)
                        callback.runOnUIThread(dataViewList);
                });
    }

    public interface Callback {
        void runOnUIThread(List<MovieDataView> dataViewList);
    }
}
