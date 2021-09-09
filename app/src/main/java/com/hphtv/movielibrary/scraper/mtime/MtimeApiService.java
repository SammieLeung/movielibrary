package com.hphtv.movielibrary.scraper.mtime;

import com.hphtv.movielibrary.scraper.mtime.request.MtimeApiRequest;
import com.hphtv.movielibrary.scraper.postbody.PostDetailRequetBody;
import com.hphtv.movielibrary.scraper.postbody.PostSearchRequetBody;
import com.hphtv.movielibrary.scraper.respone.MovieDetailRespone;
import com.hphtv.movielibrary.scraper.respone.MovieSearchRespone;
import com.hphtv.movielibrary.util.retrofit.RetrofiTools;

import io.reactivex.rxjava3.core.Observable;

/**
 * Created by tchip on 18-11-9.
 */

public class MtimeApiService {
    public static final String TAG = MtimeApiService.class.getSimpleName();
    public static int UNIONSEARCH_PAGE_SIZE=20;

    public static Observable<MovieSearchRespone> unionSearch(String keyword) {
        return unionSearch(keyword, 1);
    }

     public static Observable<MovieSearchRespone> unionSearch(String keyword, int pageIndex){
        return unionSearch(keyword,pageIndex,UNIONSEARCH_PAGE_SIZE);
    }

    public static Observable<MovieSearchRespone> unionSearch(String keyword, int pageIndex,int pageSize) {
        keyword = keyword.trim();
        MtimeApiRequest request = RetrofiTools.createMtimeRequest();
        PostSearchRequetBody body=new PostSearchRequetBody(keyword,pageIndex);
        Observable<MovieSearchRespone> searchObservable = request.createSearch(body);
        return searchObservable;
    }

    public static Observable<MovieDetailRespone> getDetials(String movieId) {
        MtimeApiRequest request = RetrofiTools.createMtimeRequest();
        PostDetailRequetBody body=new PostDetailRequetBody(movieId);
        Observable<MovieDetailRespone> detailResponeObservable = request.createDetail(body);
        return detailResponeObservable;
    }

    public static Observable<MovieSearchRespone> suggestMovies(String keyword) {
        keyword = keyword.trim();
        MtimeApiRequest request = RetrofiTools.createMtimeRequest();
        PostSearchRequetBody body=new PostSearchRequetBody(keyword,1);
        Observable<MovieSearchRespone> suggestResponeObservable = request.createSuggest(body);
        return suggestResponeObservable;
    }


}
