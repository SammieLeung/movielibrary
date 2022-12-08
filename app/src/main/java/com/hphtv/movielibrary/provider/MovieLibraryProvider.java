package com.hphtv.movielibrary.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import androidx.sqlite.db.SupportSQLiteQuery;
import androidx.sqlite.db.SupportSQLiteQueryBuilder;

import com.firefly.videonameparser.MovieNameInfo;
import com.firefly.videonameparser.VideoNameParser2;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.TABLE;
import com.hphtv.movielibrary.roomdb.dao.DeviceDao;
import com.hphtv.movielibrary.roomdb.dao.MovieDao;
import com.hphtv.movielibrary.roomdb.dao.MovieVideofileCrossRefDao;
import com.hphtv.movielibrary.roomdb.dao.ShortcutDao;
import com.hphtv.movielibrary.roomdb.dao.VideoFileDao;
import com.hphtv.movielibrary.roomdb.entity.Device;
import com.hphtv.movielibrary.roomdb.entity.Movie;
import com.hphtv.movielibrary.roomdb.entity.Shortcut;
import com.hphtv.movielibrary.roomdb.entity.VideoFile;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.hphtv.movielibrary.roomdb.entity.reference.MovieVideoFileCrossRef;
import com.hphtv.movielibrary.roomdb.entity.relation.MovieWrapper;
import com.hphtv.movielibrary.scraper.respone.MovieDetailRespone;
import com.hphtv.movielibrary.scraper.service.OnlineDBApiService;
import com.hphtv.movielibrary.scraper.service.TmdbApiService;
import com.hphtv.movielibrary.service.Thread.LocalFileScanHelper;
import com.hphtv.movielibrary.util.MovieHelper;
import com.hphtv.movielibrary.util.ScraperSourceTools;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;
import com.station.kit.util.LogUtil;
import com.station.kit.util.RegexMatcher;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Created by tchip on 18-5-15.
 */

public class MovieLibraryProvider extends ContentProvider {
    public static final String TAG = MovieLibraryProvider.class.getSimpleName();
    public static final String AUTHORITY = "com.hphtv.movielibrary.movieprovider";
    public static final int MOVIE_LIST = 1;
    public static final int FILE_LIST = 2;
    public static final int MOVIE_TOTAL = 3;
    public static final int FAVORITE = 4;
    public static final int APP_UPDATE_MOVIE = 5;
    public static final int APP_REMOVE_MOVIE = 6;
    public static final int APP_PRE_MATCH = 7;

    MovieDao mMovieDao;
    VideoFileDao mVideoFileDao;
    MovieVideofileCrossRefDao mMovieVideofileCrossRefDao;

    UriMatcher matcher;
    private SupportSQLiteOpenHelper mSupportSQLiteOpenHelper;

