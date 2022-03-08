package com.hphtv.movielibrary.roomdb.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.hphtv.movielibrary.roomdb.TABLE;
import com.hphtv.movielibrary.roomdb.VIEW;
import com.hphtv.movielibrary.roomdb.entity.Actor;
import com.hphtv.movielibrary.roomdb.entity.Genre;
import com.hphtv.movielibrary.roomdb.entity.GenreTag;
import com.hphtv.movielibrary.ui.homepage.genretag.GenreTagItem;

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
    public long[] insertGenreTags(List<GenreTag> genreTagItemList);

    @Query("DELETE FROM "+TABLE.GENRE_TAG)
    public int deleteAllGenreTags();

    @Query("SELECT name FROM "+TABLE.GENRE_TAG +" WHERE source=:source")
    public List<String> queryGenreTagNameBySource(String source);
}
