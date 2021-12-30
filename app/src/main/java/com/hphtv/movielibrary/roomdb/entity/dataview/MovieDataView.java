package com.hphtv.movielibrary.roomdb.entity.dataview;

import androidx.room.DatabaseView;

import com.hphtv.movielibrary.roomdb.TABLE;
import com.hphtv.movielibrary.roomdb.VIEW;

import java.util.Objects;

/**
 * author: Sam Leung
 * date:  2021/6/8
 */

@DatabaseView(
        value =
                "SELECT M.id,M.movie_id,M.title,M.pinyin,M.poster,M.ratings,M.year,M.source,VF.path AS file_uri,ST.uri AS dir_uri,ST.device_path AS device_uri,ST.name AS dir_name,ST.friendly_name AS dir_fname ,G.name AS genre_name,M.add_time,M.last_playtime,M.is_favorite " +
                        "FROM " + TABLE.VIDEOFILE + " AS VF " +
                        "JOIN " + TABLE.SHORTCUT + " AS ST  " +
                        "ON VF.dir_path=ST.uri " +
                        "JOIN " + TABLE.DEVICE + " AS DEV " +
                        "ON DEV.path=ST.device_path OR ST.device_type > 5 " +
                        "JOIN " + TABLE.MOVIE_VIDEOFILE_CROSS_REF + " AS MVCF " +
                        "ON MVCF.path=VF.path " +
                        "JOIN " + TABLE.MOVIE + " AS M " +
                        "ON MVCF.id=M.id " +
                        "LEFT OUTER JOIN " + TABLE.MOVIE_GENRE_CROSS_REF + " AS MGCF  " +
                        "ON M.id=MGCF.id " +
                        "LEFT OUTER JOIN " + TABLE.GENRE + " AS G " +
                        "ON MGCF.genre_id = G.genre_id",
        viewName = VIEW.MOVIE_DATAVIEW
)
public class MovieDataView {
    public long id;
    public String movie_id;
    public String title;
    public String pinyin;
    public String poster;
    public String ratings;
    public String year;
    public String source;
    public String file_uri;
    public String dir_uri;
    public String device_uri;
    public String dir_name;
    public String dir_fname;
    public String genre_name;
    public long add_time;
    public long last_playtime;
    public boolean is_favorite;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MovieDataView that = (MovieDataView) o;
        return movie_id.equals(that.movie_id) &&
                source.equals(that.source);
    }

    @Override
    public int hashCode() {
        return Objects.hash(movie_id, source);
    }
}
