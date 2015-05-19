package com.kinglin.easytravel;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.kinglin.dao.DBHelper;
import com.kinglin.dao.FileService;
import com.kinglin.dao.ModelDaoImp;
import com.kinglin.model.User;

@SuppressLint({ "ShowToast", "InflateParams" })
public class UserInformationActivity extends Activity {
	
	public static UserInformationActivity instanceActivity = null;
	
	RelativeLayout rlUserInfo;
	Button btnUserInfoCoinDetail,btnUserInfoTreasureDetail;
	EditText etUserInfoUsername,etUserInfoBirthday,etUserInfoHobby,etUserInfoCoin,etUserInfoTreasure;
	ImageButton ibtnUserInfoIcon,ibtnUserInfoSelectBirth,ibtnUserInfoEdit,btnTurnToSetting;
	RadioGroup rgUserInfoGender;
	RadioButton rbtnUserInfoMale,rbtnUserInfoFemale;
	Button btnUserInfoCancel,btnUserInfoSave;
	LinearLayout llayoutUerInfoOperation;
	PopupWindow popSelectUserIcon;
	
	String userIconPath = null, photoFilePath = null;
	
	Calendar calendar;
	private int mYear;
	private int mMonth;
	private int mDay;
	
	User user;
	int gender;
	
	SQLiteDatabase db;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_user_information);
		
		instanceActivity = this;
		initContext();
		db = dbInit();
		
		//user = (User) getIntent().getSerializableExtra("userLogin");
		ModelDaoImp mdi = new ModelDaoImp(db);
		user = mdi.getUserInformation();
		
		gender = user.getGender();
		userIconPath = user.getPicture();
		showInformation(user);
		
		btnTurnToSetting.setOnClickListener(new TurnToSettingClickListener());
		ibtnUserInfoIcon.setOnClickListener(new SelectIconClickListener());
		
		rgUserInfoGender.setOnCheckedChangeListener(new SelectGenderListener());
		ibtnUserInfoSelectBirth.setOnClickListener(new SelectBirthClickListener());
		
		btnUserInfoCoinDetail.setOnClickListener(new CoinDetailClickListener());
		btnUserInfoTreasureDetail.setOnClickListener(new TreasureDetailClickListener());
		
		ibtnUserInfoEdit.setOnClickListener(new UserInfoEditClickListener());
		
		btnUserInfoCancel.setOnClickListener(new UserInfoCancelClickListener());
		btnUserInfoSave.setOnClickListener(new UserInfoSaveClickListener());
		
		View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
			
			@SuppressLint("ClickableViewAccessibility")
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
		btnUserInfoCancel.setOnTouchListener(mOnTouchListener);
		btnUserInfoSave.setOnTouchListener(mOnTouchListener);
		
	}
	
	//初始化控件和变量
	private void initContext(){
		rlUserInfo = (RelativeLayout) findViewById(R.id.rl_userInfomation);
		btnTurnToSetting = (ImageButton) findViewById(R.id.btn_turnToSetting);
		ibtnUserInfoIcon = (ImageButton) findViewById(R.id.ibtn_userInfoIcon);
		etUserInfoUsername = (EditText) findViewById(R.id.et_userInfoUsername);
		
		rgUserInfoGender = (RadioGroup) findViewById(R.id.rg_userInfoGender);
		rbtnUserInfoMale = (RadioButton) findViewById(R.id.rbtn_userInfoMale);
		rbtnUserInfoFemale = (RadioButton) findViewById(R.id.rbtn_userInfoFemale);
		
		etUserInfoBirthday = (EditText) findViewById(R.id.et_userInfoBirthday);
		ibtnUserInfoSelectBirth = (ImageButton) findViewById(R.id.ibtn_userInfoSelectBirth);
		etUserInfoHobby = (EditText) findViewById(R.id.et_userInfoHobby);
		
		etUserInfoCoin = (EditText) findViewById(R.id.et_userInfoCoin);
		btnUserInfoCoinDetail = (Button) findViewById(R.id.btn_userInfoCoinDetail);
		etUserInfoTreasure = (EditText) findViewById(R.id.et_userInfoTeasure);
		btnUserInfoTreasureDetail = (Button) findViewById(R.id.btn_userInfoTeasureDetail);
		
		ibtnUserInfoEdit = (ImageButton) findViewById(R.id.ibtn_userInfoEdit);
		llayoutUerInfoOperation = (LinearLayout) findViewById(R.id.ll_userInfoOperation);
		btnUserInfoCancel = (Button) findViewById(R.id.btn_userInfoCancel);
		btnUserInfoSave = (Button) findViewById(R.id.btn_userInfoSave);
		
		ibtnUserInfoIcon.setEnabled(false);
		rbtnUserInfoMale.setClickable(false);
		rbtnUserInfoFemale.setClickable(false);
		ibtnUserInfoSelectBirth.setEnabled(false);
		etUserInfoHobby.setFocusable(false);
		
		//获取当前的年月日
		calendar = Calendar.getInstance();
		
		mYear = calendar.get(Calendar.YEAR);
		mMonth = calendar.get(Calendar.MONTH);
		mDay = calendar.get(Calendar.DAY_OF_MONTH);
		
		Bitmap bmp = ((BitmapDrawable)ibtnUserInfoIcon.getDrawable()).getBitmap();
		ibtnUserInfoIcon.setImageBitmap(drawImageDropShadow(getRoundBitmap(bmp)));		//将图片处理成圆形并添加阴影
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
	
	//将图片处理为圆形
	private Bitmap getRoundBitmap(Bitmap bitmap){
		
		int width = bitmap.getWidth();
	    int height = bitmap.getHeight();
	    int left = 0, top = 0, right = width, bottom = height;
	    float roundPx = height/2;
	    if (width > height) {
	      left = (width - height)/2;
	      top = 0;
	      right = left + height;
	      bottom = height;
	    } else if (height > width) {
	      left = 0;
	      top = (height - width)/2;
	      right = width;
	      bottom = top + width;
	      roundPx = width/2;
	    }

	    Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
	    Canvas canvas = new Canvas(output);
	    int color = 0xff424242;
	    Paint paint = new Paint();
	    Rect rect = new Rect(left, top, right, bottom);
	    RectF rectF = new RectF(rect);

	    paint.setAntiAlias(true);
	    canvas.drawARGB(0, 0, 0, 0);
	    paint.setColor(color);
	    canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
	    paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
	    canvas.drawBitmap(bitmap, rect, rect, paint);
	    
	    return output;
	}
	
	//给图片添加阴影
	public Bitmap drawImageDropShadow(Bitmap bitmap) {

		Bitmap roundBitmap = Bitmap.createBitmap(bitmap.getWidth()+10, 
		         bitmap.getHeight()+10, Config.ARGB_8888);
		Canvas canvas = new Canvas(roundBitmap); 
		
		Paint mPaint = new Paint();
		BlurMaskFilter bf = new BlurMaskFilter(20,BlurMaskFilter.Blur.INNER);
		int color = Color.parseColor("#FC6802");
		mPaint.setColor(color);
		mPaint.setMaskFilter(bf);

		canvas.drawBitmap(bitmap.extractAlpha(mPaint, null), 5,10, mPaint);
		canvas.drawBitmap(bitmap,0,0,null);
		
		return roundBitmap;
	}

	//显示数据库中存储的用户基本信息
	private void showInformation(User user){
		
		etUserInfoUsername.setText(user.getUsername());
		
		if (user.getGender() == 1) {
			rbtnUserInfoMale.setChecked(true);
		}
		else if (user.getGender() == 0) {
			rbtnUserInfoFemale.setChecked(true);
		}
		etUserInfoBirthday.setText(user.getBirthday());
		etUserInfoHobby.setText(user.getHobby());
		
		//用户头像处理，不为空时处理成圆形图片显示
		if (user.getPicture().equals("") == false) {
			File file = new File(user.getPicture());
			if (file.exists()) {
				Bitmap bitmap = BitmapFactory.decodeFile(user.getPicture());
				
				FileService fileService = new FileService(getApplicationContext());
	     		bitmap = fileService.confessBitmap(bitmap);
	     		
	     		ibtnUserInfoIcon.setImageBitmap(drawImageDropShadow(getRoundBitmap(bitmap)));
			}
		}
		
		ModelDaoImp mdi = new ModelDaoImp(db);
//		int coinPoint = mdi.getCoinPoint();
		int treasureCount = mdi.getTreasureCount();
		//etUserInfoCoin.setText(coinPoint);
		etUserInfoTreasure.setText(String.valueOf(treasureCount));
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
	
	//点击选择头像按钮的事件响应
	private class SelectIconClickListener implements android.view.View.OnClickListener{

		@Override
		public void onClick(View v) {
			View view = LayoutInflater.from(UserInformationActivity.this).inflate(R.layout.popmenu_selectpic, null);
			popSelectUserIcon = new PopupWindow(view, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			
			Button btnTakeAPhoto = (Button) view.findViewById(R.id.btn_takeAPhoto);
			Button btnSelectPicFromAlbum = (Button) view.findViewById(R.id.btn_selectPicFromAlbum);
			Button btnSelectPicCancel = (Button) view.findViewById(R.id.btn_selectPicCancel);
			
			btnTakeAPhoto.setOnClickListener(new TakeAPhotoClickListener());
			btnSelectPicFromAlbum.setOnClickListener(new SelectFromAlbumClickListener());
			btnSelectPicCancel.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					popSelectUserIcon.dismiss();
				}
			});
			
			popSelectUserIcon.setFocusable(true);
			popSelectUserIcon.setAnimationStyle(R.style.popwin_bottom_anim_style);
			ColorDrawable cDrawable = new ColorDrawable(0x00000000);
			popSelectUserIcon.setBackgroundDrawable(cDrawable);
			
			popSelectUserIcon.showAtLocation(rlUserInfo, Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);
			
			WindowManager.LayoutParams lp = getWindow().getAttributes();  
	        lp.alpha = 0.7f;  
	        getWindow().setAttributes(lp);
	        
			popSelectUserIcon.setOnDismissListener(new OnDismissListener() {
				@Override
				public void onDismiss() {
					WindowManager.LayoutParams lp = getWindow().getAttributes();  
			        lp.alpha = 1f;  
			        getWindow().setAttributes(lp); 
				}
			});
		}
	}
	
	//从相册中选择图片
	private class SelectFromAlbumClickListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent();    
		    intent.setType("image/*");   
		    intent.setAction(Intent.ACTION_GET_CONTENT);   
		    startActivityForResult(intent, 1);
		    
		    popSelectUserIcon.dismiss();
		}
	}
    
	//拍摄照片
    private class TakeAPhotoClickListener implements View.OnClickListener{

		@Override
		public void onClick(View v) {
			Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
			
			FileService service=new FileService(getApplicationContext());
   		 	photoFilePath = service.getPhotoFilePath();
			intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(photoFilePath)));
			startActivityForResult(intent, 2); 
			
			popSelectUserIcon.dismiss();
		}
    }
    
    //接收子窗口关闭时传回来的数据
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);  
		
        if (requestCode == 1 && resultCode == RESULT_OK) {//从相册选择图片
        	
        	Uri selectedImage = data.getData();
        	ContentResolver cr = this.getContentResolver();
			try {
				Bitmap bitmap = BitmapFactory.decodeStream(cr.openInputStream(selectedImage));
				  
	            String[] filePathColumn = { MediaStore.Images.Media.DATA };
	            Cursor cursor = getContentResolver().query(selectedImage,
	                    filePathColumn, null, null, null);
	            cursor.moveToFirst();
	            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
	            userIconPath = cursor.getString(columnIndex);
	            
	            //显示图片
	            FileService fileService = new FileService(getApplicationContext());
	     		bitmap = fileService.confessBitmap(bitmap);
	     		ibtnUserInfoIcon.setImageBitmap(drawImageDropShadow(getRoundBitmap(bitmap)));
	     		
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
     		
        }
        if (requestCode == 2 && resultCode == RESULT_OK) {//拍照图片获取 
    		
        	Bitmap bitmap = BitmapFactory.decodeFile(photoFilePath);
        	
        	FileService fileService = new FileService(getApplicationContext());
     		bitmap = fileService.confessBitmap(bitmap);
        	
        	ibtnUserInfoIcon.setImageBitmap(drawImageDropShadow(getRoundBitmap(bitmap)));
        	
			userIconPath = photoFilePath;
        }
        
    }
	
	//选择性别的事件响应
	private class SelectGenderListener implements OnCheckedChangeListener{

		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			
			if (UserInformationActivity.this.rbtnUserInfoMale.getId() == checkedId) {
				gender = 1;
			}
			else if (UserInformationActivity.this.rbtnUserInfoFemale.getId() == checkedId) {
				gender = 0;
			}
		}
	}
	
	//选择生日按钮的事件响应
	private class SelectBirthClickListener implements android.view.View.OnClickListener{

		@SuppressLint("NewApi")
		@Override
		public void onClick(View v) {
			int year, month, day;
			//若之前数据库中生日为空，则将日期选择控件初始化为当前日期；若不为空，则初始化为之前保存的日期
			if (etUserInfoBirthday.getText().toString().equals("")) {
				year = mYear;
				month = mMonth;
				day = mDay;
			}
			else {
				year = Integer.parseInt(etUserInfoBirthday.getText().toString().substring(0, 4));
				month = Integer.parseInt(etUserInfoBirthday.getText().toString().substring(5, 7)) - 1;
				day = Integer.parseInt(etUserInfoBirthday.getText().toString().substring(8, 10));
			}
			
			DatePickerDialog dpdlg = new DatePickerDialog(UserInformationActivity.this,
					new DateSelectListener(), year, month, day);
			dpdlg.getDatePicker().setMaxDate(calendar.getTimeInMillis());	//设置最大日期为当天
			dpdlg.show();
		}
	}
	
	//日期控件的回调函数
	private class DateSelectListener implements OnDateSetListener{

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			
			mYear = year;
			mMonth = monthOfYear;
			mDay = dayOfMonth;
			
			etUserInfoBirthday.setText(new StringBuilder().append(mYear).append(
					"-"+((mMonth+1) < 10 ? "0"+(mMonth+1) : (mMonth+1))).append(
					"-"+((mDay < 10) ? "0"+mDay : mDay)));
			
		}
	}
	
	//点击积分详情按钮的事件响应
	private class CoinDetailClickListener implements android.view.View.OnClickListener{

		@Override
		public void onClick(View v) {
			
			Intent intent = new Intent(getApplicationContext(), CoinActivity.class);
			startActivity(intent);
			overridePendingTransition(R.anim.center_in, R.anim.normal_fade_out);
		}
	}
	
	//点击宝藏详情按钮的事件响应
	private class TreasureDetailClickListener implements android.view.View.OnClickListener{

		@Override
		public void onClick(View v) {
			
			Intent intent = new Intent(getApplicationContext(), TreasureActivity.class);
			startActivity(intent);
			overridePendingTransition(R.anim.center_in, R.anim.normal_fade_out);
		}
	}
	
	//点击编辑按钮的事件响应
	private class UserInfoEditClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			
			TranslateAnimation tAnimation = new TranslateAnimation(
					Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 10, 
					Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0);
			tAnimation.setDuration(500);
			tAnimation.setStartOffset(0);
			tAnimation.setFillAfter(true);
			tAnimation.setAnimationListener(new UserInfoEditClickAnimationListener());
			ibtnUserInfoEdit.startAnimation(tAnimation);
			
		}	
	}
	
	//给编辑按钮的动画添加监听效果
	private class UserInfoEditClickAnimationListener implements AnimationListener{

		@Override
		public void onAnimationStart(Animation animation) {

			TranslateAnimation tAnimationll = new TranslateAnimation(
					Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, 
					Animation.RELATIVE_TO_SELF, 1, Animation.RELATIVE_TO_SELF, 0);
			tAnimationll.setDuration(500);
			tAnimationll.setStartOffset(0);
			llayoutUerInfoOperation.startAnimation(tAnimationll);
			llayoutUerInfoOperation.setVisibility(View.VISIBLE);
			
		}

		//动画结束之后所有控件才处于可编辑状态
		@Override
		public void onAnimationEnd(Animation animation) {
			ibtnUserInfoEdit.setVisibility(View.GONE);
			ibtnUserInfoEdit.setClickable(false);
			
			ibtnUserInfoIcon.setEnabled(true);
			rbtnUserInfoMale.setClickable(true);
			rbtnUserInfoFemale.setClickable(true);
			ibtnUserInfoSelectBirth.setEnabled(true);
			etUserInfoHobby.setFocusableInTouchMode(true);
			
			btnUserInfoCoinDetail.setClickable(false);
			btnUserInfoTreasureDetail.setClickable(false);
			
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
		}
		
	}
	
	//点击取消按钮的事件响应
	private class UserInfoCancelClickListener implements android.view.View.OnClickListener{

		@Override
		public void onClick(View v) {
			UserInformationActivity.this.finish();
			overridePendingTransition(R.anim.right_in, R.anim.left_out);
		}
	}
	
	//点击保存按钮的事件响应
	private class UserInfoSaveClickListener implements android.view.View.OnClickListener{

		@Override
		public void onClick(View v) {
			
			user.setGender(gender);
			user.setBirthday(etUserInfoBirthday.getText().toString());
			user.setHobby(etUserInfoHobby.getText().toString());
			
			//图片处理,当现在的图片路径和数据库中存的不一样时，再将图片重新存入
			if (userIconPath.equals(user.getPicture()) == false) {
				
				FileService service=new FileService(getApplicationContext());
			    String fileName = "/easyTravel/savefile/";
			    service.createSDCardDir(fileName);
			    
			    File sdcardDir =Environment.getExternalStorageDirectory();
			    String newPath = sdcardDir.getPath() + fileName + "usericon." + service.getExtensionName(userIconPath);
			    service.copyFile(userIconPath, newPath);
			    
			    user.setPicture(userIconPath);
			}
			
			ModelDaoImp mdi = new ModelDaoImp(db);
			if (mdi.updateUser(user)) {
				Toast.makeText(getApplicationContext(), "save success", 500).show();
//				SyncThread syncThread = new SyncThread();
//				syncThread.start();
			}else {
				Toast.makeText(getApplicationContext(), "save failed", 500).show();
			}
			UserInformationActivity.this.finish();
			overridePendingTransition(R.anim.right_in, R.anim.left_out);
		}
	}
	
//	class SyncThread extends Thread{
//		public void run(){
//			ServerConnection sc =  new ServerConnection(db);
//			try {
//				sc.syncData();
//			} catch (ClientProtocolException e) {
//				e.printStackTrace();
//			} catch (IOException e) {
//				e.printStackTrace();
//			} catch (JSONException e) {
//				e.printStackTrace();
//			}
//		}
//	}
	
	//点击返回按钮的事件响应
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) { //按下的如果是BACK，同时没有重复
			
			UserInformationActivity.this.finish();
			overridePendingTransition(R.anim.right_in, R.anim.left_out);
		    return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
