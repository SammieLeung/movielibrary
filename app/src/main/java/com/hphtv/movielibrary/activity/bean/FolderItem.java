package com.hphtv.movielibrary.activity.bean;

import androidx.annotation.NonNull;

import com.hphtv.movielibrary.roomdb.entity.Shortcut;

import java.util.Objects;

/**
 * author: Sam Leung
 * date:  2021/12/1
 */
public class FolderItem {
    public String title;
    public String uri;
    @NonNull
    public Shortcut item;
    public int type;
    public int poster_count;
    public int file_count;
    public int state;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FolderItem that = (FolderItem) o;
        return item.equals(that.item);
    }

    @Override
    public int hashCode() {
        return Objects.hash(item);
    }
}
