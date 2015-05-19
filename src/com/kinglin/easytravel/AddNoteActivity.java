 package com.kinglin.easytravel;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.Video.VideoColumns;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationUtils;
import android.view.ext.SatelliteMenu;
import android.view.ext.SatelliteMenu.SateliteClickedListener;
import android.view.ext.SatelliteMenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.kinglin.dao.DBHelper;
import com.kinglin.dao.FileService;
import com.kinglin.dao.ModelDaoImp;
import com.kinglin.model.Note;

@SuppressLint({ "InflateParams", "ShowToast", "ClickableViewAccessibility" })
public class AddNoteActivity extends Activity implements OnTouchListener,OnGestureListener{

	SQLiteDatabase db;
	
	RelativeLayout rlayoutAddNote;
	EditText etAddTitle,etAddContent;
	//Spinner spinnerAddPermission;
	ImageButton ibtnAddWeather;
	ImageView ivAddNewImage;
	LinearLayout llayoutAddNewImage,llayoutAddNewOther;
	PopupWindow popViewAdd,popSelectPic;
	TextView /*tvAddTips, */tvFingerBottom;
	ScrollView scroll;
	ImageView ivFingerBottom;
	
	GestureDetector mAddGestureDetector;		//手势操作
	private int minDistance = 250;			//上滑保存时手势滑动的最短距离
	
	//int addPermission = 0;		//记录选择的permission，默认为0，即公开
	int addWeather = 1;		//记录选择的天气，默认为1，即晴
	String photoFilePath = null;
	int videoNum = 0;
	String picturesPath = "", videoPath = "";
	int playOrNot = 0;		//判断是否播放视频
	
	List<MyImg> imgs;		//存放动态添加的ImageView的List
	
