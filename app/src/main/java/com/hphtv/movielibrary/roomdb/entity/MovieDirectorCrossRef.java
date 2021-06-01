package com.hphtv.movielibrary.roomdb.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.hphtv.movielibrary.roomdb.TABLE;

/**
 * author: Sam Leung
 * date:  2021/5/27
 */
@Entity(tableName = TABLE.MOVIE_DIRECTOR_CROSS_REF,primaryKeys = {"id","director_id"},indices = {@Index(value = "director_id")})
public class MovieDirectorCrossRef {
    public long id;
    @ColumnInfo(name = "director_id")
    public long directorId;
}
