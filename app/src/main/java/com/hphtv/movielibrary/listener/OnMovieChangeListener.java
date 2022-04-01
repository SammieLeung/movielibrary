package com.hphtv.movielibrary.listener;

import com.hphtv.movielibrary.roomdb.entity.Movie;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.hphtv.movielibrary.roomdb.entity.relation.MovieWrapper;

/**
 * author: Sam Leung
 * date:  2022/3/30
 */
public interface OnMovieChangeListener {
    void OnRematchPoster(MovieDataView movieDataView, int pos);
    void OnMovieChange(MovieDataView movieDataView,int pos);
    void OnMovieRemove( String movie_id,int pos);
    void OnMovieInsert(MovieDataView movieDataView,int pos);
}
