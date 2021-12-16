package com.hphtv.movielibrary.roomdb.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.hphtv.movielibrary.roomdb.TABLE;

import java.io.Serializable;
import java.util.Objects;

/**
 * author: Sam Leung
 * date:  2021/5/24
 */
@Entity(tableName = TABLE.VIDEOFILE,
        indices = {@Index(value = {"path"}, unique = true)})
public class VideoFile implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public long vid;
    @NonNull
    public String path;
    @ColumnInfo(name = "device_path")
    public String devicePath;
    @ColumnInfo(name = "dir_path")
    public String dirPath;
    public String filename;
    @ColumnInfo(name = "is_scanned",defaultValue = "0")
    public int isScanned;

    public String keyword;

    @ColumnInfo(name = "add_time",defaultValue = "0")
    public long addTime;//文件添加时间

    @ColumnInfo(name = "last_playtime",defaultValue = "0")
    public long lastPlayTime;//上次播放时间
    @ColumnInfo(defaultValue = "-1")
    public int season=-1;
    @ColumnInfo(defaultValue = "-1")
    public int episode=-1;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VideoFile videoFile = (VideoFile) o;
        return path.equals(videoFile.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }

    @Override
    public String toString() {
        return "VideoFile{" +
                "vid=" + vid +
                ", path='" + path + '\'' +
                ", devicePath='" + devicePath + '\'' +
                ", dirPath='" + dirPath + '\'' +
                ", filename='" + filename + '\'' +
                ", isScanned=" + isScanned +
                ", keyword='" + keyword + '\'' +
                ", addTime=" + addTime +
                '}';
    }
}
