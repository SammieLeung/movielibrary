package com.hphtv.movielibrary.scraper.postbody;

/**
 * author: Sam Leung
 * date:  2021/9/8
 */
public class PostDetailRequestBody {
    public String movie_id;
    public String type;
    public String source="db";

    public PostDetailRequestBody(String movie_id) {
        this.movie_id = movie_id;
    }

    public PostDetailRequestBody(String movie_id, String type) {
        this.movie_id = movie_id;
        this.type = type;
    }

    public PostDetailRequestBody(String movie_id, String type, String source) {
        this.movie_id = movie_id;
        this.type = type;
        this.source = source;
    }
}
