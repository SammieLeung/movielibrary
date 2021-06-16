package com.hphtv.movielibrary.viewmodel;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.MovieDao;
import com.hphtv.movielibrary.roomdb.entity.MovieWrapper;
import com.hphtv.movielibrary.roomdb.entity.Trailer;
import com.hphtv.movielibrary.util.VideoPlayTools;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * author: Sam Leung
 * date:  2021/6/15
 */
public class MovieDetailViewModel extends AndroidViewModel {
    public MutableLiveData<MovieWrapper> mWrapperMutableLiveData;
    private MovieDao mMovieDao;
    private ExecutorService mSingleThreadPool;

    public MovieDetailViewModel(@NonNull @NotNull Application application) {
        super(application);
        mSingleThreadPool = Executors.newSingleThreadExecutor();
        initData();
    }

    private void initData() {
        mMovieDao = MovieLibraryRoomDatabase.getDatabase(getApplication()).getMovieDao();
    }

    public void getMovieWrapper(long id, Callback callback) {
        Observable.just(id)
                .subscribeOn(Schedulers.from(mSingleThreadPool))
                .map(movie_id -> {
                    MovieWrapper wrapper = mMovieDao.queryMovieWrapperById(movie_id);
                    return wrapper;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<MovieWrapper>() {
                    @Override
                    public void onAction(MovieWrapper movieWrapper) {
                        callback.runOnUIThread(movieWrapper);
                    }
                });
    }

    public void playTralier(Trailer trailer){
            VideoPlayTools.play(getApplication(), Uri.parse(trailer.url));
    }

    public interface Callback {
        void runOnUIThread(Object... args);
    }
}
