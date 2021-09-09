package com.hphtv.movielibrary.scraper.postbody;

/**
 * author: Sam Leung
 * date:  2021/9/8
 */
public class PostSearchRequetBody {
    public String name;
    public int page;

    public PostSearchRequetBody(String keyword,int page){
        this.name=keyword;
        this.page=page;
    }
}
