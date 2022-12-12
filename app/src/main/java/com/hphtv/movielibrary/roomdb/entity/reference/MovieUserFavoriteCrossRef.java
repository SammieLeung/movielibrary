package com.hphtv.movielibrary.roomdb.entity.reference;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;

import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.roomdb.TABLE;


/**
 * author: Sam Leung
 * date:  2022/12/9
 */
@Entity(tableName = TABLE.MOVIE_USER_FAVORITE_CROSS_REF,primaryKeys = {"movie_id","type","source"})
public class MovieUserFavoriteCrossRef {
    @NonNull
    public String movie_id;
    @NonNull
    public Constants.VideoType type;
    @NonNull
    public String source;
}
