package com.kinglin.easytravel;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ListViewAdapter extends BaseAdapter {

	private Context context;
	private ArrayList<HashMap<String, Object>> alist;
	
	public static class ViewHolder {
        View line;
        TextView tvContent,time;
        ImageView imgContent,imgDot;
	}
	
	public ListViewAdapter(Context context, ArrayList<HashMap<String, Object>> alist) {
		this.context = context;
		this.alist = alist;
	}
	
	@Override
	public int getCount() {
		return alist.size();
	}

	@Override
	public Object getItem(int position) {
		return alist.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null; 
		if(convertView == null){
			
			holder = new ViewHolder();
			convertView = LayoutInflater.from(context).inflate(
	                 R.layout.item_show_note, null);
			holder.line = convertView.findViewById(R.id.v_line);
			holder.imgDot = (ImageView) convertView.findViewById(R.id.img);
			holder.time = (TextView) convertView.findViewById(R.id.tv_timeLineTime);
			holder.tvContent = (TextView) convertView.findViewById(R.id.tv_timeLineContent);
			holder.imgContent = (ImageView) convertView.findViewById(R.id.iv_timeLineContent);
			convertView.setTag(holder);
		}
		else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		holder.time.setText((String)alist.get(position).get("itemTime"));
		holder.tvContent.setText((String)alist.get(position).get("itemTextContent"));
		holder.imgContent.setImageBitmap((Bitmap)alist.get(position).get("itemPictures"));
		
		return convertView;
	}

}
