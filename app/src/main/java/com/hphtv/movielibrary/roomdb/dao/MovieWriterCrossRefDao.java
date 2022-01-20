package com.hphtv.movielibrary.roomdb.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;

import com.hphtv.movielibrary.roomdb.entity.reference.MovieDirectorCrossRef;
import com.hphtv.movielibrary.roomdb.entity.reference.MovieWriterCrossRef;

/**
 * author: Sam Leung
 * date:  2021/5/29
 */
@Dao
public interface MovieWriterCrossRefDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public long insertMovieWriterCrossRef(MovieWriterCrossRef movieWriterCrossRef);
}