package com.hphtv.movielibrary.scraper.mtime;


import com.hphtv.movielibrary.roomdb.entity.Movie;
import com.hphtv.movielibrary.util.retrofit.ResponeEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * author: Sam Leung
 * date:  21-5-13
 */
public class MtimeSearchRespone implements ResponeEntity<List<Movie>> {
    private Data data;

    @Override
    public List<Movie> toEntity() {
        List<Movie> movies = new ArrayList<>();
        if (data.movies != null)
            for (Data.MtimeMovie movie : data.movies) {
                movies.add(movie.toEntity());
            }
        return movies;
    }

    private class Data {
        private List<MtimeMovie> movies;


        private class MtimeMovie implements ResponeEntity<Movie> {
            private int movieId;
            private String name;
            private String nameEn;

            @Override
            public Movie toEntity() {
                Movie movie = new Movie();
                movie.movieId = (String.valueOf(movieId));
                movie.title = (name);
                movie.otherTitle = nameEn;
                return movie;
            }
        }


    }
}
