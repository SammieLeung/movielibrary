package com.hphtv.movielibrary.roomdb.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;

import com.hphtv.movielibrary.roomdb.entity.reference.MovieVideoTagCrossRef;

/**
 * author: Sam Leung
 * date:  2022/3/2
 */
@Dao
public interface MovieVideoTagCrossRefDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public long insert(MovieVideoTagCrossRef crossRef);
}
