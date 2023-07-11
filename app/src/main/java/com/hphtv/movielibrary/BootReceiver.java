package com.hphtv.movielibrary;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

import com.hphtv.movielibrary.service.DeviceMonitorService;
import com.orhanobut.logger.Logger;

/**
 * 接收开机广播启动电影匹配服务
 * Created by tchip on 17-11-13.
 */

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

//        Intent dlnaService=new Intent(context, DlnaControlService.class);
//        context.startService(dlnaService);
        //  从Application 启动可以搜索，但是无法使用DMC
        try {

            if (Settings.Global.getInt(context.getContentResolver(), Settings.Global.DEVICE_PROVISIONED, 0) == 1 &&
                    Settings.Secure.getInt(context.getContentResolver(), "user_setup_complete", 0) == 1) {
                Intent service = new Intent(context, DeviceMonitorService.class);
                context.startService(service);
            }else{
                Logger.w("Wait for setup wizard to complete");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
