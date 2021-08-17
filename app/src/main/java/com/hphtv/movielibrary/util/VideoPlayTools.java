package com.hphtv.movielibrary.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.core.content.FileProvider;

import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.station.kit.util.UriParseUtil;
import com.hphtv.movielibrary.data.ConstData;
import com.hphtv.movielibrary.sqlite.bean.VideoFile;

import java.io.File;

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

//    public static void play(Context context, VideoFile file) {
//        Uri fileUri = null;
//        String path = file.getUri();
//        String name = file.getFilename();
//        Intent intent = new Intent();
//
//        if (path != null && path.startsWith("/")) {
//            fileUri = UriParseUtil.parseVideoUri(context, path);
//        }
//
//        if (fileUri == null) {
//            if (path.startsWith("/")) {
//                File tFile = new File(path);
////                fileUri = FileProvider.getUriForFile(context, ConstData.AUTHORITIES, tFile);
//                fileUri = Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, path);
//            } else if (path != null) {
//                fileUri = Uri.parse(path);
//            }
//            if (fileUri == null) {
//                Toast.makeText(context, "can't find the file", Toast.LENGTH_SHORT).show();
//                return;
//            }
//        }
//
//        if (Build.MODEL.equalsIgnoreCase(ConstData.DeviceModel.TRV9)) {
//            intent.setAction(Intent.ACTION_VIEW);
//            intent.setDataAndType(fileUri, "video/*");
//        } else {
//            intent.setAction("firefly.intent.action.PLAY_VIDEO");
//            intent.putExtra("name", name);
//            intent.putExtra("play_url", path);
//        }
//
//        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        Log.v(TAG, "fileUri==" + fileUri.toString());
//
//        try {
//            if (Build.MODEL.equalsIgnoreCase(ConstData.DeviceModel.TRV9) && intent.resolveActivity(context.getPackageManager()) != null) {
//                Log.v(TAG, "----");
//                context.startActivity(intent);
//            } else {
//                context.startActivity(intent);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }


    public static void play(Context context, String path, String name) {
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
    }


//    public static void play(Context context, com.hphtv.movielibrary.roomdb.entity.VideoFile videoFile) {
//        Uri fileUri = null;
//        String path = videoFile.path;
//        String name = videoFile.filename;
//        if (path != null && path.startsWith("/")) {
//            fileUri = UriParseUtil.parseVideoUri(context, path);
//        }
//
//        if (fileUri == null) {
//            if (path.startsWith("/")) {
//                File tFile = new File(path);
////                fileUri = FileProvider.getUriForFile(context, ConstData.AUTHORITIES, tFile);
//                fileUri = Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, path);
//            } else if (path != null) {
//                fileUri = Uri.parse(path);
//            }
//            if (fileUri == null) {
//                Toast.makeText(context, "can't find the file", Toast.LENGTH_SHORT).show();
//                return;
//            }
//        }
//
//        Intent intent = new Intent();
//        if (Build.MODEL.equalsIgnoreCase(ConstData.DeviceModel.TRV9)) {
//            intent.setAction(Intent.ACTION_VIEW);
//            intent.setDataAndType(fileUri, "video/*");
//        } else {
//            intent.setAction("firefly.intent.action.PLAY_VIDEO");
//            intent.putExtra("name", name);
//            intent.putExtra("play_url", path);
//        }
//
//        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        Log.v(TAG, "play fileUri=" + fileUri.toString());
//
//        try {
//            if (Build.MODEL.equalsIgnoreCase(ConstData.DeviceModel.TRV9) && intent.resolveActivity(context.getPackageManager()) != null) {
//                context.startActivity(intent);
//            } else {
//                context.startActivity(intent);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
