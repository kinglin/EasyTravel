package com.kinglin.dao;

import java.util.List;

import org.json.JSONObject;

import com.kinglin.model.Coin;
import com.kinglin.model.Configuration;
import com.kinglin.model.Note;
import com.kinglin.model.Route;
import com.kinglin.model.Treasure;
import com.kinglin.model.User;

public interface ModelDao {
	
	Configuration getUserConfiguration();
	
	public void setUserConfiguration(Configuration configuration);
	
	public void saveConfiguration(Configuration configuration);
	
	public void addNote(Note note);
	
	public List<Note> getAllNotes();
	
	public void deleteNote(Note note);
	
	public void updateNote(Note note);
	
	public Note findNoteByPosition(int position);
	
	//´«ÈëÓÃ»§id£¬µ½defaultÊý¾Ý¿âµÄuser±íÖÐ²éÕÒÊÇ·ñÓÐ¸ÃÓÃ»§´æÔÚ£¨´ËÊ±ÒÑ¾­ÔÚdefaultÊý¾Ý¿âÖÐ£©£¬Ã»ÓÐ·µ»Øfalse²¢²åÈë£¬ÓÐÔò·µ»Øtrue
	public boolean hasBeenLogedIn(long userId);
	
	//ÕâÀïÊÇ»ñµÃ¸ÃÓÃ»§ËùÓÐÊý¾ÝµÄidºÍ×îºóÐÞ¸ÄÊ±¼ä£¬²¢´ò°ü³Éjson·µ»Ø
	public JSONObject getAllDataSnapshot();
	
	//ÕâÀïÊÇ´«ÈëÐÂµÇÂ¼µÄÓÃ»§µÄËùÓÐÊý¾Ý£¬ÐèÒª½âÎöjson£¬È»ºó½«Êý¾Ý´æµ½´ËÓÃ»§µÄÊý¾Ý¿â£¬²¢½«defaultÊý¾Ý¿âÖÐµÄÏìÓ¦Êý¾Ý×öÐÞ¸Ä£¬±£´æ³É¹¦·µ»Øtrue
	public boolean saveNewUserAllData(JSONObject json_allUserData);
	
	//ÕâÀïÊÇ´«ÈëÓÃ»§±¾µØÊý¾ÝºÍ·þÎñÆ÷Êý¾Ý²»Ò»ÑùµÄµØ·½£¬ÐèÒª½âÎöjson£¬È»ºó½«Êý¾Ý´æµ½´ËÓÃ»§µÄÊý¾Ý¿â£¬²¢½«defaultÊý¾Ý¿âÖÐµÄÏìÓ¦Êý¾Ý×öÐÞ¸Ä£¬³É¹¦·µ»Øtrue
	public boolean saveUserChangedData(JSONObject json_userAllChangedData);
	
	//»ñÈ¡ÓÃ»§ÏêÏ¸ÐÅÏ¢
	public User getUserInformation();
	
	//Õâ¸öº¯ÊýÊÇ²éÑ¯coin±í£¬²¢½«ËùÓÐµÄgrade¼ÓÆðÀ´£¬·µ»ØËûÃÇµÄºÍ
	public int getCoinPoint();
	
	//Õâ¸öº¯ÊýÊÇ²éÑ¯treasure±í£¬·µ»ØÌõÄ¿µÄ¸öÊý
	public int getTreasureCount();
	
	//ÕâÀïÊÇÐÞ¸ÄÓÃ»§»ù±¾ÐÅÏ¢Ê±Ê¹ÓÃ£¬½«´«ÈëµÄÓÃ»§¸üÐÂÐÅÏ¢±£´æ£¬Í¬Ê±Òª½«operationºÍlastchangedtimeÖØÐÂ¸³Öµ
	public boolean saveUser(User user);
	
	public boolean updateUser(User user);
	//»ñÈ¡»ý·ÖÏîÁÐ±í
	public List<Coin> getAllCoin();
	
	//»ñÈ¡±¦²ØÏîÁÐ±í
	public List<Treasure> getAllTreasure();
	
	//Õâ¸öÐèÒªÂÞ¾êÍ¬Ñ§ºÍÁÖ²ßÍ¬Ñ§¹²Í¬Íê³É
	//ÕâÊÇ±éÀúÊý¾Ý¿âÖÐËùÓÐ±íµÄËùÓÐÊý¾Ý£¬È»ºóÈ¡³öÄÇÐ©operation²»Îª0µÄÊý¾Ý£¬²¢´ò°ü³Éjson
	public JSONObject getAllChangedData();
	
	//ÕâÀïÐèÒª±éÀúËùÓÐ±í£¬È»ºó½«operation²»Îª0µÄÈ«¸ÄÎª0£¬²¢½«Îª4µÄÉ¾³ý
	public void changeAllOperation();
	
	//ÕâÀï½«»ñµÃµÄnotesÒ»Æð´æµ½Êý¾Ý¿â
	public void saveAllNotes(List<Note> notes);
	
	//ÕâÀï½«»ñµÃµÄroutesÒ»Æð´æµ½Êý¾Ý¿â
	public void saveAllRoutes(List<Route> routes);
	
	//ÕâÀï½«»ñµÃµÄcoinsÒ»Æð´æµ½Êý¾Ý¿â
	public void saveAllCoins(List<Coin> coins);
	
	//ÕâÀï½«»ñµÃµÄtreasuresÒ»Æð´æµ½Êý¾Ý¿â
	public void saveAllTreasures(List<Treasure> treasures);
	
	public void delete_note(Note note);
	
	public void saveRoute(Route route);
}
