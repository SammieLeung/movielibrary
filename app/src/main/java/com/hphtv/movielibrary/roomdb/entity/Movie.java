package com.hphtv.movielibrary.roomdb.entity;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Until;
import com.hphtv.movielibrary.roomdb.TABLE;

import java.io.Serializable;

/**
 * author: Sam Leung
 * date:  21-5-14
 */
@Entity(tableName = TABLE.MOVIE,indices = {@Index(value = {"movie_id","source"},unique = true)})
public class Movie  implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public long id;
    @ColumnInfo(name = "movie_id")
    public String movieId;//电影id
    public String title;//电影名称
    @ColumnInfo(name="title_other")
    public String otherTitle;//电影别名
    public String plot;//剧情
    public String ratings;//评分
    public String source;//来源
    public String type;//电影？电视剧？综艺？
    public String season;//季数
    @ColumnInfo(name = "episode_count")
    public int episodeCount;//集数
    public String poster;//海报
    public String country;//制片国家
    @ColumnInfo(name = "release_date")
    public String releaseDate;//上映时间
    @ColumnInfo(name = "release_area")
    public String releaseArea;//上映地区
    public String year;//年份
    public String duration;//时长
    public String language;//语言
    public String pinyin;//汉语拼音顺序
    @ColumnInfo(name = "add_time")
    public long addTime;//添加时间
    @ColumnInfo(name = "update_time")
    public long updateTime;//更新时间
    @ColumnInfo(name = "is_favorite")
    public boolean isFavorite;//收藏
    @ColumnInfo(name = "last_playtime")
    public long lastPlayTime;//上次播放时间

    @Ignore
    public String tag;
    @Ignore
    public String tag2;

    @Override
    public String toString() {
        return "Movie{" +
                "movieId='" + movieId + '\'' +
                ", title='" + title + '\'' +
                '}';
    }

}
