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
import com.hphtv.movielibrary.roomdb.dao.VideoFileDao;
import com.hphtv.movielibrary.roomdb.entity.MovieWrapper;
import com.hphtv.movielibrary.roomdb.entity.Trailer;
import com.hphtv.movielibrary.roomdb.entity.UnrecognizedFileDataView;
import com.hphtv.movielibrary.roomdb.entity.VideoFile;
import com.hphtv.movielibrary.util.VideoPlayTools;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;

import org.jetbrains.annotations.NotNull;

import java.util.List;
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
    private MovieDao mMovieDao;
    private MovieVideofileCrossRefDao mMovieVideofileCrossRefDao;
    private VideoFileDao mVideoFileDao;
    private ExecutorService mSingleThreadPool;
    private MovieWrapper mMovieWrapper;

    public MovieDetailViewModel(@NonNull @NotNull Application application) {
        super(application);
        mSingleThreadPool = Executors.newSingleThreadExecutor();
        initData();
    }

    private void initData() {
        mMovieDao = MovieLibraryRoomDatabase.getDatabase(getApplication()).getMovieDao();
        mMovieVideofileCrossRefDao = MovieLibraryRoomDatabase.getDatabase(getApplication()).getMovieVideofileCrossRefDao();
        mVideoFileDao = MovieLibraryRoomDatabase.getDatabase(getApplication()).getVideoFileDao();
    }

    public void getMovieWrapper(long id, MovieWrapperCallback callback) {
        Observable.just(id)
                .subscribeOn(Schedulers.from(mSingleThreadPool))
                .map(movie_id -> {
                    mMovieWrapper = mMovieDao.queryMovieWrapperById(movie_id);
                    return mMovieWrapper;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<MovieWrapper>() {
                    @Override
                    public void onAction(MovieWrapper movieWrapper) {
                        if (callback != null)
                            callback.runOnUIThread(movieWrapper);
                    }
                });
    }

    public void getUnrecogizedFile(String keyword, UnrecognizedFileCallback callback) {
        Observable.just(keyword)
                .subscribeOn(Schedulers.from(mSingleThreadPool))
                .map(sKeyword -> mVideoFileDao.queryUnrecognizedFilesByKeyword(sKeyword))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<UnrecognizedFileDataView>>() {
                    @Override
                    public void onAction(List<UnrecognizedFileDataView> unrecognizedFileDataViewList) {
                        if (callback != null)
                            callback.runOnUIThread(unrecognizedFileDataViewList);
                    }
                });
    }

    public void setFavorite(MovieWrapper movieWrapper) {
        Observable.just(movieWrapper)
                .subscribeOn(Schedulers.from(mSingleThreadPool))
                .subscribe(movieWrapper1 -> mMovieDao.updateFavorite(movieWrapper1.movie.isFavorite, movieWrapper1.movie.id));
    }


    public void playTralier(Trailer trailer) {
        VideoPlayTools.play(getApplication(), Uri.parse(trailer.url));
    }

    public void playingVideo(MovieWrapper wrapper, VideoFile file) {
        Observable.just(file)
                .subscribeOn(Schedulers.from(mSingleThreadPool))
                //记录播放时间，作为播放记录
                .doOnNext(videoFile -> {
                    String path = videoFile.path;
                    mMovieDao.updateLastPlaytime(path, System.currentTimeMillis());
                    SharePreferencesTools.getInstance(getApplication()).saveProperty(ConstData.SharePreferenceKeys.LAST_POTSER, wrapper.movie.poster);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<VideoFile>() {
                    @Override
                    public void onAction(VideoFile videoFile) {
                        VideoPlayTools.play(getApplication(), videoFile);
                    }
                });
    }

    public void removeMovieWrapper(Callback2 callback) {
        Observable.just("")
                .subscribeOn(Schedulers.from(mSingleThreadPool))
                .doOnNext(s -> {
                    long movieId = mMovieWrapper.movie.id;
                    mMovieVideofileCrossRefDao.deleteById(movieId);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<String>() {
                    @Override
                    public void onAction(String s) {
                        if (callback != null)
                            callback.runOnUIThread();
                    }
                });
    }

    public interface Callback2 {
        void runOnUIThread();
    }

    public interface Callback {
        void runOnUIThreadDelay();
    }

    public interface MovieWrapperCallback {
        void runOnUIThread(MovieWrapper movieWrapper);
    }

    public interface UnrecognizedFileCallback {
        void runOnUIThread(List<UnrecognizedFileDataView> unrecognizedFileDataViewList);
    }
}
