package com.hphtv.movielibrary.sqlite.bean.scraperBean;

import java.io.Serializable;

/**
 * 		电影影评（长评，大于140字的评论）
 */
public class Review implements Serializable{
	//打分
	private Rating rating;
	//更新时间
	private String update;
	//发表时间
	private String published;
	//作者
	private User author;
	//影评标题
	private String title;
	//赞同次数
	private int votes=0;
	//不赞同次数
	private int useless=0;
	//影评的回复数
	private int comments=0;
	//影评内容
	private String summary;
	//影评对应的电影
	private SimpleMovie movie;
	private String id;
	private String alt;
	
	public Review(){
		
	}
	
	public String toString(){
		return "[Review(title=" + title+ ")]"; 
	}
	
	
	public Rating getRating(){
		return rating;
	}
	
	public String getUpadate(){
		return update;
	}
	
	public String getPublished(){
		return published;
	}
	
	public User getAuthor(){
		return author;
	}
	
	public String getTitle(){
		return title;
	}
	
	public int getVotes(){
		return votes;
	}
	
	public int getUseless(){
		return useless;
	}
	
	public int getComments(){
		return comments;
	}
	
	public String getSummary(){
		return summary;
	}
	
	public SimpleMovie getMovie(){
		return movie;
	}
	
	public String getId(){
		return id;
	}
	
	public String getAlt(){
		return alt;
	}
	
}
