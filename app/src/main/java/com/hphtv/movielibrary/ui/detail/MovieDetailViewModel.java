package com.hphtv.movielibrary.ui.detail;

import android.app.Application;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.hphtv.movielibrary.BaseAndroidViewModel;
import com.hphtv.movielibrary.adapter.NewMovieItemListAdapter;
import com.hphtv.movielibrary.roomdb.dao.ActorDao;
import com.hphtv.movielibrary.roomdb.dao.DirectorDao;
import com.hphtv.movielibrary.roomdb.dao.GenreDao;
import com.hphtv.movielibrary.roomdb.dao.MovieActorCrossRefDao;
import com.hphtv.movielibrary.roomdb.dao.MovieDirectorCrossRefDao;
import com.hphtv.movielibrary.roomdb.dao.MovieGenreCrossRefDao;
import com.hphtv.movielibrary.roomdb.dao.SeasonDao;
import com.hphtv.movielibrary.roomdb.dao.StagePhotoDao;
import com.hphtv.movielibrary.roomdb.dao.TrailerDao;
import com.hphtv.movielibrary.roomdb.entity.Actor;
import com.hphtv.movielibrary.roomdb.entity.Director;
import com.hphtv.movielibrary.roomdb.entity.Genre;
import com.hphtv.movielibrary.roomdb.entity.Movie;
import com.hphtv.movielibrary.roomdb.entity.Season;
import com.hphtv.movielibrary.roomdb.entity.StagePhoto;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.hphtv.movielibrary.roomdb.entity.reference.MovieActorCrossRef;
import com.hphtv.movielibrary.roomdb.entity.reference.MovieDirectorCrossRef;
import com.hphtv.movielibrary.roomdb.entity.reference.MovieGenreCrossRef;
import com.hphtv.movielibrary.roomdb.entity.reference.MovieVideoFileCrossRef;
import com.hphtv.movielibrary.scraper.api.tmdb.TmdbApiService;
import com.hphtv.movielibrary.util.PinyinParseAndMatchTools;
import com.hphtv.movielibrary.util.ScraperSourceTools;
import com.hphtv.movielibrary.util.StringTools;
import com.station.kit.util.SharePreferencesTools;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.MovieDao;
import com.hphtv.movielibrary.roomdb.dao.MovieVideofileCrossRefDao;
import com.hphtv.movielibrary.roomdb.dao.VideoFileDao;
import com.hphtv.movielibrary.roomdb.entity.relation.MovieWrapper;
import com.hphtv.movielibrary.roomdb.entity.Trailer;
import com.hphtv.movielibrary.roomdb.entity.dataview.UnrecognizedFileDataView;
import com.hphtv.movielibrary.roomdb.entity.VideoFile;
import com.hphtv.movielibrary.util.VideoPlayTools;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * author: Sam Leung
 * date:  2021/6/15
 */
public class MovieDetailViewModel extends BaseAndroidViewModel {
    public static final String FROM_DB = "from_db";
    public static final String FROM_NETWORK = "from_network";

    private MovieDao mMovieDao;
    private ActorDao mActorDao;
    private DirectorDao mDirectorDao;
    private GenreDao mGenreDao;
    private MovieActorCrossRefDao mMovieActorCrossRefDao;
    private MovieDirectorCrossRefDao mMovieDirectorCrossRefDao;
    private MovieGenreCrossRefDao mMovieGenreCrossRefDao;
    private MovieVideofileCrossRefDao mMovieVideofileCrossRefDao;
    private VideoFileDao mVideoFileDao;
    private TrailerDao mTrailerDao;
    private StagePhotoDao mStagePhotoDao;
    private SeasonDao mSeasonDao;


    private ExecutorService mSingleThreadPool;
    private MovieWrapper mMovieWrapper;
    private int mCurrentMode;
    private List<UnrecognizedFileDataView> mUnrecognizedFileDataViewList;
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
        mActorDao = MovieLibraryRoomDatabase.getDatabase(getApplication()).getActorDao();
        mDirectorDao = MovieLibraryRoomDatabase.getDatabase(getApplication()).getDirectorDao();
        mGenreDao = MovieLibraryRoomDatabase.getDatabase(getApplication()).getGenreDao();
        mMovieActorCrossRefDao = MovieLibraryRoomDatabase.getDatabase(getApplication()).getMovieActorCrossRefDao();
        mMovieDirectorCrossRefDao = MovieLibraryRoomDatabase.getDatabase(getApplication()).getMovieDirectorCrossRefDao();
        mMovieGenreCrossRefDao = MovieLibraryRoomDatabase.getDatabase(getApplication()).getMovieGenreCrossRefDao();
        mMovieVideofileCrossRefDao = MovieLibraryRoomDatabase.getDatabase(getApplication()).getMovieVideofileCrossRefDao();
        mVideoFileDao = MovieLibraryRoomDatabase.getDatabase(getApplication()).getVideoFileDao();
        mTrailerDao = MovieLibraryRoomDatabase.getDatabase(getApplication()).getTrailerDao();
        mStagePhotoDao = MovieLibraryRoomDatabase.getDatabase(getApplication()).getStagePhotoDao();
        mSeasonDao = MovieLibraryRoomDatabase.getDatabase(getApplication()).getSeasonDao();
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

