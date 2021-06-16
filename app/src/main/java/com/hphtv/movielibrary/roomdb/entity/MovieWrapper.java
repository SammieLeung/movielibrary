package com.hphtv.movielibrary.roomdb.entity;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Junction;
import androidx.room.PrimaryKey;
import androidx.room.Relation;

import com.hphtv.movielibrary.roomdb.TABLE;

import java.util.List;

/**
 * author: Sam Leung
 * date:  2021/5/25
 */
public class MovieWrapper {
    @Embedded
    public Movie movie;//实体父类

    @Relation(parentColumn = "id",
            entityColumn = "path",
            associateBy = @Junction(MovieVideoFileCrossRef.class))
    public List<VideoFile> videoFiles;//视频文件列表，一对多

    @Relation(parentColumn = "id",
            entityColumn = "director_id",
            associateBy = @Junction(MovieDirectorCrossRef.class))
    public Director director;//导演，多对多关系

    @Relation(parentColumn = "id",
            entityColumn = "actor_id",
            associateBy = @Junction(MovieActorCrossRef.class))
    public List<Actor> actors;//演员列表，多对多关系

    @Relation(parentColumn = "id",
            entityColumn = "genre_id",
            associateBy = @Junction(MovieGenreCrossRef.class))
    public List<Genre> genres;//电影分类列表，多对多关系

    @Relation(
            parentColumn = "id",
            entityColumn = "tra_id"
    )
    public List<Trailer> trailers;//预告片列表

    public String toGenreString(){
        StringBuffer sb=new StringBuffer();
        for(Genre genre:genres){
            sb.append(genre.toString()+" ");
        }
        sb.replace(sb.lastIndexOf(" "),sb.length(),"");
        return sb.toString();
    }

    public String toActorString(){
        StringBuffer sb=new StringBuffer();
        int i=0;
        for(Actor actor:actors){
            if(i>=2)
                break;
            sb.append(actor.toString()+",");
            i++;
        }
        sb.replace(sb.lastIndexOf(","),sb.length(),"");
        return sb.toString();
    }
}
