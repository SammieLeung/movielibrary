package com.hphtv.movielibrary.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.sqlite.db.SupportSQLiteOpenHelper;

import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.MovieDao;
import com.hphtv.movielibrary.roomdb.dao.VideoFileDao;
import com.hphtv.movielibrary.roomdb.entity.VideoFile;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.hphtv.movielibrary.roomdb.entity.relation.MovieWrapper;
import com.hphtv.movielibrary.util.ScraperSourceTools;
import com.station.kit.util.LogUtil;
import com.station.kit.util.RegexMatcher;

import java.util.List;
import java.util.regex.Matcher;

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

    MovieDao mMovieDao;
    VideoFileDao mVideoFileDao;
    UriMatcher matcher;
    private SupportSQLiteOpenHelper mSupportSQLiteOpenHelper;

    @Override
    public boolean onCreate() {
        MovieLibraryRoomDatabase database = MovieLibraryRoomDatabase.getDatabase(getContext());
        mSupportSQLiteOpenHelper = database.getOpenHelper();
        mMovieDao = database.getMovieDao();
        mVideoFileDao = database.getVideoFileDao();
        matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(AUTHORITY, "movie_list", MOVIE_LIST);
        matcher.addURI(AUTHORITY, "file_list", FILE_LIST);
        matcher.addURI(AUTHORITY, "count", MOVIE_TOTAL);
        matcher.addURI(AUTHORITY, "favorite", FAVORITE);

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
            cursor.addRow(new Object[]{dataView.movie_id, dataView.source, dataView.is_favorite==true?1:0});
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
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        int code = matcher.match(uri);
        String source = ScraperSourceTools.getSource();
        switch (code) {
            case FAVORITE:
                if (selectionArgs != null && selectionArgs.length > 0) {
                    String movie_id = selectionArgs[0];
                    boolean is_favorite = values.getAsBoolean("is_favorite");
                    int res = mMovieDao.updateFavoriteByMovieId(is_favorite, movie_id);
                    sendRefreshFavoriteBroadcast(movie_id,is_favorite);
                    return res;
                }
        }
        return -1;
    }

    private void sendRefreshFavoriteBroadcast(String movie_id,boolean isFavorite) {
        LogUtil.v(TAG, "Broacast:" + Constants.ACTION_FAVORITE_MOVIE_CHANGE + " ->" + movie_id);
        Intent intent = new Intent();
        intent.setAction(Constants.ACTION_FAVORITE_MOVIE_CHANGE);
        intent.putExtra("movie_id", movie_id);
        intent.putExtra("is_favorite",isFavorite);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
    }
}
