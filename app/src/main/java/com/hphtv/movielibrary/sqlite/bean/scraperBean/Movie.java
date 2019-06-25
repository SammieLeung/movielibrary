package com.hphtv.movielibrary.sqlite.bean.scraperBean;

import java.io.Serializable;
import java.util.Arrays;

public class Movie implements Serializable {
    private static final String TAG = "Movie";

    //条目id
    private long id;
    //电影id
    private String movie_id;
    //载体id
    private long wrapper_id=-1;
    //中文名
    private String title;
    //原名
    private String originalTitle;
    //条目页url
    private String alt;
    //电影海报图，分别提供288px x 465px(大)，96px x 155px(中) 64px x 103px(小)尺寸
    private Images images;
    //评分
    private Rating rating;
    //如果条目类型是电影则为上映日期，如果是电视剧则为首播日期
    private String[] pubDates;
    //年代
    private String year;
    //条目分类 选项看上面常量
    private String subtype;

    //==================以上是simple版的有效字段==================//
    //又名
    private String[] aka;
    //移动版条目页url
    private String mobileUrl;
    //评分人数
    private int ratingsCount = 0;
    //想看人数
    private int wishCount = 0;
    //看过人数
    private int collectCount = 0;
    //在看人数，如果是电视剧，默认值为0，如果是电影值为null
    private String doCount;
    //导演
    private Celebrity[] directors;
    //主演，最多可获得4个
    private Celebrity[] casts;
    //编剧
    private Celebrity[] writers;
    //官方网站
    private String website;
    //豆瓣小站
    private String doubanSite;
    //语言
    private String[] languages;
    //片长
    private String[] durations;
    //影片类型，最多提供3个
    private String[] genres;
    //制片国家/地区
    private String[] countries;
    //简介
    private String summary;
    //短评数量
    private int commentsCount = 0;
    //影评数量
    private int reviewsCount = 0;
    //总季数
    private String seasonsCount;
    //当前第几季(tv only)
    private String currentSeason;
    //集数
    private String episodes;
    //影讯页URL(movie only)
    private String scheduleUrl;
    //预告片URL，对高级用户以上开放，最多开放4个地址
    private String[] trailerUrls;
    //电影剧照，前10张
    private Photo[] photos;
    //影评，前10条
    private Review[] popularReviews;
    //	//本地路径
//	private String[] localPath;
//	//时间戳
    private String uptime;
    private String addtime;//添加电影时间戳
    //	//usb的id
    //标题拼音
    private String title_pinyin;

    private int api=0;


    public String getUptime() {
        return uptime;
    }

    public void setUptime(String uptime) {
        this.uptime = uptime;
    }

    public String getAddtime() {
        return addtime;
    }

    public void setAddtime(String addtime) {
        this.addtime = addtime;
    }

    public long getId() {
        return id;
    }

    public String getMovieId() {
        return movie_id;
    }

