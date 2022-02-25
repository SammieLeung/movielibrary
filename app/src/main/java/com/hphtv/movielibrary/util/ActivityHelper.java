package com.hphtv.movielibrary.util;

import android.content.Context;
import android.content.Intent;

import com.hphtv.movielibrary.ui.homepage.NewHomePageActivity;

/**
 * author: Sam Leung
 * date:  2022/2/24
 */
public class ActivityHelper {

    public static void startHomePageActivity(Context context){
        Intent intent=new Intent(context, NewHomePageActivity.class);
        context.startActivity(intent);
    }
}
