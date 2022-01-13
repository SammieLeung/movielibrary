package com.hphtv.movielibrary.ui.shortcutmanager.bean;

import androidx.annotation.NonNull;

import com.hphtv.movielibrary.roomdb.entity.Shortcut;

import java.util.Objects;

/**
 * author: Sam Leung
 * date:  2021/12/1
 */
public class FolderItem {
    public String title;
    public String sub_title;
    @NonNull
    public Shortcut item;
    public int type;
    public int poster_count;
    public int file_count;
    public State state;

    public  enum State {
        UNSCANNED,SCANNING,SCANNED
    }

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
