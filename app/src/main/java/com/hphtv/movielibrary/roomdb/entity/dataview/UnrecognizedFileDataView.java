package com.hphtv.movielibrary.roomdb.entity.dataview;

import androidx.room.DatabaseView;

import com.hphtv.movielibrary.roomdb.TABLE;
import com.hphtv.movielibrary.roomdb.VIEW;

/**
 * author: Sam Leung
 * date:  2021/6/22
 */
@DatabaseView(value =
        "SELECT VF.vid,VF.filename,VF.keyword,VF.path,SD__DEV.dir_path,SD__DEV.device_id,SD__DEV.device_path FROM " + TABLE.VIDEOFILE + " AS VF " +
                "JOIN (SELECT SD.path AS dir_path,DEV.id AS device_id,DEV.path AS device_path FROM " + TABLE.DEVICE + " AS DEV " +
                "JOIN " + TABLE.SCAN_DIRECTORY + " AS SD " +
                "ON SD.device_path=DEV.path) AS SD__DEV " +
                "ON VF.dir_path=SD__DEV.dir_path " +
                "WHERE VF.path NOT IN (SELECT path FROM " + TABLE.MOVIE_VIDEOFILE_CROSS_REF + ")",
        viewName = VIEW.UNRECOGNIZEDFILE_DATAVIEW)
public class UnrecognizedFileDataView {
    public long vid;
    public String keyword;
    public String filename;
    public String path;
    public String dir_path;
    public String device_id;
    public String device_path;
}
