package com.hphtv.movielibrary.scraper.service;

import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.scraper.api.StationMovieProtocol;
import com.hphtv.movielibrary.scraper.postbody.PostDetailRequestBody;
import com.hphtv.movielibrary.scraper.postbody.PostSearchRequetBody;
import com.hphtv.movielibrary.scraper.respone.MovieDetailRespone;
import com.hphtv.movielibrary.scraper.respone.MovieSearchRespone;
import com.hphtv.movielibrary.util.retrofit.RetrofitTools;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * author: Sam Leung
 * date:  2021/10/20
 */
public class TmdbApiService {
    public static final String TAG = TmdbApiService.class.getSimpleName();
    public static Observable<MovieSearchRespone> unionSearch(String keyword, String api,String year) {
        return unionSearch(keyword, 1, api,year);
    }

    public static Observable<MovieSearchRespone> movieSearch(String keyword, String api,String year) {
        return movieSearch(keyword, 1, api,year);
    }

    public static Observable<MovieSearchRespone> tvSearch(String keyword, String api,String year) {
        return tvSearch(keyword, 1, api,year);
    }

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
        return unionSearch(keyword,pageIndex,api,null);
    }

    public static Observable<MovieSearchRespone> unionSearch(String keyword, int pageIndex, String api,String year) {
        return Observable.zip(unionSearch(keyword, pageIndex, api, Constants.SearchType.movie.name(), year).observeOn(Schedulers.newThread()),
                unionSearch(keyword, pageIndex, api, Constants.SearchType.tv.name(), year).observeOn(Schedulers.newThread()),
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

    public static Observable<MovieSearchRespone> unionSearch(String keyword, int pageIndex, String source, String type, String year) {
        keyword = keyword.trim();
        StationMovieProtocol request;
        switch (source) {
            case Constants.Scraper.TMDB_EN:
                request = RetrofitTools.createENRequest();
                break;
            default:
                request = RetrofitTools.createRequest();
                break;
        }
        PostSearchRequetBody body = new PostSearchRequetBody(keyword, pageIndex, type, year);
        Observable<MovieSearchRespone> searchObservable = request.createSearch(body);
        return searchObservable;
    }

    public static Observable<MovieDetailRespone> getDetail(String movieId, String source, String type) {
        StationMovieProtocol request;
        switch (source) {
            case Constants.Scraper.TMDB_EN:
                request = RetrofitTools.createENRequest();
                break;
            default:
                request = RetrofitTools.createRequest();
                break;
        }
        PostDetailRequestBody body = new PostDetailRequestBody(movieId,type);
        Observable<MovieDetailRespone> detailResponseObservable = request.createDetail(body);
        return detailResponseObservable;
    }

    public static Observable<MovieDetailRespone> getDetailFromApi(String movieId, String source, String type) {
        StationMovieProtocol request;
        switch (source) {
            case Constants.Scraper.TMDB_EN:
                request = RetrofitTools.createENRequest();
                break;
            default:
                request = RetrofitTools.createRequest();
                break;
        }
        PostDetailRequestBody body = new PostDetailRequestBody(movieId,type,"api");
        Observable<MovieDetailRespone> detailResponseObservable = request.createDetail(body);
        return detailResponseObservable;
    }

}
