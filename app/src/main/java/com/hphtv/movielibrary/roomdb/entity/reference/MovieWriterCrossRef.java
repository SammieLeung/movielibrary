package com.hphtv.movielibrary.roomdb.entity.reference;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;

import com.hphtv.movielibrary.roomdb.TABLE;

/**
 * author: Sam Leung
 * date:  2021/5/27
 */
@Entity(tableName = TABLE.MOVIE_WRITER_CROSS_REF,primaryKeys = {"id","writer_id"},indices = {@Index(value = "writer_id")})
public class MovieWriterCrossRef {
    public long id;
    @ColumnInfo(name = "writer_id")
    public long writerId;
}
