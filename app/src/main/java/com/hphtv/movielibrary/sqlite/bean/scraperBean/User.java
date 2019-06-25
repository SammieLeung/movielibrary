package com.hphtv.movielibrary.sqlite.bean.scraperBean;

import java.io.Serializable;

/**
 * 		用户
 */
public class User implements Serializable{
	//个人主页url
	private String alt;
	//头像小图
	private String avatar;
	private String id;
	private String name;
	private String signature;
	private String uid;
	
	public User(){
		
	}
	
	public String toString(){
		return "[User(name="+name+")]";
	}
	
	public String getAlt(){
		return alt;
	}
	public String getAvatar(){
		return avatar;
	}
	public String getId(){
		return id;
	}
	public String getName(){
		return name;
	}
	public String getSignature(){
		return signature;
	}
	public String getUid(){
		return uid;
	}
}
