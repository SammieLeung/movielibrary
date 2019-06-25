package com.hphtv.movielibrary.listener;

import android.support.annotation.IntDef;

import com.hphtv.movielibrary.sqlite.bean.scraperBean.SimpleMovie;
import com.hphtv.movielibrary.data.ConstData;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

/**
 * @author lxp
 * @date 19-4-13
 */
public interface WebviewListener {


    @IntDef({ConstData.SearchMode.MODE_LIST, ConstData.SearchMode.MODE_INFO})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SearchMode {
    }
    public void onStart();
    public void onGetData(List<SimpleMovie> simpleMovieList,@SearchMode int mode);
}
