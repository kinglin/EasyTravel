package com.kinglin.easytravel;

import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Window;
import android.widget.Toast;

import com.kinglin.dao.DBHelper;
import com.kinglin.dao.ModelDaoImp;
import com.kinglin.model.Configuration;
import com.kinglin.service.PushTreasureService;
import com.kinglin.service.SyncService;
import com.kinglin.service.TrackService;

@SuppressLint("ShowToast")
public class MainActivity extends Activity {

	SQLiteDatabase db;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		//获得当前使用的数据库
		db = dbInit();
		
		ModelDaoImp mdi = new ModelDaoImp(db);
		Configuration configuration = mdi.getUserConfiguration();
		
		//判断当前用户的登陆状态
		if (!configuration.getLoginUser().equals("default")) {
			
			//是否在wifi下同步
			if (configuration.getSyncByWifi() == 1) {
				
				//判断网络状态，0为未联网，1为移动网，2为wifi
				switch (netWorkState(getApplicationContext())) {
				case 0:
					Toast.makeText(getApplicationContext(), "you are offline", 1000).show();
					break;
				case 1:
					Toast.makeText(getApplicationContext(), "mobile network,no syncing", 1000).show();
					break;
				case 2:
					Toast.makeText(getApplicationContext(), "wifi", 1000).show();
					
					if (isServiceRunning(getApplicationContext(), "com.kinglin.service.SyncService")) {
						Toast.makeText(getApplicationContext(), "syncing has been start", Toast.LENGTH_SHORT).show();
					}else {
						Intent intent = new Intent(getApplicationContext(),SyncService.class);
						startService(intent);
						Toast.makeText(getApplicationContext(), "syncing", Toast.LENGTH_SHORT).show();
					}
					break;
				case 4:
					Toast.makeText(getApplicationContext(), "can't get network state", 1000).show();
					break;
				default:
					break;
				}
			}
		}
		
		//看是否跟踪路径
		if (configuration.getTrackOrNot()==1) {
			if (isServiceRunning(getApplicationContext(), "com.kinglin.service.TrackService")) {
				Toast.makeText(getApplicationContext(), "route track has been start", Toast.LENGTH_SHORT).show();
			}else {
				Intent intent = new Intent(getApplicationContext(),TrackService.class);
				startService(intent);
				Toast.makeText(getApplicationContext(), "start route track", Toast.LENGTH_SHORT).show();
			}
		}
		
		//看是否要开启推送
		if (configuration.getAutoPush()==1) {
			if (isServiceRunning(getApplicationContext(), "com.kinglin.service.PushTreasureService")) {
				Toast.makeText(getApplicationContext(), "auto push has been start", Toast.LENGTH_SHORT).show();
			}else {
				Intent intent = new Intent(getApplicationContext(),PushTreasureService.class);
				startService(intent);
				Toast.makeText(getApplicationContext(), "start auto push", Toast.LENGTH_SHORT).show();
			}
		}

		Intent intent = new Intent(MainActivity.this, AddNoteActivity.class);
		startActivity(intent);
		this.finish();
	}
	
	//工具函数，判断service是否开启
	public static boolean isServiceRunning(Context mContext,String className) {
		boolean isRunning = false;
		ActivityManager activityManager = (ActivityManager)
				mContext.getSystemService(Context.ACTIVITY_SERVICE); 
		List<ActivityManager.RunningServiceInfo> serviceList 
		= activityManager.getRunningServices(200);
		if (!(serviceList.size()>0)) {
			return false;
		}
		for (int i=0; i<serviceList.size(); i++) {
			if (serviceList.get(i).service.getClassName().equals(className) == true) {
				isRunning = true;
				break;
			}
		}
		return isRunning;
	}

	
	//返回当前应使用的数据库
	public SQLiteDatabase dbInit() {

		//先进入default数据库
		DBHelper helper=new DBHelper(MainActivity.this, "default.db", null, 1);
		SQLiteDatabase defaultdb=helper.getWritableDatabase();

		ModelDaoImp mdi = new ModelDaoImp(defaultdb);

		//拿到当前使用用户的id，并返回其数据库
		String userId = mdi.getUserConfiguration().getLoginUser();
		if (userId.equals("default")) {
			return defaultdb;
		}else {
			DBHelper helper1=new DBHelper(MainActivity.this, userId+".db", null, 1);
			SQLiteDatabase userdb=helper1.getWritableDatabase();
			return userdb;
		}
	}

	//判断是否联网，WiFi为2，移动网为1，未联网为0,获取信息失败为4
	public int netWorkState(Context context) {
		int  netState=4;
		ConnectivityManager connectivity = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE); 
		NetworkInfo activeNetInfo = connectivity.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		NetworkInfo mobNetInfo = connectivity.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		if (activeNetInfo.isConnected()) {
			netState=2;
		}else if (mobNetInfo.isConnected()) {
			netState=1;
		}
		if(!activeNetInfo.isConnected() && !mobNetInfo.isConnected()) {
			netState=0;
		}
		return netState;
	}


}
