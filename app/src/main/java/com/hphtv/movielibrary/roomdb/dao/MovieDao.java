package com.hphtv.movielibrary.roomdb.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.hphtv.movielibrary.roomdb.TABLE;
import com.hphtv.movielibrary.roomdb.entity.Movie;
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

    @Query("SELECT * FROM " + TABLE.MOVIE +" WHERE movie_id=:movie_id")
    public Movie queryByMovieId(String movie_id);

    @Query("SELECT * FROM " + TABLE.MOVIE)
    public List<MovieWrapper> queryAll();
}
