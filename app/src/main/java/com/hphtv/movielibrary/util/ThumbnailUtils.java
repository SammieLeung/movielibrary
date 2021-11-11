package com.hphtv.movielibrary.util;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;

import com.station.kit.util.LogUtil;


/**
 * author: Sam Leung
 * date:  2021/10/28
 */
public class ThumbnailUtils {
    /**
     * 获取视频文件截图
     *
     * @param path 视频文件的路径
     * @return Bitmap 返回获取的Bitmap
     */
    public static Bitmap getVideoThumb(String path,int persent) {
        try {
            MediaMetadataRetriever media = new MediaMetadataRetriever();
            media.setDataSource(path);
            String time = media.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            persent = (int) Math.ceil(Math.random() * 100);
            int duration = Integer.parseInt(time) * 10;
            long curentTime = duration * persent;
            LogUtil.e("time=" + duration + " curentTime=" + curentTime);
            Bitmap bitmap = media.getFrameAtTime(curentTime,
                    MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
            return bitmap;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }


}
