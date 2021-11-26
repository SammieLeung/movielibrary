package com.hphtv.movielibrary.roomdb.entity.dataview;

import androidx.room.DatabaseView;

import com.hphtv.movielibrary.roomdb.VIEW;

/**
 * author: Sam Leung
 * date:  2021/11/16
 */
@DatabaseView(
        viewName = VIEW.HISTORY_MOVIE_DATAVIEW,
        value = ""
)
public class HistoryMovieDataView {
}
