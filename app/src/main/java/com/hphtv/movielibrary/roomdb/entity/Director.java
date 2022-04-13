package com.hphtv.movielibrary.roomdb.entity;

import android.text.TextUtils;
import android.widget.TextView;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.roomdb.TABLE;
import com.hphtv.movielibrary.util.ScraperSourceTools;

import java.io.Serializable;

/**
 * author: Sam Leung
 * date:  2021/5/27
 */
@Entity(tableName = TABLE.DIRECTOR)
public class Director implements Serializable {
    @PrimaryKey
    public long director_id;
    public String name;
    @ColumnInfo(name = "name_en")
    public String nameEn;
    public String img;

    @Override
    public String toString() {
        if (ScraperSourceTools.getSource().equals(Constants.Scraper.TMDB))
            return name;
        else
            return TextUtils.isEmpty(nameEn) ? name : nameEn;
    }
}
