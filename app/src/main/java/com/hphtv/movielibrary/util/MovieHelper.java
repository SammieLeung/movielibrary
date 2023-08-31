package com.hphtv.movielibrary.util;

import android.content.Context;
import android.content.res.Configuration;

import com.hphtv.movielibrary.MovieApplication;
import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.bean.PlayList;
import com.hphtv.movielibrary.data.Config;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.ActorDao;
import com.hphtv.movielibrary.roomdb.dao.DirectorDao;
import com.hphtv.movielibrary.roomdb.dao.GenreDao;
import com.hphtv.movielibrary.roomdb.dao.MovieActorCrossRefDao;
import com.hphtv.movielibrary.roomdb.dao.MovieDao;
import com.hphtv.movielibrary.roomdb.dao.MovieDirectorCrossRefDao;
import com.hphtv.movielibrary.roomdb.dao.MovieGenreCrossRefDao;
import com.hphtv.movielibrary.roomdb.dao.MovieUserFavoriteCrossRefDao;
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
import com.hphtv.movielibrary.roomdb.entity.reference.MovieUserFavoriteCrossRef;
import com.hphtv.movielibrary.roomdb.entity.reference.MovieVideoFileCrossRef;
import com.hphtv.movielibrary.roomdb.entity.reference.MovieVideoTagCrossRef;
import com.hphtv.movielibrary.roomdb.entity.relation.MovieWrapper;
import com.hphtv.movielibrary.scraper.service.OnlineDBApiService;
import com.orhanobut.logger.Logger;
import com.station.kit.util.LogUtil;
import com.station.kit.util.SharePreferencesTools;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * author: Sam Leung
 * date:  2022/3/30
 */
public class MovieHelper {

    /**
     * 播放电影+保存播放记录
     *
     * @param path
     * @param name
     * @return
     */
    public static Observable<String> playingMovie(String path, String name) {
        Context context = MovieApplication.getInstance();
        return Observable.just(path)
                .subscribeOn(AndroidSchedulers.mainThread())
                //记录播放时间，作为播放记录
                .doOnNext(filepath -> {
                    VideoPlayTools.play(context, path, name);
                });
    }


