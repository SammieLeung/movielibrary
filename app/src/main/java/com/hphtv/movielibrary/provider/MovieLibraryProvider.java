package com.hphtv.movielibrary.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.hphtv.movielibrary.sqlite.bean.MovieWrapper;
import com.hphtv.movielibrary.sqlite.bean.PosterProviderBean;
import com.hphtv.movielibrary.sqlite.bean.VideoFile;
import com.hphtv.movielibrary.data.ConstData;
import com.hphtv.movielibrary.scraper.Scraper;
import com.hphtv.movielibrary.sqlite.MovieDBHelper;
import com.hphtv.movielibrary.sqlite.dao.MovieDao;
import com.hphtv.movielibrary.sqlite.dao.MovieWrapperDao;
import com.hphtv.movielibrary.sqlite.dao.PosterProviderDao;
import com.hphtv.movielibrary.sqlite.dao.VideoFileDao;
import com.hphtv.movielibrary.util.MovieSharedPreferences;
import com.hphtv.movielibrary.util.UriParseUtil;

/**
 * Created by tchip on 18-5-15.
 */

public class MovieLibraryProvider extends ContentProvider {
    MovieDBHelper dbHelper;
    MovieDao movieDao;
    VideoFileDao videoFileDao;
    UriMatcher matcher;
    private String prefix = "com.hphtv.movielibrary.movieprovider";

    public static final int MOVIELIST = 1;
    public static final int MOVIE = 2;
    public static final int FILELIST = 3;
    public static final int FILE = 4;
    public static final int UNMATCHFILE = 5;
    public static final int PLAYVIDEO = 6;
    private int api_version;

