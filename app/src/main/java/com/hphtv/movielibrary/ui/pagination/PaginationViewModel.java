package com.hphtv.movielibrary.ui.pagination;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.hphtv.movielibrary.BaseAndroidViewModel;
import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.data.Config;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.MovieDao;
import com.hphtv.movielibrary.roomdb.dao.VideoTagDao;
import com.hphtv.movielibrary.roomdb.entity.VideoTag;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.hphtv.movielibrary.util.PaginatedDataLoader;
import com.hphtv.movielibrary.util.ScraperSourceTools;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * author: Sam Leung
 * date:  2022/4/22
 */
public class PaginationViewModel extends BaseAndroidViewModel {
    public static final int LIMIT = 10;
    public static final int FIRST_LIMIT = 15;
    public static final int OPEN_RECENTLY_ADD = 1;
    public static final int OPEN_FAVORITE = 2;
    private MovieDao mMovieDao;
    private VideoTagDao mVideoTagDao;
    private int mType;
    private VideoTag mVideoTag;
    private ObservableField<String> mTitle = new ObservableField<>();

    public PaginationViewModel(@NonNull @NotNull Application application) {
        super(application);
        MovieLibraryRoomDatabase movieLibraryRoomDatabase = MovieLibraryRoomDatabase.getDatabase(getApplication());
        mMovieDao = movieLibraryRoomDatabase.getMovieDao();
        mVideoTagDao = movieLibraryRoomDatabase.getVideoTagDao();
    }

    public void setType(int type) {
        mType = type;
        switch (mType) {
            case OPEN_RECENTLY_ADD:
                mTitle.set(getString(R.string.recently_added_list_title));
                break;
            case OPEN_FAVORITE:
                mTitle.set(getString(R.string.my_favorite_list_title));
                break;
        }
    }

    public int getType() {
        return mType;
    }

    public void setVideoTag(String videoTagString) {
        if (videoTagString != null) {
            Observable.create((ObservableOnSubscribe<VideoTag>) emitter -> {
                        mVideoTag = mVideoTagDao.queryVtidByNormalTag(videoTagString);
                        emitter.onNext(mVideoTag);
                        emitter.onComplete();
                    }).subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(videoTag -> reload());
        } else {
            mVideoTag = null;
            reload();
        }

    }

    public ObservableField<String> getTitle() {
        return mTitle;
    }

    public void reload() {
        switch (mType) {
            case OPEN_RECENTLY_ADD:
                reloadRecentlyAddedMovie();
                break;
            case OPEN_FAVORITE:
                reloadFavoriteMovie();
                break;
        }
    }

    public void loadMore() {
        switch (mType) {
            case OPEN_RECENTLY_ADD:
                loadRecentlyAddedMovie();
                break;
            case OPEN_FAVORITE:
                loadFavoriteMovie();
                break;
        }
    }

    private void reloadRecentlyAddedMovie() {
        mRecentlyAddedDataLoader.reload();
    }

    private void loadRecentlyAddedMovie() {
        mRecentlyAddedDataLoader.load();
    }

    private void reloadFavoriteMovie() {
        if (Config.isGetUserUpdate)
            mUserFavoriteDataLoader.reload();
        else
            mFavoriteDataLoader.reload();
    }

    private void loadFavoriteMovie() {
        if (Config.isGetUserUpdate && mUserFavoriteDataLoader.canLoadMore())
            mUserFavoriteDataLoader.load();
        else if (mFavoriteDataLoader.canLoadMore())
            mFavoriteDataLoader.load();
    }

    private OnRefresh mOnRefresh;

    public void setOnRefresh(OnRefresh onRefresh) {
        mOnRefresh = onRefresh;
    }

    public interface OnRefresh {
        void newSearch(List<MovieDataView> newMovieDataView);

        void appendMovieDataViews(List<MovieDataView> movieDataViews);
    }

    private PaginatedDataLoader<MovieDataView> mRecentlyAddedDataLoader = new PaginatedDataLoader<MovieDataView>() {
        @Override
        public int getLimit() {
            return LIMIT;
        }

        @Override
        public int getFirstLimit() {
            return FIRST_LIMIT;
        }

        @Override
        public List<MovieDataView> loadDataFromDB(int offset, int limit) {
            String tagName = mVideoTag != null ? mVideoTag.tag.name() : null;
            List<MovieDataView> movieDataViewList = mMovieDao.queryMovieDataViewForRecentlyAddedByVideoTag(ScraperSourceTools.getSource(), tagName, Config.getSqlConditionOfChildMode(), offset, limit);
            return movieDataViewList;
        }

        @Override
        public void OnReloadResult(List<MovieDataView> result) {
            if (mOnRefresh != null)
                mOnRefresh.newSearch(result);
        }

        @Override
        public void OnLoadResult(List<MovieDataView> result) {
            if (mOnRefresh != null)
                mOnRefresh.appendMovieDataViews(result);
        }
    };

    private PaginatedDataLoader<MovieDataView> mFavoriteDataLoader = new PaginatedDataLoader<MovieDataView>() {

        @Override
        public int getLimit() {
            return LIMIT;
        }

        @Override
        public void reload() {
            super.reload();
        }

        @Override
        protected List<MovieDataView> loadDataFromDB(int offset, int limit) {
            String tagName = mVideoTag != null ? mVideoTag.tag.name() : null;
            List<MovieDataView> movieDataViewList = mMovieDao.queryFavoriteMovieDataViewByVideoTag(ScraperSourceTools.getSource(), tagName, Config.getSqlConditionOfChildMode(), offset, limit);
            return movieDataViewList;
        }

        @Override
        protected void OnReloadResult(List<MovieDataView> result) {
            if (mOnRefresh != null)
                mOnRefresh.newSearch(result);
        }

        @Override
        protected void OnLoadResult(List<MovieDataView> result) {
            if (mOnRefresh != null)
                mOnRefresh.appendMovieDataViews(result);
        }


    };


    private PaginatedDataLoader<MovieDataView> mUserFavoriteDataLoader = new PaginatedDataLoader<MovieDataView>() {

        @Override
        public int getLimit() {
            return LIMIT;
        }

        @Override
        public void reload() {
            super.reload();
        }

        @Override
        protected List<MovieDataView> loadDataFromDB(int offset, int limit) {
            String tagName = mVideoTag != null ? mVideoTag.tag.name() : null;
            List<MovieDataView> movieDataViewList = mMovieDao.queryUserFavorite(ScraperSourceTools.getSource(), tagName, Config.getSqlConditionOfChildMode(), offset, limit);
            for (MovieDataView movieDataView : movieDataViewList) {
                if (mMovieDao.queryMovieDataViewByMovieId(movieDataView.movie_id, movieDataView.type.name(), movieDataView.source) == null)
                    movieDataView.is_user_fav = true;
            }
            return movieDataViewList;
        }

        @Override
        protected void OnReloadResult(List<MovieDataView> result) {
            if (mOnRefresh != null)
                mOnRefresh.newSearch(result);
        }


        @Override
        protected void OnLoadResult(List<MovieDataView> result) {
            if (mOnRefresh != null)
                mOnRefresh.appendMovieDataViews(result);
        }
    };
}
