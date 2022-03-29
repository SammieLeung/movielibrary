package com.hphtv.movielibrary.ui.detail;

import android.app.Application;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.hphtv.movielibrary.BaseAndroidViewModel;
import com.hphtv.movielibrary.data.Config;
import com.hphtv.movielibrary.roomdb.dao.MovieDao;
import com.hphtv.movielibrary.roomdb.entity.Genre;
import com.hphtv.movielibrary.roomdb.entity.Movie;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.hphtv.movielibrary.scraper.api.tmdb.TmdbApiService;
import com.hphtv.movielibrary.scraper.respone.MovieDetailRespone;
import com.hphtv.movielibrary.util.ActivityHelper;
import com.hphtv.movielibrary.util.BroadcastHelper;
import com.hphtv.movielibrary.util.ScraperSourceTools;
import com.hphtv.movielibrary.util.StringTools;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.MovieVideofileCrossRefDao;
import com.hphtv.movielibrary.roomdb.dao.VideoFileDao;
import com.hphtv.movielibrary.roomdb.entity.relation.MovieWrapper;
import com.hphtv.movielibrary.roomdb.entity.dataview.UnrecognizedFileDataView;
import com.hphtv.movielibrary.roomdb.entity.VideoFile;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;

import org.jetbrains.annotations.NotNull;

import java.sql.Wrapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * author: Sam Leung
 * date:  2021/6/15
 */
public class MovieDetailViewModel extends BaseAndroidViewModel {
    private MovieDao mMovieDao;
    private MovieVideofileCrossRefDao mMovieVideofileCrossRefDao;
    private VideoFileDao mVideoFileDao;


    private ExecutorService mSingleThreadPool;
    private MovieWrapper mMovieWrapper;
    private List<MovieDataView> mRecommandList = new ArrayList<>();

    private String mSource;

    public MovieDetailViewModel(@NonNull @NotNull Application application) {
        super(application);
        mSingleThreadPool = Executors.newSingleThreadExecutor();
        mSource = ScraperSourceTools.getSource();
        initData();
    }

    private void initData() {
        mMovieDao = MovieLibraryRoomDatabase.getDatabase(getApplication()).getMovieDao();
        mMovieVideofileCrossRefDao = MovieLibraryRoomDatabase.getDatabase(getApplication()).getMovieVideofileCrossRefDao();
        mVideoFileDao = MovieLibraryRoomDatabase.getDatabase(getApplication()).getVideoFileDao();
    }

    public Observable<MovieWrapper> loadMovieWrapper(long id) {
        return Observable.just(id)
                .subscribeOn(Schedulers.from(mSingleThreadPool))
                .map(movie_id -> {
                    mMovieWrapper = mMovieDao.queryMovieWrapperById(movie_id, mSource);
                    return mMovieWrapper;
                })
                .observeOn(AndroidSchedulers.mainThread());
    }


