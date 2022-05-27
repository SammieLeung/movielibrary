package com.hphtv.movielibrary.roomdb.entity;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.roomdb.TABLE;

import org.jetbrains.annotations.NotNull;

/**
 * author: Sam Leung
 * date:  2022/3/1
 */
@Entity(tableName = TABLE.VIDEO_TAG, indices = {@Index(value = {"tag", "tag_name"}, unique = true)})
public class VideoTag {
    @PrimaryKey(autoGenerate = true)
    public long vtid;
    public Constants.VideoType tag;
    @NonNull
    @ColumnInfo(name = "tag_name",defaultValue = "")
    public String tagName="";
    public int flag;//0-不能删除，1-可以删除
    public int weight = 0;

    public VideoTag() {
    }

    @Ignore
    public VideoTag(Constants.VideoType type) {
        tag = type;
        flag = 0;
    }

    @Ignore
    public VideoTag(String tagName) {
        this.tagName = tagName;
        tag = Constants.VideoType.custom;
        this.flag = 1;
    }

    @Ignore
    public String toTagName(Context context) {
        switch (tag) {
            case movie:
                return context.getString(R.string.video_type_movie);
            case tv:
                return context.getString(R.string.video_type_tv);
            case child:
                return context.getString(R.string.video_type_cartoon);
            case unknow:
                return context.getString(R.string.video_type_undefine);
            case animate:
                return context.getString(R.string.video_type_animate);
            case variety_show:
                return context.getString(R.string.video_type_variety_show);
            case other:
                return context.getString(R.string.video_type_other);
            case custom:
                return tagName;
        }
        return null;
    }
}
