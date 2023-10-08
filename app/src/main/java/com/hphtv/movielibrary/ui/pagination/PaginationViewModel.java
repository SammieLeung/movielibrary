package com.hphtv.movielibrary.ui.pagination;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.hphtv.movielibrary.BaseAndroidViewModel;
import com.hphtv.movielibrary.MovieApplication;
import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.data.Config;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.MovieDao;
import com.hphtv.movielibrary.roomdb.dao.MovieUserFavoriteCrossRefDao;
import com.hphtv.movielibrary.roomdb.dao.VideoTagDao;
import com.hphtv.movielibrary.roomdb.entity.Movie;
import com.hphtv.movielibrary.roomdb.entity.VideoTag;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.hphtv.movielibrary.roomdb.entity.reference.MovieUserFavoriteCrossRef;
import com.hphtv.movielibrary.roomdb.entity.relation.MovieWrapper;
import com.hphtv.movielibrary.scraper.respone.GetUserFavoriteResponse;
import com.hphtv.movielibrary.scraper.service.OnlineDBApiService;
import com.hphtv.movielibrary.ui.homepage.fragment.BaseHomePageViewModel;
import com.hphtv.movielibrary.util.MovieHelper;
import com.hphtv.movielibrary.data.pagination.PaginatedDataLoader;
import com.hphtv.movielibrary.util.ScraperSourceTools;
import com.hphtv.movielibrary.util.rxjava.RxJavaGcManager;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * author: Sam Leung
 * date:  2022/4/22
 */
