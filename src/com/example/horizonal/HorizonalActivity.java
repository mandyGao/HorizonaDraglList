package com.example.horizonal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.example.horizonal.CustomHorizonalView.OnChanageListener;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class HorizonalActivity extends Activity {

	private GridView gridView;
	private GridViewAdapter adapter;
	private static LayoutInflater inflater;
	private static List<DataItem> dataSourceList = new ArrayList<DataItem>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_horizonal);

		inflater = (LayoutInflater) this.getApplication().getSystemService(
				this.LAYOUT_INFLATER_SERVICE);
		CustomHorizonalView customHorizonalView = (CustomHorizonalView) findViewById(R.id.horizonal);
		gridView = (GridView) findViewById(R.id.gridview);
		customHorizonalView.setDragGridView(gridView);

		// gridView.setCustomHorizonalView(customHorizonalView);

		if (dataSourceList.size() > 0) {
			dataSourceList.removeAll(dataSourceList);
		}

		initData();

		 adapter = new HorizonalActivity.GridViewAdapter();

		customHorizonalView.setOnChangeListener(new OnChanageListener() {

			@Override
			public void onChange(int from, int to) {
				DataItem temp = dataSourceList.get(from);

				// 这里的处理需要注意下
//				if (from < to) {
//					for (int i = from; i < to; i++) {
						Collections.swap(dataSourceList, from, to);
//					}
//				} else if (from > to) {
//					for (int i = from; i > to; i--) {
//						Collections.swap(dataSourceList, i, i - 1);
//					}
//				}
				dataSourceList.set(to, temp);
				// 设置新到的item隐藏，不用调用notifyDataSetChanged来刷新界面，因为setItemHide方法里面调用了
				adapter.setItemHide(to);
				

			}
		});
		//

		gridView.setAdapter(adapter);
		// int size = dataList.size();
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		float density = dm.density;
		// Log.v("mandy", "density is " + density);
		int allWidth = (int) (110 * 6 * density);
		int itemWidth = (int) (100 * density);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				allWidth, LinearLayout.LayoutParams.MATCH_PARENT);
		gridView.setLayoutParams(params);
		gridView.setColumnWidth(itemWidth);
		gridView.setHorizontalSpacing(10);
		gridView.setVerticalSpacing(300);
		gridView.setStretchMode(GridView.NO_STRETCH);
		gridView.setNumColumns(6);

	}

	 @Override
	protected void onResume() {
	    if (adapter != null) {
	    	adapter.setItemHide(-1);
	    }
		super.onResume();
	}
	private void initData() {

		for (int i = 0; i < 12; i++) {
			DataItem dataItem = new DataItem();
			dataItem.setName(i + "");
			if (i == 0) {
				dataItem.setBitmap(BitmapFactory.decodeResource(getResources(),
						R.drawable.aa));
			} else if (i == 1) {
				dataItem.setBitmap(BitmapFactory.decodeResource(getResources(),
						R.drawable.bb));

			} else if (i == 2) {
				dataItem.setBitmap(BitmapFactory.decodeResource(getResources(),
						R.drawable.cc));

			} else if (i == 3) {
				dataItem.setBitmap(BitmapFactory.decodeResource(getResources(),
						R.drawable.dd));

			} else if (i == 4) {
				dataItem.setBitmap(BitmapFactory.decodeResource(getResources(),
						R.drawable.ee));

			} else if (i == 5) {
				dataItem.setBitmap(BitmapFactory.decodeResource(getResources(),
						R.drawable.ff));

			} else if (i == 6) {

				dataItem.setBitmap(BitmapFactory.decodeResource(getResources(),
						R.drawable.mm));
			} else if (i == 7) {
				dataItem.setBitmap(BitmapFactory.decodeResource(getResources(),
						R.drawable.hh));

			} else if (i == 8) {

				dataItem.setBitmap(BitmapFactory.decodeResource(getResources(),
						R.drawable.ii));
			} else if (i == 9) {
				dataItem.setBitmap(BitmapFactory.decodeResource(getResources(),
						R.drawable.jj));
			} else if (i == 10) {
				dataItem.setBitmap(BitmapFactory.decodeResource(getResources(),
						R.drawable.kk));

			} else if (i == 11) {

				dataItem.setBitmap(BitmapFactory.decodeResource(getResources(),
						R.drawable.ll));
			}

			dataSourceList.add(dataItem);
		}

	}

	public class DataItem {
		private String name;
		private Bitmap bitmap;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Bitmap getBitmap() {
			return bitmap;
		}

		public void setBitmap(Bitmap bitmap) {
			this.bitmap = bitmap;
		}

	}

	public static class GridViewAdapter extends BaseAdapter {
        
		
		private int mHidePosition = -1;

		@Override
		public int getCount() {
			return dataSourceList.size();
		}

		public void setItemHide(int to) {
			this.mHidePosition = to; 
			notifyDataSetChanged();
		}

		@Override
		public Object getItem(int position) {
			return dataSourceList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			
			// if (convertView == null) {

			convertView = inflater.inflate(R.layout.grid_item, null);
			// }
			DataItem dataItem = dataSourceList.get(position);
            
			ImageView imageView = (ImageView) convertView
					.findViewById(R.id.imageview);
			imageView.setImageBitmap(dataItem.getBitmap());
			TextView textView = (TextView) convertView
					.findViewById(R.id.textView);

			textView.setText(dataItem.getName());
			// String str = dataList.get(position);
			// textView.setText(str);
			if(position == mHidePosition){
				Log.v("gao", "hide position is : " + mHidePosition);
				convertView.setVisibility(View.INVISIBLE);
			}
			
			
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
