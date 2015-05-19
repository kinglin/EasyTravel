package com.kinglin.easytravel;


import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.kinglin.dao.DBHelper;
import com.kinglin.dao.ModelDaoImp;
import com.kinglin.model.Configuration;
import com.kinglin.model.User;
import com.kinglin.serverconnect.ServerConnection;
import com.kinglin.service.PushTreasureService;
import com.kinglin.service.SyncService;
import com.kinglin.service.TrackService;

@SuppressLint({ "InflateParams", "HandlerLeak", "ShowToast", "ClickableViewAccessibility" })
public class LoginActivity extends Activity {

	static final int REGISTER_SUCCESS = 1;
	static final int REGISTER_FAILED = 2;
	static final int LOGIN_SUCCESS = 3;
	static final int LOGIN_FAILED = 4;
	static final int GET_USER_DATA_SUCCESS = 5;
	static final int GET_USER_DATA_FAILED = 6;
	static final int NETWORK_PROBLEM = 7;
	static final int GETTING_DATA = 8;
	
	RelativeLayout rlLogin;
	ImageView ivTurnToSetting;
	EditText etLoginUserName,etLoginPassword;
	Button btnLogin,btnSignUp;
	//Button btnQQLogin,btnWeiboLogin;
	
	User registerUser = new User();
	User loginUser = new User();
	
	MyHandler myHandler;
	
	//ProgressDialog progressDialog = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_login);
		
		initContext();
		myHandler = new MyHandler();
		
		ivTurnToSetting.setOnClickListener(new TurnToSettingClickListener());
		btnLogin.setOnClickListener(new LoginClickListener());
		//btnQQLogin.setOnClickListener(new OtherLoginClickListener());
		//btnWeiboLogin.setOnClickListener(new OtherLoginClickListener());
		btnSignUp.setOnClickListener(new SignUpBtnClickListener());
		
