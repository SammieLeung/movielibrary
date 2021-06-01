package com.hphtv.movielibrary.service;

import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.provider.DocumentsContract;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.firefly.filepicker.data.bean.FileItem;
import com.firefly.filepicker.provider.PickerContentProvider;
import com.firefly.videonameparser.MovieNameInfo;
import com.firefly.videonameparser.VideoNameParser;
import com.firefly.videonameparser.utils.StringUtils;
import com.hphtv.movielibrary.MovieApplication;
import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.sqlite.bean.Device;
import com.hphtv.movielibrary.sqlite.bean.Directory;
import com.hphtv.movielibrary.sqlite.bean.MovieWrapper;
import com.hphtv.movielibrary.sqlite.bean.ScraperInfo;
import com.hphtv.movielibrary.sqlite.bean.VideoFile;
import com.hphtv.movielibrary.sqlite.bean.others.ParseFile;
import com.hphtv.movielibrary.sqlite.bean.scraperBean.Movie;
import com.hphtv.movielibrary.sqlite.bean.scraperBean.SimpleMovie;
import com.hphtv.movielibrary.data.ConstData;
import com.hphtv.movielibrary.listener.ScanProgressListener;
import com.hphtv.movielibrary.listener.WebviewListener;
import com.hphtv.movielibrary.scraper.douban.DoubanApi;
import com.hphtv.movielibrary.scraper.imdb.ImdbApi;
import com.hphtv.movielibrary.scraper.mtime.MtimeApi;
import com.hphtv.movielibrary.sqlite.dao.DirectoryDao;
import com.hphtv.movielibrary.sqlite.dao.MovieDao;
import com.hphtv.movielibrary.sqlite.dao.MovieWrapperDao;
import com.hphtv.movielibrary.sqlite.dao.VideoFileDao;
import com.hphtv.movielibrary.util.BroadcastHelper;
import com.hphtv.movielibrary.util.DoubanMovieSearchHelper;
import com.hphtv.movielibrary.util.FileScanUtil;
import com.hphtv.movielibrary.util.LogUtil;
import com.hphtv.movielibrary.util.MyPinyinParseAndMatchUtil;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by tchip on 17-11-13.
 */

public class MovieScanService extends Service {
    @IntDef({ConstData.Scraper.UNKNOW, ConstData.Scraper.DOUBAN, ConstData.Scraper.IMDB, ConstData.Scraper.MTIME})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Api {
    }

    public static final int MODE_SEARCH_SERVICE = 0;
    public static final int MODE_GETINFO = 1;

    @IntDef({MODE_SEARCH_SERVICE, MODE_GETINFO})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Mode {
    }

    private boolean isRunning;

    public static final String TAG = "MovieScanService";
    public ScanBinder binder = new ScanBinder();
    DoubanMovieSearchHelper searchHelper;
    public ScanProgressListener mScanProgressListener;

    VideoFileDao mVideoFileDao;
    MovieDao mMovieDao;
    MovieWrapperDao mMovieWrapperDao;
    DirectoryDao mDirectoryDao;
    MediaMetadataRetriever mMediaMetadataRetriever;

    ThreadPoolExecutor mMovieInfoSearchService;
    ThreadPoolExecutor mScannerWorkers;
    Object mSync = new Object();
    Object mMovSync = new Object();

    private List<Directory> mDirectories = new ArrayList<>();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        LogUtil.v(TAG, "onBind()");
        if (binder == null)
            binder = new ScanBinder();
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        LogUtil.v(TAG, "onUnbind()");
        return true;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        LogUtil.v(TAG, "onRebind()");
    }

    //Service被创建时调用
    @Override
    public void onCreate() {
        mVideoFileDao = new VideoFileDao(this);
        mMovieDao = new MovieDao(this);
        mMovieWrapperDao = new MovieWrapperDao(this);
        mDirectoryDao = new DirectoryDao(this);
        mMediaMetadataRetriever = new MediaMetadataRetriever();
        Log.i(TAG, "onCreate方法被调用!");
        if (searchHelper == null) {
            searchHelper = ((MovieApplication) getApplication()).getSearchHelper();
            searchHelper.registerWebviewListener(mWebviewListener);
        }
        super.onCreate();
    }


    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestory方法被调用!");
