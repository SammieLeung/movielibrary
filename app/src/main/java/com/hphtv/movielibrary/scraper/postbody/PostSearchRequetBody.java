package com.hphtv.movielibrary.scraper.postbody;

import com.hphtv.movielibrary.data.Constants;

/**
 * author: Sam Leung
 * date:  2021/9/8
 */
public class PostSearchRequetBody {
    public String name;
    public int page;
    public String type;
    public String year;

    public PostSearchRequetBody(String keyword,int page){
        this.name=keyword;
        this.page=page;
    }

    public PostSearchRequetBody(String keyword,int page,String type,String year){
        this.name=keyword;
        this.page=page;
        this.type=type;
        this.year=year;
    }
}
