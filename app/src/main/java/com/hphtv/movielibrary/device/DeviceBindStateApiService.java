package com.hphtv.movielibrary.device;

import android.text.TextUtils;
import android.util.Log;

import com.hphtv.movielibrary.util.rxjava.SimpleObserver;
import com.station.device.BaseUrl;
import com.station.device.FConfigTools;
import com.station.kit.util.SystemPropertiesReflect;

import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * author: Sam Leung
 * date:  2023/1/5
 */
public class DeviceBindStateApiService {
    public static int getBindState() {
        try {
            String baseDomain = FConfigTools.getValue(BaseUrl.KEY_SERVER_NAME);
            String bindStateUrl;

            if (!TextUtils.isEmpty(baseDomain)) {
                bindStateUrl = baseDomain + "/";
            } else {
                int server = Integer.parseInt(SystemPropertiesReflect.get("persist.tvremote.mqttserver", "0"));
                if (server == 1) {
                    bindStateUrl = "https://device.stationpc.com/";
                } else {
                    bindStateUrl = "https://device.stationpc.cn/";
                }
            }
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(5000, TimeUnit.MILLISECONDS)
                    .readTimeout(5000, TimeUnit.MILLISECONDS)
                    .writeTimeout(5000, TimeUnit.MILLISECONDS)
                    .build();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(bindStateUrl)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())// 设置数据解析器
                    .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                    .build();

            DeviceStateProtocol request = retrofit.create(DeviceStateProtocol.class);
            Observable<BindStateResponse> rxResponse = request.getBindStatus(new BindStateRequestBody());

            BindStateResponse bindStateResponse = rxResponse.subscribeOn(Schedulers.io()).blockingFirst();
            if (bindStateResponse != null && bindStateResponse.data != null)
                return bindStateResponse.data.bind;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}
