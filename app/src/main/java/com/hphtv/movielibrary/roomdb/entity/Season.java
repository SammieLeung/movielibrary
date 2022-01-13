package com.hphtv.movielibrary.roomdb.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.hphtv.movielibrary.roomdb.TABLE;

/**
 * author: Sam Leung
 * date:  2022/1/13
 */
@Entity(tableName = TABLE.SEASON)
public class Season {

        @PrimaryKey
        @ColumnInfo(name = "season_id")
        public long seasonId;
        @ColumnInfo(name = "movie_id")
        public long movieId;
        @ColumnInfo(name = "season_number")
        public int seasonNumber;
        @ColumnInfo(name = "episode_count")
        public int episodeCount;
        public String name;
        @ColumnInfo(name = "name_en")
        public String nameEN;
        public String plot;
        @ColumnInfo(name = "plot_en")
        public String plotEN;
        public String poster;
        @ColumnInfo(name = "poster_en")
        public String posterEN;
        @ColumnInfo(name="air_date")
        public String airDate;


}
