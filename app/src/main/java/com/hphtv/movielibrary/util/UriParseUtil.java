package com.hphtv.movielibrary.util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

/**
 * Created by tchip on 18-6-13.
 */

public class UriParseUtil {


    public static Uri parseVideoUri(Context context, String path) {
        String[] projection = new String[] {MediaStore.Images.Media._ID};
        String selection = MediaStore.Images.Media.DATA + "= ?";
        String[] selectionArgs = new String[] {path};
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()){
                String id = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media._ID));
                return Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


}
