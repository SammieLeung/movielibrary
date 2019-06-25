package com.hphtv.movielibrary.sqlite.bean.scraperBean;

import java.io.Serializable;

public class Images implements Serializable{
    public String small;   // small image url
    public String medium;  // medium image url
    public String large;   // large image url\
    
	
	public Images() {
		super();
	}
	public Images(String small, String medium, String large) {
		super();
		this.small = small;
		this.medium = medium;
		this.large = large;
	}
	@Override
	public String toString() {
		return "Images [small=" + small + ", large=" + large + ", medium="
				+ medium + "]";
	}
	public String getSmall() {
		return small;
	}
	public void setSmall(String small) {
		this.small = small;
	}
	public String getLarge() {
		return large;
	}
	public void setLarge(String large) {
		this.large = large;
	}
	public String getMedium() {
		return medium;
	}
	public void setMedium(String medium) {
		this.medium = medium;
	}
	
    
    
}
