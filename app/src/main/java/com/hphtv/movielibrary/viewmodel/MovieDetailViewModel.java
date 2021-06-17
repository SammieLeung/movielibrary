package com.hphtv.movielibrary.viewmodel;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.firelfy.util.LogUtil;
import com.firelfy.util.SharePreferencesTools;
import com.hphtv.movielibrary.data.ConstData;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.MovieDao;
import com.hphtv.movielibrary.roomdb.dao.MovieVideofileCrossRefDao;
import com.hphtv.movielibrary.roomdb.entity.MovieWrapper;
import com.hphtv.movielibrary.roomdb.entity.Trailer;
import com.hphtv.movielibrary.roomdb.entity.VideoFile;
import com.hphtv.movielibrary.util.VideoPlayTools;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
    private MovieVideofileCrossRefDao mMovieVideofileCrossRefDao;
    private ExecutorService mSingleThreadPool;

    public MovieDetailViewModel(@NonNull @NotNull Application application) {
        super(application);
        mSingleThreadPool = Executors.newSingleThreadExecutor();
        initData();
    }

    private void initData() {
        mMovieDao = MovieLibraryRoomDatabase.getDatabase(getApplication()).getMovieDao();
        mMovieVideofileCrossRefDao=MovieLibraryRoomDatabase.getDatabase(getApplication()).getMovieVideofileCrossRefDao();
    }

    public void getMovieWrapper(long id, MovieWrapperCallback callback) {
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

    public void playingVideo(MovieWrapper wrapper,VideoFile file,Callback callback) {
        Observable.just(file)
                .subscribeOn(Schedulers.from(mSingleThreadPool))
                //记录播放时间，作为播放记录
                .doOnNext(videoFile -> {
                    String path=videoFile.path;
                    mMovieDao.updateLastPlaytime(path,System.currentTimeMillis());
                    SharePreferencesTools.getInstance(getApplication()).saveProperty(ConstData.SharePreferenceKeys.LAST_POTSER,wrapper.movie.poster);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<VideoFile>() {
                    @Override
                    public void onAction(VideoFile videoFile) {
                        VideoPlayTools.play(getApplication(), videoFile);
                        Observable.timer(2, TimeUnit.SECONDS)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new SimpleObserver<Long>() {
                                    @Override
                                    public void onAction(Long aLong) {
                                        callback.runOnUIThreadDelay();
                                    }
                                });
                    }
                });
    }

    public interface Callback {
        void runOnUIThreadDelay();

    }

    public interface MovieWrapperCallback{
        void runOnUIThread(MovieWrapper movieWrapper);
    }
}
