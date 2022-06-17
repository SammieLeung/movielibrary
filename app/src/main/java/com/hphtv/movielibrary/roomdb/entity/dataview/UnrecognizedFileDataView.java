package com.hphtv.movielibrary.roomdb.entity.dataview;

import androidx.room.DatabaseView;

import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.roomdb.TABLE;
import com.hphtv.movielibrary.roomdb.VIEW;

import java.io.Serializable;

/**
 * author: Sam Leung
 * date:  2021/6/22
 */
@DatabaseView(value =
        "SELECT VF.vid,VF.filename,VF.keyword,VF.path,ST.uri AS dir_uri,ST.access AS s_ap,DEV.path AS device_uri,VF.add_time,VF.last_playtime,VF.season,VF.episode,VF.aired " +
                "FROM " + TABLE.VIDEOFILE + " AS VF " +
                "JOIN " + TABLE.SHORTCUT + " AS ST " +
                "ON VF.dir_path=ST.uri " +
                "JOIN " + TABLE.DEVICE + " AS DEV " +
                "ON DEV.path=ST.device_path " +
                "UNION " +
                "SELECT VF.vid,VF.filename,VF.keyword,VF.path,ST.uri AS dir_uri,ST.access AS s_ap,NULL AS device_uri,VF.add_time,VF.last_playtime,VF.season,VF.episode,VF.aired " +
                "FROM " + TABLE.VIDEOFILE + " AS VF " +
                "JOIN " + TABLE.SHORTCUT + " AS ST " +
                "ON VF.dir_path=ST.uri " +
                "WHERE ST.device_type>5",
        viewName = VIEW.UNRECOGNIZEDFILE_DATAVIEW)
public class UnrecognizedFileDataView implements Serializable {
    public long vid;
    public String filename;
    public String keyword;
    public String path;
    public String dir_uri;
    public String device_uri;
    public Constants.WatchLimit s_ap;
    public long last_playtime;
    public long add_time;
    public String episode;
    public String aired;
    public int season;


}
