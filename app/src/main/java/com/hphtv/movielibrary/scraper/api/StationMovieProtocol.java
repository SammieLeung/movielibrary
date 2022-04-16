package com.hphtv.movielibrary.scraper.api;

import com.hphtv.movielibrary.scraper.postbody.DeleteMovieRequestBody;
import com.hphtv.movielibrary.scraper.postbody.DeviceConfigRequestBody;
import com.hphtv.movielibrary.scraper.postbody.PostDetailRequetBody;
import com.hphtv.movielibrary.scraper.postbody.PostSearchRequetBody;
import com.hphtv.movielibrary.scraper.postbody.RemoveFolderRequestBody;
import com.hphtv.movielibrary.scraper.postbody.UpdateHistoryRequestBody;
import com.hphtv.movielibrary.scraper.postbody.UpdateLikeRequestBody;
import com.hphtv.movielibrary.scraper.postbody.UpdateMovieRequestBody;
import com.hphtv.movielibrary.scraper.respone.BaseRespone;
import com.hphtv.movielibrary.scraper.respone.MovieDetailRespone;
import com.hphtv.movielibrary.scraper.respone.MovieSearchRespone;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * author: Sam Leung
 * date:  2021/9/8
 */
public interface StationMovieProtocol {
    @POST("movie/movie/search")
    Observable<MovieSearchRespone> createSearch(@Body PostSearchRequetBody body);

    @POST("movie/movie/detail")
    Observable<MovieDetailRespone> createDetail(@Body PostDetailRequetBody body);

    @POST("movie/file/save")
    Observable<BaseRespone> updateMovie(@Body UpdateMovieRequestBody body);

    @POST("device/movie/deleteInfo")
    Observable<BaseRespone> deleteMovie(@Body DeleteMovieRequestBody body);

    @POST("movie/history/save")
    Observable<BaseRespone> updateHistory(@Body UpdateHistoryRequestBody body);

    @POST("movie/favorite/save")
    Observable<BaseRespone> updateLike(@Body UpdateLikeRequestBody body);

    @POST("movie/file/removeFolder")
    Observable<BaseRespone> removeShortcut(@Body RemoveFolderRequestBody body);

    @POST("device/device/saveConfig")
    Observable<BaseRespone> changeConfig(@Body DeviceConfigRequestBody body);
}
 