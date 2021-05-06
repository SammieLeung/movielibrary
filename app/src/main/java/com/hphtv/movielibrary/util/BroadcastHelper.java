package com.hphtv.movielibrary.util;

import android.content.Context;
import android.content.Intent;

import com.hphtv.movielibrary.sqlite.bean.scraperBean.Movie;

/**
 * author: Sam Leung
 * date:  21-2-24
 */
public class BroadcastHelper {
    public static final String ACTION_MOVIE_ALL_SYNC = "action.movie.all.sync";
    public static final String ACTION_MOVIE_REMOVE_SYNC = "action.movie.remove.sync";
    public static final String ACTION_MOVIE_ADD_SYNC = "action.movie.add.sync";
    public static final String ACTION_MOVIE_UPDATE_SYNC = "action.movie.update.sync";

    public static void sendBroadcastMovieAllSync(Context context) {
        Intent intent = new Intent();
        intent.setAction(ACTION_MOVIE_ALL_SYNC);
        context.sendBroadcast(intent);
    }

    public static void sendBroadcastMovieRemoveSync(Context context, long id) {
        Intent intent = new Intent();
        intent.setAction(ACTION_MOVIE_REMOVE_SYNC);
        intent.putExtra("id",id);
        context.sendBroadcast(intent);
    }

    public static void sendBroadcastMovieAddSync(Context context, long id){
        Intent intent = new Intent();
        intent.setAction(ACTION_MOVIE_ADD_SYNC);
        intent.putExtra("id",id);
        context.sendBroadcast(intent);
    }

    public static void sendBroadcastMovieUpdateSync(Context context, long id){
        Intent intent = new Intent();
        intent.setAction(ACTION_MOVIE_UPDATE_SYNC);
        intent.putExtra("id",id);
        context.sendBroadcast(intent);
    }
}
