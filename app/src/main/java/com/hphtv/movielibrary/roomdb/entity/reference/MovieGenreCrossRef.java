package com.hphtv.movielibrary.roomdb.entity.reference;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.hphtv.movielibrary.roomdb.TABLE;

/**
 * author: Sam Leung
 * date:  2021/5/27
 */
@Entity(tableName = TABLE.MOVIE_GENRE_CROSS_REF,primaryKeys = {"id","genre_id"},indices = {@Index(value = "genre_id")})
public class MovieGenreCrossRef {
    public long id;
    @ColumnInfo(name = "genre_id")
    public long genreId;
}
