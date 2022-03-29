package com.hphtv.movielibrary.scraper.api.tmdb;

import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.scraper.api.tmdb.request.TmdbApiRequest;
import com.hphtv.movielibrary.scraper.postbody.PostDetailRequetBody;
import com.hphtv.movielibrary.scraper.postbody.PostSearchRequetBody;
import com.hphtv.movielibrary.scraper.respone.MovieDetailRespone;
import com.hphtv.movielibrary.scraper.respone.MovieSearchRespone;
import com.hphtv.movielibrary.util.retrofit.RetrofiTools;

import java.util.List;
import java.util.Locale;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.BiFunction;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * author: Sam Leung
 * date:  2021/10/20
 */
public class TmdbApiService {
    public static final String TAG = TmdbApiService.class.getSimpleName();

    public static Observable<MovieSearchRespone> unionSearch(String keyword, String api) {
        return unionSearch(keyword, 1, api);
    }

    public static Observable<MovieSearchRespone> movieSearch(String keyword, String api) {
        return movieSearch(keyword, 1, api);
    }

    public static Observable<MovieSearchRespone> tvSearch(String keyword, String api) {
        return tvSearch(keyword, 1, api);
    }

    public static Observable<MovieSearchRespone> unionSearch(String keyword, int pageIndex, String api) {
        return Observable.zip(unionSearch(keyword, pageIndex, api, Constants.SearchType.movie.name(), null).observeOn(Schedulers.newThread()),
                unionSearch(keyword, pageIndex, api, Constants.SearchType.tv.name(), null).observeOn(Schedulers.newThread()),
                MovieSearchRespone::combine);
    }


    public static Observable<MovieSearchRespone> movieSearch(String keyword, int pageIndex, String api) {
        return movieSearch(keyword, pageIndex, api, null);
    }

    public static Observable<MovieSearchRespone> tvSearch(String keyword, int pageIndex, String api) {
        return tvSearch(keyword, pageIndex, api, null);
    }

    public static Observable<MovieSearchRespone> movieSearch(String keyword, int pageIndex, String api, String year) {
        return unionSearch(keyword, pageIndex, api, Constants.SearchType.movie.name(), year);
    }

    public static Observable<MovieSearchRespone> tvSearch(String keyword, int pageIndex, String api,String year) {
        return unionSearch(keyword, pageIndex, api, Constants.SearchType.tv.name(), year);
    }

    public static Observable<MovieSearchRespone> unionSearch(String keyword, int pageIndex, String api, String type, String year) {
        keyword = keyword.trim();
        TmdbApiRequest request = RetrofiTools.createTmdbApiRequest();
        switch (api) {
            case Constants.Scraper.TMDB:
                request = RetrofiTools.createTmdbApiRequest();
                break;
            case Constants.Scraper.TMDB_EN:
                request = RetrofiTools.createTmdbApiRequest_EN();
                break;
        }
        PostSearchRequetBody body = new PostSearchRequetBody(keyword, pageIndex, type, year);
        Observable<MovieSearchRespone> searchObservable = request.createSearch(body);
        return searchObservable;
    }

    public static Observable<MovieDetailRespone> getDetail(String movieId, String api, String type) {
        TmdbApiRequest request = RetrofiTools.createTmdbApiRequest();
        switch (api) {
            case Constants.Scraper.TMDB:
                request = RetrofiTools.createTmdbApiRequest();
                break;
            case Constants.Scraper.TMDB_EN:
                request = RetrofiTools.createTmdbApiRequest_EN();
                break;
        }
        PostDetailRequetBody body = new PostDetailRequetBody(movieId,type);
        Observable<MovieDetailRespone> detailResponeObservable = request.createDetail(body);
        return detailResponeObservable;
    }


}
