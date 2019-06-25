package com.hphtv.movielibrary.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Movie SQLiteOpenHelper
 *
 * @author tchip
 */
public class MovieDBHelper extends SQLiteOpenHelper {
    private static final String TAG = "MovieDBHelper";
    private static final int DB_VERSION = 21;
    private static final String DB_NAME = "MovieLibrary.db";
    public static final String TABLE_MOVIE = "tb_movie";
    public static final String TABLE_TRAILER = "tb_trailer";
    public static final String TABLE_GENRES = "tb_genres";
    public static final String TABLE_DEVICE = "tb_device";
    public static final String TABLE_VIDEOFILE = "tb_videofile";
    public static final String TABLE_FAVORITE = "tb_favorite";
    public static final String TABLE_POSTERPROVIDER = "tb_posterprovider";
    public static final String TABLE_HISTORY = "tb_history";
    public static final String TABLE_DIRECTORY = "tb_directory";
    public static final String TABLE_MOVIEWRAPPER = "tb_moviewrapper";

    private static volatile MovieDBHelper instance = null;
    private String[] movie_columns = new String[]{
            "id integer primary key", // id/电影id/主键
            "movie_id text default -1",//
            "wrapper_id integer",
            "title text",// 电影标题
            "otitle text",// 电影原名(英文名)
            "alt text",// 电影条目页面url
            "images text",// 电影封面(大 中 小) json对象
            "rating text",// 电影评分 (最低分/最高分/得分/描述) json对象
            "pub_dates text",// 上映日期(国外\国内)json数组
            "year text",// 上映年份
            "subtype text",// 条目分类, movie或者tv series
            "aka text",// 又名 json数组
            "m_url text",//移动版条目页url
            "r_count integer",// 评分人数
            "w_count integer",// 想看人数
            "c_count integer",// 看过人数
            // "d_count integer",//在看人数
            "directors text",// 导演 json对象
            "casts text",// 主演 json对象
            "writers text",// 编剧 json对象
            "website text",// 官网
            "doubansite text",// 豆瓣小站
            "languages text",// 语言
            "durations text",// 片长 json数组
            "genres text",// 影片类型 json数组
            "countries text",// 制片国家 json数组
            "summary text",// 简介
            "cm_count integer",// 短评数量
            "rw_count integer",// 影评数量
            "seasons_count text",// 总季数
            "current_season text",// 当前第几季(tv only)
            "episodes text",
            "schedule_url text",// 影讯页URL(movie only)
            "trailer_urls text",// 预告片URL，对高级用户以上开放，最多开放4个地址
            "photos text",// 电影剧照，前10张
            "pop_rws text", // 影评，前10条
            "local_path text",//本地路径
            "uptime text",
            "title_pinyin text",//标题拼音
            "addtime text",//电影添加时间
            "api integer default 0"//0 douban 1 imdb 2 mtime
    };

    private String[] videofile_columns = new String[]{
            "id integer primary key",
            "wrapper_id integer",
            "uri text",
            "filename text",
            "search_name text",
            "thumbnail text",//缩略图地址
            "thumbnail_s text",//小缩略图
            "dir_id text",
            "dev_id integer",
            "is_matched integer",
            "title_pinyin text"
    };

    private String[] device_columns = new String[]{
            "id integer primary key",
            "type integer",
            "name text",
            "path text UNIQUE",
            "connect_state integer default 0",//扫描状态
    };

    private String[] directory_colums = new String[]{
            "id text primary key",
            "name text",
            "parent_id integer",
            "video_number integer",//视频数
            "uri text UNIQUE",//uri
            "path text",//路径
            "matched_video integer",//已匹配视频
            "is_encrypted integer default 0",//加密状态
            "scan_state integer default 0",//扫描状态
    };

    private String[] trailer_columns = new String[]{
            "id integer", // id/主键
            "movie_id integer",//电影id主键
            "title text",// 电影标题
            "photo text",//封面
            "duration text",//时长
            "pub_date text",//播放日期
            "alt text",//预告片地址
            "primary key (id,movie_id)"
    };

    private String[] genres_columns = new String[]{
            "id integer primary key",//主键
            "name text"
    };

    private String[] favortie_columns = new String[]{
            "id integer primary key",//主键
            "wrapper_id integer",
    };

    private String[] history_columns = new String[]{
            "id integer primary key",//主键
            "wrapper_id integer",
            "time text",//播放时间戳
            "last_play_time text"//上次播放时间
    };

    private String[] posterprovider_columns = new String[]{
            "id integer primary key",
            "poster text"
    };

