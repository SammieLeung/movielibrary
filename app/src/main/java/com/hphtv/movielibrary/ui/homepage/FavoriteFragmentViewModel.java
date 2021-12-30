package com.hphtv.movielibrary.ui.homepage;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.MovieDao;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.hphtv.movielibrary.util.ScraperSourceTools;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * author: Sam Leung
 * date:  2021/8/23
 */
public class FavoriteFragmentViewModel extends AndroidViewModel {
    private MovieDao mMovieDao;
    private String mSource;
    public FavoriteFragmentViewModel(@NonNull @NotNull Application application) {
        super(application);
        mSource= ScraperSourceTools.getSource();
        MovieLibraryRoomDatabase database = MovieLibraryRoomDatabase.getDatabase(application);
        mMovieDao = database.getMovieDao();
    }

    public void prepareFavorite(Callback callback) {
        Observable.just("")
                .map(s -> {
                    List<MovieDataView> movieDataViewList = mMovieDao.queryFavoriteMovieDataView(mSource);
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
