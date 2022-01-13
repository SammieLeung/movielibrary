package com.hphtv.movielibrary.roomdb.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.hphtv.movielibrary.roomdb.TABLE;

import java.io.Serializable;

/**
 * author: Sam Leung
 * date:  2021/5/27
 */
@Entity(tableName = TABLE.WRITER)
public class Writer implements Serializable {
    @PrimaryKey
    @ColumnInfo(name = "writer_id")
    public long writerId;
    public String name;
    @ColumnInfo(name = "name_en")
    public String nameEn;
    public String img;

    @Override
    public String toString() {
        return name+"("+nameEn+")";
    }
}
