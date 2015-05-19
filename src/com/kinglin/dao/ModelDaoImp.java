package com.kinglin.dao;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.kinglin.model.Coin;
import com.kinglin.model.Configuration;
import com.kinglin.model.Note;
import com.kinglin.model.Route;
import com.kinglin.model.Treasure;
import com.kinglin.model.User;

public class ModelDaoImp implements ModelDao {

	SQLiteDatabase db;
	
	public ModelDaoImp(SQLiteDatabase db) {
		this.db = db;
	}

	//»ñµÃÓÃ»§µÄÉèÖÃÎÄ¼þ
	public Configuration getUserConfiguration() {//»ñµÃµ±Ç°µÄÅäÖÃÐÅÏ¢£¬ÒÔConfiguration¶ÔÏó·µ»Ø

		Configuration configuration = null;
		Cursor cursor=db.rawQuery("select * from configuration where configurationId=?",new String[]{"1"});//Ä¬ÈÏConfiguration±íÎ¨Ò»¼ÇÂ¼µÄconfigurationIdÎª1£¬¿ÉÒÔ¸Ä
		if (cursor.moveToFirst()) {
			long configurationId=cursor.getLong(cursor.getColumnIndex("configurationId"));
			String loginUser=cursor.getString(cursor.getColumnIndex("loginUser"));
			int syncByWifi=cursor.getInt(cursor.getColumnIndex("syncByWifi"));
			int trackOrNot=cursor.getInt(cursor.getColumnIndex("trackOrNot"));
			int autoPush=cursor.getInt(cursor.getColumnIndex("autoPush"));
			String info=cursor.getString(cursor.getColumnIndex("info"));
			int changed = cursor.getInt(cursor.getColumnIndex("changed"));
			configuration = new Configuration(configurationId, loginUser,syncByWifi,trackOrNot,autoPush,info,changed);
		}
		cursor.close();
	    return configuration;
	}

	@Override
	public void setUserConfiguration(Configuration configuration) {//½«´«ÈëµÄconfiguration¶ÔÏó±£´æµ½±í
		db.execSQL("update configuration set loginUser=?,syncByWifi=?,trackOrNot=?,autoPush=?,info=?,changed=? where configurationId=?",
				new Object[]{configuration.getLoginUser(),configuration.getSyncByWifi(),configuration.getTrackOrNot(),configuration.getAutoPush(),configuration.getInfo(),configuration.getChanged(),configuration.getConfigurationId()});
	}
	
	public void saveConfiguration(Configuration configuration){
		db.execSQL("insert into configuration(configurationId,loginUser,syncByWifi,trackOrNot,autoPush,info,changed) values(?,?,?,?,?,?,?)",
				new Object[]{configuration.getConfigurationId(),
				configuration.getLoginUser(),configuration.getSyncByWifi(),
				configuration.getTrackOrNot(),configuration.getAutoPush(),configuration.getInfo(),configuration.getChanged()});
	}

	@Override
	public void addNote(Note note) {//Ìí¼ÓÒ»Ìõ¼ÇÊÂ¼ÇÂ¼
		db.execSQL("insert into note(noteId,time,permission,weather,text,title,pictures,voice,locationx,locationy,video,lastChangeTime,operation) values(?,?,?,?,?,?,?,?,?,?,?,?,?)",
				new Object[]{note.getNoteId(),note.getTime(),note.getPermission(),note.getWeather(),note.getText(),note.getTitle(),note.getPictures(),note.getVoice(),note.getLocationx(),note.getLocationy(),note.getVideo(),note.getLastChangeTime(),note.getOperation()});
		
	}

