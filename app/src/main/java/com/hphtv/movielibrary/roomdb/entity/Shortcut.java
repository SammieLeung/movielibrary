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
 * author: Sam Leung
 * date:  2021/11/30
 */
@Entity(tableName = TABLE.SHORTCUT, indices = {@Index(value = {"uri"}, unique = true)})
public class Shortcut {

    @PrimaryKey(autoGenerate = true)
    public long id;
    public String name;
    public String uri;
    public int type;
    @ColumnInfo(name = "file_count")
    public int fileCount;
    @ColumnInfo(name = "poster_count")
    public int posterCount;
    public Constants.FolderPermission permissions;

    public Shortcut() {
        permissions = Constants.FolderPermission.MOVIE;
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
