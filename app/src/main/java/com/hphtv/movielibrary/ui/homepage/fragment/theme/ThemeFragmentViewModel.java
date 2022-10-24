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

import io.reactivex.rxjava3.core.Observable;

/**
 * author: Sam Leung
 * date:  2022/6/7
 */
public class ThemeFragmentViewModel extends BaseHomePageViewModel {
    private Constants.SearchType mSearchType;

    public ThemeFragmentViewModel(@NonNull @NotNull Application application) {
        super(application);

    }

    @Override
    protected List<HistoryMovieDataView> queryHistoryMovieDataView() {
        return mVideoFileDao.queryHistoryMovieDataView(ScraperSourceTools.getSource(),  Config.getSqlConditionOfChildMode(),mSearchType, 0, LIMIT);
    }

    @Override
    protected List<String> queryGenresBySource() {
        return mGenreDao.queryGenresBySource(ScraperSourceTools.getSource(), mSearchType);
    }

    @Override
    protected List<MovieDataView> queryMovieDataViewForRecentlyAdded() {
        return mMovieDao.queryMovieDataViewForRecentlyAdded(ScraperSourceTools.getSource(), mSearchType, Config.getSqlConditionOfChildMode(), 0, LIMIT);
    }

    @Override
    protected List<MovieDataView> queryFavoriteMovieDataView() {
        return  mMovieDao.queryFavoriteMovieDataView(ScraperSourceTools.getSource(), mSearchType, Config.getSqlConditionOfChildMode(), 0, LIMIT);
    }

    @Override
    protected MovieDataView queryMovieDataViewByMovieId(String movie_id, String type) {
        return mMovieDao.queryMovieDataViewByMovieId(movie_id, mSearchType.name(), ScraperSourceTools.getSource());
    }

    @Override
    protected List<MovieDataView> queryRecommendByGenres(String source, List<String> genreList, List<Long> idList) {
        return mMovieDao.queryRecommend(source, mSearchType, Config.getSqlConditionOfChildMode(), genreList, idList, 0, LIMIT);
    }

    @Override
    protected List<MovieDataView> queryRecommend(String source) {
        return mMovieDao.queryRecommend(source, mSearchType, Config.getSqlConditionOfChildMode(), 0, LIMIT);
    }

    public Constants.SearchType getSearchType() {
        return mSearchType;
    }

    public void setSearchType(Constants.SearchType searchType) {
        mSearchType = searchType;
    }
}
