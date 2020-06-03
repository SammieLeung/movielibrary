package com.hphtv.movielibrary.sqlite;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.hphtv.movielibrary.sqlite.dao.DeviceDao;

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
            "dir_id integer",
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
            "id integer primary key",
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
        SQLiteDatabase db = getReadableDatabase();
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

        presetData(sqLiteDatabase);
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

    private void presetData(SQLiteDatabase sqLiteDatabase) {
        String insertDeviceSql = "INSERT OR IGNORE INTO " + TABLE_DEVICE + " VALUES (1,-1,'本地存储','/storage/emulated/0',0)";
        String insertDirSql = "INSERT OR IGNORE INTO " + TABLE_DIRECTORY + " VALUES (1,'Movies',1,6,'content://com.firefly.filepicker/local/L3N0b3JhZ2UvZW11bGF0ZWQvMC9Nb3ZpZXM%3D%0A/4/0','/storage/emulated/0/Movies',6,0,0)";
        sqLiteDatabase.execSQL(insertDeviceSql);
        sqLiteDatabase.execSQL(insertDirSql);
        String wrapper_1_sql = "INSERT OR IGNORE INTO " + TABLE_MOVIEWRAPPER + " VALUES ('1','[{\"api\":2,\"id\":1}]','[2]','[1]','[1]','灰猎犬号','h|l|q|h','http://img5.mtime.cn/mt/2020/03/06/053554.70956930_1280X720X2.jpg','0.0')";
        String wrapper_2_sql = "INSERT OR IGNORE INTO " + TABLE_MOVIEWRAPPER + " VALUES ('2','[{\"api\":2,\"id\":2}]','[3]','[1]','[1]','爱尔兰人','a|e|l|r','http://img5.mtime.cn/mt/2019/09/17/103119.85810012_1280X720X2.jpg','8.4')";
        String wrapper_3_sql = "INSERT OR IGNORE INTO " + TABLE_MOVIEWRAPPER + " VALUES ('3','[{\"api\":2,\"id\":3}]','[1]','[1]','[1]','花木兰','h|m|l','http://img5.mtime.cn/mt/2019/12/06/153112.21055685_1280X720X2.jpg','-1.0')";
        String wrapper_4_sql = "INSERT OR IGNORE INTO " + TABLE_MOVIEWRAPPER + " VALUES ('4','[{\"api\":2,\"id\":4}]','[4]','[1]','[1]','好莱坞往事','h|l|w|w|s','http://img5.mtime.cn/mt/2019/12/09/102808.50319661_1280X720X2.jpg','7.5')";
        String wrapper_5_sql = "INSERT OR IGNORE INTO " + TABLE_MOVIEWRAPPER + " VALUES ('5','[{\"api\":2,\"id\":5}]','[5]','[1]','[1]','乔乔的异想世界','q|q|d,d|y|x|s|j','http://img5.mtime.cn/mt/2020/01/14/100720.64939069_1280X720X2.jpg','8.0')";
        String wrapper_6_sql = "INSERT OR IGNORE INTO " + TABLE_MOVIEWRAPPER + " VALUES ('6','[{\"api\":2,\"id\":6}]','[6]','[1]','[1]','婚姻故事','h|y|g|s','http://img5.mtime.cn/mt/2020/01/14/094016.17222027_1280X720X2.jpg','8.1')";
        sqLiteDatabase.execSQL(wrapper_1_sql);
        sqLiteDatabase.execSQL(wrapper_2_sql);
        sqLiteDatabase.execSQL(wrapper_3_sql);
        sqLiteDatabase.execSQL(wrapper_4_sql);
        sqLiteDatabase.execSQL(wrapper_5_sql);
        sqLiteDatabase.execSQL(wrapper_6_sql);
        String movie_1_sql = "INSERT OR IGNORE INTO " + TABLE_MOVIE + " VALUES ('1','256059','1','灰猎犬号','Greyhound','https://m.mtime.cn/#!/movie/256059/','{\"large\":\"http://img5.mtime.cn/mt/2020/03/06/053554.70956930_1280X720X2.jpg\"}','{\"average\":0.0,\"max\":10,\"min\":0}','null','2019','','null','','0','0','0','[{\"name\":\"阿伦·施奈德\"}]','[{\"name\":\"汤姆·汉克斯\"},{\"name\":\"伊丽莎白·苏\"}]','null','','','null','[\"0分钟\"]','[\"3\",\"6\"]','null','影片根据C.S. Forester所著的非虚构小说《The Good Shepherd》改编，讲述1942年37艘盟军船只通过北大西洋，领头的灰猎犬号指挥官为欧内斯特·克劳斯（汤姆·汉克斯扮演），这是克劳斯首次指挥驱逐舰，就遇到一群纳粹潜艇的追击，克劳斯必须想办法逃出生天。在执行任务的同时他也要与自我怀疑和自身的恶魔作斗争，证明自己能够胜任该职位。','0','0','','','','','null','[{\"commentsCount\":0,\"icon\":\"http://img5.mtime.cn/pi/2020/03/06/053543.79108089_1000X1000.jpg\",\"imageUrl\":\"http://img5.mtime.cn/pi/2020/03/06/053543.79108089_1000X1000.jpg\",\"photosCount\":0,\"position\":0,\"recsCount\":0,\"resCount\":0,\"thumb\":\"http://img5.mtime.cn/pi/2020/03/06/053543.79108089_1000X1000.jpg\"},null,null,null,null,null,null,null,null,null]','null','','1584672453631','h|l|q|h','1584672453631','2')";
        String movie_2_sql = "INSERT OR IGNORE INTO " + TABLE_MOVIE + " VALUES ('2','138440','2','爱尔兰人','The Irishman','https://m.mtime.cn/#!/movie/138440/','{\"large\":\"http://img5.mtime.cn/mt/2019/09/17/103119.85810012_1280X720X2.jpg\"}','{\"average\":8.4,\"max\":10,\"min\":0}','null','2019','','null','','897','0','0','[{\"name\":\"马丁·斯科塞斯\"}]','[{\"name\":\"罗伯特·德尼罗\"},{\"name\":\"阿尔·帕西诺\"}]','null','','','null','[\"209分钟\"]','[\"1\",\"2\",\"3\",\"4\",\"5\"]','null','《爱尔兰人》为马丁·斯科塞斯执导的传奇巨制，罗伯特·德尼罗、阿尔·帕西诺和乔·佩西主演。通过二战老兵弗兰克·希兰的视角，讲述了战后美国有组织犯罪的故事。弗兰克·希兰是一名骗子和杀手，曾经在20世纪最恶名昭彰的人物身边工作。该电影跨越数十年，记录了美国历史上最大的悬案之一，即传奇工会领袖吉米·霍法失踪案，以宏大的故事之旅，展现有组织犯罪的隐秘通道：其内部运作、仇敌以及与主流政治的瓜葛。','0','0','','','','','null','[{\"commentsCount\":0,\"icon\":\"http://img5.mtime.cn/pi/2019/11/26/134621.84181981_1000X1000.jpg\",\"imageUrl\":\"http://img5.mtime.cn/pi/2019/11/26/134621.84181981_1000X1000.jpg\",\"photosCount\":0,\"position\":0,\"recsCount\":0,\"resCount\":0,\"thumb\":\"http://img5.mtime.cn/pi/2019/11/26/134621.84181981_1000X1000.jpg\"},{\"commentsCount\":0,\"icon\":\"http://img5.mtime.cn/pi/2019/11/28/091251.20104989_1000X1000.jpg\",\"imageUrl\":\"http://img5.mtime.cn/pi/2019/11/28/091251.20104989_1000X1000.jpg\",\"photosCount\":0,\"position\":0,\"recsCount\":0,\"resCount\":0,\"thumb\":\"http://img5.mtime.cn/pi/2019/11/28/091251.20104989_1000X1000.jpg\"},{\"commentsCount\":0,\"icon\":\"http://img5.mtime.cn/pi/2019/11/28/091251.19372627_1000X1000.jpg\",\"imageUrl\":\"http://img5.mtime.cn/pi/2019/11/28/091251.19372627_1000X1000.jpg\",\"photosCount\":0,\"position\":0,\"recsCount\":0,\"resCount\":0,\"thumb\":\"http://img5.mtime.cn/pi/2019/11/28/091251.19372627_1000X1000.jpg\"},{\"commentsCount\":0,\"icon\":\"http://img5.mtime.cn/pi/2019/11/28/091251.37973987_1000X1000.jpg\",\"imageUrl\":\"http://img5.mtime.cn/pi/2019/11/28/091251.37973987_1000X1000.jpg\",\"photosCount\":0,\"position\":0,\"recsCount\":0,\"resCount\":0,\"thumb\":\"http://img5.mtime.cn/pi/2019/11/28/091251.37973987_1000X1000.jpg\"},{\"commentsCount\":0,\"icon\":\"http://img5.mtime.cn/pi/2019/11/28/091251.59304535_1000X1000.jpg\",\"imageUrl\":\"http://img5.mtime.cn/pi/2019/11/28/091251.59304535_1000X1000.jpg\",\"photosCount\":0,\"position\":0,\"recsCount\":0,\"resCount\":0,\"thumb\":\"http://img5.mtime.cn/pi/2019/11/28/091251.59304535_1000X1000.jpg\"},{\"commentsCount\":0,\"icon\":\"http://img5.mtime.cn/pi/2019/11/28/091252.24376217_1000X1000.jpg\",\"imageUrl\":\"http://img5.mtime.cn/pi/2019/11/28/091252.24376217_1000X1000.jpg\",\"photosCount\":0,\"position\":0,\"recsCount\":0,\"resCount\":0,\"thumb\":\"http://img5.mtime.cn/pi/2019/11/28/091252.24376217_1000X1000.jpg\"},{\"commentsCount\":0,\"icon\":\"http://img5.mtime.cn/pi/2019/11/28/091252.97892971_1000X1000.jpg\",\"imageUrl\":\"http://img5.mtime.cn/pi/2019/11/28/091252.97892971_1000X1000.jpg\",\"photosCount\":0,\"position\":0,\"recsCount\":0,\"resCount\":0,\"thumb\":\"http://img5.mtime.cn/pi/2019/11/28/091252.97892971_1000X1000.jpg\"},{\"commentsCount\":0,\"icon\":\"http://img5.mtime.cn/pi/2019/11/28/091252.99560221_1000X1000.jpg\",\"imageUrl\":\"http://img5.mtime.cn/pi/2019/11/28/091252.99560221_1000X1000.jpg\",\"photosCount\":0,\"position\":0,\"recsCount\":0,\"resCount\":0,\"thumb\":\"http://img5.mtime.cn/pi/2019/11/28/091252.99560221_1000X1000.jpg\"},{\"commentsCount\":0,\"icon\":\"http://img5.mtime.cn/pi/2019/11/28/091252.95111322_1000X1000.jpg\",\"imageUrl\":\"http://img5.mtime.cn/pi/2019/11/28/091252.95111322_1000X1000.jpg\",\"photosCount\":0,\"position\":0,\"recsCount\":0,\"resCount\":0,\"thumb\":\"http://img5.mtime.cn/pi/2019/11/28/091252.95111322_1000X1000.jpg\"},{\"commentsCount\":0,\"icon\":\"http://img5.mtime.cn/pi/2019/11/28/091253.34408844_1000X1000.jpg\",\"imageUrl\":\"http://img5.mtime.cn/pi/2019/11/28/091253.34408844_1000X1000.jpg\",\"photosCount\":0,\"position\":0,\"recsCount\":0,\"resCount\":0,\"thumb\":\"http://img5.mtime.cn/pi/2019/11/28/091253.34408844_1000X1000.jpg\"}]','null','','1584672453725','a|e|l|r','1584672453725','2')";
        String movie_3_sql = "INSERT OR IGNORE INTO " + TABLE_MOVIE + " VALUES ('3','223155','3','花木兰','Mulan','https://m.mtime.cn/#!/movie/223155/','{\"large\":\"http://img5.mtime.cn/mt/2019/12/06/153112.21055685_1280X720X2.jpg\"}','{\"average\":-1.0,\"max\":10,\"min\":0}','null','2020','','null','','50','0','0','[{\"name\":\"妮基·卡罗\"}]','[{\"name\":\"刘亦菲\"},{\"name\":\"安柚鑫\"}]','null','','','null','[\"0分钟\"]','[\"7\",\"8\",\"3\",\"9\"]','null','影片将重新讲述经典迪士尼动画片《花木兰》（1998）的故事。1998年动画版一样，真人版《花木兰》根据中国历史上的女英雄花木兰改编。花木兰伪装成男人，替自己年迈的父亲从军上战场。在她可靠的守护龙木须的帮助下，她成为了一名技艺高超的战士，中国最伟大的女英雄之一。','0','0','','','','','null','[{\"commentsCount\":0,\"icon\":\"http://img5.mtime.cn/pi/2019/07/08/062630.83527791_1000X1000.jpg\",\"imageUrl\":\"http://img5.mtime.cn/pi/2019/07/08/062630.83527791_1000X1000.jpg\",\"photosCount\":0,\"position\":0,\"recsCount\":0,\"resCount\":0,\"thumb\":\"http://img5.mtime.cn/pi/2019/07/08/062630.83527791_1000X1000.jpg\"},{\"commentsCount\":0,\"icon\":\"http://img5.mtime.cn/pi/2019/07/08/062636.66366459_1000X1000.jpg\",\"imageUrl\":\"http://img5.mtime.cn/pi/2019/07/08/062636.66366459_1000X1000.jpg\",\"photosCount\":0,\"position\":0,\"recsCount\":0,\"resCount\":0,\"thumb\":\"http://img5.mtime.cn/pi/2019/07/08/062636.66366459_1000X1000.jpg\"},{\"commentsCount\":0,\"icon\":\"http://img5.mtime.cn/pi/2019/07/08/062640.90144619_1000X1000.jpg\",\"imageUrl\":\"http://img5.mtime.cn/pi/2019/07/08/062640.90144619_1000X1000.jpg\",\"photosCount\":0,\"position\":0,\"recsCount\":0,\"resCount\":0,\"thumb\":\"http://img5.mtime.cn/pi/2019/07/08/062640.90144619_1000X1000.jpg\"},{\"commentsCount\":0,\"icon\":\"http://img5.mtime.cn/pi/2019/07/08/062642.59760028_1000X1000.jpg\",\"imageUrl\":\"http://img5.mtime.cn/pi/2019/07/08/062642.59760028_1000X1000.jpg\",\"photosCount\":0,\"position\":0,\"recsCount\":0,\"resCount\":0,\"thumb\":\"http://img5.mtime.cn/pi/2019/07/08/062642.59760028_1000X1000.jpg\"},{\"commentsCount\":0,\"icon\":\"http://img5.mtime.cn/pi/2019/07/08/062643.46257733_1000X1000.jpg\",\"imageUrl\":\"http://img5.mtime.cn/pi/2019/07/08/062643.46257733_1000X1000.jpg\",\"photosCount\":0,\"position\":0,\"recsCount\":0,\"resCount\":0,\"thumb\":\"http://img5.mtime.cn/pi/2019/07/08/062643.46257733_1000X1000.jpg\"},{\"commentsCount\":0,\"icon\":\"http://img5.mtime.cn/pi/2019/07/08/062645.78413534_1000X1000.jpg\",\"imageUrl\":\"http://img5.mtime.cn/pi/2019/07/08/062645.78413534_1000X1000.jpg\",\"photosCount\":0,\"position\":0,\"recsCount\":0,\"resCount\":0,\"thumb\":\"http://img5.mtime.cn/pi/2019/07/08/062645.78413534_1000X1000.jpg\"},{\"commentsCount\":0,\"icon\":\"http://img5.mtime.cn/pi/2019/07/08/062648.14271849_1000X1000.jpg\",\"imageUrl\":\"http://img5.mtime.cn/pi/2019/07/08/062648.14271849_1000X1000.jpg\",\"photosCount\":0,\"position\":0,\"recsCount\":0,\"resCount\":0,\"thumb\":\"http://img5.mtime.cn/pi/2019/07/08/062648.14271849_1000X1000.jpg\"},{\"commentsCount\":0,\"icon\":\"http://img5.mtime.cn/pi/2019/07/08/062651.44713486_1000X1000.jpg\",\"imageUrl\":\"http://img5.mtime.cn/pi/2019/07/08/062651.44713486_1000X1000.jpg\",\"photosCount\":0,\"position\":0,\"recsCount\":0,\"resCount\":0,\"thumb\":\"http://img5.mtime.cn/pi/2019/07/08/062651.44713486_1000X1000.jpg\"},{\"commentsCount\":0,\"icon\":\"http://img5.mtime.cn/pi/2019/07/08/062654.63466342_1000X1000.jpg\",\"imageUrl\":\"http://img5.mtime.cn/pi/2019/07/08/062654.63466342_1000X1000.jpg\",\"photosCount\":0,\"position\":0,\"recsCount\":0,\"resCount\":0,\"thumb\":\"http://img5.mtime.cn/pi/2019/07/08/062654.63466342_1000X1000.jpg\"},{\"commentsCount\":0,\"icon\":\"http://img5.mtime.cn/pi/2019/07/08/062657.86748766_1000X1000.jpg\",\"imageUrl\":\"http://img5.mtime.cn/pi/2019/07/08/062657.86748766_1000X1000.jpg\",\"photosCount\":0,\"position\":0,\"recsCount\":0,\"resCount\":0,\"thumb\":\"http://img5.mtime.cn/pi/2019/07/08/062657.86748766_1000X1000.jpg\"}]','null','','1584672454504','h|m|l','1584672454504','2')";
        String movie_4_sql = "INSERT OR IGNORE INTO " + TABLE_MOVIE + " VALUES ('4','252117','4','好莱坞往事','Once Upon a Time ... in Hollywood','https://m.mtime.cn/#!/movie/252117/','{\"large\":\"http://img5.mtime.cn/mt/2019/12/09/102808.50319661_1280X720X2.jpg\"}','{\"average\":7.5,\"max\":10,\"min\":0}','null','2019','','null','','1069','0','0','[{\"name\":\"昆汀·塔伦蒂诺\"}]','[{\"name\":\"莱昂纳多·迪卡普里奥\"},{\"name\":\"布拉德·皮特\"}]','null','','','null','[\"161分钟\"]','[\"10\",\"3\"]','null','电影《好莱坞往事》是“鬼才导演”昆汀·塔伦蒂诺的第九部力作，剧情故事发生在1969年风云变幻的洛杉矶：电视剧明星(莱昂纳多·迪卡普里奥 饰)和他长期合作的替身演员(布拉德·皮特 饰)正力图扬名电影圈，却发现这个行业早已不是他们想象的样子了……','0','0','','','','','null','[{\"commentsCount\":0,\"icon\":\"http://img5.mtime.cn/pi/2019/01/26/101334.87710442_1000X1000.jpg\",\"imageUrl\":\"http://img5.mtime.cn/pi/2019/01/26/101334.87710442_1000X1000.jpg\",\"photosCount\":0,\"position\":0,\"recsCount\":0,\"resCount\":0,\"thumb\":\"http://img5.mtime.cn/pi/2019/01/26/101334.87710442_1000X1000.jpg\"},{\"commentsCount\":0,\"icon\":\"http://img5.mtime.cn/pi/2019/05/06/145959.28263459_1000X1000.jpg\",\"imageUrl\":\"http://img5.mtime.cn/pi/2019/05/06/145959.28263459_1000X1000.jpg\",\"photosCount\":0,\"position\":0,\"recsCount\":0,\"resCount\":0,\"thumb\":\"http://img5.mtime.cn/pi/2019/05/06/145959.28263459_1000X1000.jpg\"},{\"commentsCount\":0,\"icon\":\"http://img5.mtime.cn/pi/2019/07/03/173144.20379978_1000X1000.jpg\",\"imageUrl\":\"http://img5.mtime.cn/pi/2019/07/03/173144.20379978_1000X1000.jpg\",\"photosCount\":0,\"position\":0,\"recsCount\":0,\"resCount\":0,\"thumb\":\"http://img5.mtime.cn/pi/2019/07/03/173144.20379978_1000X1000.jpg\"},{\"commentsCount\":0,\"icon\":\"http://img5.mtime.cn/pi/2019/07/08/095515.96205320_1000X1000.jpg\",\"imageUrl\":\"http://img5.mtime.cn/pi/2019/07/08/095515.96205320_1000X1000.jpg\",\"photosCount\":0,\"position\":0,\"recsCount\":0,\"resCount\":0,\"thumb\":\"http://img5.mtime.cn/pi/2019/07/08/095515.96205320_1000X1000.jpg\"},{\"commentsCount\":0,\"icon\":\"http://img5.mtime.cn/pi/2019/12/09/180731.51139584_1000X1000.jpg\",\"imageUrl\":\"http://img5.mtime.cn/pi/2019/12/09/180731.51139584_1000X1000.jpg\",\"photosCount\":0,\"position\":0,\"recsCount\":0,\"resCount\":0,\"thumb\":\"http://img5.mtime.cn/pi/2019/12/09/180731.51139584_1000X1000.jpg\"},{\"commentsCount\":0,\"icon\":\"http://img5.mtime.cn/pi/2019/12/11/112813.62857444_1000X1000.jpg\",\"imageUrl\":\"http://img5.mtime.cn/pi/2019/12/11/112813.62857444_1000X1000.jpg\",\"photosCount\":0,\"position\":0,\"recsCount\":0,\"resCount\":0,\"thumb\":\"http://img5.mtime.cn/pi/2019/12/11/112813.62857444_1000X1000.jpg\"},{\"commentsCount\":0,\"icon\":\"http://img5.mtime.cn/pi/2019/01/26/101334.97545188_1000X1000.jpg\",\"imageUrl\":\"http://img5.mtime.cn/pi/2019/01/26/101334.97545188_1000X1000.jpg\",\"photosCount\":0,\"position\":0,\"recsCount\":0,\"resCount\":0,\"thumb\":\"http://img5.mtime.cn/pi/2019/01/26/101334.97545188_1000X1000.jpg\"},{\"commentsCount\":0,\"icon\":\"http://img5.mtime.cn/pi/2019/07/08/095516.31791531_1000X1000.jpg\",\"imageUrl\":\"http://img5.mtime.cn/pi/2019/07/08/095516.31791531_1000X1000.jpg\",\"photosCount\":0,\"position\":0,\"recsCount\":0,\"resCount\":0,\"thumb\":\"http://img5.mtime.cn/pi/2019/07/08/095516.31791531_1000X1000.jpg\"},{\"commentsCount\":0,\"icon\":\"http://img5.mtime.cn/pi/2019/05/06/150000.49332825_1000X1000.jpg\",\"imageUrl\":\"http://img5.mtime.cn/pi/2019/05/06/150000.49332825_1000X1000.jpg\",\"photosCount\":0,\"position\":0,\"recsCount\":0,\"resCount\":0,\"thumb\":\"http://img5.mtime.cn/pi/2019/05/06/150000.49332825_1000X1000.jpg\"},{\"commentsCount\":0,\"icon\":\"http://img5.mtime.cn/pi/2019/05/06/150000.89007954_1000X1000.jpg\",\"imageUrl\":\"http://img5.mtime.cn/pi/2019/05/06/150000.89007954_1000X1000.jpg\",\"photosCount\":0,\"position\":0,\"recsCount\":0,\"resCount\":0,\"thumb\":\"http://img5.mtime.cn/pi/2019/05/06/150000.89007954_1000X1000.jpg\"}]','null','','1584672454721','h|l|w|w|s','1584672454721','2')";
        String movie_5_sql = "INSERT OR IGNORE INTO " + TABLE_MOVIE + " VALUES ('5','255961','5','乔乔的异想世界','Jojo Rabbit','https://m.mtime.cn/#!/movie/255961/','{\"large\":\"http://img5.mtime.cn/mt/2020/01/14/100720.64939069_1280X720X2.jpg\"}','{\"average\":8.0,\"max\":10,\"min\":0}','null','2019','','null','','605','0','0','[{\"name\":\"塔伊加·维迪提\"}]','[{\"name\":\"罗曼·格里芬·戴维斯\"},{\"name\":\"斯嘉丽·约翰逊\"}]','null','','','null','[\"108分钟\"]','[\"10\",\"3\",\"6\"]','null','这是一部关于第二次世界大战的黑色幽默电影，孤独的德国男孩乔乔（罗曼·格里芬·戴维斯饰）发现自己的单亲妈妈（斯嘉丽·约翰逊饰）在阁楼里藏着一个犹太少女（托马辛·麦肯齐饰）后，他的世界发生了翻天覆地的变化。虽然有冒着傻气的假想朋友阿道夫·希特勒（塔伊加·维迪提饰）的帮助，但是乔乔逐渐发现自己对现实的认知开始瓦解。','0','0','','','','','null','[{\"commentsCount\":0,\"icon\":\"http://img5.mtime.cn/pi/2019/09/25/093330.21460238_1000X1000.jpg\",\"imageUrl\":\"http://img5.mtime.cn/pi/2019/09/25/093330.21460238_1000X1000.jpg\",\"photosCount\":0,\"position\":0,\"recsCount\":0,\"resCount\":0,\"thumb\":\"http://img5.mtime.cn/pi/2019/09/25/093330.21460238_1000X1000.jpg\"},{\"commentsCount\":0,\"icon\":\"http://img5.mtime.cn/pi/2019/05/14/095035.61994150_1000X1000.jpg\",\"imageUrl\":\"http://img5.mtime.cn/pi/2019/05/14/095035.61994150_1000X1000.jpg\",\"photosCount\":0,\"position\":0,\"recsCount\":0,\"resCount\":0,\"thumb\":\"http://img5.mtime.cn/pi/2019/05/14/095035.61994150_1000X1000.jpg\"},{\"commentsCount\":0,\"icon\":\"http://img5.mtime.cn/pi/2019/07/22/130356.25306972_1000X1000.jpg\",\"imageUrl\":\"http://img5.mtime.cn/pi/2019/07/22/130356.25306972_1000X1000.jpg\",\"photosCount\":0,\"position\":0,\"recsCount\":0,\"resCount\":0,\"thumb\":\"http://img5.mtime.cn/pi/2019/07/22/130356.25306972_1000X1000.jpg\"},{\"commentsCount\":0,\"icon\":\"http://img5.mtime.cn/pi/2020/02/13/155925.63462719_1000X1000.jpg\",\"imageUrl\":\"http://img5.mtime.cn/pi/2020/02/13/155925.63462719_1000X1000.jpg\",\"photosCount\":0,\"position\":0,\"recsCount\":0,\"resCount\":0,\"thumb\":\"http://img5.mtime.cn/pi/2020/02/13/155925.63462719_1000X1000.jpg\"},{\"commentsCount\":0,\"icon\":\"http://img5.mtime.cn/pi/2019/07/22/130358.63386519_1000X1000.jpg\",\"imageUrl\":\"http://img5.mtime.cn/pi/2019/07/22/130358.63386519_1000X1000.jpg\",\"photosCount\":0,\"position\":0,\"recsCount\":0,\"resCount\":0,\"thumb\":\"http://img5.mtime.cn/pi/2019/07/22/130358.63386519_1000X1000.jpg\"},{\"commentsCount\":0,\"icon\":\"http://img5.mtime.cn/pi/2019/08/14/101253.68267988_1000X1000.jpg\",\"imageUrl\":\"http://img5.mtime.cn/pi/2019/08/14/101253.68267988_1000X1000.jpg\",\"photosCount\":0,\"position\":0,\"recsCount\":0,\"resCount\":0,\"thumb\":\"http://img5.mtime.cn/pi/2019/08/14/101253.68267988_1000X1000.jpg\"},{\"commentsCount\":0,\"icon\":\"http://img5.mtime.cn/pi/2019/09/06/141332.84160382_1000X1000.jpg\",\"imageUrl\":\"http://img5.mtime.cn/pi/2019/09/06/141332.84160382_1000X1000.jpg\",\"photosCount\":0,\"position\":0,\"recsCount\":0,\"resCount\":0,\"thumb\":\"http://img5.mtime.cn/pi/2019/09/06/141332.84160382_1000X1000.jpg\"},{\"commentsCount\":0,\"icon\":\"http://img5.mtime.cn/pi/2019/09/06/141343.73705411_1000X1000.jpg\",\"imageUrl\":\"http://img5.mtime.cn/pi/2019/09/06/141343.73705411_1000X1000.jpg\",\"photosCount\":0,\"position\":0,\"recsCount\":0,\"resCount\":0,\"thumb\":\"http://img5.mtime.cn/pi/2019/09/06/141343.73705411_1000X1000.jpg\"},{\"commentsCount\":0,\"icon\":\"http://img5.mtime.cn/pi/2019/09/06/141358.26039584_1000X1000.jpg\",\"imageUrl\":\"http://img5.mtime.cn/pi/2019/09/06/141358.26039584_1000X1000.jpg\",\"photosCount\":0,\"position\":0,\"recsCount\":0,\"resCount\":0,\"thumb\":\"http://img5.mtime.cn/pi/2019/09/06/141358.26039584_1000X1000.jpg\"},{\"commentsCount\":0,\"icon\":\"http://img5.mtime.cn/pi/2020/01/14/104331.12970894_1000X1000.jpg\",\"imageUrl\":\"http://img5.mtime.cn/pi/2020/01/14/104331.12970894_1000X1000.jpg\",\"photosCount\":0,\"position\":0,\"recsCount\":0,\"resCount\":0,\"thumb\":\"http://img5.mtime.cn/pi/2020/01/14/104331.12970894_1000X1000.jpg\"}]','null','','1584672455238','q|q|d,d|y|x|s|j','1584672455238','2')";
        String movie_6_sql = "INSERT OR IGNORE INTO " + TABLE_MOVIE + " VALUES ('6','251962','6','婚姻故事','Marriage Story','https://m.mtime.cn/#!/movie/251962/','{\"large\":\"http://img5.mtime.cn/mt/2020/01/14/094016.17222027_1280X720X2.jpg\"}','{\"average\":8.1,\"max\":10,\"min\":0}','null','2019','','null','','529','0','0','[{\"name\":\"诺亚·鲍姆巴赫\"}]','[{\"name\":\"斯嘉丽·约翰逊\"},{\"name\":\"亚当·德赖弗\"}]','null','','','null','[\"136分钟\"]','[\"10\",\"3\"]','null','故事聚焦在妮可（斯嘉丽·约翰逊）和查理（亚当·德赖弗）这对夫妇身上，查理是一个独立的戏剧导演，而妮可是一个有着远大抱负的女演员，夫妻两人的艺术追求却导致彼此越走越远。问题到底出在哪里？妮可去洛杉矶参加拍摄一部电视剧，也正是这时候开始，他们两人商量要离婚。 两人有一个儿子，而身在纽约的查理对儿子的探视权和监护权就成了问题。种种问题导致这对夫妇走上了一条激烈争吵和付诸法律手段的道路。','0','0','','','','','null','[{\"commentsCount\":0,\"icon\":\"http://img5.mtime.cn/pi/2019/12/09/224947.99229297_1000X1000.jpg\",\"imageUrl\":\"http://img5.mtime.cn/pi/2019/12/09/224947.99229297_1000X1000.jpg\",\"photosCount\":0,\"position\":0,\"recsCount\":0,\"resCount\":0,\"thumb\":\"http://img5.mtime.cn/pi/2019/12/09/224947.99229297_1000X1000.jpg\"},{\"commentsCount\":0,\"icon\":\"http://img5.mtime.cn/pi/2019/12/09/224947.94551706_1000X1000.jpg\",\"imageUrl\":\"http://img5.mtime.cn/pi/2019/12/09/224947.94551706_1000X1000.jpg\",\"photosCount\":0,\"position\":0,\"recsCount\":0,\"resCount\":0,\"thumb\":\"http://img5.mtime.cn/pi/2019/12/09/224947.94551706_1000X1000.jpg\"},{\"commentsCount\":0,\"icon\":\"http://img5.mtime.cn/pi/2019/12/09/224947.21696060_1000X1000.jpg\",\"imageUrl\":\"http://img5.mtime.cn/pi/2019/12/09/224947.21696060_1000X1000.jpg\",\"photosCount\":0,\"position\":0,\"recsCount\":0,\"resCount\":0,\"thumb\":\"http://img5.mtime.cn/pi/2019/12/09/224947.21696060_1000X1000.jpg\"},{\"commentsCount\":0,\"icon\":\"http://img5.mtime.cn/pi/2019/12/09/224947.95752760_1000X1000.jpg\",\"imageUrl\":\"http://img5.mtime.cn/pi/2019/12/09/224947.95752760_1000X1000.jpg\",\"photosCount\":0,\"position\":0,\"recsCount\":0,\"resCount\":0,\"thumb\":\"http://img5.mtime.cn/pi/2019/12/09/224947.95752760_1000X1000.jpg\"},{\"commentsCount\":0,\"icon\":\"http://img5.mtime.cn/pi/2019/12/09/224947.74346405_1000X1000.jpg\",\"imageUrl\":\"http://img5.mtime.cn/pi/2019/12/09/224947.74346405_1000X1000.jpg\",\"photosCount\":0,\"position\":0,\"recsCount\":0,\"resCount\":0,\"thumb\":\"http://img5.mtime.cn/pi/2019/12/09/224947.74346405_1000X1000.jpg\"},{\"commentsCount\":0,\"icon\":\"http://img5.mtime.cn/pi/2019/12/09/224947.87905210_1000X1000.jpg\",\"imageUrl\":\"http://img5.mtime.cn/pi/2019/12/09/224947.87905210_1000X1000.jpg\",\"photosCount\":0,\"position\":0,\"recsCount\":0,\"resCount\":0,\"thumb\":\"http://img5.mtime.cn/pi/2019/12/09/224947.87905210_1000X1000.jpg\"},{\"commentsCount\":0,\"icon\":\"http://img5.mtime.cn/pi/2019/12/09/224948.13352612_1000X1000.jpg\",\"imageUrl\":\"http://img5.mtime.cn/pi/2019/12/09/224948.13352612_1000X1000.jpg\",\"photosCount\":0,\"position\":0,\"recsCount\":0,\"resCount\":0,\"thumb\":\"http://img5.mtime.cn/pi/2019/12/09/224948.13352612_1000X1000.jpg\"},{\"commentsCount\":0,\"icon\":\"http://img5.mtime.cn/pi/2019/12/10/233842.65786960_1000X1000.jpg\",\"imageUrl\":\"http://img5.mtime.cn/pi/2019/12/10/233842.65786960_1000X1000.jpg\",\"photosCount\":0,\"position\":0,\"recsCount\":0,\"resCount\":0,\"thumb\":\"http://img5.mtime.cn/pi/2019/12/10/233842.65786960_1000X1000.jpg\"},{\"commentsCount\":0,\"icon\":\"http://img5.mtime.cn/pi/2019/08/21/080223.96279084_1000X1000.jpg\",\"imageUrl\":\"http://img5.mtime.cn/pi/2019/08/21/080223.96279084_1000X1000.jpg\",\"photosCount\":0,\"position\":0,\"recsCount\":0,\"resCount\":0,\"thumb\":\"http://img5.mtime.cn/pi/2019/08/21/080223.96279084_1000X1000.jpg\"},{\"commentsCount\":0,\"icon\":\"http://img5.mtime.cn/pi/2019/10/18/101936.22366460_1000X1000.jpg\",\"imageUrl\":\"http://img5.mtime.cn/pi/2019/10/18/101936.22366460_1000X1000.jpg\",\"photosCount\":0,\"position\":0,\"recsCount\":0,\"resCount\":0,\"thumb\":\"http://img5.mtime.cn/pi/2019/10/18/101936.22366460_1000X1000.jpg\"}]','null','','1584672456023','h|y|g|s','1584672456023','2')";
        sqLiteDatabase.execSQL(movie_1_sql);
        sqLiteDatabase.execSQL(movie_2_sql);
        sqLiteDatabase.execSQL(movie_3_sql);
        sqLiteDatabase.execSQL(movie_4_sql);
        sqLiteDatabase.execSQL(movie_5_sql);
        sqLiteDatabase.execSQL(movie_6_sql);
        String geners_1_sql = "INSERT OR IGNORE INTO " + TABLE_GENRES + " VALUES (1,'传记')";
        String geners_2_sql = "INSERT OR IGNORE INTO " + TABLE_GENRES + " VALUES (2,'犯罪')";
        String geners_3_sql = "INSERT OR IGNORE INTO " + TABLE_GENRES + " VALUES (3,'剧情')";
        String geners_4_sql = "INSERT OR IGNORE INTO " + TABLE_GENRES + " VALUES (4,'历史')";
        String geners_5_sql = "INSERT OR IGNORE INTO " + TABLE_GENRES + " VALUES (5,'惊悚')";
        String geners_6_sql = "INSERT OR IGNORE INTO " + TABLE_GENRES + " VALUES (6,'战争')";
        String geners_7_sql = "INSERT OR IGNORE INTO " + TABLE_GENRES + " VALUES (7,'动作')";
        String geners_8_sql = "INSERT OR IGNORE INTO " + TABLE_GENRES + " VALUES (8,'冒险')";
        String geners_9_sql = "INSERT OR IGNORE INTO " + TABLE_GENRES + " VALUES (9,'家庭')";
        String geners_10_sql = "INSERT OR IGNORE INTO " + TABLE_GENRES + " VALUES (10,'喜剧')";
        sqLiteDatabase.execSQL(geners_1_sql);
        sqLiteDatabase.execSQL(geners_2_sql);
        sqLiteDatabase.execSQL(geners_3_sql);
        sqLiteDatabase.execSQL(geners_4_sql);
        sqLiteDatabase.execSQL(geners_5_sql);
        sqLiteDatabase.execSQL(geners_6_sql);
        sqLiteDatabase.execSQL(geners_7_sql);
        sqLiteDatabase.execSQL(geners_8_sql);
        sqLiteDatabase.execSQL(geners_9_sql);
        sqLiteDatabase.execSQL(geners_10_sql);
        String file_1_sql = "INSERT OR IGNORE INTO " + TABLE_VIDEOFILE + " VALUES ('1','3','/storage/emulated/0/Movies/Mulan.mp4','Mulan.mp4','Mulan','','','1','1','0','m')";
        String file_2_sql = "INSERT OR IGNORE INTO " + TABLE_VIDEOFILE + " VALUES ('2','1','/storage/emulated/0/Movies/Greyhound.mp4','Greyhound.mp4','Greyhound','','','1','1','0','g')";
        String file_3_sql = "INSERT OR IGNORE INTO " + TABLE_VIDEOFILE + " VALUES ('3','2','/storage/emulated/0/Movies/TheIrishman.mp4','TheIrishman.mp4','TheIrishman','','','1','1','0','t')";
        String file_4_sql = "INSERT OR IGNORE INTO " + TABLE_VIDEOFILE + " VALUES ('4','4','/storage/emulated/0/Movies/OnceUponATimeInHollywood.mp4','OnceUponATimeInHollywood.mp4','OnceUponATimeInHollywood','','','1','1','0','o')";
        String file_5_sql = "INSERT OR IGNORE INTO " + TABLE_VIDEOFILE + " VALUES ('5','5','/storage/emulated/0/Movies/JojoRabbit.mp4','JojoRabbit.mp4','JojoRabbit','','','1','1','0','j')";
        String file_6_sql = "INSERT OR IGNORE INTO " + TABLE_VIDEOFILE + " VALUES ('6','6','/storage/emulated/0/Movies/MarriageStory.mp4','MarriageStory.mp4','MarriageStory','','','1','1','0','m')";
        sqLiteDatabase.execSQL(file_1_sql);
        sqLiteDatabase.execSQL(file_2_sql);
        sqLiteDatabase.execSQL(file_3_sql);
        sqLiteDatabase.execSQL(file_4_sql);
        sqLiteDatabase.execSQL(file_5_sql);
        sqLiteDatabase.execSQL(file_6_sql);
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
