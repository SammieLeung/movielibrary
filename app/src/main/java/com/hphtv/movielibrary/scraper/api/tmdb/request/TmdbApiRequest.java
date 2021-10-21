package com.hphtv.movielibrary.scraper.api.tmdb.request;

import com.hphtv.movielibrary.scraper.postbody.PostDetailRequetBody;
import com.hphtv.movielibrary.scraper.postbody.PostSearchRequetBody;
import com.hphtv.movielibrary.scraper.respone.MovieDetailRespone;
import com.hphtv.movielibrary.scraper.respone.MovieSearchRespone;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * author: Sam Leung
 * date:  2021/9/8
 */
public interface TmdbApiRequest {
    @POST("movie/movie/search")
    Observable<MovieSearchRespone> createSearch(@Body PostSearchRequetBody body);

    @POST("movie/movie/detail")
    Observable<MovieDetailRespone> createDetail(@Body PostDetailRequetBody body);
}
