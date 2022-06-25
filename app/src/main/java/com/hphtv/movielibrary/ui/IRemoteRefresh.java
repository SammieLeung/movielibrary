package com.hphtv.movielibrary.ui;

/**
 * author: Sam Leung
 * date:  2022/6/24
 */
public interface IRemoteRefresh {
    void remoteUpdateMovie(long o_id,long n_id);
    void remoteUpdateFavorite(String movie_id,String type,boolean isFavorite);
}
