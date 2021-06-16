package com.hphtv.movielibrary.roomdb.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.hphtv.movielibrary.roomdb.TABLE;

/**
 * author: Sam Leung
 * date:  2021/6/15
 */
@Entity(tableName = TABLE.TRAILER)
public class Trailer {
    @PrimaryKey
    @ColumnInfo(name = "trailer_id")
    public long trailerId;
    public String url;
    public String title;
    public String img;
    @ColumnInfo(name = "movie_id")
    public long movieId;
}
