package com.hphtv.movielibrary;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.hphtv.movielibrary.service.DeviceMonitorService;
import com.hphtv.movielibrary.service.DlnaControlService;
import com.hphtv.movielibrary.service.MovieScanService;
import com.hphtv.movielibrary.service.MovieScanService2;

/**
 * 接收开机广播启动电影匹配服务
 * Created by tchip on 17-11-13.
 */

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

//        Intent dlnaService=new Intent(context, DlnaControlService.class);
//        context.startService(dlnaService);
    }
}
