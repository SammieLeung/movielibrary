package com.hphtv.movielibrary.util.retrofit;

import com.hphtv.movielibrary.scraper.mtime.request.MtimeAPIRequest;
import com.hphtv.movielibrary.scraper.mtime.MtimeURL;

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

    public static MtimeAPIRequest createMtimeRequest() {
        return MTimeRetrofitBuilder().create(MtimeAPIRequest.class);
    }

    private static Retrofit MTimeRetrofitBuilder() {
        return new Retrofit.Builder()
                .baseUrl(MtimeURL.BASE_URL)// 设置网络请求的Url地址
                .addConverterFactory(GsonConverterFactory.create())// 设置数据解析器
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
//                .client(getOkHttpClient())
                .build();
    }

    private static OkHttpClient getOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        return builder.build();
    }


}
