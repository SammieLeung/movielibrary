package com.hphtv.movielibrary;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

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
    }
}
