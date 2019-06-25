package com.hphtv.movielibrary.sqlite.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.hphtv.movielibrary.sqlite.bean.Favorite;
import com.hphtv.movielibrary.sqlite.MovieDBHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tchip on 18-5-9.
 */

public class FavoriteDao extends BaseDao<Favorite> {


    public FavoriteDao(Context context) {
        super(context, MovieDBHelper.TABLE_FAVORITE);
    }

    @Override
    public List<Favorite> parseList(Cursor cursor) {
        List<Favorite> favoriteList = new ArrayList<>();
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                Favorite favorite = new Favorite();
                long id = cursor.getInt(cursor.getColumnIndex("id"));
                long wrapper_id = cursor.getInt(cursor.getColumnIndex("wrapper_id"));

                favorite.setId(id);
                favorite.setWrapper_id(wrapper_id);

                favoriteList.add(favorite);
            }
        }
        return favoriteList;
    }

    @Override
    public ContentValues parseContentValues(Favorite favorite) {
        ContentValues contentValues = new ContentValues();
        long wrapper_id=favorite.getWrapper_id();
        contentValues.put("wrapper_id",wrapper_id);
        return contentValues;
    }
}
