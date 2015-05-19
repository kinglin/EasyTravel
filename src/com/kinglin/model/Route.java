package com.kinglin.model;

public class Route {

	long id;  //路径id
	double latitude;  //经度
	double longtitude;  //纬度
	String date;
	int changed;
	
	public Route() {
	}
	
	public Route(long id,double latitude,double longtitude,String date,int changed) {
		this.id = id;
		this.latitude = latitude;
		this.longtitude = longtitude;
		this.date = date;
		this.changed = changed;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongtitude() {
		return longtitude;
	}

	public void setLongtitude(double longtitude) {
		this.longtitude = longtitude;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public int getChanged() {
		return changed;
	}

	public void setChanged(int changed) {
		this.changed = changed;
	}
}
