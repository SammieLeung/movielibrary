package com.hphtv.movielibrary.roomdb.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.hphtv.movielibrary.roomdb.TABLE;

import java.io.Serializable;

/**
 * author: Sam Leung
 * date:  2022/1/13
 */
@Entity(tableName = TABLE.SEASON,indices = @Index(value = {"movie_id","season_number","source"},unique = true))
public class Season implements Serializable {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "season_id")
    public long seasonId;
    @ColumnInfo(name = "movie_id")
    public long movieId;
    @ColumnInfo(name = "season_number")
    public int seasonNumber;
    @ColumnInfo(name = "episode_count")
    public int episodeCount;
    public String name;
    public String plot;
    public String poster;
    @ColumnInfo(name = "air_date")
    public String airDate;
    public String source;


}
