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
        value = "SELECT u.filename,u.keyword,u.path,u.last_playtime,u.episode,u.aired,u.s_ap," +
                "mv.poster,mv.source,mv.title,mv.ratings,mv.ap," +
                "CASE WHEN s.season_number IS NOT NULL THEN s.season_number " +
                "ELSE -1 END AS season,s.name AS season_name,s.poster AS season_poster," +
                "sp.img_url AS stage_photo " +
                "FROM (SELECT * FROM " + VIEW.UNRECOGNIZEDFILE_DATAVIEW + " WHERE last_playtime >0 ORDER BY last_playtime DESC) AS u " +
                "JOIN (SELECT m.id,m.movie_id,m.title,m.ratings,m.source,m.type,m.poster,m.ap,mvcf.path FROM " + TABLE.MOVIE + " AS m " +
                "JOIN " + TABLE.MOVIE_VIDEOFILE_CROSS_REF + " AS mvcf " +
                "ON mvcf.id = m.id " +
                "ORDER BY m.movie_id) AS mv " +
                "ON mv.path = u.path " +
                "LEFT OUTER JOIN " + TABLE.SEASON + " AS s " +
                "ON s.movie_id=mv.id AND u.season=s.season_number " +
                "LEFT OUTER JOIN (SELECT * FROM (SELECT * FROM " + TABLE.STAGEPHOTO + " ORDER BY movie_id,img_url DESC) GROUP BY movie_id) AS sp " +
                "ON sp.movie_id=mv.id " +
                "WHERE u.last_playtime>0 " +
                "GROUP BY mv.movie_id " +
                "ORDER BY u.last_playtime DESC",
        viewName = VIEW.HISTORY_MOVIE_DATAVIEW
)
public class HistoryMovieDataView {
    public String poster;
    public String keyword;
    public String title;
    public String filename;
    public String ratings;
    public Constants.WatchLimit ap;
    public Constants.WatchLimit s_ap;
    public String path;
    public String source;
    public String stage_photo;
    public long last_playtime;

    public int season;
    public String season_name;
    public String season_poster;

    public int episode = -1;
    public String aired;

}
