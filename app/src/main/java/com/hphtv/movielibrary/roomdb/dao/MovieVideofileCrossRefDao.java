package com.hphtv.movielibrary.roomdb.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.hphtv.movielibrary.roomdb.TABLE;
import com.hphtv.movielibrary.roomdb.entity.reference.MovieVideoFileCrossRef;

/**
 * author: Sam Leung
 * date:  2021/5/29
 */
@Dao
public interface MovieVideofileCrossRefDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public long insertOrReplace(MovieVideoFileCrossRef movieVideoFileCrossRef);

//    @Query("SELECT * FROM "+TABLE.MOVIE_VIDEOFILE_CROSS_REF+" WHERE path=:path")
//    public MovieVideoFileCrossRef queryByPath(String path);

    @Update
    public int update(MovieVideoFileCrossRef movieVideoFileCrossRef);

    @Query("DELETE FROM "+ TABLE.MOVIE_VIDEOFILE_CROSS_REF+" WHERE id=:movie_id")
    public void deleteById(long movie_id);


}
