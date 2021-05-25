package com.hphtv.movielibrary.roomdb.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;

import com.hphtv.movielibrary.roomdb.entity.MovieGenreCrossRef;
import com.hphtv.movielibrary.roomdb.entity.MovieVideoFileCrossRef;

import java.util.List;

/**
 * author: Sam Leung
 * date:  2021/5/29
 */
@Dao
public interface MovieVideofileCrossRefDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public long insertMovieVideofileCrossRef(MovieVideoFileCrossRef movieVideoFileCrossRef);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public long[] insertMovieVideofileCrossRefs(MovieVideoFileCrossRef... movieVideoFileCrossRef);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public long[] insertMovieVideofileCrossRefs(List<MovieVideoFileCrossRef> movieVideoFileCrossRef);
}
