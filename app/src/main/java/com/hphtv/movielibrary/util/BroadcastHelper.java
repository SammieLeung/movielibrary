package com.hphtv.movielibrary.util;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;


/**
 * author: Sam Leung
 * date:  21-2-24
 */
public class BroadcastHelper {
    public static final String ACTION_MOVIE_ALL_SYNC = "action.movie.all.sync";
    public static final String ACTION_MOVIE_REMOVE_SYNC = "action.movie.remove.sync";
    public static final String ACTION_MOVIE_SEARCH_SYNC = "action.movie.search.sync";
    public static final String ACTION_MOVIE_ADD_SYNC = "action.movie.add.sync";
    public static final String ACTION_MOVIE_UPDATE_SYNC = "action.movie.update.sync";


    public static void sendBroadcastMovieAllSync(Context context) {
        Intent intent = new Intent();
        intent.setAction(ACTION_MOVIE_ALL_SYNC);
        context.sendBroadcast(intent);
    }

    public static void sendBroadcastMovieRemoveSync(Context context, String movie_id) {
        Intent intent = new Intent();
        intent.setAction(ACTION_MOVIE_REMOVE_SYNC);
        sendMovieBroadcast(intent,context,movie_id);
    }

    public static void sendBroadcastMovieAddSync(Context context, String movie_id){
        Intent intent = new Intent();
        intent.setAction(ACTION_MOVIE_ADD_SYNC);
        sendMovieBroadcast(intent,context,movie_id);
    }

    public static void sendBroadcastSearchMoviesSync(Context context, ArrayList<String> movie_ids){
        Intent intent = new Intent();
        Bundle bundle=new Bundle();
        bundle.putStringArrayList("movie_ids",movie_ids);
        bundle.putString("source",ScraperSourceTools.getSource());
        intent.putExtras(bundle);
        intent.setAction(ACTION_MOVIE_SEARCH_SYNC);
        context.sendBroadcast(intent);
    }

    public static void sendBroadcastMovieUpdateSync(Context context, String last_movie_id,String movie_id,int is_favorite){
        Intent intent = new Intent();
        intent.setAction(ACTION_MOVIE_UPDATE_SYNC);
        intent.putExtra("last_movie_id",last_movie_id);
        intent.putExtra("is_favorite",is_favorite);
        sendMovieBroadcast(intent,context,movie_id);
    }

    private static void sendMovieBroadcast(Intent intent,Context context,String movie_id){
        intent.putExtra("movie_id",movie_id);
        intent.putExtra("source",ScraperSourceTools.getSource());
        context.sendBroadcast(intent);
    }
}
