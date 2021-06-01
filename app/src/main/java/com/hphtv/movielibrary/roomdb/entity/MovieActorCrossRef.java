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
@Entity(tableName = TABLE.MOVIE_ACTOR_CROSS_REF,primaryKeys = {"id","actor_id"},indices = {@Index(value = "actor_id")})
public class MovieActorCrossRef {
    public long id;
    @ColumnInfo(name = "actor_id")
    public long actorId;
}
