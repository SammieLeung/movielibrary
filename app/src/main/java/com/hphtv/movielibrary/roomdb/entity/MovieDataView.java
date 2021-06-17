package com.hphtv.movielibrary.roomdb.entity;

import androidx.room.DatabaseView;

import com.hphtv.movielibrary.roomdb.TABLE;

/**
 * author: Sam Leung
 * date:  2021/6/8
 */

@DatabaseView(
        value =
        "SELECT M.id,M.title,M.pinyin,M.poster,M.ratings,M.year,MVCF__VF__DEV.path,MVCF__VF__DEV.device_id,genre_name,M.add_time,M.last_playtime,M.is_favorite " +
                "FROM " + TABLE.MOVIE + " AS M " +
                "JOIN (SELECT MVCF.id,MVCF.path,VF__DEV.id AS device_id FROM " + TABLE.MOVIE_VIDEOFILE_CROSS_REF + " AS MVCF " +
                "JOIN (SELECT VF.path,DEV.id from " + TABLE.VIDEOFILE + " AS VF " +
                "JOIN " + TABLE.DEVICE + " AS DEV " +
                "ON VF.device_id=DEV.id) AS VF__DEV " +
                "ON MVCF.path=VF__DEV.path) AS MVCF__VF__DEV " +
                "ON MVCF__VF__DEV.id = M.id " +
                "LEFT OUTER  JOIN (SELECT MGCF.id,G.name AS genre_name FROM " + TABLE.GENRE + " AS G " +
                "JOIN " + TABLE.MOVIE_GENRE_CROSS_REF + " AS MGCF " +
                "ON MGCF.genre_id = G.genre_id) AS MGCF__G " +
                "ON MGCF__G.id = M.id ",
        viewName = "movie_dataview"
)
public class MovieDataView {
    public long id;
    public String title;
    public String pinyin;
    public String poster;
    public String ratings;
    public String year;
    public String path;
    public String device_id;
    public String genre_name;
    public long add_time;
    public long last_playtime;
    public boolean is_favorite;
}
