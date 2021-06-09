package com.hphtv.movielibrary.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.firefly.videonameparser.MovieNameInfo;
import com.firelfy.util.EditorDistance;
import com.firelfy.util.LogUtil;
import com.hphtv.movielibrary.data.ConstData;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.ActorDao;
import com.hphtv.movielibrary.roomdb.dao.DirectorDao;
import com.hphtv.movielibrary.roomdb.dao.GenreDao;
import com.hphtv.movielibrary.roomdb.dao.MovieActorCrossRefDao;
import com.hphtv.movielibrary.roomdb.dao.MovieDao;
import com.hphtv.movielibrary.roomdb.dao.MovieDirectorCrossRefDao;
import com.hphtv.movielibrary.roomdb.dao.MovieGenreCrossRefDao;
import com.hphtv.movielibrary.roomdb.dao.MovieVideofileCrossRefDao;
import com.hphtv.movielibrary.roomdb.dao.VideoFileDao;
import com.hphtv.movielibrary.roomdb.entity.Actor;
import com.hphtv.movielibrary.roomdb.entity.Director;
import com.hphtv.movielibrary.roomdb.entity.Genre;
import com.hphtv.movielibrary.roomdb.entity.Movie;
import com.hphtv.movielibrary.roomdb.entity.MovieActorCrossRef;
import com.hphtv.movielibrary.roomdb.entity.MovieDirectorCrossRef;
import com.hphtv.movielibrary.roomdb.entity.MovieGenreCrossRef;
import com.hphtv.movielibrary.roomdb.entity.MovieVideoFileCrossRef;
import com.hphtv.movielibrary.roomdb.entity.MovieWrapper;
import com.hphtv.movielibrary.roomdb.entity.VideoFile;
import com.hphtv.movielibrary.scraper.mtime.MtimeApi2;
import com.hphtv.movielibrary.util.FileScanUtil;
import com.hphtv.movielibrary.util.retrofit.MtimeSearchRespone;
import com.hphtv.movielibrary.util.rxjava.RxJavaGcManager;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.functions.Supplier;
import io.reactivex.rxjava3.observers.DisposableObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * author: Sam Leung
 * date:  2021/5/26
 */
