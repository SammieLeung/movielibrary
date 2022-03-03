package com.hphtv.movielibrary.util;

import android.content.Context;
import android.content.Intent;

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
import com.hphtv.movielibrary.roomdb.entity.Trailer;
import com.hphtv.movielibrary.roomdb.entity.VideoFile;
import com.hphtv.movielibrary.roomdb.entity.VideoTag;
import com.hphtv.movielibrary.roomdb.entity.reference.MovieActorCrossRef;
import com.hphtv.movielibrary.roomdb.entity.reference.MovieDirectorCrossRef;
import com.hphtv.movielibrary.roomdb.entity.reference.MovieGenreCrossRef;
import com.hphtv.movielibrary.roomdb.entity.reference.MovieVideoFileCrossRef;
import com.hphtv.movielibrary.roomdb.entity.reference.MovieVideoTagCrossRef;
import com.hphtv.movielibrary.roomdb.entity.relation.MovieWrapper;
import com.hphtv.movielibrary.ui.homepage.NewHomePageActivity;
import com.station.kit.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * author: Sam Leung
 * date:  2022/2/24
 */
public class ActivityHelper {

    public static void startHomePageActivity(Context context) {
        Intent intent = new Intent(context, NewHomePageActivity.class);
        context.startActivity(intent);
    }

    /**
     * 保存自动匹配到的电影信息，并且与对应文件建立数据库关系
     * @param context
     * @param movieWrapper
     * @param videoFile
     * @param source
     */
    public static void saveMovieWrapper(Context context, MovieWrapper movieWrapper, VideoFile videoFile, String source) {
        long movie_id = saveCommonInfos(context, movieWrapper, source);
        if (movie_id != -1) {

            //VideoFile
            saveMovieVideoFileCrossRef(context, movie_id, videoFile, source);

            LogUtil.v(Thread.currentThread().getName(), "saveMovie=>: " + movie_id + ":" + videoFile.filename);
        }
    }

    /**
     * 保存手动选择电影信息，并且与对应文件建立数据库关系，并且更新文件的关键词
     * @param context
     * @param movieWrapper
     * @param videoFileList
     * @param source
     */
    public static void saveMatchedMovieWrapper(Context context, MovieWrapper movieWrapper, List<VideoFile> videoFileList, String source) {
        long movie_id = saveCommonInfos(context, movieWrapper, source);
        if (movie_id != -1&&movieWrapper.movie!=null) {
            //VideoFile
            saveMovieVideoFileCrossRef(context, movie_id,movieWrapper.movie.title,videoFileList, source);
        }
    }

    /**
     * 保存电影的基本信息（剧情、演员、评分、剧照等)
     * @param context
     * @param movieWrapper
     * @param source
     * @return
     */
    private static long saveCommonInfos(Context context, MovieWrapper movieWrapper, String source) {
        MovieDao movieDao = MovieLibraryRoomDatabase.getDatabase(context).getMovieDao();
        GenreDao genreDao = MovieLibraryRoomDatabase.getDatabase(context).getGenreDao();
        ActorDao actorDao = MovieLibraryRoomDatabase.getDatabase(context).getActorDao();
        DirectorDao directorDao = MovieLibraryRoomDatabase.getDatabase(context).getDirectorDao();
        MovieGenreCrossRefDao movieGenreCrossRefDao = MovieLibraryRoomDatabase.getDatabase(context).getMovieGenreCrossRefDao();
        MovieActorCrossRefDao movieActorCrossRefDao = MovieLibraryRoomDatabase.getDatabase(context).getMovieActorCrossRefDao();
        MovieDirectorCrossRefDao movieDirectorCrossRefDao = MovieLibraryRoomDatabase.getDatabase(context).getMovieDirectorCrossRefDao();
        StagePhotoDao stagePhotoDao = MovieLibraryRoomDatabase.getDatabase(context).getStagePhotoDao();
        SeasonDao seasonDao = MovieLibraryRoomDatabase.getDatabase(context).getSeasonDao();


        //获取各个实体类
        Movie movie = movieWrapper.movie;
        if (movie == null)
            return -1;
        List<Genre> genreList = movieWrapper.genres;
        List<Director> directorList = movieWrapper.directors;
        List<Actor> actorList = movieWrapper.actors;
//        List<Trailer> trailerList = movieWrapper.trailers;
        List<StagePhoto> stagePhotoList = movieWrapper.stagePhotos;
        List<Season> seasonList = movieWrapper.seasons;
        //插入电影到数据库
        movie.pinyin = PinyinParseAndMatchTools.parsePinyin(movie.title);
        movie.addTime = System.currentTimeMillis();
        movieDao.insertOrIgnoreMovie(movie);
        //多对多可以先插入数据库
        genreDao.insertGenres(genreList);
        actorDao.insertActors(actorList);
        directorDao.insertDirectors(directorList);

        List<String> querySelectionGenreNames = new ArrayList<>();
        for (Genre genre : genreList) {
            querySelectionGenreNames.add(genre.name);
        }

        //查询影片ID
        long movie_id = movieDao.queryByMovieId(movie.movieId, source).id;
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

        saveAutoVideoTag(context, movie, genreList);

        return movie_id;
    }

