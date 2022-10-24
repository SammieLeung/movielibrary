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

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.schedulers.Schedulers;

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
        return mVideoFileDao.queryHistoryMovieDataView(ScraperSourceTools.getSource(), Config.getSqlConditionOfChildMode(),null, 0, LIMIT);
    }

    @Override
    protected List<String> queryGenresBySource() {
        return  mGenreDao.queryGenresBySource(ScraperSourceTools.getSource(),null);
    }

    @Override
    protected List<MovieDataView> queryMovieDataViewForRecentlyAdded() {
        return mMovieDao.queryMovieDataViewForRecentlyAdded(ScraperSourceTools.getSource(),null, Config.getSqlConditionOfChildMode(), 0, LIMIT);
    }

    @Override
    protected List<MovieDataView> queryFavoriteMovieDataView() {
        return mMovieDao.queryFavoriteMovieDataView(ScraperSourceTools.getSource(), null,Config.getSqlConditionOfChildMode(), 0, LIMIT);
    }

    @Override
    protected MovieDataView queryMovieDataViewByMovieId(String movie_id,String type) {
        return  mMovieDao.queryMovieDataViewByMovieId(movie_id, type, ScraperSourceTools.getSource());
    }

    @Override
    protected List<MovieDataView> queryRecommendByGenres(String source,List<String> genreList, List<Long> idList) {
        return mMovieDao.queryRecommend(source, null,Config.getSqlConditionOfChildMode(), genreList, idList, 0, LIMIT);
    }

    @Override
    protected List<MovieDataView> queryRecommend(String source) {
        return mMovieDao.queryRecommend(source, null,Config.getSqlConditionOfChildMode(), 0, LIMIT);
    }


}
