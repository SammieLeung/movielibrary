package com.hphtv.movielibrary.util.retrofit;

import android.text.TextUtils;

import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.hphtv.movielibrary.data.AuthHelper;
import com.hphtv.movielibrary.scraper.api.StationMovieProtocol;
import com.station.device.BaseUrl;
import com.station.device.FConfigTools;
import com.station.device.StationDeviceTool;

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
public class RetrofitTools {
    public static final int RELEASE = 1;
    public static final int PRE = 2;
    public static int mode = RELEASE;
    private RetrofitTools() {

    }

    public static StationMovieProtocol createRequest() {
        String base_url = FConfigTools.getValue(BaseUrl.KEY_SERVER_NAME);
        if(!TextUtils.isEmpty(base_url)) {
          return RetrofitBuilder(base_url,AuthHelper.sTokenCN).create(StationMovieProtocol.class);
        }
        switch (mode) {
            case PRE:
                return RetrofitBuilder(BaseUrl.BASE_URL_PRE_CN,AuthHelper.sTokenCN).create(StationMovieProtocol.class);
            default:
                return RetrofitBuilder(BaseUrl.BASE_URL_CN,AuthHelper.sTokenCN).create(StationMovieProtocol.class);
        }

    }

    public static StationMovieProtocol createENRequest() {
        String base_url = FConfigTools.getValue(BaseUrl.KEY_SERVER_NAME);
        if(!TextUtils.isEmpty(base_url)) {
            return RetrofitBuilder(base_url,AuthHelper.sTokenEN).create(StationMovieProtocol.class);
        }
        switch (mode) {
            case PRE:
                return RetrofitBuilder(BaseUrl.BASE_URL_PRE_EN,AuthHelper.sTokenEN).create(StationMovieProtocol.class);
            default:
                return RetrofitBuilder(BaseUrl.BASE_URL_EN,AuthHelper.sTokenEN).create(StationMovieProtocol.class);
        }
    }


    private static Retrofit RetrofitBuilder(String base_url,String token) {
        return new Retrofit.Builder()
                .baseUrl(base_url)
                .addConverterFactory(GsonConverterFactory.create())// 设置数据解析器
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .client(getOkHttpClient(token))
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
                                .addHeader("devsn", StationDeviceTool.getDeviceSN())
                                .build();
                        return chain.proceed(request);
                    }
                })
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addNetworkInterceptor(new StethoInterceptor())
                .build();
    }


}
