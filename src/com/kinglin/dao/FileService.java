package com.kinglin.dao;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.text.format.DateFormat;
import android.widget.Toast;

public class FileService {
	private Context context;
	public FileService(Context context) {
		this.context = context;
	}
	
	/*public void saveToSD(String fname,String fcontent)throws Exception{
		File file=new File(Environment.getExternalStorageDirectory(), fname);
		FileOutputStream outputStream=new FileOutputStream(file);
		outputStream.write(fcontent.getBytes());
		outputStream.close();
	}
    
	public void save(String fname,String fcontent) throws Exception{
		
		FileOutputStream outputStream=context.openFileOutput(fname, Context.MODE_PRIVATE);;
		outputStream.write(fcontent.getBytes());
		outputStream.close();
	}
	
	public String read(String filename) throws Exception{
		FileInputStream inStream=context.openFileInput(filename);
		ByteArrayOutputStream outStream=new ByteArrayOutputStream();
		byte[] buffer=new byte[1024];
		int len=0;
		while((len=inStream.read(buffer))!= -1) {
			outStream.write(buffer, 0, len);
		}
		byte[] data=outStream.toByteArray();
		return new String(data);	
	}*/
	
	//得到SD卡根路径
	public String getSDPath(){
		File SDdir=null;
		boolean sdCardExist=Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
		if (sdCardExist) {
			SDdir=Environment.getExternalStorageDirectory();
		}
		if (SDdir!=null) {
			return SDdir.toString();
		}
		else{
			return null;
		}
	}
	
	//得到文件名及其后缀名
	public String getNameString(String path){
        String b = path.substring(path.lastIndexOf("/") + 1, path.length());
		return b;
	}
	
	/*public String getFileName(String pathandname){  //得到文件名
        
        int start=pathandname.lastIndexOf("/");  
        int end=pathandname.lastIndexOf(".");  
        if(start!=-1 && end!=-1){  
            return pathandname.substring(start+1,end);    
        }else{  
            return null;  
        }  
          
    }*/  
	
	//得到文件后缀名
    public String getExtensionName(String filename) {   
        if ((filename != null) && (filename.length() > 0)) {    
            int dot = filename.lastIndexOf('.');    
            if ((dot >-1) && (dot < (filename.length() - 1))) {    
                return filename.substring(dot + 1);    
            }    
        }    
        return filename;    
    }
	
