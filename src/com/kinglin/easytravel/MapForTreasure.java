package com.kinglin.easytravel;



import java.io.Serializable;
import java.util.List;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
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
import com.baidu.mapapi.model.LatLng;
import com.kinglin.model.Note;
import com.kinglin.model.Treasure;

@SuppressLint("InflateParams")
public class MapForTreasure extends Activity implements OnMapClickListener {

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
	Switch switch1;
	Button marker;
	Long noteID;
	Note note;
	List<Treasure> treasures;
	List<Marker> markers;
	private int flag;
	SQLiteDatabase db;
	private BitmapDescriptor bitmap;
	

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.context = this;
		SDKInitializer.initialize(getApplicationContext());

		setContentView(R.layout.activity_map);
       //��ʼ����ͼ
		initView();
		// ��ʼ��λ��
		initLocation();
		
		treasures = (List<Treasure>) getIntent().getSerializableExtra("Treasure");
       initTreasure(treasures);

		marker = (Button) findViewById(R.id.marker);
		
		marker.setVisibility(8);
		locationButton = (ImageButton) findViewById(R.id.map_my_location);
		locationButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				centerToMyLocation();

			}
		});

	}
//���ر��صĵ�
	private void initTreasure(List<Treasure> treasures2) {
		// TODO Auto-generated method stub
		
		LatLng latLng = null;
		OverlayOptions overlayOptions;
		bitmap = BitmapDescriptorFactory.fromResource(R.drawable.ic_treasure);
		Marker marker;
		for (int i = 0; i < treasures2.size(); i++) {
			// λ��
			latLng = new LatLng(treasures2.get(i).getLatitude() ,
					treasures2.get(i).getLongitude());
			// ͼ��
			overlayOptions = new MarkerOptions().position(latLng).icon(bitmap)
					.zIndex(5);
			marker = (Marker) (mBaiduMap.addOverlay(overlayOptions));
			Bundle bundle = new Bundle();
			bundle.putSerializable("note", (Serializable) treasures2.get(i));
			marker.setExtraInfo(bundle);
		}

		MapStatusUpdate s = MapStatusUpdateFactory.newLatLng(latLng);
		mBaiduMap.animateMapStatus(s);
		setMarkerClick();
	}
	// ���marker���ʱ��
		private void setMarkerClick() {
			mBaiduMap.setOnMarkerClickListener(new OnMarkerClickListener() {

				@Override
				public boolean onMarkerClick(final Marker marker) {

					// ���marker�е�����
					final Treasure mTreasure = (Treasure) marker.getExtraInfo().get("note");
					
					
					
					
					InfoWindow mInfoWindow;

					final LatLng ll = marker.getPosition();

					Point p = mBaiduMap.getProjection().toScreenLocation(ll);
					p.y -= 40;
					LatLng llInfo = mBaiduMap.getProjection().fromScreenLocation(p);
					// Ϊ������InfoWindow��ӵ���¼�
					OnInfoWindowClickListener listener = new OnInfoWindowClickListener() {

						@Override
						public void onInfoWindowClick() {
							
						}
					};
					// ��ʾInfoWindow
					View popup = LayoutInflater.from(getApplicationContext())
							.inflate(R.layout.treasure, null);
					
					// TextView content = (TextView)
					// popup.findViewById(R.id.poiTime);
					TextView dairy = (TextView) popup.findViewById(R.id.poiName);
					
					
					dairy.setText(mTreasure.getContent());

					

					BitmapDescriptor bd = BitmapDescriptorFactory.fromView(popup);
					mInfoWindow = new InfoWindow(bd, llInfo, 0, listener);
					mBaiduMap.showInfoWindow(mInfoWindow);

					// InfoWindow �ڵ�ͼ����ʾһ����Ϣ���ڣ���������һ��View��Ϊ�ô��ڵ����ݣ�Ҳ��������һ��

					return true;
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
//��ͼ�����Ƶ���ǰ��
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
//����λ����Ϣ
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

		// ��activityִ��onDestroyʱִ��mMapView.onDestroy()��ʵ�ֵ�ͼ�������ڹ���
		mMapView.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// ��activityִ��onResumeʱִ��mMapView. onResume ()��ʵ�ֵ�ͼ�������ڹ���
		mMapView.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		// ��activityִ��onPauseʱִ��mMapView. onPause ()��ʵ�ֵ�ͼ�������ڹ���
		mMapView.onPause();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();

		mLocationClient.stop();

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

	
	
}
