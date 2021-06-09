package com.hphtv.movielibrary.roomdb.dao;

import androidx.room.Dao;
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
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public long insertOrReplaceMovie(Movie movie);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public long insertOrIgnoreMovie(Movie movie);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public long[] insertMovies(Movie... movies);

    @Update
    public void updateMovie(Movie movie);

    @Query("SELECT * FROM " + TABLE.MOVIE + " WHERE movie_id=:movie_id")
    public Movie queryByMovieId(String movie_id);

    /**
     * 只查询当前以挂载的设备
     * @return
     */
    @Transaction
    @Query("SELECT * FROM " + TABLE.MOVIE + " WHERE id IN (SELECT MOVIE__VF.id FROM " + TABLE.MOVIE_VIDEOFILE_CROSS_REF + " AS MOVIE__VF JOIN " +
            "(SELECT VF.path from " + TABLE.VIDEOFILE + " AS VF JOIN " + TABLE.DEVICE + " AS DEV ON VF.device_id=DEV.id) AS VF__DEV " +
            "ON MOVIE__VF.path=VF__DEV.path) ORDER BY :orderBy")
    public List<MovieWrapper> queryAll(String orderBy);

    /**
     *
     * @param condition
     * @return
     */
    @Query("SELECT * FROM "+ VIEW.MOVIE_DATAVIEW +" WHERE :condition")
    public List<MovieDataView> queryMoiveDataView(String condition);

    @Query("SELECT year FROM " + TABLE.MOVIE + " GROUP BY year ORDER BY year DESC")
    public List<String> qureyYearsGroup();
}
