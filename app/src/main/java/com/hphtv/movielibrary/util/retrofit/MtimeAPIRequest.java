package com.hphtv.movielibrary.util.retrofit;

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
    Call<MtimeSearchRespone> searchMovieByMtime(@Query("keyword") String keyword, @Query("pageIndex") int pageIndex);

    @GET("mtime-search/search/unionSearch?pageSize=20&searchType=0&locationId=290")
    Observable<MtimeSearchRespone> searchMovieByMtimeRx(@Query("keyword") String keyword, @Query("pageIndex") int pageIndex);

    @GET("library/movie/detail.api")
    Observable<MtimeDetailRespone> getMovieDetailByMtimeRx(@Query("movieId") String movieId);
}
