package com.hphtv.movielibrary.roomdb.entity.reference;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;

import com.hphtv.movielibrary.roomdb.TABLE;

/**
 * author: Sam Leung
 * date:  2022/3/1
 */
@Entity(tableName = TABLE.MOVIE_VIDEOTAG_CROSS_REF,primaryKeys = {"vtid","id"})

public class MovieVideoTagCrossRef {
    @NonNull
    public long vtid;
    @NonNull
    public long id;
}