	@Override
	public List<Note> getAllNotes() {//²éÑ¯ËùÓÐ¼ÇÂ¼
		List<Note> notes=new ArrayList<Note>();
		Cursor cursor=db.rawQuery("select * from note where operation<>4 order by noteId asc",null);
		while (cursor.moveToNext()) {
			long noteId=cursor.getLong(cursor.getColumnIndex("noteId"));
			String time=cursor.getString(cursor.getColumnIndex("time"));
			int permission=cursor.getInt(cursor.getColumnIndex("permission"));
			int weather=cursor.getInt(cursor.getColumnIndex("weather"));
			String text=cursor.getString(cursor.getColumnIndex("text"));
			String title=cursor.getString(cursor.getColumnIndex("title"));
			String pictures=cursor.getString(cursor.getColumnIndex("pictures"));
			String voice=cursor.getString(cursor.getColumnIndex("voice"));
			double locationx=cursor.getDouble(cursor.getColumnIndex("locationx"));
			double locationy=cursor.getDouble(cursor.getColumnIndex("locationy"));
			String video=cursor.getString(cursor.getColumnIndex("video"));
			long lastChangeTime=cursor.getLong(cursor.getColumnIndex("lastChangeTime"));
			int operation=cursor.getInt(cursor.getColumnIndex("operation"));
			notes.add(new Note(noteId,time,permission,weather,text,title,pictures,voice,locationx,locationy,video,lastChangeTime,operation));
		}
		cursor.close();
		return notes;
	}

	@Override
	public void delete_note(Note note) {
		
	}


	@Override
	//¸ù¾Ý´«ÈëµÄnoteµÄnoteIdÉ¾³ý¼ÇÂ¼
	public void deleteNote(Note note) {
		db.execSQL("delete from note where noteId=?",
				new Object[]{note.getNoteId()});
	}
	
	@Override
	public void updateNote(Note note) {//¸ù¾Ý´«ÈëµÄnoteµÄnoteId¸ü¸Ä¼ÇÂ¼
		db.execSQL("update note set time=?,permission=?,weather=?,text=?,pictures=?,voice=?,locationx=?,locationy=?,video=?,lastChangeTime=?,operation=? where noteId=?",
				new Object[]{note.getTime(),note.getPermission(),note.getWeather(),note.getText(),note.getPictures(),note.getVoice(),note.getLocationx(),note.getLocationy(),note.getVideo(),note.getLastChangeTime(),2,note.getNoteId()});
	}

	@Override
	public Note findNoteByPosition(int position) {
		int offset=position-1;
		List<Note> notes=new ArrayList<Note>();
		Cursor cursor=db.rawQuery("select * from note order by noteId asc limit ?,?",
				 new String[]{String.valueOf(offset),"1"});
		while (cursor.moveToNext()) {
			long noteId=cursor.getLong(cursor.getColumnIndex("noteId"));
			String time=cursor.getString(cursor.getColumnIndex("time"));
			int permission=cursor.getInt(cursor.getColumnIndex("permission"));
			int weather=cursor.getInt(cursor.getColumnIndex("weather"));
			String text=cursor.getString(cursor.getColumnIndex("text"));
			String title=cursor.getString(cursor.getColumnIndex("title"));
			String pictures=cursor.getString(cursor.getColumnIndex("pictures"));
			String voice=cursor.getString(cursor.getColumnIndex("voice"));
			double locationx=cursor.getDouble(cursor.getColumnIndex("locationx"));
			double locationy=cursor.getDouble(cursor.getColumnIndex("locationy"));
			String video=cursor.getString(cursor.getColumnIndex("video"));
			long lastChangeTime=cursor.getLong(cursor.getColumnIndex("lastChangeTime"));
			int operation=cursor.getInt(cursor.getColumnIndex("operation"));
			notes.add(new Note(noteId,time,permission,weather,text,title,pictures,voice,locationx,locationy,video,lastChangeTime,operation));
		}
		cursor.close();
		return notes.get(0);
	}

	//´«ÈëÓÃ»§id£¬µ½defaultÊý¾Ý¿âµÄuser±íÖÐ²éÕÒÊÇ·ñÓÐ¸ÃÓÃ»§´æÔÚ£¨´ËÊ±ÒÑ¾­ÔÚdefaultÊý¾Ý¿âÖÐ£©£¬Ã»ÓÐ·µ»Øfalse²¢²åÈë£¬ÓÐÔò·µ»Øtrue
	public boolean hasBeenLogedIn(long userId) {
		Cursor cursor=db.rawQuery("select * from user where userId=?",
				 new String[]{String.valueOf(userId)});
		if (cursor.getCount()==0) {
			return false;
		}
		return true;
	}

