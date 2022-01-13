package com.hphtv.movielibrary.scraper.postbody;

/**
 * author: Sam Leung
 * date:  2021/9/8
 */
public class PostDetailRequetBody {
    public String movie_id;
    public String type;

    public PostDetailRequetBody(String movie_id) {
        this.movie_id = movie_id;
    }

    public PostDetailRequetBody(String movie_id, String type) {
        this.movie_id = movie_id;
        this.type = type;
    }

}
