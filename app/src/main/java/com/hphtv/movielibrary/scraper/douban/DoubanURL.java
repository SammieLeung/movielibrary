package com.hphtv.movielibrary.scraper.douban;

/**
 * 存储一些url地址
 */
public class DoubanURL {
	public static final String HTTP = "http://";
	public static final String HTTPS = "https://";
	
	public static final String BASE = "www.douban.com/";
	public static final String MOVIE_BASE = "movie.douban.com/";
	public static final String SERVICE = "service/";
	public static final String OAUTH_VERSION = "auth2/";
	
	public static final String API_BASE = "api.douban.com/";
	public static final String API_VERSION = "v2/";
	public static final String API_BASE_V2 ="https://api.douban.com/v2/movie/search?q=%s&start=%d&count=%d";

	//oauth2 auth url
	public static final String AUTH_URL = HTTPS + BASE + SERVICE + OAUTH_VERSION + "auth";
	//oauth2 token URL
	public static final String TOKEN_URL = HTTPS + BASE + SERVICE + OAUTH_VERSION + "token";
	
	//user[me]
	public static final String MY_USER_URL = HTTPS + BASE + API_VERSION + "user/~me";
	
	//get movie
	public static final String MOVIE_URL = HTTPS + API_BASE + API_VERSION + "movie/";
	
	//search movie
	public static final String SEARCH_URL = HTTPS + API_BASE + API_VERSION + "movie/search" ;
	
	//comments
	public static final String COMMENTS_URL = DoubanURL.MOVIE_URL + "subject/%s/comments";
	
	//celebrity
	public static final String CELEBRITY_URL = HTTPS + API_BASE + API_VERSION + "movie/celebrity/";
	
	//top250
	public static final String TOP_URL = HTTPS + API_BASE + API_VERSION + "movie/top250";
	
	//new_movie
	public static final String NEW_MOVIES_URL = HTTPS + API_BASE + API_VERSION + "movie/new_movies";
	
	//now playing
	public static final String NOW_PLAYING_URL = HTTPS + API_BASE + API_VERSION + "movie/nowplaying";
	
	//coming
	public static final String COMING_MOVIES_URL = HTTPS + API_BASE + API_VERSION + "movie/coming";
	
	//========================================================================================//
	//for oauth1
	public static final String REQUEST_TOKEN_URL = HTTP + BASE + SERVICE + "auth/request_token";
	public static final String AUTHORIZE_URL = HTTP + BASE + SERVICE + "auth/authorize";
	public static final String ACCESS_TOKEN_URL = HTTP + BASE + SERVICE + "auth/access_token"; 
	
	//for search without Api
	public static final String SEARCH_URL_NO_API=HTTP+MOVIE_BASE+"subject_search";
	public static final String TRAILER="/trailer#trailer";
	//parser Html
	//photo
	//预览图
	public static final String IMG_PHOTO = "https://img3.doubanio.com/view/photo/photo/public/p%1s.webp";
	//缩略图
	public static final String IMG_PHOTO_ICON = "https://img3.doubanio.com/view/photo/albumicon/public/p%1s.webp";
	//原图,需要验证所以暂不使用
	public static final String IMG_PHOTO_RAW = "https://img3.doubanio.com/view/photo/raw/public/p%1s.webp";
}