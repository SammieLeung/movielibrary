package com.hphtv.movielibrary.bean;

import java.util.ArrayList;

/**
 * author: Sam Leung
 * date:  2022/11/10
 */
public class PlayList {
    String path;
    String name;
    ArrayList<String> mPlayList;
    ArrayList<String> mNameList;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<String> getPlayList() {
        return mPlayList;
    }

    public void setPlayList(ArrayList<String> playList) {
        mPlayList = playList;
    }

    public ArrayList<String> getNameList() {
        return mNameList;
    }

    public void setNameList(ArrayList<String> nameList) {
        mNameList = nameList;
    }
}
