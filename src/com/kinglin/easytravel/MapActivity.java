package com.kinglin.easytravel;

import java.io.File;
import java.io.Serializable;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.InfoWindow.OnInfoWindowClickListener;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.kinglin.dao.DBHelper;
import com.kinglin.dao.FileService;
import com.kinglin.dao.ModelDaoImp;
import com.kinglin.model.Note;

@SuppressLint({ "SimpleDateFormat", "InflateParams" })
public class MapActivity extends Activity implements OnMapClickListener {

	private MapView mMapView;
	private BaiduMap mBaiduMap;
	private LocationClient mLocationClient;
	ImageButton locationButton;
	private MyLocationListener mLocationListener;
	public String mAddr;
	public double mLatitude;
	public double mLongitude;
	public boolean firstIn = true;
	public Context context;
	RelativeLayout rlMap;
	Switch switch1;
	@SuppressWarnings("unused")
	private Marker markerA;
	Button marker;
	@SuppressWarnings("unused")
	private boolean tripNameClicked;
	Long noteID;
	Note note;
	List<Note> notes;
	List<Marker> markers;
	private int position;
	private int flag;
	private BitmapDescriptor bitmap;
	SQLiteDatabase db;

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.context = this;
		SDKInitializer.initialize(getApplicationContext());

		setContentView(R.layout.activity_map);
        //初始化地图    
		initView();
		// 初始化位置
		initLocation();
		
