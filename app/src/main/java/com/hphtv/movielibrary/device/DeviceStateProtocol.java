package com.hphtv.movielibrary.device;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * author: Sam Leung
 * date:  2023/1/5
 */
public interface DeviceStateProtocol {
    @POST("device/devinfo/bindstatus")
    Observable<BindStateResponse> getBindStatus(@Body BindStateRequestBody body);
}
