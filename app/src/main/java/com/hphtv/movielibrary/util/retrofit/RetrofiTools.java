package com.hphtv.movielibrary.util.retrofit;

import com.hphtv.movielibrary.scraper.api.tmdb.TmdbURL;
import com.hphtv.movielibrary.scraper.api.tmdb.request.TmdbApiRequest;

import java.util.Locale;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * author: Sam Leung
 * date:  21-5-13
 */
public class RetrofiTools {

    private RetrofiTools() {

    }


    public static TmdbApiRequest createTmdbApiRequest() {
            return TmdbRetrofitBuilder().create(TmdbApiRequest.class);
    }

    public static TmdbApiRequest createTmdbApiRequest_EN(){
        return TmdbRetrofitBuilder_EN().create(TmdbApiRequest.class);
    }

    private static Retrofit TmdbRetrofitBuilder() {
        return new Retrofit.Builder()
                .baseUrl(TmdbURL.BASE_URL_CN)
                .addConverterFactory(GsonConverterFactory.create())// 设置数据解析器
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .client(getOkHttpClient())
                .build();
    }

    private static Retrofit TmdbRetrofitBuilder_EN() {
        return new Retrofit.Builder()
                .baseUrl(TmdbURL.BASE_URL_EN)
                .addConverterFactory(GsonConverterFactory.create())// 设置数据解析器
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .client(getOkHttpClient())
                .build();
    }

    private static OkHttpClient getOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        return builder.build();
    }


}