public class MovieScanService2 extends Service {
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
    private boolean isScanning;
    private int count;
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
    }

    private void initThreadPools() {
        mSearchMovieExecutor = new ThreadPoolExecutor(4, 4, 0, TimeUnit.SECONDS, new LinkedBlockingDeque<>());
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
        public MovieScanService2 getService() {
            return MovieScanService2.this;
        }
    }

    public boolean start(VideoFile... videoFiles) {
        if (isScanning)
            return false;
        synchronized (this) {
            if (isScanning)
                return false;
            isScanning = true;
            count=videoFiles.length;
            offset=0;
            for (VideoFile videoFile : videoFiles) {
                startSearch(videoFile);
            }
            return true;
        }
    }

    public boolean start(List<VideoFile> videoFileList) {
        if (isScanning)
            return false;
        synchronized (this) {
            if (isScanning)
                return false;
            isScanning = true;
            count=videoFileList.size();
            offset=0;
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
//        String mediaTitle = null;
//        if (!TextUtils.isEmpty(path)) {
//            try {
//                MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
//                mediaMetadataRetriever.setDataSource(path);
//                mediaTitle = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        String raw = TextUtils.isEmpty(mediaTitle) ? path : mediaTitle;
        String raw = path;
        return FileScanUtil.simpleParse(raw);
    }

    private void startSearch(VideoFile videoFile) {
        MovieSupplier movieSupplier = new MovieSupplier(videoFile);
        RxJavaGcManager.getInstance().addDisposable(Observable.defer(movieSupplier)
                .subscribeOn(Schedulers.from(mSearchMovieExecutor))
                .observeOn(Schedulers.from(mMovieDetailExecutor))
                .flatMap((Function<MovieNameInfo, ObservableSource<MtimeSearchRespone>>) movieNameInfo -> {
                    String name = movieNameInfo.getName();
                    if (!TextUtils.isEmpty(name))
                        return MtimeApi2.SearchAMovieByApi(name);
                    return null;
                }).observeOn(Schedulers.from(mMovieDetailExecutor))
                .map((Function<MtimeSearchRespone, String>) mtimeSearchRespone -> {
                    String name = movieSupplier.keyword;
                    if (mtimeSearchRespone != null) {
                        List<Movie> movies = mtimeSearchRespone.toEntity();
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
                        return mostSimilarMovie.movieId;
                    }
                    return null;
                })
                .observeOn(Schedulers.io())
                .map((Function<String, MovieWrapper>) movieId -> {

                    MovieWrapper wrapper = MtimeApi2.getMovieDetail(movieId).subscribeOn(Schedulers.io()).blockingFirst().toEntity();
                    saveMovieWrapper(wrapper, videoFile);

                    return wrapper;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<MovieWrapper>() {
                    @Override
                    public void onNext(@NonNull MovieWrapper movieWrapper) {
                        LogUtil.v("onNext " + Thread.currentThread().getName());
                        LogUtil.v("Result:origin" + videoFile.filename);
                        LogUtil.v("Result:==>" + movieWrapper.movie.title);
                        offset++;
                        //TODO 发送广播
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        if(offset==count){
                            //扫描结束
                            Intent intent=new Intent();
                            intent.setAction(ConstData.BroadCastMsg.MOVIE_SCRAP_FINISH);
                            LocalBroadcastManager.getInstance(MovieScanService2.this).sendBroadcast(intent);
                        }
                    }
                })
        );
    }

    /**
     * 保存MovieWrapper实体
     *
     * @param movieWrapper
     */
    private void saveMovieWrapper(MovieWrapper movieWrapper, VideoFile videoFile) {
        //获取各个实体类
        Movie movie = movieWrapper.movie;
        List<Genre> genreList = movieWrapper.genres;
        Director director = movieWrapper.director;
        List<Actor> actorList = movieWrapper.actors;
        //插入电影到数据库
        mMovieDao.insertOrIgnoreMovie(movie);
        //查询影片ID
        long movie_id = mMovieDao.queryByMovieId(movie.movieId).id;
        long[] genre_ids = mGenreDao.insertGenres(genreList);
        long[] actor_ids = mActorDao.insertActors(actorList);
        long director_id = mDirectorDao.insertDirector(director);

        movieWrapper.movie.id = movie_id;

        for (long genre_id : genre_ids) {
            if (genre_id != -1) {
                MovieGenreCrossRef movieGenreCrossRef = new MovieGenreCrossRef();
                movieGenreCrossRef.genreId = genre_id;
                movieGenreCrossRef.id = movie_id;
                mMovieGenreCrossRefDao.insertMovieGenreCrossRef(movieGenreCrossRef);
            }
        }

        for (long actor_id : actor_ids) {
            if (actor_id != -1) {
                MovieActorCrossRef movieActorCrossRef = new MovieActorCrossRef();
                movieActorCrossRef.actorId = actor_id;
                movieActorCrossRef.id = movie_id;
                mMovieActorCrossRefDao.insertMovieActorCrossRef(movieActorCrossRef);
            }
        }

        if (director_id != -1) {
            MovieDirectorCrossRef movieDirectorCrossRef = new MovieDirectorCrossRef();
            movieDirectorCrossRef.directorId = director_id;
            movieDirectorCrossRef.id = movie_id;
            mMovieDirectorCrossRefDao.insertMovieDirectorCrossRef(movieDirectorCrossRef);
        }

        MovieVideoFileCrossRef movieVideoFileCrossRef = new MovieVideoFileCrossRef();
        movieVideoFileCrossRef.id = movie_id;
        movieVideoFileCrossRef.path = videoFile.path;
        mMovieVideofileCrossRefDao.insertMovieVideofileCrossRef(movieVideoFileCrossRef);

        videoFile.isScanned = 1;
        LogUtil.v("videofile " + videoFile.filename + " isScanned ");
        mVideoFileDao.update(videoFile);

    }

    public class MovieSupplier implements Supplier<Observable<MovieNameInfo>> {
        VideoFile mVideoFile;
        public String keyword;

        public MovieSupplier(VideoFile videoFile) {
            mVideoFile = videoFile;
            LogUtil.v("VideoFile path=" + mVideoFile.path);
        }

        @Override
        public Observable<MovieNameInfo> get() throws Throwable {
            MovieNameInfo movieNameInfo = createMovieNameInfo(mVideoFile);
            keyword = movieNameInfo.getName();
            LogUtil.v("MovieSupplier " + keyword);
            return Observable.just(movieNameInfo);
        }
    }

}
