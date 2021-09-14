package com.hphtv.movielibrary.roomdb.entity.dataview;

import androidx.room.DatabaseView;

import com.hphtv.movielibrary.roomdb.TABLE;
import com.hphtv.movielibrary.roomdb.VIEW;

/**
 * author: Sam Leung
 * date:  2021/9/10
 */
@DatabaseView(
        value =
                "SELECT M.id,M.movie_id,M.title,M.pinyin,M.poster,M.ratings,M.year,MVCF__VF__SD.source,MVCF__VF__SD.path,MVCF__VF__SD.dir_path,MVCF__VF__SD.device_id,genre_name,M.add_time,M.last_playtime,M.is_favorite " +
                        "FROM " + TABLE.MOVIE + " AS M " +
                        "JOIN (SELECT MVCF.id,MVCF.path,MVCF.source,VF__SD__DEV.dir_path,VF__SD__DEV.device_id FROM " + TABLE.MOVIE_VIDEOFILE_CROSS_REF + " AS MVCF " +
                        "JOIN (SELECT VF__SD.path,VF__SD.dir_path,DEV.id AS device_id FROM "+ TABLE.DEVICE +" AS DEV "+
                        "JOIN (SELECT VF.path,SD.path AS dir_path,SD.device_path FROM " + TABLE.VIDEOFILE + " AS VF " +
                        "JOIN " + TABLE.SCAN_DIRECTORY + " AS SD " +
                        "ON VF.dir_path=SD.path) AS VF__SD " +
                        "ON VF__SD.device_path=DEV.path) AS VF__SD__DEV "+
                        "ON MVCF.path=VF__SD__DEV.path) AS MVCF__VF__SD " +
                        "ON MVCF__VF__SD.id = M.id " +
                        "LEFT OUTER  JOIN (SELECT MGCF.id,G.name AS genre_name FROM " + TABLE.GENRE + " AS G " +
                        "JOIN " + TABLE.MOVIE_GENRE_CROSS_REF + " AS MGCF " +
                        "ON MGCF.genre_id = G.genre_id) AS MGCF__G " +
                        "ON MGCF__G.id = M.id ",
        viewName = VIEW.REMOTE_MOVIE_DATAVIEW
)
public class RemoteMovieDataView {
}
