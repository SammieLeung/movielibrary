package com.hphtv.movielibrary;

import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.room.testing.MigrationTestHelper;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.TABLE;
import com.hphtv.movielibrary.roomdb.VIEW;

import org.jetbrains.annotations.NotNull;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

/**
 * author: Sam Leung
 * date:  2021/11/1
 */
@RunWith(AndroidJUnit4.class)
public class MigrationTest {
    private static final String TEST_DB = "migration-test";

    @Rule
    public MigrationTestHelper helper;

    public MigrationTest() {
        helper = new MigrationTestHelper(InstrumentationRegistry.getInstrumentation(),
                MovieLibraryRoomDatabase.class.getCanonicalName(),
                new FrameworkSQLiteOpenHelperFactory());
    }

    @Test
    public void migrateAll() throws IOException {
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
                MIGRATION_10_11,
                MIGRATION_11_12,
                MIGRATION_12_13,
                MIGRATION_13_14,
                MIGRATION_14_15
        };
        SupportSQLiteDatabase db = helper.createDatabase(TEST_DB, 1);
        db.execSQL("insert or ignore into " + TABLE.VIDEOFILE + " (vid,path,filename,episode,season) values (1,\"storage/emulate/0/\",\"测试数据\",\"01-08\",0)");
        // db has schema version 1. insert some data using SQL queries.
        // You cannot use DAO classes because they expect the latest schema.


        // Prepare for the next version.
        db.close();

        // Re-open the database with version 2 and provide
        // MIGRATION_1_2 as the migration process.
        db = helper.runMigrationsAndValidate(TEST_DB, 15, true, migrations);

