package com.hphtv.movielibrary;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.hphtv.movielibrary.service.MovieScanService;

/**
 * 接收开机广播启动电影匹配服务
 * Created by tchip on 17-11-13.
 */

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service=new Intent(context,MovieScanService.class);
        context.startService(service);
    }
}
