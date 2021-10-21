package com.hphtv.movielibrary.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.firefly.videonameparser.MovieNameInfo;
import com.hphtv.movielibrary.scraper.api.omdb.OmdbApiService;
import com.hphtv.movielibrary.scraper.api.tmdb.TmdbApiService;
import com.hphtv.movielibrary.scraper.respone.MovieDetailRespone;
import com.hphtv.movielibrary.scraper.respone.MovieSearchRespone;
import com.hphtv.movielibrary.util.ScraperSourceTools;
import com.station.kit.util.EditorDistance;
import com.station.kit.util.LogUtil;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.ActorDao;
import com.hphtv.movielibrary.roomdb.dao.DirectorDao;
import com.hphtv.movielibrary.roomdb.dao.GenreDao;
import com.hphtv.movielibrary.roomdb.dao.MovieActorCrossRefDao;
import com.hphtv.movielibrary.roomdb.dao.MovieDao;
import com.hphtv.movielibrary.roomdb.dao.MovieDirectorCrossRefDao;
import com.hphtv.movielibrary.roomdb.dao.MovieGenreCrossRefDao;
import com.hphtv.movielibrary.roomdb.dao.MovieVideofileCrossRefDao;
import com.hphtv.movielibrary.roomdb.dao.StagePhotoDao;
import com.hphtv.movielibrary.roomdb.dao.TrailerDao;
import com.hphtv.movielibrary.roomdb.dao.VideoFileDao;
import com.hphtv.movielibrary.roomdb.entity.Actor;
import com.hphtv.movielibrary.roomdb.entity.Director;
import com.hphtv.movielibrary.roomdb.entity.Genre;
import com.hphtv.movielibrary.roomdb.entity.Movie;
import com.hphtv.movielibrary.roomdb.entity.reference.MovieActorCrossRef;
import com.hphtv.movielibrary.roomdb.entity.reference.MovieDirectorCrossRef;
import com.hphtv.movielibrary.roomdb.entity.reference.MovieGenreCrossRef;
import com.hphtv.movielibrary.roomdb.entity.reference.MovieVideoFileCrossRef;
import com.hphtv.movielibrary.roomdb.entity.relation.MovieWrapper;
import com.hphtv.movielibrary.roomdb.entity.StagePhoto;
import com.hphtv.movielibrary.roomdb.entity.Trailer;
import com.hphtv.movielibrary.roomdb.entity.VideoFile;
import com.hphtv.movielibrary.scraper.api.mtime.MtimeApiService;
import com.hphtv.movielibrary.util.FileScanUtil;
import com.hphtv.movielibrary.util.PinyinParseAndMatchTools;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;
import com.station.kit.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.functions.BiFunction;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.functions.Supplier;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * author: Sam Leung
 * date:  2021/5/26
 */
public class MovieScanService extends Service {
    private ScanBinder mScanBinder;

    private ExecutorService mSearchMovieExecutor;
    private ExecutorService mMovieDetailExecutor;

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
    private boolean isScanning;

    private int total;
    private int offset;

