package com.hphtv.movielibrary.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.station.kit.util.SharePreferencesTools;
import com.hphtv.movielibrary.data.Constants;

/**
 * Created by tchip on 18-5-15.
 */

public class PosterContentProvider extends ContentProvider {
    String[] COLUMN_NAME = new String[]{"poster"};
    UriMatcher matcher;

    public static final int ONE_PHOTO = 1;
    public static final int LOCAL_CACHE = 2;

    @Override
    public boolean onCreate() {
        matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI("com.hphtv.movielibrary", "poster", ONE_PHOTO);
        return false;
    }

    private Cursor createPhotoCursor() {
        String poster = SharePreferencesTools.getInstance(getContext()).readProperty(Constants.SharePreferenceKeys.LAST_POTSER, "");
        MatrixCursor matrixCursor = new MatrixCursor(COLUMN_NAME);
        matrixCursor.addRow(new String[]{poster});
        return matrixCursor;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        int code = matcher.match(uri);
        if (code == ONE_PHOTO) {
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
        return 0;
    }
}
