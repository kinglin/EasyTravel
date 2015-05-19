package com.kinglin.easytravel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Video.VideoColumns;
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
import android.widget.Toast;
import android.widget.VideoView;

import com.kinglin.dao.DBHelper;
import com.kinglin.dao.FileService;
import com.kinglin.dao.ModelDaoImp;
import com.kinglin.model.Note;

@SuppressLint({ "InflateParams", "ShowToast" })
public class EditNoteActivity extends Activity {

	SQLiteDatabase db;
	
	RelativeLayout rlayoutEditNote;
	EditText etEditTitle,etEditContent;
	//Spinner spinnerEditPermission;
	ImageButton ibtnEditWeather;
	ImageView ivEditNewImage;
	LinearLayout llayoutEditNewImage,llayoutEditNewOther;
	PopupWindow popViewEdit,popSelectPic;
	Button btnEditCancel,btnEditSave;
	
	Note editNote;
	
	//int editPermission = 0;		//记录选择的permission，默认为0，即公开
	int editWeather = 1;		//记录选择的天气，默认为1，即晴
	String photoFilePath = null;
	int pictureNum = 0;			//记录图片张数，必须要的，不能只通过imgs。size()来判断张数，因为在删除时并没有在imgs中去除该图片，在点击保存按钮后才真正删除
	int videoNum = 0;
	String picturesPath = "", videoPath = "";
	int videoDeleteOrNot = 0;		//标记图片是否被删除
	int playOrNot = 0;			//标记视频是否播放
	
