package com.hphtv.movielibrary.roomdb.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.hphtv.movielibrary.roomdb.TABLE;

import java.io.Serializable;

/**
 * author: Sam Leung
 * date:  2021/6/16
 */
@Entity (tableName = TABLE.STAGEPHOTO)
public class StagePhoto implements Serializable {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "stage_id")
    public long stageId;
    @ColumnInfo(name = "movie_id")
    public long movieId;
    @ColumnInfo(name = "img_url")
    public String imgUrl;

}