    @Override
    public void onCreate() {
        super.onCreate();
        init();
        initDao();
        initThreadPools();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void init() {
    }

    /**
     * 初始化Dao类
     */
    private void initDao() {
        MovieLibraryRoomDatabase movieLibraryRoomDatabase = MovieLibraryRoomDatabase.getDatabase(this);
        mMovieDao = movieLibraryRoomDatabase.getMovieDao();
        mActorDao = movieLibraryRoomDatabase.getActorDao();
        mDirectorDao = movieLibraryRoomDatabase.getDirectorDao();
        mGenreDao = movieLibraryRoomDatabase.getGenreDao();
        mMovieActorCrossRefDao = movieLibraryRoomDatabase.getMovieActorCrossRefDao();
        mMovieDirectorCrossRefDao = movieLibraryRoomDatabase.getMovieDirectorCrossRefDao();
        mMovieGenreCrossRefDao = movieLibraryRoomDatabase.getMovieGenreCrossRefDao();
        mMovieVideofileCrossRefDao = movieLibraryRoomDatabase.getMovieVideofileCrossRefDao();
        mVideoFileDao = movieLibraryRoomDatabase.getVideoFileDao();
        mTrailerDao = movieLibraryRoomDatabase.getTrailerDao();
        mStagePhotoDao = movieLibraryRoomDatabase.getStagePhotoDao();
    }

    private void initThreadPools() {
        mSearchMovieExecutor = new ThreadPoolExecutor(8, 8, 0, TimeUnit.SECONDS, new LinkedBlockingDeque<>());
        mMovieDetailExecutor = new ThreadPoolExecutor(4, 4, 0, TimeUnit.SECONDS, new LinkedBlockingDeque<>());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if (mScanBinder == null)
            mScanBinder = new ScanBinder();
        return mScanBinder;
    }

    public class ScanBinder extends Binder {
        public MovieScanService getService() {
            return MovieScanService.this;
        }
    }

    public boolean start(VideoFile... videoFiles) {
        synchronized (this) {
            if (!isScanning) {
                total = videoFiles.length;
                offset = 0;
            } else {
                total += videoFiles.length;
            }
            isScanning = true;
            for (VideoFile videoFile : videoFiles) {
                startSearch(videoFile);
            }
            return true;
        }
    }

    public boolean start(List<VideoFile> videoFileList) {
        synchronized (this) {
            if (!isScanning) {
                total = videoFileList.size();
                offset = 0;
            } else {
                total += videoFileList.size();
            }
            isScanning = true;
            for (VideoFile videoFile : videoFileList) {
                startSearch(videoFile);
            }
            return true;
        }
    }

    /**
     * 提取关键字
     *
     * @param videoFile
     * @return
     */
    private MovieNameInfo createMovieNameInfo(VideoFile videoFile) {
        String path = videoFile.path;
        String raw = path;
        return FileScanUtil.simpleParse(raw);
    }

    private void startSearch(VideoFile videoFile) {
        MovieSupplier movieSupplier = new MovieSupplier(videoFile);

        getTmdbSearchResult(movieSupplier)
                .map(movie_id -> {
                    if (!TextUtils.isEmpty(movie_id)) {
                        MovieDetailRespone respone = TmdbApiService.getDetials(movie_id, Constants.Scraper.TMDB)
                                .onErrorReturn(throwable -> {
                                    LogUtil.e("TMDB Detail " + throwable.getMessage());
                                    return null;
                                })
                                .subscribeOn(Schedulers.io()).blockingFirst();
                        if (respone != null) {
                            MovieWrapper wrapper = respone.toEntity();
                            if (wrapper != null) {
                                saveMovieWrapper(wrapper, videoFile, Constants.Scraper.TMDB);
                            }
                        }

                    }
                    return movie_id;
                })
                .map(movie_id -> {
                    if (!TextUtils.isEmpty(movie_id)) {
                        MovieDetailRespone respone_en = TmdbApiService.getDetials(movie_id, Constants.Scraper.TMDB_EN)
                                .onErrorReturn(throwable -> {
                                    LogUtil.e("TMDB_EN Detail " + throwable.getMessage());
                                    return null;
                                })
                                .subscribeOn(Schedulers.io()).blockingFirst();
                        if (respone_en != null) {
                            MovieWrapper wrapper = respone_en.toEntity();
                            if (wrapper != null) {
                                saveMovieWrapper(wrapper, videoFile, Constants.Scraper.TMDB_EN);
                            }
                        }
                    }
                    return movie_id;
                })
                .onErrorReturn(throwable -> {
                    LogUtil.e("TMDB throw");
                    throwable.printStackTrace();
                    return "";
                })
                .subscribeOn(Schedulers.from(mMovieDetailExecutor))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new SimpleObserver<String>() {

                    @Override
                    public void onComplete() {
                        super.onComplete();
                        LogUtil.v("onComplete");
                        if (offset == total) {
                            synchronized (MovieScanService.this) {
                                if (offset == total) {
                                    //扫描结束
                                    isScanning = false;
                                    Intent intent = new Intent();
                                    intent.setAction(Constants.BroadCastMsg.MOVIE_SCRAP_FINISH);
                                    LocalBroadcastManager.getInstance(MovieScanService.this).sendBroadcast(intent);
                                }
                            }
                        }
                    }

                    @Override
                    public void onAction(String movie_id) {
                        offset++;
                        LogUtil.v("onAction offset " + offset);
                    }
                });

    }

