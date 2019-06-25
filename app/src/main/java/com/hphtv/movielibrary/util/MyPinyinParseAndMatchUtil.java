package com.hphtv.movielibrary.util;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.hphtv.movielibrary.data.ConstData;
import com.hphtv.movielibrary.sqlite.bean.MovieWrapper;
import com.hphtv.movielibrary.sqlite.bean.scraperBean.Movie;
import com.hphtv.movielibrary.sqlite.dao.MovieDao;
import com.qwertysearch.model.PinyinBaseUnit;
import com.qwertysearch.model.PinyinSearchUnit;
import com.qwertysearch.model.PinyinUnit;
import com.qwertysearch.util.PinyinUtil;
import com.qwertysearch.util.QwertyUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by tchip on 17-12-14.
 */

public class MyPinyinParseAndMatchUtil {
    public static final String TAG = "MyPinyinUtil";
    public static MyPinyinParseAndMatchUtil pinyinSearchUtil;
    private List<MovieWrapper> mWrappers;

    public static MyPinyinParseAndMatchUtil getInstance() {
        if (pinyinSearchUtil == null) {
            pinyinSearchUtil = new MyPinyinParseAndMatchUtil();
        }
        return pinyinSearchUtil;
    }

    public void initBaseDatas(List<MovieWrapper> datas) {
        mWrappers = datas;
    }

    public List<MovieWrapper> match(String rawKeyword) {
        List<MovieWrapper> matchList = new ArrayList<>();
        if (mWrappers != null && mWrappers.size() > 0) {
            for (MovieWrapper wrapper : mWrappers) {
                String title=wrapper.getTitle();
                    PinyinSearchUnit pinyinSearchUnit = new PinyinSearchUnit(title);
                    PinyinUtil.parse(pinyinSearchUnit);
                    if (QwertyUtil.match(pinyinSearchUnit, rawKeyword)) {
                        matchList.add(wrapper);
                    }
                }

        }
        return matchList;
    }

    public static String parsePinyin(String title) {
        PinyinSearchUnit pinyinSearchUnit = new PinyinSearchUnit(title);
        PinyinUtil.parse(pinyinSearchUnit);
        List<PinyinUnit> pinyinUtils = pinyinSearchUnit.getPinyinUnits();
        StringBuffer wordBuffer = new StringBuffer();
        int i=0;
        for (PinyinUnit pyUnit : pinyinUtils) {
            StringBuffer chineseBuffer = new StringBuffer();
//            Log.v(TAG,"第"+i+"轮");
            for (PinyinBaseUnit pybaseUtil : pyUnit.getPinyinBaseUnitIndex()) {
                chineseBuffer.append(pybaseUtil.getPinyin().charAt(0)+",");
//                Log.v(TAG,pybaseUtil.getOriginalString()+" ="+pybaseUtil.getPinyin());
            }
            chineseBuffer.deleteCharAt(chineseBuffer.length()-1);
            i++;
            wordBuffer.append(chineseBuffer.toString()+"|");
        }
        if(wordBuffer.length()>0){
            wordBuffer.deleteCharAt(wordBuffer.length()-1);
        }
        return wordBuffer.toString();
    }

    public static List<Movie> match(String rawKeyword, Context context) {
        List<Movie> matchList = new ArrayList<Movie>();
        rawKeyword=rawKeyword.toLowerCase();
        MovieDao dao = new MovieDao(context);
        String whereClause = "title_pinyin like ?";
        StringBuffer sqlKeyword = new StringBuffer();
        for (int i = 0; i < rawKeyword.length(); i++) {
            sqlKeyword.append("%" + rawKeyword.charAt(i)+"%|");
        }
        sqlKeyword.deleteCharAt(sqlKeyword.length()-1);
        String[] whereArgs = new String[]{sqlKeyword.toString()};
        Cursor cursor = dao.select(null, whereClause, whereArgs, "title", null, null, null);
        Log.v(TAG, "ddd " + cursor);
        matchList = dao.parseList(cursor);
        if (matchList != null)
            for (Movie movie : matchList) {
                Log.v(TAG, "mat=" + movie.getTitle());
            }
        return matchList;
    }






}