    public void setMovieId(String movie_id) {
        this.movie_id = movie_id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public String getAlt() {
        return alt;
    }

    public void setAlt(String alt) {
        this.alt = alt;
    }

    public long getWrapperId() {
        return wrapper_id;
    }

    public void setWrapper_id(long wrapper_id) {
        this.wrapper_id = wrapper_id;
    }

    @Override
    public String toString() {
        return "Movie{" +
                "id=" + id +
                ", movie_id='" + movie_id + '\'' +
                ", title='" + title + '\'' +
                ", originalTitle='" + originalTitle + '\'' +
                ", alt='" + alt + '\'' +
                ", images=" + images +
                ", rating=" + rating +
                ", pubDates=" + Arrays.toString(pubDates) +
                ", year='" + year + '\'' +
                ", subtype='" + subtype + '\'' +
                ", aka=" + Arrays.toString(aka) +
                ", mobileUrl='" + mobileUrl + '\'' +
                ", ratingsCount=" + ratingsCount +
                ", wishCount=" + wishCount +
                ", collectCount=" + collectCount +
                ", doCount='" + doCount + '\'' +
                ", directors=" + Arrays.toString(directors) +
                ", casts=" + Arrays.toString(casts) +
                ", writers=" + Arrays.toString(writers) +
                ", website='" + website + '\'' +
                ", doubanSite='" + doubanSite + '\'' +
                ", languages=" + Arrays.toString(languages) +
                ", durations=" + Arrays.toString(durations) +
                ", genres=" + Arrays.toString(genres) +
                ", countries=" + Arrays.toString(countries) +
                ", summary='" + summary + '\'' +
                ", commentsCount=" + commentsCount +
                ", reviewsCount=" + reviewsCount +
                ", seasonsCount='" + seasonsCount + '\'' +
                ", currentSeason='" + currentSeason + '\'' +
                ", episodes='" + episodes + '\'' +
                ", scheduleUrl='" + scheduleUrl + '\'' +
                ", trailerUrls=" + Arrays.toString(trailerUrls) +
                ", photos=" + Arrays.toString(photos) +
                ", popularReviews=" + Arrays.toString(popularReviews) +
                ", uptime='" + uptime + '\'' +
                ", addtime='" + addtime + '\'' +
                ", title_pinyin='" + title_pinyin + '\'' +
                ", api=" + api +
                '}';
    }

    public Images getImages() {
        return images;
    }

    public void setImages(Images images) {
        this.images = images;
    }

    public Rating getRating() {
        return rating;
    }

    public void setRating(Rating rating) {
        this.rating = rating;
    }

    public String[] getPubDates() {
        return pubDates;
    }

    public void setPubDates(String[] pubDates) {
        this.pubDates = pubDates;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getSubtype() {
        return subtype;
    }

    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }

    public String[] getAka() {
        return aka;
    }

    public void setAka(String[] aka) {
        this.aka = aka;
    }

    public String getMobileUrl() {
        return mobileUrl;
    }

    public void setMobileUrl(String mobileUrl) {
        this.mobileUrl = mobileUrl;
    }

    public int getRatingsCount() {
        return ratingsCount;
    }

    public void setRatingsCount(int ratingsCount) {
        this.ratingsCount = ratingsCount;
    }

    public int getWishCount() {
        return wishCount;
    }

    public void setWishCount(int wishCount) {
        this.wishCount = wishCount;
    }

    public int getCollectCount() {
        return collectCount;
    }

    public void setCollectCount(int collectCount) {
        this.collectCount = collectCount;
    }

    public String getDoCount() {
        return doCount;
    }

    public void setDoCount(String doCount) {
        this.doCount = doCount;
    }

    public Celebrity[] getDirectors() {
        return directors;
    }

    public void setDirectors(Celebrity[] directors) {
        this.directors = directors;
    }

    public Celebrity[] getCasts() {
        return casts;
    }

    public void setCasts(Celebrity[] casts) {
        this.casts = casts;
    }

    public Celebrity[] getWriters() {
        return writers;
    }

    public void setWriters(Celebrity[] writers) {
        this.writers = writers;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getDoubanSite() {
        return doubanSite;
    }

    public void setDoubanSite(String doubanSite) {
        this.doubanSite = doubanSite;
    }

    public String[] getLanguages() {
        return languages;
    }

    public void setLanguages(String[] languages) {
        this.languages = languages;
    }

    public String[] getDurations() {
        return durations;
    }

    public void setDurations(String[] durations) {
        this.durations = durations;
    }

    public String[] getGenres() {
        return genres;
    }

    public void setGenres(String[] genres) {
        this.genres = genres;
    }

    public String[] getCountries() {
        return countries;
    }

    public void setCountries(String[] countries) {
        this.countries = countries;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public int getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(int commentsCount) {
        this.commentsCount = commentsCount;
    }

    public int getReviewsCount() {
        return reviewsCount;
    }

    public void setReviewsCount(int reviewsCount) {
        this.reviewsCount = reviewsCount;
    }

    public String getSeasonsCount() {
        return seasonsCount;
    }

    public void setSeasonsCount(String seasonsCount) {
        this.seasonsCount = seasonsCount;
    }

    public String getCurrentSeason() {
        return currentSeason;
    }

    public String getEpisodes() {
        return episodes;
    }

    public void setEpisodes(String episodes) {
        this.episodes = episodes;
    }

    public void setCurrentSeason(String currentSeason) {
        this.currentSeason = currentSeason;
    }

    public String getScheduleUrl() {
        return scheduleUrl;
    }

    public void setScheduleUrl(String scheduleUrl) {
        this.scheduleUrl = scheduleUrl;
    }

    public String[] getTrailerUrls() {
        return trailerUrls;
    }

    public void setTrailerUrls(String[] trailerUrls) {
        this.trailerUrls = trailerUrls;
    }

    public Photo[] getPhotos() {
        return photos;
    }

    public void setPhotos(Photo[] photos) {
        this.photos = photos;
    }

    public Review[] getPopularReviews() {
        return popularReviews;
    }

    public void setPopularReviews(Review[] popularReviews) {
        this.popularReviews = popularReviews;
    }

    public String getTitlePinyin() {
        return title_pinyin;
    }

    public void setTitlePinyin(String title_pinyin) {
        this.title_pinyin = title_pinyin;
    }

    public int getApi() {
        return api;
    }

    public void setApi(int api) {
        this.api = api;
    }

}
