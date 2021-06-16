package com.hphtv.movielibrary.roomdb.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.hphtv.movielibrary.roomdb.TABLE;

/**
 * author: Sam Leung
 * date:  2021/6/16
 */
@Entity (tableName = TABLE.STAGEPHOTO)
public class StagePhoto {
    @PrimaryKey
    @ColumnInfo(name = "stage_id")
    public long stageId;
    @ColumnInfo(name = "movie_id")
    public long movieId;
    public String imgUrl;

}
