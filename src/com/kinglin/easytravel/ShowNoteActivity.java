package com.kinglin.easytravel;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
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
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kinglin.dao.DBHelper;
import com.kinglin.dao.FileService;
import com.kinglin.dao.ModelDaoImp;
import com.kinglin.model.Note;
import com.kinglin.model.User;

import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;

@SuppressLint({ "ShowToast", "ClickableViewAccessibility", "InflateParams" })
public class ShowNoteActivity extends Activity implements OnTouchListener,OnGestureListener{

	SQLiteDatabase db;
	
	RelativeLayout rlShowNote;
	ListView lvTimeLine;
	ImageButton ibtnTurnToAdd;
	PopupWindow popwinDelete;
	ImageView ivFingerRight, ivFingerLeft;
	TextView tvFingerRight, tvFingerLeft;
	
	List<Note> notes;
	GestureDetector mShowGestureDetector;		//手势操作
	private int minDistance = 200;			//上滑保存时手势滑动的最短距离
	
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
		setContentView(R.layout.activity_show_note);
	
		//锟皆控硷拷锟斤拷始锟斤拷锟斤拷锟斤拷锟斤拷锟铰凤拷实锟斤拷
		initContext();
		
		db = dbInit();
		//显示所有记事
		showNotes();
		
		//提示该界面可以左右滑动的动画
		ivFingerLeft.startAnimation(AnimationUtils.loadAnimation(this, R.anim.left_to_center));
		tvFingerLeft.startAnimation(AnimationUtils.loadAnimation(this, R.anim.left_to_center));
		ivFingerRight.startAnimation(AnimationUtils.loadAnimation(this, R.anim.right_to_center));
		tvFingerRight.startAnimation(AnimationUtils.loadAnimation(this, R.anim.right_to_center));
		
		lvTimeLine.setOnItemClickListener(new mOnItemClickListner());
		lvTimeLine.setOnItemLongClickListener(new mOnItemLongClickListener());
	
