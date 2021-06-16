package com.hphtv.movielibrary.roomdb.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;

import com.hphtv.movielibrary.roomdb.entity.Genre;
import com.hphtv.movielibrary.roomdb.entity.MovieActorCrossRef;

import java.util.List;

/**
 * author: Sam Leung
 * date:  2021/5/29
 */
@Dao
public interface MovieActorCrossRefDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public long insertMovieActorCrossRef(MovieActorCrossRef movieActorCrossRef);
}
