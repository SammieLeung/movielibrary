package com.hphtv.movielibrary.scraper.postbody;

/**
 * author: Sam Leung
 * date:  2022/3/29
 */
public class UpdateLikeRequestBody {
    public String movie_id;
    public String is_favorite;
    public String source;

    public UpdateLikeRequestBody(String movie_id, String is_favorite, String source) {
        this.movie_id = movie_id;
        this.is_favorite = is_favorite;
        this.source = source;
    }
}
