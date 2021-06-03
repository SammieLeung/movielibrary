package com.hphtv.movielibrary.scraper.mtime;

import com.hphtv.movielibrary.util.retrofit.MtimeAPIRequest;
import com.hphtv.movielibrary.util.retrofit.MtimeDetailRespone;
import com.hphtv.movielibrary.util.retrofit.MtimeSearchRespone;
import com.hphtv.movielibrary.util.retrofit.RetrofiTools;

import io.reactivex.rxjava3.core.Observable;

/**
 * Created by tchip on 18-11-9.
 */

public class MtimeApi2 {
    public static final String TAG = MtimeApi2.class.getSimpleName();


    public static Observable<MtimeSearchRespone> SearchAMovieByApi(String keyword) {
        keyword = keyword.trim();
        MtimeAPIRequest request = RetrofiTools.createMtimeRequest();
        Observable<MtimeSearchRespone> searchObservable = request.searchMovieByMtimeRx(keyword, 1);
        return searchObservable;
    }

    public static Observable<MtimeDetailRespone> getMovieDetail(String movieId) {
        MtimeAPIRequest request = RetrofiTools.createMtimeRequest();
        Observable<MtimeDetailRespone> detailResponeObservable = request.getMovieDetailByMtimeRx(movieId);
        return detailResponeObservable;
    }


}
