package com.hphtv.movielibrary.roomdb.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.hphtv.movielibrary.roomdb.TABLE;
import com.hphtv.movielibrary.roomdb.entity.Actor;
import com.hphtv.movielibrary.roomdb.entity.Genre;

import java.util.List;

/**
 * author: Sam Leung
 * date:  2021/5/29
 */
@Dao
public interface GenreDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public long[] insertGenres(Genre... genres);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public long[] insertGenres(List<Genre> genres);

    @Query("SELECT name From "+ TABLE.GENRE)
    public List<String> queryAllGenres();
}
