package com.hphtv.movielibrary.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.firefly.videonameparser.MovieNameInfo;
import com.hphtv.movielibrary.roomdb.dao.MovieDao;
import com.hphtv.movielibrary.roomdb.dao.SeasonDao;
import com.hphtv.movielibrary.roomdb.dao.ShortcutDao;
import com.hphtv.movielibrary.roomdb.entity.Season;
import com.hphtv.movielibrary.roomdb.entity.Shortcut;
import com.hphtv.movielibrary.scraper.api.tmdb.TmdbApiService;
import com.hphtv.movielibrary.scraper.respone.MovieDetailRespone;
import com.hphtv.movielibrary.scraper.respone.MovieSearchRespone;
import com.hphtv.movielibrary.util.BroadcastHelper;
import com.hphtv.movielibrary.util.ScraperSourceTools;
import com.station.kit.util.EditorDistance;
import com.station.kit.util.LogUtil;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.ActorDao;
import com.hphtv.movielibrary.roomdb.dao.DirectorDao;
import com.hphtv.movielibrary.roomdb.dao.GenreDao;
import com.hphtv.movielibrary.roomdb.dao.MovieActorCrossRefDao;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.functions.Function3;
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
    private ExecutorService mNetworkExecutor;
    private ExecutorService mNetwork2Executor;

    private MovieDao mMovieDao;
    private ActorDao mActorDao;
    private DirectorDao mDirectorDao;
    private GenreDao mGenreDao;
    private ShortcutDao mShortcutDao;
    private MovieActorCrossRefDao mMovieActorCrossRefDao;
    private MovieDirectorCrossRefDao mMovieDirectorCrossRefDao;
    private MovieGenreCrossRefDao mMovieGenreCrossRefDao;
    private MovieVideofileCrossRefDao mMovieVideofileCrossRefDao;
    private VideoFileDao mVideoFileDao;
    private TrailerDao mTrailerDao;
    private StagePhotoDao mStagePhotoDao;
    private SeasonDao mSeasonDao;

    private HashSet<Shortcut> mShortcutHashSet=new HashSet<>();


    @Override
    public void onCreate() {
        super.onCreate();
        initDao();
        initThreadPools();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
        mShortcutDao = movieLibraryRoomDatabase.getShortcutDao();
        mSeasonDao = movieLibraryRoomDatabase.getSeasonDao();
    }

    private void initThreadPools() {
        mSearchMovieExecutor = new ThreadPoolExecutor(4, 8, 10, TimeUnit.SECONDS, new LinkedBlockingDeque<>());
        mNetworkExecutor = new ThreadPoolExecutor(4, 8, 10, TimeUnit.SECONDS, new LinkedBlockingDeque<>());
        mNetwork2Executor = new ThreadPoolExecutor(4, 8, 10, TimeUnit.SECONDS, new LinkedBlockingDeque<>());
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

    public void scanVideo(Shortcut shortcut, List<VideoFile> videoFileList) {
        startSearch(shortcut, videoFileList, shortcut.folderType);
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
        if (!videoFile.path.startsWith("/")) {
            raw = videoFile.filename;
        }
        return FileScanUtil.simpleParse(raw);
    }


    AtomicInteger mGlobalTaskCount = new AtomicInteger();//后台扫描标志

    /**
     * 扫描
     *
     * @param shortcut
     * @param videoFileList
     * @param searchType
     */
    private void startSearch(Shortcut shortcut, List<VideoFile> videoFileList, Constants.SearchType searchType) {
        Log.e(TAG, "startSearch: " + searchType.name() + "模式");
        //combineLatest 将前面Observable的最新数据与最后的Observable发送的每个数据结合
        HashSet<String> tmpMoviesSet=new HashSet<>();
        Object lock = new Object();
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger indexAtom = new AtomicInteger();
        AtomicInteger currentTaskCount = new AtomicInteger();
        int total = videoFileList.size();
        Observable.combineLatest(Observable.just(shortcut), Observable.just(searchType), Observable.just(videoFileList).concatMap((Function<List<VideoFile>, ObservableSource<List<VideoFile>>>) videoFileList1 -> {
            Log.e(Thread.currentThread().getName(), "combineLatest->分割videolist");
            //为了支持4线程搜索,将数据分成大于4份,并行数量取决于mNetworkExecutor
            //[1,2,3,4,11,12,13,14,21,22,23,24,31,32]
            //                  ↓
            //[1,11,21,31],[2,12,22,32],[3,13,23],[4,14,24]
            int thredCount = 4;
            List<List<VideoFile>> dataSet = new ArrayList<>();
            for (int i = 0; i < thredCount; i++) {
                dataSet.add(new ArrayList<>());
            }
            for (int i = 0; i < videoFileList1.size(); i++) {
                dataSet.get(i % thredCount).add(videoFileList1.get(i));
            }
            return Observable.fromIterable(dataSet);
        }),
                new Function3<Shortcut, Constants.SearchType, List<VideoFile>, Object[]>() {
                    @Override
                    public Object[] apply(Shortcut shortcut1, Constants.SearchType searchType1, List<VideoFile> videoFileList) throws Throwable {
                        Log.e(Thread.currentThread().getName(), "准备分发==>" + videoFileList);

                        Observable.fromIterable(videoFileList)
                                .observeOn(Schedulers.from(mNetworkExecutor))
                                .map(videoFile -> {
                                    LogUtil.v(Thread.currentThread().getName(), "shortcut[" + shortcut1.firendlyName + "]" + "[" + videoFile.filename + "]开始匹配...");
                                    String keyword = videoFile.keyword;

                                    //获取文件关键字
                                    if (TextUtils.isEmpty(keyword)) {
                                        LogUtil.v(Thread.currentThread().getName(), "");
                                        MovieNameInfo movieNameInfo = MovieScanService.this.createMovieNameInfo(videoFile);
                                        keyword = movieNameInfo.getName();
                                        videoFile.keyword = keyword;
                                        videoFile.season = movieNameInfo.getSeason();
                                        videoFile.episode = movieNameInfo.toEpisode("0");
                                        mVideoFileDao.update(videoFile);
                                    }

                                    //选择搜索api
                                    String api = Constants.Scraper.TMDB_EN;
                                    if (StringUtils.isGB2312(keyword)) {
                                        api = Constants.Scraper.TMDB;
                                    }
                                    LogUtil.v(Thread.currentThread().getName(), "[" + videoFile.filename + "]关键字->[" + keyword + "]:" + api);
                                    //keyword不能为空
                                    if (!TextUtils.isEmpty(keyword)) {
                                        Observable<MovieSearchRespone> tmdbSearchRespone;
                                        //搜索模式 TODO 需要添加本地匹配
                                        switch (searchType1) {
                                            case movie:
                                                tmdbSearchRespone = TmdbApiService.movieSearch(keyword, api);
                                                break;
                                            case tv:
                                                tmdbSearchRespone = TmdbApiService.tvSearch(keyword, api);
                                                break;
                                            default:
                                                tmdbSearchRespone = TmdbApiService.unionSearch(keyword, api);
                                                break;
                                        }
                                        //TODO，通过缩短关键词，提高搜索几率。
                                        return Observable.zip(tmdbSearchRespone,
                                                Observable.just(keyword),
                                                (movieSearchRespone, _keyword) -> {
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

                                                    String movie_id = mostSimilarMovie == null ? "-1" : mostSimilarMovie.movieId;
                                                    if (!movie_id.equals("-1")) {
                                                        String type = mostSimilarMovie.type.name();
                                                        LogUtil.w(Thread.currentThread().getName(), "开始获取电影" + movie_id);
                                                        MovieDetailRespone respone = TmdbApiService.getDetials(movie_id, Constants.Scraper.TMDB, type)
                                                                .onErrorReturn(throwable -> {
                                                                    LogUtil.e(Thread.currentThread().getName(), "获取电影" + movie_id + "失败");
                                                                    return null;
                                                                })
                                                                .subscribeOn(Schedulers.io()).blockingFirst();
                                                        if (respone != null) {
                                                            MovieWrapper wrapper = respone.toEntity();
                                                            if (wrapper != null) {
                                                                MovieScanService.this.saveMovieWrapper(wrapper, videoFile, Constants.Scraper.TMDB);
                                                            }
                                                        }
                                                        MovieDetailRespone respone_en = TmdbApiService.getDetials(movie_id, Constants.Scraper.TMDB_EN, type)
                                                                .onErrorReturn(throwable -> {
                                                                    LogUtil.e(Thread.currentThread().getName(), "获取电影(英)" + movie_id + "失败");
                                                                    return null;
                                                                })
                                                                .subscribeOn(Schedulers.io()).blockingFirst();
                                                        if (respone_en != null) {
                                                            MovieWrapper wrapper = respone_en.toEntity();
                                                            if (wrapper != null) {
                                                                MovieScanService.this.saveMovieWrapper(wrapper, videoFile, Constants.Scraper.TMDB_EN);
                                                            }
                                                        }
                                                    }
                                                    Object[] data = new Object[2];
                                                    data[0] = movie_id;
                                                    data[1] = shortcut1;
                                                    return data;
                                                })
                                                .observeOn(Schedulers.from(mNetwork2Executor))
                                                .blockingFirst();
                                    }
                                    Object[] data = new Object[1];
                                    data[0] = shortcut1;
                                    return data;
                                })
                                .doOnNext(data -> {
                                    if (data.length == 2) {
                                        String movieId = (String) data[0];
                                        Shortcut st = (Shortcut) data[1];
                                        if (!movieId.equals("-1")) {
                                            st.posterCount = st.posterCount + 1;
                                        }
                                        st.fileCount = total;
                                        tmpMoviesSet.add(movieId);
                                        mShortcutDao.updateShortcut(st);
                                    }else{
                                        Shortcut st = (Shortcut) data[0];
                                        st.fileCount = total;
                                        mShortcutDao.updateShortcut(st);
                                    }
                                })
                                .subscribe(new SimpleObserver<Object[]>() {
                                    @Override
                                    public void onSubscribe(Disposable d) {
                                        super.onSubscribe(d);
                                        //全局任务标志为0发送广播：扫描
                                        if (mGlobalTaskCount.getAndIncrement() == 0) {
                                            Intent intent = new Intent();
                                            intent.setAction(Constants.BroadCastMsg.MOVIE_SCRAP_START);
                                            LocalBroadcastManager.getInstance(MovieScanService.this).sendBroadcast(intent);
                                        }
                                        //当前Shortcut任务标志为0发送Shortcut开始扫描Action
                                        if (currentTaskCount.getAndIncrement() == 0) {
                                            mShortcutHashSet.add(shortcut1);
                                            shortcut1.isScanned=2;
                                            Intent intent = new Intent();
                                            intent.setAction(Constants.BroadCastMsg.SHORTCUT_SCRAP_START);
                                            intent.putExtra(Constants.Extras.SHORTCUT, shortcut1);
                                            LocalBroadcastManager.getInstance(MovieScanService.this).sendBroadcast(intent);
                                        }
                                    }

                                    @Override
                                    public void onAction(Object[] data) {
                                        int scannedCount = indexAtom.incrementAndGet();
                                        if (data.length == 2) {
                                            String movieId = (String) data[0];
                                            Shortcut st = (Shortcut) data[1];
                                            if (!movieId.equals("-1")) {
                                                int success = successCount.incrementAndGet();
                                                sendMatchMovieSuccess(st, movieId, success, scannedCount, total);
//                                                BroadcastHelper.sendBroadcastMovieAddSync(getBaseContext(),movieId);
                                                LogUtil.w(TAG, "onAction(" + scannedCount + ") movieId:" + movieId);
                                            } else {
                                                sendMatchMovieFailed(st, scannedCount, total);
                                                LogUtil.w(TAG, "onAction(" + scannedCount + ") 匹配失败,movieI -1");
                                            }
                                        } else {
                                            Shortcut st = (Shortcut) data[0];
                                            sendMatchMovieFailed(st, scannedCount, total);
                                            LogUtil.w(TAG, "onAction(" + scannedCount + ") 匹配失败");
                                        }
                                    }

                                    @Override
                                    public void onComplete() {
                                        super.onComplete();
                                        LogUtil.v(TAG, "onComplete");
                                        synchronized (lock) {
                                            if (currentTaskCount.decrementAndGet() == 0) {
                                                //文件夹扫描结束
                                                shortcut1.isScanned=1;
                                                mShortcutDao.updateShortcut(shortcut1);
                                                mShortcutHashSet.remove(shortcut1);
                                                Intent intent = new Intent();
                                                intent.setAction(Constants.BroadCastMsg.SHORTCUT_SCRAP_STOP);
                                                intent.putExtra(Constants.Extras.SHORTCUT, shortcut1);
                                                LocalBroadcastManager.getInstance(MovieScanService.this).sendBroadcast(intent);
                                                ArrayList<String> movieIds=new ArrayList<>(tmpMoviesSet);
                                                BroadcastHelper.sendBroadcastSearchMoviesSync(getBaseContext(),movieIds);
                                            }
                                            if (mGlobalTaskCount.decrementAndGet() == 0) {
                                                //扫描程序结束
                                                Intent intent = new Intent();
                                                intent.setAction(Constants.BroadCastMsg.MOVIE_SCRAP_STOP);
                                                LocalBroadcastManager.getInstance(MovieScanService.this).sendBroadcast(intent);
                                            }
                                        }
                                    }

                                });
                        return new Object[0];
                    }
                })
                .subscribeOn(Schedulers.from(mSearchMovieExecutor))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<Object[]>() {
                    @Override
                    public void onAction(Object[] objects) {

                    }
                });
    }

    private void sendMatchMovieSuccess(Shortcut shortcut, String movie_id, int success, int scanned_count, int total) {
        Intent intent = new Intent();
        intent.setAction(Constants.BroadCastMsg.MATCHED_MOVIE);
        intent.putExtra(Constants.Extras.MOVIE_ID, movie_id);
        intent.putExtra(Constants.Extras.SUCCESS_COUNT, success);
        intent.putExtra(Constants.Extras.SCANNED_COUNT, scanned_count);
        intent.putExtra(Constants.Extras.TOTAL, total);
        intent.putExtra(Constants.Extras.SHORTCUT, shortcut);
        LocalBroadcastManager.getInstance(MovieScanService.this).sendBroadcast(intent);
    }

    private void sendMatchMovieFailed(Shortcut shortcut, int scanned_count, int total) {
        Intent intent = new Intent();
        intent.setAction(Constants.BroadCastMsg.MATCHED_MOVIE_FAILED);
        intent.putExtra(Constants.Extras.SCANNED_COUNT, scanned_count);
        intent.putExtra(Constants.Extras.TOTAL, total);
        intent.putExtra(Constants.Extras.SHORTCUT, shortcut);
        LocalBroadcastManager.getInstance(MovieScanService.this).sendBroadcast(intent);
    }

    public HashSet<Shortcut> getShortcutHashSet(){
        return mShortcutHashSet;
    }

    /**
     * 扫描服务运行状态
     *
     * @return
     */
    public boolean isRunning() {
        return mGlobalTaskCount.get() == 0 ? false : true;
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
        List<Season> seasonList = movieWrapper.seasons;
        //插入电影到数据库
        movie.pinyin = PinyinParseAndMatchTools.parsePinyin(movie.title);
        movie.addTime = System.currentTimeMillis();
        long id = mMovieDao.insertOrIgnoreMovie(movie);
        //多对多可以先插入数据库
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

        MovieVideoFileCrossRef movieVideoFileCrossRef = new MovieVideoFileCrossRef();
        movieVideoFileCrossRef.id = movie_id;
        movieVideoFileCrossRef.path = videoFile.path;
        movieVideoFileCrossRef.source = source;
        mMovieVideofileCrossRefDao.insertOrReplace(movieVideoFileCrossRef);

        videoFile.isScanned = 1;
        mVideoFileDao.update(videoFile);

        LogUtil.v(Thread.currentThread().getName(), "saveMovie=>: " + movie_id + ":" + videoFile.filename);

    }


}
