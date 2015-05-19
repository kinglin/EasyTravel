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
		
		//�����ȡ����ҳ�洫������note����
		notes = (List<Note>) getIntent().getSerializableExtra("notes");
		noteDetail = (Note) getIntent().getSerializableExtra("noteDetail");
		position = (int)getIntent().getSerializableExtra("notePosition");
		
		if (noteDetail.getPictures().equals("")) {
			llDetailImage.setVisibility(View.GONE);
			
			if (noteDetail.getVideo().equals("")) {
				tvDetailContent.setLines(10);
			}
		}
		
		//��ʾ������ϸ��Ϣ
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
	
	//��ʼ���ؼ�������
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
	
	//���������ȡ��ǰ�û������ݿ�
	public SQLiteDatabase dbInit() {
		
		//��������default���ݿ⣬��һ��ʹ�û��ʼ������ʵ���Ӧ�ı�
		DBHelper helper=new DBHelper(NoteDetailActivity.this, "default.db", null, 1);
	    SQLiteDatabase defaultdb=helper.getWritableDatabase();
        
        //��ѯdefault���ݿ���Configuration���loginUser,������Ӧ��db
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
	
	//��ʾ������ϸ��Ϣ
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
		
		//ͼƬ����
		Bitmap[] bitmaps = new Bitmap[]{
			null,null,null,null,
		};
		
		imagePath = note.getPictures().split(";");
		for (int j = 0; j < imagePath.length; j++) {
			
			if (imagePath[j].equals("") == false) {
				
				File file = new File(imagePath[j]);
				if (file.exists()) {
         		    //��ø�ͼƬ������ͼ������Բ�Ǵ���
					FileService fileService = new FileService(getApplicationContext());
         		    bitmaps[j] = fileService.getImageThumbnail(imagePath[j], 150, 150);
         		    bitmaps[j] = fileService.getRoundedCornerBitmap(bitmaps[j], 0);
         		    
					addNewImage(bitmaps[j], j);
					
				}
			}
		}
		
		//��Ƶ����
		if (note.getVideo().equals("") == false) {
			addNewVideo(note.getVideo());
		}
	}
	
	//��ͼƬ��ȱ���Ļ��ȳ�����ͼƬѹ���ɿ�Ⱥ���Ļ�����ͬ��ͼƬ
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
	
	
	//������һ����������
	public void turnToNextNote() {
		
		if (position == 0) {
			Toast.makeText(getApplicationContext(), "This is the last note!", 1000).show();
		}
		else {
			Note previousNote = notes.get(position-1);		//��ΪlistView�����ݿ��е�note˳�����෴�ģ�������һ������ʵ���������ݿ��е�ǰһ��
			
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
	
	//������һ����������
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
	
	//��ʾ�û�֮ǰѡ�������
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
	
	//��̬���һ����ͼƬ����һ�������Ǹ�ͼƬ��bitmap��ʽ���ڶ������������Ǹ��������еĵڼ���ͼƬ
	private void addNewImage(Bitmap bitmap, int num){
		
		FrameLayout fLayoutImg = new FrameLayout(NoteDetailActivity.this);
		
		ImageView img = new ImageView(NoteDetailActivity.this);
		img.setImageBitmap(bitmap);
		img.setScaleType(ScaleType.CENTER_CROP);
		img.setOnClickListener(new ViewBigPictureClickListener(num));	//�����ͼƬ���Բ鿴��ͼ
		
		fLayoutImg.addView(img);
		
		LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(150, 140);
		lParams.setMargins(0, 0, 15, 0);
		
		llDetailImage.addView(fLayoutImg,lParams);
	}
	
	//��̬���һ������Ƶ
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
	
	 
	//�鿴��ͼ�¼�����
	private class ViewBigPictureClickListener implements OnClickListener{
		
		int num;    //��ʾ�����ͼƬ�ǵڼ���
		
		public ViewBigPictureClickListener(int num) {
			super();
			this.num = num;
		}

		@Override
		public void onClick(View v) {
			View view = LayoutInflater.from(NoteDetailActivity.this).inflate(R.layout.big_picture, null);
			final PopupWindow popBigPic = new PopupWindow(view, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			
			//��ViewPager��װ�ظ��������е�����ͼƬ
			ViewPager viewPager = (ViewPager) view.findViewById(R.id.viewpager);
			List<ImageView> views = new ArrayList<ImageView>(); 
			
			Bitmap[] bitmaps = new Bitmap[]{
				null,null,null,null,
			};
			
			for (int j = 0; j < imagePath.length; j++) {
				
				if (imagePath[j].equals("") == false) {		//���ͼƬ·����Ϊ�գ��򽫸�ͼƬ��ӵ�List��
					
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
				
			viewPager.setAdapter(new ViewPagerAdapter(views));		//������
			viewPager.setCurrentItem(num);			//���øտ�ʼ��ʾ�ڼ���ͼƬ
			
			popBigPic.setFocusable(true);
			popBigPic.setAnimationStyle(R.style.popwin_bigpic_anim_style);
			
			ColorDrawable cDrawable = new ColorDrawable(0xe0000000);
			popBigPic.setBackgroundDrawable(cDrawable);
			popBigPic.showAtLocation(rlnoteDetail, Gravity.CENTER, 0, 0);
		}
	}
	
	//ViewPager������
	public class ViewPagerAdapter extends PagerAdapter{

		private List<ImageView> views;  
		 
        public ViewPagerAdapter(List<ImageView> views) {
           this.views = views;  
        }
        
        //��ȡ��ǰ���������
		@Override
		public int getCount() {
			return views.size();
		}

		//�ж��Ƿ��ɶ������ɽ���
		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == (arg1);
		}
		
		//����һ������������������PagerAdapter������ѡ���ĸ�������ڵ�ǰ��ViewPager��
		@Override
        public Object instantiateItem(View arg0,int arg1) {
			((ViewPager) arg0).addView(views.get(arg1), 0);
			return views.get(arg1); 
        }
		
		//�Ǵ�ViewGroup���Ƴ���ǰView
        public void destroyItem(View arg0, int arg1, Object arg2) {  
            ((ViewPager) arg0).removeView(views.get(arg1));  
        }
		
	}
	
	//������ؼ����¼���Ӧ
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			NoteDetailActivity.this.finish();
			overridePendingTransition(0, R.anim.left_bottom_out);
		    return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	//���һ����¼���Ӧ
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
