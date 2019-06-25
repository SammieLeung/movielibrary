package com.hphtv.movielibrary.scraper.douban.response;

import java.util.Arrays;
import com.hphtv.movielibrary.sqlite.bean.scraperBean.SimpleMovie;

/**
 * 
 * @author 
 * @date
 * @desc 搜索API返回的数据结构
 * 
 */
public class SearchResponse{
	public String query;
	public String tag;
	public int start;
	public int count;
	public int total;
	public SimpleMovie[] subjects;
	
	
//	public SearchResult(JSONObject json) throws JSONException{
//		init(json);
//	}
//	
//	public void init(JSONObject json) throws JSONException{
//		if( json.containsKey("query") ){
//			query = json.getString("query");
//		}
//		
//		if( json.containsKey("tag") ){
//			tag = json.getString("tag");
//		}
//		
//		if( json.containsKey("start") ){
//			start = json.getIntValue("start");
//		}
//		
//		if( json.containsKey("count") ){
//			count = json.getIntValue("count");
//		}
//		
//		if( json.containsKey("total") ){
//			total = json.getIntValue("total");
//		}
//		
//		if( json.containsKey("subjects") ){
//			JSONArray jsonArray = new JSONArray(json.getString("subjects"));
//			int size = jsonArray.length();
//			subjects = new Movie[size];
//			for(int i=0; i<size; i++){
//				subjects[i] = new Movie(jsonArray.getJSONObject(i), true); 
//			}
//		}
//	}
	
	public String getKeyword(){
		return query;
	}
	
	public String getTag(){
		return tag;
	}
	
	public int getStart(){
		return start;
	}
	
	public int getCount(){
		return count;
	}
	
	public int getTotal(){
		return total;
	}
	
	public SimpleMovie[] getSubjects(){
		return subjects;
	}

	@Override
	public String toString() {
		return "SearchResult [query=" + query + ", tag=" + tag + ", start="
				+ start + ", count=" + count + ", total=" + total
				+ ", subjects=" + Arrays.toString(subjects) + "]";
	}
	
	
}
