package com.kinglin.model;

import java.io.Serializable;


@SuppressWarnings("serial")
public class User implements Serializable{

	long userId;  //用户id，为注册时间
	int gender;//性别，1为男，0为女
	String username;
	String password;
	String picture;  //用户头像路径
	String birthday;  
	String hobby;
	String friends;  //用户好友列表
	long lastChangeTime;
	int operation;//对用户信息的操作，0为无操作，1为添加，2为修改

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
