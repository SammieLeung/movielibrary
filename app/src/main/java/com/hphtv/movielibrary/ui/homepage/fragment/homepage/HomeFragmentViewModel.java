package com.hphtv.movielibrary.ui.homepage.fragment.homepage;

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
 * date:  2021/6/1
 */
public class HomeFragmentViewModel extends BaseHomePageViewModel {

    public HomeFragmentViewModel(@NonNull @NotNull Application application) {
        super(application);

    }

    @Override
    protected List<HistoryMovieDataView> queryHistoryMovieDataView() {
        return mVideoFileDao.queryHistoryMovieDataView(ScraperSourceTools.getSource(), Config.getSqlConditionOfChildMode(), null, 0, LIMIT);
    }

    @Override
    protected List<String> queryGenresBySource() {
        return mGenreDao.queryGenresBySource(ScraperSourceTools.getSource(), null);
    }

    @Override
    protected List<MovieDataView> queryMovieDataViewForRecentlyAdded() {
        return mMovieDao.queryMovieDataViewForRecentlyAddedByVideoTag(ScraperSourceTools.getSource(), null, Config.getSqlConditionOfChildMode(), 0, LIMIT);
    }

    @Override
    protected List<MovieDataView> queryFavoriteMovieDataView() {
        return mMovieDao.queryFavoriteMovieDataViewByVideoTag(ScraperSourceTools.getSource(), null, Config.getSqlConditionOfChildMode(), 0, 6);
    }

    @Override
    protected List<MovieDataView> queryUserFavoriteDataView() {
        List<MovieDataView> movieDataViewList = mMovieDao.queryUserFavorite(ScraperSourceTools.getSource(), null,Config.getSqlConditionOfChildMode(), 0, 12);
        for (MovieDataView userFav : movieDataViewList) {
            userFav.is_user_fav = true;
        }
        return movieDataViewList;
    }

    @Override
    protected MovieDataView queryMovieDataViewByMovieId(String movie_id, String type) {
        return mMovieDao.queryMovieAsMovieDataView(movie_id, type, ScraperSourceTools.getSource());
    }

    @Override
    protected List<MovieDataView> queryRecommendByGenres(String source, List<String> genreList, List<Long> idList) {
        return mMovieDao.queryRecommend(source, null, Config.getSqlConditionOfChildMode(), genreList, idList, 0, LIMIT);
    }

    @Override
    protected List<MovieDataView> queryRecommend(String source) {
        return mMovieDao.queryRecommend(source, null, Config.getSqlConditionOfChildMode(), 0, LIMIT);
    }


}
