package com.hphtv.movielibrary.roomdb.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;

import com.hphtv.movielibrary.roomdb.entity.reference.MovieDirectorCrossRef;

/**
 * author: Sam Leung
 * date:  2021/5/29
 */
@Dao
public interface MovieDirectorCrossRefDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public long insertMovieDirectorCrossRef(MovieDirectorCrossRef movieDirectorCrossRef);
}
