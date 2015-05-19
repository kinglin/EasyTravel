package com.kinglin.model;

import java.io.Serializable;


@SuppressWarnings("serial")
public class User implements Serializable{

	long userId;  //·þÎñÆ÷·µ»Ø
	int gender;//ÐÔ±ð£¬1£ºÄÐ£¬0£ºÅ®
	String username;
	String password;
	String picture;  //Í·ÏñÂ·¾¶
	String birthday;  
	String hobby;
	String friends;  //ºÃÓÑÁÐ±í£¬Ö®¼äÓÃ·ÖºÅ¸ô¿ª
	long lastChangeTime;
	int operation;//0£ºÒÑÍ¬²½1£ºÐÂÔö2£ºÐÞ¸Ä3£ºÉ¾³ý

	public User(){
		
	}
	
	public User(long userId,int gender,String username,String password,String picture,String birthday,String hobby,String friends,long lastChangeTime,int operation) {
		this.userId = userId;
		this.gender = gender;
		this.username = username;
		this.password = password;
		this.picture = picture;
		this.birthday = birthday;
		this.hobby = hobby;
		this.friends = friends;
		this.lastChangeTime = lastChangeTime;
		this.operation = operation;
	}

	public int getGender() {
		return gender;
	}

	public void setGender(int gender) {
		this.gender = gender;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}


	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPicture() {
		return picture;
	}

	public void setPicture(String picture) {
		this.picture = picture;
	}

	public String getBirthday() {
		return birthday;
	}

	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}

	public String getHobby() {
		return hobby;
	}

	public void setHobby(String hobby) {
		this.hobby = hobby;
	}

	public String getFriends() {
		return friends;
	}

	public void setFriends(String friends) {
		this.friends = friends;
	}

	public long getLastChangeTime() {
		return lastChangeTime;
	}

	public void setLastChangeTime(long lastChangeTime) {
		this.lastChangeTime = lastChangeTime;
	}

	public int getOperation() {
		return operation;
	}

	public void setOperation(int operation) {
		this.operation = operation;
	}



}
