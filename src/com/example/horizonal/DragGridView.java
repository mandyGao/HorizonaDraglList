package com.example.horizonal;

import com.example.horizonal.CustomHorizonalView.Location;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.AdapterView.OnItemLongClickListener;

/**
 * @blog http://blog.csdn.net/xiaanming
 * 
 * @author xiaanming
 * 
 */
public class DragGridView extends GridView implements OnItemLongClickListener {
	/**
	 * DragGridView的item长按响应的时间， 默认是1000毫秒，也可以自行设置
	 */
	private long dragResponseMS = 1000;

	/**
	 * 是否可以拖拽，默认不可以
	 */
	private boolean isDrag = false;

	private int mDownX;
	private int mDownY;
	private int moveX;
	private int moveY;
	/**
	 * 正在拖拽的position
	 */
	private int mDragPosition;

	/**
	 * 刚开始拖拽的item对应的View
	 */
	private View mStartDragItemView = null;

	/**
	 * 用于拖拽的镜像，这里直接用一个ImageView
	 */
	private ImageView mDragImageView;

	private CustomHorizonalView customHorizonalView;

	/**
	 * 震动器
	 */
	private Vibrator mVibrator;

	private WindowManager mWindowManager;
	/**
	 * item镜像的布局参数
	 */
	private WindowManager.LayoutParams mWindowLayoutParams;

	/**
	 * 我们拖拽的item对应的Bitmap
	 */
	private Bitmap mDragBitmap;

	/**
	 * 按下的点到所在item的上边缘的距离
	 */
	private int mPoint2ItemTop;

	/**
	 * 按下的点到所在item的左边缘的距离
	 */
	private int mPoint2ItemLeft;

	/**
	 * DragGridView距离屏幕顶部的偏移量
	 */
	private int mOffset2Top;

	/**
	 * DragGridView距离屏幕左边的偏移量
	 */
	private int mOffset2Left;

	/**
	 * 状态栏的高度
	 */
	private int mStatusHeight;

	/**
	 * DragGridView自动向下滚动的边界值
	 */
	private int mDownScrollBorder;

	/**
	 * DragGridView自动向上滚动的边界值
	 */
	private int mUpScrollBorder;

	/**
	 * DragGridView自动滚动的速度
	 */
	private static final int speed = 20;
	
	private int mRightScrollBorder;
	private int mLeftScrollBorder;
	private int parentMoveX;
	private int ParentMoveY;

	/**
	 * item发生变化回调的接口
	 */
	private OnChanageListener onChanageListener;

	public DragGridView(Context context) {
		this(context, null);
	}

