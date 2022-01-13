package com.hphtv.movielibrary.roomdb.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;

import com.hphtv.movielibrary.roomdb.entity.Season;
import com.hphtv.movielibrary.roomdb.entity.StagePhoto;

/**
 * author: Sam Leung
 * date:  2022/1/13
 */
@Dao
public interface SeasonDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public long insertOrIgnore(Season season);
}