    private Observable<String> getMtimeSearchResult(MovieSupplier movieSupplier) {
        return Observable.zip(
                //unionSearch请求
                Observable
                        .defer(movieSupplier)
                        .subscribeOn(Schedulers.from(mSearchMovieExecutor))
                        .flatMap((Function<MovieNameInfo, ObservableSource<MovieSearchRespone>>) movieNameInfo -> {
                            LogUtil.v(Thread.currentThread().getName(), "1 ...");

                            String name = movieNameInfo.getName();
                            if (!TextUtils.isEmpty(name))
                                return MtimeApiService.unionSearch(name).observeOn(Schedulers.io());
                            return null;
                        }),
                //suggest movie 请求
                Observable
                        .defer(movieSupplier)
                        .subscribeOn(Schedulers.from(mSearchMovieExecutor))
                        .flatMap((Function<MovieNameInfo, ObservableSource<MovieSearchRespone>>) movieNameInfo -> {
                            LogUtil.v(Thread.currentThread().getName(), "2 ...");

                            String name = movieNameInfo.getName();
                            if (!TextUtils.isEmpty(name))
                                return MtimeApiService.suggestMovies(name).observeOn(Schedulers.io());
                            return null;
                        }),
                //获取最优匹配
                (unionSearchRespone, unionSuggestRespone) -> {
                    LogUtil.v(Thread.currentThread().getName(), "MTIME 获取最优匹配 ...");
                    String name = movieSupplier.keyword;
                    List<Movie> movies = new ArrayList<>();
                    if (unionSearchRespone != null) {
                        List<Movie> unionSearchMovies = unionSearchRespone.toEntity();
                        movies.addAll(unionSearchMovies);
                    }
                    if (unionSuggestRespone != null) {
                        List<Movie> suggestMovies = unionSuggestRespone.toEntity();
                        movies.addAll(suggestMovies);
                    }

                    Movie mostSimilarMovie = null;
                    float maxSimilarity = 0;
                    for (Movie movie : movies) {
                        float similarity = EditorDistance.checkLevenshtein(movie.title, name);
                        float similarityEn = EditorDistance.checkLevenshtein(movie.otherTitle, name);
                        float tmpSimilarity = Math.max(similarity, similarityEn);
                        if (tmpSimilarity == 1) {
                            mostSimilarMovie = movie;
                            break;
                        }
                        if (tmpSimilarity > maxSimilarity || mostSimilarMovie == null) {
                            mostSimilarMovie = movie;
                            maxSimilarity = tmpSimilarity;
                        }
                    }
                    if (mostSimilarMovie != null)
                        return mostSimilarMovie.movieId;
                    else
                        return "";
                })
                .onErrorReturn(throwable -> {
                    LogUtil.e("onErrorReturn MTIME-> " + throwable.getMessage());
                    return "";
                });
    }

    private Observable<String> getOmdbSearchResult(MovieSupplier movieSupplier) {
        return Observable.defer(movieSupplier)
                .subscribeOn(Schedulers.from(mSearchMovieExecutor))
                .flatMap((Function<MovieNameInfo, ObservableSource<MovieSearchRespone>>) movieNameInfo -> {
                    LogUtil.v(Thread.currentThread().getName(), "1 ...");

                    String name = movieNameInfo.getName();
                    if (!TextUtils.isEmpty(name))
                        return OmdbApiService.unionSearch(name).observeOn(Schedulers.io());
                    return null;
                })
                .map(movieSearchRespone -> {
                    LogUtil.v(Thread.currentThread().getName(), "OMDB 获取最优匹配 ...");
                    String name = movieSupplier.keyword;
                    List<Movie> movies = new ArrayList<>();
                    if (movieSearchRespone != null) {
                        List<Movie> unionSearchMovies = movieSearchRespone.toEntity();
                        movies.addAll(unionSearchMovies);
                    }
                    Movie mostSimilarMovie = null;
                    float maxSimilarity = 0;
                    for (Movie movie : movies) {
                        float similarity = EditorDistance.checkLevenshtein(movie.title, name);
                        float similarityEn = EditorDistance.checkLevenshtein(movie.otherTitle, name);
                        float tmpSimilarity = Math.max(similarity, similarityEn);
                        if (tmpSimilarity == 1) {
                            mostSimilarMovie = movie;
                            break;
                        }
                        if (tmpSimilarity > maxSimilarity || mostSimilarMovie == null) {
                            mostSimilarMovie = movie;
                            maxSimilarity = tmpSimilarity;
                        }
                    }
                    if (mostSimilarMovie != null)
                        return mostSimilarMovie.movieId;
                    else
                        return "";
                })
                .onErrorReturn(throwable -> {
                    LogUtil.e("onErrorReturn OMDB-> " + throwable.getMessage());
                    return "";
                });
    }

