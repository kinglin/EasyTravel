package com.kinglin.model;

public class Moment {
	
	long momentId;  //�����ʱ��
	long userId;  //���������ڵ��û�
	String userName;  //�û���
	String time;  //�����ʱ�䣬��ֵ��id��ȣ��������
	int weather;  //1~4����ͬ������
	String text;  //����
	String pictures;  //ͼƬ��·����֮���÷ֺŸ���
	String voice;  //������·��
	String locationx;  //����
	String locationy;  //γ��
	String video;  //��Ƶ��·��

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
