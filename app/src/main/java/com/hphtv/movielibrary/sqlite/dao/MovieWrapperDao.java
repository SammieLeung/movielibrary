package com.hphtv.movielibrary.sqlite.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.alibaba.fastjson.JSON;
import com.hphtv.movielibrary.sqlite.bean.MovieWrapper;
import com.hphtv.movielibrary.sqlite.bean.ScraperInfo;
import com.hphtv.movielibrary.sqlite.MovieDBHelper;
import com.hphtv.movielibrary.util.StrUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lxp
 * @date 19-3-29
 */
public class MovieWrapperDao extends BaseDao<MovieWrapper> {
    public MovieWrapperDao(Context context) {
        super(context, MovieDBHelper.TABLE_MOVIEWRAPPER);
    }

    @Override
    public List<MovieWrapper> parseList(Cursor cursor) {
        List<MovieWrapper> warpper_list = new ArrayList<>();
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                MovieWrapper wrapper = new MovieWrapper();
                int id = cursor.getInt(cursor.getColumnIndex("id"));
                String device_ids_json=cursor.getString(cursor.getColumnIndex("dev_ids"));
                String dir_ids_json=cursor.getString(cursor.getColumnIndex("dir_ids"));
                String scraper_infos_json = cursor.getString(cursor.getColumnIndex("scraper_infos"));
                String files_ids_json = cursor.getString(cursor.getColumnIndex("file_ids"));
                String title = cursor.getString(cursor.getColumnIndex("title"));
                String poster = cursor.getString(cursor.getColumnIndex("poster"));
                String average=cursor.getString(cursor.getColumnIndex("average"));
                ScraperInfo[] scraper_infos = null;
                Long[] file_ids = null;
                Long[] device_ids=null;
                String[] dir_ids=null;
                if (!StrUtils.isNull(scraper_infos_json))
                    scraper_infos = JSON.parseArray(scraper_infos_json, ScraperInfo.class).toArray(new ScraperInfo[0]);
                if (!StrUtils.isNull(files_ids_json)) {
                    Integer[] t_file_ids= JSON.parseArray(files_ids_json).toArray(new Integer[0]);
                    file_ids=new Long[t_file_ids.length];
                    for(int i=0;i<file_ids.length;i++){
                        file_ids[i]= Long.valueOf(t_file_ids[i]);
                    }
                }
                if (!StrUtils.isNull(device_ids_json)) {
                    Integer[] t_device_ids= JSON.parseArray(device_ids_json).toArray(new Integer[0]);
                    device_ids=new Long[t_device_ids.length];
                    for(int i=0;i<device_ids.length;i++){
                        device_ids[i]= Long.valueOf(t_device_ids[i]);
                    }
                }
                if (!StrUtils.isNull(dir_ids_json)) {
                    dir_ids= JSON.parseArray(dir_ids_json).toArray(new String[0]);
                }
                String title_pinyin=cursor.getString(cursor.getColumnIndex("title_pinyin"));


                wrapper.setId(id);
                wrapper.setDevIds(device_ids);
                wrapper.setDirIds(dir_ids);
                wrapper.setTitle(title);
                wrapper.setTitlePinyin(title_pinyin);
                wrapper.setFileIds(file_ids);
                wrapper.setPoster(poster);
                wrapper.setScraperInfos(scraper_infos);
                wrapper.setAverage(average);

                warpper_list.add(wrapper);
            }
        }
        return warpper_list;
    }

    public ContentValues parseContentValues(MovieWrapper wrapper) {
        ContentValues contentValues = new ContentValues();
        String title = wrapper.getTitle();
        String file_ids = JSON.toJSONString(wrapper.getFileIds());
        String poster = wrapper.getPoster();
        String scraper_infos = JSON.toJSONString(wrapper.getScraperInfos());
        String dev_ids=JSON.toJSONString(wrapper.getDevIds());
        String dir_ids=JSON.toJSONString(wrapper.getDirIds());
        String average=wrapper.getAverage();
        String title_pinyin=wrapper.getTitlePinyin();

        contentValues.put("title", title);
        contentValues.put("title_pinyin",title_pinyin);
        contentValues.put("file_ids", file_ids);
        contentValues.put("poster", poster);
        contentValues.put("scraper_infos", scraper_infos);
        contentValues.put("dev_ids", dev_ids);
        contentValues.put("dir_ids", dir_ids);
        contentValues.put("average",average);

        return contentValues;
    }
}
