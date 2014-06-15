package com.example.horizonal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.example.horizonal.DragGridView.OnChanageListener;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class HorizonalActivity extends Activity {
	
	private DragGridView gridView;
	private static LayoutInflater inflater;
	private static List<String> dataSourceList = new ArrayList<String>();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_horizonal);
		
		inflater = (LayoutInflater)this.getApplication()
	                .getSystemService(this.LAYOUT_INFLATER_SERVICE);  
	   CustomHorizonalView customHorizonalView = (CustomHorizonalView)findViewById(R.id.horizonal);
	   gridView = (DragGridView)findViewById(R.id.gridview);  
	   customHorizonalView.setDragGridView(gridView);
	   gridView.setCustomHorizonalView(customHorizonalView);
	   customHorizonalView.setHandler(gridView.getHandler());
	   
	   
	   if (dataSourceList.size() > 0) {
		   
		  dataSourceList.removeAll(dataSourceList);
		   
	   }
	   
	   for (int i = 0; i < 6; i++) {
		   dataSourceList.add(i+"");
	   }
	   
	  final GridViewAdapter adapter = new HorizonalActivity.GridViewAdapter();  
	  
	  gridView.setOnChangeListener(new OnChanageListener() {
			
			@Override
			public void onChange(int from, int to) {
				Log.v("mandy", "onchange");
				String temp = dataSourceList.get(from);
				
				//这里的处理需要注意下
				if(from < to){
					for(int i=from; i<to; i++){
						Collections.swap(dataSourceList, i, i+1);
					}
				}else if(from > to){
					for(int i=from; i>to; i--){
						Collections.swap(dataSourceList, i, i-1);
					}
				}
				
				dataSourceList.set(to, temp);
				adapter.notifyDataSetChanged();
				
				//设置新到的item隐藏，不用调用notifyDataSetChanged来刷新界面，因为setItemHide方法里面调用了
//				mDragAdapter.setItemHide(to);
				
			}
		});
    
   
       gridView.setAdapter(adapter);  
//       int size = dataList.size();  
       DisplayMetrics dm = new DisplayMetrics();  
       getWindowManager().getDefaultDisplay().getMetrics(dm);  
       float density = dm.density;  
//       Log.v("mandy", "density is " + density);
       int allWidth = (int) (110 * 6 * density);  
       int itemWidth = (int) (100 * density);  
       LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(  
               allWidth, LinearLayout.LayoutParams.MATCH_PARENT);  
       gridView.setLayoutParams(params);  
       gridView.setColumnWidth(itemWidth);  
       gridView.setHorizontalSpacing(10);  
//       gridView.setStretchMode(GridView.NO_STRETCH);  
       gridView.setNumColumns(6);  
		
	}

	 public static  class GridViewAdapter extends BaseAdapter {  
	        
	        @Override  
	        public int getCount() {  
	            return dataSourceList.size();  
	        }  
	  
	        @Override  
	        public Object getItem(int position) {  
	            return position;  
	        }  
	  
	        @Override  
	        public long getItemId(int position) {  
	            return position;  
	        }  
	  
	        @Override  
	        public View getView(int position, View convertView, ViewGroup parent) {  
	            
//	        if (convertView == null) {
	            
	            convertView = inflater.inflate(R.layout.grid_item, null);  
//	        }
	           
	            ImageView imageView = (ImageView) convertView  
	                    .findViewById(R.id.imageview);  
	            imageView.setImageResource(R.drawable.ic_launcher);
	            TextView textView = (TextView) convertView  
	                    .findViewById(R.id.textView);
	            
	            textView.setText(dataSourceList.get(position));
//	            String str = dataList.get(position);  
//	            textView.setText(str);  
	            return convertView;  
	        }  
	    }
	
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_horizonal, menu);
		return true;
	}

}
