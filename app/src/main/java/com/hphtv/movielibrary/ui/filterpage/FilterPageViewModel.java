package com.hphtv.movielibrary.ui.filterpage;

import android.app.Application;

import androidx.annotation.NonNull;

import com.hphtv.movielibrary.BaseAndroidViewModel;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.MovieDao;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.hphtv.movielibrary.util.ScraperSourceTools;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * author: Sam Leung
 * date:  2022/2/22
 */
public class FilterPageViewModel extends BaseAndroidViewModel {
    public static final int LIMIT = 15;
    private AtomicInteger mPage = new AtomicInteger();
    private int mTotal = 0;
    private MovieDao mMovieDao;

    public FilterPageViewModel(@NonNull @NotNull Application application) {
        super(application);
        mMovieDao = MovieLibraryRoomDatabase.getDatabase(application).getMovieDao();
    }

    public Observable<List<MovieDataView>> reloadMoiveDataViews() {
        return Observable.just("")
                .map(_offset -> {
                    mPage.set(0);
                    mTotal = mMovieDao.countAllMovieDataView(ScraperSourceTools.getSource());
                    return mMovieDao.queryAllMovieDataView(ScraperSourceTools.getSource(), mPage.get(), LIMIT);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public int getTotal(){
        return mTotal;
    }


    public Observable<List<MovieDataView>> loadMoiveDataViews() {
        return Observable.just("")
                .map(_offset -> {
                    if((mPage.get()+1)*LIMIT<mTotal) {
                       int offset=mPage.incrementAndGet()*LIMIT;
                        return mMovieDao.queryAllMovieDataView(ScraperSourceTools.getSource(), offset, LIMIT);
                    }else{
                        List<MovieDataView> emptyList=new ArrayList();
                        return emptyList;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
