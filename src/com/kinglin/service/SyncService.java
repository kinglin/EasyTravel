package com.kinglin.service;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import com.kinglin.dao.DBHelper;
import com.kinglin.dao.ModelDaoImp;
import com.kinglin.serverconnect.ServerConnection;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;

public class SyncService extends Service {

	SQLiteDatabase db;
	
	public SyncService() {
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		
		db = dbInit();
		
		super.onCreate();
		
		Timer timer = new Timer();
		timer.schedule(new Work(), 0, 2*60*1000);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
	}

	class Work extends TimerTask{
		public void run(){
			if (netWorkState(getApplicationContext())==2 && isNetworkAvailable(getApplicationContext())) {
				ServerConnection sc = new ServerConnection(db);
				try {
					sc.syncData();
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static boolean isNetworkAvailable(Context context) {   
        ConnectivityManager cm = (ConnectivityManager) context   
                .getSystemService(Context.CONNECTIVITY_SERVICE);   
        if (cm == null) {   
        } else {  
            NetworkInfo[] info = cm.getAllNetworkInfo();   
            if (info != null) {   
                for (int i = 0; i < info.length; i++) {   
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {   
                        return true;   
                    }   
                }   
            }   
        }   
        return false;   
    } 
	
	public SQLiteDatabase dbInit() {

		//��������default���ݿ⣬��һ��ʹ�û��ʼ������ʵ���Ӧ�ı�
		DBHelper helper=new DBHelper(getApplicationContext(), "default.db", null, 1);
		SQLiteDatabase defaultdb=helper.getWritableDatabase();

		//��ѯdefault���ݿ���Configuration���loginUser,������Ӧ��db
		ModelDaoImp mdi = new ModelDaoImp(defaultdb);
		String userId = mdi.getUserConfiguration().getLoginUser();
		if (userId.equals("default")) {
			return defaultdb;
		}else {
			DBHelper helper1=new DBHelper(getApplicationContext(), userId+".db", null, 1);
			SQLiteDatabase userdb=helper1.getWritableDatabase();
			return userdb;
		}
	}
	
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
