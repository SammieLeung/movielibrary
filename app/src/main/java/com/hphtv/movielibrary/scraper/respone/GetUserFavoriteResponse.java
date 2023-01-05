package com.hphtv.movielibrary.scraper.respone;

import android.util.Log;

import com.hphtv.movielibrary.MovieApplication;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.MovieDao;
import com.hphtv.movielibrary.roomdb.entity.Actor;
import com.hphtv.movielibrary.roomdb.entity.Director;
import com.hphtv.movielibrary.roomdb.entity.Genre;
import com.hphtv.movielibrary.roomdb.entity.Movie;
import com.hphtv.movielibrary.roomdb.entity.StagePhoto;
import com.hphtv.movielibrary.roomdb.entity.Trailer;
import com.hphtv.movielibrary.roomdb.entity.relation.MovieWrapper;
import com.hphtv.movielibrary.util.retrofit.ResponeEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * author: Sam Leung
 * date:  2022/12/5
 */
public class GetUserFavoriteResponse implements ResponeEntity<List<MovieWrapper>> {
    private FavoriteMovie[] data;
    private int code;
    private String msg;


    @Override
    public List<MovieWrapper> toEntity() {
        List<MovieWrapper> movieList = new ArrayList<>();
        if (data != null) {
            for (FavoriteMovie favoriteMovie : data) {
                if (favoriteMovie != null)
                    movieList.add(favoriteMovie.toEntity());
            }
        }
        return movieList;
    }

    private class FavoriteMovie implements ResponeEntity<MovieWrapper> {
        String movie_id;
        String type;
        String api;
        ResponseGenres[] genres;
        String title;
        String year;
        String released;
        String duration;
        People[] directors;
        private People[] writers;
        private ResponseActor[] actors;
        private String plot;
        private String language;
        private String country;
        private String tagline;
        private String poster;
        private String original_poster;
        private String rating;
        private Video[] videos;
        private Img[] stage_img;
        private Season[] seasons;

        private class ResponseGenres {
            String name;
        }

        private class ResponseActor {
            private long id;
            private long gender;
            private String name;
            private String original_name;
            private String known_for_department;
            private String profile_path;
            private String character;
            private int order;

        }

        private class People {
            private long id;
            private long gender;
            private String name;
            private String original_name;
            private String known_for_department;
            private String profile_path;
            private String department;
            private String job;
        }

        private class Video {
            private long video_id;
            private String url;
            private String title;
            private String img;
        }

        private class Img {
            private String file_path;
        }

        private class Season {
            private String air_date;
            private int episode_count;
            private String name;
            private String overview;
            private String poster_path;
            private int season_number;
        }

        @Override
        public MovieWrapper toEntity() {
                Movie movie = new Movie();
                movie.movieId = String.valueOf(movie_id);
                movie.title = title;
                movie.ratings = rating;
                movie.poster = poster;
                movie.releaseDate = released;
                movie.releaseArea = country;
                movie.region = country;
                movie.duration = duration;
                movie.plot = plot;
                movie.year = year;
                movie.language = language;
                movie.type = Constants.VideoType.valueOf(type);
                movie.source = api;

                List<Genre> genreList = new ArrayList<>();
                if (genres != null && genres.length > 0) {
                    for (ResponseGenres genreItem : genres) {
                        Genre genre = new Genre();
                        genre.name = genreItem.name;
                        genre.source = api;
                        genreList.add(genre);
                    }
                }

                List<Director> directorList = new ArrayList<>();
                if (directors != null && directors.length > 0) {
                    for (int i = 0; i < 10 && i < directors.length; i++) {
                        People directorPeople = directors[i];
                        Director director = new Director();
                        director.name = directorPeople.name;
                        director.nameEn = directorPeople.original_name;
                        director.img = directorPeople.profile_path;
                        director.director_id = directorPeople.id;
                        directorList.add(director);
                    }
                }

                List<Actor> actorList = new ArrayList<>();
                if (actors != null && actors.length > 0)
                    for (int i = 0; i < 10 && i < actors.length; i++) {
                        ResponseActor actorPeople = actors[i];
                        Actor actor = new Actor();
                        actor.actorId = actorPeople.id;
                        actor.img = actorPeople.profile_path;
                        actor.name = actorPeople.name;
                        actor.nameEn = actorPeople.original_name;
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
                        stagePhoto.imgUrl = img.file_path;
                        stagePhotoList.add(stagePhoto);
                    }
                }

                List<com.hphtv.movielibrary.roomdb.entity.Season> seasonList = new ArrayList<>();
                if (seasons != null && seasons.length > 0) {
                    for (Season season : seasons) {
                        com.hphtv.movielibrary.roomdb.entity.Season dbSeason = new com.hphtv.movielibrary.roomdb.entity.Season();
                        dbSeason.episodeCount = season.episode_count;
                        dbSeason.seasonNumber = season.season_number;
                        dbSeason.airDate = season.air_date;
                        dbSeason.name = season.name;
                        dbSeason.poster = season.poster_path;
                        dbSeason.plot = season.overview;
                        dbSeason.source = api;
                        seasonList.add(dbSeason);
                    }
                }


                MovieWrapper movieWrapper = new MovieWrapper();
                movieWrapper.movie = movie;
                movieWrapper.actors = actorList;
                movieWrapper.directors = directorList;
                movieWrapper.genres = genreList;
                movieWrapper.trailers = trailerList;
                movieWrapper.stagePhotos = stagePhotoList;
                movieWrapper.seasons = seasonList;

                return movieWrapper;
        }
    }
}
