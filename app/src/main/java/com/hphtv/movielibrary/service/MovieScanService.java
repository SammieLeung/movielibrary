package com.hphtv.movielibrary.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.firefly.videonameparser.MovieNameInfo;
import com.firefly.videonameparser.VideoNameParser;
import com.firefly.videonameparser.VideoNameParser2;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.ShortcutDao;
import com.hphtv.movielibrary.roomdb.dao.VideoFileDao;
import com.hphtv.movielibrary.roomdb.entity.Movie;
import com.hphtv.movielibrary.roomdb.entity.Shortcut;
import com.hphtv.movielibrary.roomdb.entity.VideoFile;
import com.hphtv.movielibrary.roomdb.entity.relation.MovieWrapper;
import com.hphtv.movielibrary.scraper.service.OnlineDBApiService;
import com.hphtv.movielibrary.scraper.service.TmdbApiService;
import com.hphtv.movielibrary.scraper.respone.MovieDetailRespone;
import com.hphtv.movielibrary.scraper.respone.MovieSearchRespone;
import com.hphtv.movielibrary.util.FileScanUtil;
import com.hphtv.movielibrary.util.MovieHelper;
import com.hphtv.movielibrary.util.ScraperSourceTools;
import com.hphtv.movielibrary.util.ServiceStatusHelper;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;
import com.station.kit.util.AppUtils;
import com.station.kit.util.EditorDistance;
import com.station.kit.util.LogUtil;
import com.station.kit.util.StringUtils;

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
    private ScanBinder mScanBinder;

    private ExecutorService mSearchMovieExecutor;
    private ExecutorService mNetworkExecutor;
    private ExecutorService mNetwork2Executor;

    private ShortcutDao mShortcutDao;
    private VideoFileDao mVideoFileDao;

    private HashSet<Shortcut> mShortcutHashSet = new HashSet<>();


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
        mVideoFileDao = movieLibraryRoomDatabase.getVideoFileDao();
        mShortcutDao = movieLibraryRoomDatabase.getShortcutDao();
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

    AtomicInteger mGlobalTaskCount = new AtomicInteger();//后台扫描标志

    /**
     * 扫描
     *
     * @param shortcut      需要搜索的索引
     * @param videoFileList 索引下包含的文件列表
     * @param searchType    搜索模式
     */
    private void startSearch(Shortcut shortcut, List<VideoFile> videoFileList, Constants.SearchType searchType) {
        //combineLatest 将前面Observable的最新数据与最后的Observable发送的每个数据结合
        Object lock = new Object();
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger indexAtom = new AtomicInteger();
        AtomicInteger currentTaskCount = new AtomicInteger();
        Observable.combineLatest(Observable.just(shortcut), Observable.just(searchType), Observable.just(videoFileList).concatMap((Function<List<VideoFile>, ObservableSource<List<VideoFile>>>) videoFileList1 -> {
                            //为了支持4线程搜索,将数据分成大于4份,并行数量取决于mNetworkExecutor
                            //[1,2,3,4,11,12,13,14,21,22,23,24,31,32]
                            //                  ↓
                            //[1,11,21,31],[2,12,22,32],[3,13,23],[4,14,24]
                            int threadCount = 4;
                            List<List<VideoFile>> dataSet = new ArrayList<>();
                            for (int i = 0; i < threadCount; i++) {
                                dataSet.add(new ArrayList<>());
                            }
                            for (int i = 0; i < videoFileList1.size(); i++) {
                                dataSet.get(i % threadCount).add(videoFileList1.get(i));
                            }
                            return Observable.fromIterable(dataSet);
                        }),
                        new Function3<Shortcut, Constants.SearchType, List<VideoFile>, Object[]>() {
                            @Override
                            public Object[] apply(Shortcut shortcut1, Constants.SearchType searchType1, List<VideoFile> videoFileList) throws Throwable {
                                Observable.fromIterable(videoFileList)
                                        .observeOn(Schedulers.from(mNetworkExecutor))
                                        .map(videoFile -> {
                                            VideoNameParser2 parser = new VideoNameParser2();
                                            MovieNameInfo nameInfo;
                                            if (videoFile.path.startsWith("http://")) {
                                                nameInfo = parser.parseVideoName(videoFile.filename);
                                            } else {
                                                nameInfo = parser.parseVideoName(videoFile.path);
                                            }
                                            String keyword = nameInfo.getName();
                                            videoFile.season=nameInfo.getSeason();
                                            videoFile.episode= nameInfo.toEpisode();
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
                                                        if (MovieNameInfo.TYPE_MOVIE.equals(nameInfo.getType())) {
                                                            tmdbSearchRespone = TmdbApiService.movieSearch(keyword, api);
                                                        } else if (MovieNameInfo.TYPE_SERIES.equals(nameInfo.getType())) {
                                                            tmdbSearchRespone = TmdbApiService.tvSearch(keyword, api);
                                                        } else {
                                                            tmdbSearchRespone = TmdbApiService.unionSearch(keyword, api);
                                                        }
                                                        break;
                                                }
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
                                                                        MovieDetailRespone respone = TmdbApiService.getDetail(movie_id, Constants.Scraper.TMDB, type)
                                                                                .onErrorReturn(throwable -> {
                                                                                    LogUtil.e(Thread.currentThread().getName(), "onErrorReturn: " + _keyword + " 获取电影" + movie_id + "失败");
                                                                                    OnlineDBApiService.uploadFile(videoFile, Constants.Scraper.TMDB);
                                                                                    return null;
                                                                                })
                                                                                .subscribeOn(Schedulers.io()).blockingFirst();
                                                                        MovieWrapper wrapper = null, wrapper_en = null;
                                                                        if (respone != null) {
                                                                            wrapper = respone.toEntity();
                                                                            if (wrapper != null) {
                                                                                wrapper.movie.ap = shortcut1.access;
                                                                                MovieHelper.saveMovieWrapper(getBaseContext(), wrapper, videoFile);
                                                                            } else {
                                                                                OnlineDBApiService.uploadFile(videoFile, Constants.Scraper.TMDB);
                                                                                LogUtil.e(Thread.currentThread().getName(), "wrapper为空 " + _keyword + " 获取电影" + movie_id + "失败");
                                                                            }
                                                                        }
                                                                        MovieDetailRespone respone_en = TmdbApiService.getDetail(movie_id, Constants.Scraper.TMDB_EN, type)
                                                                                .onErrorReturn(throwable -> {
                                                                                    LogUtil.e(Thread.currentThread().getName(), "onErrorReturn: " + _keyword + " 获取电影(英)" + movie_id + "失败");
                                                                                    OnlineDBApiService.uploadFile(videoFile, Constants.Scraper.TMDB_EN);
                                                                                    return null;
                                                                                })
                                                                                .subscribeOn(Schedulers.io()).blockingFirst();
                                                                        if (respone_en != null) {
                                                                            wrapper_en = respone_en.toEntity();
                                                                            if (wrapper_en != null) {
                                                                                wrapper_en.movie.ap = shortcut1.access;
                                                                                MovieHelper.saveMovieWrapper(getBaseContext(), wrapper_en, videoFile);
                                                                            } else {
                                                                                OnlineDBApiService.uploadFile(videoFile, Constants.Scraper.TMDB_EN);
                                                                                LogUtil.e(Thread.currentThread().getName(), "wrapper为空 " + _keyword + " 获取电影(英)" + movie_id + "失败");
                                                                            }
                                                                        }
                                                                        if (wrapper == null && wrapper_en == null) {
                                                                            throw new Throwable(_keyword + "detail respone.toEntity() faild.");
                                                                        }
                                                                    } else {
                                                                        OnlineDBApiService.uploadFile(videoFile, Constants.Scraper.TMDB_EN);
                                                                        OnlineDBApiService.uploadFile(videoFile, Constants.Scraper.TMDB);
                                                                        LogUtil.e(Thread.currentThread().getName(), _keyword + " 此关键字无搜索结果");
                                                                        videoFile.isScanned = 1;
                                                                        mVideoFileDao.update(videoFile);
                                                                    }
                                                                    Object[] data = new Object[2];
                                                                    data[0] = movie_id;
                                                                    data[1] = shortcut1;
                                                                    return data;
                                                                })
                                                        .observeOn(Schedulers.from(mNetwork2Executor))
                                                        .onErrorReturn(throwable -> {
                                                            LogUtil.e(throwable.getMessage());
                                                            Object[] data = new Object[1];
                                                            data[0] = shortcut1;
                                                            return data;
                                                        })
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
                                                mShortcutDao.updateShortcut(st);
                                            } else {
                                                Shortcut st = (Shortcut) data[0];
                                                mShortcutDao.updateShortcut(st);
                                            }
                                        })
                                        .subscribe(new SimpleObserver<Object[]>() {
                                            @Override
                                            public void onSubscribe(Disposable d) {
                                                super.onSubscribe(d);
                                                //全局任务标志为0发送广播：扫描
                                                if (mGlobalTaskCount.getAndIncrement() == 0) {
//                                            Intent intent = new Intent();
//                                            intent.setAction(Constants.ACTION.MOVIE_SCRAP_START);
//                                            LocalBroadcastManager.getInstance(MovieScanService.this).sendBroadcast(intent);
                                                }
                                                //当前Shortcut任务标志为0发送Shortcut开始扫描Action
                                                if (currentTaskCount.getAndIncrement() == 0) {
                                                    mShortcutHashSet.add(shortcut1);
                                                    shortcut1.isScanned = 2;
                                                    Intent intent = new Intent();
                                                    intent.setAction(Constants.ACTION.SHORTCUT_SCRAP_START);
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
                                                        sendMatchMovieSuccess(st, movieId, success, scannedCount, shortcut.fileCount);
//                                                BroadcastHelper.sendBroadcastMovieAddSync(getBaseContext(),movieId);
                                                    } else {
                                                        sendMatchMovieFailed(st, scannedCount, shortcut.fileCount);
                                                    }
                                                } else {
                                                    Shortcut st = (Shortcut) data[0];
                                                    sendMatchMovieFailed(st, scannedCount, shortcut.fileCount);
                                                }
                                            }

                                            @Override
                                            public void onComplete() {
                                                super.onComplete();
                                                synchronized (lock) {
                                                    if (currentTaskCount.decrementAndGet() == 0) {
                                                        //文件夹扫描结束
                                                        shortcut1.isScanned = 1;
                                                        mShortcutDao.updateShortcut(shortcut1);
                                                        mShortcutHashSet.remove(shortcut1);
                                                        Intent intent = new Intent();
                                                        intent.setAction(Constants.ACTION.SHORTCUT_SCRAP_STOP);
                                                        intent.putExtra(Constants.Extras.SHORTCUT, shortcut1);
                                                        LocalBroadcastManager.getInstance(MovieScanService.this).sendBroadcast(intent);
                                                        LogUtil.v(TAG, shortcut1.friendlyName + " finish");

                                                    }
                                                    if (mGlobalTaskCount.decrementAndGet() == 0) {
                                                        LogUtil.v(TAG, "All finish");
                                                        LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(new Intent(Constants.ACTION.MOVIE_SCRAP_STOP));
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
        intent.setAction(Constants.ACTION.MATCHED_MOVIE);
        intent.putExtra(Constants.Extras.MOVIE_ID, movie_id);
        intent.putExtra(Constants.Extras.SUCCESS_COUNT, success);
        intent.putExtra(Constants.Extras.SCANNED_COUNT, scanned_count);
        intent.putExtra(Constants.Extras.TOTAL, total);
        intent.putExtra(Constants.Extras.SHORTCUT, shortcut);
        LocalBroadcastManager.getInstance(MovieScanService.this).sendBroadcast(intent);
    }

    private void sendMatchMovieFailed(Shortcut shortcut, int scanned_count, int total) {
        Intent intent = new Intent();
        intent.setAction(Constants.ACTION.MATCHED_MOVIE_FAILED);
        intent.putExtra(Constants.Extras.SCANNED_COUNT, scanned_count);
        intent.putExtra(Constants.Extras.TOTAL, total);
        intent.putExtra(Constants.Extras.SHORTCUT, shortcut);
        LocalBroadcastManager.getInstance(MovieScanService.this).sendBroadcast(intent);
    }

    public HashSet<Shortcut> getShortcutHashSet() {
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

}
