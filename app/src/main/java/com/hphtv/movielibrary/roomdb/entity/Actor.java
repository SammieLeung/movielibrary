package com.hphtv.movielibrary.roomdb.entity;

import android.text.TextUtils;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.hphtv.movielibrary.roomdb.TABLE;

import java.io.Serializable;

/**
 * author: Sam Leung
 * date:  2021/5/27
 */
@Entity(tableName = TABLE.ACTOR)
public class Actor  implements Serializable {
    @PrimaryKey
    @ColumnInfo(name = "actor_id")
    public long actorId;
    public String name;
    @ColumnInfo(name = "name_en")
    public String nameEn;
    public String img;

    @Override
    public String toString() {
        return name + (!TextUtils.isEmpty(nameEn)? "(" + nameEn + ")":"");
    }
}
