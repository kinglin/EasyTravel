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
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.kinglin.dao.DBHelper;
import com.kinglin.dao.ModelDaoImp;
import com.kinglin.model.Treasure;

public class TreasureActivity extends Activity {

	ImageButton ibtnTreasureReturn;
	ListView lvTreasureDetails;
	
	SQLiteDatabase db;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_treasure);
		
		initContext();
		db = dbInit();
		
		showListView();
		
		ibtnTreasureReturn.setOnClickListener(new TreasureReturnClickListener());
		
	}

	//��ʼ���ؼ�
	private void initContext(){
		
		ibtnTreasureReturn = (ImageButton) findViewById(R.id.ibtn_treasureReturn);
		lvTreasureDetails = (ListView) findViewById(R.id.lv_treasureDetails);
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
	
	//������Ͻǵķ��ذ�ťʱ���¼�����
	private class TreasureReturnClickListener implements OnClickListener{
		@Override
		public void onClick(View v) {
			
			TreasureActivity.this.finish();
			overridePendingTransition(0, R.anim.center_out);
		}
	}
	
	//��ʾ�û��ѻ�õ����б�����
	private void showListView() {
		
		ModelDaoImp mdi = new ModelDaoImp(db);
		List<Treasure> treasures = mdi.getAllTreasure();
		
		ArrayList<HashMap<String, Object>> coinDetailItem = new ArrayList<HashMap<String, Object>>();
        for (int i = 0; i < treasures.size(); i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("itemTime", treasures.get(i).getTime());
            map.put("itemContent", treasures.get(i).getContent());
            map.put("itemValue", 100);
            coinDetailItem.add(map);
        }
        
        SimpleAdapter treasureItemAdapter = new SimpleAdapter(this, 
                coinDetailItem,// ����Դ
                R.layout.item_coin_detail,// ��ʾ����
                new String[] { "itemTime", "itemContent","itemValue" }, 
                new int[] { R.id.tv_coinItemTime, R.id.tv_coinItemContent, R.id.tv_coinItemValue });        
        
        lvTreasureDetails.setAdapter(treasureItemAdapter);
	}



}
