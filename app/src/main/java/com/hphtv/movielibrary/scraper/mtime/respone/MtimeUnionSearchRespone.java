package com.hphtv.movielibrary.scraper.mtime.respone;


import com.hphtv.movielibrary.data.ConstData;
import com.hphtv.movielibrary.roomdb.entity.Movie;
import com.hphtv.movielibrary.util.retrofit.ResponeEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * author: Sam Leung
 * date:  21-5-13
 */
public class MtimeUnionSearchRespone implements ResponeEntity<List<Movie>> {
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
            private List<String> actors;
            private String rating;
            private String img;
            private String movieType;
            private String realTime;


            @Override
            public Movie toEntity() {
                Movie movie = new Movie();
                movie.movieId = (String.valueOf(movieId));
                movie.title = name;
                movie.otherTitle = nameEn;
                movie.tag = movieType;
                movie.source= ConstData.ScraperSource.MTIME;
                if (actors != null && actors.size() > 0)
                    movie.tag2 = actors.toString().replaceAll("[\\[\\]]", "").replaceAll(", ", " / ");
                movie.releaseDate = realTime;
                movie.poster=img;
                return movie;
            }
        }


    }
}
