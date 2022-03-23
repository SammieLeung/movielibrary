package com.hphtv.movielibrary.roomdb.entity.reference;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.hphtv.movielibrary.roomdb.TABLE;

/**
 * 以后可能出现中、英两部电影对应一个视频。
 * author: Sam Leung
 * date:  2021/5/27
 */
@Entity(tableName = TABLE.MOVIE_VIDEOFILE_CROSS_REF,primaryKeys = {"path","source"},indices = {@Index("id")})
public class MovieVideoFileCrossRef {
    @NonNull
    public String path;
    @NonNull
    public String source;
    public long id;
    @NonNull
    @ColumnInfo(name = "timestamp")
    public long timeStamp;

}
