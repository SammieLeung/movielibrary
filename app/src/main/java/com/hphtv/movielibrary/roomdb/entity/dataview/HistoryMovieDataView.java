package com.hphtv.movielibrary.roomdb.entity.dataview;

import androidx.room.DatabaseView;

import com.hphtv.movielibrary.roomdb.TABLE;
import com.hphtv.movielibrary.roomdb.VIEW;

/**
 * author: Sam Leung
 * date:  2021/11/16
 */
@DatabaseView(
        viewName = VIEW.HISTORY_MOVIE_DATAVIEW,
        value = "SELECT u.filename,u.keyword,u.path,u.last_playtime,m.poster,m.source,m.title,st.img_url as stage_photo " +
                "FROM "+VIEW.UNRECOGNIZEDFILE_DATAVIEW+" AS u " +
                "LEFT OUTER JOIN "+VIEW.MOVIE_DATAVIEW+" AS m " +
                "ON u.path=m.file_uri " +
                "LEFT OUTER JOIN "+ TABLE.STAGEPHOTO +" AS st " +
                "ON st.movie_id=m.id "+
                "WHERE u.last_playtime!=0 " +
                "GROUP BY u.path,m.source " +
                "ORDER BY u.last_playtime DESC;"
)
public class HistoryMovieDataView {
    public String poster;
    public String keyword;
    public String title;
    public String filename;
    public String path;
    public String source;
    public String stage_photo;
    public long last_playtime;
}
