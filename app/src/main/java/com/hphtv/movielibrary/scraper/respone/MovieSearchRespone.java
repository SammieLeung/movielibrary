package com.hphtv.movielibrary.scraper.respone;


import android.text.TextUtils;

import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.roomdb.entity.Movie;
import com.hphtv.movielibrary.util.retrofit.ResponeEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * author: Sam Leung
 * date:  21-5-13
 */
public class MovieSearchRespone implements ResponeEntity<List<Movie>> {
    private Data data;


    @Override
    public List<Movie> toEntity() {
        List<Movie> movies = new ArrayList<>();
        if (data!=null&&data.list != null)
            for (Data.SearchMovie searchMovie : data.list) {
                Movie movie=searchMovie.toEntity();
                movie.source= data.source.toUpperCase();
                movies.add(movie);
            }
        return movies;
    }

    private class Data {
        private List<SearchMovie> list;
        private int total;
        private String source;

        private class SearchMovie implements ResponeEntity<Movie> {
            private String movie_id;
            private String title;
            private String title_en;
            private String year;
            private String type;
            private String poster;
            private String genre;
            private String actors;


            @Override
            public Movie toEntity() {
                Movie movie = new Movie();
                movie.movieId = movie_id;
                movie.title = title;
                movie.otherTitle = title_en;
                movie.tag = genre;
                movie.tag2 = actors;
                movie.type = type;
                movie.year = year;
                movie.poster = poster;
                return movie;
            }
        }


    }
}