    public void loadUnrecogizedFile(String keyword, UnrecognizedFileCallback callback) {
        Observable.just(keyword)
                .subscribeOn(Schedulers.from(mSingleThreadPool))
                .map(sKeyword -> {
                    mUnrecognizedFileDataViewList = mVideoFileDao.queryUnrecognizedFilesByKeyword(sKeyword, mSource);
                    return mUnrecognizedFileDataViewList;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<UnrecognizedFileDataView>>() {
                    @Override
                    public void onAction(List<UnrecognizedFileDataView> unrecognizedFileDataViewList) {
                        if (callback != null)
                            callback.runOnUIThread(unrecognizedFileDataViewList);
                    }
                });
    }

    public Observable<List<MovieDataView>> loadRecommand(List<Genre> list) {
        return Observable.just(list)
                .subscribeOn(Schedulers.from(mSingleThreadPool))
                .map(new Function<List<Genre>, List<MovieDataView>>() {
                    @Override
                    public List<MovieDataView> apply(List<Genre> list) throws Throwable {
                        List<String> genreName = new ArrayList<>();
                        for (Genre genre : list) {
                            genreName.add(genre.name);
                        }
                        List<MovieDataView> dataViewList = new ArrayList<>();
                        dataViewList.addAll(mMovieDao.queryRecommand(ScraperSourceTools.getSource(), genreName, mMovieWrapper.movie.id));
                        return dataViewList;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<List<String>> loadTags() {
        return Observable.just(mMovieWrapper)
                .subscribeOn(Schedulers.from(mSingleThreadPool))
                .map(movieWrapper -> {
                    List<String> tagList = new ArrayList<>();
                    if (!TextUtils.isEmpty(movieWrapper.movie.region)) {
                        Locale locale=new Locale.Builder().setRegion(movieWrapper.movie.region).build();
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

    public void setFavorite(MovieWrapper movieWrapper, Callback2 callback2) {
        Observable.just(movieWrapper)
                .map(new Function<MovieWrapper, Boolean>() {
                    @Override
                    public Boolean apply(MovieWrapper wrapper) throws Throwable {
                        boolean isFavorite = !wrapper.movie.isFavorite;
                        long id = wrapper.movie.id;
                        mMovieDao.updateFavorite(isFavorite, id);
                        boolean is_favorite = mMovieDao.queryFavorite(id);
                        return is_favorite;
                    }
                })
                .subscribeOn(Schedulers.from(mSingleThreadPool))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(isFavorite -> {
                    if (callback2 != null)
                        callback2.runOnUIThread(isFavorite);
                });
    }

    public void playingVideo(String path, String name) {
        getApplication().playingMovie(path,name)
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
                    long id = mMovieWrapper.movie.id;
                    String movie_id = mMovieWrapper.movie.movieId;
                    mMovieVideofileCrossRefDao.deleteById(id);
                    mMovieDao.updateFavorite(false, id);//电影的收藏状态在删除时要设置为false
                    return movie_id;
                })
                .observeOn(AndroidSchedulers.mainThread());

    }

    public void selectMovie(String movie_id, String source, Constants.SearchType type, boolean is_favorite, MovieWrapperCallback movieWrapperCallback) {

        Observable.just(source)
                .map(new Function<String, MovieWrapper>() {
                    @Override
                    public MovieWrapper apply(String _source) throws Throwable {
                        //TODO 增加可选搜索类型
                        MovieWrapper wrapper = TmdbApiService.getDetials(movie_id, _source, type.name()).subscribeOn(Schedulers.io()).blockingFirst().toEntity();
                        return wrapper;
                    }
                })
                .onErrorReturn(throwable -> {
                    throwable.printStackTrace();
                    return new MovieWrapper();
                })
                .doOnNext(wrapper -> {
                    if (wrapper.movie == null)
                        return;
                    wrapper.movie.isFavorite = is_favorite;
                    String[] paths;
                    if (mCurrentMode == Constants.MovieDetailMode.MODE_WRAPPER) {
                        paths = new String[mMovieWrapper.videoFiles.size()];
                        for (int i = 0; i < paths.length; i++) {
                            paths[i] = mMovieWrapper.videoFiles.get(i).path;
                        }
                    } else {
                        paths = new String[mUnrecognizedFileDataViewList.size()];
                        for (int i = 0; i < paths.length; i++) {
                            paths[i] = mUnrecognizedFileDataViewList.get(i).path;
                        }
                    }
                    List<VideoFile> videoFiles = mVideoFileDao.queryByPaths(paths);
                    wrapper.videoFiles = videoFiles;
                    saveMovieWrapper(wrapper, videoFiles, source);
                }).subscribeOn(Schedulers.from(mSingleThreadPool))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<MovieWrapper>() {
                    @Override
                    public void onAction(MovieWrapper movieWrapper) {
                        if (movieWrapperCallback != null)
                            movieWrapperCallback.runOnUIThread(movieWrapper);
                    }
                });
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


    /**
     * 保存MovieWrapper实体
     *
     * @param movieWrapper
     */
    private void saveMovieWrapper(MovieWrapper movieWrapper, List<VideoFile> videoFileList, String source) {
        //获取各个实体类
        Movie movie = movieWrapper.movie;
        List<Genre> genreList = movieWrapper.genres;
        List<Director> directorList = movieWrapper.directors;
        List<Actor> actorList = movieWrapper.actors;
        List<Trailer> trailerList = movieWrapper.trailers;
        List<StagePhoto> stagePhotoList = movieWrapper.stagePhotos;
        List<Season> seasonList = movieWrapper.seasons;

        //插入电影到数据库
        movie.pinyin = PinyinParseAndMatchTools.parsePinyin(movie.title);
        movie.addTime = System.currentTimeMillis();
        mMovieDao.insertOrIgnoreMovie(movie);
        mGenreDao.insertGenres(genreList);
        mActorDao.insertActors(actorList);
        mDirectorDao.insertDirectors(directorList);

        List<String> querySelectionGenreNames = new ArrayList<>();
        for (Genre genre : genreList) {
            querySelectionGenreNames.add(genre.name);
        }

        //查询影片ID
        long movie_id = mMovieDao.queryByMovieId(movie.movieId, source).id;
        long[] genre_ids = mGenreDao.queryByName(querySelectionGenreNames);

        movieWrapper.movie.id = movie_id;

        for (long genre_id : genre_ids) {
            if (genre_id != -1) {
                MovieGenreCrossRef movieGenreCrossRef = new MovieGenreCrossRef();
                movieGenreCrossRef.genreId = genre_id;
                movieGenreCrossRef.id = movie_id;
                mMovieGenreCrossRefDao.insertMovieGenreCrossRef(movieGenreCrossRef);
            }
        }

        for (Actor actor : actorList) {
            if (actor != null) {
                MovieActorCrossRef movieActorCrossRef = new MovieActorCrossRef();
                movieActorCrossRef.actorId = actor.actorId;
                movieActorCrossRef.id = movie_id;
                mMovieActorCrossRefDao.insertMovieActorCrossRef(movieActorCrossRef);
            }
        }

        for (Director director : directorList) {
            if (director != null) {
                MovieDirectorCrossRef movieDirectorCrossRef = new MovieDirectorCrossRef();
                movieDirectorCrossRef.directorId = director.director_id;
                movieDirectorCrossRef.id = movie_id;
                mMovieDirectorCrossRefDao.insertMovieDirectorCrossRef(movieDirectorCrossRef);
            }
        }

        for (Trailer trailer : trailerList) {
            if (trailer != null) {
                trailer.movieId = movie_id;
                mTrailerDao.insertOrIgnore(trailer);
            }
        }

        for (StagePhoto stagePhoto : stagePhotoList) {
            if (stagePhoto != null) {
                stagePhoto.movieId = movie_id;
                mStagePhotoDao.insertOrIgnore(stagePhoto);
            }
        }

        for (Season season : seasonList) {
            if (season != null) {
                season.movieId = movie_id;
                mSeasonDao.insertOrIgnore(season);
            }
        }

        for (VideoFile videoFile : videoFileList) {
            MovieVideoFileCrossRef movieVideoFileCrossRef = new MovieVideoFileCrossRef();
            movieVideoFileCrossRef.id = movie_id;
            movieVideoFileCrossRef.path = videoFile.path;
            movieVideoFileCrossRef.source = source;
            mMovieVideofileCrossRefDao.insertOrReplace(movieVideoFileCrossRef);
            videoFile.isScanned = 1;
            videoFile.keyword = movie.title;
            mVideoFileDao.update(videoFile);
        }

    }

    public MovieWrapper getMovieWrapper() {
        return mMovieWrapper;
    }

    public void setCurrentMode(int currentMode) {
        mCurrentMode = currentMode;
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
