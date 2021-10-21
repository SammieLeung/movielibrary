package com.hphtv.movielibrary.scraper.api.omdb;

import com.hphtv.movielibrary.scraper.api.omdb.request.OmdbApiRequest;
import com.hphtv.movielibrary.scraper.postbody.PostDetailRequetBody;
import com.hphtv.movielibrary.scraper.postbody.PostSearchRequetBody;
import com.hphtv.movielibrary.scraper.respone.MovieDetailRespone;
import com.hphtv.movielibrary.scraper.respone.MovieSearchRespone;
import com.hphtv.movielibrary.util.retrofit.RetrofiTools;

import io.reactivex.rxjava3.core.Observable;

/**
 * Created by tchip on 18-11-9.
 */

public class OmdbApiService {
    public static final String TAG = OmdbApiService.class.getSimpleName();
    public static int UNIONSEARCH_PAGE_SIZE=20;

    public static Observable<MovieSearchRespone> unionSearch(String keyword) {
        return unionSearch(keyword, 1);
    }

     public static Observable<MovieSearchRespone> unionSearch(String keyword, int pageIndex){
        return unionSearch(keyword,pageIndex,UNIONSEARCH_PAGE_SIZE);
    }

    public static Observable<MovieSearchRespone> unionSearch(String keyword, int pageIndex,int pageSize) {
        keyword = keyword.trim();
        OmdbApiRequest request = RetrofiTools.createOmdbApiRequest();
        PostSearchRequetBody body=new PostSearchRequetBody(keyword,pageIndex);
        Observable<MovieSearchRespone> searchObservable = request.createSearch(body);
        return searchObservable;
    }

    public static Observable<MovieDetailRespone> getDetials(String movieId) {
        OmdbApiRequest request = RetrofiTools.createOmdbApiRequest();
        PostDetailRequetBody body=new PostDetailRequetBody(movieId);
        Observable<MovieDetailRespone> detailResponeObservable = request.createDetail(body);
        return detailResponeObservable;
    }

}
