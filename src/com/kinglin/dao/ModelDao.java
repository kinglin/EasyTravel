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
	
	//获取用户配置数据
	Configuration getUserConfiguration();
	
	//设置用户配置数据
	public void setUserConfiguration(Configuration configuration);
	
	//保存用户配置数据
	public void saveConfiguration(Configuration configuration);
	
	//添加记事
	public void addNote(Note note);
	
	//获得所有记事
	public List<Note> getAllNotes();
	
	//删除记事
	public void deleteNote(Note note);
	
	//更新记事
	public void updateNote(Note note);
	
	//按位置查找记事
	public Note findNoteByPosition(int position);
	
	//检测当前欲登陆用户是否曾经登陆过
	public boolean hasBeenLogedIn(long userId);
	
	//获得所有数据的id和最后修改时间，便于不同客户端之间同步
	public JSONObject getAllDataSnapshot();
	
	//保存新登录的用户的所有数据
	public boolean saveNewUserAllData(JSONObject json_allUserData);
	
	//保存用户改动过的数据
	public boolean saveUserChangedData(JSONObject json_userAllChangedData);
	
	//获取用户信息
	public User getUserInformation();
	
	//获取积分总数
	public int getCoinPoint();
	
	//获取宝藏个数
	public int getTreasureCount();
	
	//保存用户，用于在default数据库中插入用户信息
	public boolean saveUser(User user);
	
	//更新用户数据
	public boolean updateUser(User user);
	
	//获取积分列表
	public List<Coin> getAllCoin();
	
	//获得宝藏列表
	public List<Treasure> getAllTreasure();
	
	//获得用户所有改动过的数据，包括添加，修改和删除
	public JSONObject getAllChangedData();
	
	//修改所有operation值为0，用在处理同步过后
	public void changeAllOperation();
	
	//保存获得的所有记事
	public void saveAllNotes(List<Note> notes);
	
	//保存所有的路径信息
	public void saveAllRoutes(List<Route> routes);
	
	//保存所有的积分信息
	public void saveAllCoins(List<Coin> coins);
	
	//保存所有的宝藏信息
	public void saveAllTreasures(List<Treasure> treasures);
	
	//删除记事
	public void delete_note(Note note);
	
	//保存单个路径
	public void saveRoute(Route route);
}
