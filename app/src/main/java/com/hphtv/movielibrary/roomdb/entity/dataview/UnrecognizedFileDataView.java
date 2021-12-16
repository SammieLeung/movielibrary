package com.hphtv.movielibrary.roomdb.entity.dataview;

import androidx.room.DatabaseView;

import com.hphtv.movielibrary.roomdb.TABLE;
import com.hphtv.movielibrary.roomdb.VIEW;

/**
 * author: Sam Leung
 * date:  2021/6/22
 */
@DatabaseView(value =
        "SELECT VF.vid,VF.filename,VF.keyword,VF.path,ST.uri AS dir_uri,DEV.path AS device_uri,VF.add_time,VF.last_playtime,VF.season,VF.episode " +
                "FROM "+TABLE.VIDEOFILE+" AS VF " +
                "JOIN "+TABLE.SHORTCUT+" AS ST " +
                "ON VF.dir_path=ST.uri " +
                "JOIN "+TABLE.DEVICE+" AS DEV " +
                "ON DEV.path=ST.device_path OR ST.device_path > 5",
        viewName = VIEW.UNRECOGNIZEDFILE_DATAVIEW)
public class UnrecognizedFileDataView {
    public long vid;
    public String filename;
    public String keyword;
    public String path;
    public String dir_uri;
    public String device_uri;
    public long last_playtime;
    public long add_time;
    public int episode;
    public int season;


}
