package com.hphtv.movielibrary.roomdb.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;

import com.hphtv.movielibrary.roomdb.entity.Actor;
import com.hphtv.movielibrary.roomdb.entity.Director;

import java.util.List;

/**
 * author: Sam Leung
 * date:  2021/5/29
 */
@Dao
public interface DirectorDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public void insertDirectors(List<Director> directorList);
}
