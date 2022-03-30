package com.hphtv.movielibrary.scraper.postbody;

/**
 * author: Sam Leung
 * date:  2022/3/29
 */
public class DeleteMovieRequestBody {
    public String movie_id;

    public DeleteMovieRequestBody(String movie_id) {
        this.movie_id = movie_id;
    }
}
