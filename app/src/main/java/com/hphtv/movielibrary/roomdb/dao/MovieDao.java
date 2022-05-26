package com.hphtv.movielibrary.roomdb.dao;


import androidx.annotation.Nullable;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.roomdb.TABLE;
import com.hphtv.movielibrary.roomdb.VIEW;
import com.hphtv.movielibrary.roomdb.entity.Movie;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.hphtv.movielibrary.roomdb.entity.relation.MovieWrapper;

import java.util.List;

/**
 * author: Sam Leung
 * date:  2021/5/25
 */
@Dao
public interface MovieDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public long insertOrIgnoreMovie(Movie movie);

    @Update
    public int update(Movie movie);

    @Query("SELECT * FROM " + TABLE.MOVIE + " WHERE movie_id=:movie_id AND source=:source")
    public Movie queryByMovieId(String movie_id, String source);

    @Query("SELECT * FROM " + TABLE.MOVIE + " WHERE movie_id=:movie_id")
    public List<Movie> queryByMovieId(String movie_id);

    @Query("SELECT * FROM " + TABLE.MOVIE + " WHERE id=(SELECT id FROM " + TABLE.MOVIE_VIDEOFILE_CROSS_REF + " WHERE path=:path AND source=:source)")
    public Movie queryByFilePath(String path, String source);

    @Query("UPDATE " + TABLE.MOVIE + " " +
            "SET is_favorite=:isFavorite " +
            "WHERE movie_id=:movie_id")
    public int updateFavoriteStateByMovieId(boolean isFavorite, String movie_id);

    @Query("UPDATE " + TABLE.MOVIE +
            " SET last_playtime=:last_playtime" +
            " WHERE movie_id=:movie_id")
    public int updateLastPlaytime(String movie_id, long last_playtime);

    @Query("UPDATE " + TABLE.MOVIE +
            " SET ap=:ap " +
            " WHERE movie_id=:movie_id")
    public int updateAccessPermission(String movie_id, Constants.WatchLimit ap);

    @Query("SELECT is_favorite FROM " + TABLE.MOVIE + " WHERE id=:id")
    public boolean queryFavorite(long id);

//    /**
//     * 只查询当前以挂载的设备
//     *
//     * @return
//     */
//    @Transaction
//    @Query("SELECT * FROM " + TABLE.MOVIE + " WHERE id IN (SELECT MOVIE__VF.id FROM " + TABLE.MOVIE_VIDEOFILE_CROSS_REF + " AS MOVIE__VF JOIN " +
//            "(SELECT VF.path from " + TABLE.VIDEOFILE + " AS VF JOIN " + TABLE.DEVICE + " AS DEV ON VF.device_id=DEV.id) AS VF__DEV " +
//            "ON MOVIE__VF.path=VF__DEV.path)")
//    public List<MovieWrapper> queryAll();

    /**
     * 按电影id查询电影wrapper
     *
     * @param id
     * @return
     */
    @Transaction