    @Override
    public boolean onCreate() {
        dbHelper = MovieDBHelper.getInstance(getContext());
        movieDao = new MovieDao(getContext());
        videoFileDao = new VideoFileDao(getContext());
        MovieSharedPreferences.getInstance().setContext(getContext());
        api_version=Scraper.getApiVersion();
        matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(prefix, "allmovie", MOVIELIST);
        matcher.addURI(prefix, "movie/#", MOVIE);
        matcher.addURI(prefix, "file/movieId/#", FILELIST);
        matcher.addURI(prefix, "unmatchmovie", UNMATCHFILE);
        matcher.addURI(prefix, "file/#", FILE);
        matcher.addURI(prefix, "play/vid/#", PLAYVIDEO);
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        int code = matcher.match(uri);
        String api_clause="";
        switch (api_version) {
            case ConstData.Scraper.DOUBAN:
                api_clause="movie_id";
                break;
            case ConstData.Scraper.IMDB:
                api_clause="imdb_movie_id";
                break;
            case ConstData.Scraper.MTIME:
                api_clause="mtime_movie_id";
                break;
        }
        if (code == MOVIELIST) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.beginTransaction();
            Log.v("TAG","api_version="+api_version);
            String queryStr="select tb_movie.* from tb_movie where id in (select "+api_clause +" from tb_videofile where "+api_clause+"!=-1) and api_version="+api_version+" order by tb_movie.title_pinyin";
            Log.v("TAG","queryStr="+queryStr);
            Cursor cursor = db.rawQuery(queryStr, null);
            db.setTransactionSuccessful();
            db.endTransaction();
            Log.v("TAG","count="+cursor.getCount());
            return cursor;
        } else if (code == MOVIE) {
            long id = ContentUris.parseId(uri);
            Cursor cursor = movieDao.select("id=?", new String[]{String.valueOf(id)}, null);
            return cursor;
        } else if (code == FILELIST) {
            long id = ContentUris.parseId(uri);
            Cursor cursor = videoFileDao.select(api_clause+"=?", new String[]{String.valueOf(id)}, null);
            return cursor;
        } else if (code == UNMATCHFILE) {
            Cursor cursor = videoFileDao.select(api_clause+"=?", new String[]{"-1"}, null);
            return cursor;
        } else if (code == FILE) {
            long id = ContentUris.parseId(uri);
            Cursor cursor = videoFileDao.select("id=?", new String[]{String.valueOf(id)}, null);
            return cursor;
        } else if (code == PLAYVIDEO) {
            long id = ContentUris.parseId(uri);
            Cursor cursor = videoFileDao.select("id=?", new String[]{String.valueOf(id)}, null);
            final VideoFile file = (new VideoFileDao(getContext())).parseList(cursor).get(0);
            Uri fileuri = null;
            String path = file.getUri();
            if (path != null && path.startsWith("/")) {
                fileuri = UriParseUtil.parseVideoUri(getContext(), path);
            }

            if (fileuri == null) {
                fileuri = Uri.parse(path);
                if (fileuri == null) {
                    Toast.makeText(getContext(), "can't find the file", Toast.LENGTH_SHORT).show();
                    return null;
                }

            }
            long wrapper_id=file.getWrapper_id();
            if (wrapper_id>0) {
                MovieWrapperDao dao = new MovieWrapperDao(getContext());
                PosterProviderDao posterProviderDao = new PosterProviderDao(getContext());
                Cursor m_cursor = dao.select("id=?", new String[]{String.valueOf(wrapper_id)}, null);
                if(m_cursor!=null&&m_cursor.getCount()>0){
                    MovieWrapper wrapper=dao.parseList(m_cursor).get(0);
                    String poster = wrapper.getPoster();
                    if (poster != null) {
                        posterProviderDao.deleteAll();
                        PosterProviderBean posterProviderBean = new PosterProviderBean();
                        posterProviderBean.setPoster(poster);
                        ContentValues values = posterProviderDao.parseContentValues(posterProviderBean);
                        posterProviderDao.insert(values);
                    }
                }

//                HistoryDao historyDao = new HistoryDao(getContext());
//                Cursor historyCursor = historyDao.select("movie_id=? and video_file_id=?", new String[]{movie_id, String.valueOf(file.getId())}, null);
//                if (historyCursor.getCount() > 0) {
//                    long currentTime = System.currentTimeMillis();
//                    History history = historyDao.parseList(historyCursor).get(0);
//                    history.setLast_play_time(String.valueOf(currentTime));
//                    ContentValues contentValues = historyDao.parseContentValues(history);
//                    historyDao.update(contentValues, "id=?", new String[]{String.valueOf(history.getId())});
//                } else {
//                    long currentTime = System.currentTimeMillis();
//                    History history = new History();
//                    history.setVideo_file_id(file.getId());
//                    history.setTime("0");
//                    history.setLast_play_time(String.valueOf(currentTime));
//                    history.setMovieId(Integer.parseInt(movie_id));
//                    ContentValues contentValues = historyDao.parseContentValues(history);
//                    historyDao.insert(contentValues);
//                }
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
//                HistoryDao historyDao = new HistoryDao(getContext());
//                Cursor historyCursor = historyDao.select("movie_id=? and video_file_id=?", new String[]{movie_id, String.valueOf(file.getId())}, null);
//                if (historyCursor.getCount() > 0) {
//                    long currentTime = System.currentTimeMillis();
//                    History history = historyDao.parseHistory(historyCursor).get(0);
//                    history.setLast_play_time(String.valueOf(currentTime));
//                    ContentValues contentValues = historyDao.parseContentValues(history);
//                    historyDao.update(contentValues, "id=?", new String[]{String.valueOf(history.getId())});
//                } else {
//                    long currentTime = System.currentTimeMillis();
//                    History history = new History();
//                    history.setVideo_file_id(file.getId());
//                    history.setTime("0");
//                    history.setLast_play_time(String.valueOf(currentTime));
//                    history.setMovieId(Integer.parseInt(movie_id));
//                    ContentValues contentValues = historyDao.parseContentValues(history);
//                    historyDao.insert(contentValues);
//                }
            }
            Intent intent = new Intent("firefly.intent.action.PLAY_VIDEO");
            intent.setDataAndType(fileuri, "video/*");
            try {
                if (intent.resolveActivity(getContext().getPackageManager()) != null) {
                    getContext().startActivity(intent);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return cursor;
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
