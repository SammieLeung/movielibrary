package com.hphtv.movielibrary.roomdb;

import kotlin.text.UStringsKt;

/**
 * author: Sam Leung
 * date:  2021/5/20
 */
public interface TABLE {
    String WRITER="writer";
    String ACTOR = "actor";
    String DEVICE = "device";
    String DIRECTOR = "director";
    String GENRE = "genre";
    String MOVIE = "movie";
    String TRAILER="trailer";
    String STAGEPHOTO ="stagephoto";
    String SHORTCUT="shortcut";
    String SEASON="season";

    String MOVIE_ACTOR_CROSS_REF = "movie_actor_cross_ref";
    String MOVIE_DIRECTOR_CROSS_REF = "movie_director_cross_ref";
    String MOVIE_WRITER_CROSS_REF = "movie_writer_cross_ref";

    String MOVIE_GENRE_CROSS_REF = "movie_genre_cross_ref";
    String MOVIE_VIDEOFILE_CROSS_REF = "movie_videofile_cross_ref";

    String SCAN_DIRECTORY = "scan_directory";
    String VIDEOFILE = "videofile";
    String GENRE_TAG="genre_tag";
}
