package com.hphtv.movielibrary.roomdb.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;

import com.hphtv.movielibrary.roomdb.entity.MovieDirectorCrossRef;
import com.hphtv.movielibrary.roomdb.entity.MovieGenreCrossRef;

import java.util.List;

/**
 * author: Sam Leung
 * date:  2021/5/29
 */
@Dao
public interface MovieGenreCrossRefDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public long insertMovieGenreCrossRef(MovieGenreCrossRef movieGenreCrossRef);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public long[] insertMovieGenreCrossRefs(MovieGenreCrossRef... movieGenreCrossRefs);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public long[] insertMovieGenreCrossRefs(List<MovieGenreCrossRef> movieGenreCrossRefs);
}
