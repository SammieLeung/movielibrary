package com.hphtv.movielibrary.roomdb.entity.relation;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import com.hphtv.movielibrary.roomdb.entity.VideoTag;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.hphtv.movielibrary.roomdb.entity.reference.MovieVideoTagCrossRef;

import java.util.List;

/**
 * author: Sam Leung
 * date:  2022/3/1
 */
public class VideoTagWithMovieDataView {
    @Embedded
    public VideoTag mVideoTag;
    @Relation(
            parentColumn = "vtid",
            entityColumn = "id",
            associateBy = @Junction(MovieVideoTagCrossRef.class)
    )
    public List<MovieDataView> mMovieDataViewList;
}