//        unregisterReceiver(binderReceiver);
        super.onDestroy();
    }

    private void onSuccess(MovieWrapper movieWrapper, ParseFile parseFile, int fileCount) {
        synchronized (parseFile) {
            updateDirectoryVideoNum(parseFile.getDirectory(), true, fileCount);
            if (mScanProgressListener != null)
                mScanProgressListener.onSuccess(parseFile, movieWrapper);
            synchronized (mDirectories) {
                if (mDirectories.size() == 0) {
                    onScanStop();
                }
            }
        }
    }

    private void onFailed(MovieWrapper movieWrapper, ParseFile parseFile, int fileCount) {
        synchronized (parseFile) {
            updateDirectoryVideoNum(parseFile.getDirectory(), false, fileCount);
            if (mScanProgressListener != null)
                mScanProgressListener.onFailed(parseFile, movieWrapper);
            synchronized (mDirectories) {
                if (mDirectories.size() == 0) {
                    onScanStop();
                }
            }
        }
    }


    public void setScanProgressListener(ScanProgressListener listener) {
        mScanProgressListener = listener;
    }

    public void initScanService() {
        if (isRunning)
            return;
        isRunning = true;
        if (mScannerWorkers == null || mScannerWorkers.isShutdown())
            mScannerWorkers = new ThreadPoolExecutor(2, 2, 20, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>());
        if (mMovieInfoSearchService == null || mMovieInfoSearchService.isShutdown())
            mMovieInfoSearchService = new ThreadPoolExecutor(4, 4, 20, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>());
        synchronized (mDirectories) {
            if (mDirectories == null)
                mDirectories = new ArrayList<>();
            mDirectories.clear();
        }
        if (mScanProgressListener != null)
            mScanProgressListener.onStart();
    }

    public void stopScan() {
        try {
            mMovieInfoSearchService.shutdownNow();
            mScannerWorkers.shutdownNow();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            onScanStop();
        }
    }


    private void onScanStop() {
        isRunning = false;
        if (mScanProgressListener != null)
            mScanProgressListener.onFinish();
    }

    /**
     * 添加待扫描目录到扫描等待队列。
     *
     * @param device
     * @param directory
     * @param isEncrypted
     */
    public void addToScanQueue(final Device device, final Directory directory, final int isEncrypted) {
        if (mScannerWorkers != null && !mScannerWorkers.isShutdown())
            mScannerWorkers.execute(new Runnable() {
                @Override
                public void run() {
                    if (isRunning)
                        scanFile(device, directory, isEncrypted);
                }
            });
    }

    /**
     * 扫描文件树
     *
     * @param device
     * @param directory
     * @param isEncrypted
     */
    private void scanFile(final Device device, Directory directory, int isEncrypted) {
        directory.setVideoNumber(0);
        directory.setMatchedVideo(0);
        directory.setScanState(ConstData.DirectoryState.SCANNING);
        synchronized (mDirectories) {
            mDirectories.add(directory);
        }
        //创建Device并保存到数据库

        //创建directory并保存到数据库
        Uri uri = Uri.parse(directory.getUri());
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = null;
        try {
            Log.v(TAG, "scan uri=" + uri.toString());
            cursor = contentResolver.query(
                    uri,
                    null,
                    null,
                    null,
                    null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<ParseFile> parseFileList = new ArrayList<>();
        if (cursor != null &&cursor.getCount() > 0) {
            final int fileCount = cursor.getCount();
            while (cursor.moveToNext() && !Thread.currentThread().isInterrupted()) {
                if (!isRunning) {
                    break;
                }
                int name_index = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME);
                int path_index = cursor.getColumnIndexOrThrow("path");
                int type_index = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE);
                int filesource_index = cursor.getColumnIndexOrThrow("file_source");
                String filename = cursor.getString(name_index);
                String path = cursor.getString(path_index);
                String metadataRetrieverTitle = null;
                int fileSource=cursor.getInt(filesource_index);
                Log.v(TAG, "path=" + path);
                Log.v(TAG, "file_source = " + filesource_index);
                String type = (cursor.getString(type_index));
                String extension = type.substring(type.lastIndexOf("/") + 1, type.length());
                LogUtil.v(TAG, "原名 filename:" + filename);
                LogUtil.v(TAG, "path " + path);
                LogUtil.v(TAG, "type " + type);
                if(fileSource==FileItem.EXTERNAL&&path!=null) {
                    try {
                        mMediaMetadataRetriever.setDataSource(path);
                        metadataRetrieverTitle = mMediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                        LogUtil.v(TAG, "MetadataRetrieverTitle:" + metadataRetrieverTitle);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                MovieNameInfo mni = FileScanUtil.simpleParse(metadataRetrieverTitle == null ? filename : metadataRetrieverTitle);
                VideoFile videoFile = new VideoFile();
                videoFile.setFilename(filename);
                videoFile.setUri(path);
                videoFile.setDev_id(device.getId());
                videoFile.setDir_id(directory.getId());
                videoFile.setSearchName(mni.getName());
                videoFile.setTitlePinyin(MyPinyinParseAndMatchUtil.parsePinyin(mni.getName()));
                videoFile = saveVideoFile(videoFile, directory, device);
                final ParseFile parseFile = new ParseFile();
                parseFile.setMni(mni);
                parseFile.setVideoFile(videoFile);
                parseFile.setDevice(device);
                parseFile.setDirectory(directory);
                parseFileList.add(parseFile);
                if (mMovieInfoSearchService != null && !mMovieInfoSearchService.isShutdown())
                    mMovieInfoSearchService.execute(new Runnable() {
                        @Override
                        public void run() {
                            if (mScanProgressListener != null)
                                mScanProgressListener.onAddToScan(parseFile);
                            searchByName(parseFile, fileCount);
                        }
                    });
            }
        } else {

            updateDirectoryVideoNum(directory, false, 0);
            synchronized (mDirectories) {
                if (mDirectories.size() == 0) {
                    onScanStop();
                }
            }

        }
    }

    /**
     * 持久化videoFile
     *
     * @param videoFile
     * @return
     */
    private VideoFile saveVideoFile(VideoFile videoFile, Directory currentDir, Device currentDev) {
        if (mVideoFileDao == null)
            mVideoFileDao = new VideoFileDao(MovieScanService.this);
        Cursor cursor = mVideoFileDao.select("uri=?", new String[]{videoFile.getUri()}, null);
        if (cursor != null && cursor.getCount() > 0) {
            VideoFile dbVideoFile = mVideoFileDao.parseList(cursor).get(0);
            if (currentDev.getType() != ConstData.DeviceType.DEVICE_TYPE_DLNA) {
                if (dbVideoFile.getDir_id() != currentDir.getId()) {
                    Cursor dirCursor = mDirectoryDao.select("id=?", new String[]{String.valueOf(dbVideoFile.getDir_id())}, null);
                    if (dirCursor.getCount() > 0) {
                        Directory dbDir = mDirectoryDao.parseList(dirCursor).get(0);
                        //这里需要获取的是videofile最准确的所属目录。
                        //判断新的目录地址是否更详细。
                        if (currentDir.getPath().contains(dbDir.getPath())) {
                            dbVideoFile.setDir_id(currentDir.getId());
                            mVideoFileDao.update(mVideoFileDao.parseContentValues(dbVideoFile), "id=?", new String[]{String.valueOf(dbVideoFile.getId())});
                        }
                    }
                }
            }
            return dbVideoFile;
        } else {
            ContentValues contentValues = mVideoFileDao.parseContentValues(videoFile);
            long id = mVideoFileDao.insert(contentValues);
            videoFile.setId(id);
            return videoFile;
        }
    }


    /**
     * 根据关键词获取电影id，从而获取电影信息。
     *
     * @param parseFile
     * @param fileCount
     */
    private void searchByName(ParseFile parseFile, int fileCount) {
        SimpleMovie simpleMovie = null;
        Movie mtimeMovie = null;
        Movie imdbMovie = null;
//        String name = getSeasonAndEpisode(parseFile);
        String name = parseFile.getMni().getName();
        if (name != null) {
            simpleMovie = MtimeApi.SearchAMovieByApi(name);
            if (simpleMovie == null) {
                if (VideoNameParser.isChinese(name)) {
                    name = StringUtils.ChineseToEnglish(name);
                    simpleMovie = MtimeApi.SearchAMovieByApi(name);
                    if (simpleMovie == null)
                        for (int i = 1; i < name.length(); i++) {
                            String newSearchKey = name.substring(0, name.length() - i);
                            simpleMovie = MtimeApi.SearchAMovieByApi(newSearchKey, name);
                            if (simpleMovie != null)
                                break;
                        }
                } else {
                    String[] nameSplits = name.split(" |_|\\.|-");
                    for (int i = 1; i < nameSplits.length - 1; i++) {
                        String newSearchKey = name.substring(0, name.indexOf(nameSplits[nameSplits.length - i]));
                        simpleMovie = MtimeApi.SearchAMovieByApi(newSearchKey);
                        if (simpleMovie != null)
                            break;
                    }
                }
            }
        }

        if (simpleMovie != null) {
            mtimeMovie = searchMovieInfo(simpleMovie, parseFile, ConstData.Scraper.MTIME, fileCount);
        }

//         TODO 目前已经无法使用该API
//
//        simpleMovie = ImdbApi.SearchAMovieByName(name);
//        if (simpleMovie != null) {
//            imdbMovie = searchMovieInfo(simpleMovie, parseFile, ConstData.Scraper.IMDB, fileCount);
//        }

        MovieWrapper movieWrapper = null;
        if (mtimeMovie == null && imdbMovie == null) {
            synchronized (mSync) {
                movieWrapper = wrapData(null, parseFile);
                onFailed(movieWrapper, parseFile, fileCount);
            }
        } else {
            synchronized (mSync) {
                if (mtimeMovie != null) {
                    movieWrapper = wrapData(mtimeMovie, parseFile);
                }
                if (imdbMovie != null) {
                    movieWrapper = wrapData(imdbMovie, parseFile);
                }
                onSuccess(movieWrapper, parseFile, fileCount);
            }
        }

    }


    /* 获取电视剧第几季第几集/
     *
     * @param fileInfo
     * @return
     */
    private String getSeasonAndEpisode(ParseFile parseFile) {
        MovieNameInfo mni = parseFile.getMni();

        String parseName = mni.getName() +
                (mni != null ? (mni.getSeason() != 0 ? " Season " + mni.getSeason() : "") : "");

        return parseName;
    }

    WebviewListener mWebviewListener = new WebviewListener() {
        @Override
        public void onStart() {

        }

        @Override
        public void onGetData(List<SimpleMovie> simpleMovieList, @SearchMode int mode) {
            switch (mode) {
                case ConstData.SearchMode.MODE_INFO:
//                    simpleMovieList.get()
//                    searchMovieInfo(simpleMovie, parseFile.getVideoFile());
                    break;
                default:

            }
        }
    };

    private Movie searchMovieInfo(final SimpleMovie simpleMovie, ParseFile parseFile, @Api int api, int fileCount) {

        if (simpleMovie == null) {
            return null;
        }
        Movie movie = getMovie(simpleMovie, false, api);
        return movie;
    }

    private void updateDirectoryVideoNum(Directory directory, boolean isSuccessMatched, int fileCount) {
        int video_num = directory.getVideo_number();
        if (fileCount > 0)
            directory.setVideoNumber(++video_num);
        if (isSuccessMatched) {
            int match_num = directory.getMatched_video();
            directory.setMatchedVideo(++match_num);
        }

        if (fileCount == directory.getVideo_number()) {
            directory.setScanState(ConstData.DirectoryState.SCANNED);
            synchronized (mDirectories) {
                boolean a = mDirectories.remove(directory);
            }
        }
        ContentValues contentValues = mDirectoryDao.parseContentValues(directory);
        mDirectoryDao.update(contentValues, "id=?", new String[]{String.valueOf(directory.getId())});
    }


    public MovieWrapper wrapData(Movie movie, ParseFile parseFile) {
        //验证movie是否对应一个wrapData
        MovieWrapper movieWrapper;

        VideoFile videoFile = parseFile.getVideoFile();
        long device_id = videoFile.getDev_id();
        long dir_id = videoFile.getDir_id();

        if (movie != null) {

            long wrapper_id = movie.getWrapperId() != -1 ? movie.getWrapperId() : (videoFile.getWrapper_id() != -1 ? videoFile.getWrapper_id() : -1);
            if (wrapper_id != -1) {
                Cursor cursor = mMovieWrapperDao.select("id=?", new String[]{String.valueOf(wrapper_id)}, "0,1");
                if (cursor.getCount() > 0) {

                    movieWrapper = mMovieWrapperDao.parseList(cursor).get(0);
                    //1.获取视频文件id集合,更新集合.
                    Long[] file_ids = movieWrapper.getFileIds();

                    List<Long> resultList;
                    if (file_ids == null) {
                        resultList = new ArrayList<>();
                        resultList.add(videoFile.getId());
                    } else {
                        resultList = new ArrayList<>(file_ids.length);
                        Collections.addAll(resultList, file_ids);
                        if (!resultList.contains(videoFile.getId()))
                            resultList.add(videoFile.getId());
                    }
                    file_ids = resultList.toArray(new Long[0]);
                    movieWrapper.setFileIds(file_ids);

                    //2.获取搜刮器集合,更新
                    ScraperInfo[] scraperInfos = movieWrapper.getScraperInfos();
                    List<ScraperInfo> scraperInfoList;
                    if (scraperInfos == null) {
                        scraperInfoList = new ArrayList<>();
                        ScraperInfo scraperInfo = new ScraperInfo();
                        scraperInfo.setId(movie.getId());
                        scraperInfo.setApi(movie.getApi());
                        scraperInfoList.add(scraperInfo);
                        if (!TextUtils.isEmpty(movie.getTitle())) {
                            movieWrapper.setTitle(movie.getTitle());
                            movieWrapper.setTitlePinyin(MyPinyinParseAndMatchUtil.parsePinyin(movie.getTitle()));
                        } else if (!TextUtils.isEmpty(movie.getOriginalTitle())) {
                            movieWrapper.setTitle(movie.getOriginalTitle());
                            movieWrapper.setTitlePinyin(MyPinyinParseAndMatchUtil.parsePinyin(movie.getOriginalTitle()));
                        } else {
                            movieWrapper.setTitle(parseFile.getMni().getName());
                            movieWrapper.setTitlePinyin(MyPinyinParseAndMatchUtil.parsePinyin(parseFile.getMni().getName()));
                        }
                        if (movie.getRating() != null)
                            movieWrapper.setAverage(String.valueOf(movie.getRating().average));
                        else
                            movieWrapper.setAverage(getString(R.string.rate_not));

                        if (movie.getImages() != null)
                            movieWrapper.setPoster(movie.getImages().getLarge());
                    } else {
                        scraperInfoList = new ArrayList<>(file_ids.length);
                        Collections.addAll(scraperInfoList, scraperInfos);
                        boolean isExsit = false;
                        for (ScraperInfo info : scraperInfoList) {
                            if (info.getApi() == movie.getApi() || info.getId() == movie.getId()) {
                                isExsit = true;
                                break;
                            }
                        }
                        if (!isExsit) {
                            ScraperInfo scraperInfo = new ScraperInfo();
                            scraperInfo.setId(movie.getId());
                            scraperInfo.setApi(movie.getApi());
                            scraperInfoList.add(scraperInfo);
                        }
                    }
                    scraperInfos = scraperInfoList.toArray(new ScraperInfo[0]);
                    movieWrapper.setScraperInfos(scraperInfos);

                    //获取videofile列表，获取设备和目录

                    StringBuffer buffer = new StringBuffer();
                    List<String> argList = new ArrayList<>();
                    for (int i = 0; i < file_ids.length; i++) {
                        buffer.append("id=? or ");
                        argList.add(String.valueOf(file_ids[i]));
                    }
                    buffer.replace(buffer.lastIndexOf(" or"), buffer.length(), "");
                    Cursor vCursor = mVideoFileDao.select(buffer.toString(), argList.toArray(new String[0]), null);
                    if (vCursor.getCount() > 0) {
                        List<VideoFile> videoFileList = mVideoFileDao.parseList(vCursor);
                        int num = videoFileList.size();
                        List<Long> dev_id_list = new ArrayList<>();
                        List<Long> dir_id_list = new ArrayList<>();
                        for (int j = 0; j < num; j++) {
                            VideoFile file = videoFileList.get(j);
                            if (!dev_id_list.contains(file.getDev_id())) {
                                dev_id_list.add(file.getDev_id());
                            }
                            if (!dir_id_list.contains(file.getDir_id())) {
                                dir_id_list.add(file.getDir_id());
                            }
                        }
                        movieWrapper.setDevIds(dev_id_list.toArray(new Long[0]));
                        movieWrapper.setDirIds(dir_id_list.toArray(new Long[0]));
                    }


                    int num = mMovieWrapperDao.update(mMovieWrapperDao.parseContentValues(movieWrapper), "id=?", new String[]{String.valueOf(movieWrapper.getId())});
                    if (num > 0) {
                        movie.setWrapper_id(movieWrapper.getId());
                        ContentValues m_values = mMovieDao.parseContentValues(movie);
                        num = mMovieDao.update(m_values, "id=?", new String[]{String.valueOf(movie.getId())});
                        if (num <= 0) {
                            LogUtil.e(TAG, "insert movie faild");
                            return null;
                        }
                        videoFile.setWrapper_id(movieWrapper.getId());
                        videoFile.setMatched(ConstData.VideoFile.IS_MATCHED);
                        ContentValues v_values = mVideoFileDao.parseContentValues(videoFile);
                        num = mVideoFileDao.update(v_values, "id=?", new String[]{String.valueOf(videoFile.getId())});
                        if (num <= 0) {
                            LogUtil.e(TAG, "insert vfile faild");
                            return null;
                        }
                        BroadcastHelper.sendBroadcastMovieUpdateSync(MovieScanService.this, movieWrapper.getId());
                        return movieWrapper;
                    }
                }
            } else {
                Cursor cursor = mMovieWrapperDao.select("id=?", new String[]{String.valueOf(wrapper_id)}, "0,1");

                //1.创建一个Wrapper并保存到数据库
                movieWrapper = new MovieWrapper();
                ScraperInfo scraperInfo = new ScraperInfo();
                scraperInfo.setId(movie.getId());
                scraperInfo.setApi(movie.getApi());
                movieWrapper.setFileIds(new Long[]{videoFile.getId()});
                movieWrapper.setDevIds(new Long[]{device_id});
                movieWrapper.setDirIds(new Long[]{dir_id});
                if (movie.getImages() != null)
                    movieWrapper.setPoster(movie.getImages().large);
                if (!TextUtils.isEmpty(movie.getTitle())) {
                    movieWrapper.setTitle(movie.getTitle());
                    movieWrapper.setTitlePinyin(MyPinyinParseAndMatchUtil.parsePinyin(movie.getTitle()));
                } else if (!TextUtils.isEmpty(movie.getOriginalTitle())) {
                    movieWrapper.setTitle(movie.getOriginalTitle());
                    movieWrapper.setTitlePinyin(MyPinyinParseAndMatchUtil.parsePinyin(movie.getOriginalTitle()));
                } else {
                    movieWrapper.setTitle(parseFile.getMni().getName());
                    movieWrapper.setTitlePinyin(MyPinyinParseAndMatchUtil.parsePinyin(movie.getOriginalTitle()));
                }
                if (movie.getRating() != null)
                    movieWrapper.setAverage(String.valueOf(movie.getRating().average));
                else
                    movieWrapper.setAverage(getString(R.string.rate_not));
                movieWrapper.setScraperInfos(new ScraperInfo[]{scraperInfo});
                long rowId = mMovieWrapperDao.insert(mMovieWrapperDao.parseContentValues(movieWrapper));
                if (rowId > 0) {
                    //2.movie保存Wrapper_id
                    int num;
                    movie.setWrapper_id(rowId);
                    ContentValues m_values = mMovieDao.parseContentValues(movie);
                    num = mMovieDao.update(m_values, "id=?", new String[]{String.valueOf(movie.getId())});
                    if (num <= 0) {
                        LogUtil.e(TAG, "insert movie faild");
                        return null;
                    }
                    //3.videoFile保存wrapper_id
                    videoFile.setWrapper_id(rowId);
                    ContentValues v_values = mVideoFileDao.parseContentValues(videoFile);
                    num = mVideoFileDao.update(v_values, "id=?", new String[]{String.valueOf(videoFile.getId())});
                                      if (num <= 0) {
                        LogUtil.e(TAG, "insert vfile faild");
                        return null;
                    }
                    BroadcastHelper.sendBroadcastMovieAddSync(MovieScanService.this,  rowId);
                }
                return movieWrapper;
            }
        } else {

            long wrapper_id = videoFile.getWrapper_id();
            //包裹空电影的话，只需要为videoFile创建一个空wrapper即可，如果已经有了就不管。
            if (wrapper_id < 0) {
                movieWrapper = new MovieWrapper();
                movieWrapper.setFileIds(new Long[]{videoFile.getId()});
                movieWrapper.setDevIds(new Long[]{device_id});
                movieWrapper.setDirIds(new Long[]{dir_id});
                movieWrapper.setTitle(parseFile.getMni().getName());
                movieWrapper.setTitlePinyin(MyPinyinParseAndMatchUtil.parsePinyin(parseFile.getMni().getName()));
                movieWrapper.setAverage("-1");
                long rowId = mMovieWrapperDao.insert(mMovieWrapperDao.parseContentValues(movieWrapper));
                if (rowId > 0) {
                    videoFile.setWrapper_id(rowId);
                    ContentValues v_values = mVideoFileDao.parseContentValues(videoFile);
                    int num = mVideoFileDao.update(v_values, "id=?", new String[]{String.valueOf(videoFile.getId())});
                    if (num <= 0) {
                        LogUtil.e(TAG, "insert vfile faild");
                        return null;
                    }
                    BroadcastHelper.sendBroadcastMovieAddSync(MovieScanService.this, rowId);
                }
            } else {
                Cursor wrapperCursor = mMovieWrapperDao.select("id=?", new String[]{String.valueOf(wrapper_id)}, null);
                if (wrapperCursor.getCount() > 0) {
                    MovieWrapper wrapper = mMovieWrapperDao.parseList(wrapperCursor).get(0);
                    wrapper.setDirIds(new Long[]{dir_id});
                    wrapper.setDevIds(new Long[]{device_id});
                    mMovieWrapperDao.update(mMovieWrapperDao.parseContentValues(wrapper), "id=?", new String[]{String.valueOf(wrapper.getId())});
                    BroadcastHelper.sendBroadcastMovieUpdateSync(MovieScanService.this, wrapper_id);
                }
            }

        }
        return null;
    }


    /**
     * @param id 电影数据库索引ID
     * @return
     */
    public Movie getMovie(String id, @Api int api) {
        return getMovie(id, null, true, MODE_GETINFO, api);
    }

    private Movie getMovie(SimpleMovie simpleMovie, boolean isUpgradeAddTime, @Api int api) {
        return getMovie(simpleMovie.getId(), simpleMovie.getAlt(), isUpgradeAddTime, MODE_SEARCH_SERVICE, api);
    }


    /**
     * 需要电影ID和url
     *
     * @param id               MODE_SEARCH_SERVICE则为电影条目ID,MODE_GETINFO则为电影数据库索引ID
     * @param isUpgradeAddTime
     * @param mode
     * @return
     */
    public synchronized Movie getMovie(String id, String search_url, boolean isUpgradeAddTime, @Mode int mode, @Api int api) {
        if (mMovieDao == null)
            mMovieDao = new MovieDao(MovieScanService.this);
        Movie movie = null;
        String exist_movie_id = null;
        long exist_wrapper_id = -1;
        if (!TextUtils.isEmpty(id) && !id.equals("-1")) {
            // 电影Id不为空
            String whereClause;
            if (mode == MODE_SEARCH_SERVICE) {
                whereClause = "movie_id=?";
            } else {
                whereClause = "id=?";
                if (api > 0) {
                    whereClause += " and api=" + api;
                }
            }
            Cursor cursor = mMovieDao.select(whereClause, new String[]{id}, "0,1");
            if (cursor.getCount() > 0) {
                movie = mMovieDao.parseList(cursor).get(0);
                String uptime = movie.getUptime();
                //upTime和addTime;
                long currentTime = System.currentTimeMillis();
                if (TextUtils.isEmpty(uptime)) {
                    uptime = String.valueOf(currentTime);
                    movie.setUptime(uptime);
                }
                if (TextUtils.isEmpty(movie.getAddtime()) || isUpgradeAddTime) {
                    String addtime = String.valueOf(currentTime);
                    movie.setAddtime(addtime);
                }
                if (isUpgradeAddTime || TextUtils.isEmpty(uptime) || TextUtils.isEmpty(movie.getAddtime())) {
                    ContentValues contentValues = mMovieDao
                            .parseContentValues(movie);
                    mMovieDao.update(contentValues, "id=?",
                            new String[]{String.valueOf(movie.getId())});
                }
                long updateTime = Long.valueOf(uptime);
                // 时间间隔为3天以上的话
                if (currentTime - updateTime < ConstData.DEFAULT_UPDATE_TIME_3DAY) {
                    return movie;
                } else {
                    exist_movie_id = movie.getMovieId();
                    exist_wrapper_id = movie.getWrapperId();
                }
            }

            switch (api) {
                case ConstData.Scraper.DOUBAN:
                    if (search_url == null && movie != null)
                        search_url = movie.getAlt();
                    if (TextUtils.isEmpty(search_url))
                        return null;
                    movie = DoubanApi
                            .parserMovieInfo(search_url);// 解析电影信息(耗时几秒)
                    break;
                case ConstData.Scraper.IMDB:
                    if (mode == MODE_GETINFO) {
                        if (!TextUtils.isEmpty(exist_movie_id))
                            movie = ImdbApi.parserMovieInfoById(exist_movie_id);
                        else
                            return null;
                    } else {
                        if (TextUtils.isEmpty(id)) {
                            return null;
                        }
                        movie = ImdbApi.parserMovieInfoById(id);
                    }

                    break;
                case ConstData.Scraper.MTIME:
                    if (mode == MODE_GETINFO) {
                        if (!TextUtils.isEmpty(exist_movie_id))
                            movie = MtimeApi.parserMovieInfoFromHtmlById(exist_movie_id);
                        else
                            return null;
                    } else {
                        if (TextUtils.isEmpty(id)) {
                            return null;
                        }
                        movie = MtimeApi.parserMovieInfoFromHtmlById(id);
                    }

                    break;
            }
            //电影为空的话就发送广播并退出
            if (movie == null) {
                return null;
            }
            //获取一个新的文件路径数组
            long currentTime = System.currentTimeMillis();
            if (api != ConstData.Scraper.UNKNOW)
                movie.setApi(api);
            movie.setUptime(String.valueOf(currentTime));
            if (TextUtils.isEmpty(movie.getAddtime())) {
                movie.setAddtime(String.valueOf(currentTime));
            }
            String titlepinyin = MyPinyinParseAndMatchUtil.parsePinyin(movie.getTitle());
            movie.setTitlePinyin(titlepinyin);
            movie.setWrapper_id(exist_wrapper_id);
            ContentValues contentValues = mMovieDao
                    .parseContentValues(movie);// 将movie对象转换为ContentValues对象
            final long rowId;
            //添加或更新电影信息
            if (mode == MODE_GETINFO) {
                if (!TextUtils.isEmpty(exist_movie_id)) {
                    rowId = mMovieDao.update(contentValues, "id=?",
                            new String[]{id});
                    movie.setId(Long.parseLong(id));
                } else {
                    rowId = mMovieDao.insert(contentValues);
                    movie.setId(rowId);
                }
                if (rowId > 0) {
                    return movie;
                }
            } else {
                if (!TextUtils.isEmpty(exist_movie_id)) {
                    rowId = mMovieDao.update(contentValues, "movie_id=?",
                            new String[]{id});
                    movie.setId(Long.parseLong(id));
                } else {
                    rowId = mMovieDao.insert(contentValues);
                    movie.setId(rowId);
                }
                if (rowId > 0) {
                    return movie;
                }
            }
        }

        return movie;

    }


    public boolean isRunning() {
        return this.isRunning;
    }

    public List<Directory> getScanDirectories() {
        List<Directory> tmpList = new ArrayList<>();
        synchronized (mDirectories) {
            if (mDirectories != null) {
                tmpList.addAll(mDirectories);
            }
        }
        return tmpList;
    }

    public class ScanBinder extends Binder {
        public MovieScanService getService() {
            return MovieScanService.this;
        }
    }
}
