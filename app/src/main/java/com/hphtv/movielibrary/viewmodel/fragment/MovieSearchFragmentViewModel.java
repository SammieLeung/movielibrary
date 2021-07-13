package com.hphtv.movielibrary.viewmodel.fragment;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModelKt;
import androidx.paging.PagingData;
import androidx.paging.rxjava3.PagingRx;

import com.hphtv.movielibrary.roomdb.entity.Movie;
import com.hphtv.movielibrary.scraper.mtime.MtimeApi2;

import org.jetbrains.annotations.NotNull;

import io.reactivex.rxjava3.core.Flowable;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.flow.Flow;

/**
 * author: Sam Leung
 * date:  2021/7/7
 */
public class MovieSearchFragmentViewModel extends AndroidViewModel {
    private Flowable<PagingData<Movie>> mCurrentSearchResult;
    private String mCurrentKeyword;
    CoroutineScope mViewModelScope;

    public MovieSearchFragmentViewModel(@NonNull @NotNull Application application) {
        super(application);
        mViewModelScope = ViewModelKt.getViewModelScope(this);
    }

    public Flowable<PagingData<Movie>> unionSearchMovie(String keyword) {
        Flowable<PagingData<Movie>> lastResult = mCurrentSearchResult;
        if (keyword.equals(mCurrentKeyword) && lastResult != null) {
            return lastResult;
        }
        mCurrentKeyword = keyword;
        mCurrentSearchResult=MtimeApi2.unionSearhFlow(keyword);
        PagingRx.cachedIn(mCurrentSearchResult,mViewModelScope);
        return mCurrentSearchResult;
    }
}
