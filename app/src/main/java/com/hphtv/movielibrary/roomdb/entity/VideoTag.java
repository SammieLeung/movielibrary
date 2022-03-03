package com.hphtv.movielibrary.roomdb.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.hphtv.movielibrary.roomdb.TABLE;

/**
 * author: Sam Leung
 * date:  2022/3/1
 */
@Entity(tableName = TABLE.VIDEO_TAG, indices = {@Index(value = {"tag"}, unique = true)})
public class VideoTag {
    @PrimaryKey(autoGenerate = true)
    public long vtid;
    public String tag;
    public int flag;//0-不能删除，1-可以删除
    public String source;

}
