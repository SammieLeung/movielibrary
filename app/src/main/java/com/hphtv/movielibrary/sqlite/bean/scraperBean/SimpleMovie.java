package com.hphtv.movielibrary.sqlite.bean.scraperBean;

import java.io.Serializable;
import java.util.Arrays;

/**
 * 电影entity
 */
public class SimpleMovie implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// private static final String TAG = "Movie";
	// 条目id
	private String id;
	// 中文名
	private String title;
	// 原名
	private String original_title;
	// 条目页url
	private String alt;
	// 电影海报图，分别提供288px x 465px(大)，96px x 155px(中) 64px x 103px(小)尺寸
	private Images images;
	// 评分
	private Rating rating;
	// 如果条目类型是电影则为上映日期，如果是电视剧则为首播日期
	private String[] pubDates;
	// 年代
	private String year;
	// 条目分类, movie或者tv
	private String subtype;

	public String[] genres;
	//描述
	public String[] abstracts;
	//
	public String ratingsCounts;
	public SimpleMovie() {

	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getOriginalTitle() {
		return original_title;
	}

	public void setOriginalTitle(String originalTitle) {
		this.original_title = originalTitle;
	}

	public String getAlt() {
		return alt;
	}

	public void setAlt(String alt) {
		this.alt = alt;
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

	public String[] getGenres() {
		return genres;
	}

	public void setGenres(String[] genres) {
		this.genres = genres;
	}

	public String[] getAbstracts() {
		return abstracts;
	}

	public void setAbstracts(String[] abstracts) {
		this.abstracts = abstracts;
	}
	
	public String getRatingsCounts() {
		return ratingsCounts;
	}
	
	public void setRatingsCounts(String ratingsCounts) {
		this.ratingsCounts = ratingsCounts;
	}

	@Override
	public String toString() {
		return "SimpleMovie [id=" + id + ", title=" + title
				+ ", original_title=" + original_title + ", alt=" + alt
				+ ", images=" + images + ", rating=" + rating +  ", ratingsCounts=" + ratingsCounts +", pubDates="
				+ Arrays.toString(pubDates) + ", year=" + year + ", subtype="
				+ subtype + ", genres=" + Arrays.toString(genres) + ", abstracts=" + Arrays.toString(abstracts) +"]";
	}

}
