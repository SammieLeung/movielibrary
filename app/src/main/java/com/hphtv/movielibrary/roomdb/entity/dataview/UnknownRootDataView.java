package com.hphtv.movielibrary.roomdb.entity.dataview;

import androidx.annotation.Nullable;
import androidx.room.DatabaseView;
import androidx.room.Ignore;

import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.roomdb.TABLE;
import com.hphtv.movielibrary.roomdb.VIEW;

import java.io.Serializable;
import java.util.Objects;

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
public class UnknownRootDataView implements Serializable {
    public String root;
    public Constants.UnknownRootType type;
    public int count;
    public Constants.WatchLimit s_ap;
    @Ignore
    public ConnectedFileDataView connectedFileView;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UnknownRootDataView that = (UnknownRootDataView) o;
        return root.equals(that.root) && type == that.type && s_ap == that.s_ap;
    }

    @Override
    public int hashCode() {
        return Objects.hash(root, type, s_ap);
    }
}
