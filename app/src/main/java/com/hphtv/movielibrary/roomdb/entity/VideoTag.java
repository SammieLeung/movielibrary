package com.hphtv.movielibrary.roomdb.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.roomdb.TABLE;

/**
 * author: Sam Leung
 * date:  2022/3/1
 */
@Entity(tableName = TABLE.VIDEO_TAG, indices = {@Index(value = {"tag","tag_name"}, unique = true)})
public class VideoTag {
    @PrimaryKey(autoGenerate = true)
    public long vtid;
    public Constants.VideoType tag;
    @ColumnInfo(name = "tag_name")
    public String tagName;
    public int flag;//0-不能删除，1-可以删除
    public int weight = 0;

    public VideoTag(){}

    @Ignore
    public VideoTag(Constants.VideoType type) {
        tag= type;
        flag=0;
    }

    @Ignore
    public VideoTag(String tagName) {
        this.tagName = tagName;
        tag= Constants.VideoType.custom;
        this.flag = 1;
    }
}
