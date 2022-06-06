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
import java.util.ArrayList;
import java.util.List;

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
                MIGRATION_4_5
        };
        SupportSQLiteDatabase db = helper.createDatabase(TEST_DB, 1);
        db.execSQL("insert or ignore into " + TABLE.VIDEOFILE + " (vid,path,filename,episode,season) values (1,\"storage/emulate/0/\",\"测试数据\",\"01-08\",0)");
        // db has schema version 1. insert some data using SQL queries.
        // You cannot use DAO classes because they expect the latest schema.


        // Prepare for the next version.
        db.close();

        // Re-open the database with version 2 and provide
        // MIGRATION_1_2 as the migration process.
        db = helper.runMigrationsAndValidate(TEST_DB, 5, true, migrations);

        // MigrationTestHelper automatically verifies the schema changes,
        // but you need to validate that the data was migrated properly.
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

                Cursor cursor2=database.query("SELECT vtid FROM "+TABLE.VIDEO_TAG+" WHERE tag=\""+tag+"\" AND tag_name IS NULL");
                StringBuffer stringBuffer=new StringBuffer();
                stringBuffer.append("(");
                while (cursor2.moveToNext()){
                    stringBuffer.append(cursor2.getLong(0)+",");
                }
                stringBuffer.replace(stringBuffer.length()-1,stringBuffer.length(),")");

                database.execSQL("UPDATE "+TABLE.MOVIE_VIDEOTAG_CROSS_REF+" SET vtid="+vtid+" WHERE vtid IN "+stringBuffer.toString());

                database.execSQL("DELETE FROM "+TABLE.VIDEO_TAG+" WHERE tag=\""+tag+"\" AND tag_name IS NULL AND vtid!="+vtid);
                database.execSQL("UPDATE "+TABLE.VIDEO_TAG+" SET tag_name='' WHERE tag=\""+tag+"\" AND  tag_name IS NULL");
            }

            database.execSQL("ALTER TABLE " + TABLE.VIDEO_TAG + " RENAME TO " + TABLE.VIDEO_TAG + tmp);
            database.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE.VIDEO_TAG + " (`vtid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `tag` TEXT, `tag_name` TEXT NOT NULL DEFAULT '', `flag` INTEGER NOT NULL, `weight` INTEGER NOT NULL)");
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_videotag_tag_tag_name` ON `"+TABLE.VIDEO_TAG+"` (`tag`, `tag_name`)");
            database.execSQL("INSERT INTO " + TABLE.VIDEO_TAG + " SELECT vtid,tag,tag_name,flag,weight FROM " + TABLE.VIDEO_TAG + tmp);
            database.execSQL("DROP TABLE " + TABLE.VIDEO_TAG + tmp);
        }
    };

    public Migration MIGRATION_4_5=new Migration(4,5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("DROP VIEW "+VIEW.HISTORY_MOVIE_DATAVIEW);
            database.execSQL("CREATE VIEW `"+VIEW.HISTORY_MOVIE_DATAVIEW+"` AS SELECT u.filename,u.keyword,u.path,u.last_playtime,u.episode,m.poster,m.source,m.title,m.ratings,m.ap,st.access AS s_ap,m.season,m.season_name,m.season_poster,sp.img_url AS stage_photo FROM unrecognizedfile_dataview AS u LEFT OUTER JOIN movie_dataview AS m ON u.path=m.file_uri LEFT OUTER JOIN stagephoto AS sp ON sp.movie_id=m.id LEFT OUTER JOIN shortcut AS st ON st.uri=u.dir_uri WHERE u.last_playtime!=0 GROUP BY u.path,m.source ORDER BY u.last_playtime DESC");
        }
    };
}