        // MigrationTestHelper automatically verifies the schema changes,
        // but you need to validate that the data was migrated properly.
    }

    @Test
    public void migrateFrom18() throws IOException {
        SupportSQLiteDatabase db = helper.createDatabase(TEST_DB, 18);
        db.execSQL("insert or ignore into " + TABLE.VIDEOFILE + " (vid,path,device_path,dir_path,filename,is_scanned,keyword,add_time,last_playtime," +
                "season,episode,aired,resolution,video_source) values " +
                "(1,\"storage/emulate/0/Movies/1.mp4\",\"\",\"storage/emulated/0\",\"测试数据\",1,\"keywrod\",5565,66666,5,1,NULL,NULL,NULL)");
        db.close();
        db = helper.runMigrationsAndValidate(TEST_DB, 19, true, MIGRATION_18_19);

    }


    @Test
    public void migrate3_4() throws IOException {
        SupportSQLiteDatabase db = helper.createDatabase(TEST_DB, 3);
        db.execSQL("insert into videotag (vtid,tag,tag_name,flag,weight) values (1,\"movie\",NULL,0,0)");
        db.execSQL("insert into videotag (vtid,tag,tag_name,flag,weight) values (2,\"tv\",1,0,0)");
        db.execSQL("insert into videotag (vtid,tag,tag_name,flag,weight) values (3,\"tv\",2,0,0)");
        db.execSQL("insert into videotag (vtid,tag,tag_name,flag,weight) values (4,\"tv\",NULL,0,0)");
        db.execSQL("insert into videotag (vtid,tag,tag_name,flag,weight) values (5,\"tv\",NULL,0,0)");
        db.execSQL("insert into videotag (vtid,tag,tag_name,flag,weight) values (6,\"tv\",NULL,0,0)");
        db.execSQL("insert into movie_videotag_cross_ref (vtid,id) values (1,1)");
        db.execSQL("insert into movie_videotag_cross_ref (vtid,id) values (2,7)");
        db.execSQL("insert into movie_videotag_cross_ref (vtid,id) values (3,8)");
        db.execSQL("insert into movie_videotag_cross_ref (vtid,id) values (3,9)");
        db.execSQL("insert into movie_videotag_cross_ref (vtid,id) values (4,2)");
        db.execSQL("insert into movie_videotag_cross_ref (vtid,id) values (5,3)");
        db.execSQL("insert into movie_videotag_cross_ref (vtid,id) values (6,4)");
        db.execSQL("insert into movie_videotag_cross_ref (vtid,id) values (6,5)");
        db.close();
        db = helper.runMigrationsAndValidate(TEST_DB, 4, true, MIGRATION_3_4);
    }

    @Test
    public void migrate5_6() throws IOException {
        SupportSQLiteDatabase db = helper.createDatabase(TEST_DB, 5);

        db = helper.runMigrationsAndValidate(TEST_DB, 6, true, MIGRATION_5_6);
    }

    @Test
    public void migrate6_7() throws IOException {
        SupportSQLiteDatabase db = helper.createDatabase(TEST_DB, 6);

        db = helper.runMigrationsAndValidate(TEST_DB, 7, true, MIGRATION_6_7);
    }


    public Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull @NotNull SupportSQLiteDatabase database) {
//            database.execSQL("ALTER TABLE videofile ADD COLUMN watch_limit TEXT");
//            database.execSQL("ALTER TABLE video_tag ADD COLUMN weight INTEGER NOT NULL DEFAULT 0;");
            String tmp = "_OLD";
            database.execSQL("ALTER TABLE " + TABLE.VIDEOFILE + " RENAME TO " + TABLE.VIDEOFILE + tmp);
            database.execSQL("DROP INDEX index_videofile_path");
            database.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE.VIDEOFILE + " (`vid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `path` TEXT NOT NULL, `device_path` TEXT, `dir_path` TEXT, `filename` TEXT, `is_scanned` INTEGER NOT NULL DEFAULT 0, `keyword` TEXT, `add_time` INTEGER NOT NULL DEFAULT 0, `last_playtime` INTEGER NOT NULL DEFAULT 0, `season` INTEGER NOT NULL DEFAULT 0, `episode` INTEGER NOT NULL DEFAULT 0)");
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_videofile_path` ON " + TABLE.VIDEOFILE + " (`path`)");
            database.execSQL("INSERT INTO " + TABLE.VIDEOFILE + " SELECT vid,path,device_path,dir_path,filename,is_scanned,keyword,add_time,last_playtime,season,0 FROM " + TABLE.VIDEOFILE + tmp);
            database.execSQL("DROP TABLE " + TABLE.VIDEOFILE + tmp);
        }
    };

    public Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("DROP VIEW " + VIEW.MOVIE_DATAVIEW);
            database.execSQL("CREATE VIEW `" + VIEW.SEASON_DATAVIEW + "` AS SELECT M.id,V.season,SS.name,SS.poster,ss.episode_count FROM videofile AS V JOIN movie_videofile_cross_ref AS MVC ON V.path=MVC.path JOIN movie AS M ON M.id=MVC.id JOIN season AS SS ON SS.movie_id=M.id WHERE V.season=SS.season_number AND V.episode>0");
            database.execSQL("CREATE VIEW `" + VIEW.MOVIE_DATAVIEW + "` AS SELECT M.id,M.movie_id,M.title,M.pinyin,M.poster,M.ratings,M.year,M.source,M.type,M.ap,M.is_watched,VF.path AS file_uri,ST.uri AS dir_uri,ST.device_path AS device_uri,ST.name AS dir_name,ST.friendly_name AS dir_fname ,ST.access AS s_ap,G.name AS genre_name,M.add_time,M.last_playtime,M.is_favorite, SD.season,SD.name AS season_name,SD.poster AS season_poster,SD.episode_count FROM videofile AS VF JOIN shortcut AS ST  ON VF.dir_path=ST.uri JOIN device AS DEV ON DEV.path=ST.device_path OR ST.device_type > 5 JOIN movie_videofile_cross_ref AS MVCF ON MVCF.path=VF.path JOIN movie AS M ON MVCF.id=M.id LEFT OUTER JOIN movie_genre_cross_ref AS MGCF  ON M.id=MGCF.id LEFT OUTER JOIN genre AS G ON MGCF.genre_id = G.genre_id LEFT OUTER JOIN season_dataview AS SD ON SD.id=M.id");
        }
    };

    public Migration MIGRATION_3_4 = new Migration(3, 4) {
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

    public Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("DROP VIEW " + VIEW.HISTORY_MOVIE_DATAVIEW);
            database.execSQL("CREATE VIEW `" + VIEW.HISTORY_MOVIE_DATAVIEW + "` AS SELECT u.filename,u.keyword,u.path,u.last_playtime,u.episode,m.poster,m.source,m.title,m.ratings,m.ap,st.access AS s_ap,m.season,m.season_name,m.season_poster,sp.img_url AS stage_photo FROM unrecognizedfile_dataview AS u LEFT OUTER JOIN movie_dataview AS m ON u.path=m.file_uri LEFT OUTER JOIN stagephoto AS sp ON sp.movie_id=m.id LEFT OUTER JOIN shortcut AS st ON st.uri=u.dir_uri WHERE u.last_playtime!=0 GROUP BY u.path,m.source ORDER BY u.last_playtime DESC");
        }
    };

    public Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE " + TABLE.VIDEOFILE + " ADD  `resolution` TEXT");
            database.execSQL("ALTER TABLE " + TABLE.VIDEOFILE + " ADD  `video_source` TEXT");
        }
    };

    public Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {

            database.execSQL("DROP VIEW " + VIEW.MOVIE_DATAVIEW);
            database.execSQL("CREATE VIEW `" + VIEW.MOVIE_DATAVIEW + "` AS SELECT M.id,M.movie_id,M.title,M.pinyin,M.poster,M.ratings,M.year,M.source,M.type,M.ap,M.is_watched,VF.path AS file_uri,VF.video_source,VF.resolution,ST.uri AS dir_uri,ST.device_path AS device_uri,ST.name AS dir_name,ST.friendly_name AS dir_fname ,ST.access AS s_ap,G.name AS genre_name,M.add_time,M.last_playtime,M.is_favorite, SD.season,SD.name AS season_name,SD.poster AS season_poster,SD.episode_count FROM videofile AS VF JOIN shortcut AS ST  ON VF.dir_path=ST.uri JOIN device AS DEV ON DEV.path=ST.device_path OR ST.device_type > 5 JOIN movie_videofile_cross_ref AS MVCF ON MVCF.path=VF.path JOIN movie AS M ON MVCF.id=M.id LEFT OUTER JOIN movie_genre_cross_ref AS MGCF  ON M.id=MGCF.id LEFT OUTER JOIN genre AS G ON MGCF.genre_id = G.genre_id LEFT OUTER JOIN season_dataview AS SD ON SD.id=M.id");
        }
    };

    public Migration MIGRATION_7_8 = new Migration(7, 8) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("DROP VIEW " + VIEW.HISTORY_MOVIE_DATAVIEW);
            database.execSQL("DROP VIEW " + VIEW.SEASON_DATAVIEW);
            database.execSQL("DROP VIEW unrecognizedfile_dataview");

            database.execSQL("ALTER TABLE " + TABLE.VIDEOFILE + " ADD COLUMN aired TEXT");
            database.execSQL("UPDATE " + TABLE.VIDEOFILE + " SET season=-1 WHERE season=0");
            database.execSQL("UPDATE " + TABLE.VIDEOFILE + " SET episode=-1 WHERE episode=0");

            String tmp = "_OLD";
            database.execSQL("ALTER TABLE " + TABLE.VIDEOFILE + " RENAME TO " + TABLE.VIDEOFILE + tmp);
            database.execSQL("DROP INDEX index_videofile_path");
            database.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE.VIDEOFILE + " (`vid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `path` TEXT NOT NULL, `device_path` TEXT, `dir_path` TEXT, `filename` TEXT, `is_scanned` INTEGER NOT NULL DEFAULT 0, `keyword` TEXT, `add_time` INTEGER NOT NULL DEFAULT 0, `last_playtime` INTEGER NOT NULL DEFAULT 0, `season` INTEGER NOT NULL DEFAULT -1, `episode` INTEGER NOT NULL DEFAULT -1, `aired` TEXT, `resolution` TEXT, `video_source` TEXT)");
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_videofile_path` ON " + TABLE.VIDEOFILE + " (`path`)");
            database.execSQL("INSERT INTO " + TABLE.VIDEOFILE + " SELECT vid,path,device_path,dir_path,filename,is_scanned,keyword,add_time,last_playtime,season,episode,-1,resolution,video_source FROM " + TABLE.VIDEOFILE + tmp);
            database.execSQL("DROP TABLE " + TABLE.VIDEOFILE + tmp);

            database.execSQL("CREATE VIEW `" + VIEW.SEASON_DATAVIEW + "` AS SELECT M.id,V.season,SS.name,SS.poster,ss.episode_count FROM videofile AS V JOIN movie_videofile_cross_ref AS MVC ON V.path=MVC.path JOIN movie AS M ON M.id=MVC.id JOIN season AS SS ON SS.movie_id=M.id WHERE V.season=SS.season_number AND ( V.episode >= 0 OR V.aired!='' )");
            database.execSQL("CREATE VIEW `unrecognizedfile_dataview` AS SELECT VF.vid,VF.filename,VF.keyword,VF.path,ST.uri AS dir_uri,ST.access AS s_ap,DEV.path AS device_uri,VF.add_time,VF.last_playtime,VF.season,VF.episode,VF.aired FROM videofile AS VF JOIN shortcut AS ST ON VF.dir_path=ST.uri JOIN device AS DEV ON DEV.path=ST.device_path UNION SELECT VF.vid,VF.filename,VF.keyword,VF.path,ST.uri AS dir_uri,ST.access AS s_ap,NULL AS device_uri,VF.add_time,VF.last_playtime,VF.season,VF.episode,VF.aired FROM videofile AS VF JOIN shortcut AS ST ON VF.dir_path=ST.uri WHERE ST.device_type>5");
            database.execSQL("CREATE VIEW `" + VIEW.HISTORY_MOVIE_DATAVIEW + "` AS SELECT u.filename,u.keyword,u.path,u.last_playtime,u.episode,u.aired,m.poster,m.source,m.title,m.ratings,m.ap,st.access AS s_ap,m.season,m.season_name,m.season_poster,sp.img_url AS stage_photo FROM unrecognizedfile_dataview AS u LEFT OUTER JOIN movie_dataview AS m ON u.path=m.file_uri LEFT OUTER JOIN stagephoto AS sp ON sp.movie_id=m.id LEFT OUTER JOIN shortcut AS st ON st.uri=u.dir_uri WHERE u.last_playtime!=0 GROUP BY u.path,m.source ORDER BY u.last_playtime DESC");
        }
    };

    public Migration MIGRATION_8_9 = new Migration(8, 9) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("DROP VIEW " + VIEW.MOVIE_DATAVIEW);
            database.execSQL("CREATE VIEW `" + VIEW.MOVIE_DATAVIEW + "` AS SELECT M.id,M.movie_id,M.title,M.pinyin,M.poster,M.ratings,M.year,M.source,M.type,M.ap,M.is_watched,VF.path AS file_uri,VF.video_source,VF.resolution,ST.uri AS dir_uri,ST.device_path AS device_uri,ST.name AS dir_name,ST.friendly_name AS dir_fname ,ST.access AS s_ap,G.name AS genre_name,M.add_time,M.last_playtime,M.is_favorite, CASE WHEN SD.season IS NOT NULL THEN SD.season ELSE -1 END AS season,SD.name AS season_name,SD.poster AS season_poster,SD.episode_count FROM videofile AS VF JOIN shortcut AS ST  ON VF.dir_path=ST.uri JOIN device AS DEV ON DEV.path=ST.device_path OR ST.device_type > 5 JOIN movie_videofile_cross_ref AS MVCF ON MVCF.path=VF.path JOIN movie AS M ON MVCF.id=M.id LEFT OUTER JOIN movie_genre_cross_ref AS MGCF  ON M.id=MGCF.id LEFT OUTER JOIN genre AS G ON MGCF.genre_id = G.genre_id LEFT OUTER JOIN season_dataview AS SD ON SD.id=M.id");

//            database.execSQL("DELETE FROM "+TABLE.MOVIE_VIDEOTAG_CROSS_REF+" WHERE vtid = (SELECT vtid FROM "+ TABLE.VIDEO_TAG +" WHERE tag='variety_show')");
        }
    };

    public Migration MIGRATION_9_10 = new Migration(9, 10) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("DROP VIEW " + VIEW.HISTORY_MOVIE_DATAVIEW);
            database.execSQL("CREATE VIEW `" + VIEW.HISTORY_MOVIE_DATAVIEW + "` AS SELECT u.filename,u.keyword,u.path,u.last_playtime,u.episode,u.aired,m.poster,m.source,m.title,m.ratings,m.ap,st.access AS s_ap,m.season,m.season_name,m.season_poster,sp.img_url AS stage_photo FROM unrecognizedfile_dataview AS u LEFT OUTER JOIN movie_dataview AS m ON u.path=m.file_uri LEFT OUTER JOIN (SELECT * FROM stagephoto WHERE movie_id IN (SELECT DISTINCT id FROM movie) GROUP BY movie_id) AS sp ON sp.movie_id=m.id LEFT OUTER JOIN shortcut AS st ON st.uri=u.dir_uri WHERE u.last_playtime!=0 GROUP BY u.path,m.source ORDER BY u.last_playtime DESC");
        }
    };

    public Migration MIGRATION_10_11 = new Migration(10, 11) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("DROP VIEW " + VIEW.HISTORY_MOVIE_DATAVIEW);
            database.execSQL("CREATE VIEW `" + VIEW.HISTORY_MOVIE_DATAVIEW + "` AS SELECT u.filename,u.keyword,u.path,u.last_playtime,u.episode,u.aired,u.s_ap,mv.poster,mv.source,mv.title,mv.ratings,mv.ap,CASE WHEN s.season_number IS NOT NULL THEN s.season_number ELSE -1 END AS season,s.name AS season_name,s.poster AS season_poster,sp.img_url AS stage_photo FROM (SELECT * FROM unrecognizedfile_dataview WHERE last_playtime >0 ORDER BY last_playtime DESC) AS u JOIN (SELECT m.id,m.movie_id,m.title,m.ratings,m.source,m.type,m.poster,m.ap,mvcf.path FROM movie AS m JOIN movie_videofile_cross_ref AS mvcf ON mvcf.id = m.id ORDER BY m.movie_id) AS mv ON mv.path = u.path LEFT OUTER JOIN season AS s ON s.movie_id=mv.id AND u.season=s.season_number LEFT OUTER JOIN (SELECT * FROM (SELECT * FROM stagephoto ORDER BY movie_id,img_url DESC) GROUP BY movie_id) AS sp ON sp.movie_id=mv.id WHERE u.last_playtime>0 GROUP BY mv.movie_id ORDER BY u.last_playtime DESC");
        }
    };

    public Migration MIGRATION_11_12 = new Migration(11, 12) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("DROP VIEW " + VIEW.HISTORY_MOVIE_DATAVIEW);
            database.execSQL("CREATE VIEW `" + VIEW.HISTORY_MOVIE_DATAVIEW + "` AS SELECT u.filename,u.keyword,u.path,u.last_playtime,u.episode,u.aired,u.s_ap,mv.poster,mv.source,mv.title,mv.ratings,mv.ap,CASE WHEN s.season_number IS NOT NULL THEN s.season_number ELSE -1 END AS season,s.name AS season_name,s.poster AS season_poster,sp.img_url AS stage_photo FROM (SELECT * FROM unrecognizedfile_dataview WHERE last_playtime >0 ORDER BY last_playtime DESC) AS u JOIN (SELECT m.id,m.movie_id,m.title,m.ratings,m.source,m.type,m.poster,m.ap,mvcf.path FROM movie AS m JOIN movie_videofile_cross_ref AS mvcf ON mvcf.id = m.id ORDER BY m.movie_id) AS mv ON mv.path = u.path LEFT OUTER JOIN season AS s ON s.movie_id=mv.id AND u.season=s.season_number LEFT OUTER JOIN (SELECT * FROM (SELECT * FROM stagephoto ORDER BY movie_id,img_url DESC) GROUP BY movie_id) AS sp ON sp.movie_id=mv.id WHERE u.last_playtime>0 GROUP BY mv.movie_id,mv.source ORDER BY u.last_playtime DESC");
        }
    };

    public Migration MIGRATION_12_13 = new Migration(12, 13) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("DROP VIEW " + VIEW.HISTORY_MOVIE_DATAVIEW);
            database.execSQL("CREATE VIEW `" + VIEW.HISTORY_MOVIE_DATAVIEW + "` AS SELECT filename,keyword,path,last_playtime,episode,aired,s_ap,NULL AS poster,NULL AS source,NULL AS title,NULL AS ratings,NULL AS ap,NULL AS type,NULL AS season,NULL AS season_name,NULL AS season_poster,NULL AS stage_photo FROM unrecognizedfile_dataview WHERE last_playtime >0 AND path NOT IN (SELECT path FROM movie_videofile_cross_ref) UNION SELECT u.filename,u.keyword,u.path,max(u.last_playtime) AS last_playtime,u.episode,u.aired,u.s_ap,mv.poster,mv.source,mv.title,mv.ratings,mv.ap,mv.type,CASE WHEN s.season_number IS NOT NULL THEN s.season_number ELSE -1 END AS season,s.name AS season_name,s.poster AS season_poster,sp.img_url AS stage_photo FROM (SELECT * FROM unrecognizedfile_dataview WHERE last_playtime >0) AS u JOIN (SELECT m.id,m.movie_id,m.title,m.ratings,m.source,m.type,m.poster,m.ap,m.type,mvcf.path FROM movie AS m JOIN movie_videofile_cross_ref AS mvcf ON mvcf.id = m.id) AS mv ON mv.path = u.path LEFT OUTER JOIN season AS s ON s.movie_id=mv.id AND u.season=s.season_number LEFT OUTER JOIN (SELECT * FROM (SELECT * FROM stagephoto ORDER BY movie_id,img_url DESC) GROUP BY movie_id) AS sp ON sp.movie_id=mv.id GROUP BY mv.movie_id,mv.source ORDER BY last_playtime DESC");

        }
    };

    public Migration MIGRATION_13_14 = new Migration(13, 14) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("DROP VIEW " + VIEW.HISTORY_MOVIE_DATAVIEW);
            database.execSQL("CREATE VIEW `" + VIEW.HISTORY_MOVIE_DATAVIEW + "` AS SELECT filename,keyword,path,last_playtime,episode,aired,s_ap,NULL AS _mid,NULL AS movie_id,NULL AS poster,NULL AS source,NULL AS title,NULL AS ratings,NULL AS ap,NULL AS type,NULL AS season,NULL AS season_name,NULL AS season_poster,NULL AS stage_photo FROM unrecognizedfile_dataview WHERE last_playtime >0 AND path NOT IN (SELECT path FROM movie_videofile_cross_ref) UNION SELECT u.filename,u.keyword,u.path,max(u.last_playtime) AS last_playtime,u.episode,u.aired,u.s_ap,mv.id AS _mid,mv.movie_id,mv.poster,mv.source,mv.title,mv.ratings,mv.ap,mv.type,CASE WHEN s.season_number IS NOT NULL THEN s.season_number ELSE -1 END AS season,s.name AS season_name,s.poster AS season_poster,sp.img_url AS stage_photo FROM (SELECT * FROM unrecognizedfile_dataview WHERE last_playtime >0) AS u JOIN (SELECT m.id,m.movie_id,m.title,m.ratings,m.source,m.type,m.poster,m.ap,m.type,mvcf.path FROM movie AS m JOIN movie_videofile_cross_ref AS mvcf ON mvcf.id = m.id) AS mv ON mv.path = u.path LEFT OUTER JOIN season AS s ON s.movie_id=mv.id AND u.season=s.season_number LEFT OUTER JOIN (SELECT * FROM (SELECT * FROM stagephoto ORDER BY movie_id,img_url DESC) GROUP BY movie_id) AS sp ON sp.movie_id=mv.id GROUP BY mv.movie_id,mv.source ORDER BY last_playtime DESC");

        }
    };

    public Migration MIGRATION_14_15 = new Migration(14, 15) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("DROP VIEW unrecognizedfile_dataview");
            database.execSQL("DROP VIEW " + VIEW.HISTORY_MOVIE_DATAVIEW);
            database.execSQL("CREATE VIEW `" + VIEW.CONNECTED_FILE_DATAVIEW + "` AS SELECT VF.vid,VF.filename,VF.keyword,VF.path,ST.name AS dir_name,ST.uri AS dir_uri,ST.access AS s_ap,DEV.path AS device_uri,VF.add_time,VF.last_playtime,VF.season,VF.episode,VF.aired FROM videofile AS VF JOIN shortcut AS ST ON VF.dir_path=ST.uri JOIN device AS DEV ON DEV.path=ST.device_path UNION SELECT VF.vid,VF.filename,VF.keyword,VF.path,ST.name AS dir_name,ST.uri AS dir_uri,ST.access AS s_ap,NULL AS device_uri,VF.add_time,VF.last_playtime,VF.season,VF.episode,VF.aired FROM videofile AS VF JOIN shortcut AS ST ON VF.dir_path=ST.uri WHERE ST.device_type>5");
            database.execSQL("CREATE VIEW `" + VIEW.HISTORY_MOVIE_DATAVIEW + "` AS SELECT filename,keyword,path,last_playtime,episode,aired,s_ap,NULL AS _mid,NULL AS movie_id,NULL AS poster,NULL AS source,NULL AS title,NULL AS ratings,NULL AS ap,NULL AS type,NULL AS season,NULL AS season_name,NULL AS season_poster,NULL AS stage_photo FROM connected_file_dataview WHERE last_playtime >0 AND path NOT IN (SELECT path FROM movie_videofile_cross_ref) UNION SELECT u.filename,u.keyword,u.path,max(u.last_playtime) AS last_playtime,u.episode,u.aired,u.s_ap,mv.id AS _mid,mv.movie_id,mv.poster,mv.source,mv.title,mv.ratings,mv.ap,mv.type,CASE WHEN s.season_number IS NOT NULL THEN s.season_number ELSE -1 END AS season,s.name AS season_name,s.poster AS season_poster,sp.img_url AS stage_photo FROM (SELECT * FROM connected_file_dataview WHERE last_playtime >0) AS u JOIN (SELECT m.id,m.movie_id,m.title,m.ratings,m.source,m.type,m.poster,m.ap,m.type,mvcf.path FROM movie AS m JOIN movie_videofile_cross_ref AS mvcf ON mvcf.id = m.id) AS mv ON mv.path = u.path LEFT OUTER JOIN season AS s ON s.movie_id=mv.id AND u.season=s.season_number LEFT OUTER JOIN (SELECT * FROM (SELECT * FROM stagephoto ORDER BY movie_id,img_url DESC) GROUP BY movie_id) AS sp ON sp.movie_id=mv.id GROUP BY mv.movie_id,mv.source ORDER BY last_playtime DESC");
        }
    };


    public Migration MIGRATION_18_19 = new Migration(18, 19) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE videofile ADD COLUMN `last_position` INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE videofile ADD COLUMN `duration` INTEGER NOT NULL DEFAULT 0");
        }
    };
}
