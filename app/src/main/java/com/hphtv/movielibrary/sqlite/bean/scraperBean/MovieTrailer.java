package com.hphtv.movielibrary.sqlite.bean.scraperBean;

public class MovieTrailer {
	private long movie_id;//所属电影id
	private String title;// 预告片名
	private String id;// 预告片id
	private String photo;// 视频图片
	private String duration;// 时长
	private String alt;// 预告片地址
	private String pub_date;//预告片日期



	public long getMovie_id() {
		return movie_id;
	}

	public void setMovie_id(long movie_id) {
		this.movie_id = movie_id;
	}
	public String getPub_date() {
		return pub_date;
	}

	public void setPub_date(String pub_date) {
		this.pub_date = pub_date;
	}



	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPhoto() {
		return photo;
	}

	public void setPhoto(String photo) {
		this.photo = photo;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public String getAlt() {
		return alt;
	}

	public void setAlt(String alt) {
		this.alt = alt;
	}


	@Override
	public String toString() {
		return "MovieTrailer [movie_id=" + movie_id + ", title=" + title
				+ ", id=" + id + ", photo=" + photo + ", duration=" + duration
				+ ", alt=" + alt + ", pub_date=" + pub_date + "]";
	}
}
