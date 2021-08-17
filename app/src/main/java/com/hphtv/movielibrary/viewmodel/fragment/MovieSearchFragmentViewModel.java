package com.hphtv.movielibrary.viewmodel.fragment;

import android.app.Application;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.adapter.MovieSearchAdapter;
import com.hphtv.movielibrary.data.ConstData;
import com.hphtv.movielibrary.roomdb.entity.Movie;
import com.hphtv.movielibrary.scraper.mtime.MtimeApi;
import com.hphtv.movielibrary.scraper.mtime.MtimeApi2;
import com.hphtv.movielibrary.util.retrofit.ResponeEntity;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;
import com.station.kit.util.ToastUtil;

import org.jetbrains.annotations.NotNull;

import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * author: Sam Leung
 * date:  2021/7/7
 */
public class MovieSearchFragmentViewModel extends AndroidViewModel {
    private int pageSize = 9;
    private int mCurrentPage;
    private int mApi;

    private String mCurrentKeyword;
    private LinkedList<Movie> mMovieLinkedList;

    public MovieSearchFragmentViewModel(@NonNull @NotNull Application application) {
        super(application);
        mApi=ConstData.Scraper.MTIME;
        mMovieLinkedList=new LinkedList<>();
    }

    public void refresh(String keyword, MovieSearchAdapter adapter) {
        if (TextUtils.isEmpty(keyword))
            return;
        mCurrentKeyword = keyword.trim();
        mMovieLinkedList.clear();
        mCurrentPage=1;
        switch (mApi) {
            case ConstData.Scraper.MTIME:
                notifyNewSearch(MtimeApi2.unionSearch(mCurrentKeyword, mCurrentPage, pageSize * 3), adapter);
                break;
        }
        mCurrentPage=3;
    }

    public void loading(MovieSearchAdapter adapter) {
        if(TextUtils.isEmpty(mCurrentKeyword))
            return;
        switch (mApi) {
            case ConstData.Scraper.MTIME:
                notifyLoadingMore(MtimeApi2.unionSearch(mCurrentKeyword, mCurrentPage+1, pageSize ), adapter);
                break;
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
                            ToastUtil.newInstance(getApplication()).toast(getApplication().getString(R.string.toast_loadmore_end));
                        } else {
                            adapter.setMovies(movies);
                        }
                        adapter.cancelLoading();
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        if(e instanceof UnknownHostException) {
                            String msg=getApplication().getString(R.string.toast_UnknownHostException);
                            ToastUtil.newInstance(getApplication()).toast(msg);
                            adapter.cancelLoadingAndShowTips(msg);
                        }else{
                            ToastUtil.newInstance(getApplication()).toast(e.getMessage());
                            adapter.cancelLoading();
                        }

                    }
                });
    }

    private void notifyLoadingMore(Observable<? extends ResponeEntity<List<Movie>>> observable, MovieSearchAdapter adapter){
        observable
                .doOnSubscribe(entity -> adapter.loading())
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .map(responeEntity -> responeEntity.toEntity())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(movies -> {
                    if(movies==null||movies.size()==0) {
                        ToastUtil.newInstance(getApplication()).toast(getApplication().getString(R.string.toast_loadmore_end));
                    }
                    else {
                        mCurrentPage++;
                        adapter.addMovies(movies);
                    }
                    adapter.cancelLoading();
                });
    }

    public String getCurrentKeyword() {
        return mCurrentKeyword;
    }

    public void setCurrentKeyword(String currentKeyword) {
        mCurrentKeyword = currentKeyword;
    }

}
