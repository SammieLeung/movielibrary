package com.hphtv.movielibrary.roomdb.entity;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.hphtv.movielibrary.roomdb.TABLE;

/**
 * author: Sam Leung
 * date:  2021/5/24
 */
@Entity(tableName = TABLE.VIDEOFILE,
        indices = {@Index(value = {"path"}, unique = true)})
public class VideoFile {
    @PrimaryKey(autoGenerate = true)
    public long vid;
    public String path;
    @ColumnInfo(name = "device_id")
    public String deviceId;
    public String filename;
    @ColumnInfo(name = "is_scanned")
    public int isScanned;

    @Ignore
    public String keyword;

}
