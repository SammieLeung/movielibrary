package com.hphtv.movielibrary.roomdb.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.hphtv.movielibrary.roomdb.TABLE;

/**
 * author: Sam Leung
 * date:  2021/5/27
 */
@Entity(tableName = TABLE.DIRECTOR)
public class Director {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "director_id")
    public long directorId;
    public String name;
    @ColumnInfo(name = "name_en")
    public String nameEn;
    public String img;
}
