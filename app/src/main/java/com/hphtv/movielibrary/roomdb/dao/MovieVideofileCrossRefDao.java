package com.hphtv.movielibrary.roomdb.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.hphtv.movielibrary.roomdb.TABLE;
import com.hphtv.movielibrary.roomdb.entity.reference.MovieVideoFileCrossRef;

import java.util.List;

/**
 * author: Sam Leung
 * date:  2021/5/29
 */
@Dao
public interface MovieVideofileCrossRefDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public long insertOrReplace(MovieVideoFileCrossRef movieVideoFileCrossRef);

    @Query("SELECT * FROM "+TABLE.MOVIE_VIDEOFILE_CROSS_REF+" WHERE path=:path")
    public List<MovieVideoFileCrossRef> queryByPath(String path);
    @Query("SELECT * FROM "+TABLE.MOVIE_VIDEOFILE_CROSS_REF+" WHERE path=:path AND source=:source")
    public MovieVideoFileCrossRef queryByPath(String path,String source);
    @Update
    public int update(MovieVideoFileCrossRef movieVideoFileCrossRef);

    @Query("DELETE FROM "+ TABLE.MOVIE_VIDEOFILE_CROSS_REF+" WHERE id=:movie_id")
    public void deleteById(long movie_id);

    @Query("DELETE FROM "+ TABLE.MOVIE_VIDEOFILE_CROSS_REF+" WHERE path IN (:paths)")
    public void deleteByPaths(List<String> paths);


}
