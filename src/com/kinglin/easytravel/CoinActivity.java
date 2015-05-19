package com.kinglin.easytravel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.kinglin.dao.DBHelper;
import com.kinglin.dao.ModelDaoImp;
import com.kinglin.model.Coin;

public class CoinActivity extends Activity {

	ImageButton ibtnCoinReturn,ibtnCoinExpandRules;
	TextView tvCoinObtainRules;
	LinearLayout llayoutCoinObtainRules;
	ListView lvCoinDetails;
	
	private int rotate = 0;
	
	SQLiteDatabase db;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_coin);
		
		initContext();
		db = dbInit();
		
		showListView();
		
		ibtnCoinReturn.setOnClickListener(new CoinReturnClickListener());
		ibtnCoinExpandRules.setOnClickListener(new ExpandRulesClickListener());
		
	}
	
	//��ʼ���ؼ�
	void initContext(){
		
		ibtnCoinReturn = (ImageButton) findViewById(R.id.ibtn_coinReturn);
		ibtnCoinExpandRules = (ImageButton) findViewById(R.id.ibtn_coinExpandRules);
		tvCoinObtainRules = (TextView) findViewById(R.id.tv_coinObtainRules);
		llayoutCoinObtainRules = (LinearLayout) findViewById(R.id.ll_coinObtainRules);
		lvCoinDetails = (ListView) findViewById(R.id.lv_coinDetails);
		
		/*tvCoinObtainRules.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);*/
	}
	
	//���������ȡ��ǰ�û������ݿ�
	public SQLiteDatabase dbInit() {
		
		//��������default���ݿ⣬��һ��ʹ�û��ʼ������ʵ���Ӧ�ı�
		DBHelper helper=new DBHelper(this, "default.db", null, 1);
	    SQLiteDatabase defaultdb=helper.getWritableDatabase();
        
        //��ѯdefault���ݿ���Configuration���loginUser,������Ӧ��db
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
	
	//������ذ�ť���¼���Ӧ
	private class CoinReturnClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			
			CoinActivity.this.finish();
			overridePendingTransition(0, R.anim.center_out);
			
		}
	}
	
	//������ֻ�ȡ����ť���¼���Ӧ
	private class ExpandRulesClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			
			RotateAnimation rAnimation;
			
			if (rotate == 0) {
				
				rAnimation = new RotateAnimation(0, 180,
						Animation.RELATIVE_TO_SELF, 0.5f,
		                Animation.RELATIVE_TO_SELF, 0.5f);
				rAnimation.setDuration(500);
				rAnimation.setFillAfter(true);
				rAnimation.setAnimationListener(new ExpandAnimationListener());
				ibtnCoinExpandRules.startAnimation(rAnimation);
				
				rotate = 1;
			}
			else {
				
				rAnimation = new RotateAnimation(180, 360,
						Animation.RELATIVE_TO_SELF, 0.5f,
		                Animation.RELATIVE_TO_SELF, 0.5f);
				rAnimation.setDuration(500);
				rAnimation.setFillAfter(true);
				rAnimation.setAnimationListener(new EnExpandAnimationListener());
				ibtnCoinExpandRules.startAnimation(rAnimation);
				
				rotate = 0;
			}
		}
	}
	
	//չ��ʱ���������¼�
	private class ExpandAnimationListener implements AnimationListener{

		//������ʼʱ
		@Override
		public void onAnimationStart(Animation animation) {
			
			TranslateAnimation tAnimation = new TranslateAnimation(
					Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, 
					Animation.RELATIVE_TO_SELF, 0, Animation.ABSOLUTE, 220);
			tAnimation.setDuration(500);
			tAnimation.setStartOffset(0);
			tAnimation.setFillAfter(true);
			
			llayoutCoinObtainRules.startAnimation(tAnimation);
			lvCoinDetails.startAnimation(tAnimation);
			
			ibtnCoinExpandRules.setClickable(false);
			
		}

		//��������ʱ
		@Override
		public void onAnimationEnd(Animation animation) {
			lvCoinDetails.clearAnimation();
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
			
			params.setMargins(0, 410, 0, 0);
			lvCoinDetails.setLayoutParams(params);
			
			ibtnCoinExpandRules.setClickable(true);
			
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
			
		}
		
	}
	
	//����ʱ���������¼�
	private class EnExpandAnimationListener implements AnimationListener{

		@Override
		public void onAnimationStart(Animation animation) {
			TranslateAnimation tAnimationll = new TranslateAnimation(
					Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, 
					Animation.ABSOLUTE, 220, Animation.RELATIVE_TO_SELF, 0);
			tAnimationll.setDuration(500);
			tAnimationll.setStartOffset(0);
			tAnimationll.setFillAfter(true);
			llayoutCoinObtainRules.startAnimation(tAnimationll);
			
			TranslateAnimation tAnimationlv = new TranslateAnimation(
					Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, 
					Animation.RELATIVE_TO_SELF, 0, Animation.ABSOLUTE, -220);
			tAnimationlv.setDuration(500);
			tAnimationlv.setStartOffset(0);
			lvCoinDetails.startAnimation(tAnimationlv);
			
			ibtnCoinExpandRules.setClickable(false);
			
		}

		@Override
		public void onAnimationEnd(Animation animation) {
			lvCoinDetails.clearAnimation();
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
			
			params.setMargins(0, 190, 0, 0);
			lvCoinDetails.setLayoutParams(params);
			
			ibtnCoinExpandRules.setClickable(true);
			
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
			
		}
		
	}

	
	//��listView���������
	@SuppressWarnings("unused")
	private void showListView(){
		
		ModelDaoImp mdi = new ModelDaoImp(db);
		List<Coin> coins = mdi.getAllCoin();
		
		//�������Լ����õ�����
		String[] cointimes = new String[]
				{
				"2015-4-17  3:20","2015-4-16  4:30","2015-4-13  5:40",
				};
		String[] coincontents = new String[]
				{
				"send a private note","new user sign up","send a public note",
				};
		String[] coinvalues = new String[]
				{
				"+20","+30","+10",
				};
		
		ArrayList<HashMap<String, Object>> coinDetailItem = new ArrayList<HashMap<String, Object>>();
        for (int i = 0; i < 3; i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("itemTime", cointimes[i]);
            map.put("itemContent", coincontents[i]);
            map.put("itemValue", coinvalues[i]);
            coinDetailItem.add(map);
        }
        
        SimpleAdapter coinItemAdapter = new SimpleAdapter(this, 
                coinDetailItem,// ����Դ
                R.layout.item_coin_detail,// ��ʾ����
                new String[] { "itemTime", "itemContent","itemValue" }, 
                new int[] { R.id.tv_coinItemTime, R.id.tv_coinItemContent, R.id.tv_coinItemValue });        
        
        lvCoinDetails.setAdapter(coinItemAdapter);
	}

	

}
