package com.hphtv.movielibrary.roomdb.entity;

import android.text.TextUtils;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.roomdb.TABLE;

import java.util.Objects;

/**
 * author: Sam Leung
 * date:  2021/11/30
 */
@Entity(tableName = TABLE.SHORTCUT, indices = {@Index(value = {"uri"}, unique = true)})
public class Shortcut {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "shortcut_id")
    public long shortcutId;
    public String name;
    @ColumnInfo(name = "device_path")
    public String devicePath;
    @ColumnInfo(name = "friendly_name")
    public String firendlyName;
    public String uri;
    @ColumnInfo(name="device_type")
    public int devcieType;//smb
    @ColumnInfo(name = "file_count")
    public int fileCount;
    @ColumnInfo(name = "poster_count")
    public int posterCount;
    @ColumnInfo(name = "folder_type")
    public Constants.FolderType folderType;
    public Constants.AccessPermission access;

    public Shortcut(String uri, int devcieType,String name,String firendlyName) {
        this.folderType = Constants.FolderType.MOVIE;
        this.access = Constants.AccessPermission.NORMAL;
        this.uri = uri;
        this.devcieType = devcieType;
        String tmp;
        switch (devcieType) {
            case Constants.DeviceType.DEVICE_TYPE_LOCAL:
            case Constants.DeviceType.DEVICE_TYPE_INTERNAL_STORAGE:
            case Constants.DeviceType.DEVICE_TYPE_USB:
            case Constants.DeviceType.DEVICE_TYPE_SDCARDS:
            case Constants.DeviceType.DEVICE_TYPE_PCIE:
            case Constants.DeviceType.DEVICE_TYPE_HARD_DISK:
            case Constants.DeviceType.DEVICE_TYPE_SMB:
                tmp = uri.endsWith("/") ? uri.substring(0, uri.length() - 1) : uri;
                this.name = TextUtils.isEmpty(name)?tmp.substring(tmp.lastIndexOf("/") + 1):name;
                this.firendlyName = TextUtils.isEmpty(firendlyName)? this.name:firendlyName;
                break;
            case Constants.DeviceType.DEVICE_TYPE_DLNA:
                this.name =  TextUtils.isEmpty(name)?uri.substring(uri.lastIndexOf(":") + 1):name;
                this.firendlyName = TextUtils.isEmpty(firendlyName)? this.name:firendlyName;
                break;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Shortcut shortcut = (Shortcut) o;
        return uri.equals(shortcut.uri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri);
    }
}
