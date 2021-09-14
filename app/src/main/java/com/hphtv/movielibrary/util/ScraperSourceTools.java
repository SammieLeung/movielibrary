package com.hphtv.movielibrary.util;

import com.hphtv.movielibrary.data.Constants;
import com.station.kit.util.LogUtil;

import java.util.Locale;

/**
 * Created by tchip on 18-8-17.
 */

public class ScraperSourceTools {

    public static String getSource() {
        Locale locale=Locale.getDefault();
        if(locale.getLanguage().equals(Locale.SIMPLIFIED_CHINESE.getLanguage())&&locale.getCountry().equals(Locale.SIMPLIFIED_CHINESE.getCountry())) {
            return Constants.ScraperSource.MTIME;
        }else{
            return Constants.ScraperSource.OMDB;
        }
    }
}