//    @Query("SELECT * FROM " + TABLE.MOVIE + " WHERE id IN (SELECT MOVIE__VF.id FROM " + TABLE.MOVIE_VIDEOFILE_CROSS_REF + " AS MOVIE__VF JOIN " +
//            "(SELECT VF.path FROM " + TABLE.VIDEOFILE + " AS VF JOIN " + TABLE.DEVICE + " AS DEV ON VF.device_path=DEV.path) AS VF__DEV " +
//            "ON MOVIE__VF.path=VF__DEV.path AND MOVIE__VF.source=:source) and id=:id")
    @Query("SELECT * FROM " + TABLE.MOVIE + " WHERE id IN " +
            "(SELECT MVCF.id FROM " + TABLE.MOVIE_VIDEOFILE_CROSS_REF + " AS MVCF  " +
            "JOIN " + TABLE.VIDEOFILE + " AS VF  " +
            "ON MVCF.path=VF.path AND MVCF.source =:source " +
            "JOIN " + TABLE.SHORTCUT + " AS ST " +
            "ON VF.dir_path=ST.uri  " +
            "JOIN " + TABLE.DEVICE + " AS DEV  " +
            "ON DEV.path=ST.device_path OR ST.device_type > 5) " +
            "AND id=:id")
    public MovieWrapper queryMovieWrapperById(long id, String source);


    /**
     * 按movie_id查询电影wrapper
     *
     * @param movie_id
     * @return
     */
    @Transaction
    @Query("SELECT * FROM " + TABLE.MOVIE + " WHERE source=:source and movie_id=:movie_id")
    public MovieWrapper queryMovieWrapperByMovieId(String movie_id, String source);


    @Transaction
    @Query("SELECT * FROM " + TABLE.MOVIE + " WHERE id=(SELECT id FROM " + TABLE.MOVIE_VIDEOFILE_CROSS_REF + " WHERE path=:path AND source=:source)")
    public MovieWrapper queryMovieWrapperByFilePath(String path, String source);

    /**
     * 按照来源分页查找
     *
     * @param source
     * @param offset
     * @param limit
     * @return
     */
    @Query("SELECT * FROM " + VIEW.MOVIE_DATAVIEW
            + " WHERE source=:source" +
            " GROUP BY id " +
            " ORDER BY " +
            " pinyin ASC " +
            " LIMIT :limit " +
            " OFFSET :offset "
    )
    public List<MovieDataView> queryMovieDataView(String source, int offset, int limit);

    /**
     * 按照电影id和来源
     *
     * @param movie_id
     * @param source
     * @return
     */
    @Query("SELECT * FROM " + VIEW.MOVIE_DATAVIEW
            + " WHERE source=:source AND movie_id=:movie_id " +
            " AND type=:type "+
            "GROUP BY id")
    public MovieDataView queryMovieDataViewByMovieId(String movie_id,String type, String source);

    /**
     * @param device_uri 设备id
     * @param year       年份
     * @param genre_name 类型
     * @param order      排序方式
     * @param isDesc     是否倒序
     * @return
     */
    @Query("SELECT * FROM " + VIEW.MOVIE_DATAVIEW
            + " WHERE source=:source" +
            " AND (:device_uri IS NULL OR device_uri=:device_uri)" +
            " AND (:year IS NULL OR year=:year)" +
            " AND (:genre_name IS NULL OR genre_name=:genre_name)" +
            " GROUP BY id " +
            " ORDER BY " +
            "CASE WHEN :order =0 AND :isDesc=0 THEN pinyin END ASC," +
            "CASE WHEN :order =1 AND :isDesc=0 THEN ratings END ASC," +
            "CASE WHEN :order =2 AND :isDesc=0 THEN year END ASC," +
            "CASE WHEN :order =3 AND :isDesc=0 THEN add_time END ASC," +
            "CASE WHEN :order =0 AND :isDesc=1 THEN pinyin END DESC," +
            "CASE WHEN :order =1 AND :isDesc=1 THEN ratings END DESC," +
            "CASE WHEN :order =2 AND :isDesc=1 THEN year END DESC," +
            "CASE WHEN :order =3 AND :isDesc=1 THEN add_time END DESC," +
            "CASE WHEN :order =4 THEN last_playtime END ASC," +
            "CASE WHEN :order =5 THEN is_favorite END ASC"
    )
    public List<MovieDataView> queryMovieDataView(@Nullable String device_uri, @Nullable String year, @Nullable String genre_name, int order, String source, @Nullable boolean isDesc);

    /**
     * 根据条件返回符合条件电影数量。
     *
     * @param dir_uri
     * @param vtid
     * @param genre_name
     * @param year
     * @param source
     * @return
     */
    @Query("SELECT COUNT(*) FROM (" +
            "SELECT * FROM " + VIEW.MOVIE_DATAVIEW
            + " WHERE source=:source" +
            " AND (:ap IS NULL OR (ap=:ap OR (ap IS NULL AND s_ap=:ap)))" +
            " AND (:dir_uri IS NULL OR dir_uri=:dir_uri)" +
            " AND (:year IS NULL OR year=:year)" +
            " AND (:genre_name IS NULL OR genre_name=:genre_name) " +
            " AND (:vtid IS -1 OR id IN (SELECT id FROM " + TABLE.MOVIE_VIDEOTAG_CROSS_REF + " WHERE vtid=:vtid))" +
            " GROUP BY id " +
            ")")
    public int countMovieDataView(@Nullable String dir_uri, @Nullable long vtid, @Nullable String genre_name, @Nullable String year, String ap, String source);

    /**
     * 根据条件返回符合条件电影
     *
     * @param dir_uri    设备id
     * @param year       年份
     * @param genre_name 类型
     * @param order      排序方式
     * @param isDesc     是否倒序
     * @return
     */
    @Query("SELECT * FROM " + VIEW.MOVIE_DATAVIEW
            + " WHERE source=:source" +
            " AND (:ap IS NULL OR (ap=:ap OR (ap IS NULL AND s_ap=:ap)))" +
            " AND (:dir_uri IS NULL OR dir_uri=:dir_uri)" +
            " AND (:year IS NULL OR year=:year)" +
            " AND (:genre_name IS NULL OR genre_name=:genre_name) " +
            " AND (:vtid IS -1 OR id IN (SELECT id FROM " + TABLE.MOVIE_VIDEOTAG_CROSS_REF + " WHERE vtid=:vtid))" +
            " GROUP BY id,season " +
            " ORDER BY " +
            "CASE WHEN :order =0 AND :isDesc=0 THEN pinyin END ASC," +
            "CASE WHEN :order =1 AND :isDesc=0 THEN ratings END ASC," +
            "CASE WHEN :order =2 AND :isDesc=0 THEN year END ASC," +
            "CASE WHEN :order =3 AND :isDesc=0 THEN add_time END ASC," +
            "CASE WHEN :order =4 AND :isDesc=0 THEN last_playtime END ASC," +
            "CASE WHEN :order =0 AND :isDesc=1 THEN pinyin END DESC," +
            "CASE WHEN :order =1 AND :isDesc=1 THEN ratings END DESC," +
            "CASE WHEN :order =2 AND :isDesc=1 THEN year END DESC," +
            "CASE WHEN :order =3 AND :isDesc=1 THEN add_time END DESC," +
            "CASE WHEN :order =4 AND :isDesc=1 THEN last_playtime END DESC," +
            "CASE WHEN :order =5 THEN is_favorite END ASC " +
            "LIMIT :offset,:limit "
    )
    public List<MovieDataView> queryMovieDataView(@Nullable String dir_uri, @Nullable long vtid, @Nullable String genre_name, @Nullable String year, int order, @Nullable String ap, @Nullable boolean isDesc, String source, int offset, int limit);

    @Query("SELECT * FROM " + VIEW.MOVIE_DATAVIEW
            + " WHERE source=:source " +
            " AND JULIANDAY('now') - JULIANDAY(DATE(add_time/1000,'UNIXEPOCH')) < 7 " +
            " GROUP BY id " +
            " ORDER BY add_time DESC "
    )
    public List<MovieDataView> queryMovieDataViewForRecentlyAdded(String source);

    @Query("SELECT COUNT(*) FROM (SELECT * FROM " + VIEW.MOVIE_DATAVIEW
            + " WHERE source=:source " +
            " AND JULIANDAY('now') - JULIANDAY(DATE(add_time/1000,'UNIXEPOCH')) < 7 " +
            " AND (:ap IS NULL OR (ap=:ap OR (ap IS NULL AND s_ap=:ap)))" +
            " GROUP BY id " +
            " ORDER BY add_time DESC)"
    )
    public int countMovieDataViewForRecentlyAdded(String source, String ap);

    @Query("SELECT * FROM " + VIEW.MOVIE_DATAVIEW
            + " WHERE source=:source " +
            " AND JULIANDAY('now') - JULIANDAY(DATE(add_time/1000,'UNIXEPOCH')) < 7 " +
            " AND (:ap IS NULL OR (ap=:ap OR (ap IS NULL AND s_ap=:ap)))" +
            " GROUP BY id " +
            " ORDER BY add_time DESC" +
            " LIMIT :offset,:limit"
    )
    public List<MovieDataView> queryMovieDataViewForRecentlyAdded(String source, String ap, int offset, int limit);


    @Query("SELECT * FROM " + VIEW.MOVIE_DATAVIEW
            + " WHERE source=:source " +
            " AND genre_name in (:genre_name) " +
            " AND id!=:id " +
            " AND (:ap IS NULL OR (ap=:ap OR (ap IS NULL AND s_ap=:ap)))" +
            " GROUP BY id " +
            " ORDER BY add_time DESC " +
            " LIMIT :offset,:limit"
    )
    public List<MovieDataView> queryRecommand(String source, String ap, List<String> genre_name, long id, int offset, int limit);


    @Query("SELECT * FROM " + VIEW.MOVIE_DATAVIEW
            + " WHERE source=:source " +
            " AND genre_name in (:genre_name) " +
            " AND id NOT IN (:ids) " +
            " AND (:ap IS NULL OR (ap=:ap OR (ap IS NULL AND s_ap=:ap)))" +
            " GROUP BY id " +
            " ORDER BY add_time DESC " +
            " LIMIT :offset,:limit"
    )
    public List<MovieDataView> queryRecommand(String source, String ap, List<String> genre_name, List<Long> ids, int offset, int limit);

    @Query("SELECT * FROM " + VIEW.MOVIE_DATAVIEW + " WHERE source=:source" +
            " AND (:ap IS NULL OR (ap=:ap OR (ap IS NULL AND s_ap=:ap)))" +
            " GROUP BY id ")
    public List<MovieDataView> queryAllMovieDataView(String source, String ap);

    @Query("SELECT * FROM " + VIEW.MOVIE_DATAVIEW + " WHERE source=:source " +
            " AND (:ap IS NULL OR (ap=:ap OR (ap IS NULL AND s_ap=:ap)))" +
            "GROUP BY id LIMIT :offset,:limit")
    public List<MovieDataView> queryAllMovieDataView(String source, String ap, int offset, int limit);

    @Query("SELECT COUNT(*) FROM (SELECT id FROM " + VIEW.MOVIE_DATAVIEW + " WHERE source=:source GROUP BY id)")
    public int countAllMovieDataView(String source);

    @Query("SELECT * FROM " + VIEW.MOVIE_DATAVIEW + " WHERE is_favorite=1 AND source=:source GROUP BY id ORDER BY pinyin ASC")
    public List<MovieDataView> queryFavoriteMovieDataView(String source);

    @Query("SELECT COUNT(*) FROM (SELECT * FROM " + VIEW.MOVIE_DATAVIEW + " WHERE is_favorite=1 AND source=:source" +
            " AND (:ap IS NULL OR (ap=:ap OR (ap IS NULL AND s_ap=:ap)))" +
            " GROUP BY id ORDER BY pinyin ASC)")
    public int countFavoriteMovieDataView(String source,String ap);

    @Query("SELECT * FROM " + VIEW.MOVIE_DATAVIEW + " WHERE is_favorite=1 AND source=:source" +
            " AND (:ap IS NULL OR (ap=:ap OR (ap IS NULL AND s_ap=:ap)))" +
            " GROUP BY id ORDER BY pinyin ASC" +
            " LIMIT :offset,:limit")
    public List<MovieDataView> queryFavoriteMovieDataView(String source,String ap, int offset, int limit);

    @Query("SELECT COUNT(*) FROM (SELECT * FROM " + TABLE.MOVIE_VIDEOFILE_CROSS_REF + " WHERE source=:source GROUP BY id)")
    public int queryTotalMovieCount(String source);

    /**
     * 查询年份
     *
     * @return
     */
    @Query("SELECT year FROM " + VIEW.MOVIE_DATAVIEW + " GROUP BY year ORDER BY year DESC")
    public List<String> queryYearsGroup();
}

