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
	
	//int editPermission = 0;		//��¼ѡ���permission��Ĭ��Ϊ0��������
	int editWeather = 1;		//��¼ѡ���������Ĭ��Ϊ1������
	String photoFilePath = null;
	int pictureNum = 0;			//��¼ͼƬ����������Ҫ�ģ�����ֻͨ��imgs��size()���ж���������Ϊ��ɾ��ʱ��û����imgs��ȥ����ͼƬ���ڵ�����水ť�������ɾ��
	int videoNum = 0;
	String picturesPath = "", videoPath = "";
	int videoDeleteOrNot = 0;		//���ͼƬ�Ƿ�ɾ��
	int playOrNot = 0;			//�����Ƶ�Ƿ񲥷�
	
	List<MyImg> imgs;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_edit_note);
		
		//�Կؼ���ʼ���������·�ʵ��
		initContext();
		
		db = dbInit();
		
		//��ȡҪ�༭�ļ���
		editNote = (Note)getIntent().getSerializableExtra("editNote");
		editWeather = editNote.getWeather();
		videoPath = editNote.getVideo();
		
		//�����еļ���������ʾ����
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


	//��ʼ�����пռ������
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
		
		/*//����spinner���
		//��������Դ
		String[] statusItems = getResources().getStringArray(R.array.status_spinner);
		//����Adapter��������Դ
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, statusItems);
		adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
		//��Adapter���ؼ�
		spinnerEditPermission.setAdapter(adapter);*/
		
		initSatelliteMenu();
		
		imgs = new ArrayList<MyImg>();
	}
	
	//���������ȡ��ǰ�û������ݿ�
	public SQLiteDatabase dbInit() {
		
		//��������default���ݿ⣬��һ��ʹ�û��ʼ������ʵ���Ӧ�ı�
		DBHelper helper=new DBHelper(EditNoteActivity.this, "default.db", null, 1);
	    SQLiteDatabase defaultdb=helper.getWritableDatabase();
        
        //��ѯdefault���ݿ���Configuration���loginUser,������Ӧ��db
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
	
	//��ʾ���м��µ�������Ϣ
	public void showNote(Note note){
		
		etEditTitle.setText(note.getTitle());
		//spinnerEditPermission.setSelection(note.getPermission());
		ibtnEditWeather.setImageResource(showWeather(note.getWeather()));
		etEditContent.setText(note.getText());
		
		String[] imagePath = new String[4];
		for (int j = 0; j < 4; j++) {
			imagePath[j] = "";
		}
		
		//�����ݿ��ŵ�ͼƬ·���ԷֺŲ𿪳�һ�����飬��Ŷ���ͼƬ·��
		imagePath = editNote.getPictures().split(";");
		//��ÿ��ͼƬ��ʾ����
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
		//��ʾ��Ƶ
		if (videoPath.equals("") == false) {
			addNewVideo(videoPath);
			videoNum ++;
		}
		
	}
	
	/*//ѡ��permission����Ӧ�¼�
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
	
	//���ѡ��������ť����Ӧ�¼�
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
	
	//ѡ��ĳ��������Ӧ�¼�
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
	
	//���popupwindow�ⲿȡ�� 
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
  
    //�жϴ���λ���Ƿ���popuwindow�ⲿ  
    private boolean isOutOfBounds(MotionEvent event) {  
        final int x=(int) event.getX();  
        final int y=(int) event.getY();  
        int slop = ViewConfiguration.get(EditNoteActivity.this).getScaledWindowTouchSlop();  
        View decorView = popViewEdit.getContentView();  
        return (x<-slop)||(y<-slop)  
        ||(x>(decorView.getWidth()+slop))  
        ||(y>(decorView.getHeight()+slop));  
    }  
    
	
	//��ʼ��SatelliteMenu
	private void initSatelliteMenu(){
		//����SatelliteMenu���
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
						selectPicPopupWindow(1);		//1��ʾ���������Ƶ
					}
					break;
				case 2:
					//etContent.setText("������item���ǣ�λ��");
					break;
				case 3:
					//etContent.setText("������item���ǣ�����");
					break;
				case 4:
					if (pictureNum == 4) {		//���ѡ��4��ͼƬ
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
	
	//����ѡ����Ƭ�����գ�ѡ����Ƶ��¼��Ƶ���Ĳ˵���idΪ0ʱ��ʾͼƬ������idΪ1ʱ��ʾ��Ƶ����
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
	
	//�������ѡ��ͼƬ
	private class SelectFromAlbumClickListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent(getApplicationContext(), ShowImageGroupActivity.class);
			intent.putExtra("num", 4 - pictureNum);
			startActivityForResult(intent, 1);
			popSelectPic.dismiss();
		}
	}
    
	//������Ƭ
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
    
    //����Ƶ����ѡ����Ƶ
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
    
    //¼����Ƶ
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
    
    //�����Ӵ��ڹر�ʱ������������
	@SuppressLint({ "ShowToast", "NewApi" }) 
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);  
		
        if (requestCode == 1) {//�������ѡ��ͼƬ 
        	
        	if (data != null) {     

            	List<String> pathList = null;
	            Bundle bundle = data.getExtras(); 
	            
	            if (bundle != null) {
	            	pathList = bundle.getStringArrayList("pathList"); // �õ��Ӵ��ڵĻش�����
	            	Toast.makeText(getApplicationContext(), "ѡ�� " + pathList.size() + " ��ͼƬ", Toast.LENGTH_LONG).show();
	            }
	            
	            llayoutEditNewImage.removeAllViews();
	            int num = pictureNum + pathList.size() - 1;		//��¼ͼƬ����-1
	            for (int i = 0; i < imgs.size(); i++) {			//��ʾ֮ǰ����ͼƬ
					if (imgs.get(i).getDeleteOrNot() == 0) {		//��ͼƬδ��ɾ��ʱ
						if (imgs.get(i).getSaveOrNot() == 0) {		//����ͼƬ������Ҫ������ʱ���ѱ������
							addNewImage(imgs.get(i).getImgPath(), num, 1, 0);
						}
						else if (imgs.get(i).getSaveOrNot() == 1) {		//����ͼƬ��Ҫ������ʱ
							addNewImage(imgs.get(i).getImgPath(), num, 1, 1);
						}
					}
				}
	            for (int i = 0; i < pathList.size(); i++) {		//��ʾ��ѡ�е�ͼƬ
	            	
	            	addNewImage(pathList.get(i),num, 0, 1);
	     		    pictureNum ++;
				}
        	}
        }
        if (requestCode == 2 && resultCode == RESULT_OK) {//����ͼƬ��ȡ 
        	
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
        if (requestCode == 3 && resultCode == RESULT_OK) {//�ӱ��ػ�ȡ��Ƶ
        	
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
        if (requestCode == 4 && resultCode == RESULT_OK) {//¼����Ƶ
        	
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
	
	//��̬���һ����ͼƬ����һ������ʱͼƬ·�����ڶ��������ǵ�ǰ����Ҫ��ʾ��ͼƬ����-1,����������ʱ�Ƿ���Ҫ��ӵ�imgs�У���4���������Ƿ���Ҫ����ͼƬ�ٱ��浽����
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
		
		//���֮ǰδ��ӵ�list�У������
		if (addToListOrNot == 0) {
			imgs.add(img);
		}
		
		//ɾ������
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

				pictureNum --;		//ʵ��ͼƬ����1
				
				llayoutEditNewImage.removeAllViews();
				for (int i = 0; i < imgs.size(); i++) {
					if (imgs.get(i).getImgPath().equals(img.getImgPath())) {
						imgs.get(i).setDeleteOrNot(1);	//��ʾ��ͼƬ��ɾ��
					}
					else {
						if (imgs.get(i).getDeleteOrNot() == 0) {	//��û��ɾ����ͼƬ��ʾ����
							if (imgs.get(i).getSaveOrNot() == 0) {
								addNewImage(imgs.get(i).getImgPath(), pictureNum-1, 1, 0);
							}
							else if (imgs.get(i).getSaveOrNot() == 1) {
								addNewImage(imgs.get(i).getImgPath(), pictureNum-1, 1, 1);
							}
						}
					}
				}
				
				//���ͼƬ��ɾ���˾Ͷ�̬���һ�ſ��Ե�����ͼƬ��ͼƬ��ť
				if (pictureNum == 0) {		
					ImageView imageView = new ImageView(getApplicationContext());
					imageView.setImageResource(R.drawable.plus);
					
					imageView.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							WindowManager.LayoutParams lp = getWindow().getAttributes();  
					        lp.alpha = 0.7f;  
					        getWindow().setAttributes(lp);
							selectPicPopupWindow(0);		//0��ʾ�������ͼƬ
						}
					});
					
					LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(
							LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
					lParams.setMargins(0, 15, 0, 0);
					llayoutEditNewImage.addView(imageView, lParams);
				}
				
			}
		});
		
		//����ͼƬ��������ÿ��ͼƬ����ʾ��С
		LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams((width-80-num*15)/(num+1), (width-80-num*15)/(num+1));
		lParams.setMargins(0, 30, 15, 0);
		llayoutEditNewImage.addView(fLayoutImg,lParams);
		
	}

	//��̬���һ������Ƶ
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
		
		//ɾ������
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
	
	//������水ť����Ӧ�¼�
	private class SaveClickListener implements OnClickListener{
		@Override
		public void onClick(View v) {
			
			//�����Ǳ���İ�ť��Ӧ,�ֽ�ҳ������Ϣ��ȡ��editnote��
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
				
				//ͼƬ����
				//��ɾ����ͼƬ��imgs���Ƴ�,���ڱ����ļ�����ɾ��
				for (int i = 0; i < imgs.size(); i++) {
					
					if (imgs.get(i).getDeleteOrNot() == 1) {
						//��ô�ɾ����ͼƬ��·������ѹ��ͼƬ��·��
						File file = new File(imgs.get(i).getImgPath());
						FileService fileService = new FileService(getApplicationContext());
						String cfsImagePath = fileService.getCfsImagePath("/easyTravel/savefile/tempImages/", imgs.get(i).getImgPath());
						File cfsFile = new File(cfsImagePath);
						//ɾ��ԭͼƬ��ѹ��ͼƬ
						if (file.exists() && cfsFile.exists()) {
							file.delete();
							cfsFile.delete();
						}
						imgs.remove(i);
						i --;		//��ɾ��һ��imgʱ��i���Լӣ������©���˵�ǰ��һ��
					}
				}
				
				
				FileService service=new FileService(getApplicationContext());
			    String fileName = "/easyTravel/savefile/images/";
			    service.createSDCardDir(fileName);
			    
			    String cfsFileName = "/easyTravel/savefile/tempImages/";
			    service.createSDCardDir(cfsFileName);
			    
				for (int i = 0; i < imgs.size(); i++) {
				    
				    try {
				    	//��ԭͼƬ������images�ļ����£����ҽ�ͼƬ·���������ݿ�
				    	if (imgs.get(i).getSaveOrNot() == 1) {		//ֻ����Ҫ����ĲŻᱣ��
				    		//ֻ��֮ǰû�б������ͼƬ��Ҫ�ڱ����ļ����б���
					    	imgs.get(i).imgPath = service.saveRealImg(fileName, imgs.get(i).imgPath);
						}
						picturesPath = picturesPath + imgs.get(i).imgPath +";";
						
						//��ѹ��ͼƬ������tempImages�ļ�����
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
				
				//��Ƶ����
			    String videoFileName = "/easyTravel/savefile/videos/";
			    service.createSDCardDir(videoFileName);
			    
			    //�ж�֮ǰ��Ƶ�Ƿ�ִ�й�ɾ������
			    if (videoDeleteOrNot == 1) {

					File file = new File(videoPath);
					if (file.exists()) {
						file.delete();
					}
					videoPath = "";
				}
			    //�ж����ڵ���Ƶ·���Ƿ�Ϊ�գ����ǣ���ֱ�Ӹ�ֵ������Ҫ�����ڵ���Ƶ�����ض�·����
				if (videoPath.equals("")) {	
					editNote.setVideo(videoPath);
				}
				else {
					//�ж����ڵ���Ƶ·�������ݿ����Ƿ���ͬ������ͬ���򲻱���
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
	
	//���ȡ����ť����Ӧ�¼�
	private class CancelClickListener implements OnClickListener{
		@Override
		public void onClick(View v) {
			
			EditNoteActivity.this.finish();
			overridePendingTransition(0, R.anim.center_out);
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
	
	//�̳�ImageView�࣬����ͼƬ·������
	class MyImg extends ImageView{

		String imgPath;				//��¼��ͼƬ·��
		int deleteOrNot;			//��¼��ͼƬ�Ƿ�ɾ����0��ʾδɾ����1��ʾɾ��
		int saveOrNot;				//��¼��ͼƬ�Ƿ���Ҫ�ڱ����ļ����б��棬�����֮ǰ�еģ��Ͳ���Ҫ�ٱ���һ�Σ�0��ʾ����Ҫ���棬1��ʾ��Ҫ����

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
	
	//������ذ�ť���¼���Ӧ
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) { //���µ������BACK��ͬʱû���ظ�
			
			EditNoteActivity.this.finish();
			overridePendingTransition(0, R.anim.center_out);
		    return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	

}
