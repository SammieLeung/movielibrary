package com.hphtv.movielibrary.scraper.api.mtime.request;

import com.hphtv.movielibrary.scraper.respone.MovieDetailRespone;
import com.hphtv.movielibrary.scraper.respone.MovieSearchRespone;
import com.hphtv.movielibrary.scraper.postbody.PostDetailRequetBody;
import com.hphtv.movielibrary.scraper.postbody.PostSearchRequetBody;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * author: Sam Leung
 * date:  21-5-13
 */
public interface MtimeApiRequest {

    @POST("movie/movie/search")
    Observable<MovieSearchRespone> createSearch(@Body PostSearchRequetBody body);

    @POST("movie/movie/suggest")
    Observable<MovieSearchRespone> createSuggest(@Body PostSearchRequetBody body);

    @POST("movie/movie/detail")
    Observable<MovieDetailRespone> createDetail(@Body PostDetailRequetBody body);

}
