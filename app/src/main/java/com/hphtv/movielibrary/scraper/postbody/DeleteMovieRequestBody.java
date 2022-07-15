package com.hphtv.movielibrary.scraper.postbody;

/**
 * author: Sam Leung
 * date:  2022/3/29
 */
public class DeleteMovieRequestBody {
    public String movie_id;
    public String type;

    public DeleteMovieRequestBody(String movie_id,String type) {
        this.movie_id = movie_id;
        this.type=type;
    }
}
