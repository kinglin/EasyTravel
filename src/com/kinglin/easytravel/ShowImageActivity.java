package com.kinglin.easytravel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.kinglin.easytravel.NoteDetailActivity.ViewPagerAdapter;

import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout.LayoutParams;

public class ShowImageActivity extends Activity {
	
	Map<Integer, Boolean> isCheckMap =  new HashMap<Integer, Boolean>();
    final List<HashMap<String, Integer>> isCheckList = new ArrayList<HashMap<String, Integer>>();
	private RelativeLayout rlShowImage;
    private GridView mGridView;
	private Button makesure;
	private List<String> list;
	private ChildAdapter adapter;
	private int picNum;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_show_image);
		
		rlShowImage = (RelativeLayout) findViewById(R.id.rl_showImage);
		mGridView = (GridView) findViewById(R.id.child_grid);
		makesure=(Button)findViewById(R.id.btn_childSure);
		makesure.setOnClickListener(new okButtonClickListener());
		
		list = getIntent().getStringArrayListExtra("data");
		picNum = (int) getIntent().getSerializableExtra("num");
		
		adapter = new ChildAdapter(this, list, mGridView);
		mGridView.setAdapter(adapter);
		mGridView.setOnItemClickListener(mGItemClickListener);
	}
	
	//点击GridView的item的事件监听
	private OnItemClickListener mGItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			//本文件夹图片少于100张时可查看大图，否则不可查看大图
			if (list.size() < 100) {
				Bitmap[] bitmaps = new Bitmap[list.size()];
				
				View bigView = LayoutInflater.from(ShowImageActivity.this).inflate(R.layout.big_picture, null);
				PopupWindow popBigPic = new PopupWindow(bigView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
				
				ViewPager viewPager = (ViewPager) bigView.findViewById(R.id.viewpager);
				List<ImageView> views = new ArrayList<ImageView>();
				
				for(int i = 0; i < list.size() ; i++){
					bitmaps[i] = null;
					bitmaps[i] = BitmapFactory.decodeFile(list.get(i));
					bitmaps[i] = getMatchWindowBitmap(bitmaps[i]);
					
					ImageView iv = new ImageView(ShowImageActivity.this);
			        LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			        iv.setLayoutParams(lp);
			        iv.setPadding(30, 0, 30, 0);
			        iv.setImageBitmap(bitmaps[i]);
			        views.add(iv);
			    }
				
				viewPager.setAdapter(new ViewPagerAdapter(views));
				viewPager.setCurrentItem(position);
				
				popBigPic.setFocusable(true);
				popBigPic.setAnimationStyle(R.style.popwin_bigpic_anim_style);
				
				ColorDrawable cDrawable = new ColorDrawable(0xe0000000);
				popBigPic.setBackgroundDrawable(cDrawable);
				popBigPic.showAtLocation(rlShowImage, Gravity.CENTER, 0, 0);
			}
			
		}
	};
	
	private final class  okButtonClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			if (adapter.getSelectItems().size() > picNum) {
				
				Toast.makeText(getApplicationContext(), "You can only choose " + picNum + "pictures", Toast.LENGTH_LONG).show();
				
			}else {
				List<String> newList = new ArrayList<String>();
				for (int i = 0; i < adapter.getSelectItems().size(); i++) {
					newList.add(list.get(adapter.getSelectItems().get(i)));
				}
				
				Intent intent = new Intent();
				intent.putExtra("pathList", (Serializable)newList);
				ShowImageActivity.this.setResult(1, intent);
				
				ShowImageActivity.this.finish();
			}
			
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

}
