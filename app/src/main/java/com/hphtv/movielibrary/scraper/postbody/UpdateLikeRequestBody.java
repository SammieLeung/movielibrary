package com.hphtv.movielibrary.scraper.postbody;

/**
 * author: Sam Leung
 * date:  2022/3/29
 */
public class UpdateLikeRequestBody {
    public String movie_id;
    public String is_favorite;
    public String type;

    public UpdateLikeRequestBody(String movie_id, String is_favorite, String type) {
        this.movie_id = movie_id;
        this.is_favorite = is_favorite;
        this.type = type;
    }
}
