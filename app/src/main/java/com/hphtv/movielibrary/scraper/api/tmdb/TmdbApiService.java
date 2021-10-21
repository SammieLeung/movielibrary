package com.hphtv.movielibrary.scraper.api.tmdb;

import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.scraper.api.omdb.OmdbApiService;
import com.hphtv.movielibrary.scraper.api.omdb.request.OmdbApiRequest;
import com.hphtv.movielibrary.scraper.api.tmdb.request.TmdbApiRequest;
import com.hphtv.movielibrary.scraper.postbody.PostDetailRequetBody;
import com.hphtv.movielibrary.scraper.postbody.PostSearchRequetBody;
import com.hphtv.movielibrary.scraper.respone.MovieDetailRespone;
import com.hphtv.movielibrary.scraper.respone.MovieSearchRespone;
import com.hphtv.movielibrary.util.retrofit.RetrofiTools;

import java.util.Locale;

import io.reactivex.rxjava3.core.Observable;

/**
 * author: Sam Leung
 * date:  2021/10/20
 */
public class TmdbApiService {
    public static final String TAG = TmdbApiService.class.getSimpleName();
    public static int UNIONSEARCH_PAGE_SIZE=20;

    public static Observable<MovieSearchRespone> unionSearch(String keyword,String type) {
        return unionSearch(keyword, 1,type);
    }

    public static Observable<MovieSearchRespone> unionSearch(String keyword, int pageIndex,String type){
        return unionSearch(keyword,pageIndex,UNIONSEARCH_PAGE_SIZE,type);
    }

    public static Observable<MovieSearchRespone> unionSearch(String keyword, int pageIndex,int pageSize,String type) {
        keyword = keyword.trim();
        TmdbApiRequest request=RetrofiTools.createTmdbApiRequest();
        switch (type){
            case Constants.Scraper.TMDB:
                request = RetrofiTools.createTmdbApiRequest();
                break;
            case Constants.Scraper.TMDB_EN:
                request = RetrofiTools.createTmdbApiRequest_EN();
                break;
        }
        PostSearchRequetBody body=new PostSearchRequetBody(keyword,pageIndex);
        Observable<MovieSearchRespone> searchObservable = request.createSearch(body);
        return searchObservable;
    }

    public static Observable<MovieDetailRespone> getDetials(String movieId,String type) {
        TmdbApiRequest request=RetrofiTools.createTmdbApiRequest();
        switch (type){
            case Constants.Scraper.TMDB:
                request = RetrofiTools.createTmdbApiRequest();
                break;
            case Constants.Scraper.TMDB_EN:
                request = RetrofiTools.createTmdbApiRequest_EN();
                break;
        }
        PostDetailRequetBody body=new PostDetailRequetBody(movieId);
        Observable<MovieDetailRespone> detailResponeObservable = request.createDetail(body);
        return detailResponeObservable;
    }


}