    private Observable<String> getTmdbSearchResult(MovieSupplier movieSupplier) {

        return Observable.defer(movieSupplier)
                .subscribeOn(Schedulers.from(mSearchMovieExecutor))
                .flatMap((Function<MovieNameInfo, ObservableSource<MovieSearchRespone>>) movieNameInfo -> {
                    LogUtil.v(Thread.currentThread().getName(), "1 ...");

                    String name = movieNameInfo.getName();
                    String api = Constants.Scraper.TMDB_EN;
                    if (StringUtils.isGB2312(name)) {
                        api = Constants.Scraper.TMDB;
                    }
                    if (!TextUtils.isEmpty(name))
                        return TmdbApiService.unionSearch(name, api).observeOn(Schedulers.io());
                    return null;
                })
                .map(movieSearchRespone -> {
                    LogUtil.v(Thread.currentThread().getName(), "TMDB 获取最优匹配 ...");
                    String name = movieSupplier.keyword;
                    List<Movie> movies = new ArrayList<>();
                    if (movieSearchRespone != null) {
                        List<Movie> unionSearchMovies = movieSearchRespone.toEntity();
                        movies.addAll(unionSearchMovies);
                    }
                    Movie mostSimilarMovie = null;
                    float maxSimilarity = 0;
                    for (Movie movie : movies) {
                        float similarity = EditorDistance.checkLevenshtein(movie.title, name);
                        float similarityEn = EditorDistance.checkLevenshtein(movie.otherTitle, name);
                        float tmpSimilarity = Math.max(similarity, similarityEn);
                        if (tmpSimilarity == 1) {
                            mostSimilarMovie = movie;
                            break;
                        }
                        if (tmpSimilarity > maxSimilarity || mostSimilarMovie == null) {
                            mostSimilarMovie = movie;
                            maxSimilarity = tmpSimilarity;
                        }
                    }
                    if (mostSimilarMovie != null)
                        return mostSimilarMovie.movieId;
                    else
                        return "";
                })
                .onErrorReturn(throwable -> {
                    LogUtil.e("onErrorReturn OMDB-> " + throwable.getMessage());
                    return "";
                });
    }

    /**
     * 保存MovieWrapper实体
     *
     * @param movieWrapper
     */
    private void saveMovieWrapper(MovieWrapper movieWrapper, VideoFile videoFile, String source) {
        //获取各个实体类
        Movie movie = movieWrapper.movie;
        if (movie == null)
            return;
        List<Genre> genreList = movieWrapper.genres;
        List<Director> directorList = movieWrapper.directors;
        List<Actor> actorList = movieWrapper.actors;
        List<Trailer> trailerList = movieWrapper.trailers;
        List<StagePhoto> stagePhotoList = movieWrapper.stagePhotos;
        //插入电影到数据库
        movie.pinyin = PinyinParseAndMatchTools.parsePinyin(movie.title);
        movie.addTime = System.currentTimeMillis();
        long id = mMovieDao.insertOrIgnoreMovie(movie);
        LogUtil.v(movie.title + " " + movie.source + " insertrelut:" + id);
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


        MovieVideoFileCrossRef movieVideoFileCrossRef = new MovieVideoFileCrossRef();
        movieVideoFileCrossRef.id = movie_id;
        movieVideoFileCrossRef.path = videoFile.path;
        movieVideoFileCrossRef.source = source;
        mMovieVideofileCrossRefDao.insertOrReplace(movieVideoFileCrossRef);

        LogUtil.v("saveMovieWrapper==>: " + videoFile.filename + " isScanned ");

    }

    public class MovieSupplier implements Supplier<Observable<MovieNameInfo>> {
        VideoFile mVideoFile;
        public String keyword;

        public MovieSupplier(VideoFile videoFile) {
            mVideoFile = videoFile;
        }

        @Override
        public Observable<MovieNameInfo> get() throws Throwable {
            MovieNameInfo movieNameInfo = createMovieNameInfo(mVideoFile);
            keyword = movieNameInfo.getName();
            mVideoFile.keyword = keyword;
            mVideoFileDao.update(mVideoFile);
            LogUtil.v(Thread.currentThread().getName(), "MovieSupplier " + keyword);
            return Observable.just(movieNameInfo);
        }
    }

}
