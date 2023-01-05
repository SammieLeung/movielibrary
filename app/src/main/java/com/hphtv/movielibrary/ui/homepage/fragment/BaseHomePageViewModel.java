package com.hphtv.movielibrary.ui.homepage.fragment;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.hphtv.movielibrary.BaseAndroidViewModel;
import com.hphtv.movielibrary.MovieApplication;
import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.bean.PlayList;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.GenreDao;
import com.hphtv.movielibrary.roomdb.dao.MovieDao;
import com.hphtv.movielibrary.roomdb.dao.MovieUserFavoriteCrossRefDao;
import com.hphtv.movielibrary.roomdb.dao.VideoFileDao;
import com.hphtv.movielibrary.roomdb.entity.Genre;
import com.hphtv.movielibrary.roomdb.entity.Movie;
import com.hphtv.movielibrary.roomdb.entity.dataview.HistoryMovieDataView;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.hphtv.movielibrary.roomdb.entity.reference.MovieUserFavoriteCrossRef;
import com.hphtv.movielibrary.roomdb.entity.relation.MovieWrapper;
import com.hphtv.movielibrary.scraper.respone.GetUserFavoriteResponse;
import com.hphtv.movielibrary.scraper.service.OnlineDBApiService;
import com.hphtv.movielibrary.ui.AppBaseActivity;
import com.hphtv.movielibrary.ui.detail.MovieDetailActivity;
import com.hphtv.movielibrary.util.MovieHelper;
import com.hphtv.movielibrary.util.ScraperSourceTools;
import com.hphtv.movielibrary.util.rxjava.RxJavaGcManager;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;

import org.fourthline.cling.support.model.Res;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;
import kotlin.SuccessOrFailureKt;

/**
 * author: Sam Leung
 * date:  2022/10/19
 */
public abstract class BaseHomePageViewModel extends BaseAndroidViewModel {
    public static final String TAG = BaseAndroidViewModel.class.getSimpleName();
    public static final int LIMIT = 10;
    protected GenreDao mGenreDao;
    protected VideoFileDao mVideoFileDao;
    protected MovieDao mMovieDao;
    private List<HistoryMovieDataView> mRecentlyPlayedList = new ArrayList<>();
    private List<String> mGenreTagList = new ArrayList<>();
    private List<MovieDataView> mRecentlyAddedList = new ArrayList<>();
    private List<MovieDataView> mFavoriteList = new ArrayList<>();
    private List<MovieDataView> mRecommendList = new ArrayList<>();

    public BaseHomePageViewModel(@NonNull @NotNull Application application) {
        super(application);
        initDao();
    }

    protected abstract List<HistoryMovieDataView> queryHistoryMovieDataView();

    protected abstract List<String> queryGenresBySource();

    protected abstract List<MovieDataView> queryMovieDataViewForRecentlyAdded();

    protected abstract List<MovieDataView> queryFavoriteMovieDataView();

    protected abstract List<MovieDataView> queryUserFavoriteDataView();

    protected abstract MovieDataView queryMovieDataViewByMovieId(String movie_id, String type);

    protected abstract List<MovieDataView> queryRecommendByGenres(String source, List<String> genreList, List<Long> idList);

    protected abstract List<MovieDataView> queryRecommend(String source);

    protected String getUserFavoriteParamsFm(){
        return "";
    }
    /**
     * 初始化Dao类
     */
    private void initDao() {
        MovieLibraryRoomDatabase movieLibraryRoomDatabase = MovieLibraryRoomDatabase.getDatabase(getApplication());
        mGenreDao = movieLibraryRoomDatabase.getGenreDao();
        mVideoFileDao = movieLibraryRoomDatabase.getVideoFileDao();
        mMovieDao = movieLibraryRoomDatabase.getMovieDao();
    }

