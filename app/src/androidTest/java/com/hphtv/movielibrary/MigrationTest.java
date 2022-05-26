package com.hphtv.movielibrary;

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
         Migration[] migrations=new Migration[]{
                MIGRATION_1_2,
                MIGRATION_2_3
        };
        SupportSQLiteDatabase db = helper.createDatabase(TEST_DB, 1);
        db.execSQL("insert or ignore into "+TABLE.VIDEOFILE+" (vid,path,filename,episode,season) values (1,\"storage/emulate/0/\",\"测试数据\",\"01-08\",0)");
        // db has schema version 1. insert some data using SQL queries.
        // You cannot use DAO classes because they expect the latest schema.


        // Prepare for the next version.
        db.close();

        // Re-open the database with version 2 and provide
        // MIGRATION_1_2 as the migration process.
        db = helper.runMigrationsAndValidate(TEST_DB, 3, true, migrations);

        // MigrationTestHelper automatically verifies the schema changes,
        // but you need to validate that the data was migrated properly.
    }



    public Migration MIGRATION_1_2=new Migration(1,2) {
        @Override
        public void migrate(@NonNull @NotNull SupportSQLiteDatabase database) {
//            database.execSQL("ALTER TABLE videofile ADD COLUMN watch_limit TEXT");
//            database.execSQL("ALTER TABLE video_tag ADD COLUMN weight INTEGER NOT NULL DEFAULT 0;");
            String tmp="_OLD";
            database.execSQL("ALTER TABLE "+TABLE.VIDEOFILE+" RENAME TO "+TABLE.VIDEOFILE+tmp);
            database.execSQL("DROP INDEX index_videofile_path" );
            database.execSQL("CREATE TABLE IF NOT EXISTS "+TABLE.VIDEOFILE+" (`vid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `path` TEXT NOT NULL, `device_path` TEXT, `dir_path` TEXT, `filename` TEXT, `is_scanned` INTEGER NOT NULL DEFAULT 0, `keyword` TEXT, `add_time` INTEGER NOT NULL DEFAULT 0, `last_playtime` INTEGER NOT NULL DEFAULT 0, `season` INTEGER NOT NULL DEFAULT 0, `episode` INTEGER NOT NULL DEFAULT 0)");
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_videofile_path` ON "+TABLE.VIDEOFILE+" (`path`)");
            database.execSQL("INSERT INTO "+TABLE.VIDEOFILE+" SELECT vid,path,device_path,dir_path,filename,is_scanned,keyword,add_time,last_playtime,season,0 FROM "+TABLE.VIDEOFILE+tmp);
            database.execSQL("DROP TABLE "+TABLE.VIDEOFILE+tmp);
        }
    };

    public Migration MIGRATION_2_3=new Migration(2,3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("DROP VIEW "+ VIEW.MOVIE_DATAVIEW);
            database.execSQL("CREATE VIEW `"+VIEW.SEASON_DATAVIEW+"` AS SELECT M.id,V.season,SS.name,SS.poster,ss.episode_count FROM videofile AS V JOIN movie_videofile_cross_ref AS MVC ON V.path=MVC.path JOIN movie AS M ON M.id=MVC.id JOIN season AS SS ON SS.movie_id=M.id WHERE V.season=SS.season_number AND V.episode>0");
            database.execSQL("CREATE VIEW `"+ VIEW.MOVIE_DATAVIEW+"` AS SELECT M.id,M.movie_id,M.title,M.pinyin,M.poster,M.ratings,M.year,M.source,M.type,M.ap,M.is_watched,VF.path AS file_uri,ST.uri AS dir_uri,ST.device_path AS device_uri,ST.name AS dir_name,ST.friendly_name AS dir_fname ,ST.access AS s_ap,G.name AS genre_name,M.add_time,M.last_playtime,M.is_favorite, SD.season,SD.name AS season_name,SD.poster AS season_poster,SD.episode_count FROM videofile AS VF JOIN shortcut AS ST  ON VF.dir_path=ST.uri JOIN device AS DEV ON DEV.path=ST.device_path OR ST.device_type > 5 JOIN movie_videofile_cross_ref AS MVCF ON MVCF.path=VF.path JOIN movie AS M ON MVCF.id=M.id LEFT OUTER JOIN movie_genre_cross_ref AS MGCF  ON M.id=MGCF.id LEFT OUTER JOIN genre AS G ON MGCF.genre_id = G.genre_id LEFT OUTER JOIN season_dataview AS SD ON SD.id=M.id");
        }
    };


}
