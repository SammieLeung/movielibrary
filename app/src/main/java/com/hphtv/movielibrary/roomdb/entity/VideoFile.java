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
    @ColumnInfo(name = "device_id")
    public String deviceId;
    public String filename;
    @ColumnInfo(name = "is_scanned")
    public int isScanned;

    @Ignore
    public String keyword;

}
