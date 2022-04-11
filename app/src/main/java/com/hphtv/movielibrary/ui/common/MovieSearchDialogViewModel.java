package com.hphtv.movielibrary.ui.common;

import android.app.Application;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.roomdb.entity.Movie;
import com.hphtv.movielibrary.roomdb.entity.relation.MovieWrapper;
import com.hphtv.movielibrary.scraper.service.TmdbApiService;
import com.hphtv.movielibrary.util.ScraperSourceTools;
import com.hphtv.movielibrary.util.retrofit.ResponeEntity;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;
import com.station.kit.util.ToastUtil;

import org.jetbrains.annotations.NotNull;

import java.net.UnknownHostException;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * author: Sam Leung
 * date:  2021/7/7
 */
public class MovieSearchDialogViewModel extends AndroidViewModel {
    private int pageSize = 9;
    private int mCurrentPage;
    private String mSource;
    private int mSearchMode;

    private String mCurrentKeyword;

    public MovieSearchDialogViewModel(@NonNull @NotNull Application application) {
        super(application);
        mSource = ScraperSourceTools.getSource();
    }

    public void refresh(String keyword, MovieSearchAdapter adapter) {
        if (TextUtils.isEmpty(keyword))
            return;
        mCurrentKeyword = keyword.trim();
        adapter.clearAll();
        mCurrentPage = 1;
        notifyNewSearch(autoSearch(mCurrentPage), adapter);
    }

    public void loading(MovieSearchAdapter adapter) {
        if (TextUtils.isEmpty(mCurrentKeyword))
            return;
        notifyLoadingMore(autoSearch(mCurrentPage+1), adapter);
    }

    /**
     * 根据对应模式调用相应接口搜索
     * 电影模式
     * 电视模式
     * 混合模式（电影+电视）
     * @param page
     * @return
     */
    public Observable<? extends ResponeEntity<List<Movie>>> autoSearch(int page) {
        switch (mSearchMode) {
            case 1:
                return TmdbApiService.movieSearch(mCurrentKeyword, page, mSource);
            case 2:
                return TmdbApiService.tvSearch(mCurrentKeyword, page, mSource);
            default:
                return TmdbApiService.unionSearch(mCurrentKeyword, page, mSource);
        }
    }

    private void notifyNewSearch(Observable<? extends ResponeEntity<List<Movie>>> observable, MovieSearchAdapter adapter) {
        observable
                .doOnSubscribe(entity -> adapter.loading())
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .map(responeEntity -> responeEntity.toEntity())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<Movie>>() {
                    @Override
                    public void onAction(List<Movie> movies) {
                        if (movies == null || movies.size() == 0) {
                            adapter.cancelLoadingAndShowTips(getApplication().getString(R.string.toast_newsearh_notfound));
                        } else {
                            adapter.setMovies(movies);
                            adapter.cancelLoading();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        String msg;
                        if (e instanceof UnknownHostException) {
                            msg = getApplication().getString(R.string.toast_UnknownHostException);
                        } else {
                            msg = getApplication().getString(R.string.toast_selectmovie_faild);
                            e.printStackTrace();
                        }
                        adapter.cancelLoadingAndShowTips(msg);

                    }
                });
    }

    private void notifyLoadingMore(Observable<? extends ResponeEntity<List<Movie>>> observable, MovieSearchAdapter adapter) {
        observable
                .doOnSubscribe(entity -> adapter.loading())
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .map(responeEntity -> responeEntity.toEntity())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<Movie>>() {
                    @Override
                    public void onAction(List<Movie> movies) {
                        if (movies == null || movies.size() == 0) {
                            ToastUtil.newInstance(getApplication()).toast(getApplication().getString(R.string.toast_loadmore_end));
                        } else {
                            mCurrentPage++;
                            adapter.addMovies(movies);
                        }
                        adapter.cancelLoading();
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        String msg;
                        if (e instanceof UnknownHostException) {
                            msg = getApplication().getString(R.string.toast_UnknownHostException);
                        } else {
                            msg = e.getMessage();
                        }
                        ToastUtil.newInstance(getApplication()).toast(msg);
                        adapter.cancelLoadingAndShowTips(msg);
                    }
                });
    }

    public Observable<MovieWrapper> selectMovie(final String movie_id, final String source, final Constants.SearchType type) {
        return Observable.create((ObservableOnSubscribe<MovieWrapper>) emitter -> {
            MovieWrapper wrapper = TmdbApiService.getDetail(movie_id, source, type.name())
                    .blockingFirst().toEntity();
            emitter.onNext(wrapper);
            emitter.onComplete();
        }).subscribeOn(Schedulers.newThread());
    }

    public String getCurrentKeyword() {
        return mCurrentKeyword;
    }

    public void setCurrentKeyword(String currentKeyword) {
        mCurrentKeyword = currentKeyword;
    }

    public void setSearchMode(int searchMode,MovieSearchAdapter adapter) {
        mSearchMode=searchMode;
        refresh(mCurrentKeyword,adapter);
    }
}
