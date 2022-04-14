package com.hphtv.movielibrary.scraper.postbody;

import com.hphtv.movielibrary.data.Constants;

/**
 * author: Sam Leung
 * date:  2022/4/14
 */
public class MovieAccessRequestBody {
    public String movie_id;
    public String type;
    public String is_show;

    public MovieAccessRequestBody(String movie_id, String type, Constants.AccessPermission ap){
        this.movie_id=movie_id;
        this.type=type;
        this.is_show=ap.equals(Constants.AccessPermission.ADULT)?"0":"1";
    }
}
