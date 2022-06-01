package com.hphtv.movielibrary.util;

import android.content.Context;

import com.hphtv.movielibrary.MovieApplication;
import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.ActorDao;
import com.hphtv.movielibrary.roomdb.dao.DirectorDao;
import com.hphtv.movielibrary.roomdb.dao.GenreDao;
import com.hphtv.movielibrary.roomdb.dao.MovieActorCrossRefDao;
import com.hphtv.movielibrary.roomdb.dao.MovieDao;
import com.hphtv.movielibrary.roomdb.dao.MovieDirectorCrossRefDao;
import com.hphtv.movielibrary.roomdb.dao.MovieGenreCrossRefDao;
import com.hphtv.movielibrary.roomdb.dao.MovieVideoTagCrossRefDao;
import com.hphtv.movielibrary.roomdb.dao.MovieVideofileCrossRefDao;
import com.hphtv.movielibrary.roomdb.dao.SeasonDao;
import com.hphtv.movielibrary.roomdb.dao.StagePhotoDao;
import com.hphtv.movielibrary.roomdb.dao.VideoFileDao;
import com.hphtv.movielibrary.roomdb.dao.VideoTagDao;
import com.hphtv.movielibrary.roomdb.entity.Actor;
import com.hphtv.movielibrary.roomdb.entity.Director;
import com.hphtv.movielibrary.roomdb.entity.Genre;
import com.hphtv.movielibrary.roomdb.entity.Movie;
import com.hphtv.movielibrary.roomdb.entity.Season;
import com.hphtv.movielibrary.roomdb.entity.StagePhoto;
import com.hphtv.movielibrary.roomdb.entity.VideoFile;
import com.hphtv.movielibrary.roomdb.entity.VideoTag;
import com.hphtv.movielibrary.roomdb.entity.reference.MovieActorCrossRef;
import com.hphtv.movielibrary.roomdb.entity.reference.MovieDirectorCrossRef;
import com.hphtv.movielibrary.roomdb.entity.reference.MovieGenreCrossRef;
import com.hphtv.movielibrary.roomdb.entity.reference.MovieVideoFileCrossRef;
import com.hphtv.movielibrary.roomdb.entity.reference.MovieVideoTagCrossRef;
import com.hphtv.movielibrary.roomdb.entity.relation.MovieWrapper;
import com.hphtv.movielibrary.scraper.service.OnlineDBApiService;
import com.station.kit.util.LogUtil;
import com.station.kit.util.SharePreferencesTools;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * author: Sam Leung
 * date:  2022/3/30
 */
public class MovieHelper {

