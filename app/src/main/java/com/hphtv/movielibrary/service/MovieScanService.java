package com.hphtv.movielibrary.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.SparseArray;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.firefly.videonameparser.MovieNameInfo;
import com.hphtv.movielibrary.scraper.api.tmdb.TmdbApiService;
import com.hphtv.movielibrary.scraper.respone.MovieDetailRespone;
import com.hphtv.movielibrary.scraper.respone.MovieSearchRespone;
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
import com.hphtv.movielibrary.util.FileScanUtil;
import com.hphtv.movielibrary.util.PinyinParseAndMatchTools;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;
import com.station.kit.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.BiFunction;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.functions.Supplier;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * author: Sam Leung
 * date:  2021/5/26
 */
public class MovieScanService extends Service {
    public static final String TAG = MovieScanService.class.getSimpleName();
    public static final int KEY_MOVIE_ID = 0;
    public static final int KEY_VIDEOFILE = 1;
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

    private ConcurrentLinkedQueue<VideoFile> mQueue;
    private ConcurrentLinkedQueue<VideoFile> mScannedQueue;

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
        mQueue = new ConcurrentLinkedQueue<>();
        mScannedQueue = new ConcurrentLinkedQueue<>();
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

    /**
     * 将videofile加入匹配队列
     *
     * @param videoFileList
     * @return
     */
    public void addToPairingQueue(List<VideoFile> videoFileList) {
        for (VideoFile videoFile : videoFileList) {
            if (!mQueue.contains(videoFile)) {
                mQueue.add(videoFile);
            }
        }
        if (!isScanning)
            startSearch();
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

    private void startSearch() {
        startTmdbSearch()
                .map(sparseArray -> {
                    String movie_id = null;
                    if (sparseArray.get(KEY_MOVIE_ID) != null) {
                        movie_id = (String) sparseArray.get(KEY_MOVIE_ID);
                        VideoFile videoFile = (VideoFile) sparseArray.get(KEY_VIDEOFILE);
                        LogUtil.w(TAG, "获取" + movie_id + "详情");
                        MovieDetailRespone respone = TmdbApiService.getDetials(movie_id, Constants.Scraper.TMDB)
                                .onErrorReturn(throwable -> {
                                    LogUtil.w(TAG, "TMDB Detail " + throwable.getMessage());
                                    return null;
                                })
                                .subscribeOn(Schedulers.io()).blockingFirst();
                        if (respone != null) {
                            MovieWrapper wrapper = respone.toEntity();
                            if (wrapper != null) {
                                saveMovieWrapper(wrapper, videoFile, Constants.Scraper.TMDB);
                            }
                        }
                        MovieDetailRespone respone_en = TmdbApiService.getDetials(movie_id, Constants.Scraper.TMDB_EN)
                                .onErrorReturn(throwable -> {
                                    LogUtil.w(TAG, "TMDB_EN Detail " + throwable.getMessage());
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
                    LogUtil.e(TAG, "TMDB throw");
                    throwable.printStackTrace();
                    return "";
                })
                .subscribeOn(Schedulers.from(mMovieDetailExecutor))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        super.onSubscribe(d);
                        Intent intent = new Intent();
                        intent.setAction(Constants.BroadCastMsg.MOVIE_SCRAP_START);
                        LocalBroadcastManager.getInstance(MovieScanService.this).sendBroadcast(intent);
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                        LogUtil.v(TAG, "onComplete");
                        //扫描结束
                        isScanning = false;
                        Intent intent = new Intent();
                        intent.setAction(Constants.BroadCastMsg.MOVIE_SCRAP_FINISH);
                        LocalBroadcastManager.getInstance(MovieScanService.this).sendBroadcast(intent);
                        offset = 0;
                        mScannedQueue.clear();
                    }

                    @Override
                    public void onAction(String movie_id) {
                        offset++;
                        Intent intent = new Intent();
                        intent.setAction(Constants.BroadCastMsg.MATCHED_MOVIE);
                        intent.putExtra(Constants.Extras.MOVIE_ID, movie_id);
                        LocalBroadcastManager.getInstance(MovieScanService.this).sendBroadcast(intent);
                        LogUtil.w(TAG, "onAction offset " + offset);
                    }
                });
    }

//    private Observable<String> getMtimeSearchResult(MovieSupplier movieSupplier) {
//        return Observable.zip(
//                //unionSearch请求
//                Observable
//                        .defer(movieSupplier)
//                        .subscribeOn(Schedulers.from(mSearchMovieExecutor))
//                        .flatMap((Function<MovieNameInfo, ObservableSource<MovieSearchRespone>>) movieNameInfo -> {
//                            LogUtil.v(Thread.currentThread().getName(), "1 ...");
//
//                            String name = movieNameInfo.getName();
//                            if (!TextUtils.isEmpty(name))
//                                return MtimeApiService.unionSearch(name).observeOn(Schedulers.io());
//                            return null;
//                        }),
//                //suggest movie 请求
//                Observable
//                        .defer(movieSupplier)
//                        .subscribeOn(Schedulers.from(mSearchMovieExecutor))
//                        .flatMap((Function<MovieNameInfo, ObservableSource<MovieSearchRespone>>) movieNameInfo -> {
//                            LogUtil.v(Thread.currentThread().getName(), "2 ...");
//
//                            String name = movieNameInfo.getName();
//                            if (!TextUtils.isEmpty(name))
//                                return MtimeApiService.suggestMovies(name).observeOn(Schedulers.io());
//                            return null;
//                        }),
//                //获取最优匹配
//                (unionSearchRespone, unionSuggestRespone) -> {
//                    LogUtil.v(Thread.currentThread().getName(), "MTIME 获取最优匹配 ...");
//                    String name = movieSupplier.keyword;
//                    List<Movie> movies = new ArrayList<>();
//                    if (unionSearchRespone != null) {
//                        List<Movie> unionSearchMovies = unionSearchRespone.toEntity();
//                        movies.addAll(unionSearchMovies);
//                    }
//                    if (unionSuggestRespone != null) {
//                        List<Movie> suggestMovies = unionSuggestRespone.toEntity();
//                        movies.addAll(suggestMovies);
//                    }
//
//                    Movie mostSimilarMovie = null;
//                    float maxSimilarity = 0;
//                    for (Movie movie : movies) {
//                        float similarity = EditorDistance.checkLevenshtein(movie.title, name);
//                        float similarityEn = EditorDistance.checkLevenshtein(movie.otherTitle, name);
//                        float tmpSimilarity = Math.max(similarity, similarityEn);
//                        if (tmpSimilarity == 1) {
//                            mostSimilarMovie = movie;
//                            break;
//                        }
//                        if (tmpSimilarity > maxSimilarity || mostSimilarMovie == null) {
//                            mostSimilarMovie = movie;
//                            maxSimilarity = tmpSimilarity;
//                        }
//                    }
//                    if (mostSimilarMovie != null)
//                        return mostSimilarMovie.movieId;
//                    else
//                        return "";
//                })
//                .onErrorReturn(throwable -> {
//                    LogUtil.e("onErrorReturn MTIME-> " + throwable.getMessage());
//                    return "";
//                });
//    }
//
//    private Observable<String> getOmdbSearchResult(MovieSupplier movieSupplier) {
//        return Observable.defer(movieSupplier)
//                .subscribeOn(Schedulers.from(mSearchMovieExecutor))
//                .flatMap((Function<MovieNameInfo, ObservableSource<MovieSearchRespone>>) movieNameInfo -> {
//                    LogUtil.v(Thread.currentThread().getName(), "1 ...");
//
//                    String name = movieNameInfo.getName();
//                    if (!TextUtils.isEmpty(name))
//                        return OmdbApiService.unionSearch(name).observeOn(Schedulers.io());
//                    return null;
//                })
//                .map(movieSearchRespone -> {
//                    LogUtil.v(Thread.currentThread().getName(), "OMDB 获取最优匹配 ...");
//                    String name = movieSupplier.keyword;
//                    List<Movie> movies = new ArrayList<>();
//                    if (movieSearchRespone != null) {
//                        List<Movie> unionSearchMovies = movieSearchRespone.toEntity();
//                        movies.addAll(unionSearchMovies);
//                    }
//                    Movie mostSimilarMovie = null;
//                    float maxSimilarity = 0;
//                    for (Movie movie : movies) {
//                        float similarity = EditorDistance.checkLevenshtein(movie.title, name);
//                        float similarityEn = EditorDistance.checkLevenshtein(movie.otherTitle, name);
//                        float tmpSimilarity = Math.max(similarity, similarityEn);
//                        if (tmpSimilarity == 1) {
//                            mostSimilarMovie = movie;
//                            break;
//                        }
//                        if (tmpSimilarity > maxSimilarity || mostSimilarMovie == null) {
//                            mostSimilarMovie = movie;
//                            maxSimilarity = tmpSimilarity;
//                        }
//                    }
//                    if (mostSimilarMovie != null)
//                        return mostSimilarMovie.movieId;
//                    else
//                        return "";
//                })
//                .onErrorReturn(throwable -> {
//                    LogUtil.e("onErrorReturn OMDB-> " + throwable.getMessage());
//                    return "";
//                });
//    }

    private Observable<SparseArray<Object>> startTmdbSearch() {
        return Observable.create((ObservableOnSubscribe<VideoFile>) emitter -> {
            isScanning = true;
            while (mQueue.size() > 0) {
                VideoFile videoFile = mQueue.poll();
                if (videoFile != null && !mScannedQueue.contains(videoFile)) {
                    mScannedQueue.add(videoFile);
                    emitter.onNext(videoFile);
                }
            }
            emitter.onComplete();
        }).subscribeOn(Schedulers.from(mSearchMovieExecutor))
                .flatMap((Function<VideoFile, ObservableSource<SparseArray<Object>>>) videoFile -> {
                    LogUtil.v(Thread.currentThread().getName(), "1 ...");
                    MovieNameInfo movieNameInfo = createMovieNameInfo(videoFile);
                    String keyword = movieNameInfo.getName();
                    videoFile.keyword = keyword;
                    mVideoFileDao.update(videoFile);
                    String api = Constants.Scraper.TMDB_EN;
                    if (StringUtils.isGB2312(keyword)) {
                        api = Constants.Scraper.TMDB;
                    }
                    if (!TextUtils.isEmpty(keyword)) {
                        String finalApi = api;
                        return Observable.combineLatest(TmdbApiService.unionSearch(keyword, api).observeOn(Schedulers.io()),
                                Observable.just(keyword),
                                (movieSearchRespone, _keyword) -> {
                                    LogUtil.v(Thread.currentThread().getName(), "从" + finalApi + "结果列表获取最优匹配 ...");
                                    List<Movie> movies = new ArrayList<>();
                                    if (movieSearchRespone != null) {
                                        List<Movie> unionSearchMovies = movieSearchRespone.toEntity();
                                        movies.addAll(unionSearchMovies);
                                    }
                                    Movie mostSimilarMovie = null;
                                    float maxSimilarity = 0;
                                    for (Movie movie : movies) {
                                        float similarity = EditorDistance.checkLevenshtein(movie.title, _keyword);
                                        float similarityEn = EditorDistance.checkLevenshtein(movie.otherTitle, _keyword);
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

                                    SparseArray<Object> sparseArray = new SparseArray<>(2);
                                    if (mostSimilarMovie != null) {
                                        sparseArray.append(KEY_MOVIE_ID, mostSimilarMovie.movieId);
                                        sparseArray.append(KEY_VIDEOFILE, videoFile);
                                    }
                                    return sparseArray;
                                });
                    }
                    return null;
                })
                .onErrorReturn(throwable -> {
                    LogUtil.e(TAG, "onErrorReturn TMDB");
                    throwable.printStackTrace();
                    return new SparseArray<>(2);
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
        LogUtil.v(TAG, movie.title + " " + movie.source + " insertrelut:" + id);
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

        videoFile.isScanned = 1;
        mVideoFileDao.update(videoFile);

        LogUtil.v(TAG, "saveMovieWrapper==>: " + videoFile.filename + " isScanned ");

    }

}
