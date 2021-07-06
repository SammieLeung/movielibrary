package com.hphtv.movielibrary.scraper.mtime.respone;

import com.hphtv.movielibrary.roomdb.entity.Movie;
import com.hphtv.movielibrary.util.retrofit.ResponeEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * author: Sam Leung
 * date:  2021/6/30
 */
public class MtimeSuggestRespone implements ResponeEntity<List<Movie>>{
    private Data data;

    @Override
    public List<Movie> toEntity() {
        List<Movie> movies = new ArrayList<>();
        if (data.suggestions != null)
            for (Data.SuggestMovie movie : data.suggestions) {
                movies.add(movie.toEntity());
            }
        return movies;
    }

    private class Data{
        private List<SuggestMovie> suggestions;

        private class SuggestMovie implements ResponeEntity<Movie> {
            private int movieId;
            private String titleCn;
            private String titleEn;
            private String contentType;

            @Override
            public Movie toEntity() {
                Movie movie = new Movie();
                movie.movieId = (String.valueOf(movieId));
                movie.title = titleCn;
                movie.otherTitle = titleEn;
                return movie;
            }
        }
    }

}
