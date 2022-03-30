package com.hphtv.movielibrary.scraper.postbody;

/**
 * author: Sam Leung
 * date:  2022/3/29
 */
public class UpdateHistoryRequestBody {
    public String devsn;
    public String path;

    public UpdateHistoryRequestBody(String devsn, String path) {
        this.devsn = devsn;
        this.path = path;
    }
}
