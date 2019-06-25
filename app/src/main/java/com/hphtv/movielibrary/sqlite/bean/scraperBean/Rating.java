package com.hphtv.movielibrary.sqlite.bean.scraperBean;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 		电影评分）
 */
public class Rating implements Serializable{
	//最大值（目前5）
	public int max=10;
	//最小指（目前0）
	public int min=0;
	//评分值
	public float average=0;
	
	public String stars;
	
	public Rating(){
		
	}
	public Rating(JSONObject json) throws JSONException{
		init(json);
	}
	
	public String toString(){
		return "[Rating(max=" + max + ", min=" + min + ", average=" + average + ", stars=" + stars +")]"; 
	}
	
	public void init(JSONObject json) throws JSONException{
		/*if( !json.isNull("max_rating") ){
			max = json.getInt("max_rating");
		}
		else*/ if( !json.isNull("max") ){
			max = json.getInt("max");
		}
		
		/*if( !json.isNull("min_rating") ){
			min = json.getInt("min_rating");
		}
		else*/ if( !json.isNull("min") ){
			min = json.getInt("min");
		}
		
		if( !json.isNull("value") ){
			average = json.getInt("value");
		}
		else if( !json.isNull("average") ){
			average = json.getLong("average");
		}
		
		if( !json.isNull("stars") ){
			stars = json.getString("stars");
		}
	}
}
