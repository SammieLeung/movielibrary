package com.hphtv.movielibrary.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.VideoFileDao;
import com.hphtv.movielibrary.roomdb.entity.VideoFile;
import com.hphtv.movielibrary.util.ScraperSourceTools;
import com.station.kit.util.SharePreferencesTools;
import com.hphtv.movielibrary.data.Constants;

/**
 * Created by tchip on 18-5-15.
 */

public class PosterContentProvider extends ContentProvider {

    private UriMatcher matcher;
    private VideoFileDao mVideoFileDao;

    public static final int POSTER = 1;
    public static final int LOCAL_CACHE = 2;

    @Override
    public boolean onCreate() {
        MovieLibraryRoomDatabase database = MovieLibraryRoomDatabase.getDatabase(getContext());
        mVideoFileDao = database.getVideoFileDao();
        matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI("com.hphtv.movielibrary", "poster", POSTER);
        return false;
    }

    private Cursor createPhotoCursor() {
        String poster = SharePreferencesTools.getInstance(getContext()).readProperty(Constants.SharePreferenceKeys.LAST_POTSER, "");
        MatrixCursor matrixCursor = new MatrixCursor(new String[]{"poster"});
        matrixCursor.addRow(new String[]{poster});
        return matrixCursor;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        int code = matcher.match(uri);
        if (code == POSTER) {
            return createPhotoCursor();
        }
        return null;
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
            case POSTER:
                String path= values.getAsString("path");
                VideoFile videoFile = mVideoFileDao.queryByPath(path);
                mVideoFileDao.updateLastPlaytime(path, System.currentTimeMillis());
                String poster = mVideoFileDao.getPoster(videoFile.path, source);
                SharePreferencesTools.getInstance(getContext()).saveProperty(Constants.SharePreferenceKeys.LAST_POTSER,poster);
                return 1;
        }
        return -1;
    }
}
