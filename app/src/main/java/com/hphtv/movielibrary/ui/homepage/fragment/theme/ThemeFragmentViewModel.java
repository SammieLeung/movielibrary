package com.hphtv.movielibrary.ui.homepage.fragment.theme;

import android.app.Application;

import androidx.annotation.NonNull;

import com.hphtv.movielibrary.data.Config;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.roomdb.entity.dataview.HistoryMovieDataView;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.hphtv.movielibrary.ui.homepage.fragment.BaseHomePageViewModel;
import com.hphtv.movielibrary.util.ScraperSourceTools;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * author: Sam Leung
 * date:  2022/6/7
 */
public class ThemeFragmentViewModel extends BaseHomePageViewModel {
    private Constants.VideoType mVideoType;

    public ThemeFragmentViewModel(@NonNull @NotNull Application application) {
        super(application);

    }

    @Override
    protected List<HistoryMovieDataView> queryHistoryMovieDataView() {
        return mVideoFileDao.queryHistoryMovieDataViewByVideoTag(ScraperSourceTools.getSource(),  Config.getSqlConditionOfChildMode(), mVideoType.name(), 0, LIMIT);
    }

    @Override
    protected List<String> queryGenresBySource() {
        return mGenreDao.queryGenresBySource(ScraperSourceTools.getSource(), mVideoType.name());
    }

    @Override
    protected List<MovieDataView> queryMovieDataViewForRecentlyAdded() {
        return mMovieDao.queryMovieDataViewForRecentlyAddedByVideoTag(ScraperSourceTools.getSource(), mVideoType.name(), Config.getSqlConditionOfChildMode(), 0, LIMIT);
    }

    @Override
    protected List<MovieDataView> queryFavoriteMovieDataView() {
        return  mMovieDao.queryFavoriteMovieDataViewByVideoTag(ScraperSourceTools.getSource(), mVideoType.name(), Config.getSqlConditionOfChildMode(), 0, 6);
    }

    @Override
    protected List<MovieDataView> queryUserFavoriteDataView() {
        List<MovieDataView> movieDataViewList = mMovieDao.queryUserFavorite(ScraperSourceTools.getSource(), mVideoType.name(), Config.getSqlConditionOfChildMode(),0, 12);
        for (MovieDataView userFav : movieDataViewList) {
            userFav.is_user_fav = true;
        }
        return movieDataViewList;
    }

    @Override
    protected MovieDataView queryMovieDataViewByMovieId(String movie_id, String type) {
        return mMovieDao.queryMovieAsMovieDataView(movie_id, mVideoType.name(), ScraperSourceTools.getSource());
    }

    @Override
    protected List<MovieDataView> queryRecommendByGenres(String source, List<String> genreList, List<Long> idList) {
        return mMovieDao.queryRecommend(source, mVideoType.name(), Config.getSqlConditionOfChildMode(), genreList, idList, 0, LIMIT);
    }

    @Override
    protected List<MovieDataView> queryRecommend(String source) {
        return mMovieDao.queryRecommend(source, mVideoType.name(), Config.getSqlConditionOfChildMode(), 0, LIMIT);
    }

    public Constants.VideoType getVideoType() {
        return mVideoType;
    }

    public void setVideoType(Constants.VideoType videoType) {
        mVideoType = videoType;
    }
}
