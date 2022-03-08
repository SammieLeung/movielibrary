package com.hphtv.movielibrary.ui.homepage.genretag;

import androidx.databinding.ObservableBoolean;

import com.hphtv.movielibrary.roomdb.entity.GenreTag;

import java.util.Objects;

import io.reactivex.rxjava3.core.Observable;

/**
 * author: Sam Leung
 * date:  2021/11/13
 */
public class GenreTagItem {
    private String name;
    private ObservableBoolean isChecked=new ObservableBoolean();

    public GenreTagItem(String name, boolean isChecked) {
        this.name = name;
        this.isChecked.set(isChecked);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ObservableBoolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        this.isChecked.set(checked);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GenreTagItem that = (GenreTagItem) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
