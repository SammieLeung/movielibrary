package com.hphtv.movielibrary.sqlite.bean.scraperBean;

import java.io.Serializable;

/**
 * 		图片属性
 */
public class Photo implements Serializable{
	//图片id
	private String id;
	//条目id
	private String subjectId;
	//图片展示页url
	private String alt;
	//图片地址，icon尺寸
	private String icon;
	//图片地址，image尺寸
	private String imageUrl;
	//图片地址，thumb尺寸
	private String thumb;
	//封面
	private String cover;
	//创建时间
	private String createdAt;
	//描述
	private String desc;
	//上传用户
	private User author;
	//相册id	
	private String albumId;
	//相册标题
	private String albumTitle;
	//相册地址
	private String albumUrl;
	//下一张图片
	private String nextPhotoId;
	//上一张图片
	private String prevPhotoId;
	//图片在相册中的位置，按照时间排序
	private int position=0;
	//评论数
	private int commentsCount=0;
	//评论数
	private int photosCount=0;
	//全部剧照数量
	private int recsCount=0;
	
	public Photo() {
		super();
	}


	public Photo(String url){
		imageUrl = url;
	}
	
	
	public String toString(){
		return "[Photo(url=" + imageUrl + ")]";
	}
	
	public String getAlbumId(){
		return albumId;
	}
	
	public String getAlbumTitle(){
		return albumTitle;
	}
	
	public String getAlbumUrl(){
		return albumUrl;
	}
	
	public String getAlt(){
		return alt;
	}
	
	public User getAuthor(){
		return author;
	}
	
	public int getCommentsCount(){
		return commentsCount;
	}
	
	public String getCover(){
		return cover;
	}
	
	public String getCreatedAt(){
		return createdAt;
	}
	
	public String getDesc(){
		return desc;
	}
	
	public String getIcon(){
		return icon;
	}
	
	public String getId(){
		return id;
	}
	
	public String getImageUrl(){
		return imageUrl;
	}
	
	public String getNextPhotoId(){
		return nextPhotoId;
	}
	
	public int getPhotosCount(){
		return photosCount;
	}
	
	public int getPosition(){
		return position;
	}
	
	public String getPrevPhotoId(){
		return prevPhotoId;
	}
	
	public int getResCount(){
		return recsCount;
	}
	
	public String getSubjectId(){
		return subjectId;
	}
	
	public String getThumb(){
		return thumb;
	}


	public int getRecsCount() {
		return recsCount;
	}


	public void setRecsCount(int recsCount) {
		this.recsCount = recsCount;
	}


	public void setId(String id) {
		this.id = id;
	}


	public void setSubjectId(String subjectId) {
		this.subjectId = subjectId;
	}


	public void setAlt(String alt) {
		this.alt = alt;
	}


	public void setIcon(String icon) {
		this.icon = icon;
	}


	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}


	public void setThumb(String thumb) {
		this.thumb = thumb;
	}


	public void setCover(String cover) {
		this.cover = cover;
	}


	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}


	public void setDesc(String desc) {
		this.desc = desc;
	}


	public void setAuthor(User author) {
		this.author = author;
	}


	public void setAlbumId(String albumId) {
		this.albumId = albumId;
	}


	public void setAlbumTitle(String albumTitle) {
		this.albumTitle = albumTitle;
	}


	public void setAlbumUrl(String albumUrl) {
		this.albumUrl = albumUrl;
	}


	public void setNextPhotoId(String nextPhotoId) {
		this.nextPhotoId = nextPhotoId;
	}


	public void setPrevPhotoId(String prevPhotoId) {
		this.prevPhotoId = prevPhotoId;
	}


	public void setPosition(int position) {
		this.position = position;
	}


	public void setCommentsCount(int commentsCount) {
		this.commentsCount = commentsCount;
	}


	public void setPhotosCount(int photosCount) {
		this.photosCount = photosCount;
	}
	
}
