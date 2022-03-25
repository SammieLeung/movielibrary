package com.hphtv.movielibrary.ui.postermenu;

import android.app.Application;

import androidx.annotation.NonNull;

import com.hphtv.movielibrary.BaseAndroidViewModel;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;

import org.jetbrains.annotations.NotNull;

/**
 * author: Sam Leung
 * date:  2022/3/25
 */
public class PosterMenuViewModel extends BaseAndroidViewModel {
    public MovieDataView mMovieDataView;

    public PosterMenuViewModel(@NonNull @NotNull Application application) {
        super(application);
    }

    public void setMovieDataView(MovieDataView movieDataView) {
        mMovieDataView = movieDataView;
    }
}