    @Override
    public boolean onCreate() {
        MovieLibraryRoomDatabase database = MovieLibraryRoomDatabase.getDatabase(getContext());
        mSupportSQLiteOpenHelper = database.getOpenHelper();
        mMovieDao = database.getMovieDao();
        mVideoFileDao = database.getVideoFileDao();
        mMovieVideofileCrossRefDao = database.getMovieVideofileCrossRefDao();
        matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(AUTHORITY, "movie_list", MOVIE_LIST);
        matcher.addURI(AUTHORITY, "file_list", FILE_LIST);
        matcher.addURI(AUTHORITY, "count", MOVIE_TOTAL);
        matcher.addURI(AUTHORITY, "favorite", FAVORITE);
        matcher.addURI(AUTHORITY, "app_update_movie", APP_UPDATE_MOVIE);
        matcher.addURI(AUTHORITY, "app_remove_movie", APP_REMOVE_MOVIE);
        matcher.addURI(AUTHORITY, "app_pre_match", APP_PRE_MATCH);

        return false;
    }


    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        int code = matcher.match(uri);
        String source = ScraperSourceTools.getSource();
        switch (code) {
            case MOVIE_LIST:
                int offset = 0;
                int limit = -1;
                if (TextUtils.isEmpty(sortOrder)) {
                    Matcher matcher = RegexMatcher.match("limit ([0-9]+),([0-9]+)", sortOrder);
                    if (!matcher.matches())
                        matcher = RegexMatcher.match("limit ([0-9]+) offset ([0-9]+)", sortOrder);
                    if (matcher.matches()) {
                        offset = Integer.parseInt(matcher.group(1));
                        limit = Integer.parseInt(matcher.group(2));
                    }
                }
                return createMovieListCursor(offset, limit, source);
            case FILE_LIST:
                if (selectionArgs != null && selectionArgs.length > 0)
                    return createFileListCurosr(selectionArgs[0], source);
                break;
            case MOVIE_TOTAL:
                return getMovieCount(source);
        }
        return null;
    }

    public Cursor createMovieListCursor(int offset, int limit, String source) {
        String[] columnNames = new String[]{"movie_id", "source", "is_favorite"};
        MatrixCursor cursor = new MatrixCursor(columnNames);
        List<MovieDataView> dataViews = mMovieDao.queryMovieDataView(source, offset, limit);
        for (MovieDataView dataView : dataViews) {
            cursor.addRow(new Object[]{dataView.movie_id, dataView.source, dataView.is_favorite == true ? 1 : 0});
        }
        return cursor;
    }

    public Cursor createFileListCurosr(String movie_id, String source) {
        String[] columnNames = new String[]{"file_id", "path", "filename"};
        MatrixCursor cursor = new MatrixCursor(columnNames);
        MovieWrapper movieWrapper = mMovieDao.queryMovieWrapperByMovieId(movie_id, source);
        if (movieWrapper != null && movieWrapper.videoFiles != null) {
            for (VideoFile videoFile : movieWrapper.videoFiles) {
                cursor.addRow(new Object[]{videoFile.vid, videoFile.path, videoFile.filename});
            }
        }
        return cursor;
    }

    public Cursor getMovieCount(String source) {
        String[] columnNames = new String[]{"total"};
        MatrixCursor cursor = new MatrixCursor(columnNames);
        int count = mMovieDao.queryTotalMovieCount(source);
        cursor.addRow(new Object[]{count});
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int code = matcher.match(uri);
        switch (code) {
            case APP_REMOVE_MOVIE:
                LogUtil.w("APP_REMOVE_MOVIE " + Thread.currentThread().getName());
                if (selectionArgs != null && selectionArgs.length == 2) {
                    String movie_id = selectionArgs[0];
                    String type = selectionArgs[1];
                    MovieDao movieDao = MovieLibraryRoomDatabase.getDatabase(getContext()).getMovieDao();
                    MovieVideofileCrossRefDao movieVideofileCrossRefDao = MovieLibraryRoomDatabase.getDatabase(getContext()).getMovieVideofileCrossRefDao();
                    List<Movie> movieList = movieDao.queryByMovieIdAndType(movie_id, type);
                    for (Movie movie : movieList) {
                        movieVideofileCrossRefDao.deleteById(movie.id);
                    }
                    movieDao.updateFavoriteStateByMovieId(movie_id, type, false);//电影的收藏状态在删除时要设置为false
                    sendRemoveMovie(movie_id, type);
                    return 1;
                }
                break;
        }
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        int code = matcher.match(uri);
        switch (code) {
            case FAVORITE:
                if (selectionArgs != null && selectionArgs.length > 0) {
                    String movie_id = selectionArgs[0];
                    boolean is_favorite = values.getAsBoolean("is_favorite");
                    String type = values.getAsString("type");
                    int res = mMovieDao.updateFavoriteStateByMovieId(movie_id, type, is_favorite);
                    sendRefreshFavoriteBroadcast(movie_id, type, is_favorite);
                    return res;
                }
                break;
            case APP_UPDATE_MOVIE:
                if (selectionArgs != null && selectionArgs.length > 0) {
                    String path = selectionArgs[0];
                    String movie_id = values.getAsString("movie_id");
                    String type = values.getAsString("type");

                    Movie oldMovie = mMovieDao.queryByFilePath(path, ScraperSourceTools.getSource());
                    if (oldMovie != null) {
                        mMovieDao.updateFavoriteStateByMovieId(oldMovie.movieId, type, false);//电影的收藏状态在删除时要设置为false
                    }

                    long timeStamp = System.currentTimeMillis();

                    appUpdateMovie(path, movie_id, type, Constants.Scraper.TMDB, timeStamp);
                    appUpdateMovie(path, movie_id, type, Constants.Scraper.TMDB_EN, timeStamp);
                    return 1;
                }
                break;
            case APP_PRE_MATCH:
                String movieId = values.getAsString("movie_id");
                String type = values.getAsString("type");
                String downloadPath = values.getAsString("download_path");
                String videoFileStr = values.getAsString("file_list");
                preMatchMovie(movieId, type, downloadPath, videoFileStr);

                break;

        }
        return -1;
    }


    /**
     * 根据提供的文件路径查询当前对应旧电影的其他文件列表，然后需要先查询更新的新电影是否已经存在，不存在需要重新获取详情信息，否则直接更改对应关系即可
     */
    private void appUpdateMovie(String path, String movie_id, String type, String source, long timeStamp) {
        MovieVideoFileCrossRef movieVideoFileCrossRef = mMovieVideofileCrossRefDao.queryByPath(path, source);
        Movie new_movie = mMovieDao.queryByMovieIdAndType(movie_id, source, type);

        //1 已匹配电影重新匹配海报
        if (movieVideoFileCrossRef != null) {
            long old_id = movieVideoFileCrossRef.id;
            List<VideoFile> videoFileList = mVideoFileDao.queryVideoFilesById(old_id);
            if (new_movie != null) {
                for (VideoFile videoFile : videoFileList) {
                    MovieVideoFileCrossRef tmpRef = mMovieVideofileCrossRefDao.queryByPath(videoFile.path, source);
                    tmpRef.id = new_movie.id;
                    tmpRef.timeStamp = timeStamp;
                    mMovieVideofileCrossRefDao.update(tmpRef);
                }
                if (source.equals(ScraperSourceTools.getSource()))
                    sendRefreshMovie(old_id, new_movie.id);

            } else {
                MovieWrapper wrapper = TmdbApiService.getDetail(movie_id, source, type)
                        .blockingFirst().toEntity();
                //被动更新无需通知
                MovieHelper.manualSaveMovie(getContext(), wrapper, videoFileList, false);
                if (source.equals(ScraperSourceTools.getSource()))
                    sendRefreshMovie(old_id, wrapper.movie.id);
            }


        } else {
            //2 未匹配电影匹配
            if (new_movie != null) {
                MovieVideoFileCrossRef tmpRef = new MovieVideoFileCrossRef();
                tmpRef.id = new_movie.id;
                tmpRef.source = source;
                tmpRef.path = path;
                tmpRef.timeStamp = timeStamp;
                mMovieVideofileCrossRefDao.insertOrReplace(tmpRef);
                if (source.equals(ScraperSourceTools.getSource()))
                    sendRefreshMovie(-1, new_movie.id);
            } else {
                List<VideoFile> videoFileList = mVideoFileDao.queryByPaths(path);
                MovieWrapper wrapper = TmdbApiService.getDetail(movie_id, source, type)
                        .blockingFirst().toEntity();
                //被动更新无需通知
                MovieHelper.manualSaveMovie(getContext(), wrapper, videoFileList, false);
                if (source.equals(ScraperSourceTools.getSource()))
                    sendRefreshMovie(-1, wrapper.movie.id);
            }

        }


    }

    /**
     * 根据下载器提供的信息预匹配海报
     *
     * @param movieId
     * @param type
     * @param downloadPath
     * @param videoFileStr
     * @return
     */
    private int preMatchMovie(String movieId, String type, String downloadPath, String videoFileStr) {
        //1、整理信息
        String shortcutUri = new File(downloadPath).getAbsolutePath();
        String shortcutName = shortcutUri.substring(shortcutUri.lastIndexOf("/") + 1);
        String[] videoFilePaths = videoFileStr.split("\\|");

        //2.1、添加下载文件夹到监听索引
        ShortcutDao shortcutDao = MovieLibraryRoomDatabase.getDatabase(getContext()).getShortcutDao();
        Shortcut shortcut = shortcutDao.queryShortcutByUri(shortcutUri);
        String devicePath = null;
        if (shortcut == null) {
            shortcut = new Shortcut(shortcutUri, Constants.DeviceType.DEVICE_TYPE_LOCAL, shortcutName, shortcutName, shortcutUri);
            shortcut.access = Constants.WatchLimit.ALL_AGE;
            shortcut.folderType = Constants.SearchType.auto;
            shortcut.isScanned = 1;
            DeviceDao deviceDao = MovieLibraryRoomDatabase.getDatabase(getContext()).getDeviceDao();
            List<Device> deviceList = deviceDao.qureyAll();
            for (Device device : deviceList) {
                if (shortcutUri.startsWith(device.path)) {
                    shortcut.devicePath = device.path;
                    shortcut.deviceType = device.type;
                    devicePath = device.path;
                    shortcutDao.insertShortcut(shortcut);
                    break;
                }
            }
        } else {
            shortcut.isScanned = 1;
            devicePath = shortcut.devicePath;
            shortcutDao.updateShortcut(shortcut);
        }

        //2.2、创建文件列表
        List<VideoFile> videoFileList = new ArrayList<>();
        VideoFileDao videoFileDao = MovieLibraryRoomDatabase.getDatabase(getContext()).getVideoFileDao();
        for (String videoFilePath : videoFilePaths) {
            VideoFile videoFile = new VideoFile();
            videoFile.filename = videoFilePath.substring(videoFilePath.lastIndexOf("/") + 1);
            videoFile.path = videoFilePath;
            videoFile.dirPath = shortcutUri;
            videoFile.devicePath = devicePath;
            VideoNameParser2 parser = new VideoNameParser2();
            MovieNameInfo nameInfo = parser.parseVideoName(videoFile.path);
            String keyword = nameInfo.getName();
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
            //2.3、添加文件入数据库
            long vid = videoFileDao.insertOrIgnore(videoFile);
            if (vid < 0) {
                videoFile = videoFileDao.queryByPath(videoFilePath);
            } else {
                videoFile.vid = vid;
            }
            videoFileList.add(videoFile);
        }

        //3.查询/插入电影
        MovieWrapper wrapper = mMovieDao.queryWrapperByMovieIdAndType(movieId, Constants.Scraper.TMDB, type);
        if (wrapper == null)
            TmdbApiService.getDetail(movieId, Constants.Scraper.TMDB, type)
                    .subscribe(new SimpleObserver<MovieDetailRespone>() {
                        @Override
                        public void onAction(MovieDetailRespone movieDetailRespone) {
                            MovieWrapper TMDBWrapper = movieDetailRespone.toEntity();
                            MovieHelper.manualSaveMovie(getContext(), TMDBWrapper, videoFileList);
                        }
                    });
        else MovieHelper.manualSaveMovie(getContext(), wrapper, videoFileList);


        MovieWrapper wrapper_en = mMovieDao.queryWrapperByMovieIdAndType(movieId, Constants.Scraper.TMDB_EN, type);
        if (wrapper_en == null)
            TmdbApiService.getDetail(movieId, Constants.Scraper.TMDB_EN, type)
                    .subscribe(new SimpleObserver<MovieDetailRespone>() {
                        @Override
                        public void onAction(MovieDetailRespone movieDetailRespone) {
                            MovieWrapper TMDBENWrapper = movieDetailRespone.toEntity();
                            MovieHelper.manualSaveMovie(getContext(), TMDBENWrapper, videoFileList);
                        }
                    });
        else MovieHelper.manualSaveMovie(getContext(), wrapper_en, videoFileList);
        return 1;
    }

    private void sendRefreshFavoriteBroadcast(String movie_id, String type, boolean isFavorite) {
        LogUtil.v(TAG, "Broadcast:" + Constants.ACTION_FAVORITE_MOVIE_CHANGE + " ->" + movie_id);
        Intent intent = new Intent();
        intent.setAction(Constants.ACTION_FAVORITE_MOVIE_CHANGE);
        intent.putExtra("movie_id", movie_id);
        intent.putExtra("is_favorite", isFavorite);
        intent.putExtra("type", type);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
    }

    private void sendRefreshMovie(long old_id, long new_id) {
        LogUtil.v(TAG, "Broadcast:" + Constants.ACTION_APP_UPDATE_MOVIE + old_id + " ->" + new_id);
        Intent intent = new Intent();
        intent.setAction(Constants.ACTION_APP_UPDATE_MOVIE);
        intent.putExtra("old", old_id);
        intent.putExtra("new", new_id);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
    }

    private void sendRemoveMovie(String movie_id, String type) {
        LogUtil.v(TAG, "Broadcast:" + Constants.ACTION_APP_REMOVE_MOVIE + " :" + movie_id + " ->" + type);
        Intent intent = new Intent();
        intent.setAction(Constants.ACTION_APP_REMOVE_MOVIE);
        intent.putExtra("movie_id", movie_id);
        intent.putExtra("type", type);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
    }
}