    /**
     * 关联电影信息和文件
     * @param context
     * @param movie_id
     * @param videoFile
     * @param source
     */
    private static void saveMovieVideoFileCrossRef(Context context, long movie_id, VideoFile videoFile, String source) {
        MovieVideofileCrossRefDao movieVideofileCrossRefDao = MovieLibraryRoomDatabase.getDatabase(context).getMovieVideofileCrossRefDao();
        VideoFileDao videoFileDao = MovieLibraryRoomDatabase.getDatabase(context).getVideoFileDao();

        MovieVideoFileCrossRef movieVideoFileCrossRef = new MovieVideoFileCrossRef();
        movieVideoFileCrossRef.id = movie_id;
        movieVideoFileCrossRef.path = videoFile.path;
        movieVideoFileCrossRef.source = source;
        movieVideofileCrossRefDao.insertOrReplace(movieVideoFileCrossRef);

        videoFile.isScanned = 1;
        videoFileDao.update(videoFile);
    }

    /**
     * 关联电影信息和文件,并更新关键词
     * @param context
     * @param movie_id
     * @param keyword
     * @param videoFileList
     * @param source
     */
    private static void saveMovieVideoFileCrossRef(Context context, long movie_id, String keyword, List<VideoFile> videoFileList, String source) {
        MovieVideofileCrossRefDao movieVideofileCrossRefDao = MovieLibraryRoomDatabase.getDatabase(context).getMovieVideofileCrossRefDao();
        VideoFileDao videoFileDao = MovieLibraryRoomDatabase.getDatabase(context).getVideoFileDao();

        for (VideoFile videoFile : videoFileList) {
            MovieVideoFileCrossRef movieVideoFileCrossRef = new MovieVideoFileCrossRef();
            movieVideoFileCrossRef.id = movie_id;
            movieVideoFileCrossRef.path = videoFile.path;
            movieVideoFileCrossRef.source = source;
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
     * @param context
     * @param movie
     * @param genreList
     */
    private static void saveAutoVideoTag(Context context, Movie movie, List<Genre> genreList) {
        VideoTagDao videoTagDao = MovieLibraryRoomDatabase.getDatabase(context).getVideoTagDao();
        MovieVideoTagCrossRefDao movieVideoTagCrossRefDao = MovieLibraryRoomDatabase.getDatabase(context).getMovieVideoTagCrossRefDao();

        VideoTag typeTag;
        if (movie.type != null) {
            String type = movie.type.name();
            typeTag = videoTagDao.queryVtidBySysTag(type);
            if (typeTag == null) {
                typeTag = new VideoTag();
                typeTag.tag = type;
                typeTag.flag = 0;
                long vtid = videoTagDao.insertOrIgnore(typeTag);
                if (vtid > -1) {
                    typeTag.vtid = vtid;
                } else {
                    LogUtil.e("setVideoTag(sys): insert error " + typeTag.tag);
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
            VideoTag childTag = videoTagDao.queryVtidBySysTag(Constants.VideoType.child.name());
            if (childTag == null) {
                childTag = new VideoTag();
                childTag.tag = Constants.VideoType.child.name();
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
