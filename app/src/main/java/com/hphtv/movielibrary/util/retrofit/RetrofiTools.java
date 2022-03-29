package com.hphtv.movielibrary.util.retrofit;

import com.hphtv.movielibrary.data.AuthHelper;
import com.hphtv.movielibrary.scraper.api.tmdb.TmdbURL;
import com.hphtv.movielibrary.scraper.api.tmdb.request.TmdbApiRequest;
import com.station.device.TokenHelper;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
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

    public static TmdbApiRequest createTmdbApiRequest_EN() {
        return TmdbRetrofitBuilder_EN().create(TmdbApiRequest.class);
    }

    private static Retrofit TmdbRetrofitBuilder() {
        return new Retrofit.Builder()
                .baseUrl(TmdbURL.BASE_URL_CN)
                .addConverterFactory(GsonConverterFactory.create())// 设置数据解析器
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .client(getOkHttpClient(AuthHelper.sPreToken))
                .build();
    }

    private static Retrofit TmdbRetrofitBuilder_EN() {
        return new Retrofit.Builder()
                .baseUrl(TmdbURL.BASE_URL_EN)
                .addConverterFactory(GsonConverterFactory.create())// 设置数据解析器
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .client(getOkHttpClient(AuthHelper.sPreTokenEN))
                .build();
    }

    private static OkHttpClient getOkHttpClient(String token) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        return builder.readTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request request = chain.request()
                                .newBuilder()
                                .addHeader("token", token)
                                .addHeader("client", "device")
                                .build();
                        return chain.proceed(request);
                    }
                })
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }


}
