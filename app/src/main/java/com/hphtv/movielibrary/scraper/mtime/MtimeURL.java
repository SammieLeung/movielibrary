package com.hphtv.movielibrary.scraper.mtime;

/**
 * Created by tchip on 18-11-9.
 */

public class MtimeURL {
    public static final String SEARCH_URL="http://m.mtime.cn/Service/callback.mi/Showtime/SearchVoice.api?keyword=%1s";
    public static final String MOVIE_PAGE="http://movie.mtime.com/%1s/";
    public static final String MOVIE_PLOTS_PAGE="https://api-m.mtime.cn/Movie/plots.api?movieId=%1s";
    public static final String MOVIE_FULLCREDITS_PAGE="https://api-m.mtime.cn/Movie/MovieCreditsWithTypes.api?movieId=%1s";
    public static final String MOVIE_TRAILER_PAGE="https://api-m.mtime.cn/Movie/Video.api?pageIndex=1&movieId=%1s";
    public static final String MOVIE_POSTER_PAGE="https://api-m.mtime.cn/Movie/Image.api?movieId=%1s";
    public static final String MOVIE_DETAIL="http://m.mtime.cn/Service/callback.mi/movie/Detail.api?movieId=%1s";

    /**
     * post 方式
     * param t 20194210461333802
     *       keyword 关键字
     *       locationId 290
     */
    public static final String SEARCH_NEW_API="http://m.mtime.cn/Service/callback.mi/Search/SearchSuggestionNew.api";

}
