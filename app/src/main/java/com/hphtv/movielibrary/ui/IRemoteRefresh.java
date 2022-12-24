package com.hphtv.movielibrary.ui;

/**
 * author: Sam Leung
 * date:  2022/6/24
 */
public interface IRemoteRefresh {
    void remoteUpdateMovieNotify(long o_id, long n_id);
    void remoteUpdateFavoriteNotify(String movie_id, String type, boolean isFavorite);
    void remoteRemoveMovieNotify(String movie_id, String type);
}
