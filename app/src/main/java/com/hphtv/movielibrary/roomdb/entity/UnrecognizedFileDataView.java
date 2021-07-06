package com.hphtv.movielibrary.roomdb.entity;

import androidx.room.DatabaseView;

import com.hphtv.movielibrary.roomdb.TABLE;
import com.hphtv.movielibrary.roomdb.VIEW;

/**
 * author: Sam Leung
 * date:  2021/6/22
 */
@DatabaseView(value ="SELECT VF__DEV.vid,VF__DEV.keyword,VF__DEV.filename,VF__DEV.path,VF__DEV.id AS device_id,VF__DEV.device_path FROM " +
        "(SELECT DEV.id,VF.vid,VF.filename,VF.keyword,VF.path,VF.is_scanned,DEV.local_path AS device_path FROM " + TABLE.VIDEOFILE+" AS VF " +
        "JOIN "+TABLE.DEVICE+" AS DEV " +
        "ON VF.device_id=DEV.id) AS VF__DEV " +
        "LEFT OUTER JOIN "+TABLE.MOVIE_VIDEOFILE_CROSS_REF+" AS MVCF " +
        "ON MVCF.path = VF__DEV.path " +
        "WHERE  VF__DEV.path NOT IN (SELECT path FROM movie_videofile_cross_ref)",
        viewName = VIEW.UNRECOGNIZEDFILE_DATAVIEW)
public class UnrecognizedFileDataView {
    public long vid;
    public String keyword;
    public String filename;
    public String path;
    public String device_id;
    public String device_path;
}
