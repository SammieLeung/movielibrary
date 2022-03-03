package com.hphtv.movielibrary.roomdb.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;

import com.hphtv.movielibrary.roomdb.entity.reference.MovieGenreCrossRef;

/**
 * author: Sam Leung
 * date:  2021/5/29
 */
@Dao
public interface MovieGenreCrossRefDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public long insertMovieGenreCrossRef(MovieGenreCrossRef movieGenreCrossRef);
}
