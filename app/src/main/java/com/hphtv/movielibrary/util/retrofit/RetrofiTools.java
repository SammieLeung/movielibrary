package com.hphtv.movielibrary.util.retrofit;

import com.hphtv.movielibrary.data.AuthHelper;
import com.hphtv.movielibrary.scraper.api.BaseUrl;
import com.hphtv.movielibrary.scraper.api.StationMovieProtocol;
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
public class RetrofiTools {
    public static final int TEST = 0;
    public static final int RELEASE = 1;
    public static final int PRE = 2;
    private static int mode = TEST;

    private RetrofiTools() {

    }


    public static StationMovieProtocol createRequest() {
        switch (mode) {
            case TEST:
                return RetrofitTestBuilder().create(StationMovieProtocol.class);
            case PRE:
                return PreRetrofitBuilder().create(StationMovieProtocol.class);
            default:
                return RetrofitBuilder().create(StationMovieProtocol.class);
        }
    }

    public static StationMovieProtocol createENRequest() {
        switch (mode) {
            case TEST:
                return RetrofitTestBuilder().create(StationMovieProtocol.class);
            case PRE:
                return PreRetrofitENBuilder().create(StationMovieProtocol.class);
            default:
                return RetrofitENBuilder().create(StationMovieProtocol.class);
        }
    }

    //正式-中文
    private static Retrofit RetrofitBuilder() {
        return new Retrofit.Builder()
                .baseUrl(BaseUrl.BASE_URL_CN)
                .addConverterFactory(GsonConverterFactory.create())// 设置数据解析器
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .client(getOkHttpClient(AuthHelper.sToken))
                .build();
    }

    //正式-英语
    private static Retrofit RetrofitENBuilder() {
        return new Retrofit.Builder()
                .baseUrl(BaseUrl.BASE_URL_EN)
                .addConverterFactory(GsonConverterFactory.create())// 设置数据解析器
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .client(getOkHttpClient(AuthHelper.sTokenEN))
                .build();
    }

    //预发布-汉语
    private static Retrofit PreRetrofitBuilder() {
        return new Retrofit.Builder()
                .baseUrl(BaseUrl.PRE_BASE_URL_CN)
                .addConverterFactory(GsonConverterFactory.create())// 设置数据解析器
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .client(getOkHttpClient(AuthHelper.sPreToken))
                .build();
    }

    //预发布-英语
    private static Retrofit PreRetrofitENBuilder() {
        return new Retrofit.Builder()
                .baseUrl(BaseUrl.PRE_BASE_URL_EN)
                .addConverterFactory(GsonConverterFactory.create())// 设置数据解析器
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .client(getOkHttpClient(AuthHelper.sPreTokenEN))
                .build();
    }


    //测试-中文
    private static Retrofit RetrofitTestBuilder() {
        return new Retrofit.Builder()
                .baseUrl(BaseUrl.BASE_URL_TEST)
                .addConverterFactory(GsonConverterFactory.create())// 设置数据解析器
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .client(getOkHttpClient(AuthHelper.sTestToken))
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
                .build();
    }


}
