package com.hphtv.movielibrary.scraper.mtime;

/**
 * Created by tchip on 18-11-9.
 */

public class MtimeURL {
//    public static final String SEARCH_NEW_URL="http://m.mtime.cn/Service/callback.mi/Showtime/SearchVoice.api?keyword=%1s";
    public static final String SEARCH_NEW_URL ="https://m.mtime.cn/Service/callback.mi/Search/SearchSuggestionNew.api?keyword=%1s";
    public static final String SEARCH_NEW_URL2="http://front-gateway.mtime.com/mtime-search/search/unionSearch?keyword=%1s&pageIndex=%2d&pageSize=20&searchType=0&locationId=290&genreTypes=&area=&year=";

    public static final String MOVIE_PAGE="http://movie.mtime.com/%1s/";
    public static final String MOVIE_PLOTS_PAGE="https://api-m.mtime.cn/Movie/plots.api?movieId=%1s";
    public static final String MOVIE_FULLCREDITS_PAGE="https://api-m.mtime.cn/Movie/MovieCreditsWithTypes.api?movieId=%1s";
    public static final String MOVIE_TRAILER_PAGE="http://front-gateway.mtime.com/library/movie/category/video.api?movieId=%1s&type=0&pageIndex=-1";
    public static final String MOVIE_POSTER_PAGE="http://front-gateway.mtime.com/library/movie/image.api?movieId=%1s&locationId=290";
    public static final String MOVIE_DETAIL="http://front-gateway.mtime.com/library/movie/detail.api?movieId=%1s";

    /**
     * post 方式
     * param t 20194210461333802
     *       keyword 关键字
     *       locationId 290
     */
    public static final String SEARCH_NEW_API="http://m.mtime.cn/Service/callback.mi/Search/SearchSuggestionNew.api";

}
