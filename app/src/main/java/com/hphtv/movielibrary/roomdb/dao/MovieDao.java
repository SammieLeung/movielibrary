package com.hphtv.movielibrary.roomdb.dao;

import androidx.annotation.Nullable;
import androidx.room.Dao;
import androidx.room.DatabaseView;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.hphtv.movielibrary.roomdb.TABLE;
import com.hphtv.movielibrary.roomdb.VIEW;
import com.hphtv.movielibrary.roomdb.entity.Movie;
import com.hphtv.movielibrary.roomdb.entity.MovieDataView;
import com.hphtv.movielibrary.roomdb.entity.MovieWrapper;

import java.util.List;

/**
 * author: Sam Leung
 * date:  2021/5/25
 */
@Dao
public interface MovieDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public long insertOrIgnoreMovie(Movie movie);


    @Query("SELECT * FROM " + TABLE.MOVIE + " WHERE movie_id=:movie_id")
    public Movie queryByMovieId(String movie_id);

    /**
     * 只查询当前以挂载的设备
     * @return
     */
    @Transaction
    @Query("SELECT * FROM " + TABLE.MOVIE + " WHERE id IN (SELECT MOVIE__VF.id FROM " + TABLE.MOVIE_VIDEOFILE_CROSS_REF + " AS MOVIE__VF JOIN " +
            "(SELECT VF.path from " + TABLE.VIDEOFILE + " AS VF JOIN " + TABLE.DEVICE + " AS DEV ON VF.device_id=DEV.id) AS VF__DEV " +
            "ON MOVIE__VF.path=VF__DEV.path)")
    public List<MovieWrapper> queryAll();

    @Transaction
    @Query("SELECT * FROM " + TABLE.MOVIE + " WHERE id IN (SELECT MOVIE__VF.id FROM " + TABLE.MOVIE_VIDEOFILE_CROSS_REF + " AS MOVIE__VF JOIN " +
            "(SELECT VF.path from " + TABLE.VIDEOFILE + " AS VF JOIN " + TABLE.DEVICE + " AS DEV ON VF.device_id=DEV.id) AS VF__DEV " +
            "ON MOVIE__VF.path=VF__DEV.path) and id=:id")
    public MovieWrapper queryMovieWrapperById(long id);

    /**
     *
     * @param device_id 设备id
     * @param year 年份
     * @param genre_name 类型
     * @param order 排序方式
     * @param isDesc 是否倒序
     * @return
     */
    @Query("SELECT * FROM "+ VIEW.MOVIE_DATAVIEW
            +" WHERE (:device_id IS NULL OR device_id=:device_id)" +
            " AND (:year IS NULL OR year=:year)" +
            " AND (:genre_name IS NULL OR genre_name=:genre_name)" +
            " GROUP BY id " +
            " ORDER BY " +
            "CASE WHEN :order =0 AND :isDesc=0 THEN pinyin END ASC," +
            "CASE WHEN :order =1 AND :isDesc=0 THEN ratings END ASC," +
            "CASE WHEN :order =2 AND :isDesc=0 THEN genre_name END ASC," +
            "CASE WHEN :order =3 AND :isDesc=0 THEN year END ASC," +
            "CASE WHEN :order =4 AND :isDesc=0 THEN add_time END ASC," +
            "CASE WHEN :order =0 AND :isDesc=1 THEN pinyin END DESC," +
            "CASE WHEN :order =1 AND :isDesc=1 THEN ratings END DESC," +
            "CASE WHEN :order =2 AND :isDesc=1 THEN genre_name END DESC," +
            "CASE WHEN :order =3 AND :isDesc=1 THEN year END DESC," +
            "CASE WHEN :order =4 AND :isDesc=1 THEN add_time END DESC"
           )
    public List<MovieDataView> queryMoiveDataView(@Nullable String device_id,@Nullable String year,@Nullable String genre_name,  int order,@Nullable boolean isDesc);

    @Query("SELECT year FROM " + TABLE.MOVIE + " GROUP BY year ORDER BY year DESC")
    public List<String> qureyYearsGroup();
}
