package com.hphtv.movielibrary.roomdb.entity;

import android.text.TextUtils;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.roomdb.TABLE;

import java.io.Serializable;
import java.util.Objects;

/**
 * author: Sam Leung
 * date:  2021/11/30
 */
@Entity(tableName = TABLE.SHORTCUT, indices = {@Index(value = {"uri"}, unique = true)})
public class Shortcut implements Serializable {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "shortcut_id")
    public long shortcutId;
    public String name;
    @ColumnInfo(name = "device_path")
    public String devicePath;
    @ColumnInfo(name = "friendly_name")
    public String friendlyName;
    public String uri;
    @ColumnInfo(name = "device_type")
    public int deviceType;//smb
    @ColumnInfo(name = "file_count")
    public int fileCount = 0;
    @ColumnInfo(name = "poster_count")
    public int posterCount = 0;
    @ColumnInfo(name = "folder_type")
    public Constants.SearchType folderType;
    public Constants.AccessPermission access;
    @ColumnInfo(name = "query_uri")
    public String queryUri;
    @ColumnInfo(name = "is_scanned")
    public int isScanned = 0;
    @ColumnInfo(name="autoscan")
    public int autoScan=0;

    public Shortcut(String uri, int deviceType, String name, String friendlyName, String queryUri) {
        this.folderType = Constants.SearchType.auto;
        this.access = Constants.AccessPermission.ALL_AGE;
        this.uri = uri;
        this.deviceType = deviceType;
        this.queryUri = queryUri;
        String tmp;
        switch (deviceType) {
            case Constants.DeviceType.DEVICE_TYPE_LOCAL:
            case Constants.DeviceType.DEVICE_TYPE_INTERNAL_STORAGE:
            case Constants.DeviceType.DEVICE_TYPE_USB:
            case Constants.DeviceType.DEVICE_TYPE_SDCARDS:
            case Constants.DeviceType.DEVICE_TYPE_PCIE:
            case Constants.DeviceType.DEVICE_TYPE_HARD_DISK:
            case Constants.DeviceType.DEVICE_TYPE_SMB:
                tmp = uri.endsWith("/") ? uri.substring(0, uri.length() - 1) : uri;
                this.name = TextUtils.isEmpty(name) ? tmp.substring(tmp.lastIndexOf("/") + 1) : name;
                this.friendlyName = TextUtils.isEmpty(friendlyName) ? this.name : friendlyName;
                break;
            case Constants.DeviceType.DEVICE_TYPE_DLNA:
                this.name = TextUtils.isEmpty(name) ? uri.substring(uri.lastIndexOf(":") + 1) : name;
                this.friendlyName = TextUtils.isEmpty(friendlyName) ? this.name : friendlyName;
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

    @Override
    public String toString() {
        return "Shortcut{" +
                "shortcutId=" + shortcutId +
                ", name='" + name + '\'' +
                ", devicePath='" + devicePath + '\'' +
                ", firendlyName='" + friendlyName + '\'' +
                ", uri='" + uri + '\'' +
                ", devcieType=" + deviceType +
                ", fileCount=" + fileCount +
                ", posterCount=" + posterCount +
                ", folderType=" + folderType +
                ", access=" + access +
                ", queryUri='" + queryUri + '\'' +
                ", isScanned=" + isScanned +
                ", autoScan=" + autoScan +
                '}';
    }
}
