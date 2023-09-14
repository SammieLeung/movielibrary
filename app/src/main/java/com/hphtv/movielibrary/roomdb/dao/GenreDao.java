package com.hphtv.movielibrary.roomdb.dao;

import androidx.annotation.Nullable;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.hphtv.movielibrary.data.Constants;
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

    @Query("SELECT name FROM " + TABLE.GENRE)
    public List<String> queryAllGenres();

    @Query("SELECT genre_id FROM " + TABLE.GENRE + " WHERE name in (:names)")
    public long[] queryByName(List<String> names);

    @Query("SELECT name FROM " + TABLE.GENRE + " WHERE genre_id=:genre_id ")
    public String queryGenreName(long genre_id);

    @Query("SELECT genre_name FROM " + VIEW.MOVIE_DATAVIEW + " AS M " +
            " JOIN " + TABLE.MOVIE_VIDEOTAG_CROSS_REF + " AS MVCF " +
            " ON M.id=MVCF.id " +
            " JOIN " + TABLE.VIDEO_TAG + " AS VT " +
            " ON VT.vtid=MVCF.vtid " +
            " WHERE source=:source " +
            " AND genre_name!=\"\" "+
            " AND (:video_tag IS NULL OR VT.tag=:video_tag)" +
            " GROUP BY genre_name")
    public List<String> queryGenresBySource(String source, @Nullable String video_tag);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public long[] insertGenreTags(List<GenreTag> genreTagItemList);

    @Query("DELETE FROM " + TABLE.GENRE_TAG)
    public int deleteAllGenreTags();

    @Query("SELECT name FROM " + TABLE.GENRE_TAG + " WHERE source=:source ORDER BY weight")
    public List<String> queryGenreTagNameBySource(String source);

    @Query("SELECT * FROM " + TABLE.GENRE_TAG + " WHERE source=:source ORDER BY weight")
    public List<GenreTag> queryGenreTagBySource(String source);

    @Query("SELECT G.name FROM "+TABLE.MOVIE_GENRE_CROSS_REF+" AS MGCF " +
            "JOIN "+TABLE.GENRE+" AS G ON MGCF.genre_id=G.genre_id " +
            "WHERE MGCF.id=:id AND G.source=:source")
    public List<String> queryGenreNamesById(Long id, String source);
}
