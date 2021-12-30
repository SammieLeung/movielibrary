package com.hphtv.movielibrary.ui.shortcutmanager.bean;

import java.util.List;
import java.util.Objects;

/**
 * author: Sam Leung
 * date:  2021/12/30
 */
public class ShortcutOptionsItem{
    private String mTitle;
    private int mPos=0;
    private List<String> mOptionList;

    public ShortcutOptionsItem(String title, List<String> optionList) {
        mTitle = title;
        mOptionList=optionList;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }


    public List<String> getOptionList() {
        return mOptionList;
    }

    public void setOptionList(List<String> optionList) {
        mOptionList = optionList;
    }

    public int getPos() {
        return mPos;
    }

    public void setPos(int pos) {
        mPos = pos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShortcutOptionsItem that = (ShortcutOptionsItem) o;
        return Objects.equals(mTitle, that.mTitle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mTitle);
    }
}
