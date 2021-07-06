package com.hphtv.movielibrary.roomdb.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.hphtv.movielibrary.roomdb.TABLE;
import com.hphtv.movielibrary.roomdb.VIEW;
import com.hphtv.movielibrary.roomdb.entity.UnrecognizedFileDataView;
import com.hphtv.movielibrary.roomdb.entity.VideoFile;

import java.util.List;

/**
 * author: Sam Leung
 * date:  2021/5/24
 */
@Dao
public interface VideoFileDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public long insertOrIgnore(VideoFile videoFile);

    @Update
    public int update(VideoFile videoFile);

    @Query("SELECT * FROM " + TABLE.VIDEOFILE)
    public List<VideoFile> queryAll();

    @Query("SELECT * FROM " + TABLE.VIDEOFILE + " WHERE device_id in (:device_ids) AND is_scanned=0")
    public List<VideoFile> queryAllNotScanedByIds(String... device_ids);

    @Query("SELECT * FROM " + TABLE.VIDEOFILE + " WHERE path=:path")
    public VideoFile queryByPath(String path);

    @Query("DELETE FROM " + TABLE.VIDEOFILE + " WHERE device_id=:deviceId and path not in (:paths) ")
    public void deleteByDeviceId(String deviceId, String[] paths);

    @Query("DELETE FROM " + TABLE.VIDEOFILE)
    public void deleteAll();


    @Query("SELECT * FROM " + VIEW.UNRECOGNIZEDFILE_DATAVIEW +
            " GROUP BY keyword")
    public List<UnrecognizedFileDataView> queryUnrecognizedFiles();

    @Query("SELECT * FROM " + VIEW.UNRECOGNIZEDFILE_DATAVIEW + " WHERE keyword=:keyword")
    public List<UnrecognizedFileDataView> queryUnrecognizedFilesByKeyword(String keyword);
}
