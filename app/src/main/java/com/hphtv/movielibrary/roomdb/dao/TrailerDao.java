package com.hphtv.movielibrary.roomdb.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;

import com.hphtv.movielibrary.roomdb.entity.Trailer;

/**
 * author: Sam Leung
 * date:  2021/6/15
 */
@Dao
public interface TrailerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertOrReplace(Trailer... trailers);
}


