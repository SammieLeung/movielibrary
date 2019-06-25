package com.hphtv.movielibrary.sqlite.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.hphtv.movielibrary.sqlite.bean.History;
import com.hphtv.movielibrary.sqlite.MovieDBHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tchip on 18-5-9.
 */

public class HistoryDao extends BaseDao<History> {


    public HistoryDao(Context context) {
        super(context, MovieDBHelper.TABLE_HISTORY);
    }

    @Override
    public List<History> parseList(Cursor cursor) {
        List<History> historyList = new ArrayList<>();
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                History history = new History();
                long id = cursor.getInt(cursor.getColumnIndex("id"));
                String time = cursor.getString(cursor.getColumnIndex("time"));
                String last_play_time = cursor.getString(cursor.getColumnIndex("last_play_time"));
                long wrapper_id = cursor.getInt(cursor.getColumnIndex("wrapper_id"));

                history.setId(id);
                history.setWrapper_id(wrapper_id);
                history.setTime(time);
                history.setLast_play_time(last_play_time);

                historyList.add(history);
            }
        }
        return historyList;
    }

    public ContentValues parseContentValues(History history) {
        ContentValues contentValues = new ContentValues();
        String time = history.getTime();
        String last_play_time = history.getLast_play_time();
        long wrapper_id=history.getWrapper_id();

        contentValues.put("wrapper_id",wrapper_id);
        contentValues.put("time", time);
        contentValues.put("last_play_time", last_play_time);
        return contentValues;
    }
}
