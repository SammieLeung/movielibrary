package com.hphtv.movielibrary.sqlite.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.hphtv.movielibrary.sqlite.bean.scraperBean.MovieTrailer;
import com.hphtv.movielibrary.sqlite.MovieDBHelper;

import java.util.ArrayList;
import java.util.List;

public class MovietrailerDao extends BaseDao<MovieTrailer> {

    public MovietrailerDao(Context context) {
        super(context,MovieDBHelper.TABLE_TRAILER);
    }
    /**
     * 数据库结果集转换MovieList
     *
     * @param cursor
     * @return
     */
    @Override
    public List<MovieTrailer> parseList(Cursor cursor) {
        List<MovieTrailer> movietrailers=new ArrayList<>();
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                MovieTrailer movietrailer = new MovieTrailer();

                int id = cursor.getInt(cursor.getColumnIndex("id"));
                int movie_id = cursor.getInt(cursor.getColumnIndex("movie_id"));
                String title = cursor.getString(cursor.getColumnIndex("title"));
                String photo = cursor.getString(cursor.getColumnIndex("photo"));
                String duration = cursor.getString(cursor
                        .getColumnIndex("duration"));
                String alt = cursor.getString(cursor.getColumnIndex("alt"));
                String pub_date = cursor.getString(cursor
                        .getColumnIndex("pub_date"));

                movietrailer.setId(String.valueOf(id));
                movietrailer.setMovie_id(movie_id);
                movietrailer.setTitle(title);
                movietrailer.setPhoto(photo);
                movietrailer.setDuration(duration);
                movietrailer.setAlt(alt);
                movietrailer.setPub_date(pub_date);
                movietrailers.add(movietrailer);

            }
        }
        return movietrailers;
    }

    public ContentValues parseContentValues(MovieTrailer movieTrailer) {
        ContentValues contentValues = new ContentValues();
        LogUtil.v("db",movieTrailer.getId());
        int id = Integer.valueOf(movieTrailer.getId());
        long movie_id = movieTrailer.getMovie_id();
        String title = movieTrailer.getTitle();
        String photo = movieTrailer.getPhoto();
        String duration = movieTrailer.getDuration();
        String alt = movieTrailer.getAlt();
        String pub_date = movieTrailer.getPub_date();

        contentValues.put("id", id);
        contentValues.put("movie_id", movie_id);
        contentValues.put("title", title);
        contentValues.put("alt", alt);
        contentValues.put("photo", photo);
        contentValues.put("duration", duration);
        contentValues.put("pub_date", pub_date);

        return contentValues;
    }

}
