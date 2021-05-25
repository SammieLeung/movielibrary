package com.hphtv.movielibrary.util.retrofit;


import com.hphtv.movielibrary.data.ConstData;
import com.hphtv.movielibrary.roomdb.entity.Actor;
import com.hphtv.movielibrary.roomdb.entity.Director;
import com.hphtv.movielibrary.roomdb.entity.Genre;
import com.hphtv.movielibrary.roomdb.entity.Movie;
import com.hphtv.movielibrary.roomdb.entity.MovieWrapper;

import java.util.ArrayList;
import java.util.List;

/**
 * author: Sam Leung
 * date:  21-5-13
 */
public class MtimeDetailRespone implements ResponeEntity<MovieWrapper> {
    private Data data;

    @Override
    public MovieWrapper toEntity() {
        return data.basic.toEntity();
    }

    private class Data {
        private Basic basic;

        private class Basic implements ResponeEntity<MovieWrapper> {
            private int movieId;
            private String name;
            private String nameEn;
            private String overallRating;
            private String bigImage;
            private String[] type;
            private Director director;
            private Actor[] actors;
            private String releaseDate;
            private String releaseArea;
            private String mins;
            private String story;
            private String year;
            private Country[] countries;

            @Override
            public MovieWrapper toEntity() {
                Movie movie = new Movie();
                movie.movieId = String.valueOf(movieId);
                movie.title = name;
                movie.otherTitle = nameEn;
                movie.ratings = overallRating;
                movie.poster = bigImage;
                movie.releaseDate = releaseDate;
                movie.releaseArea = releaseArea;
                movie.duration = mins;
                movie.plot = story;
                movie.year = year;
                movie.source = ConstData.ScraperSource.MTIME;

                String t_countries = "";
                if (countries != null && countries.length > 0) {
                    for (Country country : countries) {
                        t_countries += country.name + ",";
                    }
                    t_countries = t_countries.substring(0, t_countries.lastIndexOf(","));
                    movie.country = t_countries;
                }

                List<Genre> genreList = new ArrayList<>();
                if (type != null && type.length > 0)
                    for (String type1 : type) {
                        Genre genre = new Genre();
                        genre.name = type1;
                        genreList.add(genre);
                    }

                com.hphtv.movielibrary.roomdb.entity.Director t_director = new com.hphtv.movielibrary.roomdb.entity.Director();
                if (director != null) {
                    t_director.name = director.name;
                    t_director.img = director.img;
                    t_director.directorId = director.directorId;
                    t_director.nameEn = director.nameEn;
                }

                List<com.hphtv.movielibrary.roomdb.entity.Actor> actorList = new ArrayList<>();
                if (actors != null && actors.length > 0)
                    for (int i = 0; i < 10 && i < actors.length; i++) {
                        Actor actor = actors[i];
                        com.hphtv.movielibrary.roomdb.entity.Actor t_actor = new com.hphtv.movielibrary.roomdb.entity.Actor();
                        t_actor.actorId = actor.actorId;
                        t_actor.img = actor.img;
                        t_actor.name = actor.name;
                        t_actor.nameEn = actor.nameEn;
                        actorList.add(t_actor);
                    }

                MovieWrapper movieWrapper = new MovieWrapper();
                movieWrapper.movie = movie;
                movieWrapper.actors = actorList;
                movieWrapper.director = t_director;
                movieWrapper.genres = genreList;

                return movieWrapper;
            }

            private class Director {
                private int directorId;
                private String name;
                private String nameEn;
                private String img;

            }

            private class Actor {
                private int actorId;
                private String name;
                private String nameEn;
                private String img;
            }

            private class Country {
                private String name;
                private String nameEn;
            }
        }


    }
}
