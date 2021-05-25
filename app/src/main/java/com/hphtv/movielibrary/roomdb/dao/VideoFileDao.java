package com.hphtv.movielibrary.roomdb.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.hphtv.movielibrary.roomdb.TABLE;
import com.hphtv.movielibrary.roomdb.entity.VideoFile;

import java.util.List;

/**
 * author: Sam Leung
 * date:  2021/5/24
 */
@Dao
public interface VideoFileDao {

    @Query("SELECT * FROM "+TABLE.VIDEOFILE)
    public List<VideoFile> queryAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public long[] insert(VideoFile... fileInfos);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public long[] insert(List<VideoFile> fileInfos);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    public void update(VideoFile videoFile);

    @Query("DELETE FROM " + TABLE.VIDEOFILE + " WHERE vid=:deviceId")
    public void deleteByDeviceId(String deviceId);

    @Query("DELETE FROM "+ TABLE.VIDEOFILE)
    public void deleteAll();

}
