package com.kinglin.easytravel;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.kinglin.dao.DBHelper;
import com.kinglin.dao.FileService;
import com.kinglin.dao.ModelDaoImp;
import com.kinglin.model.Note;

public class NoteDetailActivity extends Activity implements OnTouchListener,OnGestureListener{

	SQLiteDatabase db;
	
	RelativeLayout rlnoteDetail;
	TextView tvDetailTitle,/*tvDetailPermission,*/tvDetailTime,tvDetailContent;
	ImageButton ibtnDetailLeft,ibtnDetailRight;
	ImageView ivDetailWeather;
	LinearLayout llDetailImage,llDetailOther;
	
	GestureDetector mDetailGestureDetector;
	private int minDistance = 100;
	
	List<Note> notes;
	Note noteDetail;
	int position;
	
	String[] imagePath = new String[4];
	
	int playOrNot = 0;
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_note_detail);
	
		initContext();
		
		db = dbInit();
		
		//这里获取从主页面传过来的note对象
		notes = (List<Note>) getIntent().getSerializableExtra("notes");
		noteDetail = (Note) getIntent().getSerializableExtra("noteDetail");
		position = (int)getIntent().getSerializableExtra("notePosition");
		
		if (noteDetail.getPictures().equals("")) {
			llDetailImage.setVisibility(View.GONE);
			
			if (noteDetail.getVideo().equals("")) {
				tvDetailContent.setLines(10);
			}
		}
		
		//显示记事详细信息
		showNote(noteDetail);
		
		ibtnDetailLeft.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				turnToPreviousNote();
			}
		});
		
		ibtnDetailRight.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				turnToNextNote();
			}
		});
		
		rlnoteDetail.setOnTouchListener(this);
		rlnoteDetail.setLongClickable(true);
	}
	
	//初始化控件及变量
	@SuppressWarnings("deprecation")
	private void initContext(){
		mDetailGestureDetector = new GestureDetector(this);
		
		rlnoteDetail = (RelativeLayout) findViewById(R.id.rl_notedetail);
		tvDetailTitle = (TextView) findViewById(R.id.tv_detailTitle);
		ibtnDetailLeft = (ImageButton) findViewById(R.id.ibtn_detailLeft);
		ibtnDetailRight = (ImageButton) findViewById(R.id.ibtn_detailRight);
		//tvDetailPermission = (TextView) findViewById(R.id.tv_detailPermission);
		ivDetailWeather = (ImageView) findViewById(R.id.iv_detailWeather);
		tvDetailTime = (TextView) findViewById(R.id.tv_detailTime);
		tvDetailContent = (TextView) findViewById(R.id.tv_detailContent);
		llDetailImage = (LinearLayout) findViewById(R.id.ll_detailImage);
		llDetailOther = (LinearLayout) findViewById(R.id.ll_detailOther);
		
		for (int j = 0; j < 4; j++) {
			imagePath[j] = "";
		}
		
	}
	
	//这个函数获取当前用户的数据库
	public SQLiteDatabase dbInit() {
		
		//先连接上default数据库，第一次使用会初始化各种实体对应的表
		DBHelper helper=new DBHelper(NoteDetailActivity.this, "default.db", null, 1);
	    SQLiteDatabase defaultdb=helper.getWritableDatabase();
        
        //查询default数据库中Configuration表的loginUser,返回相应的db
	    ModelDaoImp mdi = new ModelDaoImp(defaultdb);
        String userId = mdi.getUserConfiguration().getLoginUser();
        if (userId.equals("default")) {
			return defaultdb;
		}else {
			DBHelper helper1=new DBHelper(NoteDetailActivity.this, userId+".db", null, 1);
		    SQLiteDatabase userdb=helper1.getWritableDatabase();
		    return userdb;
		}
	}
	
	//显示记事详细信息
	public void showNote(Note note){
		tvDetailTitle.setText(note.getTitle());
		tvDetailTime.setText(note.getTime());
		tvDetailContent.setText(note.getText());
		ivDetailWeather.setImageResource(showWeather(note.getWeather()));
		
		/*if(note.getPermission() == 0){
			tvDetailPermission.setText("public");
		}
		else if(note.getPermission() == 1){
			tvDetailPermission.setText("private");
		}*/
		
		//图片处理
		Bitmap[] bitmaps = new Bitmap[]{
			null,null,null,null,
		};
		
		imagePath = note.getPictures().split(";");
		for (int j = 0; j < imagePath.length; j++) {
			
			if (imagePath[j].equals("") == false) {
				
				File file = new File(imagePath[j]);
				if (file.exists()) {
         		    //获得该图片的缩略图并进行圆角处理
					FileService fileService = new FileService(getApplicationContext());
         		    bitmaps[j] = fileService.getImageThumbnail(imagePath[j], 150, 150);
         		    bitmaps[j] = fileService.getRoundedCornerBitmap(bitmaps[j], 0);
         		    
					addNewImage(bitmaps[j], j);
					
				}
			}
		}
		
		//视频处理
		if (note.getVideo().equals("") == false) {
			addNewVideo(note.getVideo());
		}
	}
	
	//若图片宽度比屏幕宽度长，把图片压缩成宽度和屏幕宽度相同的图片
	@SuppressWarnings("deprecation")
	private Bitmap getMatchWindowBitmap(Bitmap bitmap){
		
		WindowManager wm = this.getWindowManager();
	    int width = wm.getDefaultDisplay().getWidth();
	    
	    Bitmap cfsBitmap = null;
		int bmpwidth = bitmap.getWidth();
		int bmpheight = bitmap.getHeight();
		
		if (bmpwidth > width) {
			int newWidth = width;
			float scaleWidth = ((float) newWidth) / bmpwidth;
			int newHeight=(int)(scaleWidth*bmpheight);
			cfsBitmap = Bitmap.createScaledBitmap(bitmap,newWidth , newHeight, false);
		}
		else {
			cfsBitmap = bitmap;
		}
	    
		return cfsBitmap;
	}
	
	
	//跳到下一条记事详情
	public void turnToNextNote() {
		
		if (position == 0) {
			Toast.makeText(getApplicationContext(), "This is the last note!", 1000).show();
		}
		else {
			Note previousNote = notes.get(position-1);		//因为listView和数据库中的note顺序是相反的，所以下一条记事实际上是数据库中的前一条
			
			Intent intent = new Intent();
			intent.setClass(NoteDetailActivity.this, NoteDetailActivity.class);
			intent.putExtra("notes", (Serializable)notes);
			intent.putExtra("noteDetail", (Serializable)previousNote);
			intent.putExtra("notePosition", position-1);
			NoteDetailActivity.this.finish();
			startActivity(intent);
			overridePendingTransition(R.anim.right_in, R.anim.left_out);	
		}
		
	}
	
	//跳到上一条记事详情
	private void turnToPreviousNote() {
		if (position == notes.size()-1) {
			Toast.makeText(getApplicationContext(), "This is the first note!", 1000).show();
		}
		else {
			Note nextNote = notes.get(position+1);
			
			Intent intent = new Intent();
			intent.setClass(NoteDetailActivity.this, NoteDetailActivity.class);
			intent.putExtra("notes", (Serializable)notes);
			intent.putExtra("noteDetail", (Serializable)nextNote);
			intent.putExtra("notePosition", position+1);
			NoteDetailActivity.this.finish();
			startActivity(intent);
			overridePendingTransition(R.anim.left_in, R.anim.right_out);
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
	
	//动态添加一张新图片，第一个参数是该图片的bitmap格式，第二个参数是这是该条记事中的第几张图片
	private void addNewImage(Bitmap bitmap, int num){
		
		FrameLayout fLayoutImg = new FrameLayout(NoteDetailActivity.this);
		
		ImageView img = new ImageView(NoteDetailActivity.this);
		img.setImageBitmap(bitmap);
		img.setScaleType(ScaleType.CENTER_CROP);
		img.setOnClickListener(new ViewBigPictureClickListener(num));	//点击该图片可以查看大图
		
		fLayoutImg.addView(img);
		
		LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(150, 140);
		lParams.setMargins(0, 0, 15, 0);
		
		llDetailImage.addView(fLayoutImg,lParams);
	}
	
	//动态添加一个新视频
	@SuppressWarnings("deprecation")
	private void addNewVideo(String path){

		AlphaAnimation animation = new AlphaAnimation(0.0f,1.0f);
		animation.setDuration(500);
		animation.setStartOffset(0);
		
		final FrameLayout fLayoutOther = new FrameLayout(NoteDetailActivity.this);
		
		VideoView videoView = new VideoView(NoteDetailActivity.this);
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
		
		LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 200);
		lParams.setMargins(0, 0, 15, 10);
		llDetailOther.addView(fLayoutOther, lParams);
		
	}
	
	 
	//查看大图事件监听
	private class ViewBigPictureClickListener implements OnClickListener{
		
		int num;    //表示点击的图片是第几张
		
		public ViewBigPictureClickListener(int num) {
			super();
			this.num = num;
		}

		@Override
		public void onClick(View v) {
			View view = LayoutInflater.from(NoteDetailActivity.this).inflate(R.layout.big_picture, null);
			final PopupWindow popBigPic = new PopupWindow(view, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			
			//用ViewPager来装载该条记事中的所有图片
			ViewPager viewPager = (ViewPager) view.findViewById(R.id.viewpager);
			List<ImageView> views = new ArrayList<ImageView>(); 
			
			Bitmap[] bitmaps = new Bitmap[]{
				null,null,null,null,
			};
			
			for (int j = 0; j < imagePath.length; j++) {
				
				if (imagePath[j].equals("") == false) {		//如果图片路径不为空，则将该图片添加到List中
					
					File file = new File(imagePath[j]);
					if (file.exists()) {
	         		   	
						bitmaps[j] = BitmapFactory.decodeFile(imagePath[j]);
						bitmaps[j] = getMatchWindowBitmap(bitmaps[j]);
						
						ImageView iv = new ImageView(NoteDetailActivity.this);
				        LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				        iv.setLayoutParams(lp);
				        iv.setPadding(30, 0, 30, 0);
				        iv.setImageBitmap(bitmaps[j]);
				        views.add(iv);
					}
				}
			}
				
			viewPager.setAdapter(new ViewPagerAdapter(views));		//适配器
			viewPager.setCurrentItem(num);			//设置刚开始显示第几张图片
			
			popBigPic.setFocusable(true);
			popBigPic.setAnimationStyle(R.style.popwin_bigpic_anim_style);
			
			ColorDrawable cDrawable = new ColorDrawable(0xe0000000);
			popBigPic.setBackgroundDrawable(cDrawable);
			popBigPic.showAtLocation(rlnoteDetail, Gravity.CENTER, 0, 0);
		}
	}
	
	//ViewPager适配器
	public class ViewPagerAdapter extends PagerAdapter{

		private List<ImageView> views;  
		 
        public ViewPagerAdapter(List<ImageView> views) {
           this.views = views;  
        }
        
        //获取当前窗体界面数
		@Override
		public int getCount() {
			return views.size();
		}

		//判断是否由对象生成界面
		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == (arg1);
		}
		
		//返回一个对象，这个对象表明了PagerAdapter适配器选择哪个对象放在当前的ViewPager中
		@Override
        public Object instantiateItem(View arg0,int arg1) {
			((ViewPager) arg0).addView(views.get(arg1), 0);
			return views.get(arg1); 
        }
		
		//是从ViewGroup中移出当前View
        public void destroyItem(View arg0, int arg1, Object arg2) {  
            ((ViewPager) arg0).removeView(views.get(arg1));  
        }
		
	}
	
	//点击返回键的事件响应
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			NoteDetailActivity.this.finish();
			overridePendingTransition(0, R.anim.left_bottom_out);
		    return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	//左右滑动事件响应
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		
		if(e1.getX() - e2.getX() > minDistance){
			turnToNextNote();
		}
		else if (e2.getX() - e1.getX() > minDistance) {
			turnToPreviousNote();
		}
		
		return false;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		
		return mDetailGestureDetector.onTouchEvent(event);
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

	
}
