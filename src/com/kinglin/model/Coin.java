package com.kinglin.model;

public class Coin {

	long coinId;  //»ý·Ö²úÉúÊ±¼ä
	String time;  //»ý·Ö²úÉúÊ±¼ä£¬ºÍidÇø·Ö¿ª
	int grade;  //·ÖÖµ
	String content;  //»ý·ÖÄÚÈÝ
	
	public Coin() {
	}
	
	public Coin(long coinId,String time,int grade,String content) {
		this.coinId = coinId;
		this.time = time;
		this.grade = grade;
		this.content = content;
	}

	public long getCoinId() {
		return coinId;
	}

	public void setCoinId(long coinId) {
		this.coinId = coinId;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public int getGrade() {
		return grade;
	}

	public void setGrade(int grade) {
		this.grade = grade;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

}
