package com.hphtv.movielibrary.roomdb.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.hphtv.movielibrary.roomdb.TABLE;

import java.util.UUID;

/**
 * author: Sam Leung
 * date:  2021/5/20
 */

@Entity(tableName = TABLE.DEVICE)
public class Device {

    @PrimaryKey
    @NonNull
    public String id = UUID.randomUUID().toString();
    @ColumnInfo(name = "name")
    public String name;
    @ColumnInfo(name = "type")
    public int type;
    @ColumnInfo(name = "local_path")
    public String localPath;
    @ColumnInfo(name = "network_path")
    public String networkPath;
    @ColumnInfo(name = "connect_state")
    public int connectState;
    @ColumnInfo(name = "file_count")
    public int fileCount;

}