    private String[] moviewrapper_columns = new String[]{
            "id integer primary key",
            "scraper_infos text",
            "file_ids text",
            "dev_ids text",
            "dir_ids text",
            "title text",
            "title_pinyin text",
            "poster text",
            "average text"
    };

    private MovieDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    /**
     * 创建实例
     *
     * @param context
     * @return
     */
    public static synchronized MovieDBHelper getInstance(Context context) {
        if (instance == null) {
            synchronized (MovieDBHelper.class) {
                if (instance == null)
                    instance = new MovieDBHelper(context);
            }
        }
        return instance;
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String createMovieSql = CreateSqlParamasString(TABLE_MOVIE, movie_columns);
        String createMovieTrailerSql = CreateSqlParamasString(TABLE_TRAILER, trailer_columns);
        String createGenersSql = CreateSqlParamasString(TABLE_GENRES, genres_columns);
        String createDeviceSql = CreateSqlParamasString(TABLE_DEVICE, device_columns);
        String createVideoFileSql = CreateSqlParamasString(TABLE_VIDEOFILE, videofile_columns);
        String createFavoriteSql = CreateSqlParamasString(TABLE_FAVORITE, favortie_columns);
        String createPosterproviderSql = CreateSqlParamasString(TABLE_POSTERPROVIDER, posterprovider_columns);
        String createHistorySql = CreateSqlParamasString(TABLE_HISTORY, history_columns);
        String createDirectorySql = CreateSqlParamasString(TABLE_DIRECTORY, directory_colums);
        String createMovieWrapperSql = CreateSqlParamasString(TABLE_MOVIEWRAPPER, moviewrapper_columns);

        sqLiteDatabase.execSQL(createMovieSql);
        sqLiteDatabase.execSQL(createMovieTrailerSql);
        sqLiteDatabase.execSQL(createGenersSql);
        sqLiteDatabase.execSQL(createDeviceSql);
        sqLiteDatabase.execSQL(createVideoFileSql);
        sqLiteDatabase.execSQL(createFavoriteSql);
        sqLiteDatabase.execSQL(createPosterproviderSql);
        sqLiteDatabase.execSQL(createHistorySql);
        sqLiteDatabase.execSQL(createDirectorySql);
        sqLiteDatabase.execSQL(createMovieWrapperSql);
    }


    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion,
                          int newVersion) {
        Log.v(TAG, "onUpgrade current db version is " + newVersion + ";from oldVersion " + oldVersion);

        upgrade(sqLiteDatabase, oldVersion, newVersion);


    }

    private void upgrade(SQLiteDatabase sqLiteDatabase, int oldVersion,
                         int newVersion) {
        Log.v(TAG, "upgrade current db version is " + newVersion + ";from oldVersion " + oldVersion);
        switch (oldVersion) {
            case 20:
                dropTable(sqLiteDatabase, TABLE_MOVIE);
                dropTable(sqLiteDatabase, TABLE_TRAILER);
                dropTable(sqLiteDatabase, TABLE_GENRES);
                dropTable(sqLiteDatabase, TABLE_DEVICE);
                dropTable(sqLiteDatabase, TABLE_VIDEOFILE);
                dropTable(sqLiteDatabase, TABLE_FAVORITE);
                dropTable(sqLiteDatabase, TABLE_POSTERPROVIDER);
                dropTable(sqLiteDatabase, TABLE_HISTORY);
                onCreate(sqLiteDatabase);
                break;
            case 19:
                sqLiteDatabase.beginTransaction();
                addAColumn(sqLiteDatabase, TABLE_VIDEOFILE, "mtime_movie_id integer default -1");
                sqLiteDatabase.setTransactionSuccessful();
                sqLiteDatabase.endTransaction();
                upgrade(sqLiteDatabase, 20, newVersion);
                break;
            case 18:
                sqLiteDatabase.beginTransaction();
                renameColmunName(sqLiteDatabase, TABLE_MOVIE, new String[]{
                        "id integer primary key", // id/电影id/主键
                        "movie_id text default -1",//
                        "title text",// 电影标题
                        "otitle text",// 电影原名(英文名)
                        "alt text",// 电影条目页面url
                        "images text",// 电影封面(大 中 小) json对象
                        "rating text",// 电影评分 (最低分/最高分/得分/描述) json对象
                        "pub_dates text",// 上映日期(国外\国内)json数组
                        "year text",// 上映年份
                        "subtype text",// 条目分类, movie或者tv series
                        "imdbid text", //
                        "imdburl text", //
                        "aka text",// 又名 json数组
                        "m_url text",//移动版条目页url
                        "r_count integer",// 评分人数
                        "w_count integer",// 想看人数
                        "c_count integer",// 看过人数
                        // "d_count integer",//在看人数
                        "directors text",// 导演 json对象
                        "casts text",// 主演 json对象
                        "writers text",// 编剧 json对象
                        "website text",// 官网
                        "doubansite text",// 豆瓣小站
                        "languages text",// 语言
                        "durations text",// 片长 json数组
                        "genres text",// 影片类型 json数组
                        "countries text",// 制片国家 json数组
                        "summary text",// 简介
                        "cm_count integer",// 短评数量
                        "rw_count integer",// 影评数量
                        "seasons_count text",// 总季数
                        "current_season text",// 当前第几季(tv only)
                        "episodes text",
                        "schedule_url text",// 影讯页URL(movie only)
                        "trailer_urls text",// 预告片URL，对高级用户以上开放，最多开放4个地址
                        "photos text",// 电影剧照，前10张
                        "pop_rws text", // 影评，前10条
                        "local_path text",//本地路径
                        "uptime text",
                        "title_pinyin text",//标题拼音
                        "addtime text",//电影添加时间
                        "api_version integer default 0"//0 douban 1 imdb
                });
                sqLiteDatabase.setTransactionSuccessful();
                sqLiteDatabase.endTransaction();
                upgrade(sqLiteDatabase, 19, newVersion);
                break;
            case 17:
                sqLiteDatabase.beginTransaction();
                addAColumn(sqLiteDatabase, TABLE_MOVIE, "api_version integer default 0");
                sqLiteDatabase.setTransactionSuccessful();
                sqLiteDatabase.endTransaction();
                upgrade(sqLiteDatabase, 18, newVersion);
                break;
            case 16:
                sqLiteDatabase.beginTransaction();
                addAColumn(sqLiteDatabase, TABLE_VIDEOFILE, "imdb_movie_id integer default -1");
                sqLiteDatabase.setTransactionSuccessful();
                sqLiteDatabase.endTransaction();
                upgrade(sqLiteDatabase, 17, newVersion);
                break;
            case 15:
                sqLiteDatabase.beginTransaction();
                String createHistorySql = CreateSqlParamasString(TABLE_HISTORY, history_columns);
                sqLiteDatabase.execSQL(createHistorySql);
                sqLiteDatabase.setTransactionSuccessful();
                sqLiteDatabase.endTransaction();
                upgrade(sqLiteDatabase, 16, newVersion);
                break;
            case 14:
                sqLiteDatabase.beginTransaction();
                addAColumn(sqLiteDatabase, TABLE_VIDEOFILE, "thumbnail_s text");
                sqLiteDatabase.setTransactionSuccessful();
                sqLiteDatabase.endTransaction();
                upgrade(sqLiteDatabase, 15, newVersion);
                break;
            case 13:
                sqLiteDatabase.beginTransaction();
                addAColumn(sqLiteDatabase, TABLE_VIDEOFILE, "thumbnail text");
                sqLiteDatabase.setTransactionSuccessful();
                sqLiteDatabase.endTransaction();
                upgrade(sqLiteDatabase, 14, newVersion);

                break;
            case 12:
                sqLiteDatabase.beginTransaction();
                dropColmun(sqLiteDatabase, TABLE_MOVIE, movie_columns);
                sqLiteDatabase.setTransactionSuccessful();
                sqLiteDatabase.endTransaction();
                upgrade(sqLiteDatabase, 13, newVersion);
                break;
            case 11:
                sqLiteDatabase.beginTransaction();
                renameColmunName(sqLiteDatabase, TABLE_VIDEOFILE, new String[]{
                        "id integer primary key",
                        "movie_id integer default -1",
                        "devId text",
                        "devType text",
                        "uri text",
                        "filename text",
                });
                sqLiteDatabase.setTransactionSuccessful();
                sqLiteDatabase.endTransaction();
                upgrade(sqLiteDatabase, 12, newVersion);
                break;
            case 10:
                sqLiteDatabase.beginTransaction();
                String createPosterproviderSql = CreateSqlParamasString(TABLE_POSTERPROVIDER, posterprovider_columns);
                sqLiteDatabase.execSQL(createPosterproviderSql);
                sqLiteDatabase.setTransactionSuccessful();
                sqLiteDatabase.endTransaction();
                upgrade(sqLiteDatabase, 11, newVersion);
                break;
            case 9:
                sqLiteDatabase.beginTransaction();
                renameColmunName(sqLiteDatabase, TABLE_FAVORITE, favortie_columns);
                sqLiteDatabase.setTransactionSuccessful();
                sqLiteDatabase.endTransaction();
                upgrade(sqLiteDatabase, 10, newVersion);
                break;
            case 8:
                sqLiteDatabase.beginTransaction();
                String createFavoriteSql = CreateSqlParamasString(TABLE_FAVORITE, favortie_columns);
                sqLiteDatabase.execSQL(createFavoriteSql);
                sqLiteDatabase.setTransactionSuccessful();
                sqLiteDatabase.endTransaction();
                upgrade(sqLiteDatabase, 9, newVersion);
                break;
            default:
                dropTable(sqLiteDatabase, TABLE_MOVIE);
                dropTable(sqLiteDatabase, TABLE_TRAILER);
                dropTable(sqLiteDatabase, TABLE_GENRES);
                dropTable(sqLiteDatabase, TABLE_DEVICE);
                dropTable(sqLiteDatabase, TABLE_VIDEOFILE);
                dropTable(sqLiteDatabase, TABLE_FAVORITE);
                onCreate(sqLiteDatabase);
                break;
        }
    }

    private String getColumnString(String[] Columns) {
        StringBuffer sb = new StringBuffer();
        for (String column : Columns) {
            sb.append(column);
            sb.append(',');
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    private String getSelectColumnString(String[] Columns) {
        StringBuffer sb = new StringBuffer();
        for (String column : Columns) {
            String[] columnParams = column.split(" ");
            sb.append(columnParams[0]);
            sb.append(',');
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }


    public void addAColumn(SQLiteDatabase sqLiteDatabase, String tablename, String text) {
        Log.v(TAG, "db==========>:addColumns   " + tablename + "===>" + text);
        String sql = "ALTER TABLE " + tablename + " ADD " + text + "";
        sqLiteDatabase.execSQL(sql);
    }

    /**
     * 返回创建表的sql
     *
     * @param tableName
     * @param Columns
     * @return
     */
    public String CreateSqlParamasString(String tableName, String[] Columns) {

        String sql = "create table if not exists " + tableName + " ("
                + getColumnString(Columns) + ")";
        Log.v(TAG, "db==========>:CREATE TABLE SQL===>" + sql);
        return sql;
    }

    /**
     * 删除表
     *
     * @param sqLiteDatabase
     * @param tbName
     */
    private void dropTable(SQLiteDatabase sqLiteDatabase, String tbName) {
        Log.v(TAG, "db==========>:dropTable TABLE " + tbName);
        String sql = "DROP TABLE IF EXISTS " + tbName;
        sqLiteDatabase.execSQL(sql);

    }

    private void dropColmun(SQLiteDatabase sqLiteDatabase, String table, String[] Clomuns) {
        Log.v(TAG, "db==========>:dropColmun TABLE " + table + " new Clomuns=" + Clomuns.toString());
        renameTableNames(sqLiteDatabase, table, table + "old");//修改原表的名称
        String fields = getSelectColumnString(Clomuns);
        String sql = "CREATE TABLE " + table + " AS SELECT " + fields + " FROM " + table + "old" + " WHERE 1 = 1";
        sqLiteDatabase.execSQL(sql);
        dropTable(sqLiteDatabase, table + "old");
    }

    /**
     * 修改字段名
     *
     * @param sqLiteDatabase
     * @param table
     * @param newColumns
     */
    private void renameColmunName(SQLiteDatabase sqLiteDatabase, String table, String[] newColumns) {
        Log.v(TAG, "db==========>:renameColmunName TABLE " + table);
        renameTableNames(sqLiteDatabase, table, table + "old");//修改原表的名称

        String sql = CreateSqlParamasString(table, newColumns);
        sqLiteDatabase.execSQL(sql);//新建修改字段后的表

        insertRowsFromTables(sqLiteDatabase, table + "old", table, "*");//从旧表中查询出数据 并插入新表

        dropTable(sqLiteDatabase, table + "old");//删除旧表
    }

    private void insertRowsFromTables(SQLiteDatabase sqLiteDatabase, String sourceTable, String targetTable, String sourceColumnsStr) {
        Log.v(TAG, "db==========>:insertRowsFromTables TABLE " + targetTable + " column=" + sourceColumnsStr);
        String sql = "INSERT INTO " + targetTable + " SELECT " + sourceColumnsStr + " FROM " + sourceTable;
        sqLiteDatabase.execSQL(sql);
    }

    /**
     * 重命名表名
     *
     * @param sqLiteDatabase
     * @param table
     * @param newTableName
     */
    private void renameTableNames(SQLiteDatabase sqLiteDatabase, String table, String newTableName) {
        Log.v(TAG, "db==========>:renameTableNames TABLE " + table + " new TABLE=" + newTableName);
        String sql = "ALTER TABLE " + table + " RENAME TO " + newTableName;
        sqLiteDatabase.execSQL(sql);
    }

    public void closeAll() {
        this.close();
    }
}
