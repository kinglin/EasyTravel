package com.kinglin.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.kinglin.dao.DBHelper;
import com.kinglin.dao.ModelDaoImp;
import com.kinglin.easytravel.MapForTreasure;
import com.kinglin.easytravel.R;
import com.kinglin.model.Treasure;

public class PushTreasureService extends Service{

	@SuppressWarnings("unused")
	private Context context;
	private LocationManager manager;
	private String provider;
	private LocationListener locationListener;
	double Redius = 6371;
	private SQLiteDatabase db; 
	List<Treasure> list;
	List<Treasure> rsltList;
	Location mLocation;
	protected NotificationManager mNotificationManager;
	protected Notification mNotification;
	@Override

	public IBinder onBind(Intent intent) {
		return null;
	}
	@Override
	public void onCreate() {

		this.context=this;
		Log.d("myservice", "hit service"); 

		manager=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
		provider=getLocationProvider(manager);
		
		locationListener=new LocationListener() {

			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {
			}

			@Override
			public void onProviderEnabled(String provider) {
			}

			@Override
			public void onProviderDisabled(String provider) {
			}

			@Override
			public void onLocationChanged(Location location) {
				if  (location !=  null ) {  
					Log.d("SuperMap" ,  "Location changed : Lat: "   
							+ location.getLatitude() + " Lng: "   
							+ location.getLongitude());  

					list=getAllTreasure(location);
					if (list.isEmpty()) {
						Log.d("Treasure" ,  "no treasure found surroundly！ "   ); 
					}else{
						//执行推送通知操作
						changeIsChecked(list);
						//展示通知栏
						showNotification(list);
						Log.d("Treasure" ,  "You Have Found A Treasure!" + list.get(0).getContent() + "Have A Look!！ "   ); 
					}
				}
				else{
					Log.d("SuperMap" ,  "Location null "   ); 
				}  
			}
		};

		Log.d("myservice", "onClick: create service"); 

		super.onCreate();
	}
	//通知拦点击事件
	protected void changeIsChecked(List<Treasure> list2) {
		db=dbInit2();
		for (Treasure treasure : list2) {

			String sql="update treasure set isChecked=1 where treasureId="+treasure.getTreasureId();
			db.execSQL(sql);
		}
		db.close();
	}
	@SuppressWarnings("deprecation")
	protected void showNotification(List<Treasure> list2) {
		for (int i = 0; i < list2.size(); i++) {


			NotificationManager nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);               
			Notification n = new Notification(R.drawable.easy_travel,list.get(i).getContent(), System.currentTimeMillis());             
			n.flags = Notification.FLAG_AUTO_CANCEL;                
			Intent i1 = new Intent(PushTreasureService.this, MapForTreasure.class);
			i1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK); 


			i1.putExtra( "Treasure",  (Serializable) list2 );
			//PendingIntent
			PendingIntent contentIntent = PendingIntent.getActivity(
					this, 
					R.string.app_name, 
					i1, 
					PendingIntent.FLAG_UPDATE_CURRENT);

			n.setLatestEventInfo(
					this,
					"附近发现优惠活动", 
					list2.get(i).getContent()+"   点击查看", 
					contentIntent);
			nm.notify(R.string.app_name, n);
		}
	}
