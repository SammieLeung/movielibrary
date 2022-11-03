package com.hphtv.movielibrary.roomdb.entity.dataview;

import androidx.room.DatabaseView;
import androidx.room.Ignore;

import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.roomdb.TABLE;
import com.hphtv.movielibrary.roomdb.VIEW;

/**
 * author: Sam Leung
 * date:  2022/10/31
 */
@DatabaseView(
        value = "SELECT RTRIM(path,REPLACE(path,'/','')) AS root,'FOLDER' AS type,COUNT(RTRIM(PATH,REPLACE(path,'/','')) ) AS count,s_ap " +
                "FROM "+ VIEW.CONNECTED_FILE_DATAVIEW+
                " WHERE path NOT IN (SELECT path FROM "+ TABLE.MOVIE_VIDEOFILE_CROSS_REF+") GROUP BY root HAVING COUNT(root)>1 " +
                "UNION " +
                "SELECT path AS root,'FILE' AS type,1 AS count,s_ap " +
                "FROM "+ VIEW.CONNECTED_FILE_DATAVIEW+
                " WHERE path NOT IN (SELECT path FROM "+ TABLE.MOVIE_VIDEOFILE_CROSS_REF+") GROUP BY RTRIM(path,REPLACE(path,'/',''))   HAVING COUNT(RTRIM(PATH,REPLACE(PATH,'/',''))) =1 " +
                "ORDER BY type DESC",
        viewName = VIEW.UNKNOWN_ROOT_DATAVIEW
)
public class UnknownRootDataView {
    public String root;
    public Constants.UnknownRootType type;
    public int count;
    public Constants.WatchLimit s_ap;
}
