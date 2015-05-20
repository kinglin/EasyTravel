package com.kinglin.serverconnect;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.database.sqlite.SQLiteDatabase;

import com.kinglin.dao.ModelDaoImp;
import com.kinglin.model.User;

public class ServerConnection {

	SQLiteDatabase db;
	
	String baseUrl = "http://192.168.1.109:9000/App";
	
	public ServerConnection() {
	}
	
	public ServerConnection(SQLiteDatabase db) {
		this.db = db;
	}
	
	public void syncData() throws ClientProtocolException, IOException, JSONException {
		
		//当开启wifi下同步时，每两分钟就会调用一次这个函数
		ModelDaoImp mdi = new ModelDaoImp(db);
		JSONObject json_allChangedData = mdi.getAllChangedData();
		if (json_allChangedData.length()!=0) {
			
			//进入这里代表有数据需要同步
			String url = baseUrl+"/SyncData";
			HttpPost httpPost = new HttpPost(url);
			NameValuePair strUserId = new BasicNameValuePair("struserId", String.valueOf(mdi.getUserInformation().getUserId()));
			NameValuePair allChangedData = new BasicNameValuePair("json_allChangedData", json_allChangedData+"");
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(strUserId);
			nameValuePairs.add(allChangedData);
			HttpEntity httpEntity = new UrlEncodedFormEntity(nameValuePairs);
			
			httpPost.setEntity(httpEntity);
			
			//将数据推送到服务器
			HttpClient httpClient = new DefaultHttpClient();
			HttpResponse httpResponse = httpClient.execute(httpPost);
			
			//从服务器获得返回值
			httpEntity = httpResponse.getEntity();
			InputStream inputStream = httpEntity.getContent();
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
			
			String line = "";
			String resultString = "";
			while ((line = bufferedReader.readLine())!=null) {
				resultString = resultString+line;
			}
			
			//对数据库的返回json进行处理
			resultString = resultString.substring(1, resultString.length()-1);
			resultString = resultString.replace("\\", "");
			JSONTokener jsonTokener = new JSONTokener(resultString);
			JSONObject json_syncResult = (JSONObject) jsonTokener.nextValue();
			
			inputStream.close();
			
			//如果返回成功，修改当前数据库中数据的operation标记
			if (json_syncResult.getString("syncResult").equals("yes")) {
				mdi.changeAllOperation();
			}
		}
	}

	//处理用户注册
	public JSONObject UserRegister(User registerUser) throws JSONException, IOException{
		
		//告知服务器这里是注册操作
		String url = baseUrl+"/Register";
		HttpPost httpPost = new HttpPost(url);
		NameValuePair username = new BasicNameValuePair("username", registerUser.getUsername());
		NameValuePair password = new BasicNameValuePair("password", registerUser.getPassword());
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(username);
		nameValuePairs.add(password);
		HttpEntity httpEntity = new UrlEncodedFormEntity(nameValuePairs);
		
		httpPost.setEntity(httpEntity);
		
		//向服务器发送数据
		HttpClient httpClient = new DefaultHttpClient();
		HttpResponse httpResponse = httpClient.execute(httpPost);
		
		//从服务器获得返回值
		httpEntity = httpResponse.getEntity();
		InputStream inputStream = httpEntity.getContent();
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
		
		String line = "";
		String resultString = "";
		while ((line = bufferedReader.readLine())!=null) {
			resultString = resultString+line;
		}
		resultString = resultString.substring(1, resultString.length()-1);
		resultString = resultString.replace("\\", "");
		JSONTokener jsonTokener = new JSONTokener(resultString);
		JSONObject json_registerResult = (JSONObject) jsonTokener.nextValue();
		
		inputStream.close();
		
		return json_registerResult;
	}
	
	//处理用户登陆
	public JSONObject UserLogin(User loginUser) throws IOException, JSONException{
		
		//告知进行登陆验证
		String url = baseUrl+"/Login";
		HttpPost httpPost = new HttpPost(url);
		NameValuePair username = new BasicNameValuePair("username", loginUser.getUsername());
		NameValuePair password = new BasicNameValuePair("password", loginUser.getPassword());
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(username);
		nameValuePairs.add(password);
		HttpEntity httpEntity = new UrlEncodedFormEntity(nameValuePairs);

		httpPost.setEntity(httpEntity);

		//向服务器发送数据
		HttpClient httpClient = new DefaultHttpClient();
		HttpResponse httpResponse = httpClient.execute(httpPost);

		//从服务器获得返回值
		httpEntity = httpResponse.getEntity();
		InputStream inputStream = httpEntity.getContent();
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

		String line = "";
		String resultString = "";
		while ((line = bufferedReader.readLine())!=null) {
			resultString = resultString+line;
		}
		resultString = resultString.substring(1, resultString.length()-1);
		resultString = resultString.replace("\\", "");
		JSONTokener jsonTokener = new JSONTokener(resultString);
		JSONObject json_loginResult = (JSONObject) jsonTokener.nextValue();

		inputStream.close();

		return json_loginResult;	
	}

	//获得当前用户的所有数据
	public JSONObject getAllUserData(long userId) throws IOException, JSONException {
		
		//告知获得全部数据
		String url = baseUrl+"/GetAllUserData";
		HttpPost httpPost = new HttpPost(url);
		NameValuePair t_userId = new BasicNameValuePair("struserId", String.valueOf(userId));
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(t_userId);
		HttpEntity httpEntity = new UrlEncodedFormEntity(nameValuePairs);

		httpPost.setEntity(httpEntity);

		//向服务器发送数据
		HttpClient httpClient = new DefaultHttpClient();
		HttpResponse httpResponse = httpClient.execute(httpPost);

		//从服务器获得返回值
		httpEntity = httpResponse.getEntity();
		InputStream inputStream = httpEntity.getContent();
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

		String line = "";
		String resultString = "";
		while ((line = bufferedReader.readLine())!=null) {
			resultString = resultString+line;
		}
		resultString = resultString.substring(1, resultString.length()-1);
		resultString = resultString.replace("\\", "");
		JSONTokener jsonTokener = new JSONTokener(resultString);
		JSONObject json_getAllUserDataResult = (JSONObject) jsonTokener.nextValue();

		inputStream.close();

		return json_getAllUserDataResult;
	}

	public JSONObject getAllUserChangedData(long userId,JSONObject json_userAllDataSnapshot){
		return null;
	}

}
