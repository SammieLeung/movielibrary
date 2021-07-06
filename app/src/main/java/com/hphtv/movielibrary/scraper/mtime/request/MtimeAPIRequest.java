package com.hphtv.movielibrary.scraper.mtime;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * author: Sam Leung
 * date:  21-5-13
 */
public interface MtimeAPIRequest {


    @GET("mtime-search/search/unionSearch?pageSize=20&searchType=0&locationId=290")
    Call<MtimeSearchRespone> unioSearch(@Query("keyword") String keyword, @Query("pageIndex") int pageIndex);

    @GET("mtime-search/search/unionSearch?pageSize=20&searchType=0&locationId=290")
    Observable<MtimeSearchRespone> unionSearchRx(@Query("keyword") String keyword, @Query("pageIndex") int pageIndex);

    @GET("mtime-search/search/suggest?locationId=290&suggestType=1")
    Call<MtimeSearchRespone> suggestMovie(@Query("keyword") String keyword);

    @GET("mtime-search/search/suggest?locationId=290&suggestType=1")
    Observable<MtimeSearchRespone> suggestMovieRx(@Query("keyword") String keyword);

    @GET("library/movie/detail.api")
    Observable<MtimeDetailRespone> movieDetailRx(@Query("movieId") String movieId);

}
