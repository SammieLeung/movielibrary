package com.hphtv.movielibrary.roomdb.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.hphtv.movielibrary.roomdb.TABLE;
import com.hphtv.movielibrary.roomdb.entity.reference.MovieUserFavoriteCrossRef;

/**
 * author: Sam Leung
 * date:  2022/12/9
 */
@Dao
public interface MovieUserFavoriteCrossRefDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public long insertOrIgnore(MovieUserFavoriteCrossRef movieUserFavoriteCrossRef);

    @Query("SELECT * FROM "+ TABLE.MOVIE_USER_FAVORITE_CROSS_REF+" " +
            "WHERE movie_id=:movie_id " +
            "AND type=:type " +
            "AND source=:source")
    public MovieUserFavoriteCrossRef query(String movie_id,String type,String source);

    @Query("DELETE FROM "+TABLE.MOVIE_USER_FAVORITE_CROSS_REF)
    public int deleteAll();

    @Query("DELETE FROM "+TABLE.MOVIE_USER_FAVORITE_CROSS_REF+" WHERE movie_id=:movie_id AND type=:type AND source=:source")
    public int delete(String movie_id,String type,String source);

}
