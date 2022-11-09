package com.hphtv.movielibrary.roomdb.entity.dataview;

import androidx.room.DatabaseView;

import com.hphtv.movielibrary.roomdb.TABLE;
import com.hphtv.movielibrary.roomdb.VIEW;

/**
 * author: Sam Leung
 * date:  2022/5/26
 */
@DatabaseView(
        value ="SELECT M.id,V.season,SS.name,SS.poster,ss.episode_count FROM "+ TABLE.VIDEOFILE+" AS V " +
                "JOIN movie_videofile_cross_ref AS MVC " +
                "ON V.path=MVC.path " +
                "JOIN movie AS M " +
                "ON M.id=MVC.id " +
                "JOIN season AS SS " +
                "ON SS.movie_id=M.id " +
                "WHERE V.season=SS.season_number",

        viewName = VIEW.SEASON_DATAVIEW
)
public class SeasonDataView {
    public long id;
    public int season;
    public String name;
    public String poster;
    public String episode_count;
}
