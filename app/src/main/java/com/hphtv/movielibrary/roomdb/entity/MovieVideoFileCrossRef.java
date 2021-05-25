package com.hphtv.movielibrary.roomdb.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;

import com.hphtv.movielibrary.roomdb.TABLE;

/**
 * author: Sam Leung
 * date:  2021/5/27
 */
@Entity(tableName = TABLE.MOVIE_VIDEOFILE_CROSS_REF,primaryKeys = {"id","vid"})
public class MovieVideoFileCrossRef {
    public long id;
    public long vid;
}
