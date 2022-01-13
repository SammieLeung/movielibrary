package com.hphtv.movielibrary.ui.shortcutmanager.bean;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.hphtv.movielibrary.BR;

import java.util.List;
import java.util.Objects;

/**
 * author: Sam Leung
 * date:  2021/12/30
 */
public class ShortcutOptionsItem extends BaseObservable {
    @Bindable
    private String mTitle;
    @Bindable
    private int mPos=0;
    @Bindable
    private List<String> mOptionList;
    @Bindable
    private String mSubTitle;

    public ShortcutOptionsItem(String title, List<String> optionList) {
        mTitle = title;
        mOptionList=optionList;
    }

    public ShortcutOptionsItem(String title,String subTitle){
        mTitle=title;
        mSubTitle=subTitle;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
        notifyPropertyChanged(BR.title);
    }


    public List<String> getOptionList() {
        return mOptionList;
    }

    public void setOptionList(List<String> optionList) {
        mOptionList = optionList;
        notifyPropertyChanged(BR.optionList);
    }

    public int getPos() {
        return mPos;
    }

    public void setPos(int pos) {
        mPos = pos;
        notifyPropertyChanged(BR.pos);
    }

    public String getSubTitle() {
        return mSubTitle;
    }

    public void setSubTitle(String subTitle) {
        mSubTitle = subTitle;
        notifyPropertyChanged(BR.subTitle);
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
