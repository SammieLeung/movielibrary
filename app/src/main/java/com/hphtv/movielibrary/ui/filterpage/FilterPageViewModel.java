package com.hphtv.movielibrary.ui.filterpage;

import android.app.Application;

import androidx.annotation.NonNull;

import com.hphtv.movielibrary.BaseAndroidViewModel;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.MovieDao;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * author: Sam Leung
 * date:  2022/2/22
 */
public class FilterPageViewModel extends BaseAndroidViewModel {
    public static final int sLimit=15;
    private int mOffset=0;
    private MovieDao mMovieDao;
    public FilterPageViewModel(@NonNull @NotNull Application application) {
        super(application);
        mMovieDao= MovieLibraryRoomDatabase.getDatabase(application).getMovieDao();
    }

    public Observable<List<MovieDataView>> prepareMovieDataView(int offset){
       return Observable.just(offset)
                .map(new Function<Integer, List<MovieDataView>>() {
                    @Override
                    public List<MovieDataView> apply(Integer integer) throws Throwable {
                        return null;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
