package com.hphtv.movielibrary.roomdb.dao;

import androidx.annotation.Nullable;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.hphtv.movielibrary.roomdb.TABLE;
import com.hphtv.movielibrary.roomdb.VIEW;
import com.hphtv.movielibrary.roomdb.entity.Movie;
import com.hphtv.movielibrary.roomdb.entity.VideoFile;
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


    @Query("SELECT * FROM " + TABLE.MOVIE + " WHERE movie_id=:movie_id AND source=:source")
    public Movie queryByMovieId(String movie_id,String source);


    @Query("UPDATE " + TABLE.MOVIE + " " +
            "SET is_favorite=:isFavorite " +
            "WHERE id=:id")
    public int updateFavorite(boolean isFavorite, long id);

    @Query("UPDATE " + TABLE.MOVIE + " " +
            "SET is_favorite=:isFavorite " +
            "WHERE movie_id=:movie_id")
    public int updateFavoriteByMovieId(boolean isFavorite, String movie_id);

    @Query("SELECT is_favorite FROM "+TABLE.MOVIE +" WHERE id=:id")
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
    @Query("SELECT * FROM " + TABLE.MOVIE + " WHERE id IN (SELECT MOVIE__VF.id FROM " + TABLE.MOVIE_VIDEOFILE_CROSS_REF + " AS MOVIE__VF JOIN " +
            "(SELECT VF.path FROM " + TABLE.VIDEOFILE + " AS VF JOIN " + TABLE.DEVICE + " AS DEV ON VF.device_id=DEV.id) AS VF__DEV " +
            "ON MOVIE__VF.path=VF__DEV.path AND MOVIE__VF.source=:source) and id=:id")
    public MovieWrapper queryMovieWrapperById(long id,String source);


    /**
     * 按movie_id查询电影wrapper
     *
     * @param movie_id
     * @return
     */
    @Transaction
    @Query("SELECT * FROM " + TABLE.MOVIE + " WHERE source=:source and movie_id=:movie_id")
    public MovieWrapper queryMovieWrapperByMovieId(String source, String movie_id);

    /**
     * @param device_id  设备id
     * @param year       年份
     * @param genre_name 类型
     * @param order      排序方式
     * @param isDesc     是否倒序
     * @return
     */
    @Query("SELECT * FROM " + VIEW.MOVIE_DATAVIEW
            + " WHERE source=:source" +
            " AND (:device_id IS NULL OR device_id=:device_id)" +
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
    public List<MovieDataView> queryMovieDataView(@Nullable String device_id, @Nullable String year, @Nullable String genre_name, int order, String source,@Nullable boolean isDesc);
    
    @Query("SELECT * FROM " + VIEW.MOVIE_DATAVIEW + " WHERE source=:source GROUP BY id ")
    public List<MovieDataView> queryAllMovieDataView(String source);


    @Query("SELECT * FROM "+VIEW.MOVIE_DATAVIEW+" WHERE is_favorite=1 AND source=:source GROUP BY id ORDER BY pinyin ASC")
    public List<MovieDataView> queryFavoriteMovieDataView(String source);
    /**
     * 查询年份
     *
     * @return
     */
    @Query("SELECT year FROM " + TABLE.MOVIE + " GROUP BY year ORDER BY year DESC")
    public List<String> qureyYearsGroup();
}
