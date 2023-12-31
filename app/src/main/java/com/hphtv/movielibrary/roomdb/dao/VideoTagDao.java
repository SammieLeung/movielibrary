package com.hphtv.movielibrary.roomdb.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.roomdb.TABLE;
import com.hphtv.movielibrary.roomdb.entity.VideoTag;

import java.util.List;

/**
 * author: Sam Leung
 * date:  2022/3/1
 */
@Dao
public interface VideoTagDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public long insertOrIgnore(VideoTag videoTag);

    @Query("SELECT * FROM "+ TABLE.VIDEO_TAG+" WHERE tag=:tag")
    public VideoTag queryVtidByNormalTag(String tag);

    @Query("SELECT * FROM "+ TABLE.VIDEO_TAG+" WHERE tag=:tag AND flag=0")
    public VideoTag queryVtidBySysTag(Constants.VideoType tag);

    @Query("SELECT * FROM "+TABLE.VIDEO_TAG+" WHERE flag=0")
    public List<VideoTag> queryAllVideoTags();
}
