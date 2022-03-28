package com.hphtv.movielibrary.roomdb.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.hphtv.movielibrary.roomdb.VIEW;
import com.hphtv.movielibrary.roomdb.entity.VideoTag;
import com.hphtv.movielibrary.roomdb.entity.reference.MovieVideoTagCrossRef;
import com.hphtv.movielibrary.roomdb.entity.relation.MovieDataViewWithVdieoTags;

import java.util.List;

/**
 * author: Sam Leung
 * date:  2022/3/2
 */
@Dao
public interface MovieVideoTagCrossRefDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public long insert(MovieVideoTagCrossRef crossRef);

    @Query("SELECT * FROM "+ VIEW.MOVIE_DATAVIEW +" WHERE id=:id")
    @Transaction
    public MovieDataViewWithVdieoTags queryMovieDataViewWithVideoTags(long id);
}
