package com.hphtv.movielibrary.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.sqlite.db.SupportSQLiteOpenHelper;

import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.MovieDao;
import com.hphtv.movielibrary.roomdb.dao.VideoFileDao;

/**
 * Created by tchip on 18-5-15.
 */

public class MovieLibraryProvider extends ContentProvider {
    MovieDao mMovieDao;
    VideoFileDao mVideoFileDao;
    UriMatcher matcher;
    private SupportSQLiteOpenHelper mSupportSQLiteOpenHelper;
    public static final String AUTHORITY = "com.hphtv.movielibrary.movieprovider";

    public static final int ALL_MOVIE_WRAPPER = 1;
    public static final int MOVIE_INFO = 2;
    public static final int FILE_LIST = 4;
    public static final int PLAYVIDEO = 3;
    public static final int GENRES = 5;
    public static final int MOVIE_NUMBERS = 6;
    public static final int WRAPPER_TITLE = 7;
    public static final int MOVIE_WRAPPER = 8;
    public static final int FAVORITE = 9;
    public static final int PLAYVIDEO2 = 10;

    public static final int MOVIE_DATAVIEW_LIST = 11;

    private int api_version;

    @Override
    public boolean onCreate() {
        MovieLibraryRoomDatabase database = MovieLibraryRoomDatabase.getDatabase(getContext());
        mSupportSQLiteOpenHelper = database.getOpenHelper();
        mMovieDao = database.getMovieDao();
        mVideoFileDao = database.getVideoFileDao();
        matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(AUTHORITY, "wrapper", ALL_MOVIE_WRAPPER);
        matcher.addURI(AUTHORITY, "movie_info/#", MOVIE_INFO);
        matcher.addURI(AUTHORITY, "file/wrapper_id/#", FILE_LIST);
        matcher.addURI(AUTHORITY, "play/vid/#", PLAYVIDEO);
        matcher.addURI(AUTHORITY, "play2/vid/#", PLAYVIDEO2);
        matcher.addURI(AUTHORITY, "genres", GENRES);
        matcher.addURI(AUTHORITY, "movie_numbers", MOVIE_NUMBERS);
        matcher.addURI(AUTHORITY, "wrapper_title", WRAPPER_TITLE);
        matcher.addURI(AUTHORITY, "wrapper/#", MOVIE_WRAPPER);
        matcher.addURI(AUTHORITY, "favorite/#", FAVORITE);
        return false;
    }


    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        int code = matcher.match(uri);
        api_version = Constants.Scraper.MTIME;
        Cursor cursor = null;
        switch (code) {
            case ALL_MOVIE_WRAPPER:
                break;
            case FILE_LIST:
                break;
            case MOVIE_INFO:
                break;
            case PLAYVIDEO:
                break;
            case GENRES:
                break;
            case MOVIE_NUMBERS:
                break;
            case WRAPPER_TITLE:
                break;
            case MOVIE_WRAPPER:
                break;
            case FAVORITE:
                break;
            case PLAYVIDEO2:
                break;
            case MOVIE_DATAVIEW_LIST:
                cursor= getAllMovieDataViews();
                break;
        }
        return cursor;
    }

    public Cursor getAllMovieDataViews() {
        return mSupportSQLiteOpenHelper.getWritableDatabase().query("SELECT M.movie_id,M.path,M.source FROM movie_dataview");
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
        return 0;
    }



}
