package com.hphtv.movielibrary.scraper.mtime;

import com.hphtv.movielibrary.scraper.mtime.request.MtimeAPIRequest;
import com.hphtv.movielibrary.scraper.mtime.respone.MtimeDetailRespone;
import com.hphtv.movielibrary.scraper.mtime.respone.MtimeSuggestRespone;
import com.hphtv.movielibrary.scraper.mtime.respone.MtimeUnionSearchRespone;
import com.hphtv.movielibrary.util.retrofit.RetrofiTools;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

/**
 * Created by tchip on 18-11-9.
 */

public class MtimeApi2 {
    public static final String TAG = MtimeApi2.class.getSimpleName();
    public static int UNIONSEARCH_PAGE_SIZE=20;


    public static Observable<MtimeUnionSearchRespone> unionSearch(String keyword) {
        return unionSearch(keyword, 1);
    }

     public static Observable<MtimeUnionSearchRespone> unionSearch(String keyword, int pageIndex){
        return unionSearch(keyword,pageIndex,UNIONSEARCH_PAGE_SIZE);
    }

    public static Observable<MtimeUnionSearchRespone> unionSearch(String keyword, int pageIndex,int pageSize) {
        keyword = keyword.trim();
        MtimeAPIRequest request = RetrofiTools.createMtimeRequest();
        Observable<MtimeUnionSearchRespone> searchObservable = request.unionSearchRx(keyword, pageIndex,pageSize);
        return searchObservable;
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
