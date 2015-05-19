package com.kinglin.easytravel;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.kinglin.model.Note;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

public class ShowImageGroupActivity extends Activity {
	
	private HashMap<String, List<String>> mGruopMap = new HashMap<String, List<String>>();
	private List<ImageBean> list = new ArrayList<ImageBean>();
	private final static int SCAN_OK = 1;
	private ProgressDialog mProgressDialog;
	private GroupAdapter adapter;
	private GridView mGroupGridView;
	private int picNum;
	
	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case SCAN_OK:
				//�رս�����
				mProgressDialog.dismiss();
				
				adapter = new GroupAdapter(ShowImageGroupActivity.this, list = subGroupOfImage(mGruopMap), mGroupGridView);
				mGroupGridView.setAdapter(adapter);
				break;
			}
		}
		
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_show_image_group);
		
		mGroupGridView = (GridView) findViewById(R.id.group_grid);
		
		getImages();
		picNum = (int) getIntent().getSerializableExtra("num");
		Toast.makeText(getApplicationContext(), "You can choose "+ picNum + "pictures", Toast.LENGTH_SHORT).show();
		
		mGroupGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				List<String> childList = mGruopMap.get(list.get(position).getFolderName());
				Intent mIntent = new Intent(ShowImageGroupActivity.this, ShowImageActivity.class);
				mIntent.putExtra("num", picNum);
				mIntent.putStringArrayListExtra("data", (ArrayList<String>)childList);
				startActivityForResult(mIntent, 1);
			}
		});
	}

	//�����Ӵ��ڹر�ʱ������������
	@SuppressWarnings("unchecked")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (resultCode == 1) {
			if (data != null) {             
	            Bundle bundle = data.getExtras();             
	            if (bundle != null) {                 
	            	List<String> newList;
	            	newList = bundle.getStringArrayList("pathList"); // �õ��Ӵ��ڵĻش�����
	    			
	    			Intent intent = new Intent();
	    			intent.putExtra("pathList", (Serializable)newList);
	    			ShowImageGroupActivity.this.setResult(1, intent);
	    			
	    			ShowImageGroupActivity.this.finish();              
	            }          
	        }
		}
	}

	/**
	 * ����ContentProviderɨ���ֻ��е�ͼƬ���˷��������������߳���
	 */
	private void getImages() {
		if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			Toast.makeText(this, "�����ⲿ�洢", Toast.LENGTH_SHORT).show();
			return;
		}
		
		//��ʾ������
		mProgressDialog = ProgressDialog.show(this, null, "���ڼ���...");
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				ContentResolver mContentResolver = ShowImageGroupActivity.this.getContentResolver();

				//ֻ��ѯjpeg��png��ͼƬ
				Cursor mCursor = mContentResolver.query(mImageUri, null,
						MediaStore.Images.Media.MIME_TYPE + "=? or "
								+ MediaStore.Images.Media.MIME_TYPE + "=?",
						new String[] { "image/jpeg", "image/png" }, MediaStore.Images.Media.DATE_MODIFIED);
				
				while (mCursor.moveToNext()) {
					//��ȡͼƬ��·��
					String path = mCursor.getString(mCursor
							.getColumnIndex(MediaStore.Images.Media.DATA));
					
					//��ȡ��ͼƬ�ĸ�·����
					String parentName = new File(path).getParentFile().getName();
					
					//���ݸ�·������ͼƬ���뵽mGruopMap��
					if (!mGruopMap.containsKey(parentName)) {	
						List<String> chileList = new ArrayList<String>();
						chileList.add(path);
						mGruopMap.put(parentName, chileList);
					} else {
						mGruopMap.get(parentName).add(path);
					}
				}
				mCursor.close();
				
				//֪ͨHandlerɨ��ͼƬ���
				mHandler.sendEmptyMessage(SCAN_OK);
			}
		}).start();
	}
	
	
	/*��װ�������GridView������Դ����Ϊ����ɨ���ֻ���ʱ��ͼƬ��Ϣ����HashMap��
	������Ҫ����HashMap��������װ��List*/
	private List<ImageBean> subGroupOfImage(HashMap<String, List<String>> mGruopMap){
		if(mGruopMap.size() == 0){
			return null;
		}
		List<ImageBean> list = new ArrayList<ImageBean>();
		
		Iterator<Map.Entry<String, List<String>>> it = mGruopMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, List<String>> entry = it.next();
			ImageBean mImageBean = new ImageBean();
			String key = entry.getKey();
			List<String> value = entry.getValue();
			
			mImageBean.setFolderName(key);
			mImageBean.setImageCounts(value.size());
			mImageBean.setTopImagePath(value.get(0));//��ȡ����ĵ�һ��ͼƬ
			list.add(mImageBean);
		}
		return list;
	}

}