	List<MyImg> imgs;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_edit_note);
		
		//对控件初始化，需在下方实现
		initContext();
		
		db = dbInit();
		
		//获取要编辑的记事
		editNote = (Note)getIntent().getSerializableExtra("editNote");
		editWeather = editNote.getWeather();
		videoPath = editNote.getVideo();
		
		//将现有的记事内容显示出来
		showNote(editNote);
		
		//spinnerEditPermission.setOnItemSelectedListener(new PermissionSelectedListener());
		ibtnEditWeather.setOnClickListener(new WeatherBtnClickListener());
		ivEditNewImage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				WindowManager.LayoutParams lp = getWindow().getAttributes();  
		        lp.alpha = 0.7f;  
		        getWindow().setAttributes(lp);
				selectPicPopupWindow(0);
			}
		});
		
		btnEditSave.setOnClickListener(new SaveClickListener());
		btnEditCancel.setOnClickListener(new CancelClickListener());
		
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
		btnEditCancel.setOnTouchListener(mOnTouchListener);
		btnEditSave.setOnTouchListener(mOnTouchListener);
	}


	//初始化所有空间和数据
	private void initContext() {
		
		rlayoutEditNote = (RelativeLayout) findViewById(R.id.rl_editNote);
		etEditTitle = (EditText) findViewById(R.id.et_editTitle);
		//spinnerEditPermission = (Spinner) findViewById(R.id.spinner_editPermission);
		ibtnEditWeather = (ImageButton) findViewById(R.id.ibtn_editWeather);
		etEditContent = (EditText) findViewById(R.id.et_editContent);
		ivEditNewImage = (ImageView) findViewById(R.id.iv_editNewImage);
		llayoutEditNewImage = (LinearLayout) findViewById(R.id.ll_editNewImage);
		llayoutEditNewOther = (LinearLayout) findViewById(R.id.ll_editNewOther);
		btnEditCancel = (Button) findViewById(R.id.btn_editCancel);
		btnEditSave = (Button) findViewById(R.id.btn_editSave);
		
		/*//设置spinner相关
		//建立数据源
		String[] statusItems = getResources().getStringArray(R.array.status_spinner);
		//建立Adapter并绑定数据源
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, statusItems);
		adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
		//绑定Adapter到控件
		spinnerEditPermission.setAdapter(adapter);*/
		
		initSatelliteMenu();
		
		imgs = new ArrayList<MyImg>();
	}
	
	//这个函数获取当前用户的数据库
	public SQLiteDatabase dbInit() {
		
		//先连接上default数据库，第一次使用会初始化各种实体对应的表
		DBHelper helper=new DBHelper(EditNoteActivity.this, "default.db", null, 1);
	    SQLiteDatabase defaultdb=helper.getWritableDatabase();
        
        //查询default数据库中Configuration表的loginUser,返回相应的db
	    ModelDaoImp mdi = new ModelDaoImp(defaultdb);
        String userId = mdi.getUserConfiguration().getLoginUser();
       if (userId.equals("default")) {
			return defaultdb;
		}else {
			DBHelper helper1=new DBHelper(EditNoteActivity.this, userId+".db", null, 1);
		    SQLiteDatabase userdb=helper1.getWritableDatabase();
		    return userdb;
		}
	}
	
	//显示现有记事的详情信息
	public void showNote(Note note){
		
		etEditTitle.setText(note.getTitle());
		//spinnerEditPermission.setSelection(note.getPermission());
		ibtnEditWeather.setImageResource(showWeather(note.getWeather()));
		etEditContent.setText(note.getText());
		
		String[] imagePath = new String[4];
		for (int j = 0; j < 4; j++) {
			imagePath[j] = "";
		}
		
		//将数据库存放的图片路径以分号拆开成一个数组，存放多张图片路径
		imagePath = editNote.getPictures().split(";");
		//将每张图片显示出来
		for (int j = 0; j < imagePath.length; j++) {
			
			if (imagePath[j].equals("") == false) {
				
				File file = new File(imagePath[j]);
				if (file.exists()) {
					
					ivEditNewImage.setVisibility(View.GONE);
					
					addNewImage(imagePath[j], imagePath.length -1, 0, 0);
					pictureNum ++;
					
				}
			}
		}
		//显示视频
		if (videoPath.equals("") == false) {
			addNewVideo(videoPath);
			videoNum ++;
		}
		
	}
	
	/*//选择permission的响应事件
	private class PermissionSelectedListener implements OnItemSelectedListener{
		
		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			
			editPermission = position;
		}
		@Override
		public void onNothingSelected(AdapterView<?> parent) {
		}
		
	}*/
	
	//点击选择天气按钮的响应事件
	private class WeatherBtnClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			if(popViewEdit!=null && popViewEdit.isShowing())
			{
				popViewEdit.dismiss();
			}
			else {
				View view = LayoutInflater.from(EditNoteActivity.this).inflate(R.layout.popmenu_selectweather, null);
				popViewEdit = new PopupWindow(view, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				
				ImageView imgSun = (ImageView) view.findViewById(R.id.iv_sun);
				ImageView imgCloud = (ImageView) view.findViewById(R.id.iv_cloud);
				ImageView imgRain = (ImageView) view.findViewById(R.id.iv_rain);
				ImageView imgSnow = (ImageView) view.findViewById(R.id.iv_snow);
				
				imgSun.setOnClickListener(new WeatherClick());
				imgCloud.setOnClickListener(new WeatherClick());
				imgRain.setOnClickListener(new WeatherClick());
				imgSnow.setOnClickListener(new WeatherClick());
				
				popViewEdit.setAnimationStyle(R.style.popwin_top_anim_style);
				popViewEdit.setFocusable(false);
				popViewEdit.setOutsideTouchable(true);
				popViewEdit.showAsDropDown(v, 0, 0);
			}
		}
		
	}
	
	//选择某天气的响应事件
	private class WeatherClick implements OnClickListener{

		@Override
		public void onClick(View v) {
			
			switch (v.getId()) {
			case R.id.iv_sun:
				ibtnEditWeather.setImageResource(R.drawable.ic_sun);
				editWeather = 1;
				break;
			case R.id.iv_cloud:
				ibtnEditWeather.setImageResource(R.drawable.ic_cloud);
				editWeather = 2;
				break;
			case R.id.iv_rain:
				ibtnEditWeather.setImageResource(R.drawable.ic_rain);
				editWeather = 3;
				break;
			case R.id.iv_snow:
				ibtnEditWeather.setImageResource(R.drawable.ic_snow);
				editWeather = 4;
				break;
			default:
				break;
			}
			popViewEdit.dismiss();
			
		}
	}
	
	//点击popupwindow外部取消 
    @Override  
    public boolean dispatchTouchEvent(MotionEvent ev) {  
        if(popViewEdit == null || !popViewEdit.isShowing()) {
        	
            return super.dispatchTouchEvent(ev);  
        }  
        boolean isOut = isOutOfBounds(ev);  
        if(ev.getAction()==MotionEvent.ACTION_DOWN && isOut) {  
            popViewEdit.dismiss();  
            return true;  
        }  
        return false;  
    }  
  
    //判断触摸位置是否在popuwindow外部  
    private boolean isOutOfBounds(MotionEvent event) {  
        final int x=(int) event.getX();  
        final int y=(int) event.getY();  
        int slop = ViewConfiguration.get(EditNoteActivity.this).getScaledWindowTouchSlop();  
        View decorView = popViewEdit.getContentView();  
        return (x<-slop)||(y<-slop)  
        ||(x>(decorView.getWidth()+slop))  
        ||(y>(decorView.getHeight()+slop));  
    }  
    
	
	//初始化SatelliteMenu
	private void initSatelliteMenu(){
		//设置SatelliteMenu相关
		SatelliteMenu menu = (SatelliteMenu) findViewById(R.id.sat_menu_edit);
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
					if (videoNum != 0) {
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
					if (pictureNum == 4) {		//最多选择4张图片
						Toast.makeText(getApplicationContext(), "You can not add more than 4 pictures", 100);
					}
					else {
						WindowManager.LayoutParams lp = getWindow().getAttributes();  
				        lp.alpha = 0.7f;  
				        getWindow().setAttributes(lp);
						selectPicPopupWindow(0);
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
		View view = LayoutInflater.from(EditNoteActivity.this).inflate(R.layout.popmenu_selectpic, null);
		popSelectPic = new PopupWindow(view, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		
		Button btnTakeAPhoto = (Button) view.findViewById(R.id.btn_takeAPhoto);
		Button btnSelectPicFromAlbum = (Button) view.findViewById(R.id.btn_selectPicFromAlbum);
		Button btnSelectPicCancel = (Button) view.findViewById(R.id.btn_selectPicCancel);
		
		if (id == 0) {
			btnTakeAPhoto.setOnClickListener(new TakeAPhotoClickListener());
			btnSelectPicFromAlbum.setOnClickListener(new SelectFromAlbumClickListener());
		}
		else if (id == 1) {
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
		popSelectPic.setAnimationStyle(R.style.popwin_bottom_anim_style);
		ColorDrawable cDrawable = new ColorDrawable(0x00000000);
		popSelectPic.setBackgroundDrawable(cDrawable);
		
		popSelectPic.showAtLocation(rlayoutEditNote, Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);
		
		popSelectPic.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss() {
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
			intent.putExtra("num", 4 - pictureNum);
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
			intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(photoFilePath)));
			
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
			intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
			intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 10);
			startActivityForResult(intent, 4);
			popSelectPic.dismiss();
		}
    	
    }
    
    //接收子窗口关闭时传回来的数据
	@SuppressLint({ "ShowToast", "NewApi" }) 
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);  
		
        if (requestCode == 1) {//从相册中选择图片 
        	
        	if (data != null) {     

            	List<String> pathList = null;
	            Bundle bundle = data.getExtras(); 
	            
	            if (bundle != null) {
	            	pathList = bundle.getStringArrayList("pathList"); // 得到子窗口的回传数据
	            	Toast.makeText(getApplicationContext(), "选中 " + pathList.size() + " 张图片", Toast.LENGTH_LONG).show();
	            }
	            
	            llayoutEditNewImage.removeAllViews();
	            int num = pictureNum + pathList.size() - 1;		//记录图片张数-1
	            for (int i = 0; i < imgs.size(); i++) {			//显示之前已有图片
					if (imgs.get(i).getDeleteOrNot() == 0) {		//当图片未被删除时
						if (imgs.get(i).getSaveOrNot() == 0) {		//当该图片不再需要被保存时（已保存过）
							addNewImage(imgs.get(i).getImgPath(), num, 1, 0);
						}
						else if (imgs.get(i).getSaveOrNot() == 1) {		//当该图片需要被保存时
							addNewImage(imgs.get(i).getImgPath(), num, 1, 1);
						}
					}
				}
	            for (int i = 0; i < pathList.size(); i++) {		//显示新选中的图片
	            	
	            	addNewImage(pathList.get(i),num, 0, 1);
	     		    pictureNum ++;
				}
        	}
        }
        if (requestCode == 2 && resultCode == RESULT_OK) {//拍照图片获取 
        	
        	llayoutEditNewImage.removeAllViews();
        	for (int i = 0; i < imgs.size(); i++) {
        		if (imgs.get(i).getDeleteOrNot() == 0) {
        			if (imgs.get(i).getSaveOrNot() == 0) {
						addNewImage(imgs.get(i).getImgPath(), pictureNum, 1, 0);
					}
					else if (imgs.get(i).getSaveOrNot() == 1) {
						addNewImage(imgs.get(i).getImgPath(), pictureNum, 1, 1);
					}
				}
			}
        	
        	addNewImage(photoFilePath, pictureNum, 0, 1);
    		pictureNum ++;
        }
        if (requestCode == 3 && resultCode == RESULT_OK) {//从本地获取视频
        	
        	Uri uri = data.getData();
        	String[] filePathColumn = { MediaStore.Video.Media.DATA };
            Cursor cursor = getContentResolver().query(uri,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            videoPath = cursor.getString(columnIndex);
            
            addNewVideo(videoPath);
            videoNum ++;
        }
        if (requestCode == 4 && resultCode == RESULT_OK) {//录制视频
        	
        	Uri uri=data.getData();
        	Cursor cursor=this.getContentResolver().query(uri, null, null, null, null);
        	
        	if (cursor!=null&&cursor.moveToNext()) {
        		
				videoPath = cursor.getString(cursor.getColumnIndex(VideoColumns.DATA));
				cursor.close();
				
				addNewVideo(videoPath);
	            videoNum ++;
        	}
        }
        
    }
	
	//动态添加一张新图片，第一个参数时图片路径，第二个参数是当前所有要显示的图片张数-1,第三个参数时是否需要添加到imgs中，第4个参数是是否需要将该图片再保存到本地
	private void addNewImage(String path, int num, int addToListOrNot, int saveOrNot){
		
		WindowManager wm = this.getWindowManager();
	    int width = wm.getDefaultDisplay().getWidth();
	    
		AlphaAnimation animation = new AlphaAnimation(0.0f,1.0f);
		animation.setDuration(500);
		animation.setStartOffset(0);
		
		FileService fileService = new FileService(getApplicationContext());
		Bitmap bitmap = fileService.getImageThumbnail(path, (width-80-num*15)/(num+1), (width-80-num*15)/(num+1));
	    bitmap = fileService.getRoundedCornerBitmap(bitmap, 0);
		
		final FrameLayout fLayoutImg = new FrameLayout(EditNoteActivity.this);
		
		final MyImg img = new MyImg(EditNoteActivity.this);
		
		img.setImageBitmap(bitmap);
		img.setImgPath(path);
		img.setDeleteOrNot(0);
		img.setScaleType(ScaleType.CENTER_CROP);
		
		if (saveOrNot == 0) {
			img.setSaveOrNot(0);
		}
		else if (saveOrNot == 1) {
			img.setSaveOrNot(1);
		}
		
		fLayoutImg.addView(img);
		
		//如果之前未添加到list中，则添加
		if (addToListOrNot == 0) {
			imgs.add(img);
		}
		
		//删除操作
		ImageView iv_delete = new ImageView(EditNoteActivity.this);
		iv_delete.setImageResource(R.drawable.ic_delete);

		FrameLayout.LayoutParams fParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
		fLayoutImg.addView(iv_delete, fParams);
		fLayoutImg.startAnimation(animation);
		iv_delete.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				AlphaAnimation animation_remove = new AlphaAnimation(1.0f,0.0f);
				animation_remove.setDuration(500);
				animation_remove.setStartOffset(0);
				fLayoutImg.startAnimation(animation_remove);

				pictureNum --;		//实际图片数减1
				
				llayoutEditNewImage.removeAllViews();
				for (int i = 0; i < imgs.size(); i++) {
					if (imgs.get(i).getImgPath().equals(img.getImgPath())) {
						imgs.get(i).setDeleteOrNot(1);	//表示该图片被删除
					}
					else {
						if (imgs.get(i).getDeleteOrNot() == 0) {	//将没被删除的图片显示出来
							if (imgs.get(i).getSaveOrNot() == 0) {
								addNewImage(imgs.get(i).getImgPath(), pictureNum-1, 1, 0);
							}
							else if (imgs.get(i).getSaveOrNot() == 1) {
								addNewImage(imgs.get(i).getImgPath(), pictureNum-1, 1, 1);
							}
						}
					}
				}
				
				//如果图片都删完了就动态添加一张可以点击添加图片的图片按钮
				if (pictureNum == 0) {		
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
					llayoutEditNewImage.addView(imageView, lParams);
				}
				
			}
		});
		
		//根据图片张数调整每张图片的显示大小
		LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams((width-80-num*15)/(num+1), (width-80-num*15)/(num+1));
		lParams.setMargins(0, 30, 15, 0);
		llayoutEditNewImage.addView(fLayoutImg,lParams);
		
	}

	//动态添加一个新视频
	@SuppressWarnings("deprecation")
	private void addNewVideo(String path){
		AlphaAnimation animation = new AlphaAnimation(0.0f,1.0f);
		animation.setDuration(500);
		animation.setStartOffset(0);
		
		final FrameLayout fLayoutOther = new FrameLayout(EditNoteActivity.this);
		
		VideoView videoView = new VideoView(EditNoteActivity.this);
		FileService fileService = new FileService(getApplicationContext());
		videoView.setBackgroundDrawable(new BitmapDrawable(fileService.getVideoThumbnail(path,  MediaStore.Images.Thumbnails.MINI_KIND)));
		videoView.setVideoPath(path);
		videoView.requestFocus();
		
		videoView.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				v.setBackgroundColor(Color.parseColor("#00000000"));
				if (playOrNot == 0) {
					
					((VideoView) v).start();
					playOrNot = 1;
				}
				else if (playOrNot == 1) {
					((VideoView) v).pause();
					playOrNot = 0;
				}
				
				return false;
			}
		});
		
		fLayoutOther.addView(videoView);
		fLayoutOther.startAnimation(animation);
		
		//删除操作
		ImageView iv_delete = new ImageView(EditNoteActivity.this);
		iv_delete.setImageResource(R.drawable.ic_delete);
		iv_delete.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				videoDeleteOrNot = 1;
				videoNum --;
				
				llayoutEditNewOther.removeView(fLayoutOther);
				
			}
		});
		FrameLayout.LayoutParams fParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
		fLayoutOther.addView(iv_delete, fParams);
		
		LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(250, 200);
		lParams.setMargins(0, 20, 15, 10);
		llayoutEditNewOther.addView(fLayoutOther, lParams);
		
	}
	
	//点击保存按钮的响应事件
	private class SaveClickListener implements OnClickListener{
		@Override
		public void onClick(View v) {
			
			//下面是保存的按钮响应,现将页面上信息获取到editnote中
			String editContent = etEditContent.getText().toString();
			if (editContent.length() == 0) {
				Toast.makeText(getApplicationContext(), "Text should not be empty", 1000).show();
			}
			else if (editContent.length() > 100) {
				Toast.makeText(getApplicationContext(), "Text should not be more than 100", 1000).show();
			}
			else {
				String editTitle = etEditTitle.getText().toString();
				
				if (editTitle.length() == 0) {
					if (editContent.length() < 5) {
						editTitle = editContent;
					}
					else {
						editTitle = editContent.substring(0, 5);
					}
				}
				
				//图片处理
				//将删除的图片从imgs中移除,并在本地文件夹里删除
				for (int i = 0; i < imgs.size(); i++) {
					
					if (imgs.get(i).getDeleteOrNot() == 1) {
						//获得待删除的图片的路径及其压缩图片的路径
						File file = new File(imgs.get(i).getImgPath());
						FileService fileService = new FileService(getApplicationContext());
						String cfsImagePath = fileService.getCfsImagePath("/easyTravel/savefile/tempImages/", imgs.get(i).getImgPath());
						File cfsFile = new File(cfsImagePath);
						//删除原图片和压缩图片
						if (file.exists() && cfsFile.exists()) {
							file.delete();
							cfsFile.delete();
						}
						imgs.remove(i);
						i --;		//当删除一个img时，i不自加，否则就漏掉了当前的一个
					}
				}
				
				
				FileService service=new FileService(getApplicationContext());
			    String fileName = "/easyTravel/savefile/images/";
			    service.createSDCardDir(fileName);
			    
			    String cfsFileName = "/easyTravel/savefile/tempImages/";
			    service.createSDCardDir(cfsFileName);
			    
				for (int i = 0; i < imgs.size(); i++) {
				    
				    try {
				    	//将原图片保存在images文件夹下，并且将图片路径存入数据库
				    	if (imgs.get(i).getSaveOrNot() == 1) {		//只有需要保存的才会保存
				    		//只有之前没有保存过的图片需要在本地文件夹中保存
					    	imgs.get(i).imgPath = service.saveRealImg(fileName, imgs.get(i).imgPath);
						}
						picturesPath = picturesPath + imgs.get(i).imgPath +";";
						
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
			    
			    //判断之前视频是否执行过删除操作
			    if (videoDeleteOrNot == 1) {

					File file = new File(videoPath);
					if (file.exists()) {
						file.delete();
					}
					videoPath = "";
				}
			    //判断现在的视频路径是否为空，若是，则直接赋值，否则要将现在的视频存在特定路径中
				if (videoPath.equals("")) {	
					editNote.setVideo(videoPath);
				}
				else {
					//判断现在的视频路径与数据库中是否相同，若相同，则不保存
					if (videoPath.equals(editNote.getVideo()) == false) {
						try {
							videoPath = service.saveMyVideo(videoFileName, videoPath);
							editNote.setVideo(videoPath);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
				
				editNote.setTitle(editTitle);
				//editNote.setPermission(editPermission);
				editNote.setWeather(editWeather);
				editNote.setText(editContent);
				editNote.setPictures(picturesPath);
				
				
				ModelDaoImp mdi = new ModelDaoImp(db);
				mdi.updateNote(editNote);
				Toast.makeText(getApplicationContext(), "update success", 1000).show();
				
				EditNoteActivity.this.finish();
				overridePendingTransition(0, R.anim.center_out);
				
			}
			
		}
	}
	
	//点击取消按钮的响应事件
	private class CancelClickListener implements OnClickListener{
		@Override
		public void onClick(View v) {
			
			EditNoteActivity.this.finish();
			overridePendingTransition(0, R.anim.center_out);
		}
	}
	
	//显示用户之前选择的天气
	private int showWeather(int id){
		switch (id) {
		case 1:
			return R.drawable.ic_sun;
		case 2:
			return R.drawable.ic_cloud;
		case 3:
			return R.drawable.ic_rain;
		case 4:
			return R.drawable.ic_snow;
		default:
			return R.drawable.ic_sun;
		}
	}
	
	//继承ImageView类，加了图片路径属性
	class MyImg extends ImageView{

		String imgPath;				//记录该图片路径
		int deleteOrNot;			//记录该图片是否被删除，0表示未删除，1表示删除
		int saveOrNot;				//记录该图片是否需要在本地文件夹中保存，如果是之前有的，就不需要再保存一次，0表示不需要保存，1表示需要保存

		public int getSaveOrNot() {
			return saveOrNot;
		}

		public void setSaveOrNot(int saveOrNot) {
			this.saveOrNot = saveOrNot;
		}
		
		public int getDeleteOrNot() {
			return deleteOrNot;
		}

		public void setDeleteOrNot(int deleteOrNot) {
			this.deleteOrNot = deleteOrNot;
		}
		
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
	
	//点击返回按钮的事件响应
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) { //按下的如果是BACK，同时没有重复
			
			EditNoteActivity.this.finish();
			overridePendingTransition(0, R.anim.center_out);
		    return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	

}
