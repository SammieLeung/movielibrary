package com.hphtv.movielibrary.roomdb;

import android.content.Context;
import android.database.Cursor;

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
import com.hphtv.movielibrary.roomdb.entity.dataview.SeasonDataView;
import com.hphtv.movielibrary.roomdb.entity.dataview.UnrecognizedFileDataView;
import com.hphtv.movielibrary.roomdb.entity.reference.MovieActorCrossRef;
import com.hphtv.movielibrary.roomdb.entity.reference.MovieDirectorCrossRef;
import com.hphtv.movielibrary.roomdb.entity.reference.MovieGenreCrossRef;
import com.hphtv.movielibrary.roomdb.entity.reference.MovieVideoFileCrossRef;
import com.hphtv.movielibrary.roomdb.entity.reference.MovieVideoTagCrossRef;
import com.hphtv.movielibrary.roomdb.entity.reference.MovieWriterCrossRef;

import org.jetbrains.annotations.NotNull;

/**
 * author: Sam Leung
 * date:  21-5-14
 */

@Database(entities = {Actor.class, Device.class, Director.class, Writer.class, Genre.class, Movie.class, MovieActorCrossRef.class,
        MovieDirectorCrossRef.class, MovieWriterCrossRef.class, MovieGenreCrossRef.class, MovieVideoFileCrossRef.class,
        ScanDirectory.class, VideoFile.class, Trailer.class, StagePhoto.class, Shortcut.class, GenreTag.class,
        Season.class, VideoTag.class, MovieVideoTagCrossRef.class},
        views = {MovieDataView.class, UnrecognizedFileDataView.class, HistoryMovieDataView.class, SeasonDataView.class},
        version = 11)
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
                    Migration[] migrations = new Migration[]{
                            MIGRATION_1_2,
                            MIGRATION_2_3,
                            MIGRATION_3_4,
                            MIGRATION_4_5,
                            MIGRATION_5_6,
                            MIGRATION_6_7,
                            MIGRATION_7_8,
                            MIGRATION_8_9,
                            MIGRATION_9_10,
                            MIGRATION_10_11};
                    sInstance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    MovieLibraryRoomDatabase.class, "movielibrary_db_v2")
                            .createFromAsset("database/movielibrary_db_v2_version_1.db")
                            .addMigrations(migrations)
                            .build();
                }
            }
        }
        return sInstance;
    }

    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull @NotNull SupportSQLiteDatabase database) {
            String tmp = "_OLD";
            database.execSQL("ALTER TABLE " + TABLE.VIDEOFILE + " RENAME TO " + TABLE.VIDEOFILE + tmp);
            database.execSQL("DROP INDEX index_videofile_path");
            database.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE.VIDEOFILE + " (`vid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `path` TEXT NOT NULL, `device_path` TEXT, `dir_path` TEXT, `filename` TEXT, `is_scanned` INTEGER NOT NULL DEFAULT 0, `keyword` TEXT, `add_time` INTEGER NOT NULL DEFAULT 0, `last_playtime` INTEGER NOT NULL DEFAULT 0, `season` INTEGER NOT NULL DEFAULT 0, `episode` INTEGER NOT NULL DEFAULT 0)");
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_videofile_path` ON " + TABLE.VIDEOFILE + " (`path`)");
            database.execSQL("INSERT INTO " + TABLE.VIDEOFILE + " SELECT vid,path,device_path,dir_path,filename,is_scanned,keyword,add_time,last_playtime,season,0 FROM " + TABLE.VIDEOFILE + tmp);
            database.execSQL("DROP TABLE " + TABLE.VIDEOFILE + tmp);
        }
    };

    public static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("DROP VIEW " + VIEW.MOVIE_DATAVIEW);
            database.execSQL("CREATE VIEW `" + VIEW.SEASON_DATAVIEW + "` AS SELECT M.id,V.season,SS.name,SS.poster,ss.episode_count FROM videofile AS V JOIN movie_videofile_cross_ref AS MVC ON V.path=MVC.path JOIN movie AS M ON M.id=MVC.id JOIN season AS SS ON SS.movie_id=M.id WHERE V.season=SS.season_number AND V.episode>0");
            database.execSQL("CREATE VIEW `" + VIEW.MOVIE_DATAVIEW + "` AS SELECT M.id,M.movie_id,M.title,M.pinyin,M.poster,M.ratings,M.year,M.source,M.type,M.ap,M.is_watched,VF.path AS file_uri,ST.uri AS dir_uri,ST.device_path AS device_uri,ST.name AS dir_name,ST.friendly_name AS dir_fname ,ST.access AS s_ap,G.name AS genre_name,M.add_time,M.last_playtime,M.is_favorite, SD.season,SD.name AS season_name,SD.poster AS season_poster,SD.episode_count FROM videofile AS VF JOIN shortcut AS ST  ON VF.dir_path=ST.uri JOIN device AS DEV ON DEV.path=ST.device_path OR ST.device_type > 5 JOIN movie_videofile_cross_ref AS MVCF ON MVCF.path=VF.path JOIN movie AS M ON MVCF.id=M.id LEFT OUTER JOIN movie_genre_cross_ref AS MGCF  ON M.id=MGCF.id LEFT OUTER JOIN genre AS G ON MGCF.genre_id = G.genre_id LEFT OUTER JOIN season_dataview AS SD ON SD.id=M.id");
        }
    };

    public static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            String tmp = "_OLD";
            database.execSQL("DROP INDEX index_videotag_tag_tag_name");
            Cursor cursor = database.query("SELECT vtid,tag FROM " + TABLE.VIDEO_TAG + " WHERE tag_name IS NULL GROUP BY tag ");
            while (cursor.moveToNext()) {
                String tag = cursor.getString(cursor.getColumnIndex("tag"));
                long vtid = cursor.getLong(cursor.getColumnIndex("vtid"));

                Cursor cursor2 = database.query("SELECT vtid FROM " + TABLE.VIDEO_TAG + " WHERE tag=\"" + tag + "\" AND tag_name IS NULL");
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("(");
                while (cursor2.moveToNext()) {
                    stringBuffer.append(cursor2.getLong(0) + ",");
                }
                stringBuffer.replace(stringBuffer.length() - 1, stringBuffer.length(), ")");

                database.execSQL("UPDATE " + TABLE.MOVIE_VIDEOTAG_CROSS_REF + " SET vtid=" + vtid + " WHERE vtid IN " + stringBuffer.toString());

                database.execSQL("DELETE FROM " + TABLE.VIDEO_TAG + " WHERE tag=\"" + tag + "\" AND tag_name IS NULL AND vtid!=" + vtid);
                database.execSQL("UPDATE " + TABLE.VIDEO_TAG + " SET tag_name='' WHERE tag=\"" + tag + "\" AND  tag_name IS NULL");
            }

            database.execSQL("ALTER TABLE " + TABLE.VIDEO_TAG + " RENAME TO " + TABLE.VIDEO_TAG + tmp);
            database.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE.VIDEO_TAG + " (`vtid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `tag` TEXT, `tag_name` TEXT NOT NULL DEFAULT '', `flag` INTEGER NOT NULL, `weight` INTEGER NOT NULL)");
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_videotag_tag_tag_name` ON `" + TABLE.VIDEO_TAG + "` (`tag`, `tag_name`)");
            database.execSQL("INSERT INTO " + TABLE.VIDEO_TAG + " SELECT vtid,tag,tag_name,flag,weight FROM " + TABLE.VIDEO_TAG + tmp);
            database.execSQL("DROP TABLE " + TABLE.VIDEO_TAG + tmp);
        }
    };

    public static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("DROP VIEW " + VIEW.HISTORY_MOVIE_DATAVIEW);
            database.execSQL("CREATE VIEW `" + VIEW.HISTORY_MOVIE_DATAVIEW + "` AS SELECT u.filename,u.keyword,u.path,u.last_playtime,u.episode,m.poster,m.source,m.title,m.ratings,m.ap,st.access AS s_ap,m.season,m.season_name,m.season_poster,sp.img_url AS stage_photo FROM unrecognizedfile_dataview AS u LEFT OUTER JOIN movie_dataview AS m ON u.path=m.file_uri LEFT OUTER JOIN stagephoto AS sp ON sp.movie_id=m.id LEFT OUTER JOIN shortcut AS st ON st.uri=u.dir_uri WHERE u.last_playtime!=0 GROUP BY u.path,m.source ORDER BY u.last_playtime DESC");
        }
    };

    public static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE " + TABLE.VIDEOFILE + " ADD  `resolution` TEXT");
            database.execSQL("ALTER TABLE " + TABLE.VIDEOFILE + " ADD  `video_source` TEXT");
        }
    };

    public static final Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("DROP VIEW " + VIEW.MOVIE_DATAVIEW);
            database.execSQL("CREATE VIEW `" + VIEW.MOVIE_DATAVIEW + "` AS SELECT M.id,M.movie_id,M.title,M.pinyin,M.poster,M.ratings,M.year,M.source,M.type,M.ap,M.is_watched,VF.path AS file_uri,VF.video_source,VF.resolution,ST.uri AS dir_uri,ST.device_path AS device_uri,ST.name AS dir_name,ST.friendly_name AS dir_fname ,ST.access AS s_ap,G.name AS genre_name,M.add_time,M.last_playtime,M.is_favorite, SD.season,SD.name AS season_name,SD.poster AS season_poster,SD.episode_count FROM videofile AS VF JOIN shortcut AS ST  ON VF.dir_path=ST.uri JOIN device AS DEV ON DEV.path=ST.device_path OR ST.device_type > 5 JOIN movie_videofile_cross_ref AS MVCF ON MVCF.path=VF.path JOIN movie AS M ON MVCF.id=M.id LEFT OUTER JOIN movie_genre_cross_ref AS MGCF  ON M.id=MGCF.id LEFT OUTER JOIN genre AS G ON MGCF.genre_id = G.genre_id LEFT OUTER JOIN season_dataview AS SD ON SD.id=M.id");
        }
    };

    public static final Migration MIGRATION_7_8=new Migration(7,8) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("DROP VIEW "+VIEW.HISTORY_MOVIE_DATAVIEW);
            database.execSQL("DROP VIEW "+VIEW.SEASON_DATAVIEW);
            database.execSQL("DROP VIEW "+VIEW.UNRECOGNIZEDFILE_DATAVIEW);

            database.execSQL("ALTER TABLE "+TABLE.VIDEOFILE+" ADD COLUMN aired TEXT");
            database.execSQL("UPDATE "+TABLE.VIDEOFILE+" SET season=-1 WHERE season=0");
            database.execSQL("UPDATE "+TABLE.VIDEOFILE+" SET episode=-1 WHERE episode=0");

            String tmp = "_OLD";
            database.execSQL("ALTER TABLE " + TABLE.VIDEOFILE + " RENAME TO " + TABLE.VIDEOFILE + tmp);
            database.execSQL("DROP INDEX index_videofile_path");
            database.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE.VIDEOFILE + " (`vid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `path` TEXT NOT NULL, `device_path` TEXT, `dir_path` TEXT, `filename` TEXT, `is_scanned` INTEGER NOT NULL DEFAULT 0, `keyword` TEXT, `add_time` INTEGER NOT NULL DEFAULT 0, `last_playtime` INTEGER NOT NULL DEFAULT 0, `season` INTEGER NOT NULL DEFAULT -1, `episode` INTEGER NOT NULL DEFAULT -1, `aired` TEXT, `resolution` TEXT, `video_source` TEXT)");
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_videofile_path` ON " + TABLE.VIDEOFILE + " (`path`)");
            database.execSQL("INSERT INTO " + TABLE.VIDEOFILE + " SELECT vid,path,device_path,dir_path,filename,is_scanned,keyword,add_time,last_playtime,season,episode,-1,resolution,video_source FROM " + TABLE.VIDEOFILE + tmp);
            database.execSQL("DROP TABLE " + TABLE.VIDEOFILE + tmp);

            database.execSQL("CREATE VIEW `"+VIEW.SEASON_DATAVIEW+"` AS SELECT M.id,V.season,SS.name,SS.poster,ss.episode_count FROM videofile AS V JOIN movie_videofile_cross_ref AS MVC ON V.path=MVC.path JOIN movie AS M ON M.id=MVC.id JOIN season AS SS ON SS.movie_id=M.id WHERE V.season=SS.season_number AND ( V.episode >= 0 OR V.aired!='' )");
            database.execSQL("CREATE VIEW `"+VIEW.UNRECOGNIZEDFILE_DATAVIEW+"` AS SELECT VF.vid,VF.filename,VF.keyword,VF.path,ST.uri AS dir_uri,ST.access AS s_ap,DEV.path AS device_uri,VF.add_time,VF.last_playtime,VF.season,VF.episode,VF.aired FROM videofile AS VF JOIN shortcut AS ST ON VF.dir_path=ST.uri JOIN device AS DEV ON DEV.path=ST.device_path UNION SELECT VF.vid,VF.filename,VF.keyword,VF.path,ST.uri AS dir_uri,ST.access AS s_ap,NULL AS device_uri,VF.add_time,VF.last_playtime,VF.season,VF.episode,VF.aired FROM videofile AS VF JOIN shortcut AS ST ON VF.dir_path=ST.uri WHERE ST.device_type>5");
            database.execSQL("CREATE VIEW `"+VIEW.HISTORY_MOVIE_DATAVIEW+"` AS SELECT u.filename,u.keyword,u.path,u.last_playtime,u.episode,u.aired,m.poster,m.source,m.title,m.ratings,m.ap,st.access AS s_ap,m.season,m.season_name,m.season_poster,sp.img_url AS stage_photo FROM unrecognizedfile_dataview AS u LEFT OUTER JOIN movie_dataview AS m ON u.path=m.file_uri LEFT OUTER JOIN stagephoto AS sp ON sp.movie_id=m.id LEFT OUTER JOIN shortcut AS st ON st.uri=u.dir_uri WHERE u.last_playtime!=0 GROUP BY u.path,m.source ORDER BY u.last_playtime DESC");
        }
    };

    public static final Migration MIGRATION_8_9=new Migration(8,9) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("DROP VIEW "+VIEW.MOVIE_DATAVIEW);
            database.execSQL("CREATE VIEW `"+VIEW.MOVIE_DATAVIEW+"` AS SELECT M.id,M.movie_id,M.title,M.pinyin,M.poster,M.ratings,M.year,M.source,M.type,M.ap,M.is_watched,VF.path AS file_uri,VF.video_source,VF.resolution,ST.uri AS dir_uri,ST.device_path AS device_uri,ST.name AS dir_name,ST.friendly_name AS dir_fname ,ST.access AS s_ap,G.name AS genre_name,M.add_time,M.last_playtime,M.is_favorite, CASE WHEN SD.season IS NOT NULL THEN SD.season ELSE -1 END AS season,SD.name AS season_name,SD.poster AS season_poster,SD.episode_count FROM videofile AS VF JOIN shortcut AS ST  ON VF.dir_path=ST.uri JOIN device AS DEV ON DEV.path=ST.device_path OR ST.device_type > 5 JOIN movie_videofile_cross_ref AS MVCF ON MVCF.path=VF.path JOIN movie AS M ON MVCF.id=M.id LEFT OUTER JOIN movie_genre_cross_ref AS MGCF  ON M.id=MGCF.id LEFT OUTER JOIN genre AS G ON MGCF.genre_id = G.genre_id LEFT OUTER JOIN season_dataview AS SD ON SD.id=M.id");

//            database.execSQL("DELETE FROM "+TABLE.MOVIE_VIDEOTAG_CROSS_REF+" WHERE vtid = (SELECT vtid FROM "+ TABLE.VIDEO_TAG +" WHERE tag='variety_show')");
        }
    };

    public static final Migration MIGRATION_9_10=new Migration(9,10) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("DROP VIEW "+VIEW.HISTORY_MOVIE_DATAVIEW);
            database.execSQL("CREATE VIEW `"+VIEW.HISTORY_MOVIE_DATAVIEW+"` AS SELECT u.filename,u.keyword,u.path,u.last_playtime,u.episode,u.aired,m.poster,m.source,m.title,m.ratings,m.ap,st.access AS s_ap,m.season,m.season_name,m.season_poster,sp.img_url AS stage_photo FROM unrecognizedfile_dataview AS u LEFT OUTER JOIN movie_dataview AS m ON u.path=m.file_uri LEFT OUTER JOIN (SELECT * FROM stagephoto WHERE movie_id IN (SELECT DISTINCT id FROM movie) GROUP BY movie_id) AS sp ON sp.movie_id=m.id LEFT OUTER JOIN shortcut AS st ON st.uri=u.dir_uri WHERE u.last_playtime!=0 GROUP BY u.path,m.source ORDER BY u.last_playtime DESC");
        }
    };

    public static final Migration MIGRATION_10_11=new Migration(10,11) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("DROP VIEW "+VIEW.HISTORY_MOVIE_DATAVIEW);
            database.execSQL("CREATE VIEW `"+VIEW.HISTORY_MOVIE_DATAVIEW+"` AS SELECT u.filename,u.keyword,u.path,u.last_playtime,u.episode,u.aired,u.s_ap,mv.poster,mv.source,mv.title,mv.ratings,mv.ap,CASE WHEN s.season_number IS NOT NULL THEN s.season_number ELSE -1 END AS season,s.name AS season_name,s.poster AS season_poster,sp.img_url AS stage_photo FROM (SELECT * FROM unrecognizedfile_dataview WHERE last_playtime >0 ORDER BY last_playtime DESC) AS u JOIN (SELECT m.id,m.movie_id,m.title,m.ratings,m.source,m.type,m.poster,m.ap,mvcf.path FROM movie AS m JOIN movie_videofile_cross_ref AS mvcf ON mvcf.id = m.id ORDER BY m.movie_id) AS mv ON mv.path = u.path LEFT OUTER JOIN season AS s ON s.movie_id=mv.id AND u.season=s.season_number LEFT OUTER JOIN (SELECT * FROM (SELECT * FROM stagephoto ORDER BY movie_id,img_url DESC) GROUP BY movie_id) AS sp ON sp.movie_id=mv.id WHERE u.last_playtime>0 GROUP BY mv.movie_id ORDER BY u.last_playtime DESC");
        }
    };
}