	//Õâ¸öº¯ÊýÐèÒªÂÞ¾êÍ¬Ñ§ºÍÁÖ²ßÍ¬Ñ§¹²Í¬Íê³É
	//ÕâÀïÊÇ»ñµÃ¸ÃÓÃ»§ËùÓÐÊý¾ÝµÄidºÍ×îºóÐÞ¸ÄÊ±¼ä£¬²¢´ò°ü³Éjson·µ»Ø
	@Override
	public JSONObject getAllDataSnapshot() {
		return null;
	}

	//Õâ¸öº¯ÊýÐèÒªÂÞ¾êÍ¬Ñ§ºÍÁÖ²ßÍ¬Ñ§¹²Í¬Íê³É
	//ÕâÀïÊÇ´«ÈëÐÂµÇÂ¼µÄÓÃ»§µÄËùÓÐÊý¾Ý£¬ÐèÒª½âÎöjson£¬È»ºó½«Êý¾Ý´æµ½Êý¾Ý¿â£¬±£´æ³É¹¦·µ»Øtrue
	@Override
	public boolean saveNewUserAllData(JSONObject json_allUserData) {
		try {
			if (json_allUserData.getString("getResult").equals("yes")) {
				
				//ÕâÀï»ñÈ¡userÐÅÏ¢²¢±£´æ
				JSONObject json_user = json_allUserData.getJSONObject("user");
				User user = new User(json_user.getLong("userId"), 
						json_user.getInt("gender"), 
						json_user.getString("username"), 
						json_user.getString("password"), 
						json_user.getString("picture"), 
						json_user.getString("birthday"), 
						json_user.getString("hobby"), 
						json_user.getString("friends"), 
						json_user.getLong("lastChangeTime"), 
						json_user.getInt("operation"));
				saveUser(user);
				
				//ÕâÀï»ñÈ¡configuration²¢±£´æ
				JSONObject json_conf = json_allUserData.getJSONObject("configuration");
				Configuration configuration = new Configuration(
						json_conf.getLong("configurationId"), 
						json_conf.getString("loginUser"), 
						json_conf.getInt("syncByWifi"), 
						json_conf.getInt("trackOrNot"), 
						json_conf.getInt("autoPush"),
						json_conf.getString("info"), 
						json_conf.getInt("changed"));
				setUserConfiguration(configuration);
				
				
				//ÕâÀï»ñÈ¡notes²¢±£´æ
				
//				if (!json_allUserData.isNull("routes")) {
//					JSONArray jsonArray_routes = json_allUserData.getJSONArray("routes");
//					JSONObject json_route = new JSONObject();
//					Route route = new Route();
//					for (int i = 0; i < jsonArray_routes.length(); i++) {
//						json_route = jsonArray_routes.getJSONObject(i);
//						
//						route.setId(json_route.getLong("id"));
//						route.setDate(json_route.getString("date"));
//						route.setLatitude(json_route.getDouble("latitude"));
//						route.setLongtitude(json_route.getDouble("longtitude"));
//						route.setChanged(0);
//						
//						saveRoute(route);
//					}
//				}
				
				if (!json_allUserData.isNull("notes")) {
					JSONArray jsonArray_notes = json_allUserData.getJSONArray("notes");
					List<Note> notes = new ArrayList<Note>();
					
					for (int i = 0; i < jsonArray_notes.length(); i++) {
						JSONObject json_note = jsonArray_notes.getJSONObject(i);
						Note note = new Note(json_note.getLong("noteId"), 
								json_note.getString("time"), 
								json_note.getInt("permission"), 
								json_note.getInt("weather"), 
								json_note.getString("text"), 
								json_note.getString("title"), 
								json_note.getString("pictures"), 
								json_note.getString("voice"), 
								json_note.getDouble("locationx"), 
								json_note.getDouble("locationy"), 
								json_note.getString("video"), 
								json_note.getLong("lastChangeTime"), 
								json_note.getInt("operation"));
						notes.add(note);
					}
					saveAllNotes(notes);
				}
				
				//ÕâÀï»ñÈ¡routes²¢±£´æ
//				if (!json_allUserData.isNull("routes")) {
//					JSONArray jsonArray_routes = json_allUserData.getJSONArray("routes");
//					List<Route> routes = new ArrayList<Route>();
//					for (int i = 0; i < jsonArray_routes.length(); i++) {
//						JSONObject json_route = jsonArray_routes.getJSONObject(i);
//						Route route = new Route(json_route.getLong("id"), 
//								json_route.getDouble("latitude"), 
//								json_route.getDouble("longtitude"),
//								json_route.getString("date"),0);
//						routes.add(route);
//					}
//					saveAllRoutes(routes);
//				}
				
				if (!json_allUserData.isNull("routes")) {
					JSONArray jsonArray_routes = json_allUserData.getJSONArray("routes");
					JSONObject json_route = new JSONObject();
					Route route = new Route();
					for (int i = 0; i < jsonArray_routes.length(); i++) {
						json_route = jsonArray_routes.getJSONObject(i);
						
						route.setId(json_route.getLong("id"));
						route.setChanged(0);
						route.setDate(json_route.getString("date"));
						route.setLatitude(json_route.getDouble("latitude"));
						route.setLongtitude(json_route.getDouble("longtitude"));
						
						saveRoute(route);
					}
				}
				
				
				
				//ÕâÀï»ñÈ¡coins²¢±£´æ
				if (!json_allUserData.isNull("coins")) {
					JSONArray jsonArray_coins = json_allUserData.getJSONArray("coins");
					List<Coin> coins = new ArrayList<Coin>();
					for (int i = 0; i < jsonArray_coins.length(); i++) {
						JSONObject json_coin = jsonArray_coins.getJSONObject(i);
						Coin coin = new Coin(json_coin.getLong("coinId"), 
								json_coin.getString("time"), 
								json_coin.getInt("grade"), 
								json_coin.getString("content"));
						coins.add(coin);
					}
					saveAllCoins(coins);
				}
				
				
				//ÕâÀï»ñÈ¡treasures²¢±£´æ
				if (!json_allUserData.isNull("treasures")) {
					JSONArray jsonArray_treasures = json_allUserData.getJSONArray("treasures");
					List<Treasure> treasures = new ArrayList<Treasure>();
					for (int i = 0; i < jsonArray_treasures.length(); i++) {
						JSONObject json_treasure = jsonArray_treasures.getJSONObject(i);
						Treasure treasure = new Treasure(json_treasure.getLong("treasureId"), 
								json_treasure.getString("time"), 
								json_treasure.getString("content"), 
								 
								json_treasure.getDouble("latitude"),
								json_treasure.getDouble("longitude")
								, json_treasure.getString("title"),
								json_treasure.getInt("isChecked")
								
								);
						treasures.add(treasure);
					}
					saveAllTreasures(treasures);
				}
				
				
				return true;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return false;
	}
	

	@Override
	public User getUserInformation() {
		User user = null;
		Cursor newCursor=db.rawQuery("select * from user",null);
		while (newCursor.moveToNext()) {	
			long userId=newCursor.getLong(newCursor.getColumnIndex("userId"));
			int gender=newCursor.getInt(newCursor.getColumnIndex("gender"));
			String username=newCursor.getString(newCursor.getColumnIndex("username"));
			String password=newCursor.getString(newCursor.getColumnIndex("password"));
			String picture=newCursor.getString(newCursor.getColumnIndex("picture"));
			String birthday=newCursor.getString(newCursor.getColumnIndex("birthday"));
			String hobby=newCursor.getString(newCursor.getColumnIndex("hobby"));
			String friends=newCursor.getString(newCursor.getColumnIndex("friends"));
			long lastChangeTime=newCursor.getLong(newCursor.getColumnIndex("lastChangeTime"));
			int operation=newCursor.getInt(newCursor.getColumnIndex("operation"));
			user = new User(userId,gender,username,password,picture,birthday,hobby,friends,lastChangeTime,operation);
		}
		newCursor.close();
	    return user;
	}

	
	//Õâ¸öº¯ÊýÐèÒªÂÞ¾êÍ¬Ñ§ºÍÁÖ²ßÍ¬Ñ§¹²Í¬Íê³É
	//ÕâÀïÊÇ´«ÈëÓÃ»§±¾µØÊý¾ÝºÍ·þÎñÆ÷Êý¾Ý²»Ò»ÑùµÄµØ·½£¬ÐèÒª½âÎöjson£¬È»ºó±£´æ£¬³É¹¦·µ»Øtrue
	
	public boolean saveUserChangedData(JSONObject json_userAllChangedData) {
		return false;
	}

	
	@Override
	public int getCoinPoint() {
		int count=0;
		Cursor cursor=db.rawQuery("select * from coin order by coinId asc",null);
		while (cursor.moveToNext()) {
			int grade=cursor.getInt(cursor.getColumnIndex("grade"));
			count=count+grade;
		}
		return count;
	}

	
	@Override
	public int getTreasureCount() {
//		Cursor cursor=db.rawQuery("select * from treasure where isChecked=1",null);
//		return cursor.getCount();
		return getAllTreasure().size();
		
	}

	@Override
	public boolean saveUser(User user) {
		db.execSQL("insert into user(userId,gender,username,password,picture,birthday,hobby,friends,lastChangeTime,operation) values(?,?,?,?,?,?,?,?,?,?)",
				new Object[]{user.getUserId(),user.getGender(),user.getUsername(),user.getPassword(),user.getPicture(),user.getBirthday(),user.getHobby(),user.getFriends(),user.getLastChangeTime(),user.getOperation()});
		return true;
	}
	

	@Override
	public List<Coin> getAllCoin() {
		List<Coin> coins=new ArrayList<Coin>();
		Cursor cursor=db.rawQuery("select * from coin order by coinId asc",null);
		while (cursor.moveToNext()) {
			long coinId=cursor.getLong(cursor.getColumnIndex("coinId"));
			String time=cursor.getString(cursor.getColumnIndex("time"));
			int grade=cursor.getInt(cursor.getColumnIndex("grade"));
			String content=cursor.getString(cursor.getColumnIndex("content"));
			coins.add(new Coin(coinId,time,grade,content));
		}
		cursor.close();
		return coins;
	}

	
	
	@Override
	public List<Treasure> getAllTreasure() {
		List<Treasure> treasures=new ArrayList<Treasure>();
		Cursor cursor=db.rawQuery("select * from treasure where isChecked=1",null);
		while (cursor.moveToNext()) {
			long treasureId=cursor.getLong(cursor.getColumnIndex("treasureId"));
			String time=cursor.getString(cursor.getColumnIndex("time"));
			String content=cursor.getString(cursor.getColumnIndex("content"));
			double latitude=cursor.getDouble(cursor.getColumnIndex("latitude"));
			double longitude=cursor.getDouble(cursor.getColumnIndex("longitude"));

			String title=cursor.getString(cursor.getColumnIndex("title"));
			treasures.add(new Treasure(treasureId,time,content, latitude, longitude, title, 1));
		}
		cursor.close();
		return treasures;
	}
	

	@Override
	
	//Õâ¸öº¯Êý»¹Ã»×ö
	public JSONObject getAllChangedData() {
		
		
		JSONObject json_allChangedData = new JSONObject();
		
		Configuration configuration = null;
		JSONObject json_configuration = new JSONObject();
		Cursor cursor=db.rawQuery("select * from configuration where changed=?", new String[]{"1"});
		while (cursor.moveToNext()) {
			configuration = new Configuration();
			configuration.setConfigurationId(cursor.getLong(cursor.getColumnIndex("configurationId")));
			configuration.setLoginUser(cursor.getString(cursor.getColumnIndex("loginUser")));
			configuration.setSyncByWifi(cursor.getInt(cursor.getColumnIndex("syncByWifi")));
			configuration.setChanged(1);
			configuration.setInfo(cursor.getString(cursor.getColumnIndex("info")));
			configuration.setTrackOrNot(cursor.getInt(cursor.getColumnIndex("trackOrNot")));
			configuration.setAutoPush(cursor.getInt(cursor.getColumnIndex("autoPush")));

			try {
				json_configuration.put("configurationId", configuration.getConfigurationId());
				json_configuration.put("loginUser", configuration.getLoginUser());
				json_configuration.put("syncByWifi", configuration.getSyncByWifi());
				json_configuration.put("changed", configuration.getChanged());
				json_configuration.put("info", configuration.getInfo());
				json_configuration.put("trackOrNot", configuration.getTrackOrNot());
				json_configuration.put("autoPush", configuration.getAutoPush());
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		if (configuration != null) {
			try {
				json_allChangedData.put("configuration", json_configuration);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		cursor.close();
		
		Cursor cursor2=db.rawQuery("select * from user where operation=?", new String[]{"2"});
		JSONObject json_user = null;
		while (cursor2.moveToNext()) {
			json_user = new JSONObject();
			try {
				json_user.put("userId", cursor2.getLong(cursor2.getColumnIndex("userId")));
				json_user.put("gender", cursor2.getInt(cursor2.getColumnIndex("gender")));
				json_user.put("username", cursor2.getString(cursor2.getColumnIndex("username")));
				json_user.put("password", cursor2.getString(cursor2.getColumnIndex("password")));
				json_user.put("picture", cursor2.getString(cursor2.getColumnIndex("picture")));
				json_user.put("birthday", cursor2.getString(cursor2.getColumnIndex("birthday")));
				json_user.put("hobby", cursor2.getString(cursor2.getColumnIndex("hobby")));
				json_user.put("friends", cursor2.getString(cursor2.getColumnIndex("friends")));
				json_user.put("lastChangeTime", cursor2.getLong(cursor2.getColumnIndex("lastChangeTime")));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		if (json_user!=null) {
			try {
				json_allChangedData.put("user", json_user);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		cursor2.close();
		
		JSONArray json_routes = new JSONArray();
		Cursor cursor1=db.rawQuery("select * from route where changed=?", new String[]{"1"});
		while (cursor1.moveToNext()) {
			try {
				JSONObject json_route = new JSONObject();
				json_route.put("id", cursor1.getLong(cursor1.getColumnIndex("id")));
				json_route.put("latitude", cursor1.getDouble(cursor1.getColumnIndex("latitude")));
				json_route.put("longtitude", cursor1.getDouble(cursor1.getColumnIndex("longtitude")));
				json_route.put("date", cursor1.getString(cursor1.getColumnIndex("date")));
				json_routes.put(json_route);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		if (json_routes.length()!=0) {
			try {
				json_allChangedData.put("routes", json_routes);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		cursor1.close();

		JSONArray json_notes = new JSONArray();
		Cursor cursor3=db.rawQuery("select * from note", null);
		while (cursor3.moveToNext()) {
			int operation=cursor3.getInt(cursor3.getColumnIndex("operation"));
			if (operation!=0) {
				JSONObject json_note = new JSONObject();
				try {
					json_note.put("noteId", cursor3.getLong(cursor3.getColumnIndex("noteId")));
					json_note.put("operation", cursor3.getInt(cursor3.getColumnIndex("operation")));
					json_note.put("time", cursor3.getString(cursor3.getColumnIndex("time")));
					json_note.put("weather", cursor3.getInt(cursor3.getColumnIndex("weather")));
					json_note.put("permission", cursor3.getInt(cursor3.getColumnIndex("permission")));
					json_note.put("text", cursor3.getString(cursor3.getColumnIndex("text")));
					json_note.put("title", cursor3.getString(cursor3.getColumnIndex("title")));
					json_note.put("pictures", cursor3.getString(cursor3.getColumnIndex("pictures")));
					json_note.put("voice", cursor3.getString(cursor3.getColumnIndex("voice")));
					json_note.put("locationx", cursor3.getDouble(cursor3.getColumnIndex("locationx")));
					json_note.put("locationy", cursor3.getDouble(cursor3.getColumnIndex("locationy")));
					json_note.put("video", cursor3.getString(cursor3.getColumnIndex("video")));
					json_note.put("lastChangeTime", cursor3.getLong(cursor3.getColumnIndex("lastChangeTime")));
					json_notes.put(json_note);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		if (json_notes.length()!=0) {
			try {
				json_allChangedData.put("notes", json_notes);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		cursor3.close();

		return json_allChangedData;
	}
	

	@Override
	public void changeAllOperation() {
		Cursor cursor=db.rawQuery("select * from configuration",null);
		while (cursor.moveToNext()) {
			int changed=cursor.getInt(cursor.getColumnIndex("changed"));
			if (changed!=0) {
				db.execSQL("update configuration set changed=? where configurationId=?",
						new Object[]{0,1});
			}
		}
		cursor.close();
		
		Cursor cursor1=db.rawQuery("select * from user", null);
		while (cursor1.moveToNext()) {
			int operation=cursor1.getInt(cursor1.getColumnIndex("operation"));
			if (operation!=0) {
				db.execSQL("update user set operation=?",
						new Object[]{0});
			}
		}
		cursor1.close();
		
		Cursor cursor2=db.rawQuery("select * from note", null);
		while (cursor2.moveToNext()) {
			int operation=cursor2.getInt(cursor2.getColumnIndex("operation"));
			if (operation!=0) {
				db.execSQL("update note set operation=?",
						new Object[]{"0"});
			}
		}
		cursor2.close();
		
		Cursor cursor3=db.rawQuery("select * from route where changed=?", new String[]{"1"});
		while (cursor3.moveToNext()) {
			int changed=cursor3.getInt(cursor3.getColumnIndex("changed"));
			if (changed!=0) {
				db.execSQL("update route set changed=?",
						new Object[]{0});
			}
		}
		cursor3.close();
	}
	

	@Override
	public void saveAllNotes(List<Note> notes) {
		for (int i = 0; i < notes.size(); i++) {
			Note note= new Note();
			note=notes.get(i);
			addNote(note);
		}
	}
	

	@Override
	public void saveAllRoutes(List<Route> routes) {
		for (int i = 0; i < routes.size(); i++) {
			Route route= new Route();
			route=routes.get(i);
			db.execSQL("insert into route(id,latitude,longtitude,date,changed) values(?,?,?,?,?)",
					new Object[]{route.getId(),route.getLatitude(),route.getLongtitude(),route.getDate(),0});
		}
	}

	public void saveRoute(Route route){
		db.execSQL("insert into route(id,latitude,longtitude,date,changed) values(?,?,?,?,?)",
				new Object[]{route.getId(),route.getLatitude(),route.getLongtitude(),route.getDate(),0});
		
	}
	
	
	@Override
	public void saveAllCoins(List<Coin> coins) {
		for (int i = 0; i < coins.size(); i++) {
			Coin coin= new Coin();
			coin=coins.get(i);
			db.execSQL("insert into coin(coinId,time,grade,content) values(?,?,?,?)",
					new Object[]{coin.getCoinId(),coin.getTime(),coin.getGrade(),coin.getContent()});
		}
	}

	
	@Override
	public void saveAllTreasures(List<Treasure> treasures) {
		for (int i = 0; i < treasures.size(); i++) {
			Treasure treasure= new Treasure();
			treasure=treasures.get(i);
			db.execSQL("insert into treasure(treasureId,time,content,latitude,longitude,title,isChecked) values(?,?,?,?,?,?,?)",
					new Object[]{treasure.getTreasureId(),treasure.getTime(),treasure.getContent(),treasure.getLatitude(),treasure.getLongitude(),treasure.getTitle(),treasure.getIsChecked()});
		}
	}

	
	@Override
	public boolean updateUser(User user) {
		db.execSQL("update user set gender=?,picture=?,hobby=?,friends=?,birthday=?,operation=?,lastChangeTime=? where userId=?",
				new Object[]{user.getGender(),user.getPicture(),user.getHobby(),user.getFriends(),user.getBirthday(),2,System.currentTimeMillis(),user.getUserId()});
		return true;
	}


}
