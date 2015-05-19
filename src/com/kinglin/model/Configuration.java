package com.kinglin.model;

public class Configuration {

	long configurationId;//用户配置id
	String loginUser;  //当前登陆用户的id
	int syncByWifi;  //1为wifi下同步
	int trackOrNot;  //1为开启路径跟踪
	int autoPush;   //1为开启宝藏推送

	String info;  //存放软件信息

	int changed;
	
	public Configuration() {
	
	}

	public Configuration(long configurationId, String loginUser, int syncByWifi, int trackOrNot,int autoPush, String info,int changed) {
		this.configurationId = configurationId;
		this.loginUser = loginUser;
		this.syncByWifi=syncByWifi;
		this.trackOrNot=trackOrNot;
		this.autoPush = autoPush;
		this.info=info;
		this.changed = changed;
	}

	
	public long getConfigurationId() {
		return configurationId;
	}


	public void setConfigurationId(long configurationId) {
		this.configurationId = configurationId;
	}


	public int getSyncByWifi() {
		return syncByWifi;
	}

	public void setSyncByWifi(int syncByWifi) {
		this.syncByWifi = syncByWifi;
	}

	public int getTrackOrNot() {
		return trackOrNot;
	}

	public void setTrackOrNot(int trackOrNot) {
		this.trackOrNot = trackOrNot;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public String getLoginUser() {
		return loginUser;
	}

	public void setLoginUser(String loginUser) {
		this.loginUser = loginUser;
	}

	public int getChanged() {
		return changed;
	}

	public void setChanged(int changed) {
		this.changed = changed;
	}
	public int getAutoPush() {
		return autoPush;
	}

	public void setAutoPush(int autoPush) {
		this.autoPush = autoPush;
	}
   
}
