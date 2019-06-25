package com.hphtv.movielibrary.sqlite.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hphtv.movielibrary.sqlite.bean.PosterProviderBean;
import com.hphtv.movielibrary.sqlite.MovieDBHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tchip on 18-5-15.
 */

public class PosterProviderDao extends  BaseDao<PosterProviderBean> {
    private MovieDBHelper movieDBHelper;
    private Context context;
    SQLiteDatabase db;

    public PosterProviderDao(Context context) {
        super(context,MovieDBHelper.TABLE_POSTERPROVIDER);
    }

@Override
    public List<PosterProviderBean> parseList(Cursor cursor) {
        List<PosterProviderBean> parsePosterProviderBeanList = new ArrayList<>();
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                PosterProviderBean poster = new PosterProviderBean();
                int id = cursor.getInt(cursor.getColumnIndex("id"));
                String posterstr = cursor.getString(cursor.getColumnIndex("poster"));
                poster.setId(id);
                poster.setPoster(posterstr);

                parsePosterProviderBeanList.add(poster);
            }
        }
        return parsePosterProviderBeanList;
    }

    public ContentValues parseContentValues(PosterProviderBean posterBean) {
        ContentValues contentValues = new ContentValues();
        String poster = posterBean.getPoster();
        contentValues.put("poster", poster);
        return contentValues;
    }


}
