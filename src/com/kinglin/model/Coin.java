package com.kinglin.model;

public class Coin {

	long coinId;  //积分项的id
	String time;  //积分获取时间
	int grade;  //积分分值
	String content;  //积分内容
	
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