		ibtnTurnToAdd.setOnClickListener(new TurnToAddClickListener());
		ibtnTurnToAdd.setOnTouchListener(new TurnToAddTouchListener());
		rlShowNote.setOnTouchListener(this);
		rlShowNote.setLongClickable(true);
	}
	
	//初始化控件
	@SuppressWarnings("deprecation")
	private void initContext() {
		mShowGestureDetector = new GestureDetector(this);
		
		rlShowNote = (RelativeLayout) findViewById(R.id.rl_showNote);
		lvTimeLine = (ListView) findViewById(R.id.lv_timeLine);
		ibtnTurnToAdd = (ImageButton) findViewById(R.id.ibtn_turn_to_add);
		ivFingerLeft = (ImageView) findViewById(R.id.iv_fingerLeft);
		ivFingerRight = (ImageView) findViewById(R.id.iv_fingerRight);
		tvFingerLeft = (TextView) findViewById(R.id.tv_fingerLeft);
		tvFingerRight = (TextView) findViewById(R.id.tv_fingerRight);
	}
	
	//这个函数获取当前用户的数据库
	public SQLiteDatabase dbInit() {
		
		//先连接上default数据库，第一次使用会初始化各种实体对应的表
		DBHelper helper=new DBHelper(ShowNoteActivity.this, "default.db", null, 1);
	    SQLiteDatabase defaultdb=helper.getWritableDatabase();
        
        //查询default数据库中Configuration表的loginUser,返回相应的db
	    ModelDaoImp mdi = new ModelDaoImp(defaultdb);
        String userId = mdi.getUserConfiguration().getLoginUser();
        if (userId.equals("default")) {
			return defaultdb;
		}else {
			DBHelper helper1=new DBHelper(ShowNoteActivity.this, userId+".db", null, 1);
		    SQLiteDatabase userdb=helper1.getWritableDatabase();
		    return userdb;
		}
	}
	
	//从别的界面返回该界面时的响应，把所有记事再显示一遍
	@Override
	protected void onPostResume() {
		super.onPostResume();
		db = dbInit();
		showNotes();
	}
	
    
	//点击ListView的item的事件监听
	private class mOnItemClickListner implements OnItemClickListener{

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			Note detailnote = notes.get(notes.size() - position - 1);   //因为显示顺序和数据库中的顺序是相反的，这里要注意！！！
			
			Intent detailintent = new Intent();
			detailintent.setClass(ShowNoteActivity.this, NoteDetailActivity.class);
			detailintent.putExtra("notes", (Serializable)notes);
			detailintent.putExtra("noteDetail", (Serializable)detailnote);
			detailintent.putExtra("notePosition", notes.size() - position - 1);
			startActivity(detailintent);
			overridePendingTransition(R.anim.left_bottom_in, R.anim.normal_fade_out);
			
		}
	}
	
	//长按listview的item的事件响应
	private class mOnItemLongClickListener implements OnItemLongClickListener{

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				int position, long id) {
			
			final int realPosition = notes.size() - position -1;		//数据库中的该note的位置，注意listVIew中的顺序和数据库中是相反的
			
			View popView = LayoutInflater.from(ShowNoteActivity.this).inflate(R.layout.popmenu_operation, null);
			final PopupWindow popwinOperation = new PopupWindow(popView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			
			final Button menuDelete = (Button) popView.findViewById(R.id.menu_delete);
			final Button menuEdit = (Button) popView.findViewById(R.id.menu_edit);
			final Button menuShareTo = (Button) popView.findViewById(R.id.menu_shareTo);
			final Button menuTurnToMap = (Button) popView.findViewById(R.id.menu_turnToMap);
			
			menuDelete.setOnClickListener(new OnClickListener() {		//点击删除按钮
				@Override
				public void onClick(View v) {
					popwinOperation.dismiss();
					showDeletePopwin(realPosition);
				}
			});
			menuEdit.setOnClickListener(new OnClickListener() {			//点击编辑按钮
				@Override
				public void onClick(View v) {
					popwinOperation.dismiss();
					
					Note editNote = notes.get(realPosition);
					
		        	Intent editintent = new Intent();
		        	editintent.setClass(ShowNoteActivity.this, EditNoteActivity.class);
		        	editintent.putExtra("editNote", (Serializable)editNote);
		        	startActivity(editintent);
		        	overridePendingTransition(R.anim.center_in, R.anim.normal_fade_out);
				}
			});
			menuShareTo.setOnClickListener(new OnClickListener() {		//点击分享按钮
				@Override
				public void onClick(View v) {
					
					popwinOperation.dismiss();
					
					Note shareNote = notes.get(realPosition);
					String[] imagePath = new String[4];
		    		for (int j = 0; j < 4; j++) {
						imagePath = null;
					}
		    		imagePath = shareNote.getPictures().split(";");
					
		    		//分享到其他平台
					ShareSDK.initSDK(getApplicationContext());
					 OnekeyShare oks = new OnekeyShare();
					 //关闭sso授权
					 oks.disableSSOWhenAuthorize();
					 oks.setText(shareNote.getText());
					 oks.setImagePath(imagePath[0]);
					// 启动分享GUI
					 oks.show(getApplicationContext());	
					
				}
			});
			menuTurnToMap.setOnClickListener(new OnClickListener() {		//点击转到地图按钮
				@Override
				public void onClick(View v) {
					popwinOperation.dismiss();
					
					Note note = notes.get(realPosition);
		        	
		    		Intent mapintent = new Intent(getApplicationContext(), MapActivity.class);
		    		mapintent.putExtra("flag", 1);
		    		mapintent.putExtra("Note",(Serializable)note);
		    		mapintent.putExtra("Notes",(Serializable)notes);
		    		int mapPosition=notes.indexOf(note);
		    		
		    		mapintent.putExtra("position",mapPosition);
		    		startActivity(mapintent);
		    		overridePendingTransition(R.anim.right_in, R.anim.left_out);
				}
			});
			
			popwinOperation.setFocusable(true);
			popwinOperation.setBackgroundDrawable(getResources().getDrawable(R.drawable.popwin_bg));
			popwinOperation.setAnimationStyle(R.style.popwin_anim_style);
			popwinOperation.showAtLocation(rlShowNote, Gravity.CENTER, 0, 0);
			
			//弹出框时父窗口颜色变暗
			WindowManager.LayoutParams lp = getWindow().getAttributes();  
			lp.alpha = 0.7f;  
			getWindow().setAttributes(lp);
			//窗口消失时父窗口颜色恢复正常
			popwinOperation.setOnDismissListener(new OnDismissListener() {
			    @Override  
			    public void onDismiss() {  
			        WindowManager.LayoutParams lp = getWindow().getAttributes();  
			        lp.alpha = 1f;  
			        getWindow().setAttributes(lp); 
			    }  
			});
			
			return true;
		}
		
	}
	
	//弹出delete窗口
	private void showDeletePopwin(int position){
		View view = LayoutInflater.from(ShowNoteActivity.this).inflate(R.layout.popwin_alert, null);
		popwinDelete = new PopupWindow(view, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		
		TextView tvDelete = (TextView) view.findViewById(R.id.tv_alert);
		final Button btnDeleteYes = (Button) view.findViewById(R.id.btn_alertYes);
		final Button btnDeleteNo = (Button) view.findViewById(R.id.btn_alertNo);
		
		tvDelete.setText("Are you sure to delete this note?");
		
		btnDeleteYes.setOnClickListener(new mDeleteListener(position));
		btnDeleteNo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				popwinDelete.dismiss();
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
		btnDeleteYes.setOnTouchListener(mOnTouchListener);
		btnDeleteNo.setOnTouchListener(mOnTouchListener);
		
		popwinDelete.setFocusable(true);
		popwinDelete.setBackgroundDrawable(getResources().getDrawable(R.drawable.popwin_bg));
		popwinDelete.setAnimationStyle(R.style.popwin_anim_style);
		popwinDelete.showAtLocation(rlShowNote, Gravity.CENTER, 0, 0);
		
		//弹出框时父窗口颜色变暗
		WindowManager.LayoutParams lp = getWindow().getAttributes();  
		lp.alpha = 0.7f;  
		getWindow().setAttributes(lp);
		//窗口消失时父窗口颜色恢复正常
		popwinDelete.setOnDismissListener(new OnDismissListener() {
		    @Override  
		    public void onDismiss() {  
		        WindowManager.LayoutParams lp = getWindow().getAttributes();  
		        lp.alpha = 1f;  
		        getWindow().setAttributes(lp); 
		    }  
		});
	}
	
	//点击删除按钮的事件响应
	private class mDeleteListener implements OnClickListener{

		int position;
		public mDeleteListener(int position) {
			this.position = position;
		}

		@Override
		public void onClick(View v) {
			//在本地文件夹中删除该条记事的图片
			String[] imagePath = new String[4];
    		for (int j = 0; j < 4; j++) {
				imagePath = null;
			}
    		
    		imagePath = notes.get(position).getPictures().split(";");
    		for (int i = 0; i < imagePath.length; i++) {
    			
    			if (imagePath[i].equals("") == false) {
    				FileService fileService = new FileService(getApplicationContext());
            		String cfsImagePath = fileService.getCfsImagePath("/easyTravel/savefile/tempImages/", imagePath[i]);
            		
    				File file = new File(imagePath[i]);
    				File cfsFile = new File(cfsImagePath);
    				file.delete();
    				cfsFile.delete();
    			}
			}
    		
    		//在本地文件夹中删除该条记事的视频
    		if (notes.get(position).getVideo().equals("") == false) {
				File file = new File(notes.get(position).getVideo());
				if (file.exists()) {
					file.delete();
				}
			}
    		
    		//在数据库中删除该条记事
			ModelDaoImp mdi = new ModelDaoImp(db);
			mdi.deleteNote(notes.get(position));
			popwinDelete.dismiss();
			showNotes();
		}
	}
	
	//适配器，要把里面的内容写到showNote（）函数中去
	@SuppressLint("SimpleDateFormat")
	private void showNotes(){
		
		ModelDaoImp mdi = new ModelDaoImp(db);
		notes = mdi.getAllNotes();
		
		//对从数据库中获得的数据进行修改后再显示
		String[] times = new String[100];
		Bitmap[] bitmaps = new Bitmap[100];
		String[] textContents = new String[100];
		for (int i = 0; i < 100; i++) {
			times[i] = null;
			bitmaps[i] = null;
			textContents[i] = null;
		}
		
		ArrayList<HashMap<String, Object>> lstImageItem = new ArrayList<HashMap<String, Object>>();
        for (int i = 0; i < notes.size(); i++) {
    		String[] imagePath = new String[4];
    		for (int j = 0; j < 4; j++) {
				imagePath = null;
			}
    		
    		SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd");       
			String date = sDateFormat.format(new java.util.Date());
			
			//以下部分都要注意从数据库中获得的notes的顺序与我们要显示的顺序相反！！！
        	times[i] = notes.get(notes.size()-i-1).getTime().substring(0, 10);
        	//如果是当天则显示时间，否则显示日期
        	if (times[i].equals(date)) {
				times[i] = notes.get(notes.size()-i-1).getTime().substring(11, 19);
			}
        	
        	if (notes.get(notes.size()-i-1).getPictures().equals("") == false) {	//当图片不为空
        		//将图片的路径拆分成多个
        		imagePath = notes.get(notes.size()-i-1).getPictures().split(";");
        		
        		//获得压缩图片
        		FileService fileService = new FileService(getApplicationContext());
        		String cfsImagePath = fileService.getCfsImagePath("/easyTravel/savefile/tempImages/", imagePath[0]);
        		
        		File file = new File(cfsImagePath);
				if (file.exists()) {
					bitmaps[i] = BitmapFactory.decodeFile(cfsImagePath);
					bitmaps[i] = fileService.getRoundedCornerBitmap(bitmaps[i], 1);		//对图片圆角处理
					textContents[i] = "";		//图片为空时不显示文字
				}
        	}
        	else {		//当图片为空
				textContents[i] = notes.get(notes.size()-i-1).getText();
			}
        	
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("itemNoteId", notes.get(notes.size()-i-1).getNoteId());
            map.put("itemTime", times[i]);
            map.put("itemTitle", notes.get(notes.size()-i-1).getTitle());
            //map.put("itemPermission", notes.get(notes.size()-i-1).getPermission());
            map.put("itemWeather", notes.get(notes.size()-i-1).getWeather());
            map.put("itemTextContent", textContents[i]);
            map.put("itemPictures", bitmaps[i]);
            map.put("itemVoice", null);
            map.put("itemVideo", notes.get(notes.size()-i-1).getVideo());
            map.put("itemLocationx", notes.get(notes.size()-i-1).getLocationx());
            map.put("itemLocationy", notes.get(notes.size()-i-1).getLocationy());
            map.put("itemLastChangeTime", notes.get(notes.size()-i-1).getLastChangeTime());
            map.put("itemOperation", notes.get(notes.size()-i-1).getOperation());
            lstImageItem.add(map);
        }
		
		lvTimeLine.setAdapter(new ListViewAdapter(this, lstImageItem));
        
	}
	
	
	//加号按钮的触摸事件监听，可以随意拖动该按钮到屏幕任何位置
	private class TurnToAddTouchListener implements View.OnTouchListener{

		int lastX,lastY,x1,x2,y1,y2; 
	        
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			
			DisplayMetrics dm = getResources().getDisplayMetrics();  
		    final int screenWidth = dm.widthPixels;  
		    final int screenHeight = dm.heightPixels - 50; 
		     
			switch (event.getAction()) {  
	            
				case MotionEvent.ACTION_DOWN:  
	                lastX = (int) event.getRawX();  
	                lastY = (int) event.getRawY();
	                x1 = (int) event.getRawX();
	                y1 = (int) event.getRawY();
	                
	                v.setScaleX(0.9f);
					v.setScaleY(0.9f);
	                break;  
	            
				case MotionEvent.ACTION_MOVE:  
	                int dx = (int) event.getRawX() - lastX;  
	                int dy = (int) event.getRawY() - lastY;  
	
	                int left = v.getLeft() + dx;  
	                int top = v.getTop() + dy;  
	                int right = v.getRight() + dx;  
	                int bottom = v.getBottom() + dy;  
	
	                if (left < 20) {  
	                    left = 20;  
	                    right = left + v.getWidth();  
	                }  
	
	                if (right > screenWidth-20) {  
	                    right = screenWidth-20;  
	                    left = right - v.getWidth();  
	                }  
	
	                if (top < 20) {  
	                    top = 20;  
	                    bottom = top + v.getHeight();  
	                }  
	
	                if (bottom > screenHeight-20) {  
	                    bottom = screenHeight-20;  
	                    top = bottom - v.getHeight();  
	                }  
	
	                v.layout(left, top, right, bottom);  
	
	                lastX = (int) event.getRawX();  
	                lastY = (int) event.getRawY();  
	
	                break;  
				case MotionEvent.ACTION_UP:
					x2 = (int) event.getRawX();  
	                y2 = (int) event.getRawY();
	                
	                v.setScaleX(1f);
					v.setScaleY(1f);
	                
	                double distance = Math.sqrt(Math.abs(x1-x2)*Math.abs(x1-x2)+Math.abs(y1-y2)*Math.abs(y1-y2));
	                if (distance < 15) {		//距离较小，当作click事件来处理
						return false;			//返回值为false时表示可以执行OnClickListener
					}
	                else {
						return true;		//返回值为true时表示不会执行OnClickListener
					}  
            }  
			
			return false;
		}
	}
	
	//加号按钮点击事件监听
	private class TurnToAddClickListener implements View.OnClickListener{
		@Override
		public void onClick(View v) {
			
			Intent addintent = new Intent();
			addintent.setClass(ShowNoteActivity.this, AddNoteActivity.class);
			ShowNoteActivity.this.finish();
			startActivity(addintent);
			overridePendingTransition(R.anim.top_in, R.anim.bottom_out);
		}
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
	
	//设置2秒内点击两次返回键就退出程序
	public void exit(){
		if (!isExit) {  
            isExit = true;  
            Toast.makeText(getApplicationContext(), "one more click to exit", Toast.LENGTH_SHORT).show();  
            mHandler.sendEmptyMessageDelayed(0, 2000);  
        } else {  
            Intent intent = new Intent(Intent.ACTION_MAIN);  
            intent.addCategory(Intent.CATEGORY_HOME);  
            startActivity(intent);  
            System.exit(0);  
        }  
	}
	
	//手势操作事件监听
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		
		if(e1.getX() - e2.getX() > minDistance)
		{
			//这里是直接右划进入地图模块的响应函数
			Intent mapintent = new Intent(getApplicationContext(), MapActivity.class);
			mapintent.putExtra("flag", 2);
			mapintent.putExtra("notes", (Serializable)notes);
			startActivity(mapintent);
			overridePendingTransition(R.anim.right_in, R.anim.left_out);
		}
		else if(e2.getX() - e1.getX() > minDistance){
			
			/*从现在的db中取得username，若不是default就跳到用户信息管理界面，
			若为default就跳到登陆的界面*/
			
			DBHelper helper=new DBHelper(ShowNoteActivity.this, "default.db", null, 1);
		    SQLiteDatabase defaultdb=helper.getWritableDatabase();
			ModelDaoImp mdi = new ModelDaoImp(defaultdb);
			String loginUser = mdi.getUserConfiguration().getLoginUser();
			
			if (loginUser.equals("default")) {
				Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
				startActivity(intent);
				overridePendingTransition(R.anim.left_in, R.anim.right_out);
			}else{
				Intent userInforintent = new Intent(getApplicationContext(), UserInformationActivity.class);
				User user = mdi.getUserInformation();
				userInforintent.putExtra("userLogin", user);
				startActivity(userInforintent);
				overridePendingTransition(R.anim.left_in, R.anim.right_out);
			}
			
		}
		
		return false;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return mShowGestureDetector.onTouchEvent(event);
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
