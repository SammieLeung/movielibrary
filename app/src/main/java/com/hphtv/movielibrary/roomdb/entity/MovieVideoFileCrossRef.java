package com.hphtv.movielibrary.roomdb.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;

import com.hphtv.movielibrary.roomdb.TABLE;

/**
 * author: Sam Leung
 * date:  2021/5/27
 */
@Entity(tableName = TABLE.MOVIE_VIDEOFILE_CROSS_REF,primaryKeys = {"id","path"},indices = {@Index(value = "path")})
public class MovieVideoFileCrossRef {
    public long id;
    @NonNull
    public String path;
}
