package com.hphtv.movielibrary.ui.homepage.fragment.customtag;

import android.app.Application;

import androidx.annotation.NonNull;

import com.hphtv.movielibrary.data.Config;
import com.hphtv.movielibrary.roomdb.entity.dataview.HistoryMovieDataView;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.hphtv.movielibrary.ui.homepage.fragment.BaseHomePageViewModel;
import com.hphtv.movielibrary.util.ScraperSourceTools;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * author: Sam Leung
 * date:  2022/10/25
 */
public class CustomTagViewModel extends BaseHomePageViewModel {
    public CustomTagViewModel(@NonNull @NotNull Application application) {
        super(application);
    }

    @Override
    protected List<HistoryMovieDataView> queryHistoryMovieDataView() {

        return mVideoFileDao.queryHistoryMovieDataViewByVideoTag(ScraperSourceTools.getSource(), Config.getSqlConditionOfChildMode(),"child", 0, LIMIT);
    }

    @Override
    protected List<String> queryGenresBySource() {
        return null;
    }

    @Override
    protected List<MovieDataView> queryMovieDataViewForRecentlyAdded() {
        return null;
    }

    @Override
    protected List<MovieDataView> queryFavoriteMovieDataView() {
        return null;
    }

    @Override
    protected MovieDataView queryMovieDataViewByMovieId(String movie_id, String type) {
        return null;
    }

    @Override
    protected List<MovieDataView> queryRecommendByGenres(String source, List<String> genreList, List<Long> idList) {
        return null;
    }

    @Override
    protected List<MovieDataView> queryRecommend(String source) {
        return null;
    }
}
