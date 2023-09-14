package com.hphtv.movielibrary.roomdb.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.hphtv.movielibrary.roomdb.entity.StagePhoto;

import java.util.List;

/**
 * author: Sam Leung
 * date:  2021/6/15
 */
@Dao
public interface StagePhotoDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public long insertOrIgnore(StagePhoto stagePhoto);

    @Query("SELECT * FROM STAGEPHOTO WHERE movie_id = :id LIMIT :limit OFFSET :offset")
    public List<StagePhoto> queryStagePhotosById(long id, int limit, int offset);


}