//获得当前宝藏信息
	protected List<Treasure> getTreasure(Location location) {
		list=getAllTreasure(location);
		for (int i = 0; i < list.size(); i++) {
			double lat=list.get(i).getLatitude();
			double lng=list.get(i).getLongitude();
			mLocation.setLatitude(lat);
			mLocation.setLongitude(lng);
			Location l=new Location(mLocation);
			double distance=getDistance(location, l);
			if (distance<500.0) {
				rsltList.add(list.get(i));
			}

		}
		return rsltList;
	}
	private List<Treasure> getAllTreasure(Location location) {
		db=dbInit();
		double lat=location.getLatitude()+0.00337;
		double lng=location.getLongitude()+0.012;
		String sql="select * from treasure where "+lat+"-latitude between -0.003 and 0.003 and "+lng+"-longitude between -0.003 and 0.003 and isChecked=0";
		Cursor cursor = db.rawQuery(sql, null);
		List<Treasure> mlList=new ArrayList<Treasure>();
		while (cursor.moveToNext()) {

			long treasureId = cursor.getLong(cursor.getColumnIndex("treasureId"));
			String time = cursor.getString(cursor.getColumnIndex("time"));

			double latitude = cursor.getDouble(cursor
					.getColumnIndex("latitude"));
			double longitude = cursor.getDouble(cursor
					.getColumnIndex("longitude"));
			String content=cursor.getString(cursor.getColumnIndex("content"));
			String title=cursor.getString(cursor.getColumnIndex("title"));
			int isChecked=cursor.getInt(cursor.getColumnIndex("isChecked"));
			mlList.add(new Treasure(treasureId,time, content, latitude, longitude, title, isChecked));
		}

		cursor.close();
		db.close();
		return mlList;

	}

	private static double rad(double d)
	{
		return d * Math.PI / 180.0;
	}
	//计算距离
	protected double getDistance(Location location1,Location location2) {

		Double lat1=location1.getLatitude();	
		Double lng1=location1.getLongitude();
		double lat2=location2.getLatitude();
		double lng2=location2.getLongitude();

		double radLat1 = rad(lat1);
		double radLat2 = rad(lat2);
		double a = radLat1 - radLat2;
		double b = rad(lng1) - rad(lng2);
		double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a/2),2) + 
				Math.cos(radLat1)*Math.cos(radLat2)*Math.pow(Math.sin(b/2),2)));
		s = s * Redius;
		s = Math.round(s * 10000) / 10000;
		return s;

	}
	//获得位置服务商
	private String getLocationProvider(LocationManager manager2) {
		Criteria cri=new  Criteria();
		cri.setAccuracy(Criteria.ACCURACY_FINE);
		cri.setPowerRequirement(Criteria.POWER_HIGH);
		cri.setSpeedRequired(true);
		cri.setCostAllowed(true);
		cri.setAltitudeRequired(false);
		cri.setBearingRequired(false);
		String lp=manager2.getBestProvider(cri, true);
		return lp;		

	}
	@SuppressWarnings("deprecation")
	@Override
	public void onStart(Intent intent, int startId) {
		if(provider==null){  
			Log.i("PROVIDER ERROR", "No GPS provider found!");  
		}  
		Log.i("PROVIDER ERROR", provider); 
		manager.requestLocationUpdates(provider, 30*1000, 0, locationListener);
		Log.d("myservice", "onClick: starting service"); 
		super.onStart(intent, startId);
	}
	@Override
	public void onDestroy() {
		Log.d("myservice", "onClick: stop service"); 
		manager.removeUpdates(locationListener);
		super.onDestroy();
	}
	//连接数据库
	public SQLiteDatabase dbInit() {

		DBHelper helper=new DBHelper(PushTreasureService.this, "default.db", null, 1);
		SQLiteDatabase defaultdb=helper.getReadableDatabase();
		ModelDaoImp mdi = new ModelDaoImp(defaultdb);
		String userId = mdi.getUserConfiguration().getLoginUser();
		if (userId.equals("default")) {
			return defaultdb;
		}else {
			DBHelper helper1=new DBHelper(PushTreasureService.this, userId+".db", null, 1);
			SQLiteDatabase userdb=helper1.getReadableDatabase();
			return userdb;
		}
	}
	//连接数据库
	public SQLiteDatabase dbInit2() {

		DBHelper helper=new DBHelper(PushTreasureService.this, "default.db", null, 1);
		SQLiteDatabase defaultdb=helper.getWritableDatabase();

		
		ModelDaoImp mdi = new ModelDaoImp(defaultdb);
		String userId = mdi.getUserConfiguration().getLoginUser();
		if (userId.equals("default")) {
			return defaultdb;
		}else {
			DBHelper helper1=new DBHelper(PushTreasureService.this, userId+".db", null, 1);
			SQLiteDatabase userdb=helper1.getWritableDatabase();
			return userdb;
		}
	}
}