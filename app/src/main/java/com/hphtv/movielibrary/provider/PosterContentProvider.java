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
import com.hphtv.movielibrary.roomdb.dao.MovieDao;
import com.hphtv.movielibrary.roomdb.dao.VideoFileDao;
import com.hphtv.movielibrary.roomdb.entity.Movie;
import com.hphtv.movielibrary.roomdb.entity.VideoFile;
import com.hphtv.movielibrary.scraper.service.OnlineDBApiService;
import com.hphtv.movielibrary.util.ScraperSourceTools;
import com.station.kit.util.LogUtil;
import com.station.kit.util.SharePreferencesTools;
import com.hphtv.movielibrary.data.Constants;

/**
 * Created by tchip on 18-5-15.
 */

public class PosterContentProvider extends ContentProvider {

    private UriMatcher matcher;

    public static final int POSTER = 1;
    public static final int LOCAL_CACHE = 2;

    @Override
    public boolean onCreate() {
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
            Uri aliveUri=Uri.parse("content://com.android.alive.tvremote/keepAlive");
            Cursor cursor=getContext().getContentResolver().query(aliveUri,null,null,null,null);
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

                VideoFileDao videoFileDao = MovieLibraryRoomDatabase.getDatabase(getContext()).getVideoFileDao();
                MovieDao movieDao = MovieLibraryRoomDatabase.getDatabase(getContext()).getMovieDao();

                long currentTime = System.currentTimeMillis();
                videoFileDao.updateLastPlaytime(path, currentTime);

                String poster = videoFileDao.getPoster(path, source);
                Movie movie = movieDao.queryByFilePath(path, ScraperSourceTools.getSource());
                if (movie != null)
                    movieDao.updateLastPlaytime(movie.movieId, currentTime);
                SharePreferencesTools.getInstance(getContext()).saveProperty(Constants.SharePreferenceKeys.LAST_POTSER,poster);
                OnlineDBApiService.updateHistory(path, ScraperSourceTools.getSource());
                return 1;
        }
        return -1;
    }
}
