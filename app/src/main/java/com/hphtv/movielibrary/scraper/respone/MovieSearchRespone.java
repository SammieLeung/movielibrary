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
        if (data != null && data.list != null)
            for (Data.SearchMovie searchMovie : data.list) {
                Movie movie = searchMovie.toEntity();
                movie.source = TextUtils.isEmpty(data.source) ? null : data.source.toUpperCase();
                movies.add(movie);
            }
        return movies;
    }

    public MovieSearchRespone combine(MovieSearchRespone respone) {
        if (respone.data == null || respone.data.list == null) {
            return this;
        } else if (this.data != null) {
            if (this.data.list == null)
                this.data.list = new ArrayList<>();
            this.data.list.addAll(respone.data.list);
            this.data.total = this.data.list.size();
            return this;
        } else {
            return this;
        }
    }

    private class Data {
        private List<SearchMovie> list;
        private int total;
        private String source;
        private boolean cache;

        private class SearchMovie implements ResponeEntity<Movie> {
            private String movie_id;
            private String title;
            private String year;
            private String type;
            private String poster;
            private String genre;//一般为空
            private String rating;
            private String actors;//一般为空
            private String plot;


            @Override
            public Movie toEntity() {
                Movie movie = new Movie();
                movie.movieId = movie_id;
                movie.title = title;
                movie.tag = genre;
                movie.tag2 = actors;
                movie.type =  Constants.SearchType.valueOf(type);
                movie.releaseDate = year;
                movie.ratings=rating;
                movie.plot=plot;
                movie.poster = poster;
                return movie;
            }
        }


    }
}
