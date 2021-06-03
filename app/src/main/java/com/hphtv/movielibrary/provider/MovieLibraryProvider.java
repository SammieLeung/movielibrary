package com.hphtv.movielibrary.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hphtv.movielibrary.sqlite.bean.History;
import com.hphtv.movielibrary.sqlite.bean.MovieWrapper;
import com.hphtv.movielibrary.sqlite.bean.PosterProviderBean;
import com.hphtv.movielibrary.sqlite.bean.VideoFile;
import com.hphtv.movielibrary.data.ConstData;
import com.hphtv.movielibrary.sqlite.MovieDBHelper;
import com.hphtv.movielibrary.sqlite.dao.FavoriteDao;
import com.hphtv.movielibrary.sqlite.dao.HistoryDao;
import com.hphtv.movielibrary.sqlite.dao.MovieDao;
import com.hphtv.movielibrary.sqlite.dao.MovieWrapperDao;
import com.hphtv.movielibrary.sqlite.dao.PosterProviderDao;
import com.hphtv.movielibrary.sqlite.dao.VideoFileDao;
import com.hphtv.movielibrary.util.VideoPlayTools;

/**
 * Created by tchip on 18-5-15.
 */

public class MovieLibraryProvider extends ContentProvider {
    MovieDBHelper dbHelper;
    MovieDao mMovieDao;
    VideoFileDao mVideoFileDao;
    MovieWrapperDao mWrapperDao;
    FavoriteDao mFavoriteDao;
    UriMatcher matcher;
    private String prefix = "com.hphtv.movielibrary.movieprovider";

    public static final int ALL_MOVIE_WRAPPER = 1;
    public static final int MOVIE_INFO = 2;
    public static final int FILE_LIST = 4;
    public static final int PLAYVIDEO = 3;
    public static final int GENRES = 5;
    public static final int MOVIE_NUMBERS = 6;
    public static final int WRAPPER_TITLE = 7;
    public static final int MOVIE_WRAPPER = 8;
    public static final int FAVORITE = 9;

    private int api_version;

    @Override
    public boolean onCreate() {
        dbHelper = MovieDBHelper.getInstance(getContext());
        mMovieDao = new MovieDao(getContext());
        mVideoFileDao = new VideoFileDao(getContext());
        mWrapperDao = new MovieWrapperDao(getContext());
        mFavoriteDao = new FavoriteDao(getContext());
        MovieSharedPreferences.getInstance().setContext(getContext());
        matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(prefix, "wrapper", ALL_MOVIE_WRAPPER);
        matcher.addURI(prefix, "movie_info/#", MOVIE_INFO);
        matcher.addURI(prefix, "file/wrapper_id/#", FILE_LIST);
        matcher.addURI(prefix, "play/vid/#", PLAYVIDEO);
        matcher.addURI(prefix, "genres", GENRES);
        matcher.addURI(prefix, "movie_numbers", MOVIE_NUMBERS);
        matcher.addURI(prefix, "wrapper_title", WRAPPER_TITLE);
        matcher.addURI(prefix, "wrapper/#", MOVIE_WRAPPER);
        matcher.addURI(prefix, "favorite/#", FAVORITE);
        return false;
    }


    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        int code = matcher.match(uri);
        api_version = ConstData.Scraper.MTIME;
        Cursor reslut = null;
        switch (code) {
            case ALL_MOVIE_WRAPPER:
                reslut = getAllMovieWrapper();
                break;
            case FILE_LIST:
                reslut = getFileList(uri);
                break;
            case MOVIE_INFO:
                reslut = getMovieDetail(uri);
                break;
            case PLAYVIDEO:
                reslut = playVideo(uri);
                break;
            case GENRES:
                reslut = getGenres(selection, selectionArgs);
                break;
            case MOVIE_NUMBERS:
                reslut = getMovieCount();
                break;
            case WRAPPER_TITLE:
                reslut = getMovieWrapperTitle(selection, selectionArgs);
                break;
            case MOVIE_WRAPPER:
                reslut = getMovieWrapper(uri);
                break;
            case FAVORITE:
                reslut = getFavorite(uri);
                break;

        }
        return reslut;
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