    public Observable<List<MovieDataView>> loadRecommand() {
        return Observable.just(mMovieWrapper.genres)
                .subscribeOn(Schedulers.from(mSingleThreadPool))
                .map(list -> {
                    List<String> genreName = new ArrayList<>();
                    for (Genre genre : list) {
                        genreName.add(genre.name);
                    }
                    List<MovieDataView> dataViewList = new ArrayList<>();
                    dataViewList.addAll(mMovieDao.queryRecommand(ScraperSourceTools.getSource(), Config.getSqlConditionOfChildMode(), genreName, mMovieWrapper.movie.id, 0, 10));
                    return dataViewList;
                })
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<List<String>> loadTags() {
        return Observable.just(mMovieWrapper)
                .subscribeOn(Schedulers.from(mSingleThreadPool))
                .map(movieWrapper -> {
                    List<String> tagList = new ArrayList<>();
                    if (!TextUtils.isEmpty(movieWrapper.movie.region)) {
                        Locale locale = new Locale.Builder().setRegion(movieWrapper.movie.region).build();
                        tagList.add(locale.getDisplayName());
                    }
                    if (movieWrapper.genres.size() > 0) {
                        for (int i = 0; i < movieWrapper.genres.size() && i < 3; i++) {
                            tagList.add(movieWrapper.genres.get(i).name);
                        }
                    }
                    if (!TextUtils.isEmpty(movieWrapper.movie.year)) {
                        tagList.add(movieWrapper.movie.year);
                    }
                    return tagList;
                }).observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<Boolean> setLike(boolean isLike) {
        return Observable.just(mMovieWrapper)
                .map(wrapper -> {
                    boolean isFavorite = isLike;
                    String movieId = wrapper.movie.movieId;
                    mMovieDao.updateFavoriteStateByMovieId(isFavorite, movieId);
                    return isFavorite;
                }).subscribeOn(Schedulers.from(mSingleThreadPool))
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<Boolean> toggleLike() {
        return setLike(!mMovieWrapper.movie.isFavorite);
    }

    public void playingVideo(String path, String name) {
        getApplication().playingMovie(path, name)
                .subscribe(new SimpleObserver<String>() {
                    @Override
                    public void onAction(String s) {
                    }
                });
    }

    public Observable<String> removeMovieWrapper() {
        return Observable.just("")
                .subscribeOn(Schedulers.from(mSingleThreadPool))
                .map(s -> {
                    String movie_id = mMovieWrapper.movie.movieId;

                    List<Movie> movieList = mMovieDao.queryByMovieId(movie_id);
                    for (Movie movie : movieList) {
                        mMovieVideofileCrossRefDao.deleteById(movie.id);
                    }
                    mMovieDao.updateFavoriteStateByMovieId(false, movie_id);//电影的收藏状态在删除时要设置为false
                    return movie_id;
                })
                .observeOn(AndroidSchedulers.mainThread());

    }

    public Observable<MovieWrapper> selectMovie(final String movie_id, final String source, final Constants.SearchType type) {
        return Observable.create((ObservableOnSubscribe<MovieWrapper>) emitter -> {
            MovieWrapper wrapper = TmdbApiService.getDetail(movie_id, source, type.name())
                    .blockingFirst().toEntity();
            if (wrapper != null) {
                String last_movie_id = mMovieWrapper.movie.movieId;
                boolean is_favoirte = mMovieWrapper.movie.isFavorite;
                boolean is_watched = mMovieWrapper.movie.isWatched;
                BroadcastHelper.sendBroadcastMovieUpdateSync(getApplication(), last_movie_id, movie_id, is_favoirte ? 1 : 0);//向手机助手发送电影更改的广播
                ActivityHelper.saveMatchedMovieWrapper(getApplication(), wrapper, mMovieWrapper.videoFiles);
            } else {
                throw new Throwable();
            }
            emitter.onNext(wrapper);
            emitter.onComplete();
        }).subscribeOn(Schedulers.from(mSingleThreadPool))
                .observeOn(AndroidSchedulers.mainThread());

    }

    public Observable<String> loadFileList() {
        return Observable.just(mMovieWrapper)
                .subscribeOn(Schedulers.from(mSingleThreadPool))
                .map(new Function<MovieWrapper, String>() {
                    @Override
                    public String apply(MovieWrapper wrapper) throws Throwable {
                        List<VideoFile> list = wrapper.videoFiles;
                        StringBuffer sb = new StringBuffer();
                        for (VideoFile videoFile : list) {
                            sb.append(StringTools.hideSmbAuthInfo(videoFile.path) + "\n");
                        }
                        return sb.toString();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread());
    }

    public MovieWrapper getMovieWrapper() {
        return mMovieWrapper;
    }


    public List<MovieDataView> getRecommandList() {
        return mRecommandList;
    }

    public interface Callback2 {
        void runOnUIThread(Object... args);
    }

    public interface MovieWrapperCallback {
        void runOnUIThread(MovieWrapper movieWrapper);
    }

    public interface UnrecognizedFileCallback {
        void runOnUIThread(List<UnrecognizedFileDataView> unrecognizedFileDataViewList);
    }
}
