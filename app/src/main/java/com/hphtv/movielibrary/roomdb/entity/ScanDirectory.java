package com.hphtv.movielibrary.roomdb.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.hphtv.movielibrary.roomdb.TABLE;


/**
 * 扫描任务中需要扫描的文件夹
 * author: Sam Leung
 * date:  2021/5/15
 */
@Entity(tableName = TABLE.SCAN_DIRECTORY)
public class ScanDirectory {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String path;
    public String deviceId;

    public ScanDirectory(String path, String deviceId) {
        this.path = path;
        this.deviceId = deviceId;
    }

}
