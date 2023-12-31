package com.hphtv.movielibrary.roomdb.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;

import com.hphtv.movielibrary.roomdb.entity.reference.MovieActorCrossRef;

/**
 * author: Sam Leung
 * date:  2021/5/29
 */
@Dao
public interface MovieActorCrossRefDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public long insertMovieActorCrossRef(MovieActorCrossRef movieActorCrossRef);
}
