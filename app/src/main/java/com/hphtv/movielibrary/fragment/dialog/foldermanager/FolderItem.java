package com.hphtv.movielibrary.fragment.dialog.foldermanager;

import androidx.annotation.NonNull;

import java.util.Objects;

/**
 * author: Sam Leung
 * date:  2021/12/1
 */
public class FolderItem {
    public String title;
    public String uri;
    @NonNull
    public Object item;
    public int type;

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
