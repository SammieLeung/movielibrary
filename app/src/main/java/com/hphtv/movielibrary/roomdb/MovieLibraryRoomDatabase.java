package com.hphtv.movielibrary.roomdb;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.hphtv.movielibrary.roomdb.dao.ActorDao;
import com.hphtv.movielibrary.roomdb.dao.DeviceDao;
import com.hphtv.movielibrary.roomdb.dao.DirectorDao;
import com.hphtv.movielibrary.roomdb.dao.GenreDao;
import com.hphtv.movielibrary.roomdb.dao.MovieActorCrossRefDao;
import com.hphtv.movielibrary.roomdb.dao.MovieDao;
import com.hphtv.movielibrary.roomdb.dao.MovieDirectorCrossRefDao;
import com.hphtv.movielibrary.roomdb.dao.MovieGenreCrossRefDao;
import com.hphtv.movielibrary.roomdb.dao.MovieVideofileCrossRefDao;
import com.hphtv.movielibrary.roomdb.dao.ScanDirectoryDao;
import com.hphtv.movielibrary.roomdb.dao.StagePhotoDao;
import com.hphtv.movielibrary.roomdb.dao.TrailerDao;
import com.hphtv.movielibrary.roomdb.dao.VideoFileDao;
import com.hphtv.movielibrary.roomdb.entity.Actor;
import com.hphtv.movielibrary.roomdb.entity.Device;
import com.hphtv.movielibrary.roomdb.entity.Director;
import com.hphtv.movielibrary.roomdb.entity.Genre;
import com.hphtv.movielibrary.roomdb.entity.Movie;
import com.hphtv.movielibrary.roomdb.entity.MovieActorCrossRef;
import com.hphtv.movielibrary.roomdb.entity.MovieDataView;
import com.hphtv.movielibrary.roomdb.entity.MovieDirectorCrossRef;
import com.hphtv.movielibrary.roomdb.entity.MovieGenreCrossRef;
import com.hphtv.movielibrary.roomdb.entity.MovieVideoFileCrossRef;
import com.hphtv.movielibrary.roomdb.entity.ScanDirectory;
import com.hphtv.movielibrary.roomdb.entity.StagePhoto;
import com.hphtv.movielibrary.roomdb.entity.Trailer;
import com.hphtv.movielibrary.roomdb.entity.VideoFile;

import org.fourthline.cling.support.model.container.MovieGenre;

/**
 * author: Sam Leung
 * date:  21-5-14
 */

@Database(entities = {Actor.class, Device.class, Director.class, Genre.class, Movie.class, MovieActorCrossRef.class,
        MovieDirectorCrossRef.class, MovieGenreCrossRef.class, MovieVideoFileCrossRef.class,
        ScanDirectory.class, VideoFile.class, Trailer.class, StagePhoto.class}, views = {MovieDataView.class}, version = 2)
public abstract class MovieLibraryRoomDatabase extends RoomDatabase {
    private static MovieLibraryRoomDatabase sInstance;//创建单例
    //获取DAO
    public abstract ActorDao getActorDao();

    public abstract DeviceDao getDeviceDao();

    public abstract DirectorDao getDirectorDao();

    public abstract GenreDao getGenreDao();

    public abstract MovieActorCrossRefDao getMovieActorCrossRefDao();

    public abstract MovieDirectorCrossRefDao getMovieDirectorCrossRefDao();

    public abstract MovieVideofileCrossRefDao getMovieVideofileCrossRefDao();

    public abstract MovieGenreCrossRefDao getMovieGenreCrossRefDao();

    public abstract VideoFileDao getVideoFileDao();

    public abstract MovieDao getMovieDao();

    public abstract ScanDirectoryDao getScanDirectoryDao();

    public abstract TrailerDao getTrailerDao();

    public abstract StagePhotoDao getStagePhotoDao();

    public static MovieLibraryRoomDatabase getDatabase(final Context context) {
        if (sInstance == null) {
            synchronized (MovieLibraryRoomDatabase.class) {
                if (sInstance == null) {
                    sInstance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            MovieLibraryRoomDatabase.class, "movielibrary_db_v2")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return sInstance;
    }

}
