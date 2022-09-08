package com.hphtv.movielibrary.util;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import androidx.core.content.FileProvider;

import com.hphtv.movielibrary.data.Config;

import java.io.File;
import java.util.HashMap;
import java.util.List;

/**
 * @author lxp
 * @date 19-8-14
 */
public class VideoPlayTools {
    public static final String TAG = VideoPlayTools.class.getSimpleName();

    public static void play(Context context, Uri uri) {
        Intent intent = new Intent();
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            intent.setAction("firefly.intent.action.PLAY_VIDEO");
        } else {
            intent.setAction(Intent.ACTION_VIEW);
        }
        intent.setDataAndType(uri, "video/*");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Log.v(TAG, "fileUri==" + uri.toString());
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void play(Context context, String path, String name) {
        if (Config.getPlayerPackage().equals(Config.SYSTEM_PLAYER_PACKAGE)) {
            if (Build.VERSION.SDK_INT == 32) { //适配Android 12
                try {
                    playByComponentName(context, path, name);
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            Intent intent = new Intent();
            intent.setAction("firefly.intent.action.PLAY_VIDEO");
            intent.putExtra("name", name);
            intent.putExtra("play_url", path);

            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Log.v(TAG, "play path=" + path);
            try {
                context.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setPackage(Config.getPlayerPackage());
            File file = new File(path);
            Uri uri = Uri.parse(path);
            if (file.exists()) {
                ContentResolver contentResolver = context.getContentResolver();
                Cursor cursor = contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Video.Media.DATA + "=?", new String[]{path}, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int _id = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media._ID));
                    uri = Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, String.valueOf(_id));
                }
            }
            intent.setDataAndType(uri, "video/*");// type:改成"video/*"表示获取视频的
            context.startActivity(intent);
        }
    }

    //适配Android 12, 解决播放界面关闭, 出现"一闪"的问题;
    public static void playByComponentName(Context context, String path, String name) {
        ComponentName component = new ComponentName("com.hph.videoplayer",
                "com.hph.videoplayer.ui.VideoPlayerExternalActivity");

        Intent intent = new Intent();
        intent.putExtra("name", name);
        intent.putExtra("play_url", path);
        intent.setComponent(component);

        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        context.startActivity(intent);
        Log.v(TAG, "playByComponentName path=" + path);
    }

    public static HashMap<String, String> getVideoPlayers(Context context) {
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(uri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int idCol = cursor.getColumnIndex(MediaStore.Video.Media._ID);
            int id = cursor.getInt(idCol);
            cursor.close();
            uri = Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, String.valueOf(id));
            PackageManager pm = context.getPackageManager();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "video/*");// type:改成"video/*"表示获取视频的
            List<ResolveInfo> mResolveInfoList = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            if (mResolveInfoList.size() > 0) {
                HashMap<String, String> map = new HashMap<>();
                for (ResolveInfo ri : mResolveInfoList) {
                    String name = ri.loadLabel(pm).toString();
                    String packageName = ri.activityInfo.packageName;
                    if (!packageName.equals("com.android.gallery3d")) {
                        map.put(packageName, name);
                    }
                }
                return map;
            }
        }
        return null;
    }

}
