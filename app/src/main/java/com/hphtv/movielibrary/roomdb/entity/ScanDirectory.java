package com.hphtv.movielibrary.roomdb.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.roomdb.TABLE;

import java.util.Objects;


/**
 * 扫描任务中需要扫描的文件夹
 * author: Sam Leung
 * date:  2021/5/15
 */
@Entity(tableName = TABLE.SCAN_DIRECTORY,indices = {@Index(value = {"path"},unique = true)})
public class ScanDirectory {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "scan_dir_id")
    public long scanDirId;
    public String path;
    @ColumnInfo(name = "device_path")
    public String devicePath;
    @ColumnInfo(name = "is_user_add")
    public boolean  isUserAdd;
    @ColumnInfo(name="is_hidden")
    public boolean isHidden;
    @ColumnInfo(name = "friendly_name")
    public String friendlyName;
    public Constants.FolderPermission permissions;

    @Ignore
    public String parentPath;

    public ScanDirectory(String path, String devicePath) {
        this.path = path;
        this.devicePath = devicePath;
        this.isUserAdd=false;
        this.isHidden=false;
        this.parentPath=path;
        this.permissions = Constants.FolderPermission.MOVIE;
        this.friendlyName=path.substring(path.lastIndexOf("/")+1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScanDirectory that = (ScanDirectory) o;
        return Objects.equals(path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }
}
