package com.hphtv.movielibrary.viewmodel.fragment;

import android.app.Application;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.adapter.MovieSearchAdapter;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.roomdb.entity.Movie;
import com.hphtv.movielibrary.scraper.api.mtime.MtimeApiService;
import com.hphtv.movielibrary.scraper.api.omdb.OmdbApiService;
import com.hphtv.movielibrary.scraper.api.tmdb.TmdbApiService;
import com.hphtv.movielibrary.util.ScraperSourceTools;
import com.hphtv.movielibrary.util.retrofit.ResponeEntity;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;
import com.station.kit.util.ToastUtil;

import org.jetbrains.annotations.NotNull;

import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * author: Sam Leung
 * date:  2021/7/7
 */
public class MovieSearchFragmentViewModel extends AndroidViewModel {
    private int pageSize = 9;
    private int mCurrentPage;
    private String mSource;

    private String mCurrentKeyword;
    private LinkedList<Movie> mMovieLinkedList;

    public MovieSearchFragmentViewModel(@NonNull @NotNull Application application) {
        super(application);
        mSource = ScraperSourceTools.getSource();
        mMovieLinkedList = new LinkedList<>();
    }

    public void refresh(String keyword, MovieSearchAdapter adapter) {
        if (TextUtils.isEmpty(keyword))
            return;
        mCurrentKeyword = keyword.trim();
        mMovieLinkedList.clear();
        mCurrentPage = 1;
        notifyNewSearch(TmdbApiService.unionSearch(mCurrentKeyword, mCurrentPage, pageSize * 3, mSource), adapter);
//        mCurrentPage = 3;
    }

    public void loading(MovieSearchAdapter adapter) {
        if (TextUtils.isEmpty(mCurrentKeyword))
            return;
        notifyLoadingMore(TmdbApiService.unionSearch(mCurrentKeyword, mCurrentPage + 1, pageSize, mSource), adapter);
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
                            ToastUtil.newInstance(getApplication()).toast(getApplication().getString(R.string.toast_loadmore_end));
                        } else {
                            adapter.setMovies(movies);
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

    public String getCurrentKeyword() {
        return mCurrentKeyword;
    }

    public void setCurrentKeyword(String currentKeyword) {
        mCurrentKeyword = currentKeyword;
    }

}
