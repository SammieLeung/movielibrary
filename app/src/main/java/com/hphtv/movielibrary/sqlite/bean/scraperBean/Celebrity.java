package com.hphtv.movielibrary.sqlite.bean.scraperBean;

import com.hphtv.movielibrary.util.StrUtils;

import android.text.TextUtils;

import java.io.Serializable;

/**
 * 		影人信息（演员、导演、剧本作者等）
 */
public class Celebrity implements Serializable{
	private String id;
	private String name;
	//条目页URL
	private String alt;
	//影人头像，分别提供420px x 600px(大)，140px x 200px(中) 70px x 100px(小)尺寸
//	private String largeAvatar;
//	private String mediumAvatar;
//	private String smallAvatar;
	private Images avatars;
	//英文名
	private String name_en;
	//移动版条目页URL
	private String mobileUrl;
	//简介
	private String summary;
	//更多中文名
	private String[] aka;
	//更多英文名
	private String[] aka_en;
	//官方网站
	private String website;
	//性别
	private String gender;
	//生日
	private String birthday;
	//出生地
	private String bornPlace;
	//星座
	private String constellation;
	//影人剧照，最多10张
	private Photo[] photos;
	//职业
	private String[] professions;
	//影人作品，最多5部
	private SimpleMovie[] works;
	
	//imdb编号
	private String imdb_id;
	private String imdb_url;
	public Celebrity(){
		
	}
	
public Celebrity(String name ,String alt){
		this.name = name;
		this.alt = alt;
	}

	
	public String toString(){
		return "[Celebrity(name=" + name + ", alt=" + alt + ((avatars!= null)?(", avatars=" + avatars.toString()):"") +",imdb_url" +imdb_url+")]";
	}
	
	public String[] getAka(){
		return aka;
	}
	
	public String[] getAkaEn(){
		return aka_en;
	}
	
	public String getAlt(){
		return alt;
	}
 	
	public String getName_en() {
		return name_en;
	}


	public void setName_en(String name_en) {
		this.name_en = name_en;
	}


	public String[] getAka_en() {
		return aka_en;
	}


	public void setAka_en(String[] aka_en) {
		this.aka_en = aka_en;
	}


	public void setId(String id) {
		this.id = id;
	}


	public void setName(String name) {
		this.name = name;
	}


	public void setAlt(String alt) {
		this.alt = alt;
	}


	public void setMobileUrl(String mobileUrl) {
		this.mobileUrl = mobileUrl;
	}


	public void setSummary(String summary) {
		this.summary = summary;
	}


	public void setAka(String[] aka) {
		this.aka = aka;
	}


	public void setWebsite(String website) {
		this.website = website;
	}


	public void setGender(String gender) {
		this.gender = gender;
	}


	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}


	public void setBornPlace(String bornPlace) {
		this.bornPlace = bornPlace;
	}


	public void setConstellation(String constellation) {
		this.constellation = constellation;
	}


	public void setPhotos(Photo[] photos) {
		this.photos = photos;
	}


	public void setProfessions(String[] professions) {
		this.professions = professions;
	}


	public void setWorks(SimpleMovie[] works) {
		this.works = works;
	}


	public Images getAvatars() {
		return avatars;
	}


	public void setAvatars(Images avatars) {
		this.avatars = avatars;
	}


	public String getBirthday(){
		return birthday;
	}
	
	public String getBornPlace(){
		return bornPlace;
	}
	
	public String getConstellation(){
		return constellation;
	}
	
	public String getGender(){
		return gender;
	}
	
	public String getId(){
		if(TextUtils.isEmpty(id) && !TextUtils.isEmpty(alt))
		{
			String[] matches = StrUtils.matcher("movie.douban.com/celebrity/([0-9]+)/", alt);
			if(matches != null && matches.length > 1)
			{
				id = matches[1];
			}
		}
		return id;
	}
	
	public String getMobileUrl(){
		return mobileUrl;
	}
	
	public String getName(){
		return name;
	}
	
	public String getNameEn(){
		return name_en;
	}
	
	public Photo[] getPhotos(){
		return photos;
	}
	
	public String[] getProfessions(){
		return professions;
	}
	
	public SimpleMovie[] getWorks(){
		return works;
	}
	
	public String getSummary(){
		return summary;
	}
	
	public String getWebsite(){
		return website;
	}

	public String getImdb_id() {
		return imdb_id;
	}

	public void setImdb_id(String imdb_id) {
		this.imdb_id = imdb_id;
	}

	public String getImdb_url() {
		return imdb_url;
	}

	public void setImdb_url(String imdb_url) {
		this.imdb_url = imdb_url;
	}
	
}