		flag = (int) getIntent().getSerializableExtra("flag");
		notes = (List<Note>) getIntent().getSerializableExtra("Notes");
        //判断是从点击跳转还是左划跳转
		if (flag == 1) {
			note = (Note) getIntent().getSerializableExtra("Note");

			position = (int) getIntent().getSerializableExtra("position");
			initNote(note);
			try {
				initRoute(note);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else if (flag == 2) {
			Date dateNowDate=new Date();
			SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd");
			String dateString=dateFormat.format(dateNowDate);
			Log.d("date", dateString);
			try {
				initRoute(dateString, dateString);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
		}
		rlMap = (RelativeLayout) findViewById(R.id.rl_map);

//		switch1 = (Switch) findViewById(R.id.switch1);
//		switch1.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//
//			public void onCheckedChanged(CompoundButton buttonView,
//					boolean isChecked) {
//				// TODO Auto-generated method stub
//				if (isChecked) {
//					Intent intent = new Intent(MapActivity.this,
//							com.kinglin.service.PushTreasureService.class);
//					startService(intent);
//					Toast.makeText(context, "Refresh Start", Toast.LENGTH_SHORT)
//							.show();
//					Log.d("myservice", "service");
//				} else {
//					Intent intent = new Intent(MapActivity.this,
//							com.kinglin.service.PushTreasureService.class);
//					stopService(intent);
//					Toast.makeText(context, "Refresh End", Toast.LENGTH_SHORT)
//							.show();
//				}
//			}
//		});
		marker = (Button) findViewById(R.id.marker);
		if (flag == 1) {
			marker.setText("Show All Moments Today");
		} else if (flag == 2) {
			marker.setText("Choose Time");
		}
		marker.setOnTouchListener(new View.OnTouchListener() {
			@SuppressLint("ClickableViewAccessibility")
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					v.setScaleX(0.95f);
					v.setScaleY(0.95f);
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
		});
		marker.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (flag == 1) {
					try {
						initAll(note);

					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					;
				} else {

					onChooseClick();
				}

			}
		});
		locationButton = (ImageButton) findViewById(R.id.map_my_location);
		locationButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				centerToMyLocation();

			}
		});

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);

	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onRestoreInstanceState(savedInstanceState);

	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		mBaiduMap.setMyLocationEnabled(true);
		if (!mLocationClient.isStarted()) {
			mLocationClient.start();
		}
	}
	//地图移动到我当前的位置
	protected void centerToMyLocation() {
		LatLng latLng = new LatLng(mLatitude, mLongitude);
		MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latLng);
		mBaiduMap.setMapStatus(msu);
		if (mAddr == null) {
			Toast.makeText(context, "Network Error:Can not init map",
					Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(context, "You are at:" + mAddr, Toast.LENGTH_SHORT)
					.show();
		}

	}
   //获得位置
	private void initLocation() {
		mLocationClient = new LocationClient(this);
		mLocationListener = new MyLocationListener();
		mLocationClient.registerLocationListener(mLocationListener);
		LocationClientOption option = new LocationClientOption();
		option.setCoorType("bd09ll");
		option.setIsNeedAddress(true);
		option.setOpenGps(true);
		option.setScanSpan(3000);
		mLocationClient.setLocOption(option);
	}
  //位置监听
	public class MyLocationListener implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			MyLocationData data = new MyLocationData.Builder()//
					.accuracy(location.getRadius())//
					.latitude(location.getLatitude())//
					.longitude(location.getLongitude()).build();

			mBaiduMap.setMyLocationData(data);
			mAddr = location.getAddrStr();
			mLatitude = location.getLatitude();
			mLongitude = location.getLongitude();
			if (firstIn && flag == 2) {
				LatLng latLng = new LatLng(location.getLatitude(),
						location.getLongitude());
				MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latLng);
				mBaiduMap.setMapStatus(msu);

				firstIn = false;
				Toast.makeText(context, "You are at:" + location.getAddrStr(),
						Toast.LENGTH_SHORT).show();
			}
		}

	}
	//加载地图
	private void initView() {
		mMapView = (MapView) findViewById(R.id.bmapView);
		mBaiduMap = mMapView.getMap();
		MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(17.0f);
		mBaiduMap.setMapStatus(msu);

		mBaiduMap.setOnMapClickListener(this);

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		// 在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
		mMapView.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// 在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
		mMapView.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		// 在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
		mMapView.onPause();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();

		mLocationClient.stop();

	}

	
	// 得到当天notes
	public List<Note> getTodayNotes() throws ParseException {
		db = dbInit();
		Format f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		String date = note.getTime();
		Date d = (Date) f.parseObject(date);
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");// 设置日期格式

		String dateForSql = df.format(d);
		String sql = "select * from note where time like '" + dateForSql + "%'";

		List<Note> todayNotes = new ArrayList<Note>();

		Cursor cursor = db.rawQuery(sql, null);

		while (cursor.moveToNext()) {

			long noteId = cursor.getLong(cursor.getColumnIndex("noteId"));
			String time = cursor.getString(cursor.getColumnIndex("time"));
			int permission = cursor.getInt(cursor.getColumnIndex("permission"));
			int weather = cursor.getInt(cursor.getColumnIndex("weather"));
			String text = cursor.getString(cursor.getColumnIndex("text"));
			String title = cursor.getString(cursor.getColumnIndex("title"));
			String pictures = cursor.getString(cursor
					.getColumnIndex("pictures"));
			String voice = cursor.getString(cursor.getColumnIndex("voice"));
			double locationx = cursor.getDouble(cursor
					.getColumnIndex("locationx"));
			double locationy = cursor.getDouble(cursor
					.getColumnIndex("locationy"));
			String video = cursor.getString(cursor.getColumnIndex("video"));
			long lastChangeTime = cursor.getLong(cursor
					.getColumnIndex("lastChangeTime"));
			int operation = cursor.getInt(cursor.getColumnIndex("operation"));
			todayNotes.add(new Note(noteId, time, permission, weather, text,
					title, pictures, voice, locationx, locationy, video,
					lastChangeTime, operation));
		}

		cursor.close();
		db.close();

		return todayNotes;

	}

	// 查询当天路径
	public List<LatLng> getPoints() throws ParseException {

		db = dbInit();
		Format f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		String date = note.getTime();
		Date d = (Date) f.parseObject(date);
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");// 设置日期格式

		String dateForSql = df.format(d);
		String sql = "select * from route where date like '" + dateForSql
				+ "%'";

		List<LatLng> pointList = new ArrayList<LatLng>();

		Cursor cursor = db.rawQuery(sql, null);

		while (cursor.moveToNext()) {

			double latitude = cursor.getDouble(cursor
					.getColumnIndex("latitude")) + 0.00337;
			double longitude = cursor.getDouble(cursor
					.getColumnIndex("longtitude")) + 0.012;
			LatLng point = new LatLng(latitude, longitude);
			pointList.add(point);
		}

		cursor.close();
		db.close();
		return pointList;
	}

	// 加载该记事当天所有动态
	private void initAll(final Note note) throws ParseException {

		List<Note> todayNotes = getTodayNotes();
		LatLng latLng = null;
		OverlayOptions overlayOptions;
		bitmap = BitmapDescriptorFactory.fromResource(R.drawable.location1);
		Marker marker;
		for (int i = 0; i < todayNotes.size(); i++) {
			// 位置
			latLng = new LatLng(todayNotes.get(i).getLocationx() + 0.00337,
					todayNotes.get(i).getLocationy() + 0.012);
			// 图标
			overlayOptions = new MarkerOptions().position(latLng).icon(bitmap)
					.zIndex(5);
			marker = (Marker) (mBaiduMap.addOverlay(overlayOptions));
			Bundle bundle = new Bundle();
			bundle.putSerializable("note", todayNotes.get(i));
			marker.setExtraInfo(bundle);
		}

		//MapStatusUpdate s = MapStatusUpdateFactory.newLatLng(latLng);
		//mBaiduMap.animateMapStatus(s);
		setMarkerClick();

	}

	// 添加marker点击时间
	private void setMarkerClick() {
		mBaiduMap.setOnMarkerClickListener(new OnMarkerClickListener() {

			@Override
			public boolean onMarkerClick(final Marker marker) {

				// 获得marker中的数据
				final Note mNote = (Note) marker.getExtraInfo().get("note");
				final int mPosition = getPositionByNote(mNote);
				InfoWindow mInfoWindow;

				final LatLng ll = marker.getPosition();

				Point p = mBaiduMap.getProjection().toScreenLocation(ll);
				p.y -= 40;
				LatLng llInfo = mBaiduMap.getProjection().fromScreenLocation(p);
				// 为弹出的InfoWindow添加点击事件
				OnInfoWindowClickListener listener = new OnInfoWindowClickListener() {

					@Override
					public void onInfoWindowClick() {
						Intent intent = new Intent(MapActivity.this,
								NoteDetailActivity.class);
						intent.setClass(MapActivity.this,
								NoteDetailActivity.class);
						intent.putExtra("notes", (Serializable) notes);
						intent.putExtra("noteDetail", (Serializable) mNote);
						intent.putExtra("notePosition", mPosition);
						startActivity(intent);
					}
				};
				// 显示InfoWindow
				View popup = LayoutInflater.from(getApplicationContext())
						.inflate(R.layout.pop, null);
				TextView title = (TextView) popup.findViewById(R.id.poiName);
			
				TextView dairy = (TextView) popup.findViewById(R.id.poiDiary);
				ImageView img = (ImageView) popup.findViewById(R.id.poiImage);

				title.setText(mNote.getTitle());
				
				dairy.setText(mNote.getTime());
			//加载图片
				Bitmap bmp = ((BitmapDrawable)img.getDrawable()).getBitmap();
				FileService service =   new FileService(getApplicationContext());
				img.setImageBitmap(service.getRoundedCornerBitmap(bmp, 1));

				if (mNote.getPictures().equals("") == false) {

					String[] imagePath = new String[4];
					for (int j = 0; j < 4; j++) {
						imagePath = null;
					}
					imagePath = mNote.getPictures().split(";");

					service = new FileService(
							getApplicationContext());
					String cfsImagePath = service.getCfsImagePath(
							"/easyTravel/savefile/tempImages/", imagePath[0]);

					File file = new File(cfsImagePath);
					if (file.exists()) {
						Bitmap bitmap = BitmapFactory.decodeFile(cfsImagePath);
						service = new FileService(
								getApplicationContext());
						bitmap = service.getRoundedCornerBitmap(bitmap, 1);
						img.setImageBitmap(bitmap);
					}
				}

				BitmapDescriptor bd = BitmapDescriptorFactory.fromView(popup);
				mInfoWindow = new InfoWindow(bd, llInfo, 0, listener);
				mBaiduMap.showInfoWindow(mInfoWindow);

				

				return true;
			}
		});
	}

	// 得到某个Note的Position
	protected int getPositionByNote(Note mNote) {
		// TODO Auto-generated method stub
		db = dbInit();
		ModelDaoImp mdi = new ModelDaoImp(db);
		notes = mdi.getAllNotes();
		
		for (int i=0; i<notes.size();i++){
			if (mNote.getNoteId()==notes.get(i).getNoteId()) {
				return i;
			}
		}
		return -1;
	}

	// 选择时间区间点击事件
	public void onChooseClick() {
		//tripNameClicked = false;

		View view = LayoutInflater.from(MapActivity.this).inflate(R.layout.choose_time, null);
		final PopupWindow popwinChooseTime = new PopupWindow(view, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		
		final EditText start= (EditText) view.findViewById(R.id.start);
		final EditText end = (EditText) view.findViewById(R.id.end);
		final ImageButton btnSelectStartTime = (ImageButton) view.findViewById(R.id.ibtn_selectStartTime);
		final ImageButton btnSelectEndTime = (ImageButton) view.findViewById(R.id.ibtn_selectEndTime);
		final Button btnSelectTimeCancel = (Button) view.findViewById(R.id.btn_selectTimeCancel);
		final Button btnSelectTimeOK = (Button) view.findViewById(R.id.btn_selectTimeOK);
		
		
		btnSelectStartTime.setOnClickListener(new OnClickListener() {
			@SuppressLint("SimpleDateFormat")
			@Override
			public void onClick(View v) {

				//获取当前的年月日
				Calendar calendar = Calendar.getInstance();
				
				DatePickerDialog dpdlg = new DatePickerDialog(MapActivity.this,
						new OnDateSetListener() {
							@Override
							public void onDateSet(DatePicker view, int year, int monthOfYear,
									int dayOfMonth) {
								
								start.setText(new StringBuilder().append(year).append(
										"-"+((monthOfYear+1) < 10 ? "0"+(monthOfYear+1) : (monthOfYear+1))).append(
										"-"+((dayOfMonth < 10) ? "0"+ dayOfMonth : dayOfMonth)));
								
							}
						}, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
				
				//结束日期不为空时，将结束日期设定为开始日期选择按钮的最大值；否则就把当天设定为最大值
				if (end.getText().toString().equals("") == false) {		

					String endTime = end.getText().toString().concat(" 00:00:00");
					Format f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Long endLong = null;
					try {
						endLong = ((Date)f.parseObject(endTime)).getTime();
					} catch (ParseException e) {
						e.printStackTrace();
					}
					dpdlg.getDatePicker().setMaxDate(endLong);
				}
				else {
					dpdlg.getDatePicker().setMaxDate(calendar.getTimeInMillis());
				}
				dpdlg.show();
			}
		});
		btnSelectEndTime.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				//获取当前的年月日
				Calendar calendar = Calendar.getInstance();
				
				DatePickerDialog dpdlg = new DatePickerDialog(MapActivity.this,
						new OnDateSetListener() {
							@Override
							public void onDateSet(DatePicker view, int year, int monthOfYear,
									int dayOfMonth) {
								
								end.setText(new StringBuilder().append(year).append(
										"-"+((monthOfYear+1) < 10 ? "0"+(monthOfYear+1) : (monthOfYear+1))).append(
										"-"+((dayOfMonth < 10) ? "0"+ dayOfMonth : dayOfMonth)));
							}
						}, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
				
				//开始日期不为空时，将开始日期设定为结束日期选择按钮的最小值
				if (start.getText().toString().equals("") == false) {		

					String startTime = start.getText().toString().concat(" 00:00:00");
					Format f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Long startLong = null;
					try {
						startLong = ((Date)f.parseObject(startTime)).getTime();
					} catch (ParseException e) {
						e.printStackTrace();
					}
					dpdlg.getDatePicker().setMinDate(startLong);
				}
				dpdlg.getDatePicker().setMaxDate(calendar.getTimeInMillis());
				dpdlg.show();
				
			}
		});
		
		btnSelectTimeCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				popwinChooseTime.dismiss();
			}
		});
		btnSelectTimeOK.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				popwinChooseTime.dismiss();
				
				String startTime = start.getText().toString();
				String endTime = end.getText().toString();

				if (startTime.equals("")) {
					Toast.makeText(MapActivity.this,
							getString(R.string.input_the_start),
							Toast.LENGTH_LONG).show();
				} else {

					try {
						initAll(startTime, endTime);
						initRoute(startTime, endTime);
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
				
			}
		});
		
		popwinChooseTime.setFocusable(true);
		popwinChooseTime.setBackgroundDrawable(getResources().getDrawable(R.drawable.popwin_bg));
		popwinChooseTime.setAnimationStyle(R.style.popwin_anim_style);
		popwinChooseTime.showAtLocation(rlMap, Gravity.CENTER, 0, 0);

		//弹出框时父窗口颜色变暗
		WindowManager.LayoutParams lp = getWindow().getAttributes();  
		lp.alpha = 0.7f;  
		getWindow().setAttributes(lp);
		//窗口消失时父窗口颜色恢复正常
		popwinChooseTime.setOnDismissListener(new OnDismissListener() {
		    @Override  
		    public void onDismiss() {  
		        WindowManager.LayoutParams lp = getWindow().getAttributes();  
		        lp.alpha = 1f;  
		        getWindow().setAttributes(lp); 
		    }  
		});

	}

	// 按时间搜索路径
	public List<LatLng> getPoints(String startTime, String endTime)
			throws ParseException {

		List<LatLng> list = new ArrayList<LatLng>();
		db = dbInit();
		String start = startTime.concat(" 00:00:00");
		String end = endTime.concat(" 23:59:59");
		Format f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			Date sDate = (Date) f.parseObject(start);
			Date eDate = (Date) f.parseObject(end);
			Long startLong = sDate.getTime();
			Long endLong = eDate.getTime();

			String sql = "select * from route where id between " + startLong
					+ " and " + endLong;
			Cursor cursor = db.rawQuery(sql, null);

			while (cursor.moveToNext()) {

				double latitude = cursor.getDouble(cursor
						.getColumnIndex("latitude")) + 0.00337;
				double longitude = cursor.getDouble(cursor
						.getColumnIndex("longtitude")) + 0.012;
				LatLng point = new LatLng(latitude, longitude);
				list.add(point);
			}

			cursor.close();
		} catch (ParseException e) {
			// TODO Auto-generated catch block F
			e.printStackTrace();
		}

		db.close();
		return list;
	}

	// 按时间段搜索note
	protected List<Note> searchNote(String startTime, String endTime) {
		List<Note> list = new ArrayList<Note>();
		db = dbInit();
		String start = startTime.concat(" 00:00:00");
		String end = endTime.concat(" 23:59:59");
		Format f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			Date sDate = (Date) f.parseObject(start);
			Date eDate = (Date) f.parseObject(end);
			Long startLong = sDate.getTime();
			Long endLong = eDate.getTime();

			String sql = "select * from note where noteId between " + startLong
					+ " and " + endLong;
			Cursor cursor = db.rawQuery(sql, null);

			while (cursor.moveToNext()) {

				long noteId = cursor.getLong(cursor.getColumnIndex("noteId"));
				String time = cursor.getString(cursor.getColumnIndex("time"));
				int permission = cursor.getInt(cursor
						.getColumnIndex("permission"));
				int weather = cursor.getInt(cursor.getColumnIndex("weather"));
				String text = cursor.getString(cursor.getColumnIndex("text"));
				String title = cursor.getString(cursor.getColumnIndex("title"));
				String pictures = cursor.getString(cursor
						.getColumnIndex("pictures"));
				String voice = cursor.getString(cursor.getColumnIndex("voice"));
				double locationx = cursor.getDouble(cursor
						.getColumnIndex("locationx"));
				double locationy = cursor.getDouble(cursor
						.getColumnIndex("locationy"));
				String video = cursor.getString(cursor.getColumnIndex("video"));
				long lastChangeTime = cursor.getLong(cursor
						.getColumnIndex("lastChangeTime"));
				int operation = cursor.getInt(cursor
						.getColumnIndex("operation"));
				list.add(new Note(noteId, time, permission, weather, text,
						title, pictures, voice, locationx, locationy, video,
						lastChangeTime, operation));
			}

			cursor.close();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		db.close();

		return list;

	}

	// 加载时间段内所有动态
	private void initAll(String startTime, String endTime)
			throws ParseException {

		List<Note> list = searchNote(startTime, endTime);
		mBaiduMap.clear();
		LatLng latLng = null;
		OverlayOptions overlayOptions;
		bitmap = BitmapDescriptorFactory.fromResource(R.drawable.location1);
		Marker marker;
		for (int i = 0; i < list.size(); i++) {
			// 位置
			latLng = new LatLng(list.get(i).getLocationx() + 0.00337, list.get(
					i).getLocationy() + 0.012);
			// 图标
			overlayOptions = new MarkerOptions().position(latLng).icon(bitmap)
					.zIndex(5);
			marker = (Marker) (mBaiduMap.addOverlay(overlayOptions));
			Bundle bundle = new Bundle();
			bundle.putSerializable("note", list.get(i));
			marker.setExtraInfo(bundle);
		}
//		MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(latLng);
//		mBaiduMap.animateMapStatus(u);
		setMarkerClick();

	}

	// 加载时间段内所有路线
	private void initRoute(String startTime, String endTime)
			throws ParseException {
		List<LatLng> pointList = getPoints(startTime, endTime);

		if (pointList.size() > 2) {
			OverlayOptions polylineOption = new PolylineOptions()
					.points(pointList).width(6).color(0xAAFF6600);
        //  mBaiduMap.clear();       
			// 在地图上添加多边形Option，用于显示
			mBaiduMap.addOverlay(polylineOption);
		} else {
			return;
		}
	}

	// 加载当天的路线
	private void initRoute(final Note note) throws ParseException {

		List<LatLng> pointList = getPoints();

		if (pointList.size() > 2) {
			OverlayOptions polylineOption = new PolylineOptions()
					.points(pointList).width(6).color(0xAAFF6600);

			// 在地图上添加多边形Option，用于显示
			mBaiduMap.addOverlay(polylineOption);
		} else {
			return;
		}

	}

	// 加载该记事
	private void initNote(final Note note) {

		LatLng latLng = new LatLng(note.getLocationx() + 0.00337,
				note.getLocationy() + 0.012);
		MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latLng);
		mBaiduMap.animateMapStatus(msu);
		// 构建MarkerOption，用于在地图上添加Marker
		BitmapDescriptor bitmap = BitmapDescriptorFactory
				.fromResource(R.drawable.location1);
		OverlayOptions option = new MarkerOptions().position(latLng).icon(
				bitmap);
		// 在地图上添加Marker，并显示
		mBaiduMap.addOverlay(option);
		// 为marker设置监听事件
		mBaiduMap.setOnMarkerClickListener(new OnMarkerClickListener() {

			private boolean isShowInfoWindow;

			@SuppressLint("InflateParams")
			@Override
			public boolean onMarkerClick(Marker marker) {
				final LatLng ll = marker.getPosition();

				if (isShowInfoWindow) {
					mBaiduMap.hideInfoWindow();
					isShowInfoWindow = false;
				} else {

					/*
					 * getProjection()获取地图投影坐标转换器, 当地图初始化完成之前返回 null，在
					 * OnMapLoadedCallback.onMapLoaded() 之后才能正常;
					 * Projection接口用于屏幕像素点坐标系统和地球表面经纬度点坐标系统之间的变换
					 */

					/*
					 * public Point toScreenLocation(LatLng location)
					 * 将地理坐标转换成屏幕坐标 参数: location - 地理坐标 如果传入 null 则返回null 返回:
					 * 屏幕坐标
					 */
					Point p = mBaiduMap.getProjection().toScreenLocation(ll);
					// p.y -= 40;
					LatLng llInfo = mBaiduMap.getProjection()
							.fromScreenLocation(p);
					OnInfoWindowClickListener listener = new OnInfoWindowClickListener() {

						@Override
						public void onInfoWindowClick() {

							Intent intent = new Intent(MapActivity.this,
									NoteDetailActivity.class);
							intent.setClass(MapActivity.this,
									NoteDetailActivity.class);
							intent.putExtra("notes", (Serializable) notes);
							intent.putExtra("noteDetail", (Serializable) note);
							intent.putExtra("notePosition", position);
							startActivity(intent);
						}
					};
					
					View popup = LayoutInflater.from(getApplicationContext())
							.inflate(R.layout.pop, null);
					TextView title = (TextView) popup
							.findViewById(R.id.poiName);
					
					TextView dairy = (TextView) popup
							.findViewById(R.id.poiDiary);
					ImageView img = (ImageView) popup
							.findViewById(R.id.poiImage);

					title.setText(note.getTitle());
					
					dairy.setText(note.getTime());
					
					Bitmap bmp = ((BitmapDrawable)img.getDrawable()).getBitmap();
					FileService service =   new FileService(getApplicationContext());
					img.setImageBitmap(service.getRoundedCornerBitmap(bmp, 1));

					if (note.getPictures().equals("") == false) {

						String[] imagePath = new String[4];
						for (int j = 0; j < 4; j++) {
							imagePath = null;
						}
						imagePath = note.getPictures().split(";");

						service = new FileService(
								getApplicationContext());
						String cfsImagePath = service.getCfsImagePath(
								"/easyTravel/savefile/tempImages/",
								imagePath[0]);

						File file = new File(cfsImagePath);
						if (file.exists()) {
							Bitmap bitmap = BitmapFactory
									.decodeFile(cfsImagePath);
							service = new FileService(
									getApplicationContext());
							bitmap = service.getRoundedCornerBitmap(bitmap, 1);
							img.setImageBitmap(bitmap);
						}
					}

					BitmapDescriptor bd = BitmapDescriptorFactory
							.fromView(popup);
					InfoWindow mInfoWindow = new InfoWindow(bd, llInfo, 0,
							listener);
					mBaiduMap.showInfoWindow(mInfoWindow);

					
					isShowInfoWindow = true;
				}
				return false;
			}
		});
	}

	@Override
	public void onMapClick(LatLng arg0) {
		// TODO Auto-generated method stub
		mBaiduMap.hideInfoWindow();
	}

	@Override
	public boolean onMapPoiClick(MapPoi arg0) {
		// TODO Auto-generated method stub
		return false;
	}
	//连接数据库
	public SQLiteDatabase dbInit() {

		
		DBHelper helper = new DBHelper(MapActivity.this, "default.db", null, 1);
		SQLiteDatabase defaultdb = helper.getReadableDatabase();

		
		ModelDaoImp mdi = new ModelDaoImp(defaultdb);
		String userId = mdi.getUserConfiguration().getLoginUser();
		if (userId.equals("default")) {
			return defaultdb;
		} else {
			DBHelper helper1 = new DBHelper(MapActivity.this, userId + ".db",
					null, 1);
			SQLiteDatabase userdb = helper1.getReadableDatabase();
			return userdb;
		}
	}

}