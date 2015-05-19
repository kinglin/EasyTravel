package com.kinglin.model;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Treasure implements Serializable{
	
	long treasureId;  //宝藏id
	String time;  //宝藏发现时间
	String content;  //宝藏内容
	String title;
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	Double latitude;
	Double longitude;
    int isChecked;
	public int getIsChecked() {
		return isChecked;
	}

	public void setIsChecked(int isChecked) {
		this.isChecked = isChecked;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double altitude) {
		this.latitude = altitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public Treasure() {
	}

	public Treasure(long treasureId,String time,String content,double latitude,double longitude,String title,int isChecked) {
		this.treasureId = treasureId;
		this.time = time;
		this.content = content;
		this.latitude=latitude;
		this.longitude=longitude;
		this.title=title;
		this.isChecked=isChecked;
	}

	public long getTreasureId() {
		return treasureId;
	}


	public void setTreasureId(long treasureId) {
		this.treasureId = treasureId;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

}
