package com.hphtv.movielibrary.roomdb.entity.dataview;

import androidx.room.ColumnInfo;
import androidx.room.DatabaseView;

import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.roomdb.TABLE;
import com.hphtv.movielibrary.roomdb.VIEW;

/**
 * author: Sam Leung
 * date:  2022/2/22
 */
@DatabaseView(
        value = "SELECT filename,keyword,path,last_playtime,episode,aired,s_ap,last_position,duration," +
                "-1 AS _mid,NULL AS movie_id,NULL AS poster,NULL AS source,NULL AS title,NULL AS ratings,NULL AS ap,NULL AS type," +
                "-1 AS season,NULL AS season_name,NULL AS season_poster," +
                "NULL AS stage_photo " +
                "FROM " + VIEW.CONNECTED_FILE_DATAVIEW + " " +
                "WHERE last_playtime >0 AND path NOT IN (SELECT path FROM "+TABLE.MOVIE_VIDEOFILE_CROSS_REF+") " +
                "UNION " +
                "SELECT u.filename,u.keyword,u.path,max(u.last_playtime) AS last_playtime,u.episode,u.aired,u.s_ap,u.last_position,u.duration," +
                "mv.id AS _mid,mv.movie_id,mv.poster,mv.source,mv.title,mv.ratings,mv.ap,mv.type," +
                "CASE WHEN s.season_number IS NOT NULL THEN s.season_number ELSE -1 END AS season,s.name AS season_name,s.poster AS season_poster," +
                "sp.img_url AS stage_photo " +
                "FROM " +
                "(SELECT * FROM " + VIEW.CONNECTED_FILE_DATAVIEW + " WHERE last_playtime >0) AS u " +
                "JOIN " +
                "(SELECT m.id,m.movie_id,m.title,m.ratings,m.source,m.type,m.poster,m.ap,m.type,mvcf.path FROM "+TABLE.MOVIE+" AS m JOIN "+TABLE.MOVIE_VIDEOFILE_CROSS_REF+" AS mvcf ON mvcf.id = m.id) AS mv " +
                "ON mv.path = u.path " +
                "LEFT OUTER JOIN "+TABLE.SEASON+" AS s " +
                "ON s.movie_id=mv.id AND u.season=s.season_number " +
                "LEFT OUTER JOIN (SELECT * FROM (SELECT * FROM "+TABLE.STAGEPHOTO+" ORDER BY movie_id,img_url DESC) GROUP BY movie_id) AS sp " +
                "ON sp.movie_id=mv.id " +
                "GROUP BY mv.movie_id,mv.source " +
                "ORDER BY last_playtime DESC",
        viewName = VIEW.HISTORY_MOVIE_DATAVIEW
)
public class HistoryMovieDataView {
    public String filename;
    public String keyword;
    public String path;
    public long last_playtime;
    public int episode = -1;
    public String aired;
    public Constants.WatchLimit s_ap;
    public long last_position;
    public long duration;

    public long _mid;
    public String movie_id;
    public String poster;
    public String source;
    public String title;
    public String ratings;
    public Constants.WatchLimit ap;
    public Constants.SearchType type;

    public int season;
    public String season_name;
    public String season_poster;

    public String stage_photo;
}