    private Cursor getAllMovieWrapper() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String asc = "asc";
        db.beginTransaction();
        Cursor cursor = mWrapperDao.select(null, null, null, null, null, "title_pinyin " + asc + ",title " + asc, null);
        db.setTransactionSuccessful();
        db.endTransaction();
        return cursor;
    }

    private Cursor getMovieWrapper(Uri uri) {
        long id = ContentUris.parseId(uri);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        Cursor cursor = mWrapperDao.select("id=?", new String[]{String.valueOf(id)}, null);
        db.setTransactionSuccessful();
        db.endTransaction();
        return cursor;
    }

    private Cursor getFileList(Uri uri) {
        long id = ContentUris.parseId(uri);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        Cursor cursor = mVideoFileDao.select("wrapper_id=?", new String[]{String.valueOf(id)}, null);
        db.setTransactionSuccessful();
        db.endTransaction();
        return cursor;
    }

    private Cursor getMovieDetail(Uri uri) {
        long id = ContentUris.parseId(uri);
        Cursor wrapperCursor = mWrapperDao.select("id=?", new String[]{String.valueOf(id)}, null);
        if (wrapperCursor != null && wrapperCursor.getCount() > 0 && wrapperCursor.moveToNext()) {
            String scrpaer_infos = wrapperCursor.getString(wrapperCursor.getColumnIndex("scraper_infos"));
            if (!TextUtils.isEmpty(scrpaer_infos) && !scrpaer_infos.equalsIgnoreCase("null")) {
                JSONArray jsonArray = JSON.parseArray(scrpaer_infos);
                if (jsonArray.size() > 0) {
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    int movie_id = jsonObject.getInteger("id");
                    Cursor cursor = mMovieDao.select("id=?", new String[]{String.valueOf(movie_id)}, null);
                    return cursor;
                }
            }
        }
        return null;
    }

    private Cursor playVideo(Uri uri) {
        long id = ContentUris.parseId(uri);
        Cursor cursor = mVideoFileDao.select("id=?", new String[]{String.valueOf(id)}, null);
        final VideoFile file = (new VideoFileDao(getContext())).parseList(cursor).get(0);
        long wrapper_id = file.getWrapper_id();
        if (wrapper_id > 0) {
            MovieWrapperDao dao = new MovieWrapperDao(getContext());
            PosterProviderDao posterProviderDao = new PosterProviderDao(getContext());
            Cursor m_cursor = dao.select("id=?", new String[]{String.valueOf(wrapper_id)}, null);
            if (m_cursor != null && m_cursor.getCount() > 0) {
                MovieWrapper wrapper = dao.parseList(m_cursor).get(0);
                String poster = wrapper.getPoster();
                if (poster != null) {
                    posterProviderDao.deleteAll();
                    PosterProviderBean posterProviderBean = new PosterProviderBean();
                    posterProviderBean.setPoster(poster);
                    ContentValues values = posterProviderDao.parseContentValues(posterProviderBean);
                    posterProviderDao.insert(values);
                }
            }

            HistoryDao historyDao = new HistoryDao(getContext());
            Cursor historyCursor = historyDao.select("wrapper_id=?", new String[]{String.valueOf(wrapper_id)}, null);
            if (historyCursor.getCount() > 0) {
                long currentTime = System.currentTimeMillis();
                History history = historyDao.parseList(historyCursor).get(0);
                history.setLast_play_time(String.valueOf(currentTime));
                ContentValues contentValues = historyDao.parseContentValues(history);
                historyDao.update(contentValues, "id=?", new String[]{String.valueOf(history.getId())});
            } else {
                long currentTime = System.currentTimeMillis();
                History history = new History();
                history.setWrapper_id(file.getId());
                history.setTime("0");
                history.setLast_play_time(String.valueOf(currentTime));
                ContentValues contentValues = historyDao.parseContentValues(history);
                historyDao.insert(contentValues);
            }
        } else {
            PosterProviderDao posterProviderDao = new PosterProviderDao(getContext());
            String poster = file.getThumbnail();
            if (poster != null) {
                posterProviderDao.deleteAll();
                PosterProviderBean posterProviderBean = new PosterProviderBean();
                posterProviderBean.setPoster(poster);
                ContentValues values = posterProviderDao.parseContentValues(posterProviderBean);
                posterProviderDao.insert(values);
            }
            HistoryDao historyDao = new HistoryDao(getContext());
            Cursor historyCursor = historyDao.select("wrapper_id=?", new String[]{String.valueOf(wrapper_id)}, null);
            if (historyCursor.getCount() > 0) {
                long currentTime = System.currentTimeMillis();
                History history = historyDao.parseList(historyCursor).get(0);
                history.setLast_play_time(String.valueOf(currentTime));
                ContentValues contentValues = historyDao.parseContentValues(history);
                historyDao.update(contentValues, "id=?", new String[]{String.valueOf(history.getId())});
            } else {
                long currentTime = System.currentTimeMillis();
                History history = new History();
                history.setTime("0");
                history.setLast_play_time(String.valueOf(currentTime));
                history.setWrapper_id(wrapper_id);
                ContentValues contentValues = historyDao.parseContentValues(history);
                historyDao.insert(contentValues);
            }
        }
        VideoPlayTools.play(getContext(), file);
        return cursor;
    }

    private Cursor getGenres(String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        Cursor cursor = db.query(MovieDBHelper.TABLE_GENRES, null, selection, selectionArgs, null, null, null);
        db.setTransactionSuccessful();
        db.endTransaction();
        return cursor;
    }

    private Cursor getMovieCount() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        Cursor cursor = db.rawQuery("select count(id) from " + MovieDBHelper.TABLE_MOVIEWRAPPER, null);
        db.setTransactionSuccessful();
        db.endTransaction();
        return cursor;
    }

    private Cursor getMovieWrapperTitle(String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        Cursor cursor = db.query(MovieDBHelper.TABLE_VIDEOFILE, new String[]{"wrapper_id"}, selection, selectionArgs, null, null, null);
        Cursor wrapperCursor = null;
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            int wrapper_id = cursor.getInt(cursor.getColumnIndex("wrapper_id"));
            wrapperCursor = db.query(MovieDBHelper.TABLE_MOVIEWRAPPER, new String[]{"title", "poster"}, "id=?", new String[]{String.valueOf(wrapper_id)}, null, null, null);
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        return wrapperCursor;
    }

    private Cursor getFavorite(Uri uri) {
        long id = ContentUris.parseId(uri);
        Cursor cursor = mFavoriteDao.select("wrapper_id=?", new String[]{String.valueOf(id)}, null);
        return cursor;
    }


}
