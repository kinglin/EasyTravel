package com.kinglin.dao;

import java.util.ArrayList;
import java.util.List;

import com.kinglin.model.Configuration;
import com.kinglin.model.Treasure;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

	public DBHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		//´´½¨ÓÃ»§±í,ÕâÀïÒª¶ÔÆäËûµÄ±í½øÐÐ³õÊ¼»¯´´½¨
	    String configuration_table="create table configuration(configurationId long primary key,loginUser text,syncByWifi integer,trackOrNot integer,autoPush integer,info text,changed integer)";
	    String user_table="create table user (userId long primary key,gender integer,username text,password text,picture text,birthday text,hobby text,friends text,lastChangeTime long,operation integer)"; 
	    String note_table="create table note(noteId long primary key,time text,permission integer,weather integer,text text,title text,pictures text,voice text,locationx double,locationy double,video text,lastChangeTime long,operation integer)";
	    String moment_table="create table moment(momentId long primary key,userId long,userName text,time text,weather integer,text text,pictures text,voice text,locationx text,locationy text,video text)";
	    String route_table="create table route(id long primary key,latitude double,longtitude double,date text,changed integer)";
	    String treasure_table="create table treasure(treasureId long primary key,time text,title text,content text,latitude double,longitude double,isChecked integer)";
	    String coin_table="create table coin(coinId long primary key,time text,grade integer,content text)";
	    db.execSQL(configuration_table); 
	    db.execSQL(user_table);  
	    db.execSQL(note_table); 
	    db.execSQL(moment_table); 
	    db.execSQL(route_table); 
	    db.execSQL(treasure_table); 
	    db.execSQL(coin_table); 
		
	    Configuration fconfiguration=new Configuration(1,"default",0,1,1,null,0);
	    ModelDaoImp mdi = new ModelDaoImp(db);
	    mdi.saveConfiguration(fconfiguration);
	    
	    List<Treasure> treasures = new ArrayList<Treasure>();
	    Treasure treasure1 = new Treasure(1, "2015-6-8"," dongjiu", 30.5203, 114.433, "dongjiu", 0);
	    Treasure treasure2 = new Treasure(2, "2015-6-7","yunyuan hotel", 30.5203, 114.438, "yunyuan hotel", 0);
	    Treasure treasure3 = new Treasure(3, "2015-6-6"," xue yi", 30.5173, 114.438, " xue yi", 0);
	    Treasure treasure4 = new Treasure(4, "2015-6-5"," tongweitang", 30.5205, 114.431, "tongweitang", 0);
	    Treasure treasure5 = new Treasure(5, "2015-6-4"," yuyuan", 30.5203, 114.4277, "yuyuan", 0);
	    Treasure treasure6 = new Treasure(6, "2015-6-3","tushuguan ", 30.51983, 114.417, "tushuguan", 0);
	    Treasure treasure7 = new Treasure(7, "2015-6-2","nanyilou ", 30.51573, 114.4186, "nanyilou", 0);
	    Treasure treasure8 = new Treasure(8, "2015-6-7","jifang ", 30.517, 114.441, "jifang", 0);
	    
	    treasures.add(treasure1);
	    treasures.add(treasure2);
	    treasures.add(treasure3);
	    treasures.add(treasure4);
	    treasures.add(treasure5);
	    treasures.add(treasure6);
	    treasures.add(treasure7);
	    treasures.add(treasure8);
	    mdi.saveAllTreasures(treasures);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

}
