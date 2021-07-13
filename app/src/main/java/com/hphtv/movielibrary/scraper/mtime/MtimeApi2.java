package com.hphtv.movielibrary.scraper.mtime;

import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.PagingSource;
import androidx.paging.rxjava3.PagingRx;

import com.hphtv.movielibrary.paging.MtimeUnionSearchResponePagingSource;
import com.hphtv.movielibrary.roomdb.entity.Movie;
import com.hphtv.movielibrary.scraper.mtime.request.MtimeAPIRequest;
import com.hphtv.movielibrary.scraper.mtime.respone.MtimeDetailRespone;
import com.hphtv.movielibrary.scraper.mtime.respone.MtimeSuggestRespone;
import com.hphtv.movielibrary.scraper.mtime.respone.MtimeUnionSearchRespone;
import com.hphtv.movielibrary.util.retrofit.RetrofiTools;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import kotlin.jvm.functions.Function0;

/**
 * Created by tchip on 18-11-9.
 */

public class MtimeApi2 {
    public static final String TAG = MtimeApi2.class.getSimpleName();
    public static int UNIONSEARCH_PAGE_SIZE=6;


    public static Observable<MtimeUnionSearchRespone> unionSearch(String keyword) {
        return unionSearch(keyword, 1);
    }

    public static Observable<MtimeUnionSearchRespone> unionSearch(String keyword, int pageIndex) {
        keyword = keyword.trim();
        MtimeAPIRequest request = RetrofiTools.createMtimeRequest();
        Observable<MtimeUnionSearchRespone> searchObservable = request.unionSearchRx(keyword, pageIndex);
        return searchObservable;
    }

    public static Single<MtimeUnionSearchRespone> singleUnionSearch(String keyword, int pageIndex, int pageSize) {
        keyword = keyword.trim();
        MtimeAPIRequest request = RetrofiTools.createMtimeRequest();
        Single<MtimeUnionSearchRespone> searchObservable = request.unionSearchSingleRx(keyword, pageIndex, pageSize);
        return searchObservable;
    }

    public static Flowable<PagingData<Movie>> unionSearhFlow(String keyword) {
        PagingConfig pagingConfig = new PagingConfig(UNIONSEARCH_PAGE_SIZE, UNIONSEARCH_PAGE_SIZE, false);
        //Pager 对象会调用 PagingSource 对象的 load() 方法，为其提供 LoadParams 对象，并接收 LoadResult 对象作为交换。
        Pager pager = new Pager(pagingConfig, (Function0<PagingSource>) () -> new MtimeUnionSearchResponePagingSource(keyword));
        return PagingRx.getFlowable(pager);
    }

    public static Observable<MtimeDetailRespone> getDetials(String movieId) {
        MtimeAPIRequest request = RetrofiTools.createMtimeRequest();
        Observable<MtimeDetailRespone> detailResponeObservable = request.movieDetailRx(movieId);
        return detailResponeObservable;
    }

    public static Observable<MtimeSuggestRespone> suggestMovies(String keyword) {
        keyword = keyword.trim();
        MtimeAPIRequest request = RetrofiTools.createMtimeRequest();
        Observable<MtimeSuggestRespone> suggestResponeObservable = request.suggestMovieRx(keyword);
        return suggestResponeObservable;
    }


}
