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
import com.hphtv.movielibrary.roomdb.dao.MovieVideoTagCrossRefDao;
import com.hphtv.movielibrary.roomdb.dao.MovieVideofileCrossRefDao;
import com.hphtv.movielibrary.roomdb.dao.MovieWriterCrossRefDao;
import com.hphtv.movielibrary.roomdb.dao.ScanDirectoryDao;
import com.hphtv.movielibrary.roomdb.dao.SeasonDao;
import com.hphtv.movielibrary.roomdb.dao.ShortcutDao;
import com.hphtv.movielibrary.roomdb.dao.StagePhotoDao;
import com.hphtv.movielibrary.roomdb.dao.TrailerDao;
import com.hphtv.movielibrary.roomdb.dao.VideoFileDao;
import com.hphtv.movielibrary.roomdb.dao.VideoTagDao;
import com.hphtv.movielibrary.roomdb.dao.WriterDao;
import com.hphtv.movielibrary.roomdb.entity.Actor;
import com.hphtv.movielibrary.roomdb.entity.Device;
import com.hphtv.movielibrary.roomdb.entity.Director;
import com.hphtv.movielibrary.roomdb.entity.Genre;
import com.hphtv.movielibrary.roomdb.entity.GenreTag;
import com.hphtv.movielibrary.roomdb.entity.Movie;
import com.hphtv.movielibrary.roomdb.entity.ScanDirectory;
import com.hphtv.movielibrary.roomdb.entity.Season;
import com.hphtv.movielibrary.roomdb.entity.Shortcut;
import com.hphtv.movielibrary.roomdb.entity.StagePhoto;
import com.hphtv.movielibrary.roomdb.entity.Trailer;
import com.hphtv.movielibrary.roomdb.entity.VideoFile;
import com.hphtv.movielibrary.roomdb.entity.VideoTag;
import com.hphtv.movielibrary.roomdb.entity.Writer;
import com.hphtv.movielibrary.roomdb.entity.dataview.HistoryMovieDataView;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.hphtv.movielibrary.roomdb.entity.dataview.UnrecognizedFileDataView;
import com.hphtv.movielibrary.roomdb.entity.reference.MovieActorCrossRef;
import com.hphtv.movielibrary.roomdb.entity.reference.MovieDirectorCrossRef;
import com.hphtv.movielibrary.roomdb.entity.reference.MovieGenreCrossRef;
import com.hphtv.movielibrary.roomdb.entity.reference.MovieVideoFileCrossRef;
import com.hphtv.movielibrary.roomdb.entity.reference.MovieVideoTagCrossRef;
import com.hphtv.movielibrary.roomdb.entity.reference.MovieWriterCrossRef;

/**
 * author: Sam Leung
 * date:  21-5-14
 */

@Database(entities = {Actor.class, Device.class, Director.class, Writer.class, Genre.class, Movie.class, MovieActorCrossRef.class,
        MovieDirectorCrossRef.class, MovieWriterCrossRef.class, MovieGenreCrossRef.class, MovieVideoFileCrossRef.class,
        ScanDirectory.class, VideoFile.class, Trailer.class, StagePhoto.class,Shortcut.class, GenreTag.class,
        Season.class, VideoTag.class, MovieVideoTagCrossRef.class},
        views = {MovieDataView.class, UnrecognizedFileDataView.class, HistoryMovieDataView.class},
        version = 1)
public abstract class MovieLibraryRoomDatabase extends RoomDatabase {
    private static MovieLibraryRoomDatabase sInstance;//创建单例
    //获取DAO
    public abstract ActorDao getActorDao();

    public abstract DeviceDao getDeviceDao();

    public abstract WriterDao getWriterDao();

    public abstract DirectorDao getDirectorDao();

    public abstract GenreDao getGenreDao();

    public abstract MovieActorCrossRefDao getMovieActorCrossRefDao();

    public abstract MovieDirectorCrossRefDao getMovieDirectorCrossRefDao();

    public abstract MovieWriterCrossRefDao getMovieWriterCrossRefDao();

    public abstract MovieVideofileCrossRefDao getMovieVideofileCrossRefDao();

    public abstract MovieGenreCrossRefDao getMovieGenreCrossRefDao();

    public abstract VideoFileDao getVideoFileDao();

    public abstract MovieDao getMovieDao();

    public abstract ScanDirectoryDao getScanDirectoryDao();

    public abstract TrailerDao getTrailerDao();

    public abstract StagePhotoDao getStagePhotoDao();

    public abstract ShortcutDao getShortcutDao();

    public abstract SeasonDao getSeasonDao();

    public abstract VideoTagDao getVideoTagDao();

    public abstract MovieVideoTagCrossRefDao getMovieVideoTagCrossRefDao();

    public static MovieLibraryRoomDatabase getDatabase(final Context context) {
        if (sInstance == null) {
            synchronized (MovieLibraryRoomDatabase.class) {
                if (sInstance == null) {
                    sInstance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            MovieLibraryRoomDatabase.class, "movielibrary_db_v2")
//                            .createFromAsset("database/movielibrary_db_v2_version_1.db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return sInstance;
    }


}
