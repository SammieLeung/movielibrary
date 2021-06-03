package com.hphtv.movielibrary.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hphtv.movielibrary.sqlite.dao.MovieWrapperDao;
import com.hphtv.movielibrary.sqlite.dao.PosterProviderDao;

/**
 * Created by tchip on 18-5-15.
 */

public class PosterContentProvider extends ContentProvider {
    MovieWrapperDao mMovieWrapperDao;
    PosterProviderDao posterProviderDao;
    UriMatcher matcher;

    public static final int ONE_PHOTO = 1;
    public static final int LOCAL_CACHE = 2;

    @Override
    public boolean onCreate() {
        mMovieWrapperDao = new MovieWrapperDao(getContext());
        posterProviderDao = new PosterProviderDao(getContext());
        matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI("com.hphtv.movielibrary", "poster", ONE_PHOTO);
//        matcher.addURI("com.hphtv.movielibrary", "poster_cache/#", LOCAL_CACHE);
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        int code = matcher.match(uri);
        if (code == ONE_PHOTO) {
            Cursor posterCursor = posterProviderDao.selectAll();
            return posterCursor;
        }
//        else if (code == LOCAL_CACHE)
//        {
//            long id = ContentUris.parseId(uri);
//            Cursor cursor = posterProviderDao.selectAll();
//            if (cursor.getCount() < 0) {
//                Cursor movieWrapperCursor = mMovieWrapperDao.selectAll();
//                List<MovieWrapper> movieList = mMovieWrapperDao.parseList(movieWrapperCursor);
//                for (MovieWrapper movie : movieList) {
//                    String poster = null;
//                    movie
//                    if (movie.getImages().large != null) {
//                        poster = movie.getImages().large;
//                    } else if (movie.getImages().medium != null) {
//                        poster = movie.getImages().medium;
//                    } else if (movie.getImages().small != null) {
//                        poster = movie.getImages().small;
//                    }
//                    if (poster != null) {
//                        PosterProviderBean posterProviderBean = new PosterProviderBean();
//                        posterProviderBean.setPoster(poster);
//                        ContentValues values = posterProviderDao.parseContentValues(posterProviderBean);
//                        posterProviderDao.insert(values);
//                    }
//                }
//                cursor = posterProviderDao.selectAll();
//            }
//
//        }
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