public class PaginationViewModel extends BaseAndroidViewModel {
    public static final String TAG = PaginationViewModel.class.getSimpleName();
    public static final int LIMIT = 10;
    public static final int FIRST_LIMIT = 15;
    public static final int OPEN_RECENTLY_ADD = 1;
    public static final int OPEN_FAVORITE = 2;
    private MovieDao mMovieDao;
    private VideoTagDao mVideoTagDao;
    private int mType;
    private VideoTag mVideoTag;
    private ObservableField<String> mTitle = new ObservableField<>();
    private PaginatedDataLoader<MovieDataView> mLastLoader;

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
        mRecentlyAddedDataLoader.loadNext();
    }

    private void reloadFavoriteMovie() {
        if (MovieApplication.getInstance().isDeviceBound() && MovieApplication.hasNetworkConnection) {
            mNetworkUserFavoriteDataLoader.reload();
            mLastLoader = mNetworkUserFavoriteDataLoader;
        } else if (MovieApplication.getInstance().isDeviceBound() && !MovieApplication.hasNetworkConnection) {
            mUserFavoriteDataLoader.reload();
            mLastLoader = mUserFavoriteDataLoader;
        } else {
            mFavoriteDataLoader.reload();
            mLastLoader = mFavoriteDataLoader;
        }
    }

    private void loadFavoriteMovie() {
        if (mLastLoader != null && mLastLoader.canLoadNext())
            mLastLoader.loadNext();
    }

    public void cancel() {
        if (mLastLoader != null)
            mLastLoader.cancel();
    }

    private OnRefresh mOnRefresh;

    public void setOnRefresh(OnRefresh onRefresh) {
        mOnRefresh = onRefresh;
    }

    public interface OnRefresh {
        void beforeLoad();

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
        public void OnLoadNextResult(List<MovieDataView> result) {
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
        protected void OnLoadNextResult(List<MovieDataView> result) {
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
        protected void OnLoadNextResult(List<MovieDataView> result) {
            if (mOnRefresh != null)
                mOnRefresh.appendMovieDataViews(result);
        }
    };

    private PaginatedDataLoader<MovieDataView> mNetworkUserFavoriteDataLoader = new PaginatedDataLoader<MovieDataView>() {

        private boolean isReload = false;

        @Override
        public int getLimit() {
            return LIMIT;
        }

        @Override
        public void reload() {
            if(mOnRefresh!=null)
                mOnRefresh.beforeLoad();
            super.reload();
            isReload = true;

        }

        @Override
        protected List<MovieDataView> loadDataFromDB(int offset, int limit) {
            String tagName = mVideoTag != null ? mVideoTag.tag.name() : null;
            int page = 1 + offset / limit;
            GetUserFavoriteResponse response = OnlineDBApiService.getUserFavorites(ScraperSourceTools.getSource(), tagName, page, limit).blockingFirst();
            try {
                if (response != null) {
                    MovieUserFavoriteCrossRefDao movieUserFavoriteCrossRefDao = MovieLibraryRoomDatabase.getDatabase(getApplication()).getMovieUserFavoriteCrossRefDao();
                    MovieDao movieDao = MovieLibraryRoomDatabase.getDatabase(getApplication()).getMovieDao();
                    List<MovieWrapper> movieList = response.toEntity();
                    int count = movieList.size();
                    if (isReload) {
                        movieUserFavoriteCrossRefDao.deleteAll();
                        isReload = false;
                    }
                    if (count > 0) {

                        for (MovieWrapper movieWrapper : movieList) {
                            Movie movie = movieWrapper.movie;
                            if (movie != null) {
                                Movie dbMovie = movieDao.queryByMovieIdAndType(movie.movieId, movie.source, movie.type.name());
                                if (dbMovie == null) {
                                    movieWrapper.movie.isFavorite = true;
                                    MovieHelper.saveBaseInfo(getApplication(), movieWrapper);
                                } else {
                                    dbMovie.isFavorite = true;
                                    movieDao.update(dbMovie);
                                }
                                MovieUserFavoriteCrossRef movieUserFavoriteCrossRef = movieUserFavoriteCrossRefDao.query(movie.movieId, movie.type.name(), movie.source);
                                if (movieUserFavoriteCrossRef == null) {
                                    movieUserFavoriteCrossRef = new MovieUserFavoriteCrossRef();
                                    movieUserFavoriteCrossRef.movie_id = movie.movieId;
                                    movieUserFavoriteCrossRef.source = movie.source;
                                    movieUserFavoriteCrossRef.type = movie.type;
                                    movieUserFavoriteCrossRef.update_time = System.currentTimeMillis();
                                    movieUserFavoriteCrossRefDao.insertOrIgnore(movieUserFavoriteCrossRef);
                                }
                            }
                        }
                    }
                    List<MovieDataView> movieDataViewList = mMovieDao.queryUserFavorite(ScraperSourceTools.getSource(), tagName, Config.getSqlConditionOfChildMode(), offset, limit);
                    for (MovieDataView movieDataView : movieDataViewList) {
                        if (mMovieDao.queryMovieDataViewByMovieId(movieDataView.movie_id, movieDataView.type.name(), movieDataView.source) == null)
                            movieDataView.is_user_fav = true;
                    }
                    return movieDataViewList;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new ArrayList<>();
        }

        @Override
        protected void OnReloadResult(List<MovieDataView> result) {
            if (mOnRefresh != null)
                mOnRefresh.newSearch(result);
        }


        @Override
        protected void OnLoadNextResult(List<MovieDataView> result) {
            if (mOnRefresh != null)
                mOnRefresh.appendMovieDataViews(result);
        }
    };

    public void updateUserFavorites(String source, Disposable lastDisposable, BaseHomePageViewModel.OnUserFavorites disposableCallback) {
        RxJavaGcManager.getInstance().disposableActive(lastDisposable);
        Observable.zip(Observable.just(source), Observable.just(""), Observable.just(0), Observable.just(6),
                        OnlineDBApiService::getUserFavorites)
                .subscribeOn(Schedulers.newThread())
                .flatMap((Function<Observable<GetUserFavoriteResponse>, ObservableSource<GetUserFavoriteResponse>>) favoriteResponseObservable -> favoriteResponseObservable)
                .onErrorReturn(throwable -> {
                    Log.e(TAG, "updateUserFavorites: " + throwable.getMessage());
                    return new GetUserFavoriteResponse();
                })
                .subscribe(new SimpleObserver<GetUserFavoriteResponse>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        super.onSubscribe(d);
                        if (disposableCallback != null)
                            disposableCallback.onDisposableReturn(d);
                    }

                    @Override
                    public void onAction(GetUserFavoriteResponse getUserFavoriteResponse) {
                        MovieUserFavoriteCrossRefDao movieUserFavoriteCrossRefDao = MovieLibraryRoomDatabase.getDatabase(getApplication()).getMovieUserFavoriteCrossRefDao();
                        MovieDao movieDao = MovieLibraryRoomDatabase.getDatabase(getApplication()).getMovieDao();
                        List<MovieWrapper> movieList = getUserFavoriteResponse.toEntity();
                        int count = movieList.size();
                        if (count > 0) {
                            for (MovieWrapper movieWrapper : movieList) {
                                Movie movie = movieWrapper.movie;
                                if (movie != null) {
                                    Movie dbMovie = movieDao.queryByMovieIdAndType(movie.movieId, movie.source, movie.type.name());
                                    if (dbMovie == null) {
                                        movieWrapper.movie.isFavorite = true;
                                        MovieHelper.saveBaseInfo(getApplication(), movieWrapper);
                                    } else {
                                        dbMovie.isFavorite = true;
                                        movieDao.update(dbMovie);
                                    }
                                    MovieUserFavoriteCrossRef movieUserFavoriteCrossRef = movieUserFavoriteCrossRefDao.query(movie.movieId, movie.type.name(), movie.source);
                                    if (movieUserFavoriteCrossRef == null) {
                                        movieUserFavoriteCrossRef = new MovieUserFavoriteCrossRef();
                                        movieUserFavoriteCrossRef.movie_id = movie.movieId;
                                        movieUserFavoriteCrossRef.source = movie.source;
                                        movieUserFavoriteCrossRef.type = movie.type;
                                        movieUserFavoriteCrossRefDao.insertOrIgnore(movieUserFavoriteCrossRef);
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                        if (disposableCallback != null)
                            disposableCallback.onResultReturn(new BaseHomePageViewModel.Success(""));
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        if (disposableCallback != null)
                            disposableCallback.onResultReturn(new BaseHomePageViewModel.Error(e.getMessage()));
                    }
                });
    }


}
