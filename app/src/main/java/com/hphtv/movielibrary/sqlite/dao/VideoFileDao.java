package com.hphtv.movielibrary.sqlite.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.hphtv.movielibrary.sqlite.bean.Directory;
import com.hphtv.movielibrary.sqlite.bean.VideoFile;
import com.hphtv.movielibrary.sqlite.MovieDBHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tchip on 18-3-26.
 */

public class VideoFileDao extends BaseDao<VideoFile> {
    DirectoryDao mDirectoryDao;

    public VideoFileDao(Context context) {
        super(context, MovieDBHelper.TABLE_VIDEOFILE);
        if (mDirectoryDao == null)
            mDirectoryDao = new DirectoryDao(context);
    }

    /**
     * 数据库结果集转换MovieList
     *
     * @param cursor
     * @return
     */
    @Override
    public List<VideoFile> parseList(Cursor cursor) {
        List<VideoFile> videoFiles = new ArrayList<>();
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                VideoFile videoFile = new VideoFile();


                long id = cursor.getInt(cursor.getColumnIndex("id"));
                int wrapper_id = cursor.getInt(cursor.getColumnIndex("wrapper_id"));
                String uri = cursor.getString(cursor.getColumnIndex("uri"));
                String filename = cursor.getString(cursor.getColumnIndex("filename"));
                String thumbnail = cursor.getString(cursor.getColumnIndex("thumbnail"));
                String thumbnail_s = cursor.getString(cursor.getColumnIndex("thumbnail_s"));
                long dir_id = cursor.getInt(cursor.getColumnIndex("dir_id"));
                long dev_id = cursor.getInt(cursor.getColumnIndex("dev_id"));
                int is_matched=cursor.getInt(cursor.getColumnIndex("is_matched"));
                String search_name=cursor.getString(cursor.getColumnIndex("search_name"));
                String title_pinyin=cursor.getString(cursor.getColumnIndex("title_pinyin"));

                videoFile.setId(id);
                videoFile.setUri(uri);
                videoFile.setFilename(filename);
                videoFile.setThumbnail(thumbnail);
                videoFile.setThumbnail_s(thumbnail_s);
                videoFile.setWrapper_id(wrapper_id);
                videoFile.setDir_id(dir_id);
                videoFile.setDev_id(dev_id);
                videoFile.setMatched(is_matched);
                videoFile.setSearchName(search_name);
                videoFile.setTitlePinyin(title_pinyin);

                videoFiles.add(videoFile);

            }
        }
        return videoFiles;
    }


    @Override
    public ContentValues parseContentValues(VideoFile videoFile) {
        ContentValues contentValues = new ContentValues();

        long wrapper_id = videoFile.getWrapper_id();
        String uri = videoFile.getUri();
        String filename = videoFile.getFilename();
        String thumbnail = videoFile.getThumbnail();
        String thumbnail_s = videoFile.getThumbnail_s();
        long dir_id = videoFile.getDir_id();
        long dev_id = videoFile.getDev_id();
        int is_matched=videoFile.isMatched();
        String search_name=videoFile.getSearchName();
        String title_pinyin=videoFile.getTitlePinyin();


        contentValues.put("uri", uri);
        contentValues.put("wrapper_id", wrapper_id);
        contentValues.put("filename", filename);
        contentValues.put("thumbnail", thumbnail);
        contentValues.put("thumbnail_s", thumbnail_s);
        contentValues.put("dir_id", dir_id);
        contentValues.put("dev_id", dev_id);
        contentValues.put("is_matched",is_matched);
        contentValues.put("search_name",search_name);
        contentValues.put("title_pinyin",title_pinyin);

        return contentValues;
    }


    public boolean isEncrypted(String dir_id) {

        Cursor cursor = mDirectoryDao.select("id=?", new String[]{dir_id}, "0,1");
        if (cursor != null && cursor.getColumnCount() > 0) {
            Directory directory = mDirectoryDao.parseList(cursor).get(0);
            return directory.getIsEncrypted() == 1 ? true : false;
        }
        return false;
    }

}
