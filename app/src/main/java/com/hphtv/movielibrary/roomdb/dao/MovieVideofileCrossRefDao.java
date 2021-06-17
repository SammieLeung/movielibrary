package com.hphtv.movielibrary.roomdb.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.hphtv.movielibrary.roomdb.entity.MovieGenreCrossRef;
import com.hphtv.movielibrary.roomdb.entity.MovieVideoFileCrossRef;
import com.hphtv.movielibrary.roomdb.entity.MovieWrapper;

import java.util.List;

/**
 * author: Sam Leung
 * date:  2021/5/29
 */
@Dao
public interface MovieVideofileCrossRefDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public long insertMovieVideofileCrossRef(MovieVideoFileCrossRef movieVideoFileCrossRef);


}