	boolean isExit = false;
	@SuppressLint("HandlerLeak")
	Handler mHandler = new Handler() {  
		  
        @Override  
        public void handleMessage(Message msg) {  
            super.handleMessage(msg);  
            isExit = false;  
        }
    };
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_add_note);
		
		//对控件初始化
		initContext();
		db = dbInit();
		
		ivFingerBottom.startAnimation(AnimationUtils.loadAnimation(this, R.anim.bottom_to_center));
		tvFingerBottom.startAnimation(AnimationUtils.loadAnimation(this, R.anim.bottom_to_center));
		
		//spinnerAddPermission.setOnItemSelectedListener(new PermissionSelectedListener());
		ibtnAddWeather.setOnClickListener(new WeatherBtnClickListener());
		ivAddNewImage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				WindowManager.LayoutParams lp = getWindow().getAttributes();  
		        lp.alpha = 0.7f;  
		        getWindow().setAttributes(lp);
				selectPicPopupWindow(0);		//0表示点击的是图片
			}
		});
		
		//设置触摸事件监听
		rlayoutAddNote.setOnTouchListener(this);
		rlayoutAddNote.setLongClickable(true);
		
	}

	//对控件和数据初始化
	@SuppressWarnings("deprecation")
	private void initContext() {
		
		mAddGestureDetector = new GestureDetector(this);
		
		rlayoutAddNote = (RelativeLayout) findViewById(R.id.rl_addNote);
		etAddTitle = (EditText) findViewById(R.id.et_addTitle);
		//spinnerAddPermission = (Spinner) findViewById(R.id.spinner_addPermission);
		ibtnAddWeather = (ImageButton) findViewById(R.id.ibtn_addWeather);
		
		etAddContent = (EditText) findViewById(R.id.et_addContent);
		//tvAddTips = (TextView) findViewById(R.id.tv_addTips);
		ivAddNewImage = (ImageView) findViewById(R.id.iv_addNewImage);
		llayoutAddNewImage = (LinearLayout) findViewById(R.id.ll_addNewImage);
		llayoutAddNewOther = (LinearLayout) findViewById(R.id.ll_addNewOther);
		scroll = (ScrollView) findViewById(R.id.scroll);
		ivFingerBottom = (ImageView) findViewById(R.id.iv_fingerBottom);
		tvFingerBottom = (TextView) findViewById(R.id.tv_fingerBottom);
		
		/*//设置spinner相关
		//建立数据源
		String[] statusItems = getResources().getStringArray(R.array.status_spinner);
		//建立Adapter并绑定数据源
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, statusItems);
		adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
		//绑定Adapter到控件
		spinnerAddPermission.setAdapter(adapter);*/
		
		//tvAddTips.setText("Tips: 可上滑保存哦！");
		
		initSatelliteMenu();
		
		imgs = new ArrayList<MyImg>();
	}
	
	//这个函数获取当前用户的数据库
	public SQLiteDatabase dbInit() {
		
		//先连接上default数据库，第一次使用会初始化各种实体对应的表
		DBHelper helper=new DBHelper(AddNoteActivity.this, "default.db", null, 1);
	    SQLiteDatabase defaultdb=helper.getWritableDatabase();
        
        //查询default数据库中Configuration表的loginUser,返回相应的db
	    ModelDaoImp mdi = new ModelDaoImp(defaultdb);
        String userId = mdi.getUserConfiguration().getLoginUser();
        if (userId.equals("default")) {
			return defaultdb;
		}else {
			DBHelper helper1=new DBHelper(AddNoteActivity.this, userId+".db", null, 1);
		    SQLiteDatabase userdb=helper1.getWritableDatabase();
		    return userdb;
		}
	}

	/*//选择permission的响应事件
	private class PermissionSelectedListener implements OnItemSelectedListener{
		
		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			
			addPermission = position;
		}
		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			
		}
		
	}*/
	
	//点击选择天气按钮的响应事件
	private class WeatherBtnClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			if(popViewAdd!=null && popViewAdd.isShowing())
			{
				popViewAdd.dismiss();
			}
			else {
				View view = LayoutInflater.from(AddNoteActivity.this).inflate(R.layout.popmenu_selectweather, null);
				popViewAdd = new PopupWindow(view, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				
				ImageView imgSun = (ImageView) view.findViewById(R.id.iv_sun);
				ImageView imgCloud = (ImageView) view.findViewById(R.id.iv_cloud);
				ImageView imgRain = (ImageView) view.findViewById(R.id.iv_rain);
				ImageView imgSnow = (ImageView) view.findViewById(R.id.iv_snow);
				
				imgSun.setOnClickListener(new WeatherClick());
				imgCloud.setOnClickListener(new WeatherClick());
				imgRain.setOnClickListener(new WeatherClick());
				imgSnow.setOnClickListener(new WeatherClick());
				
				popViewAdd.setAnimationStyle(R.style.popwin_top_anim_style);
				popViewAdd.setFocusable(false);
				popViewAdd.setOutsideTouchable(true);
				popViewAdd.showAsDropDown(v, 0, 0);
			}
		}
		
	}
	
	//选择某天气的响应事件
	private class WeatherClick implements OnClickListener{

		@Override
		public void onClick(View v) {
			
			switch (v.getId()) {
			case R.id.iv_sun:
				ibtnAddWeather.setImageResource(R.drawable.ic_sun);
				addWeather = 1;
				break;
			case R.id.iv_cloud:
				ibtnAddWeather.setImageResource(R.drawable.ic_cloud);
				addWeather = 2;
				break;
			case R.id.iv_rain:
				ibtnAddWeather.setImageResource(R.drawable.ic_rain);
				addWeather = 3;
				break;
			case R.id.iv_snow:
				ibtnAddWeather.setImageResource(R.drawable.ic_snow);
				addWeather = 4;
				break;
			default:
				break;
			}
			popViewAdd.dismiss();
			
		}
	}
	
	//点击popupwindow外部取消 
    @Override  
    public boolean dispatchTouchEvent(MotionEvent ev) {  
        if(popViewAdd == null || !popViewAdd.isShowing()) {
        	
        	mAddGestureDetector.onTouchEvent(ev);
            //scroll.onTouchEvent(ev);
            return super.dispatchTouchEvent(ev);  
        }  
        boolean isOut = isOutOfBounds(ev);  
        if(ev.getAction()==MotionEvent.ACTION_DOWN && isOut) {  
            popViewAdd.dismiss();  
            return true;  
        }  
        return false;  
    }  
  
    //判断触摸位置是否在popuwindow外部  
    private boolean isOutOfBounds(MotionEvent event) {  
        final int x=(int) event.getX();  
        final int y=(int) event.getY();  
        int slop = ViewConfiguration.get(AddNoteActivity.this).getScaledWindowTouchSlop();  
        View decorView = popViewAdd.getContentView();  
        return (x<-slop)||(y<-slop)  
        ||(x>(decorView.getWidth()+slop))  
        ||(y>(decorView.getHeight()+slop));  
    }  
    
	
	//初始化SatelliteMenu
	private void initSatelliteMenu(){
		//设置SatelliteMenu相关
		SatelliteMenu menu = (SatelliteMenu) findViewById(R.id.sat_menu_add);
		List<SatelliteMenuItem> items = new ArrayList<SatelliteMenuItem>();
		items.add(new SatelliteMenuItem(1, R.drawable.ic_video));
		items.add(new SatelliteMenuItem(2, R.drawable.ic_position));
		items.add(new SatelliteMenuItem(3, R.drawable.ic_recorder));
		items.add(new SatelliteMenuItem(4, R.drawable.ic_camera));
		menu.addItems(items);
		
		menu.setSatelliteDistance(250);
		menu.setMainImage(R.drawable.ic_plus);
		
		menu.setOnItemClickedListener(new SateliteClickedListener() {
			
			@Override
			public void eventOccured(int id) {
				
				switch (id) {
				case 1:
					if (videoNum != 0) {		//最多添加一个视频
						Toast.makeText(getApplicationContext(), "You can only add 1 video", 100).show();
					}
					else {
						WindowManager.LayoutParams lp = getWindow().getAttributes();  
				        lp.alpha = 0.7f;  
				        getWindow().setAttributes(lp);
						selectPicPopupWindow(1);		//1表示点击的是视频
					}
					break;
				case 2:
					//etContent.setText("你点击的item的是：位置");
					break;
				case 3:
					//etContent.setText("你点击的item的是：语音");
					break;
				case 4:
					if (imgs.size() == 4) {			//设置最多添加4张图片
						Toast.makeText(getApplicationContext(), "You can not add more than 4 pictures", 100).show();
					}
					else {
						//下面3行代码是使屏幕变暗
						WindowManager.LayoutParams lp = getWindow().getAttributes();  
				        lp.alpha = 0.7f;  
				        getWindow().setAttributes(lp);
				        
						selectPicPopupWindow(0);		//0表示点击的是图片
					}
					break;
				default:
					break;
				}	
			}
		});
		
	}
	
	//弹出选择照片或拍照（选择视频或录视频）的菜单，id为0时表示图片操作，id为1时表示视频操作
	private void selectPicPopupWindow(int id){
		View view = LayoutInflater.from(AddNoteActivity.this).inflate(R.layout.popmenu_selectpic, null);
		popSelectPic = new PopupWindow(view, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		
		Button btnTakeAPhoto = (Button) view.findViewById(R.id.btn_takeAPhoto);
		Button btnSelectPicFromAlbum = (Button) view.findViewById(R.id.btn_selectPicFromAlbum);
		Button btnSelectPicCancel = (Button) view.findViewById(R.id.btn_selectPicCancel);
		
		if (id == 0) {		//图片操作
			btnTakeAPhoto.setOnClickListener(new TakeAPhotoClickListener());
			btnSelectPicFromAlbum.setOnClickListener(new SelectFromAlbumClickListener());
		}
		else if (id == 1) {			//视频操作
			btnTakeAPhoto.setText("make a video");
			btnTakeAPhoto.setOnClickListener(new MakeAVideoClickListener());
			btnSelectPicFromAlbum.setText("select form video gallery");
			btnSelectPicFromAlbum.setOnClickListener(new SelectFromVideoGalleryClickListener());
		}
		btnSelectPicCancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				popSelectPic.dismiss();
			}
		});
		
		popSelectPic.setFocusable(true);
		popSelectPic.setAnimationStyle(R.style.popwin_bottom_anim_style);		//设置弹入弹出动画
		ColorDrawable cDrawable = new ColorDrawable(0x00000000);
		popSelectPic.setBackgroundDrawable(cDrawable);			//设置背景透明
		
		popSelectPic.showAtLocation(rlayoutAddNote, Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);	//设置显示位置
		
		popSelectPic.setOnDismissListener(new OnDismissListener() {		//设置popupWindow消失的事件监听
			@Override
			public void onDismiss() {
				//屏幕恢复原来亮度
				WindowManager.LayoutParams lp = getWindow().getAttributes();  
		        lp.alpha = 1f;  
		        getWindow().setAttributes(lp); 
			}
		});
		
	}
	
	//从相册中选择图片
	private class SelectFromAlbumClickListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			
			Intent intent = new Intent(getApplicationContext(), ShowImageGroupActivity.class);
			intent.putExtra("num", 4 - imgs.size());		//传数据显示还能添加几张图片
			startActivityForResult(intent, 1);
			popSelectPic.dismiss();
		}
	}
    
	//拍摄照片
    private class TakeAPhotoClickListener implements View.OnClickListener{

		@Override
		public void onClick(View v) {
			Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");

			FileService service=new FileService(getApplicationContext());
   		 	photoFilePath = service.getPhotoFilePath();
			intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(photoFilePath)));	//拍好的图片放在photoFilePath路径
			
			startActivityForResult(intent, 2); 
			popSelectPic.dismiss();
		}
    }
    
    //从视频库中选择视频
    private final class  SelectFromVideoGalleryClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent();
			intent.setType("video/*"); 
			intent.setAction(Intent.ACTION_GET_CONTENT);   
			startActivityForResult(intent, 3);
			popSelectPic.dismiss();
		}
	}
    
    //录制视频
    private final class  MakeAVideoClickListener implements View.OnClickListener{

		@Override
		public void onClick(View v) {
			Intent intent=new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
			intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);		//设置录制视频质量
			intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 10);	//设置录制视频最长时间
			startActivityForResult(intent, 4);
			popSelectPic.dismiss();
		}
    	
    }
    
    //接收子窗口关闭时传回来的数据
	@SuppressLint({ "ShowToast", "NewApi" }) 
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);  
		
        if (requestCode == 1) {//从相册中选择
        	
        	if (data != null) {
            	List<String> pathList = null;
	            Bundle bundle = data.getExtras(); 
	            
	            if (bundle != null) {
	            	pathList = bundle.getStringArrayList("pathList"); // 得到子窗口的回传数据，是所选择图片的list
	            	Toast.makeText(getApplicationContext(), "picked " + pathList.size() + " picture", 200).show();
	            }
	            
	            llayoutAddNewImage.removeAllViews();
	            int num = imgs.size() + pathList.size() - 1;		//所有图片张数-1
	            for (int i = 0; i < imgs.size(); i++) {			//重新添加imgs中的所有图片，因为图片大小要改变
					
					addNewImage(imgs.get(i).getImgPath(), num, 1);		//1表示该图片不需要再添加到imgs中
				}
	            for (int i = 0; i < pathList.size(); i++) {			//显示刚刚选中的图片
        		    
	     		    addNewImage(pathList.get(i),num, 0);		//0表示该图片需要增加到imgs中
				}
        	}
        }
        if (requestCode == 2 && resultCode == RESULT_OK) {//拍照图片获取 
        	
        	llayoutAddNewImage.removeAllViews();
            for (int i = 0; i < imgs.size(); i++) {
				
				addNewImage(imgs.get(i).getImgPath(), imgs.size(), 1);
			}
		    
    		addNewImage(photoFilePath, imgs.size(), 0);
    		
        }
        if (requestCode==3&&resultCode == RESULT_OK) {//从本地获取视频
        	
        	Uri uri = data.getData();
        	String[] filePathColumn = { MediaStore.Video.Media.DATA };
            Cursor cursor = getContentResolver().query(uri,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            videoPath = cursor.getString(columnIndex);
            
            addNewVideo(videoPath);		//显示视频
            videoNum ++;
        }
        if (requestCode == 4 && resultCode == RESULT_OK) {//录制视频
        	
        	Uri uri=data.getData();
        	Cursor cursor=this.getContentResolver().query(uri, null, null, null, null);
        	
        	if (cursor!=null&&cursor.moveToNext()) {
        		
				videoPath = cursor.getString(cursor.getColumnIndex(VideoColumns.DATA));
				cursor.close();
				
				addNewVideo(videoPath);		//显示视频
	            videoNum ++;
        	}
        }
        
    }
	
	//动态添加一张新图片,第一个参数是图片路径，第二个参数是当前所有要显示的图片张数-1，第三个参数表示是否要将该img添加到imgs中
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	private void addNewImage(String path, int num, int addToListOrNot){
		//获得屏幕宽度
		WindowManager wm = this.getWindowManager();
	    int width = wm.getDefaultDisplay().getWidth();
	    //淡入动画
		AlphaAnimation animation = new AlphaAnimation(0.0f,1.0f);
		animation.setDuration(500);
		animation.setStartOffset(0);
		//获得图片缩略图并圆角处理
		FileService fileService = new FileService(getApplicationContext());
		Bitmap bitmap = fileService.getImageThumbnail(path, (width-80-num*15)/(num+1), (width-80-num*15)/(num+1));
	    bitmap = fileService.getRoundedCornerBitmap(bitmap, 0);
	    
		final FrameLayout fLayoutImg = new FrameLayout(AddNoteActivity.this);		
		
		final MyImg img = new MyImg(AddNoteActivity.this);
		//设置img的属性
		img.setImageBitmap(bitmap);
		img.setImgPath(path);
		img.setScaleType(ScaleType.CENTER_CROP);
		
		fLayoutImg.addView(img);
		
		if (addToListOrNot == 0) {			//该参数为0时才需要将img添加到imgs中
			imgs.add(img);
		}

		//表示删除的imageView
		ImageView iv_delete = new ImageView(AddNoteActivity.this);
		iv_delete.setImageResource(R.drawable.ic_delete);
		iv_delete.setOnClickListener(new OnClickListener() {	//点击删除的事件监听
			
			@Override
			public void onClick(View v) {
				//淡出动画
				AlphaAnimation animation_remove = new AlphaAnimation(1.0f,0.0f);	
				animation_remove.setDuration(500);
				animation_remove.setStartOffset(0);
				fLayoutImg.startAnimation(animation_remove);
				
				//通过该img的imgPath属性在imgs中比较，找到时将该img从imgs中移除
				for (int i = 0; i < imgs.size(); i++) {
					if (imgs.get(i).getImgPath().equals(img.getImgPath())) {
						
						imgs.remove(i);
						break;
					}
				}
				
				//重新显示所有图片
				llayoutAddNewImage.removeAllViews();
				for (int j = 0; j < imgs.size(); j++) {
					addNewImage(imgs.get(j).getImgPath(), imgs.size()-1, 1);
				}
				
				//如果图片都删完了就动态添加一张可以点击添加图片的图片按钮
				if (imgs.size() == 0) {
					//tvAddTips.setVisibility(View.VISIBLE);
					ImageView imageView = new ImageView(getApplicationContext());
					imageView.setImageResource(R.drawable.plus);
					
					imageView.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							WindowManager.LayoutParams lp = getWindow().getAttributes();  
					        lp.alpha = 0.7f;  
					        getWindow().setAttributes(lp);
							selectPicPopupWindow(0);		//0表示点击的是图片
						}
					});
					
					LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(
							LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
					lParams.setMargins(0, 15, 0, 0);
					llayoutAddNewImage.addView(imageView, lParams);
				}
			}
		});
		FrameLayout.LayoutParams fParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
		fLayoutImg.addView(iv_delete, fParams);
		fLayoutImg.startAnimation(animation);
		
		//根据图片张数调整每张图片的显示大小
		LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams((width-80-num*15)/(num+1), (width-80-num*15)/(num+1));
		lParams.setMargins(0, 20, 15, 10);
		llayoutAddNewImage.addView(fLayoutImg,lParams);
		
		//tvAddTips.setVisibility(View.GONE);
	}
	
	//动态添加一个新视频
	@SuppressWarnings("deprecation")
	private void addNewVideo(String path){
		AlphaAnimation animation = new AlphaAnimation(0.0f,1.0f);
		animation.setDuration(500);
		animation.setStartOffset(0);
		
		final FrameLayout fLayoutOther = new FrameLayout(AddNoteActivity.this);
		
		VideoView videoView = new VideoView(AddNoteActivity.this);
		//获得视频第一帧图片，在未播放视频时作为背景图片
		FileService fileService = new FileService(getApplicationContext());
		videoView.setBackgroundDrawable(new BitmapDrawable(fileService.getVideoThumbnail(path,  MediaStore.Images.Thumbnails.MINI_KIND)));
		videoView.setVideoPath(path);
		videoView.requestFocus();
		//设置视频触摸事件监听
		videoView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				v.setBackgroundColor(Color.parseColor("#00000000"));
				if (playOrNot == 0) {		//若未播放，触摸之后播放
					((VideoView) v).start();
					playOrNot = 1;
				}
				else if (playOrNot == 1) {		//若正在播放，触摸之后暂停
					((VideoView) v).pause();
					playOrNot = 0;
				}
				
				return false;
			}
		});
		
		fLayoutOther.addView(videoView);
		fLayoutOther.startAnimation(animation);
		
		//删除操作
		ImageView iv_delete = new ImageView(AddNoteActivity.this);
		iv_delete.setImageResource(R.drawable.ic_delete);
		iv_delete.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				videoPath = null;
				videoNum --;
				llayoutAddNewOther.removeView(fLayoutOther);
				
				/*if (imgs.size() == 0 && videoNum == 0) {
					tvAddTips.setVisibility(View.VISIBLE);
				}*/
			}
		});
		FrameLayout.LayoutParams fParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
		fLayoutOther.addView(iv_delete, fParams);
		
		LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(250, 200);
		lParams.setMargins(0, 20, 15, 10);
		llayoutAddNewOther.addView(fLayoutOther, lParams);
		
		//tvAddTips.setVisibility(View.GONE);
	}
	
	//点击返回键的事件响应
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			exit();
		    return false;
		}
		else {

			return super.onKeyDown(keyCode, event);
		}
	}
	
	//设置2秒内点击两次返回键就跳转到显示记事界面
	public void exit(){
		if (!isExit) {  	//点击一次时提示再点一次就返回主界面
            isExit = true;  
            Toast.makeText(getApplicationContext(), "one more click to return the main page", Toast.LENGTH_SHORT).show();  
            mHandler.sendEmptyMessageDelayed(0, 2000);  
        } else {  
        	Intent intent = new Intent(getApplicationContext(), ShowNoteActivity.class);
			AddNoteActivity.this.finish();
			startActivity(intent);
			overridePendingTransition(R.anim.bottom_in, R.anim.top_out); 
        }  
	}
	
	//手势操作事件监听
	@SuppressLint("SimpleDateFormat")
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		//上滑事件响应
		if(e1.getY() - e2.getY() > minDistance)
		{
			String addContent = etAddContent.getText().toString();
			//文字不能为空，若文字输入为空，则弹框提示是否放弃该记事
			if (addContent.length() == 0) {
				
				View view = LayoutInflater.from(AddNoteActivity.this).inflate(R.layout.popwin_alert, null);
				final PopupWindow popwinAlert = new PopupWindow(view, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				
				TextView tvDelete = (TextView) view.findViewById(R.id.tv_alert);
				final Button btnAlertYes = (Button) view.findViewById(R.id.btn_alertYes);
				final Button btnAlertNo = (Button) view.findViewById(R.id.btn_alertNo);
				
				tvDelete.setText("Text should not be empty. Do you want to give up this note?");
				
				btnAlertYes.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(getApplicationContext(), ShowNoteActivity.class);
						startActivity(intent);
						AddNoteActivity.this.finish();
						overridePendingTransition(R.anim.bottom_in, R.anim.top_out);
						popwinAlert.dismiss();
					}
				});
				btnAlertNo.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						popwinAlert.dismiss();
					}
				});
				//按钮触摸事件监听，按下时按钮变下，弹起时恢复原样
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
				btnAlertYes.setOnTouchListener(mOnTouchListener);
				btnAlertNo.setOnTouchListener(mOnTouchListener);
				
				popwinAlert.setFocusable(true);
				popwinAlert.setBackgroundDrawable(getResources().getDrawable(R.drawable.popwin_bg));
				popwinAlert.setAnimationStyle(R.style.popwin_anim_style);
				popwinAlert.showAtLocation(rlayoutAddNote, Gravity.CENTER, 0, 0);
				
				//弹出框时父窗口颜色变暗
				WindowManager.LayoutParams lp = getWindow().getAttributes();  
				lp.alpha = 0.7f;  
				getWindow().setAttributes(lp);
				//窗口消失时父窗口颜色恢复正常
				popwinAlert.setOnDismissListener(new OnDismissListener() {
				    @Override  
				    public void onDismiss() {  
				        WindowManager.LayoutParams lp = getWindow().getAttributes();  
				        lp.alpha = 1f;  
				        getWindow().setAttributes(lp); 
				    }  
				});  
				
			}
			else if(addContent.length() > 100){			//文字不能超过100，超过时提示
				Toast.makeText(getApplicationContext(), "Text should not be more than 100", 1000).show();
			}
			else{		//正常情况下
				String addTitle = etAddTitle.getText().toString();
				
				if(addTitle.length() == 0)		//若标题为空，则标题为文字前5个字
				{
					if (addContent.length() < 5) {
						addTitle = addContent;
					}
					else {
						addTitle = addContent.substring(0, 5);
					}
				}
				
				//图片处理
				FileService service=new FileService(getApplicationContext());
			    String fileName = "/easyTravel/savefile/images/";
			    service.createSDCardDir(fileName);
			    
			    String cfsFileName = "/easyTravel/savefile/tempImages/";
			    service.createSDCardDir(cfsFileName);
			    
			    for (int i = 0; i < imgs.size(); i++) {
					try {
						imgs.get(i).imgPath = service.saveRealImg(fileName, imgs.get(i).imgPath);
						
						picturesPath = picturesPath + imgs.get(i).imgPath + ";";	//图片路径，多张图片路径用分号隔开
						
						//将压缩图片保存在tempImages文件夹下
						File file = new File(imgs.get(i).imgPath);
						if (file.exists()) {
							Bitmap bm = BitmapFactory.decodeFile(imgs.get(i).imgPath);
							Bitmap cfsBitmap = service.confessBitmap(bm);
							service.saveMyImg(100, cfsBitmap, cfsFileName, imgs.get(i).imgPath);
						}
						
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
				//视频处理
			    String videoFileName = "/easyTravel/savefile/videos/";
			    service.createSDCardDir(videoFileName);
			    
			    if (videoPath.equals("") == false) {
			    	try {
						videoPath = service.saveMyVideo(videoFileName, videoPath);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
				//下面格式写成HH时使用24小时制，写成hh时使用12小时制
				SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());       
				String time = sDateFormat.format(new java.util.Date());
				//获取经纬度相关
				LocationManager loctionManager;
				String contextService=Context.LOCATION_SERVICE;
				//通过系统服务，取得LocationManager对象
				loctionManager=(LocationManager) getSystemService(contextService);
				      
				String provider=getLocationProvider(loctionManager);
				Location location = loctionManager.getLastKnownLocation(provider);
				
				
				Note note = new Note();
				if (location==null) {
					Toast.makeText(getApplicationContext(), "位置获取失败，请确认GPS可用", 1000).show();
					note.setLocationx(0);
					note.setLocationy(0);
				}else{
					note.setLocationx(location.getLatitude());
					note.setLocationy(location.getLongitude());
					
				}
				note.setNoteId(System.currentTimeMillis());
				note.setTitle(addTitle);
				//note.setPermission(addPermission);
				note.setWeather(addWeather);
				note.setText(addContent);
				note.setTime(time);
				note.setPictures(picturesPath);
				note.setVideo(videoPath);
				note.setOperation(1);
				
				ModelDaoImp mdi = new ModelDaoImp(db);
				mdi.addNote(note);
				
				Toast.makeText(getApplicationContext(), "add success", 1000).show();
				
				Intent intent = new Intent(getApplicationContext(), ShowNoteActivity.class);
				AddNoteActivity.this.finish();
				startActivity(intent);
				overridePendingTransition(R.anim.bottom_in, R.anim.top_out);
			}
			
		}
		
		return false;
	}
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

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return true;
	}
	
	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		
	}
	
	//继承ImageView类，加了图片路径属性
	class MyImg extends ImageView{
		
		String imgPath;
		
		public String getImgPath() {
			return imgPath;
		}

		public void setImgPath(String imgPath) {
			this.imgPath = imgPath;
		}

		public MyImg(Context context) {
			super(context);
		}
		
	}

}
