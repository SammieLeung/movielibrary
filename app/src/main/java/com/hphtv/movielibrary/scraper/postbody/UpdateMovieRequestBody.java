package com.hphtv.movielibrary.scraper.postbody;

/**
 * author: Sam Leung
 * date:  2022/3/29
 */
public class UpdateMovieRequestBody {
    public String movie_id;
    public String type;
    public String path;
    public String keyword;
    public String filename;
    public String storage;
    public String folder;
    public String duration;
    public String current_point;
    public String watch_limit;

    public UpdateMovieRequestBody(String path, String filename) {
        this.path = path;
        this.filename = filename;
    }

    public UpdateMovieRequestBody(String movie_id, String type, String path, String keyword, String filename, String storage, String folder, String duration, String current_point,String watch_limit) {
        this.movie_id = movie_id;
        this.type = type;
        this.path = path;
        this.keyword = keyword;
        this.filename = filename;
        this.storage = storage;
        this.folder = folder;
        this.duration = duration;
        this.current_point = current_point;
        this.watch_limit=watch_limit;
    }
}
