package com.kinglin.model;

import android.R.integer;

public class Configuration {

	long configurationId;//id¹Ì¶¨£¬ÒòÎªÖ»ÓÐÒ»¸ö¼ÇÂ¼
	String loginUser;  //µ±Ç°ÓÃ»§id£¬ÅÐ¶ÏÊÇ·ñÊÇµÇÂ½×´Ì¬,defaultÎªÎ´µÇÂ¼£¬ÆäËûÎªµÇÂ½
	int syncByWifi;  //1ÎªwifiÏÂÍ¬²½£¬0Îª²»Í¬²½
	int trackOrNot;  //1ÎªÂ·¾¶¸ú×Ù£¬0Îª²»¸ú×Ù
	int autoPush;

	String info;  //Èí¼þÏà¹ØÐÅÏ¢

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