    /**
     * 播放影片
     *
     * @param path
     * @param name
     * @return
     */
    public static Observable<String> playingMovie(String path, String name) {
        Context context = MovieApplication.getInstance();
        return Observable.just(path)
                .subscribeOn(Schedulers.io())
                //记录播放时间，作为播放记录
                .doOnNext(filepath -> {
                    VideoFileDao videoFileDao = MovieLibraryRoomDatabase.getDatabase(context).getVideoFileDao();
                    MovieDao movieDao = MovieLibraryRoomDatabase.getDatabase(context).getMovieDao();
                    long currentTime = System.currentTimeMillis();
                    videoFileDao.updateLastPlaytime(filepath, currentTime);
                    Movie movie = movieDao.queryByFilePath(filepath, ScraperSourceTools.getSource());
                    if (movie != null)
                        movieDao.updateLastPlaytime(movie.movieId, currentTime);
                    String poster = videoFileDao.getPoster(filepath, ScraperSourceTools.getSource());
                    SharePreferencesTools.getInstance(context).saveProperty(Constants.SharePreferenceKeys.LAST_POTSER, poster);
                    OnlineDBApiService.updateHistory(filepath, ScraperSourceTools.getSource());
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(s -> VideoPlayTools.play(context, path, name));
    }

    /**
     * 保存自动匹配到的电影信息，并且与对应文件建立数据库关系
     *
     * @param context
     * @param movieWrapper
     * @param videoFile
     */
    public static void addNewMovieInfo(Context context, MovieWrapper movieWrapper, VideoFile videoFile) {
        long movie_id = saveBaseInfo(context, movieWrapper);
        if (movie_id != -1) {

            //VideoFile
            establishRelationshipBetweenPosterAndVideos(context, movie_id, videoFile, movieWrapper.movie.source);
            OnlineDBApiService.uploadMovie(movieWrapper.movie, videoFile, movieWrapper.movie.source);
            LogUtil.v(Thread.currentThread().getName(), "saveMovie: " + movie_id + "<=>" + videoFile.filename);
        }
    }

    /**
     * 保存手动选择电影信息，并且与对应文件建立数据库关系，并且更新文件的关键词
     *
     * @param context
     * @param movieWrapper
     * @param videoFileList
     */
    public static void manualSaveMovie(Context context, MovieWrapper movieWrapper, List<VideoFile> videoFileList) {
        long movie_id = saveBaseInfo(context, movieWrapper);
        if (movie_id != -1 && movieWrapper.movie != null) {
            //VideoFile
            establishRelationshipBetweenPosterAndVideos(context, movie_id, movieWrapper.movie.title, videoFileList, movieWrapper.movie.source);
            OnlineDBApiService.uploadMovie(movieWrapper, videoFileList, movieWrapper.movie.source);
        }
    }

    /**
     * 保存电影的基本信息（剧情、演员、评分、剧照等)
     *
     * @param context
     * @param movieWrapper
     * @return
     */
    private static long saveBaseInfo(Context context, MovieWrapper movieWrapper) {
        MovieDao movieDao = MovieLibraryRoomDatabase.getDatabase(context).getMovieDao();
        GenreDao genreDao = MovieLibraryRoomDatabase.getDatabase(context).getGenreDao();
        ActorDao actorDao = MovieLibraryRoomDatabase.getDatabase(context).getActorDao();
        DirectorDao directorDao = MovieLibraryRoomDatabase.getDatabase(context).getDirectorDao();
        MovieGenreCrossRefDao movieGenreCrossRefDao = MovieLibraryRoomDatabase.getDatabase(context).getMovieGenreCrossRefDao();
        MovieActorCrossRefDao movieActorCrossRefDao = MovieLibraryRoomDatabase.getDatabase(context).getMovieActorCrossRefDao();
        MovieDirectorCrossRefDao movieDirectorCrossRefDao = MovieLibraryRoomDatabase.getDatabase(context).getMovieDirectorCrossRefDao();
        StagePhotoDao stagePhotoDao = MovieLibraryRoomDatabase.getDatabase(context).getStagePhotoDao();
        SeasonDao seasonDao = MovieLibraryRoomDatabase.getDatabase(context).getSeasonDao();

        if (movieWrapper.movie == null) {
            return -1;
        }

        Movie new_movie = movieWrapper.movie;
        Movie old_movie = movieDao.queryByMovieId(new_movie.movieId, new_movie.source);
        if (old_movie != null) {
            new_movie.id = old_movie.id;
            new_movie.addTime = old_movie.addTime;
            new_movie.updateTime = System.currentTimeMillis();
            new_movie.isFavorite = old_movie.isFavorite;
            new_movie.isWatched = old_movie.isWatched;
            new_movie.lastPlayTime = old_movie.lastPlayTime;
            new_movie.pinyin = old_movie.pinyin;
            movieDao.update(new_movie);
        } else {
            new_movie.pinyin = Observable.just(new_movie.title)
                    .map(s -> {
                        String pinyin = PinyinParseAndMatchTools.parsePinyin(s);
                        return pinyin;
                    }).observeOn(Schedulers.newThread())
                    .blockingFirst();
            new_movie.addTime = System.currentTimeMillis();
            new_movie.updateTime = new_movie.addTime;
            long id = movieDao.insertOrIgnoreMovie(new_movie);
            new_movie.id = id;
        }


        List<Genre> genreList = movieWrapper.genres;
        List<Director> directorList = movieWrapper.directors;
        List<Actor> actorList = movieWrapper.actors;
        List<StagePhoto> stagePhotoList = movieWrapper.stagePhotos;
        List<Season> seasonList = movieWrapper.seasons;

        //多对多可以先插入数据库
        genreDao.insertGenres(genreList);
        actorDao.insertActors(actorList);
        directorDao.insertDirectors(directorList);

        List<String> querySelectionGenreNames = new ArrayList<>();
        for (Genre genre : genreList) {
            querySelectionGenreNames.add(genre.name);
        }

        //查询影片ID
        long movie_id = new_movie.id;
        long[] genre_ids = genreDao.queryByName(querySelectionGenreNames);

        movieWrapper.movie.id = movie_id;

        for (long genre_id : genre_ids) {
            if (genre_id != -1) {
                MovieGenreCrossRef movieGenreCrossRef = new MovieGenreCrossRef();
                movieGenreCrossRef.genreId = genre_id;
                movieGenreCrossRef.id = movie_id;
                movieGenreCrossRefDao.insertMovieGenreCrossRef(movieGenreCrossRef);
            }
        }

        for (Actor actor : actorList) {
            if (actor != null) {
                MovieActorCrossRef movieActorCrossRef = new MovieActorCrossRef();
                movieActorCrossRef.actorId = actor.actorId;
                movieActorCrossRef.id = movie_id;
                movieActorCrossRefDao.insertMovieActorCrossRef(movieActorCrossRef);
            }
        }

        for (Director director : directorList) {
            if (director != null) {
                MovieDirectorCrossRef movieDirectorCrossRef = new MovieDirectorCrossRef();
                movieDirectorCrossRef.directorId = director.director_id;
                movieDirectorCrossRef.id = movie_id;
                movieDirectorCrossRefDao.insertMovieDirectorCrossRef(movieDirectorCrossRef);
            }
        }

        for (StagePhoto stagePhoto : stagePhotoList) {
            if (stagePhoto != null) {
                stagePhoto.movieId = movie_id;
                stagePhotoDao.insertOrIgnore(stagePhoto);
            }
        }

        for (Season season : seasonList) {
            if (season != null) {
                season.movieId = movie_id;
                seasonDao.insertOrIgnore(season);
            }
        }

        autoClassification(context, new_movie, genreList);

        return movie_id;
    }

    /**
     * 关联电影信息和文件
     *
     * @param context
     * @param movie_id
     * @param videoFile
     * @param source
     */
    public static void establishRelationshipBetweenPosterAndVideos(Context context, long movie_id, VideoFile videoFile, String source) {
        MovieVideofileCrossRefDao movieVideofileCrossRefDao = MovieLibraryRoomDatabase.getDatabase(context).getMovieVideofileCrossRefDao();
        VideoFileDao videoFileDao = MovieLibraryRoomDatabase.getDatabase(context).getVideoFileDao();

        MovieVideoFileCrossRef movieVideoFileCrossRef = new MovieVideoFileCrossRef();
        movieVideoFileCrossRef.id = movie_id;
        movieVideoFileCrossRef.path = videoFile.path;
        movieVideoFileCrossRef.source = source;
        movieVideoFileCrossRef.timeStamp = System.currentTimeMillis();
        movieVideofileCrossRefDao.insertOrReplace(movieVideoFileCrossRef);

        videoFile.isScanned = 1;
        videoFileDao.update(videoFile);
    }

    /**
     * 关联电影信息和文件,并更新关键词
     *
     * @param context
     * @param movie_id
     * @param keyword
     * @param videoFileList
     * @param source
     */
    public static void establishRelationshipBetweenPosterAndVideos(Context context, long movie_id, String keyword, List<VideoFile> videoFileList, String source) {
        MovieVideofileCrossRefDao movieVideofileCrossRefDao = MovieLibraryRoomDatabase.getDatabase(context).getMovieVideofileCrossRefDao();
        VideoFileDao videoFileDao = MovieLibraryRoomDatabase.getDatabase(context).getVideoFileDao();

        for (VideoFile videoFile : videoFileList) {
            MovieVideoFileCrossRef movieVideoFileCrossRef = new MovieVideoFileCrossRef();
            movieVideoFileCrossRef.id = movie_id;
            movieVideoFileCrossRef.path = videoFile.path;
            movieVideoFileCrossRef.source = source;
            movieVideoFileCrossRef.timeStamp = System.currentTimeMillis();
            movieVideofileCrossRefDao.insertOrReplace(movieVideoFileCrossRef);
            videoFile.isScanned = 1;
//            videoFile.keyword = movie.title;
            videoFile.keyword = keyword;
            videoFileDao.update(videoFile);
        }
    }

    /**
     * 自动帮助影片归类
     * 当前自动区分电影、电视、儿童
     *
     * @param context
     * @param movie
     * @param genreList
     */
    private static void autoClassification(Context context, Movie movie, List<Genre> genreList) {
        VideoTagDao videoTagDao = MovieLibraryRoomDatabase.getDatabase(context).getVideoTagDao();
        MovieVideoTagCrossRefDao movieVideoTagCrossRefDao = MovieLibraryRoomDatabase.getDatabase(context).getMovieVideoTagCrossRefDao();

        if (movie.type != null) {
            Constants.VideoType type = Constants.VideoType.valueOf(movie.type.name());
            VideoTag typeTag = videoTagDao.queryVtidBySysTag(type);
            if (typeTag == null) {
                typeTag = new VideoTag(type);
                long vtid = videoTagDao.insertOrIgnore(typeTag);
                if (vtid > -1) {
                    typeTag.vtid = vtid;
                } else {
                    LogUtil.e("setVideoTag(sys): insert error " + typeTag.tag);//TODO
                }
            }
            if (typeTag != null) {
                MovieVideoTagCrossRef movieVideoTagCrossRef = new MovieVideoTagCrossRef();
                movieVideoTagCrossRef.id = movie.id;
                movieVideoTagCrossRef.vtid = typeTag.vtid;
                movieVideoTagCrossRefDao.insert(movieVideoTagCrossRef);
            }
        }
        String kids = context.getString(R.string.kids);
        String animation = context.getString(R.string.animation);
        String comedy = context.getString(R.string.comedy);
        String family = context.getString(R.string.family);
        boolean isKids = false;
        boolean isComedy = false;
        boolean isFamily = false;
        boolean isAnimation = false;

        for (Genre genre : genreList) {
            String name = genre.name;
            if (name.equals(kids))
                isKids = true;
            if (name.equals(animation))
                isAnimation = true;
            if (name.equals(comedy))
                isComedy = true;
            if (name.equals(family))
                isFamily = true;
        }

        if (isKids || (isAnimation && (isFamily || isComedy))) {
            VideoTag childTag = videoTagDao.queryVtidBySysTag(Constants.VideoType.child);
            if (childTag == null) {
                childTag = new VideoTag(Constants.VideoType.child);
                childTag.flag = 0;
                long vtid = videoTagDao.insertOrIgnore(childTag);
                if (vtid > -1) {
                    childTag.vtid = vtid;
                } else {
                    LogUtil.e("setVideoTag: insert error " + childTag.tag);
                }
            }
            if (childTag != null) {
                MovieVideoTagCrossRef movieVideoTagCrossRef = new MovieVideoTagCrossRef();
                movieVideoTagCrossRef.id = movie.id;
                movieVideoTagCrossRef.vtid = childTag.vtid;
                movieVideoTagCrossRefDao.insert(movieVideoTagCrossRef);
            }

        }

    }
}
