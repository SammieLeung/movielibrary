package com.hphtv.movielibrary.util.retrofit;


import com.hphtv.movielibrary.roomdb.entity.Movie;

/**
 * author: Sam Leung
 * date:  21-5-13
 */
public class MtimeSearchMovie implements ResponeEntity<Movie> {
    public int movieId;
    public String name;

    @Override
    public Movie toEntity() {
        Movie movie = new Movie();
        movie.title=(name);
        return movie;
    }
}
