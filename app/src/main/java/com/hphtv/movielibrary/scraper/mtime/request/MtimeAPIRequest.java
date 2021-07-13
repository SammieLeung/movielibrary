package com.hphtv.movielibrary.scraper.mtime.request;

import com.hphtv.movielibrary.scraper.mtime.respone.MtimeDetailRespone;
import com.hphtv.movielibrary.scraper.mtime.respone.MtimeSuggestRespone;
import com.hphtv.movielibrary.scraper.mtime.respone.MtimeUnionSearchRespone;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * author: Sam Leung
 * date:  21-5-13
 */
public interface MtimeAPIRequest {

    @GET("mtime-search/search/unionSearch?pageSize=20&searchType=0&locationId=290")
    Call<MtimeUnionSearchRespone> unioSearch(@Query("keyword") String keyword, @Query("pageIndex") int pageIndex);

    @GET("mtime-search/search/unionSearch?pageSize=20&searchType=0&locationId=290")
    Observable<MtimeUnionSearchRespone> unionSearchRx(@Query("keyword") String keyword, @Query("pageIndex") int pageIndex);

    @GET("mtime-search/search/unionSearch?searchType=0&locationId=290")
    Single<MtimeUnionSearchRespone> unionSearchSingleRx(@Query("keyword") String keyword, @Query("pageIndex") int pageIndex,@Query("pageSize") int pageSize);

    @GET("mtime-search/search/suggest?locationId=290&suggestType=1")
    Call<MtimeSuggestRespone> suggestMovie(@Query("keyword") String keyword);

    @GET("mtime-search/search/suggest?locationId=290&suggestType=1")
    Observable<MtimeSuggestRespone> suggestMovieRx(@Query("keyword") String keyword);

    @GET("library/movie/detail.api")
    Observable<MtimeDetailRespone> movieDetailRx(@Query("movieId") String movieId);

}
