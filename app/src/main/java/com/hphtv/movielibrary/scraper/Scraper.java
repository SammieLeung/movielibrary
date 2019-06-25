package com.hphtv.movielibrary.scraper;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.firefly.videonameparser.MovieNameInfo;
import com.hphtv.movielibrary.sqlite.bean.scraperBean.Images;
import com.hphtv.movielibrary.sqlite.bean.scraperBean.Rating;
import com.hphtv.movielibrary.sqlite.bean.scraperBean.SimpleMovie;
import com.hphtv.movielibrary.util.LogUtil;
import com.hphtv.movielibrary.util.MovieSharedPreferences;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Scraper {
    //http://kodi.wiki/view/Scrapers
    //https://github.com/xbmc/repo-scrapers/blob/gotham/metadata.themoviedb.org/tmdb.xml

    private static final String TAG = "Scraper";
       /***
     *
     * @param search_api https://api.douban.com/v2/movie/search?q=%1s
     * @param info
     */

    public static void createSearchUrl(String search_api, MovieNameInfo info) {
        String search = String.format(search_api, info.getName());
        Log.v(TAG, "createSearchUrl search_url:" + search);
        OkHttpClient client = new OkHttpClient.Builder().build();
        Request request = new Request.Builder()
                .url(search)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.v(TAG, "createSearchUrl response: onFailure");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                LogUtil.v(TAG, "createSearchUrl response:" + result);

                JSONObject jsonObj = JSON.parseObject(result);
                LogUtil.v(TAG, "createSearchUrl total:" + jsonObj.getInteger("total") + ",start:" + jsonObj.getInteger("start"));
            }
        });
    }

    /**
     * @param search_data_list 保存电影搜索结果的List
     * @param title            电影名字
     * @param year             年份
     * @param cover            封面
     * @param rating           评分
     * @param ratingCounts     评分人数
     * @param descrption1      描述1
     * @param descrption2      描述2
     * @param href             电影信息url
     */
    public static void addSearchMovieData(
            List<SimpleMovie> search_data_list, String title,
            String year, String cover, String rating, String ratingCounts,
            String descrption1, String descrption2, String href, String id) {

        Rating mRating = new Rating();
        mRating.max = 10;
        float fRating = 0;
        try {
            fRating = Float.valueOf(rating);
        } catch (Exception e) {
        }
        mRating.average = fRating;

        SimpleMovie simpleMovie = new SimpleMovie();
        simpleMovie.setAlt(href);
        simpleMovie.setTitle(title);
        String[] abstracts = {descrption1, descrption2};
        simpleMovie.setAbstracts(abstracts);
        simpleMovie.setYear(year);
        Images images = new Images();
        images.setLarge(cover);
        simpleMovie.setImages(images);
        simpleMovie.setRating(mRating);
        simpleMovie.setRatingsCounts(ratingCounts);
        simpleMovie.setId(id);
        if (search_data_list != null)
            search_data_list.add(simpleMovie);
    }
    /**
     * 语言为zh则使用豆瓣，否则使用IMDB
     * @return
     */
    public static int getApiVersion() {
        return MovieSharedPreferences.getInstance().getSearchAPI();
    }

    public static void setApiVersion(int api){
        MovieSharedPreferences.getInstance().setSearchAPI(api);
    }

}
