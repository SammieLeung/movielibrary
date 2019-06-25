package com.hphtv.movielibrary.scraper.douban.response;

import java.util.Arrays;

import com.hphtv.movielibrary.sqlite.bean.scraperBean.Comment;
import com.hphtv.movielibrary.sqlite.bean.scraperBean.SimpleMovie;


/**
 *        	需要登录
 * 		调用获取短评接口返回的数据结构
 */
public class CommentResponse{
	public int start;
	public int count;
	public int total;
	public SimpleMovie subject;
	public Comment[] comments;
	public int getStart() {
		return start;
	}
	public void setStart(int start) {
		this.start = start;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public int getTotal() {
		return total;
	}
	public void setTotal(int total) {
		this.total = total;
	}
	public SimpleMovie getSubject() {
		return subject;
	}
	public void setSubject(SimpleMovie subject) {
		this.subject = subject;
	}
	public Comment[] getComments() {
		return comments;
	}
	public void setComments(Comment[] comments) {
		this.comments = comments;
	}
	@Override
	public String toString() {
		return "CommentResponse [start=" + start + ", count=" + count
				+ ", total=" + total + ", subject=" + subject + ", comments="
				+ Arrays.toString(comments) + "]";
	}
	

	
}