	public DragGridView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public DragGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mVibrator = (Vibrator) context
				.getSystemService(Context.VIBRATOR_SERVICE);
		mWindowManager = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		mStatusHeight = getStatusHeight(context); // 获取状态栏的高度
		setOnItemLongClickListener(this);
//		customHorizonalView.setHandler(handler);

	}
	
	public Handler getHandler () {
		
		return handler;
		
	}
	
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 0) {
				Location location = (Location)msg.obj;
				onDragItem(location.moveX, location.moveY);
				parentMoveX = location.moveX;
				parentMoveX = location.moveY;
			} else if (msg.what == 1) {
				onStopDrag();
				isDrag = false;
			} else if (msg.what ==2) {
//				onSwapItem(moveX, moveY);
			}
			
			
		};
		
	};

	public void setCustomHorizonalView(CustomHorizonalView customHorizonalView) {
		this.customHorizonalView = customHorizonalView;
	}

	public Handler mHandler = new Handler();

	// 用来处理是否为长按的Runnable
	private Runnable mLongClickRunnable = new Runnable() {

		@Override
		public void run() {
			isDrag = true; // 设置可以拖拽
			mVibrator.vibrate(50); // 震动一下
			mStartDragItemView.setVisibility(View.INVISIBLE);// 隐藏该item

			// 根据我们按下的点显示item镜像
			createDragImage(mDragBitmap, mDownX, mDownY);
		}
	};

	/**
	 * 设置回调接口
	 * 
	 * @param onChanageListener
	 */
	public void setOnChangeListener(OnChanageListener onChanageListener) {
		this.onChanageListener = onChanageListener;
	}

	/**
	 * 设置响应拖拽的毫秒数，默认是1000毫秒
	 * 
	 * @param dragResponseMS
	 */
	public void setDragResponseMS(long dragResponseMS) {
		this.dragResponseMS = dragResponseMS;
	}

	/**
	 * 是否点击在GridView的item上面
	 * 
	 * @param itemView
	 * @param x
	 * @param y
	 * @return
	 */
	private boolean isTouchInItem(View dragView, int x, int y) {
       if (dragView  == null) {
    	   
    	   return false;
       }
		// Log.v("mandy", "is touch in item, left: "+ dragView.getLeft()
		// '\' +" right: " + dragView.getRight() + "top: " + dragView.getTop() +
		// "bottom: " + dragView.getBottom());
		int leftOffset = dragView.getLeft();
		int topOffset = dragView.getTop();

		// 判断是否是在item view 上
		if (x < leftOffset || x > leftOffset + dragView.getWidth()) {
			return false;
		}

		if (y < topOffset || y > topOffset + dragView.getHeight()) {
			return false;
		}

		return true;
	}

	// @Override
	// public boolean onTouchEvent(MotionEvent ev) {
	// if(isDrag && mDragImageView != null){
	// switch(ev.getAction()){
	// case MotionEvent.ACTION_MOVE:
	// // Log.v("mandy", "ontouchevent ACTION_MOVE");
	// moveX = (int) ev.getX();
	// moveY = (int) ev.getY();
	// //拖动item
	// onDragItem(moveX, moveY);
	// break;
	// case MotionEvent.ACTION_UP:
	// // Log.v("mandy", "ontouchevent action up");
	// onStopDrag();
	// isDrag = false;
	// break;
	// }
	// return true;
	// }
	// return super.onTouchEvent(ev);
	// }

	/**
	 * 创建拖动的镜像
	 * 
	 * @param bitmap
	 * @param downX
	 *            按下的点相对父控件的X坐标
	 * @param downY
	 *            按下的点相对父控件的X坐标
	 */
	private void createDragImage(Bitmap bitmap, int downX, int downY) {
		mWindowLayoutParams = new WindowManager.LayoutParams();
		mWindowLayoutParams.format = PixelFormat.TRANSLUCENT; // 图片之外的其他地方透明
		mWindowLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
		mWindowLayoutParams.x = downX - mPoint2ItemLeft + mOffset2Left;
		mWindowLayoutParams.y = downY - mPoint2ItemTop + mOffset2Top
				- mStatusHeight;
		mWindowLayoutParams.alpha = 0.55f; // 透明度
		mWindowLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
		mWindowLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
		mWindowLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
				| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;

		mDragImageView = new ImageView(getContext());
		mDragImageView.setImageBitmap(bitmap);
		mWindowManager.addView(mDragImageView, mWindowLayoutParams);
	}

	/**
	 * 从界面上面移动拖动镜像
	 */
	private void removeDragImage() {
		if (mDragImageView != null) {
			mWindowManager.removeView(mDragImageView);
			mDragImageView = null;
		}
	}

	/**
	 * 拖动item，在里面实现了item镜像的位置更新，item的相互交换以及GridView的自行滚动
	 * 
	 * @param x
	 * @param y
	 */
	private void onDragItem(int moveX, int moveY) {
		if (isDrag && mDragImageView != null) {
		Log.v("mandy", "onDragItem.............");
		mWindowLayoutParams.x = moveX - mPoint2ItemLeft + mOffset2Left;
		mWindowLayoutParams.y = moveY - mPoint2ItemTop + mOffset2Top
				- mStatusHeight;
		mWindowManager.updateViewLayout(mDragImageView, mWindowLayoutParams); // 更新镜像的位置
		onSwapItem(this.moveX, this.moveY);
		// Log.v("mandy", "on DragItem: " + moveX + "moveY:  " + moveY);
		// GridView自动滚动
//		customHorizonalView.mHandler.post(customHorizonalView.mScrollRunnable);
		}
	}

	/**
	 * 当moveY的值大于向上滚动的边界值，触发GridView自动向上滚动 当moveY的值小于向下滚动的边界值，触犯GridView自动向下滚动
	 * 否则不进行滚动
	 */
	private Runnable mScrollRunnable = new Runnable() {

		@Override
		public void run() {
			int scrollY;
			if (parentMoveX > mLeftScrollBorder) {
				scrollY = speed;
				mHandler.postDelayed(mScrollRunnable, 25);
			} else if (parentMoveX < mRightScrollBorder) {
				scrollY = -speed;
				mHandler.postDelayed(mScrollRunnable, 25);
			} else {
				scrollY = 0;
				mHandler.removeCallbacks(mScrollRunnable);
			}

			// 当我们的手指到达GridView向上或者向下滚动的偏移量的时候，可能我们手指没有移动，但是DragGridView在自动的滚动
			// 所以我们在这里调用下onSwapItem()方法来交换item
			onSwapItem(moveX, moveY);
            Log.v("mandy", "mScrollRunnable...........");
			customHorizonalView.scorllLocation(scrollY, 10);

//			smoothScrollBy(scrollY, 10);
		}
	};

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {

		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			 Log.v("mandy", "action down......*********************************");
			// 使用Handler延迟dragResponseMS执行mLongClickRunnable
			mHandler.postDelayed(mLongClickRunnable,
					dragResponseMS);

			mDownX = (int) ev.getX();
			mDownY = (int) ev.getY();

			// 根据按下的X,Y坐标获取所点击item的position
			mDragPosition = pointToPosition(mDownX, mDownY);
			
			Log.v("mandy", "mDragPosition: " + mDragPosition);

			if (mDragPosition == AdapterView.INVALID_POSITION) {
				return super.dispatchTouchEvent(ev);
			}

			// Log.v("mandy", "drag position: " + mDragPosition +
			// " fristVisible position: " + getFirstVisiblePosition());
			// 根据position获取该item所对应的View
			mStartDragItemView = getChildAt(mDragPosition 
					);

		
			mPoint2ItemTop = mDownY - mStartDragItemView.getTop();
			mPoint2ItemLeft = mDownX - mStartDragItemView.getLeft();

			mOffset2Top = (int) (ev.getRawY() - mDownY);
			mOffset2Left = (int) (ev.getRawX() - mDownX);

			// 获取DragGridView自动向右滚动的偏移量，小于这个值，DragGridView向右滚动
			mRightScrollBorder = customHorizonalView.getWidth() / 4;

			// 获取DragGridView自动向右滚动的偏移量，大于这个值，DragGridView向左滚动
			mLeftScrollBorder = customHorizonalView.getWidth() * 3 / 4;

//			Log.v("mandy", "gridview width: " + getWidth());

			// 开启mDragItemView绘图缓存
			mStartDragItemView.setDrawingCacheEnabled(true);
			// 获取mDragItemView在缓存中的Bitmap对象
			mDragBitmap = Bitmap.createBitmap(mStartDragItemView
					.getDrawingCache());
			// 这一步很关键，释放绘图缓存，避免出现重复的镜像
			mStartDragItemView.destroyDrawingCache();

			break;
		case MotionEvent.ACTION_MOVE:
			// Log.v("mandy", "action move......");
			int moveX = (int) ev.getX();
			int moveY = (int) ev.getY();
			
//			Log.v("mandy", "mStartDragItemView...: " + mStartDragItemView);

			// Log.v("mandy", "x: " + moveX + "Y: " + moveY);

			// 如果我们在按下的item上面移动，只要不超过item的边界我们就不移除mRunnable
			if (!isTouchInItem(mStartDragItemView, moveX, moveY)) {
				// Log.v("mandy", "remove call backs ...............");
				mHandler.removeCallbacks(mLongClickRunnable);
			}
			break;
		case MotionEvent.ACTION_UP:
			Log.v("mandy", "action up..........");
			mHandler.removeCallbacks(mLongClickRunnable);
			customHorizonalView.mHandler.removeCallbacks(customHorizonalView.mScrollRunnable);
			break;
		}
		return super.dispatchTouchEvent(ev);

	};

	 @Override
	 public boolean onTouchEvent(MotionEvent ev) {
//		 switch(ev.getAction()){
		  
//		 case MotionEvent.ACTION_MOVE:
			 moveX = (int) ev.getX();
			 moveY = (int) ev.getY();
			 Log.v("mandy", "moveX: " + moveX);
			 Log.v("mandy", "moveY: " + moveY);
//			 break;
//		 }
		
		
		 
//	 if(customHorizonalView.isDrag && customHorizonalView.mDragImageView !=
//	 null){
//	 switch(ev.getAction()){
//	 case MotionEvent.ACTION_MOVE:
//	 // Log.v("mandy", "ontouchevent ACTION_MOVE");
	
//	
//	 Log.v("mandy", "custom moveX: " + moveX + " custom moveY: " + moveY);
//	 //拖动item
//	 customHorizonalView.onDragItem(moveX, moveY);
//	 break;
//	 case MotionEvent.ACTION_UP:
//	 // Log.v("mandy", "ontouchevent action up");
//	 customHorizonalView.onStopDrag();
//	 customHorizonalView.isDrag = false;
//	 break;
//	 }
//	
//	
//	 // Log.v("mandy", " moveX: " + ev.getX() + "  moveY: " + ev.getY());
//	 //
//	 // int position = pointToPosition((int)ev.getX(), (int)ev.getY());
//	 //
//	 // Log.v("mandy", " position  " + position);
//	 return true;
//	 }
	 return super.onTouchEvent(ev);
	
	 };

	/**
	 * 交换item,并且控制item之间的显示与隐藏效果
	 * 
	 * @param moveX
	 * @param moveY
	 */
	public void onSwapItem(int moveX, int moveY) {
		
		
//		Log.v("mandy", "onSwapItem.....");
		// 获取我们手指移动到的那个item的position
		int tempPosition = pointToPosition(moveX, moveY);
//        Log.v("mandy", "tempPosition:  " + tempPosition);
		// 假如tempPosition 改变了并且tempPosition不等于-1,则进行交换
		if (tempPosition != mDragPosition
				&& tempPosition != AdapterView.INVALID_POSITION) {
			if (onChanageListener != null) {
				Log.v("mandy", "on swap item");
				onChanageListener.onChange(mDragPosition, tempPosition);
			}

			getChildAt(tempPosition - getFirstVisiblePosition()).setVisibility(
					View.INVISIBLE);// 拖动到了新的item,新的item隐藏掉
//			getChildAt(tempPosition).setVisibility(View.INVISIBLE);
//			getChildAt(mDragPosition).setVisibility(View.VISIBLE);
//			
			getChildAt(mDragPosition - getFirstVisiblePosition())
					.setVisibility(View.VISIBLE);// 之前的item显示出来

			mDragPosition = tempPosition;
		}
	}

	/**
	 * 停止拖拽我们将之前隐藏的item显示出来，并将镜像移除
	 */
	private void onStopDrag() {
		View view = getChildAt(mDragPosition - getFirstVisiblePosition());
		if (view != null) {
			view.setVisibility(View.VISIBLE);
		}
		removeDragImage();
	}

	/**
	 * 获取状态栏的高度
	 * 
	 * @param context
	 * @return
	 */
	private static int getStatusHeight(Context context) {
		int statusHeight = 0;
		Rect localRect = new Rect();
		((Activity) context).getWindow().getDecorView()
				.getWindowVisibleDisplayFrame(localRect);
		statusHeight = localRect.top;
		if (0 == statusHeight) {
			Class<?> localClass;
			try {
				localClass = Class.forName("com.android.internal.R$dimen");
				Object localObject = localClass.newInstance();
				int i5 = Integer.parseInt(localClass
						.getField("status_bar_height").get(localObject)
						.toString());
				statusHeight = context.getResources().getDimensionPixelSize(i5);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return statusHeight;
	}

	/**
	 * 
	 * @author xiaanming
	 * 
	 */
	public interface OnChanageListener {

		/**
		 * 当item交换位置的时候回调的方法，我们只需要在该方法中实现数据的交换即可
		 * 
		 * @param form
		 *            开始的position
		 * @param to
		 *            拖拽到的position
		 */
		public void onChange(int form, int to);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {

		Log.v("mandy", "onitem long click....");

		return false;
	}
}