	//创建一个新的文件夹
	@SuppressLint("ShowToast")
	public void createSDCardDir(String name){
		if (getSDPath()==null) {
			Toast.makeText(this.context,"SD card not exit", 1).show();
		}
		else {
			if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
				
				File sdcardDir =Environment.getExternalStorageDirectory();
				
				String newPath=sdcardDir.getPath()+name;
                
                File imgFile = new File(newPath);
                if (!imgFile.exists()) {
                	
                	imgFile.mkdirs();
                	System.out.println("锟斤拷锟斤拷锟侥硷拷锟叫成癸拷锟斤拷目录锟斤拷"+newPath);
				}
			}
			else {
				System.out.println("锟斤拷锟斤拷锟侥硷拷锟斤拷失锟斤拷");
			}
		}
	}
	
	//将视频保存在本地指定文件夹中
	@SuppressWarnings("static-access")
	public String saveMyVideo(String newPath, String oldPath)throws IOException{
		 File sdcardDir =Environment.getExternalStorageDirectory();
		 String name = (String) new DateFormat().format("yyyyMMdd_HHmmss",Calendar.getInstance(Locale.CHINA));
		 newPath = sdcardDir.getPath() + newPath + name + getNameString(oldPath);
		 copyFile(oldPath,newPath);
		 return newPath;
	}
	
	//复制文件
    @SuppressWarnings("resource")
	public void copyFile(String oldPath, String newPath) {
	    try {   
	        int bytesum = 0;   
	        int byteread = 0;   
	        File oldfile = new File(oldPath);   
	        if (oldfile.exists()) { //当源文件存在时
	            InputStream inStream = new FileInputStream(oldPath); 
	            int length = inStream.available();
	            FileOutputStream fs = new FileOutputStream(newPath);   
	            byte[] buffer = new byte[length];  
	            while ( (byteread = inStream.read(buffer)) != -1) {   
	                bytesum += byteread; 
	                System.out.println(bytesum);   
	                fs.write(buffer, 0, byteread);   
	            }   
	            inStream.close();   
	        }   
	    }   
	    catch (Exception e) {   
	        System.out.println("锟斤拷锟狡碉拷锟斤拷锟侥硷拷锟斤拷锟斤拷锟斤拷锟斤拷");   
	        e.printStackTrace();   
	    }   
	  
	}  

    //
	public void saveMyImg(int percent, Bitmap bitmap, String newPath, String oldPath)throws IOException{
		 File sdcardDir = Environment.getExternalStorageDirectory();
         newPath = sdcardDir.getPath() + newPath + getNameString(oldPath);
		 File f = new File(newPath);
		 f.createNewFile();
		 FileOutputStream fOut=null;
		 try {
			 fOut=new FileOutputStream(f);
			 bitmap.compress(Bitmap.CompressFormat.JPEG, percent, fOut);
			 fOut.flush();
			 fOut.close();
		  } catch (FileNotFoundException e) {
			e.printStackTrace();
		  }
		   catch (IOException e)  
	        {  
	            e.printStackTrace();  
	        }  

	}
	
	//将图片保存在本地指定文件夹下
	@SuppressWarnings("static-access")
	public String saveRealImg(String newPath, String oldPath)throws IOException{
		File sdcardDir =Environment.getExternalStorageDirectory();
		String name = (String) new DateFormat().format("yyyyMMdd_HHmmss",Calendar.getInstance(Locale.CHINA));
		newPath = sdcardDir.getPath() + newPath + name + getNameString(oldPath);
		copyFile(oldPath,newPath);
	    return newPath;
	}
	
	//将原图片转换为压缩图片
	public Bitmap confessBitmap(Bitmap bitmap){
		
		Bitmap cfsBitmap;
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		if (width >= height+10) {
			
			int newWidth = 440;
			float scaleWidth = ((float) newWidth) / width;
			int newHeight=(int)(scaleWidth*height);
			cfsBitmap = Bitmap.createScaledBitmap(bitmap,newWidth , newHeight, false);
		}
		 else {
			int newWidth = 240;
			float scaleWidth = ((float) newWidth) / width;
			int newHeight=(int)(scaleWidth*height); 
			cfsBitmap = Bitmap.createScaledBitmap(bitmap,newWidth , newHeight, false);
		} 
		
		return cfsBitmap;
	}
	
	//根据图片路径得到其压缩图片的路径
	public String getCfsImagePath(String cfsImagePath, String oldPath){
		
		File sdcardDir = Environment.getExternalStorageDirectory();
		cfsImagePath = sdcardDir.getPath() + cfsImagePath + getNameString(oldPath);
		
		return cfsImagePath;
	}
	
	//将用照相机照的相片存放在cameraImages文件夹下，名字为当前日期.jpg
	public String getPhotoFilePath(){
		String fileName = "/easyTravel/savefile/cameraImages/";
		createSDCardDir(fileName);
	    new DateFormat();
	    
	    File sdcardDir = Environment.getExternalStorageDirectory();
	    String name = DateFormat.format("yyyyMMdd_hhmmss",Calendar.getInstance(Locale.CHINA)) + ".jpg"; 
	    fileName = sdcardDir + fileName + name;
	    return fileName;
	}
	
	/*private int getFileLen(String path) throws IOException {
	      File dF = new File(path); 
	      FileInputStream fis= new FileInputStream(dF);
	      int fileLen=fis.available();
	      return fileLen;
	}*/
	
	//将原图片转换成圆角图片
	public Bitmap getRoundedCornerBitmap(Bitmap bitmap, int shadow) 
	{
	    
	    Bitmap roundBitmap = Bitmap.createBitmap(bitmap.getWidth(), 
	         bitmap.getHeight(), Config.ARGB_8888); 
	     Canvas canvas = new Canvas(roundBitmap); 
	     int color = Color.parseColor("#FC6802"); 
	     
	     Paint paint = new Paint(); 
	     Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()); 
	     RectF rectF = new RectF(rect); 
	     float roundPx = 30;      //设置圆角弧度
	     
	     paint.setAntiAlias(true); 		//设置是否显示成锯齿状，true为圆滑，false为锯齿
	     canvas.drawARGB(0, 0, 0, 0);
	     
	     if (shadow == 1) {

		     BlurMaskFilter bf = new BlurMaskFilter(8,BlurMaskFilter.Blur.INNER);
		     paint.setColor(color);
		     paint.setMaskFilter(bf);
		}
	     
	     canvas.drawRoundRect(rectF, roundPx, roundPx, paint); 
	     paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN)); 
	     canvas.drawBitmap(bitmap, rect, rect, paint);
	     //canvas.drawBitmap(bitmap, 0, 0, paint);
	     
	     return roundBitmap;
	}
	
	//获得图片的缩略图
	public Bitmap getImageThumbnail(String imagePath, int width, int height) {  
		
		Bitmap bitmap = null;  
		BitmapFactory.Options options = new BitmapFactory.Options();  
		options.inJustDecodeBounds = true;  
		// 获取这个图片的宽和高，注意此处的bitmap为null  
		bitmap = BitmapFactory.decodeFile(imagePath, options);  
		options.inJustDecodeBounds = false; // 设为 false  
		// 计算缩放比  
		int h = options.outHeight;  
		int w = options.outWidth;  
		int beWidth = w / width;  
		int beHeight = h / height;  
		int be = 1;  
		if (beWidth < beHeight) {  
			be = beWidth;  
		} else {  
			be = beHeight;  
		}  
		if (be <= 0) {  
			be = 1;  
		}  
		options.inSampleSize = be;  
		// 重新读入图片，读取缩放后的bitmap，注意这次要把options.inJustDecodeBounds 设为 false  
		bitmap = BitmapFactory.decodeFile(imagePath, options);  
		// 利用ThumbnailUtils来创建缩略图，这里要指定要缩放哪个Bitmap对象  
		bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,  
				ThumbnailUtils.OPTIONS_RECYCLE_INPUT);  
		return bitmap;  
	}
	
	//获得视频的第一帧图片
	public Bitmap getVideoThumbnail(String videoPath, int kind) {
		
		Bitmap bitmap = null;
		//获得视频的第一帧图片
		MediaMetadataRetriever media = new MediaMetadataRetriever();
		media.setDataSource(videoPath);
		bitmap = media.getFrameAtTime();
		
		/*// 获取视频的缩略图  
		bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);
		bitmap = ThumbnailUtils.extractThumbnail(bitmap, 200, 200,  
				ThumbnailUtils.OPTIONS_RECYCLE_INPUT); */
		return bitmap;  
	}
	
}
