package com.hphtv.movielibrary.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.firefly.videonameparser.MovieNameInfo;
import com.firefly.videonameparser.VideoNameParserV2;
import com.hphtv.movielibrary.MovieApplication;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.MovieDao;
import com.hphtv.movielibrary.roomdb.dao.ShortcutDao;
import com.hphtv.movielibrary.roomdb.dao.VideoFileDao;
import com.hphtv.movielibrary.roomdb.entity.Genre;
import com.hphtv.movielibrary.roomdb.entity.Movie;
import com.hphtv.movielibrary.roomdb.entity.Season;
import com.hphtv.movielibrary.roomdb.entity.Shortcut;
import com.hphtv.movielibrary.roomdb.entity.StagePhoto;
import com.hphtv.movielibrary.roomdb.entity.VideoFile;
import com.hphtv.movielibrary.roomdb.entity.relation.MovieWrapper;
import com.hphtv.movielibrary.scraper.service.OnlineDBApiService;
import com.hphtv.movielibrary.scraper.service.TmdbApiService;
import com.hphtv.movielibrary.scraper.respone.MovieDetailRespone;
import com.hphtv.movielibrary.scraper.respone.MovieSearchRespone;
import com.hphtv.movielibrary.util.MovieHelper;
import com.hphtv.movielibrary.util.PinyinParseAndMatchTools;
import com.hphtv.movielibrary.util.nfo.NFOEntity;
import com.hphtv.movielibrary.util.nfo.NFOEpisodes;
import com.hphtv.movielibrary.util.nfo.NFOMovie;
import com.hphtv.movielibrary.util.nfo.NFOMovieKt;
import com.hphtv.movielibrary.util.nfo.NFOTVShow;
import com.hphtv.movielibrary.util.nfo.NFOTVShowKt;
import com.hphtv.movielibrary.util.nfo.NFOType;
import com.hphtv.movielibrary.util.nfo.factory.KodiNFOFactory;
import com.hphtv.movielibrary.util.nfo.reader.NFOReader;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;
import com.station.kit.util.EditorDistance;
import com.station.kit.util.LogUtil;
import com.station.kit.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private MovieDao mMovieDao;
    private VideoFileDao mVideoFileDao;

    private HashSet<Shortcut> mShortcutHashSet = new HashSet<>();

    private NFOReader mNFOReader;

    @Override
    public void onCreate() {
        super.onCreate();
        initDao();
        initThreadPools();
        mNFOReader = new KodiNFOFactory().createReader();
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
        mMovieDao = movieLibraryRoomDatabase.getMovieDao();
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
        Boolean tryReadNFO = true;
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
                                            if (tryReadNFO) {
                                                if (seamLikeEpisode(videoFile.filename).find() || seamLikeEpisode(videoFile.filename).find()) {
                                                    File nfoFile = findTVShowInfoFile(videoFile.path, 2);
                                                    if (nfoFile != null) {
                                                        return getMovieInfoFromNFO(nfoFile, videoFile, shortcut1);
                                                    }
                                                } else {
                                                    File nfoFile = new File(videoFile.path.substring(0, videoFile.path.lastIndexOf(".")) + ".nfo");
                                                    if (nfoFile.exists()) {
                                                        return getMovieInfoFromNFO(nfoFile, videoFile, shortcut1);
                                                    }
                                                }
                                            }
                                            return getMovieInfoFromApi(videoFile, searchType1, shortcut1);
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
                                                        LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(new Intent(Constants.ACTION.MOVIE_SCRAP_STOP_AND_REFRESH));
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

    private MovieNameInfo parseNameInfo(VideoFile videoFile) {
        VideoNameParserV2 parser = new VideoNameParserV2();
        MovieNameInfo nameInfo;
        if (videoFile.path.startsWith("http://")) {
            nameInfo = parser.parseVideoName(videoFile.filename);
        } else {
            nameInfo = parser.parseVideoName(videoFile.path);
        }
        return nameInfo;
    }

    private Object[] getMovieInfoFromApi(VideoFile videoFile, Constants.SearchType searchType1, Shortcut shortcut1) {
        MovieNameInfo nameInfo = parseNameInfo(videoFile);
        String keyword = nameInfo.getName();
        String year = nameInfo.getYear() == 0 ? null : String.valueOf(nameInfo.getYear());
        videoFile.keyword = keyword;
        videoFile.season = nameInfo.getSeason();
        videoFile.episode = nameInfo.toEpisode();
        videoFile.aired = nameInfo.getAired();

        if (nameInfo.getResolution() != null) {
            videoFile.resolution = nameInfo.getResolution();
        }
        if (nameInfo.getVideoSource() != null) {
            videoFile.videoSource = nameInfo.getVideoSource();
        }
        //选择搜索api
        String api = Constants.Scraper.TMDB_EN;
        if (StringUtils.isGB2312(keyword)) {
            api = Constants.Scraper.TMDB;
        }
        LogUtil.v(Thread.currentThread().getName(), "[" + videoFile.filename + "]关键字->[" + keyword + "]:" + api);
        //keyword不能为空
        if (MovieApplication.hasNetworkConnection&& !TextUtils.isEmpty(keyword)) {
            Observable<MovieSearchRespone> tmdbSearchRespone;
            switch (searchType1) {
                case movie:
                    tmdbSearchRespone = TmdbApiService.movieSearch(keyword, api, year);
                    break;
                case tv:
                    tmdbSearchRespone = TmdbApiService.tvSearch(keyword, api);
                    break;
                default:
                    if (MovieNameInfo.TYPE_MOVIE.equals(nameInfo.getType()) || MovieNameInfo.TYPE_EXTRAS.equals(nameInfo.getType())) {
                        tmdbSearchRespone = TmdbApiService.movieSearch(keyword, api, year);
                    } else if (MovieNameInfo.TYPE_SERIES.equals(nameInfo.getType())) {
                        tmdbSearchRespone = TmdbApiService.tvSearch(keyword, api);
                    } else {
                        tmdbSearchRespone = TmdbApiService.unionSearch(keyword, api, year);
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
                                    getMovieDetail(movie_id, videoFile, shortcut1.access, Constants.Scraper.TMDB, type);
                                    getMovieDetail(movie_id, videoFile, shortcut1.access, Constants.Scraper.TMDB_EN, type);
                                } else {
                                    OnlineDBApiService.uploadFile(videoFile, Constants.Scraper.TMDB_EN);
                                    OnlineDBApiService.uploadFile(videoFile, Constants.Scraper.TMDB);
                                    LogUtil.e(Thread.currentThread().getName(), "[" + _keyword + "][" + year + "] 此关键字无搜索结果");
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
    }

    private Object[] getMovieInfoFromNFO(File nfoFile, VideoFile videoFile, Shortcut shortcut) {
        try {
            FileInputStream fileInputStream = new FileInputStream(nfoFile);
            NFOEntity entity = mNFOReader.readFromXML(fileInputStream);
            if (entity != null) {
                switch (entity.getNFOType()) {
                    case MOVIE:
                        NFOMovie nfoMovie = (NFOMovie) entity;
                        Movie movie = NFOMovieKt.toMovie(nfoMovie);
                        List<Genre> movieGenreList = NFOMovieKt.toGenreList(nfoMovie);
                        List<StagePhoto> movieThumbList = NFOMovieKt.toStagePhotoList(nfoMovie);
                        updateNFOMovie(movie);
                        MovieHelper.addNewMovieInfo(getBaseContext(), movie, movieGenreList, nfoMovie.getDirectors(), nfoMovie.getActors(), movieThumbList, Collections.emptyList(), videoFile);
                        Object[] data = new Object[2];
                        data[0] = movie.movieId;
                        data[1] = shortcut;
                        return data;
                    case TVSHOW:
                        NFOTVShow nfotvShow = (NFOTVShow) entity;
                        Movie tvShow = NFOTVShowKt.toMovie(nfotvShow);
                        List<Genre> tvGenreList = NFOTVShowKt.toGenreList(nfotvShow);
                        List<StagePhoto> tvStagePhotoList = NFOTVShowKt.toStagePhotoList(nfotvShow);
                        List<Season> tvSeasonList = NFOTVShowKt.toSeasonList(nfotvShow);
                        getEpisodeInfoFromNFO(videoFile);
                        updateNFOMovie(tvShow);
                        MovieHelper.addNewMovieInfo(getBaseContext(), tvShow, tvGenreList, nfotvShow.getDirectors(), nfotvShow.getActors(), tvStagePhotoList, tvSeasonList, videoFile);
                        Object[] tvData = new Object[2];
                        tvData[0] = tvShow.movieId;
                        tvData[1] = shortcut;
                        return tvData;
                    case EPISODE:
                        File tvNFOFile = findTVShowInfoFile(nfoFile.getPath(), 2);
                        if (tvNFOFile != null) {
                            return getMovieInfoFromNFO(tvNFOFile, videoFile, shortcut);
                        }
                        break;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Object[] data = new Object[1];
        data[0] = shortcut;
        return data;
    }

    private void getEpisodeInfoFromNFO(VideoFile videoFile) {
        try {
            File nfoFile = new File(videoFile.path.substring(0, videoFile.path.lastIndexOf(".")) + ".nfo");
            FileInputStream fileInputStream = new FileInputStream(nfoFile);
            NFOEntity entity = mNFOReader.readFromXML(fileInputStream);
            if (entity != null) {
                if (entity.getNFOType() == NFOType.EPISODE) {
                    NFOEpisodes nfoEpisodes = (NFOEpisodes) entity;
                    if (nfoEpisodes.getSeason() != null) {
                        videoFile.season = Integer.parseInt(nfoEpisodes.getSeason());
                    }
                    if (nfoEpisodes.getEpisode() != null) {
                        videoFile.episode = Integer.parseInt(nfoEpisodes.getEpisode());
                    }
                    if (nfoEpisodes.getPremiered() != null) {
                        videoFile.aired = nfoEpisodes.getPremiered();
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void updateNFOMovie(Movie nfoMovie) {
        Movie movie = mMovieDao.queryByMovieIdAndType(nfoMovie.movieId, nfoMovie.source, nfoMovie.type.name());
        if (movie != null) {
            nfoMovie.id = movie.id;
            nfoMovie.region = movie.region;
            nfoMovie.language = movie.language;
            nfoMovie.pinyin = movie.pinyin;
            nfoMovie.addTime = movie.addTime;
            nfoMovie.updateTime = System.currentTimeMillis();
            nfoMovie.isFavorite = movie.isFavorite;
            nfoMovie.lastPlayTime = movie.lastPlayTime;
            nfoMovie.isWatched = movie.isWatched;
            nfoMovie.ap = movie.ap;
        } else {
            nfoMovie.addTime = System.currentTimeMillis();
            nfoMovie.updateTime = System.currentTimeMillis();
            nfoMovie.pinyin = PinyinParseAndMatchTools.parsePinyin(nfoMovie.title);
            nfoMovie.ap = Constants.WatchLimit.ALL_AGE;
        }
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


    private void getMovieDetail(String movie_id, VideoFile videoFile, Constants.WatchLimit limit, String source, String type) {
        Movie movie = mMovieDao.queryByMovieIdAndType(movie_id, source, type);
        if (movie != null) {
            movie.addTime = System.currentTimeMillis();
            mMovieDao.update(movie);
            MovieHelper.establishRelationshipBetweenPosterAndVideos(getBaseContext(), movie.id, videoFile, source);
            MovieHelper.quickAutoClassification(getBaseContext(), movie_id, source);
            OnlineDBApiService.uploadMovie(movie, videoFile, source);
        } else {
            MovieDetailRespone response = TmdbApiService.getDetail(movie_id, source, type)
                    .onErrorReturn(throwable -> {
                        LogUtil.e(Thread.currentThread().getName(), "onErrorReturn: " + videoFile.keyword + " 获取电影" + movie_id + "失败");
                        OnlineDBApiService.uploadFile(videoFile, source);
                        return null;
                    })
                    .subscribeOn(Schedulers.io()).blockingFirst();
            MovieWrapper wrapper = null;
            if (response != null) {
                try {
                    wrapper = response.toEntity();
                    if (wrapper != null) {
                        wrapper.movie.ap = limit;
                        MovieHelper.addNewMovieInfo(getBaseContext(), wrapper, videoFile);
                    } else {
                        OnlineDBApiService.uploadFile(videoFile, source);
                        LogUtil.e(Thread.currentThread().getName(), source + "：获取电影详情为空[keyword:" + videoFile.keyword + "][movie_id:" + movie_id + "]");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    OnlineDBApiService.uploadFile(videoFile, source);
                    LogUtil.e(Thread.currentThread().getName(), source + "：获取电影详情执行toEntity失败[keyword:" + videoFile.keyword + "][movie_id:" + movie_id + "]");
                }
            } else {
                OnlineDBApiService.uploadFile(videoFile, source);
            }
        }
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

    /**
     * 找寻当前目录上两级的名为tvshow.nfo的文件
     *
     * @param
     * @return
     */
    private File findTVShowInfoFile(String path, int depth) {
        if (depth < 0) {
            return null;
        }
        File file = new File(path);
        if (file.isDirectory()) {
            File[] subFiles = file.listFiles();
            for (int i = 0; i < subFiles.length; i++) {
                File subFile = subFiles[i];
                if (subFile.getName().equalsIgnoreCase("tvshow.nfo")) {
                    return subFile;
                }
            }
        }
        return findTVShowInfoFile(file.getParentFile().getPath(), depth - 1);
    }

    private Matcher seamLikeVarietyShow(String fileName) {
        String regex = "20\\d{2}[-_\\.\\s]?\\d{2}[-_\\.\\s]?\\d{2}.*\\.";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        return pattern.matcher(fileName);
    }

    private Matcher seamLikeEpisode(String fileName) {
        String regex = "(?:(?:s(?:eason)?0?[0-9]+)?ep?(?:isode)?0?[0-9]+.*\\..*)|(^0?[0-9]+\\..*)";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        return pattern.matcher(fileName);
    }

    private Matcher matchSeason(String folderName) {
        String regex = "^[s|S](?:eason)?[\\s|_|+]?([0]?[0-9]+)";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        return pattern.matcher(folderName);
    }

    private Matcher matchSP(String seasonSp) {
        String regex = "^(?:[s|S](?:eason)?)?[\\s|_|+]?sp(ecial)?";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        return pattern.matcher(seasonSp);
    }

}
