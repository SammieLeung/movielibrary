package com.hphtv.movielibrary;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.alibaba.fastjson.JSON;
import com.orhanobut.logger.Logger;
import com.station.kit.util.Shell;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ProviderTest {

    @Test
    public void testRecommends() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        String Authority = "com.hphtv.movielibrary.provider.v2";
        Uri uri = Uri.parse("content://" + Authority + "/recommends");

        Cursor cursor=context.getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Long id = cursor.getLong(cursor.getColumnIndex("id"));//视频id
                int season=cursor.getInt(cursor.getColumnIndex("season"));//电视 季
                /*
                    data是一个json字符串，包含了视频的所有信息
                   {
                        "summary":"2023·电影·爱情·剧情",
                        "thumb":"https://download.stationpc.cn/movie/stage/202301/19/jABOG8lL2NUE6x4Pur1mU14v8C4.jpg",
                        "description":"",
                        "cmd":"am start -a com.hphtv.movielibrary.detail --el \"movie_id\" 11 --ei \"season\" -1",
                        "title":"01",
                        "poster":"https://download.stationpc.cn/movie/stage/202301/19/nBzqrtypSMUjhn3Z3a3tV6bzAcl.jpg"
                   }
                 */
                String data=cursor.getString(cursor.getColumnIndex("data"));
                Log.e("ProviderTest", "id=" + id + ",season=" + season + ",data=" + data);
            }
            cursor.close();
        }
    }

    @Test
    public void testThumb(){
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        String Authority = "com.hphtv.movielibrary.provider.v2";
        //17是 recommends中获取到的id
        Uri uri = Uri.parse("content://" + Authority + "/thumb/17");

        Cursor cursor=context.getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String thumb=cursor.getString(cursor.getColumnIndex("thumb"));
                Log.e("ProviderTest", "thumb="+thumb);
            }
            cursor.close();
        }
    }

    @Test
    public void startDetailActivity(){
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        String Authority = "com.hphtv.movielibrary.provider.v2";
        Uri uri = Uri.parse("content://" + Authority + "/recommends");

        Cursor cursor=context.getContentResolver().query(uri, null, null, null, null);
        String data="{}";
        if (cursor != null) {
           if (cursor.moveToNext()) {
                 data=cursor.getString(cursor.getColumnIndex("data"));
            }
            cursor.close();
        }
        String cmd=JSON.parseObject(data).getString("cmd");
        Log.d("ProviderTest", "cmd="+cmd);
    }
}
