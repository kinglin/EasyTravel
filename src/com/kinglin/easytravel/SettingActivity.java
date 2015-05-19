package com.kinglin.easytravel;

import java.io.File;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.kinglin.dao.DBHelper;
import com.kinglin.dao.ModelDaoImp;
import com.kinglin.model.Configuration;
import com.kinglin.service.PushTreasureService;
import com.kinglin.service.SyncService;
import com.kinglin.service.TrackService;

@SuppressLint("ShowToast")
public class SettingActivity extends Activity {

	ImageButton ibtnSettingExpandAboutUs;
	LinearLayout llayoutSettingAboutUs;
	Switch switchWifi,switchTracker,switchAutoPush;
	Button btnLogout;
	
	int rotate = 0;
	
	SQLiteDatabase db;
	Configuration configuration;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_setting);
		
		db = dbInit();
		initContext();
		
		ModelDaoImp mdi = new ModelDaoImp(db);
		configuration = mdi.getUserConfiguration();
		String loginUser = mdi.getUserConfiguration().getLoginUser();
		
		if (loginUser.equals("default")) {
			btnLogout.setVisibility(View.GONE);
		}
		
		//根据数据库中所存的数据来显示当前开关的状态
		if (configuration.getSyncByWifi() == 1) {
			switchWifi.setChecked(true);
		}
		else {
			switchWifi.setChecked(false);
		}
		
		if (configuration.getTrackOrNot() == 1) {
			switchTracker.setChecked(true);
		}
		else {
			switchTracker.setChecked(false);
		}
		
		if (configuration.getAutoPush() == 1) {
			switchAutoPush.setChecked(true);
		}
		else {
			switchAutoPush.setChecked(false);
		}
		
		switchWifi.setOnCheckedChangeListener(new SyncByWifiCheckedChangeListener());
		switchTracker.setOnCheckedChangeListener(new RouteTrackerCheckedChangeListener());
		switchAutoPush.setOnCheckedChangeListener(new AutoPushCheckedChangeListener());
		
		ibtnSettingExpandAboutUs.setOnClickListener(new ExpandAboutUsClickListener());
		btnLogout.setOnClickListener(new LogoutClickListener());
		
	}

	//这个函数获取当前用户的数据库
	public SQLiteDatabase dbInit() {
		
		//先连接上default数据库，第一次使用会初始化各种实体对应的表
		DBHelper helper=new DBHelper(this, "default.db", null, 1);
	    SQLiteDatabase defaultdb=helper.getWritableDatabase();
        
        //查询default数据库中Configuration表的loginUser,返回相应的db
	    ModelDaoImp mdi = new ModelDaoImp(defaultdb);
        String userId = mdi.getUserConfiguration().getLoginUser();
        if (userId.equals("default")) {
			return defaultdb;
		}else {
			DBHelper helper1=new DBHelper(this, userId+".db", null, 1);
		    SQLiteDatabase userdb=helper1.getWritableDatabase();
		    return userdb;
		}
	}
	
	//初始化控件
	private void initContext(){
		
		switchWifi = (Switch) findViewById(R.id.switch_syncByWifi);
		switchTracker = (Switch) findViewById(R.id.switch_routeTracker);
		switchAutoPush = (Switch) findViewById(R.id.switch_autoPush);
		ibtnSettingExpandAboutUs = (ImageButton) findViewById(R.id.ibtn_settingExpandAboutUs);
		llayoutSettingAboutUs = (LinearLayout) findViewById(R.id.ll_settingAboutUs);
		btnLogout = (Button) findViewById(R.id.btn_logout);
	}
	
	//wifi同步状态切换事件监听
	private class SyncByWifiCheckedChangeListener implements OnCheckedChangeListener{
		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			
			ModelDaoImp mdi = new ModelDaoImp(db);
			if (isChecked) {		//开关打开时
				if (mdi.getUserConfiguration().getLoginUser().equals("default")) {
					Toast.makeText(getApplicationContext(), "please login before sync", 500).show();
					switchWifi.setChecked(false);
				}else {
					configuration.setSyncByWifi(1);
					configuration.setChanged(1);
					mdi.setUserConfiguration(configuration);
					if(!isServiceRunning(getApplicationContext(), "com.kinglin.service.SyncService")){
						Intent intent = new Intent(getApplicationContext(),SyncService.class);
						startService(intent);
						Toast.makeText(getApplicationContext(), "start sync", Toast.LENGTH_SHORT).show();
					}
				}
				
			}
			else {				//开关关闭时
				configuration.setSyncByWifi(0);
				configuration.setChanged(1);
				mdi.setUserConfiguration(configuration);
				if(isServiceRunning(getApplicationContext(), "com.kinglin.service.SyncService")){
					Intent intent = new Intent(getApplicationContext(),SyncService.class);
					stopService(intent);
					Toast.makeText(getApplicationContext(), "stop sync", Toast.LENGTH_SHORT).show();
				}
			}
			
		}
	}
	
	//路径跟踪状态切换事件监听
	private class RouteTrackerCheckedChangeListener implements OnCheckedChangeListener{
		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			
			ModelDaoImp mdi = new ModelDaoImp(db);
			if (isChecked) {		//开关打开时
				configuration.setTrackOrNot(1);
				configuration.setChanged(1);
				mdi.setUserConfiguration(configuration);
				if(!isServiceRunning(getApplicationContext(), "com.kinglin.service.TrackService")){
					Intent intent = new Intent(getApplicationContext(),TrackService.class);
					startService(intent);
					Toast.makeText(getApplicationContext(), "start route track", Toast.LENGTH_SHORT).show();
				}
			}
			else {				//开关关闭时
				configuration.setTrackOrNot(0);
				configuration.setChanged(1);
				mdi.setUserConfiguration(configuration);
				if(isServiceRunning(getApplicationContext(), "com.kinglin.service.TrackService")){
					Intent intent = new Intent(getApplicationContext(),TrackService.class);
					stopService(intent);
					Toast.makeText(getApplicationContext(), "stop route track", Toast.LENGTH_SHORT).show();
				}
			}
			
		}
	}
	
	//自动后台推送状态切换事件监听
	private class AutoPushCheckedChangeListener implements OnCheckedChangeListener{

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			ModelDaoImp mdi = new ModelDaoImp(db);
			if (isChecked) {		//开关打开时
				configuration.setAutoPush(1);
				configuration.setChanged(1);
				mdi.setUserConfiguration(configuration);
				if(!isServiceRunning(getApplicationContext(), "com.kinglin.service.PushTreasureService")){
					Intent intent = new Intent(getApplicationContext(),PushTreasureService.class);
					startService(intent);
					Toast.makeText(getApplicationContext(), "start auto push", Toast.LENGTH_SHORT).show();
				}
			}
			else {				//开关关闭时
				configuration.setAutoPush(0);
				configuration.setChanged(1);
				mdi.setUserConfiguration(configuration);
				if(isServiceRunning(getApplicationContext(), "com.kinglin.service.PushTreasureService")){
					Intent intent = new Intent(getApplicationContext(),PushTreasureService.class);
					stopService(intent);
					Toast.makeText(getApplicationContext(), "stop auto push", Toast.LENGTH_SHORT).show();
				}
			}
			
		}
		
	}
	
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
	
	//点击about us展开按钮的事件响应
	private class ExpandAboutUsClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			
			RotateAnimation rAnimation;
			
			if (rotate == 0) {
				
				rAnimation = new RotateAnimation(0, 180,
						Animation.RELATIVE_TO_SELF, 0.5f,
		                Animation.RELATIVE_TO_SELF, 0.5f);
				rAnimation.setDuration(500);
				rAnimation.setFillAfter(true);
				ibtnSettingExpandAboutUs.startAnimation(rAnimation);
				
				llayoutSettingAboutUs.setVisibility(View.VISIBLE);
				
				TranslateAnimation tAnimation = new TranslateAnimation(
						Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, 
						Animation.RELATIVE_TO_SELF, -1, Animation.RELATIVE_TO_SELF, 0);
				tAnimation.setDuration(500);
				tAnimation.setStartOffset(0);
				tAnimation.setFillAfter(true);
				
				llayoutSettingAboutUs.startAnimation(tAnimation);
				
				rotate = 1;
			}
			else {
				
				rAnimation = new RotateAnimation(180, 360,
						Animation.RELATIVE_TO_SELF, 0.5f,
		                Animation.RELATIVE_TO_SELF, 0.5f);
				rAnimation.setDuration(500);
				rAnimation.setFillAfter(true);
				ibtnSettingExpandAboutUs.startAnimation(rAnimation);
				
				TranslateAnimation tAnimation = new TranslateAnimation(
						Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, 
						Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, -1);
				tAnimation.setDuration(500);
				tAnimation.setStartOffset(0);
				tAnimation.setFillAfter(true);
				
				llayoutSettingAboutUs.startAnimation(tAnimation);
				
				rotate = 0;
			}
		}
	}
	

	//点击退出登录时的事件响应
	private class LogoutClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			ModelDaoImp mdi;
			mdi = new ModelDaoImp(db);
			long userId = mdi.getUserInformation().getUserId();
			File file = getApplicationContext().getDatabasePath(String.valueOf(userId)+".db");
			file.delete();
			file = getApplicationContext().getDatabasePath(String.valueOf(userId)+".db-journal");
			file.delete();
			
			DBHelper helper=new DBHelper(SettingActivity.this, "default.db", null, 1);
		    SQLiteDatabase defaultdb=helper.getWritableDatabase();
	        
		    mdi = new ModelDaoImp(defaultdb);
		    
		    Configuration mConfiguration = mdi.getUserConfiguration();
		    mConfiguration.setLoginUser("default");
		    mdi.setUserConfiguration(mConfiguration);
		    
		    if (mdi.getUserConfiguration().getTrackOrNot() == 0 
		    		&& isServiceRunning(getApplicationContext(), "com.kinglin.service.TrackService")) {
		    	Intent serviceintent = new Intent(getApplicationContext(),TrackService.class);
				stopService(serviceintent);
			}else if (mdi.getUserConfiguration().getTrackOrNot() == 1 
		    		&& (!isServiceRunning(getApplicationContext(), "com.kinglin.service.TrackService"))) {
				Intent serviceintent = new Intent(getApplicationContext(),TrackService.class);
				startService(serviceintent);
			}
		    
		    if (mdi.getUserConfiguration().getAutoPush() == 0 
		    		&& isServiceRunning(getApplicationContext(), "com.kinglin.service.PushTreasureService")) {
		    	Intent serviceintent = new Intent(getApplicationContext(),PushTreasureService.class);
				stopService(serviceintent);
			}else if (mdi.getUserConfiguration().getAutoPush() == 1 
		    		&& (!isServiceRunning(getApplicationContext(), "com.kinglin.service.PushTreasureService"))) {
				Intent serviceintent = new Intent(getApplicationContext(),PushTreasureService.class);
				startService(serviceintent);
			}
		    
		    if (mdi.getUserConfiguration().getSyncByWifi() == 1) {
		    	Intent serviceintent = new Intent(getApplicationContext(),SyncService.class);
				startService(serviceintent);
			}
		    
		    Intent intent = new Intent(SettingActivity.this, LoginActivity.class);
		    startActivity(intent);
		    UserInformationActivity.instanceActivity.finish();
		    SettingActivity.this.finish();
		}
		
	}
}
