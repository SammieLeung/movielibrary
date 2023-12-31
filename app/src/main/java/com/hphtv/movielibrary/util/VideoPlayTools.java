package com.hphtv.movielibrary.util;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import androidx.core.content.FileProvider;

import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.data.Config;
import com.hphtv.movielibrary.provider.FileContentProvider;
import com.station.kit.util.ToastUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author lxp
 * @date 19-8-14
 */
public class VideoPlayTools {
    public static final String TAG = VideoPlayTools.class.getSimpleName();

    public static final String DANGBEI_PKG = "com.dangbei.lerad.videoposter";

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
        play(context, path, name, null, null);
    }


    public static void play(Context context, String path, String name, ArrayList<String> pathList, ArrayList<String> nameList) {
        File file = new File(path);
        if (Config.getPlayerPackage().equals(Config.SYSTEM_PLAYER_PACKAGE) || !file.exists()) {
            try {
                playByComponentName(context, path, name, pathList, nameList);
                return;
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

            Uri uri = FileContentProvider.getUriForFile(file.getPath());
            Log.d(TAG, "player " + Config.getPlayerPackage());
            if (Config.getPlayerPackage().equals(DANGBEI_PKG))
                uri = FileProvider.getUriForFile(context.getApplicationContext(), "com.hphtv.movielibrary.fileprovider2", file);

            intent.setDataAndType(uri, "video/*");// type:改成"video/*"表示获取视频的
            try {
                context.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                ToastUtil.newInstance(context).toast(context.getString(R.string.toast_player_error, Config.getPlayerName()));
            }
        }
    }

    //适配Android 12, 解决播放界面关闭, 出现"一闪"的问题;
    public static void playByComponentName(Context context, String path, String name) {
        playByComponentName(context, path, name, null, null);
    }

    public static void playByComponentName(Context context, String path, String name, ArrayList<String> pathList, ArrayList<String> nameList) {
        ComponentName component = new ComponentName("com.hph.videoplayer",
                "com.hph.videoplayer.ui.VideoPlayerExternalActivity");

        Intent intent = new Intent();
        intent.setAction("firefly.intent.action.PLAY_VIDEO");
        intent.putExtra("name", name);
        intent.putExtra("play_url", path);
        intent.setComponent(component);

        if (nameList != null && pathList != null) {
            intent.putStringArrayListExtra("MEDIA_CENTER_VIDOE_LIST", pathList);
            intent.putStringArrayListExtra("MEDIA_CENTER_VIDOE_NAME", nameList);
        }

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
                    if (!packageName.equals("org.xbmc.kodi")
                            && !packageName.equals("com.rockchips.mediacenter")
                            && !packageName.equals("com.android.gallery3d")) {
                        map.put(packageName, name);
                    }
                }
                return map;
            }
        }
        return null;
    }

}
