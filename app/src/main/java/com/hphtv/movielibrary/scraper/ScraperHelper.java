package com.hphtv.movielibrary.scraper;

import com.hphtv.movielibrary.service.MovieScanService2;

import java.lang.ref.WeakReference;

/**
 * author: Sam Leung
 * date:  2021/8/18
 */
public class ScraperHelper {
    private static ScraperHelper sScraperHelper;
    WeakReference<MovieScanService2> mWeakReference;

    public static ScraperHelper getInstance() {
        if (sScraperHelper == null) {
            synchronized (ScraperHelper.class) {
                if (sScraperHelper == null) {
                     sScraperHelper=new ScraperHelper();
                }
            }
        }
        return sScraperHelper;
    }

    public void setScanService2(MovieScanService2 scanService2) {
        mWeakReference = new WeakReference<>(scanService2);
    }

}
