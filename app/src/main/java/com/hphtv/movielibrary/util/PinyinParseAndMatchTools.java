package com.hphtv.movielibrary.util;

import android.text.TextUtils;

import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.qwertysearch.model.PinyinBaseUnit;
import com.qwertysearch.model.PinyinSearchUnit;
import com.qwertysearch.model.PinyinUnit;
import com.qwertysearch.util.PinyinUtil;
import com.qwertysearch.util.QwertyUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tchip on 17-12-14.
 */

public class PinyinParseAndMatchTools {
    public static final String TAG = "MyPinyinUtil";
    public static PinyinParseAndMatchTools sPinyinParseAndMatchTools;

    public static PinyinParseAndMatchTools getInstance() {
        if (sPinyinParseAndMatchTools == null) {
            synchronized (PinyinParseAndMatchTools.class) {
                if (sPinyinParseAndMatchTools == null) {
                    sPinyinParseAndMatchTools = new PinyinParseAndMatchTools();
                }
            }
        }
        return sPinyinParseAndMatchTools;
    }

    public List<MovieDataView> match(List<MovieDataView> list, String rawKeyword) {
        List<MovieDataView> matchList = new ArrayList<>();
        if (list != null && list.size() > 0) {
            for (MovieDataView dataView : list) {
                String title = dataView.title;
                PinyinSearchUnit pinyinSearchUnit = new PinyinSearchUnit(title);
                PinyinUtil.parse(pinyinSearchUnit);
                if (!TextUtils.isEmpty(rawKeyword))
                    if (QwertyUtil.match(pinyinSearchUnit, rawKeyword)) {
                        matchList.add(dataView);
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
        int i = 0;
        for (PinyinUnit pyUnit : pinyinUtils) {
            wordBuffer.append("(");
            for (PinyinBaseUnit pybaseUtil : pyUnit.getPinyinBaseUnitIndex()) {
                wordBuffer.append(pybaseUtil.getPinyin().charAt(0) + ",");
            }
            wordBuffer.replace(wordBuffer.lastIndexOf(","), wordBuffer.length(), ")");
            i++;
        }
        return wordBuffer.toString();
    }


}
