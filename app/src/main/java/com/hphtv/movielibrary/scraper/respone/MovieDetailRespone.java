package com.hphtv.movielibrary.scraper.respone;


import com.hphtv.movielibrary.roomdb.entity.Actor;
import com.hphtv.movielibrary.roomdb.entity.Director;
import com.hphtv.movielibrary.roomdb.entity.Genre;
import com.hphtv.movielibrary.roomdb.entity.Movie;
import com.hphtv.movielibrary.roomdb.entity.relation.MovieWrapper;
import com.hphtv.movielibrary.roomdb.entity.StagePhoto;
import com.hphtv.movielibrary.roomdb.entity.Trailer;
import com.hphtv.movielibrary.util.retrofit.ResponeEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * author: Sam Leung
 * date:  21-5-13
 */
public class MovieDetailRespone implements ResponeEntity<MovieWrapper> {
    private Data data;

    @Override
    public MovieWrapper toEntity() {
        if (data != null)
            return data.toEntity();
        return null;
    }

    private class Data implements ResponeEntity<MovieWrapper> {
        private String movie_id;
        private String title;
        private String title_en;
        private String year;
        private String released;
        private String duration;
        private String[] genre;
        private People[] directors;
        private People[] writers;
        private People[] actors;
        private String plot;
        private String language;
        private String country;
        private String poster;
        private String rating;
        private String type;
        private Video[] videos;
        private Img[] stage_img;
        private String api;


        @Override
        public MovieWrapper toEntity() {
            Movie movie = new Movie();
            movie.movieId = String.valueOf(movie_id);
            movie.title = title;
            movie.otherTitle = title_en;
            movie.ratings = rating;
            movie.poster = poster;
            movie.releaseDate = released;
            movie.releaseArea = country;
            movie.country = country;
            movie.duration = duration;
            movie.plot = plot;
            movie.year = year;
            movie.language = language;
            movie.type = type;
            movie.source = api;

            List<Genre> genreList = new ArrayList<>();
            if (genre != null && genre.length > 0) {
                for (String genreItem : genre) {
                    Genre genre = new Genre();
                    genre.name = genreItem;
                    genreList.add(genre);
                }
            }

            List<Director> directorList = new ArrayList<>();
            if (directors != null && directors.length > 0) {
                for (int i = 0; i < 10 && i < directors.length; i++) {
                    People directorPeople = directors[i];
                    Director director = new Director();
                    director.name = directorPeople.name;
                    director.nameEn = directorPeople.name_en;
                    director.img = directorPeople.img;
                    director.director_id = directorPeople.person_id;
                    directorList.add(director);
                }
            }

            List<Actor> actorList = new ArrayList<>();
            if (actors != null && actors.length > 0)
                for (int i = 0; i < 10 && i < actors.length; i++) {
                    People actorPeople = actors[i];
                    Actor actor = new Actor();
                    actor.actorId = actorPeople.person_id;
                    actor.img = actorPeople.img;
                    actor.name = actorPeople.name;
                    actor.nameEn = actorPeople.name_en;
                    actorList.add(actor);
                }

            List<Trailer> trailerList = new ArrayList<>();
            if (videos != null && videos.length > 0) {
                for (int i = 0; i < 10 && i < videos.length; i++) {
                    Trailer trailer = new Trailer();
                    trailer.img = videos[i].img;
                    trailer.url = videos[i].url;
                    trailer.title = videos[i].title;
                    trailer.trailerId = videos[i].video_id;
                    trailerList.add(trailer);
                }
            }

            List<StagePhoto> stagePhotoList = new ArrayList<>();
            if (stage_img != null && stage_img.length > 0) {
                for (int i = 0; i < 10 && i < stage_img.length; i++) {
                    Img img = stage_img[i];
                    StagePhoto stagePhoto = new StagePhoto();
                    stagePhoto.imgUrl = img.img_url;
                    stagePhoto.stageId = img.img_id;
                    stagePhotoList.add(stagePhoto);
                }
            }


            MovieWrapper movieWrapper = new MovieWrapper();
            movieWrapper.movie = movie;
            movieWrapper.actors = actorList;
            movieWrapper.directors = directorList;
            movieWrapper.genres = genreList;
            movieWrapper.trailers = trailerList;
            movieWrapper.stagePhotos = stagePhotoList;

            return movieWrapper;
        }

        private class People {
            private long person_id;
            private String name;
            private String name_en;
            private String img;
        }

        private class Video {
            private long video_id;
            private String url;
            private String title;
            private String img;
        }

        private class Img {
            private long img_id;
            private String img_url;
        }
    }

}
