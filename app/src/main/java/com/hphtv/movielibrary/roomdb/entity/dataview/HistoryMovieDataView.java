package com.hphtv.movielibrary.roomdb.entity.dataview;

import androidx.room.DatabaseView;

import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.roomdb.TABLE;
import com.hphtv.movielibrary.roomdb.VIEW;

/**
 * author: Sam Leung
 * date:  2022/2/22
 */
@DatabaseView(
        value = "SELECT u.filename,u.keyword,u.path,u.last_playtime," +
                "m.poster,m.source,m.title,m.ratings,m.ap,st.access AS s_ap," +
                "sp.img_url AS stage_photo " +
                "FROM "+VIEW.UNRECOGNIZEDFILE_DATAVIEW+" AS u " +
                "LEFT OUTER JOIN "+VIEW.MOVIE_DATAVIEW+" AS m " +
                "ON u.path=m.file_uri " +
                "LEFT OUTER JOIN "+ TABLE.STAGEPHOTO +" AS sp " +
                "ON sp.movie_id=m.id "+
                "LEFT OUTER JOIN shortcut AS st " +
                "ON st.uri=u.dir_uri "+
                "WHERE u.last_playtime!=0 " +
                "GROUP BY u.path,m.source " +
                "ORDER BY u.last_playtime DESC"
        ,
        viewName = VIEW.HISTORY_MOVIE_DATAVIEW
)
public class HistoryMovieDataView {
    public String poster;
    public String keyword;
    public String title;
    public String filename;
    public String ratings;
    public Constants.AccessPermission ap;
    public Constants.AccessPermission s_ap;
    public String path;
    public String source;
    public String stage_photo;
    public long last_playtime;
}
