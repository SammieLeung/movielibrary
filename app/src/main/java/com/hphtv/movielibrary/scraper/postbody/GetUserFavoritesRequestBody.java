package com.hphtv.movielibrary.scraper.postbody;

/**
 * author: Sam Leung
 * date:  2022/12/5
 */
public class GetUserFavoritesRequestBody {
    public String type="1";//1 电影|2 片单
    public int page;
    public int limit;

}
