package com.hphtv.movielibrary.sqlite.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.hphtv.movielibrary.sqlite.bean.Directory;
import com.hphtv.movielibrary.sqlite.MovieDBHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lxp
 * @date 19-3-28
 */
public class DirectoryDao extends BaseDao<Directory> {
    public DirectoryDao(Context context) {
        super(context, MovieDBHelper.TABLE_DIRECTORY);
    }

    public List<Directory> parseList(Cursor cursor) {
        List<Directory> dir_list = new ArrayList<>();
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                Directory directory = new Directory();
                long id = cursor.getInt(cursor.getColumnIndex("id"));
                String name = cursor.getString(cursor.getColumnIndex("name"));
                long parent_id = cursor.getInt(cursor.getColumnIndex("parent_id"));
                int video_number = cursor.getInt(cursor.getColumnIndex("video_number"));
                String uri = cursor.getString(cursor.getColumnIndex("uri"));
                String path=cursor.getString(cursor.getColumnIndex("path"));
                int matched_video = cursor.getInt(cursor.getColumnIndex("matched_video"));
                int is_encrypted = cursor.getInt(cursor.getColumnIndex("is_encrypted"));
                int scan_state = cursor.getInt(cursor.getColumnIndex("scan_state"));

                directory.setId(id);
                directory.setName(name);
                directory.setParentId(parent_id);
                directory.setVideoNumber(video_number);
                directory.setUri(uri);
                directory.setPath(path);
                directory.setMatchedVideo(matched_video);
                directory.setIsEcrypted(is_encrypted);
                directory.setScanState(scan_state);
                dir_list.add(directory);

            }
        }
        return dir_list;
    }

    public ContentValues parseContentValues(Directory directory) {
        ContentValues contentValues = new ContentValues();
        String name = directory.getName();
        long parent_id = directory.getParent_id();
        int video_number = directory.getVideo_number();
        String uri = directory.getUri();
        String path=directory.getPath();
        int matched_video = directory.getMatched_video();
        int is_encrypted = directory.getIsEncrypted();
        int scan_state = directory.getScan_state();

        contentValues.put("name", name);
        contentValues.put("parent_id", parent_id);
        contentValues.put("video_number", video_number);
        contentValues.put("uri", uri);
        contentValues.put("path",path);
        contentValues.put("matched_video", matched_video);
        contentValues.put("is_encrypted", is_encrypted);
        contentValues.put("scan_state", scan_state);

        return contentValues;
    }
}
