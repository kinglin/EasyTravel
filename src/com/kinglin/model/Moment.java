package com.kinglin.model;

public class Moment {
	
	long momentId;  //保存的时间
	long userId;  //记事所属于的用户
	String userName;  //用户名
	String time;  //保存的时间，数值和id相等，以免混淆
	int weather;  //1~4代表不同的天气
	String text;  //文字
	String pictures;  //图片的路径，之间用分号隔开
	String voice;  //语音的路径
	String locationx;  //经度
	String locationy;  //纬度
	String video;  //视频的路径

	public Moment() {
	}


	public long getMomentId() {
		return momentId;
	}


	public void setMomentId(long momentId) {
		this.momentId = momentId;
	}


	public long getUserId() {
		return userId;
	}


	public void setUserId(long userId) {
		this.userId = userId;
	}


	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public int getWeather() {
		return weather;
	}

	public void setWeather(int weather) {
		this.weather = weather;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getPictures() {
		return pictures;
	}

	public void setPictures(String pictures) {
		this.pictures = pictures;
	}

	public String getVoice() {
		return voice;
	}

	public void setVoice(String voice) {
		this.voice = voice;
	}

	public String getLocationx() {
		return locationx;
	}

	public void setLocationx(String locationx) {
		this.locationx = locationx;
	}

	public String getLocationy() {
		return locationy;
	}

	public void setLocationy(String locationy) {
		this.locationy = locationy;
	}

	public String getVideo() {
		return video;
	}

	public void setVideo(String video) {
		this.video = video;
	}

}
