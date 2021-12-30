package com.hphtv.movielibrary.ui.homepage.genretag;

/**
 * author: Sam Leung
 * date:  2021/11/13
 */
public class GenreTagItem {
    private String name;
    private boolean isChecked;

    public GenreTagItem(String name, boolean isChecked) {
        this.name = name;
        this.isChecked = isChecked;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}
