package com.hphtv.movielibrary.roomdb;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

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
import com.hphtv.movielibrary.roomdb.entity.reference.MovieActorCrossRef;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.hphtv.movielibrary.roomdb.entity.reference.MovieDirectorCrossRef;
import com.hphtv.movielibrary.roomdb.entity.reference.MovieGenreCrossRef;
import com.hphtv.movielibrary.roomdb.entity.reference.MovieVideoFileCrossRef;
import com.hphtv.movielibrary.roomdb.entity.ScanDirectory;
import com.hphtv.movielibrary.roomdb.entity.StagePhoto;
import com.hphtv.movielibrary.roomdb.entity.Trailer;
import com.hphtv.movielibrary.roomdb.entity.dataview.UnrecognizedFileDataView;
import com.hphtv.movielibrary.roomdb.entity.VideoFile;

import org.jetbrains.annotations.NotNull;

/**
 * author: Sam Leung
 * date:  21-5-14
 */

@Database(entities = {Actor.class, Device.class, Director.class, Genre.class, Movie.class, MovieActorCrossRef.class,
        MovieDirectorCrossRef.class, MovieGenreCrossRef.class, MovieVideoFileCrossRef.class,
        ScanDirectory.class, VideoFile.class, Trailer.class, StagePhoto.class}, views = {MovieDataView.class, UnrecognizedFileDataView.class}, version = 1)
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
//                            .createFromAsset("database/moviedb_v2_version_1.db")
//                            .addMigrations(MIGRATION_1_2,MIGRATION_2_3)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return sInstance;
    }

    static final Migration MIGRATION_1_2=new Migration(1,2) {
        @Override
        public void migrate(@NonNull @NotNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE UNIQUE INDEX `index_scan_directory_path` ON `scan_directory` (`path`)");
        }
    };

    static final Migration MIGRATION_2_3=new Migration(2,3) {
        @Override
        public void migrate(@NonNull @NotNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE `videofile` ADD COLUMN `dir_path` TEXT");
        }
    };

}