		View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					v.setScaleX(0.9f);
					v.setScaleY(0.9f);
					break;
				case MotionEvent.ACTION_UP:
					v.setScaleX(1f);
					v.setScaleY(1f);
					break;
				default:
					break;
				}
				return false;
			}
		};
		btnLogin.setOnTouchListener(mOnTouchListener);
		btnSignUp.setOnTouchListener(mOnTouchListener);
	}
	
	//初始化控件
	private void initContext(){
		rlLogin = (RelativeLayout) findViewById(R.id.rl_login);
		ivTurnToSetting = (ImageView) findViewById(R.id.iv_turnToSetting);
		etLoginUserName = (EditText) findViewById(R.id.et_loginUserName);
		etLoginPassword = (EditText) findViewById(R.id.et_loginPassword);
		btnLogin = (Button) findViewById(R.id.btn_login);
		//btnQQLogin = (Button) findViewById(R.id.btn_qqlogin);
		//btnWeiboLogin = (Button) findViewById(R.id.btn_weiboLogin);
		btnSignUp = (Button) findViewById(R.id.btn_signup);
	}
	
	//点击设置按钮的事件响应
	private class TurnToSettingClickListener implements android.view.View.OnClickListener{

		@Override
		public void onClick(View v) {
			Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
			startActivity(intent);
			overridePendingTransition(R.anim.right_top_in, R.anim.left_bottom_out);
		}
	}
	
	//点击登录按钮的事件响应
	private class LoginClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			String username = etLoginUserName.getText().toString();
			String password = etLoginPassword.getText().toString();
			
			if(username.equals("")|| password.equals("")){
				Toast.makeText(getApplicationContext(),"please enter username&password", 1000).show();
			}else {
				loginUser.setUsername(username);
				loginUser.setPassword(password);
				
				LoginThread loginThread = new LoginThread();
				loginThread.start();
			}
		}
	}
	
	
	/*private class OtherLoginClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
			View view = LayoutInflater.from(LoginActivity.this).inflate(R.layout.other_login_dlg, null);
			builder.setView(view);
			
			final EditText etOtherLoginUsername= (EditText) view.findViewById(R.id.et_otherLoginUsername);
			final EditText etOtherLoginPassword = (EditText) view.findViewById(R.id.et_otherLoginPassword);
			
			if (v.getId() == R.id.btn_qqlogin) {
				builder.setTitle("QQ");
			}
			else if(v.getId() == R.id.btn_weiboLogin){
				builder.setTitle("Weibo");
			}
				
			builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
					String username = etOtherLoginUsername.getText().toString();
					String password = etOtherLoginPassword.getText().toString();
					Toast.makeText(LoginActivity.this, username+","+password, Toast.LENGTH_SHORT).show();
				}
			});
			builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
				}
			});
			
			builder.show();
			
		}	
	}*/
	
	//点击注册按钮的事件响应
	private class SignUpBtnClickListener implements OnClickListener{
		
		@Override
		public void onClick(View v) {
			View view = LayoutInflater.from(LoginActivity.this).inflate(R.layout.popwin_signup, null);
			final PopupWindow popwinSignup = new PopupWindow(view, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			
			final EditText etSignupUsername= (EditText) view.findViewById(R.id.et_signupUsername);
			final EditText etSignupPassword = (EditText) view.findViewById(R.id.et_signupPassword);
			final EditText etSignupConfirmPsd= (EditText) view.findViewById(R.id.et_signupConfirmPsd);
			final Button btnSignupCancel = (Button) view.findViewById(R.id.btn_signupCancel);
			final Button btnSignupOK = (Button) view.findViewById(R.id.btn_signupOK);
			
			btnSignupOK.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					
					String username = etSignupUsername.getText().toString();
					String password = etSignupPassword.getText().toString();
					String confirmpsd = etSignupConfirmPsd.getText().toString();
					if (!password.equals(confirmpsd)) {
						etSignupConfirmPsd.setText("");
						Toast.makeText(LoginActivity.this, "conformpsd is not in accord with password", Toast.LENGTH_SHORT).show();
					}
					else {
						
						registerUser.setUsername(username);
						registerUser.setPassword(password);
						RegisterThread registerThread = new RegisterThread();
						registerThread.start();
					}
					popwinSignup.dismiss();
				}
			});
			btnSignupCancel.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					popwinSignup.dismiss();
				}
			});
			
			View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
				
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						v.setScaleX(0.9f);
						v.setScaleY(0.9f);
						break;
					case MotionEvent.ACTION_UP:
						v.setScaleX(1f);
						v.setScaleY(1f);
						break;
					default:
						break;
					}
					return false;
				}
			};
			btnSignupOK.setOnTouchListener(mOnTouchListener);
			btnSignupCancel.setOnTouchListener(mOnTouchListener);
			
			popwinSignup.setFocusable(true);
			popwinSignup.setBackgroundDrawable(getResources().getDrawable(R.drawable.popwin_bg));
			popwinSignup.setAnimationStyle(R.style.popwin_anim_style);
			popwinSignup.showAtLocation(rlLogin, Gravity.CENTER, 0, 0);
			
			//弹出框时父窗口颜色变暗
			WindowManager.LayoutParams lp = getWindow().getAttributes();  
			lp.alpha = 0.7f;  
			getWindow().setAttributes(lp);
			//窗口消失时父窗口颜色恢复正常
			popwinSignup.setOnDismissListener(new OnDismissListener() {
			    @Override  
			    public void onDismiss() {  
			        WindowManager.LayoutParams lp = getWindow().getAttributes();  
			        lp.alpha = 1f;  
			        getWindow().setAttributes(lp); 
			    }  
			});
			
		}
	}

	
	public class RegisterThread extends Thread{
		public void run() {
			
			ServerConnection sc = new ServerConnection();
			Message msg = Message.obtain();
			
			try {
				JSONObject json_registerResult = sc.UserRegister(registerUser);
				
				if (json_registerResult.get("registerResult").equals("success")) {
					msg.arg1 = REGISTER_SUCCESS;
					myHandler.sendMessage(msg);
				}else {
					msg.arg1 = REGISTER_FAILED;
					msg.obj = json_registerResult.getString("registerResult");
					myHandler.sendMessage(msg);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (IOException e) {
				msg.arg1 = NETWORK_PROBLEM;
				myHandler.sendMessage(msg);
				e.printStackTrace();
			}
		}
	}
	
	//登陆线程
	public class LoginThread extends Thread{
		
		public void run() {
			
			ServerConnection sc = new ServerConnection();
			Message msg = Message.obtain();
			long userId;
			
			try {
				JSONObject json_loginUser = sc.UserLogin(loginUser);
				
				if(!json_loginUser.get("loginResult").toString().equals("yes")){
					msg.arg1 = LOGIN_FAILED;
					msg.obj = json_loginUser.get("loginResult").toString();
					myHandler.sendMessage(msg);
				}else{
					
					//登陆成功时，再次向服务器发送请求，
					msg.arg1 = LOGIN_SUCCESS;
					myHandler.sendMessage(msg);
					userId = (long) json_loginUser.get("userId");
					
					DBHelper helper=new DBHelper(getApplicationContext(), "default.db", null, 1);
				    SQLiteDatabase defaultdb=helper.getWritableDatabase();
				   
				    ModelDaoImp mdi = new ModelDaoImp(defaultdb);
				    boolean hasBeenLogedIn = mdi.hasBeenLogedIn(userId);
				    
				    if (!hasBeenLogedIn || hasBeenLogedIn) {
				    	Configuration configuration = new Configuration(1, String.valueOf(userId), 0, 1,1, "", 0);
				    	mdi.setUserConfiguration(configuration);
				    	defaultdb.close();
						GetAllUserDataThread getAllUserDataThread = new GetAllUserDataThread(userId);
						getAllUserDataThread.start();
					}else {
						helper=new DBHelper(getApplicationContext(), userId+".db", null, 1);
					    SQLiteDatabase userdb=helper.getWritableDatabase();
					    mdi = new ModelDaoImp(userdb);
					    JSONObject json_userAllDataSnapshot = mdi.getAllDataSnapshot();
					    GetUserChangedDataThread getUserChangedDataThread = new GetUserChangedDataThread(userId,json_userAllDataSnapshot);
					    getUserChangedDataThread.start();
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (IOException e) {
				msg.arg1 = NETWORK_PROBLEM;
				myHandler.sendMessage(msg);
				e.printStackTrace();
			}
			
		}
	}
	
	//鏉╂瑦妲搁柦鍫濐嚠缁楊兛绔村▎鈥虫躬鐠囥儲澧滈張杞扮瑐閻у妾伴惃鍕暏閹达拷
	//閸氭垶婀囬崝鈥虫珤鐠囬攱鐪伴懢宄板絿閻€劍鍩涢幍锟芥箒閻ㄥ嫪淇婇幁锟介悞璺烘倵娣囨繂鐡ㄩ崚鐗堟拱閸︾増鏆熼幑顔肩氨閿涘苯鑻熸潪顒�煂UserInformationActivity
	public class GetAllUserDataThread extends Thread{
		
		long userId;
		GetAllUserDataThread(long userId){
			this.userId = userId;
		}
		
		public void run(){
			ServerConnection sc = new ServerConnection();
			Message msg = Message.obtain();
			
			//閼惧嘲褰囬悽銊﹀煕閹碉拷婀佹穱鈩冧紖
			JSONObject json_allUserData;
			try {
				json_allUserData = sc.getAllUserData(userId);
				if (json_allUserData != null) {
					msg.arg1 = GET_USER_DATA_SUCCESS;
					myHandler.sendMessage(msg);
					
					//娑撹櫣鏁ら幋宄扮磻閸掓稑缂撻弫鐗堝祦鎼存搫绱濋獮璺虹殺閹碉拷婀佹穱鈩冧紖鐎涙绻橀崢锟�
					DBHelper helper=new DBHelper(getApplicationContext(), userId+".db", null, 1);
				    SQLiteDatabase userdb=helper.getWritableDatabase();
				    ModelDaoImp mdi = new ModelDaoImp(userdb);
				    
				    if (mdi.saveNewUserAllData(json_allUserData)) {
				    	//鐏忓棜顕氶悽銊﹀煕娑擃亙姹夋穱鈩冧紖娴肩姴鍩孶serInformationActivity
					    User user = mdi.getUserInformation();
					    
					    mdi = new ModelDaoImp(userdb);
					    if (mdi.getUserConfiguration().getTrackOrNot() == 0 
					    		&& isServiceRunning(getApplicationContext(), "com.kinglin.service.TrackService")) {
					    	Intent serviceintent = new Intent(getApplicationContext(),TrackService.class);
							stopService(serviceintent);
						}else if (mdi.getUserConfiguration().getTrackOrNot() == 1 
					    		&& (!isServiceRunning(getApplicationContext(), "com.kinglin.service.TrackService"))) {
							Intent serviceintent = new Intent(getApplicationContext(),TrackService.class);
							startService(serviceintent);
							Toast.makeText(getApplicationContext(), "TrackService start", 100);
						}
					    
					    if (mdi.getUserConfiguration().getAutoPush() == 0 
					    		&& isServiceRunning(getApplicationContext(), "com.kinglin.service.PushTreasureService")) {
					    	Intent serviceintent = new Intent(getApplicationContext(),PushTreasureService.class);
							stopService(serviceintent);
						}else if (mdi.getUserConfiguration().getAutoPush() == 1 
					    		&& (!isServiceRunning(getApplicationContext(), "com.kinglin.service.PushTreasureService"))) {
							Intent serviceintent = new Intent(getApplicationContext(),PushTreasureService.class);
							startService(serviceintent);
							Toast.makeText(getApplicationContext(), "PushTreasureService start", 100);
						}
					    
					    if (mdi.getUserConfiguration().getSyncByWifi() == 1) {
					    	Intent serviceintent = new Intent(getApplicationContext(),SyncService.class);
							startService(serviceintent);
							Toast.makeText(getApplicationContext(), "SyncService start", 100);
						}
					    
					    userdb.close();
					    
					    helper=new DBHelper(getApplicationContext(), "default.db", null, 1);
					    SQLiteDatabase defaultdb=helper.getWritableDatabase();
					    mdi = new ModelDaoImp(defaultdb);
					    mdi.saveUser(user);
					    defaultdb.close();
					    
					    Intent intent = new Intent(getApplicationContext(),UserInformationActivity.class);
					    intent.putExtra("userLogin", (Serializable)user);
					    startActivity(intent);
					    LoginActivity.this.finish();
					}
				}else {
					msg.arg1 = GET_USER_DATA_FAILED;
					myHandler.sendMessage(msg);
				}
//				msg.arg1 = GET_USER_DATA_SUCCESS;
//				myHandler.sendMessage(msg);
			} catch (IOException | JSONException e) {
				e.printStackTrace();
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
	
	
	//鏉╂瑦妲搁柦鍫濐嚠閺囧墽绮￠崷銊嚉閹靛婧�稉濠勬闂勫棜绻冮惃鍕暏閹达拷
	//娴肩姴鍙嗛惃鍕嚉閻€劍鍩涢幍锟芥箒娣団剝浼呴惃鍒琩閸滃本娓堕崥搴濇叏閺�妞傞梻杈剧礉娴犲孩婀囬崝鈩冪湴閼惧嘲褰囩憰浣锋叏閺�湱娈戦幎濠傚敶鐎圭懓鑻熺�妯哄煂閺佺増宓佹惔鎿勭礉閻掕泛鎮楁潪顒�煂UserInformationActivity
	public class GetUserChangedDataThread extends Thread{
		long userId;
		JSONObject json_userAllDataSnapshot;
		
		GetUserChangedDataThread(long userId,JSONObject json_userAllDataSnapshot){
			this.userId = userId;
			this.json_userAllDataSnapshot = json_userAllDataSnapshot;
		}
		
		public void run(){
			ServerConnection sc = new ServerConnection();
			JSONObject json_userAllChangedData= sc.getAllUserChangedData(userId,json_userAllDataSnapshot);
			
			DBHelper helper=new DBHelper(getApplicationContext(), userId+".db", null, 1);
		    SQLiteDatabase userdb=helper.getWritableDatabase();
		    ModelDaoImp mdi = new ModelDaoImp(userdb);
		    
		    if (mdi.saveUserChangedData(json_userAllChangedData)) {
		    	User user = mdi.getUserInformation();
			    Intent intent = new Intent(getApplicationContext(),UserInformationActivity.class);
			    intent.putExtra("userLogin", (Serializable)user);
			    startActivity(intent);
			}
		}
	}
	
	public class MyHandler extends Handler{

		MyHandler() {  
			super(); 
		}  

		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			switch (msg.arg1) {
			case NETWORK_PROBLEM:
				Toast.makeText(getApplicationContext(), "network problem", 1000).show();
				break;
			case REGISTER_SUCCESS:
				Toast.makeText(getApplicationContext(), "register success,please login", 1000).show();
				break;
			case REGISTER_FAILED:
				Toast.makeText(getApplicationContext(), (CharSequence) msg.obj, 1000).show();
				break;
			case LOGIN_SUCCESS:
				Toast.makeText(getApplicationContext(), "login success", 1000).show();
//				progressDialog = new ProgressDialog(getApplicationContext());
//				progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//				progressDialog.setTitle("login success");
//				progressDialog.setMessage("getting your data");
//				progressDialog.setIcon(R.drawable.ic_cloud);
//				progressDialog.setIndeterminate(false);
//				progressDialog.setCancelable(true);
//				progressDialog.show();
				break;
			case LOGIN_FAILED:
				Toast.makeText(getApplicationContext(), (CharSequence) msg.obj, 1000).show();
				break;
			case GET_USER_DATA_SUCCESS:
				Toast.makeText(getApplicationContext(), "get user data success", 2000).show();
//				if (progressDialog!=null) {
//					progressDialog.cancel();
//				}
				break;
			case GET_USER_DATA_FAILED:
				Toast.makeText(getApplicationContext(), "get user data failed", 1000).show();
//				if (progressDialog!=null) {
//					progressDialog.cancel();
//				}
				break;
			default:
				break;
			}
		}
	}
	
	//点击返回键的事件响应
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) { //按下的如果是BACK，同时没有重复
			
			LoginActivity.this.finish();
			overridePendingTransition(R.anim.right_in, R.anim.left_out);
		    return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
