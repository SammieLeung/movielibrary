package com.hphtv.movielibrary.roomdb.entity.relation;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import com.hphtv.movielibrary.roomdb.entity.Actor;
import com.hphtv.movielibrary.roomdb.entity.Director;
import com.hphtv.movielibrary.roomdb.entity.Genre;
import com.hphtv.movielibrary.roomdb.entity.Movie;
import com.hphtv.movielibrary.roomdb.entity.Season;
import com.hphtv.movielibrary.roomdb.entity.StagePhoto;
import com.hphtv.movielibrary.roomdb.entity.Trailer;
import com.hphtv.movielibrary.roomdb.entity.VideoFile;
import com.hphtv.movielibrary.roomdb.entity.reference.MovieActorCrossRef;
import com.hphtv.movielibrary.roomdb.entity.reference.MovieDirectorCrossRef;
import com.hphtv.movielibrary.roomdb.entity.reference.MovieGenreCrossRef;
import com.hphtv.movielibrary.roomdb.entity.reference.MovieVideoFileCrossRef;

import java.io.Serializable;
import java.util.List;

/**
 * author: Sam Leung
 * date:  2021/5/25
 */
public class MovieWrapper implements Serializable {
    @Embedded
    public Movie movie;//实体父类

    @Relation(parentColumn = "id",
            entityColumn = "path",
            associateBy = @Junction(MovieVideoFileCrossRef.class))
    public List<VideoFile> videoFiles;//视频文件列表，多对多

    @Relation(parentColumn = "id",
            entityColumn = "director_id",
            associateBy = @Junction(MovieDirectorCrossRef.class))
    public List<Director> directors;//导演，多对多关系

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
            entityColumn = "movie_id"
    )
    public List<Trailer> trailers;//预告片列表，一对多

    @Relation(parentColumn = "id",
            entityColumn = "movie_id")
    public List<StagePhoto> stagePhotos;//剧照，一对多

    @Relation(parentColumn = "id",
    entityColumn = "movie_id")//分季,一对多
    public List<Season> seasons;

    public String toGenreString() {
        StringBuffer sb = new StringBuffer();
        if (genres != null) {
            for (Genre genre : genres) {
                sb.append(genre.toString() + " ");
            }
            if (sb.length() > 0)
                sb.replace(sb.lastIndexOf(" "), sb.length(), "");
        }
        return sb.toString();
    }

    public String toActorString() {
        StringBuffer sb = new StringBuffer();
        if(actors!=null) {
            int i = 0;
            for (Actor actor : actors) {
                if (i >= 3)
                    break;
                sb.append(actor.toString() + ",");
                i++;
            }
            if (sb.length() > 0)
                sb.replace(sb.lastIndexOf(","), sb.length(), "");
        }
        return sb.toString();
    }

}
