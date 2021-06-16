package com.hphtv.movielibrary.roomdb.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.hphtv.movielibrary.roomdb.TABLE;

/**
 * author: Sam Leung
 * date:  2021/6/15
 */
@Entity(tableName = TABLE.TRAILER)
public class Trailer {
    @PrimaryKey
    public long tra_id;
    public String url;
    public String title;
    public String img;
    public long m_id;
}
