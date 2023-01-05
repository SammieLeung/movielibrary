package com.hphtv.movielibrary.device;

import com.station.device.StationDeviceTool;

/**
 * author: Sam Leung
 * date:  2023/1/5
 */
public class BindStateRequestBody {
    String devsn;
    String token;
    long timestamp;
    String sign;

    public BindStateRequestBody() {
        devsn = StationDeviceTool.getDeviceSN();
        token = SignEncrypteTools.getToken();
        timestamp = System.currentTimeMillis();
        sign = SignEncrypteTools.getEncryptedSignature(devsn, token, timestamp);
    }
}
