package com.kinglin.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import com.kinglin.dao.DBHelper;
import com.kinglin.dao.ModelDaoImp;
import com.kinglin.easytravel.ShowNoteActivity;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;


@SuppressLint("SimpleDateFormat")
@SuppressWarnings("unused")
public class TrackService extends Service{
	private boolean threadDisable;
	private Context context;
	MediaPlayer player;
	protected int count;

	private  SQLiteDatabase db;
	private LocationManager manager;

	private String provider;
	private LocationListener locationListener;
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	@Override
	public void onCreate() {
		this.context=this;
		Log.d("myservice", "hit service"); 

		db = dbInit();
		manager=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
		provider=getLocationProvider(manager);
		//        manager.setTestProviderEnabled("gps", true);
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

					insertLocation(location);

					Log.d("SuperMap" ,  "INSERT SUCCESS! "   ); 

				}
				else{
					Log.d("SuperMap" ,  "Location null "   ); 
				}  


			}


		};


		Log.d("myservice", "onClick: create service"); 

		super.onCreate();
	}
	//获得provider
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

		//��������default���ݿ⣬��һ��ʹ�û��ʼ������ʵ���Ӧ�ı�
		DBHelper helper=new DBHelper(TrackService.this, "default.db", null, 1);
		SQLiteDatabase defaultdb=helper.getWritableDatabase();

		//��ѯdefault���ݿ���Configuration���loginUser,������Ӧ��db
		ModelDaoImp mdi = new ModelDaoImp(defaultdb);
		String userId = mdi.getUserConfiguration().getLoginUser();
		if (userId.equals("default")) {
			return defaultdb;
		}else {
			DBHelper helper1=new DBHelper(TrackService.this, userId+".db", null, 1);
			SQLiteDatabase userdb=helper1.getWritableDatabase();
			return userdb;
		}
	}
	//插入到数据库
	private long insertLocation(Location location) {

		ContentValues values=new ContentValues();
		values.put("id", System.currentTimeMillis());
		values.put("latitude", location.getLatitude());
		values.put("longtitude", location.getLongitude());
		values.put("date", currentlyTime());
		values.put("changed", 1);
		long rowID=db.insert("route", "date", values);
		return rowID;
	}
	//获取当前时间
	public String currentlyTime() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式

		return  df.format(new Date());
	}

}