    /**
     * 播放电视剧/综艺
     *
     * @param path
     * @param name
     * @return
     */
    public static Observable<PlayList> playingSeriesWithPlayList(String path, String name) {
        Context context = MovieApplication.getInstance();
        return Observable.create((ObservableOnSubscribe<PlayList>) emitter -> {
                    VideoFileDao videoFileDao = MovieLibraryRoomDatabase.getDatabase(context).getVideoFileDao();
                    List<String> nameList = videoFileDao.queryVideoFileNameList(path);
                    List<String> pathList = videoFileDao.queryVideoFilePathList(path);
                    PlayList playList = new PlayList();
                    playList.setPath(path);
                    playList.setName(name);
                    playList.setPlayList(new ArrayList<>(pathList));
                    playList.setNameList(new ArrayList<>(nameList));
                    emitter.onNext(playList);
                    emitter.onComplete();
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(playList -> {
                    VideoPlayTools.play(context, playList.getPath(), playList.getName(), playList.getPlayList(), playList.getNameList());
                });
    }

    /**
     * 保存播放记录
     *
     * @param path
     * @param position
     * @param duration
     * @return
     */
    public static Observable<String> updateHistory(String path, long position, long duration) {
        Context context = MovieApplication.getInstance();
        return Observable.create((ObservableOnSubscribe<String>) emitter -> {
            VideoFileDao videoFileDao = MovieLibraryRoomDatabase.getDatabase(context).getVideoFileDao();
            MovieDao movieDao = MovieLibraryRoomDatabase.getDatabase(context).getMovieDao();
            long currentTime = System.currentTimeMillis();
            VideoFile videoFile = videoFileDao.queryByPath(path);
            videoFile.lastPlayTime = currentTime;
            videoFile.lastPosition = position;
            videoFile.duration = duration;
            videoFileDao.update(videoFile);
            Movie movie = movieDao.queryByFilePath(path, ScraperSourceTools.getSource());
            if (movie != null) movieDao.updateLastPlaytime(movie.movieId, currentTime);
            String poster = videoFileDao.getPoster(path, ScraperSourceTools.getSource());
            SharePreferencesTools.getInstance(context).saveProperty(Constants.SharePreferenceKeys.LAST_POTSER, poster);
            OnlineDBApiService.updateHistory(path, ScraperSourceTools.getSource());
            emitter.onNext(path);
            emitter.onComplete();
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 保存播放记录
     *
     * @param path
     * @return
     */
    public static Observable<String> updateHistory(String path) {
        Context context = MovieApplication.getInstance();
        return Observable.create((ObservableOnSubscribe<String>) emitter -> {
            VideoFileDao videoFileDao = MovieLibraryRoomDatabase.getDatabase(context).getVideoFileDao();
            MovieDao movieDao = MovieLibraryRoomDatabase.getDatabase(context).getMovieDao();
            long currentTime = System.currentTimeMillis();
            videoFileDao.updateLastPlaytime(path, currentTime);
            Movie movie = movieDao.queryByFilePath(path, ScraperSourceTools.getSource());
            if (movie != null) movieDao.updateLastPlaytime(movie.movieId, currentTime);
            String poster = videoFileDao.getPoster(path, ScraperSourceTools.getSource());
            SharePreferencesTools.getInstance(context).saveProperty(Constants.SharePreferenceKeys.LAST_POTSER, poster);
            OnlineDBApiService.updateHistory(path, ScraperSourceTools.getSource());
            emitter.onNext(path);
            emitter.onComplete();
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread());
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

    public static void addNewMovieInfo(Context context, Movie movie, List<Genre> genreList, List<Director> directorList, List<Actor> actorList, List<StagePhoto> stagePhotoList, List<Season> seasonList,VideoFile videoFile) {
        long movie_id = saveBaseInfo(context, movie, genreList, directorList, actorList, stagePhotoList, seasonList);
        if (movie_id != -1) {
            //VideoFile
            establishRelationshipBetweenPosterAndVideos(context, movie_id, videoFile, movie.source);
            OnlineDBApiService.uploadMovie(movie, videoFile, movie.source);
            LogUtil.v(Thread.currentThread().getName(), "saveMovie: " + movie_id + "<=>" + videoFile.filename);
        }
    }

    public static void manualSaveMovie(Context context, MovieWrapper movieWrapper, List<VideoFile> videoFileList) {
        manualSaveMovie(context, movieWrapper, videoFileList, true);
    }

    /**
     * 保存手动选择电影信息，并且与对应文件建立数据库关系，并且更新文件的关键词
     *
     * @param context
     * @param movieWrapper
     * @param videoFileList
     */
    public static void manualSaveMovie(Context context, MovieWrapper movieWrapper, List<VideoFile> videoFileList, boolean notifyServer) {
        long movie_id = saveBaseInfo(context, movieWrapper);
        if (movie_id != -1 && movieWrapper.movie != null) {
            //VideoFile
            establishRelationshipBetweenPosterAndVideos(context, movie_id, movieWrapper.movie.title, videoFileList, movieWrapper.movie.source);
            if (notifyServer)
                OnlineDBApiService.uploadMovie(movieWrapper, videoFileList, movieWrapper.movie.source);
            LogUtil.v(Thread.currentThread().getName(), "manualSaveMovie: " + movie_id + " for " + videoFileList.size() + " files");
        }
    }


    public static long saveBaseInfo(Context context, Movie movie, List<Genre> genreList, List<Director> directorList, List<Actor> actorList, List<StagePhoto> stagePhotoList, List<Season> seasonList) {
        if(movie==null)
            return -1;
        MovieDao movieDao = MovieLibraryRoomDatabase.getDatabase(context).getMovieDao();
        GenreDao genreDao = MovieLibraryRoomDatabase.getDatabase(context).getGenreDao();
        ActorDao actorDao = MovieLibraryRoomDatabase.getDatabase(context).getActorDao();
        DirectorDao directorDao = MovieLibraryRoomDatabase.getDatabase(context).getDirectorDao();
        MovieGenreCrossRefDao movieGenreCrossRefDao = MovieLibraryRoomDatabase.getDatabase(context).getMovieGenreCrossRefDao();
        MovieActorCrossRefDao movieActorCrossRefDao = MovieLibraryRoomDatabase.getDatabase(context).getMovieActorCrossRefDao();
        MovieDirectorCrossRefDao movieDirectorCrossRefDao = MovieLibraryRoomDatabase.getDatabase(context).getMovieDirectorCrossRefDao();
        StagePhotoDao stagePhotoDao = MovieLibraryRoomDatabase.getDatabase(context).getStagePhotoDao();
        SeasonDao seasonDao = MovieLibraryRoomDatabase.getDatabase(context).getSeasonDao();

        Movie oldMovie = movieDao.queryByMovieIdAndType(movie.movieId, movie.source, movie.type.name());
        if (oldMovie != null) {
            movie.id = oldMovie.id;
            movie.addTime = oldMovie.addTime;
            movie.updateTime = System.currentTimeMillis();
            movie.isFavorite = oldMovie.isFavorite;
            movie.isWatched = oldMovie.isWatched;
            movie.lastPlayTime = oldMovie.lastPlayTime;
            movie.pinyin = oldMovie.pinyin;
            movieDao.update(movie);
        } else {
            movie.pinyin = Observable.just(movie.title).map(s -> {
                String pinyin = PinyinParseAndMatchTools.parsePinyin(s);
                return pinyin;
            }).observeOn(Schedulers.newThread()).blockingFirst();
            movie.addTime = System.currentTimeMillis();
            movie.updateTime = movie.addTime;
            long id = movieDao.insertOrIgnoreMovie(movie);
            if (id < 0) {
                movie = movieDao.queryByMovieIdAndType(movie.movieId, movie.source, movie.type.name());
            } else {
                movie.id = id;
            }
        }
        //多对多可以先插入数据库
        genreDao.insertGenres(genreList);
        actorDao.insertActors(actorList);
        directorDao.insertDirectors(directorList);

        List<String> querySelectionGenreNames = new ArrayList<>();
        for (Genre genre : genreList) {
            querySelectionGenreNames.add(genre.name);
        }

        //查询影片ID
        long movie_id = movie.id;
        long[] genre_ids = genreDao.queryByName(querySelectionGenreNames);

        movie.id = movie_id;

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

        autoClassification(context, movie, genreList);

        return movie_id;
    }

    /**
     * 保存电影的基本信息（剧情、演员、评分、剧照等)
     *
     * @param context
     * @param movieWrapper
     * @return
     */
    public static long saveBaseInfo(Context context, MovieWrapper movieWrapper) {
        return saveBaseInfo(context,movieWrapper.movie,movieWrapper.genres,movieWrapper.directors,movieWrapper.actors,movieWrapper.stagePhotos,movieWrapper.seasons);
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

    public static void quickAutoClassification(Context context, String movie_id, String source) {
        MovieDao movieDao = MovieLibraryRoomDatabase.getDatabase(context).getMovieDao();
        MovieWrapper wrapper = movieDao.queryMovieWrapperByMovieId(movie_id, source);
        autoClassification(context, wrapper.movie, wrapper.genres);
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
                    LogUtil.w("setVideoTag(sys): insert error " + typeTag.tag + ";try to query");
                    typeTag = videoTagDao.queryVtidBySysTag(type);
                }
            }
            MovieVideoTagCrossRef movieVideoTagCrossRef = new MovieVideoTagCrossRef();
            movieVideoTagCrossRef.id = movie.id;
            movieVideoTagCrossRef.vtid = typeTag.vtid;
            movieVideoTagCrossRefDao.insert(movieVideoTagCrossRef);
        }
        Locale locale = Constants.Scraper.TMDB.equals(movie.source) ? Locale.CHINESE : Locale.ENGLISH;
        Configuration configuration = new Configuration(context.getResources().getConfiguration());
        configuration.setLocale(locale);
        Context context_2 = context.createConfigurationContext(configuration);

        String kids = context_2.getString(R.string.kids);
        String animation = context_2.getString(R.string.animation);
        String comedy = context_2.getString(R.string.comedy);
        String family = context_2.getString(R.string.family);
        String talk = context_2.getString(R.string.talk);
        String reality = context_2.getString(R.string.reality);


        //不宜儿童观看的类型
        String crime = context_2.getString(R.string.crime);
        String horror = context_2.getString(R.string.horror);
        String thriller = context_2.getString(R.string.thriller);

        boolean isKids = false;
        boolean isComedy = false;
        boolean isFamily = false;
        boolean isAnimation = false;
        boolean isShow = false;

        boolean isNotForKids = false;

        for (Genre genre : genreList) {
            String name = genre.name;
            if (name.equals(kids)) isKids = true;
            if (name.equals(animation)) isAnimation = true;
            if (name.equals(comedy)) isComedy = true;
            if (name.equals(family)) isFamily = true;
            if (name.equals(talk) || name.equals(reality)) isShow = true;
            if (name.equals(crime) || name.equals(horror) || name.equals(thriller))
                isNotForKids = true;
        }

        if (!isNotForKids && (isKids || (isAnimation && (isFamily || isComedy)))) {
            VideoTag childTag = videoTagDao.queryVtidBySysTag(Constants.VideoType.child);
            if (childTag == null) {
                childTag = new VideoTag(Constants.VideoType.child);
                long vtid = videoTagDao.insertOrIgnore(childTag);
                if (vtid > -1) {
                    childTag.vtid = vtid;
                } else {
                    childTag = videoTagDao.queryVtidBySysTag(Constants.VideoType.child);
                    LogUtil.w("setVideoTag(sys): insert error " + childTag.tag + ";try to query");
                }
            }
            MovieVideoTagCrossRef movieVideoTagCrossRef = new MovieVideoTagCrossRef();
            movieVideoTagCrossRef.id = movie.id;
            movieVideoTagCrossRef.vtid = childTag.vtid;
            movieVideoTagCrossRefDao.insert(movieVideoTagCrossRef);
        }

        if (isShow) {
            VideoTag showTag = videoTagDao.queryVtidBySysTag(Constants.VideoType.variety_show);
            if (showTag == null) {
                showTag = new VideoTag(Constants.VideoType.variety_show);
                long vtid = videoTagDao.insertOrIgnore(showTag);

                if (vtid > -1) {
                    showTag.vtid = vtid;
                } else {
                    showTag = videoTagDao.queryVtidBySysTag(Constants.VideoType.variety_show);
                    LogUtil.w("setVideoTag(sys): insert error " + showTag.tag + ";try to query");
                }
            }
            MovieVideoTagCrossRef movieVideoTagCrossRef = new MovieVideoTagCrossRef();
            movieVideoTagCrossRef.id = movie.id;
            movieVideoTagCrossRef.vtid = showTag.vtid;
            movieVideoTagCrossRefDao.insert(movieVideoTagCrossRef);
        }

    }

    public static int setMovieFavoriteState(Context context, String movie_id, String videoType, boolean like) {
        return setMovieFavoriteState(context, movie_id, videoType, like, true, true);
    }

    public static int setMovieFavoriteState(Context context, String movie_id, String videoType, boolean like, boolean notifyServer, boolean saveToUserFavorite) {
        MovieDao movieDao = MovieLibraryRoomDatabase.getDatabase(context).getMovieDao();
        int count = movieDao.updateFavoriteStateByMovieId(like, movie_id, videoType);
        if (notifyServer)
            OnlineDBApiService.updateLike(movie_id, like, ScraperSourceTools.getSource(), videoType);
        if (saveToUserFavorite) {
            MovieUserFavoriteCrossRefDao movieUserFavoriteCrossRefDao = MovieLibraryRoomDatabase.getDatabase(context).getMovieUserFavoriteCrossRefDao();
            if (like) {
                MovieUserFavoriteCrossRef favoriteCrossRef = new MovieUserFavoriteCrossRef();
                favoriteCrossRef.type = Constants.VideoType.valueOf(videoType);
                favoriteCrossRef.movie_id = movie_id;
                favoriteCrossRef.source = ScraperSourceTools.getSource();
                movieUserFavoriteCrossRefDao.insertOrIgnore(favoriteCrossRef);
            } else
                movieUserFavoriteCrossRefDao.delete(movie_id, videoType, ScraperSourceTools.getSource());
        }
        return count;
    }
}
