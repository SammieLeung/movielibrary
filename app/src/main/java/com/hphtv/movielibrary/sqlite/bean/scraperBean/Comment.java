package com.hphtv.movielibrary.sqlite.bean.scraperBean;

/**
 * 		电影短评
 */
public class Comment {
	//短评id
	public String id;
	//创建时间
	public String createdAt;
	//电影id
	public String subjectId;
	//短评作者
	public User author;
	//短评内容,140字以内
	public String content;
	//评分
	public Rating rating;
	//有用数
	public int usefulCount;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	public String getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(String subjectId) {
		this.subjectId = subjectId;
	}

	public User getAuthor() {
		return author;
	}

	public void setAuthor(User author) {
		this.author = author;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Rating getRating() {
		return rating;
	}

	public void setRating(Rating rating) {
		this.rating = rating;
	}

	public int getUsefulCount() {
		return usefulCount;
	}

	public void setUsefulCount(int usefulCount) {
		this.usefulCount = usefulCount;
	}

	@Override
	public String toString() {
		return "Comment [id=" + id + ", createdAt=" + createdAt
				+ ", subjectId=" + subjectId + ", author=" + author
				+ ", content=" + content + ", rating=" + rating
				+ ", usefulCount=" + usefulCount + "]";
	}
	
}
