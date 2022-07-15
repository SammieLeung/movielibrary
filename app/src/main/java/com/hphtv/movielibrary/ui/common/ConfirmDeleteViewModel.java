package com.hphtv.movielibrary.ui.common;

import android.app.Application;

import androidx.annotation.NonNull;

import com.hphtv.movielibrary.BaseAndroidViewModel;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.MovieDao;
import com.hphtv.movielibrary.roomdb.dao.MovieVideofileCrossRefDao;
import com.hphtv.movielibrary.roomdb.entity.Movie;
import com.hphtv.movielibrary.scraper.service.OnlineDBApiService;
import com.hphtv.movielibrary.util.ScraperSourceTools;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * author: Sam Leung
 * date:  2022/3/30
 */
public class ConfirmDeleteViewModel extends BaseAndroidViewModel {

    public ConfirmDeleteViewModel(@NonNull @NotNull Application application) {
        super(application);
    }


    public Observable<String> removeMovieWrapper(String movie_id,String type) {
        MovieDao movieDao= MovieLibraryRoomDatabase.getDatabase(getApplication()).getMovieDao();
        MovieVideofileCrossRefDao movieVideofileCrossRefDao= MovieLibraryRoomDatabase.getDatabase(getApplication()).getMovieVideofileCrossRefDao();
        return Observable.just("")
                .subscribeOn(Schedulers.newThread())
                .map(str -> {
                    List<Movie> movieList = movieDao.queryByMovieIdAndType(movie_id,type);
                    for (Movie movie : movieList) {
                        movieVideofileCrossRefDao.deleteById(movie.id);
                    }
                    movieDao.updateFavoriteStateByMovieId(movie_id,type,false);//电影的收藏状态在删除时要设置为false
                    OnlineDBApiService.deleteMovie(movie_id,type, ScraperSourceTools.getSource());
                    return movie_id;
                })
                .observeOn(AndroidSchedulers.mainThread());
    }





}
