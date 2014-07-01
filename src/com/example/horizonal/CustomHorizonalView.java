package com.example.horizonal;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;

public class CustomHorizonalView extends HorizontalScrollView {

	private GridView dragGridView;
	/**
	 * DragGridView的item长按响应的时间， 默认是1000毫秒，也可以自行设置
	 */
	private long dragResponseMS = 200;
	
	private boolean isVerticalScroll = false;

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
	/**
	 * item发生变化回调的接口
	 */
	private OnChanageListener onChanageListener;

	/**
	 * 设置回调接口
	 * 
	 * @param onChanageListener
	 */
	public void setOnChangeListener(OnChanageListener onChanageListener) {
		this.onChanageListener = onChanageListener;
	}

	public void setDragGridView(GridView dragGridView) {
		this.dragGridView = dragGridView;
	}

	public interface OnChanageListener {

		void onChange(int from, int to);

	}

	private int left;

	public CustomHorizonalView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);

	}

	public CustomHorizonalView(Context context) {
		this(context, null);
	}

	public CustomHorizonalView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mVibrator = (Vibrator) context
				.getSystemService(Context.VIBRATOR_SERVICE);
		mWindowManager = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		mStatusHeight = getStatusHeight(context); // 获取状态栏的高度

		// mVibrator = (Vibrator)
		// context.getSystemService(Context.VIBRATOR_SERVICE);
		// mWindowManager = (WindowManager)
		// context.getSystemService(Context.WINDOW_SERVICE);
		// mStatusHeight = getStatusHeight(context); //获取状态栏的高度
		// setOnItemLongClickListener(this);

	}

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
		mWindowLayoutParams.x = moveX - mPoint2ItemLeft + mOffset2Left;
		mWindowLayoutParams.y = moveY - mPoint2ItemTop;
		mWindowManager.updateViewLayout(mDragImageView, mWindowLayoutParams); // 更新镜像的位置
		onSwapItem(moveX, moveY);

		// Log.v("mandy", "on DragItem: " + moveX + "moveY:  " + moveY);
		// GridView自动滚动
		mHandler.post(mScrollRunnable);
	}

	/**
	 * 交换item,并且控制item之间的显示与隐藏效果
	 * 
	 * @param moveX
	 * @param moveY
	 */
	private void onSwapItem(int moveX, int moveY) {
		// 获取我们手指移动到的那个item的position
		int tempPosition = dragGridView.pointToPosition(moveX + left, moveY);

		// Log.v("mandy", "temp position: " + tempPosition);

		// 假如tempPosition 改变了并且tempPosition不等于-1,则进行交换
		if (tempPosition != mDragPosition
				&& tempPosition != AdapterView.INVALID_POSITION) {
			if (onChanageListener != null) {
				onChanageListener.onChange(mDragPosition, tempPosition);
			}
//            Log.v("gao", "swap.....  " + mDragPosition +" " + tempPosition);
            
           
            
            
//            for (int i = 0; i < dragGridView.getAdapter().getCount(); i++) {
////				
//            	  Log.v("gao", "whitch child view is hide?  " + i + "==" + ((View) dragGridView.getAdapter().getItem(i)).getVisibility());
////            	  dragGridView.getChildAt(i).setVisibility(View.VISIBLE);
//			}
            
			dragGridView.getChildAt(tempPosition).setVisibility(View.INVISIBLE);// 拖动到了新的item,新的item隐藏掉
			dragGridView.getChildAt(mDragPosition).setVisibility(View.VISIBLE);// 之前的item显示出来

			mDragPosition = tempPosition;
		}else {
			
//			dragGridView.getChildAt(mDragPosition).setVisibility(View.VISIBLE);// 之前的item显示出来
			
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
			if (moveX > mUpScrollBorder) {
				scrollY = speed;
				mHandler.postDelayed(mScrollRunnable, 25);
			} else if (moveX < mDownScrollBorder) {
				scrollY = -speed;
				mHandler.postDelayed(mScrollRunnable, 25);
			} else {
				scrollY = 0;
				mHandler.removeCallbacks(mScrollRunnable);
			}

			// 当我们的手指到达GridView向上或者向下滚动的偏移量的时候，可能我们手指没有移动，但是DragGridView在自动的滚动
			// 所以我们在这里调用下onSwapItem()方法来交换item
			onSwapItem(moveX, moveY);

			// smoothScrollBy(moveX, scrollY);

			smoothScrollBy(scrollY, 10);
		}
	};

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		// checkTotalHeight();
		Rect outRect = new Rect();
		getDrawingRect(outRect);

		left = l;

		// OnScrollStopListner actionScrollListener = (onScrollstopListner !=
		// null) ? onScrollstopListner : defaultOnScrollstopListner;
		// actionScrollListener.onScrollStoped();
		Log.d("mandy", "onScrollChanged  l " + l + " t " + t + " oldL " + oldl
				+ " oldt " + oldt + " outRect.bottom " + outRect.bottom);
		// if(t == 0) {
		// // actionScrollListener.onScrollToTopEdge();
		// } else if (childHeight + getPaddingTop()
		// + getPaddingBottom() == outRect.bottom) {
		// // actionScrollListener.onScrollToBottomEdge();
		// } else {
		// // actionScrollListener.onScrollToMiddle();
		// }
		super.onScrollChanged(l, t, oldl, oldt);
	}

	private Handler mHandler = new Handler() {
		
		public void handleMessage(android.os.Message msg) {
			
//			 isVerticalScroll = false;	
			
			
		};
		
		
	};

	// 用来处理是否为长按的Runnable
	private Runnable mLongClickRunnable = new Runnable() {

		@Override
		public void run() {

			Log.v("mandy", "hide item view");
			isDrag = true; // 设置可以拖拽
			mVibrator.vibrate(50); // 震动一下
			mStartDragItemView.setVisibility(View.INVISIBLE);// 隐藏该item

			// 根据我们按下的点显示item镜像
			createDragImage(mDragBitmap, mDownX, mDownY);
		}
	};
	private int x = 0;

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {

		// mDragPosition = dragGridView.pointToPosition((int)ev.getX()+left,
		// (int)ev.getY());

//		Log.v("mandy", "dispatch touch event" + ev);
		// Log.v("mandy", "mDragPosition: " + mDragPosition);

		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			 Log.v("mandy",
			 "action down......*********************************");
			// 使用Handler延迟dragResponseMS执行mLongClickRunnable
			mHandler.postDelayed(mLongClickRunnable, dragResponseMS);
			//
			mDownX = (int) ev.getX();
			mDownY = (int) ev.getY();
			//
			// 根据按下的X,Y坐标获取所点击item的position
			mDragPosition = dragGridView.pointToPosition(mDownX + left, mDownY);
			//
			Log.v("mandy", "mDragPosition: " + mDragPosition);
			//
			if (mDragPosition == AdapterView.INVALID_POSITION) {
				return super.dispatchTouchEvent(ev);
			}
			//
			// // Log.v("mandy", "drag position: " + mDragPosition +
			// // " fristVisible position: " + getFirstVisiblePosition());
			// // 根据position获取该item所对应的View
			mStartDragItemView = dragGridView.getChildAt(mDragPosition);
			//
			//
			mPoint2ItemTop = mDownY - mStartDragItemView.getTop();
			mPoint2ItemLeft = mDownX - (mStartDragItemView.getLeft() - left);

			Log.v("mandy", "mDownX: " + mDownY);
			Log.v("mandy", "left: " + mStartDragItemView.getTop());

			//
			mOffset2Top = (int) (ev.getRawY() - mDownY);
			mOffset2Left = (int) (ev.getRawX() - mDownX);
			//
			// // 获取DragGridView自动向上滚动的偏移量，小于这个值，DragGridView向下滚动
			mDownScrollBorder = getWidth() / 4;
			//
			// // 获取DragGridView自动向下滚动的偏移量，大于这个值，DragGridView向上滚动
			mUpScrollBorder = getWidth() * 3 / 4;
			//
			// // Log.v("mandy", "gridview width: " + getWidth());
			//
			// // 开启mDragItemView绘图缓存
			mStartDragItemView.setDrawingCacheEnabled(true);
			// // 获取mDragItemView在缓存中的Bitmap对象
			mDragBitmap = Bitmap.createBitmap(mStartDragItemView
					.getDrawingCache());
			// // 这一步很关键，释放绘图缓存，避免出现重复的镜像
			mStartDragItemView.destroyDrawingCache();
			//
			break;
		case MotionEvent.ACTION_MOVE:
			Log.v("mandy", "action move......");
			int moveX = (int) ev.getX();
			int moveY = (int) ev.getY();
			//
		
			//
			// // 如果我们在按下的item上面移动，只要不超过item的边界我们就不移除mRunnable
			if (!isTouchInItem(mStartDragItemView, moveX + left, moveY)) {
				// Log.v("mandy", "remove call backs ...............");
				mHandler.removeCallbacks(mLongClickRunnable);
			}
			
			//当垂直向下滑动的时候系统不会去调用ontouchEvent，因此需要判断当两次滑动的x值没有发生变化说明是垂直滑动，手动去调用，
			//暂时没有想到更好的方法
			if (moveX == x && isDrag && mStartDragItemView != null) {
				  Log.v("mandy", "x: " + x + "movex : " + moveX);
				  onTouchEvent(ev);
				  isVerticalScroll = true; 
			}
			 x = moveX;
			
//			if (isVerticalScroll && isDrag && mStartDragItemView != null) {
//				
//				Log.v("gao", "invoke ontouch event");
//				  onTouchEvent(ev);
//				
//			}
			
			
			// onTouchEvent(ev);
			break;
		case MotionEvent.ACTION_UP:
			// Log.v("mandy", "action up......");
			mHandler.removeCallbacks(mLongClickRunnable);
			mHandler.removeCallbacks(mScrollRunnable);
			if (isVerticalScroll) {
				onTouchEvent(ev);
				isVerticalScroll = false;
				
			}
			
			break;
		}
		return super.dispatchTouchEvent(ev);
	}

	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		Log.v("mandy", "horizonal onTouchEvent: " + ev.getAction());

		if (isDrag && mDragImageView != null) {
			switch (ev.getAction()) {
			case MotionEvent.ACTION_MOVE:
				
				mHandler.sendEmptyMessage(0);
				Log.v("mandy", "ontouchevent ACTION_MOVE");
				moveX = (int) ev.getX();
				moveY = (int) ev.getY();
				// moveX = left;
				//
				// Log.v("mandy", "custom moveX: " + moveX + " custom moveY: " +
				// moveY);
				// //拖动item
				onDragItem(moveX, moveY);
				break;
			case MotionEvent.ACTION_UP:
				// // Log.v("mandy", "ontouchevent action up");
				onStopDrag();
				isDrag = false;
				break;
			}
			return true;
		}
		return super.onTouchEvent(ev);
	}

	/**
	 * 停止拖拽我们将之前隐藏的item显示出来，并将镜像移除
	 */
	private void onStopDrag() {
		
		View view = dragGridView.getChildAt(mDragPosition);
		if (view != null) {
			view.setVisibility(View.VISIBLE);
		}
		removeDragImage();
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

		if (dragView == null) {
			return false;

		}
		// Log.v("mandy", "is touch in item, left: "+ dragView.getLeft()
		// '\' +" right: " + dragView.getRight() + "top: " + dragView.getTop() +
		// "bottom: " + dragView.getBottom());
		int leftOffset = dragView.getLeft();

		// Log.v("mandy", "leftOffset: " + left);
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

}
