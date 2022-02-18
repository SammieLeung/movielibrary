package com.hphtv.movielibrary.roomdb.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.hphtv.movielibrary.roomdb.TABLE;
import com.hphtv.movielibrary.roomdb.VIEW;
import com.hphtv.movielibrary.roomdb.entity.Actor;
import com.hphtv.movielibrary.roomdb.entity.Genre;
import com.hphtv.movielibrary.roomdb.entity.GenreTag;

import java.util.List;

/**
 * author: Sam Leung
 * date:  2021/5/29
 */
@Dao
public interface GenreDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public long[] insertGenres(List<Genre> genres);

    @Query("SELECT name FROM "+ TABLE.GENRE)
    public List<String> queryAllGenres();

    @Query("SELECT genre_id FROM "+TABLE.GENRE +" WHERE name in (:names)")
    public long[] queryByName(List<String> names);

    @Query("SELECT genre_name FROM "+ VIEW.MOVIE_DATAVIEW +" WHERE source=:source AND genre_name!=\"\" GROUP BY genre_name;")
    public List<String> queryGenresBySource(String source);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public long insertGenreTag(GenreTag genreTag);

    @Query("SELECT name FROM "+TABLE.GENRE_TAG +" WHERE source=:source")
    public List<String> queryGenreTagBySource(String source);
}
