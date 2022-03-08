package com.hphtv.movielibrary.roomdb.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;

import com.hphtv.movielibrary.roomdb.TABLE;

/**
 * author: Sam Leung
 * date:  2021/11/10
 */
@Entity(tableName = TABLE.GENRE_TAG,primaryKeys ={"name","source"})
public class GenreTag {
    @NonNull
    public String name;
    @NonNull
    public String source;
    public int weight=0;
}