    public Observable<List<HistoryMovieDataView>> prepareHistory() {
        return Observable.just("").map(s -> {
            List<HistoryMovieDataView> movieDataViewList = queryHistoryMovieDataView();
            return movieDataViewList;
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());

    }

    public Observable<PlayList> playingVideo(String path, String name) {
        return MovieHelper.playingSeriesWithPlayList(path, name);
    }

    public void startDetailActivity(AppBaseActivity appBaseActivity, MovieDataView movieDataView) {
        Intent intent = new Intent(appBaseActivity, MovieDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putLong(Constants.Extras.MOVIE_ID, movieDataView.id);
        bundle.putInt(Constants.Extras.SEASON, movieDataView.season);
        intent.putExtras(bundle);
        appBaseActivity.startActivityForResult(intent);
    }

    public Observable<List<String>> prepareGenreList() {
        return Observable.just(3).subscribeOn(Schedulers.io()).map(defalut_count -> {
            //优先顺序 自定义>已有电影>固定排序
            List<String> newCustomTags = new ArrayList<>();
            List<String> customGenreTags = mGenreDao.queryGenreTagNameBySource(ScraperSourceTools.getSource());
            if (customGenreTags.size() == 0) {
                List<String> allMovieGenres = queryGenresBySource();
                //已有电影分类数量不足
                if (allMovieGenres.size() < defalut_count) {
                    List<String> genreArr = Arrays.asList(getApplication().getResources().getStringArray(R.array.genre_tags).clone());
                    newCustomTags.addAll(allMovieGenres);
                    for (int i = 0; newCustomTags.size() < defalut_count && i < genreArr.size(); i++) {
                        String newTag = genreArr.get(i);
                        //不添加重复的Tag
                        if (!newCustomTags.contains(newTag)) newCustomTags.add(genreArr.get(i));
                    }
                } else {
                    for (int i = 0; i < defalut_count; i++) {
                        String newTag = allMovieGenres.get(i);
                        if (!newCustomTags.contains(newTag)) newCustomTags.add(newTag);
                    }
                }
            } else {
                newCustomTags.addAll(customGenreTags);
            }
            return newCustomTags;
        }).observeOn(AndroidSchedulers.mainThread());

    }

    public Observable<List<MovieDataView>> prepareRecentlyAddedMovie() {
        return Observable.create((ObservableOnSubscribe<List<MovieDataView>>) emitter -> {
            List<MovieDataView> movieDataViewList = queryMovieDataViewForRecentlyAdded();
            emitter.onNext(movieDataViewList);
            emitter.onComplete();
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<List<MovieDataView>> prepareFavorite() {
        if (MovieApplication.getInstance().isDeviceBound()) {
            return prepareUserFavorites();
        } else {
            return prepareLocalFavorite();
        }
    }



    public void updateUserFavorites(String source, Disposable lastDisposable, OnUserFavorites disposableCallback) {
        RxJavaGcManager.getInstance().disposableActive(lastDisposable);
        Observable.zip(Observable.just(source), Observable.just(getUserFavoriteParamsFm()),Observable.just(0), Observable.just(6),
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
                            disposableCallback.onResultReturn(new Success(""));
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        if (disposableCallback != null)
                            disposableCallback.onResultReturn(new Error(e.getMessage()));
                    }
                });
    }


    public Observable<List<MovieDataView>> prepareLocalFavorite() {
        return Observable.create((ObservableOnSubscribe<List<MovieDataView>>) emitter -> {
            List<MovieDataView> movieDataViewList = queryFavoriteMovieDataView();
            emitter.onNext(movieDataViewList);
            emitter.onComplete();
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<List<MovieDataView>> prepareUserFavorites() {
        return Observable.create((ObservableOnSubscribe<List<MovieDataView>>) emitter -> {
            List<MovieDataView> movieDataViewList = queryUserFavoriteDataView();
            emitter.onNext(movieDataViewList);
            emitter.onComplete();
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread());
    }


    public Observable<List<MovieDataView>> prepareRecommend() {
        return Observable.create((ObservableOnSubscribe<List<MovieDataView>>) emitter -> {
            String source = ScraperSourceTools.getSource();
            if (mRecentlyPlayedList.size() > 0) {
                List<String> genreList = new ArrayList<>();
                List<Long> idList = new ArrayList<>();
                for (int i = 0; idList.size() < 3 && i < mRecentlyPlayedList.size(); i++) {
                    MovieWrapper wrapper = mMovieDao.queryMovieWrapperByFilePath(mRecentlyPlayedList.get(i).path, source);
                    if (wrapper != null) {
                        for (Genre genre : wrapper.genres) {
                            if (genre.source.equals(source) && !genreList.contains(genre.name)) {
                                genreList.add(genre.name);
                            }
                        }
                        idList.add(wrapper.movie.id);
                    }
                }
                emitter.onNext(queryRecommendByGenres(source, genreList, idList));
            } else {
                emitter.onNext(queryRecommend(source));
            }
            emitter.onComplete();
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());

    }

    public Observable<MovieDataView> getUpdatingFavorite(String movie_id, String type) {
        return Observable.create((ObservableOnSubscribe<MovieDataView>) emitter -> {
            MovieDataView movieDataView = queryMovieDataViewByMovieId(movie_id, type);
            emitter.onNext(movieDataView);
            emitter.onComplete();
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public List<HistoryMovieDataView> getRecentlyPlayedList() {
        return mRecentlyPlayedList;
    }

    public List<String> getGenreTagList() {
        return mGenreTagList;
    }

    public List<MovieDataView> getRecentlyAddedList() {
        return mRecentlyAddedList;
    }

    public List<MovieDataView> getFavoriteList() {
        return mFavoriteList;
    }

    public List<MovieDataView> getRecommendList() {
        return mRecommendList;
    }

    public interface OnUserFavorites<T> {
        void onDisposableReturn(Disposable disposable);

        void onResultReturn(Result<T> result);
    }

    public interface Result<T> {
        public T result();
    }

    public static class Success<T> implements Result<T> {
        private T result;

        public Success(T result) {
            this.result = result;
        }

        @Override
        public T result() {
            return result;
        }
    }

    public static class Error implements Result {
        private String errMsg;

        public Error(String msg) {
            this.errMsg = msg;
        }

        public String errorMessage() {
            return errMsg;
        }

        @Override
        public Object result() {
            return null;
        }
    }
}
