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
@Entity(tableName = TABLE.GENRE,indices = {@Index(value = "name",unique = true)})
public class Genre {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "genre_id")
    public long genreId;
    public String name;
}
