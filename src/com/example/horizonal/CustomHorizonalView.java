package com.example.horizonal;

import com.example.horizonal.DragGridView.OnChanageListener;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;

@SuppressLint("NewApi")
public class CustomHorizonalView extends HorizontalScrollView {

	private DragGridView dragGridView;
	/**
	 * DragGridView的item长按响应的时间， 默认是1000毫秒，也可以自行设置
	 */
	private long dragResponseMS = 1000;
	
	private Handler handler;

	/**
	 * 是否可以拖拽，默认不可以
	 */
	public boolean isDrag = false;

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
	public ImageView mDragImageView;
	
	
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


	private int mRightScrollBorder;

	private int mLeftScrollBorder;

	/**
	 * DragGridView自动滚动的速度
	 */
	private static final int speed = 20;
	/**
	 * item发生变化回调的接口
	 */
	private OnChanageListener onChanageListener;
//	private int gridMoveX;
//	private int gridMoveY;
	
	/**
	 * 设置回调接口
	 * @param onChanageListener
	 */
	public void setOnChangeListener(OnChanageListener onChanageListener){
		this.onChanageListener = onChanageListener;
	}
	

	public void setDragGridView(DragGridView dragGridView) {
		this.dragGridView = dragGridView;
		
	}

	public CustomHorizonalView(Context context, AttributeSet attrs) {
		this(context, attrs,0);

	}

	public CustomHorizonalView(Context context) {
		this(context,null);
	}

	public CustomHorizonalView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
		mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		mStatusHeight = getStatusHeight(context); //获取状态栏的高度
		 
		// mVibrator = (Vibrator)
		// context.getSystemService(Context.VIBRATOR_SERVICE);
		// mWindowManager = (WindowManager)
		// context.getSystemService(Context.WINDOW_SERVICE);
		// mStatusHeight = getStatusHeight(context); //获取状态栏的高度
		// setOnItemLongClickListener(this);

	}
	
	

	public void setHandler(Handler handler) {
		this.handler = handler;
	}

	public Handler mHandler = new Handler();

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
	
	/**
	 * 创建拖动的镜像
	 * @param bitmap 
	 * @param downX
	 * 			按下的点相对父控件的X坐标
	 * @param downY
	 * 			按下的点相对父控件的X坐标
	 */
	private void createDragImage(Bitmap bitmap, int downX , int downY){
		mWindowLayoutParams = new WindowManager.LayoutParams();
		mWindowLayoutParams.format = PixelFormat.TRANSLUCENT; //图片之外的其他地方透明
		mWindowLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
		mWindowLayoutParams.x = downX - mPoint2ItemLeft + mOffset2Left;
		mWindowLayoutParams.y = downY - mPoint2ItemTop + mOffset2Top - mStatusHeight;
		mWindowLayoutParams.alpha = 0.55f; //透明度
		mWindowLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;  
		mWindowLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;  
		mWindowLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE  
	                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE ;
		  
		mDragImageView = new ImageView(getContext());  
		mDragImageView.setImageBitmap(bitmap);  
		mWindowManager.addView(mDragImageView, mWindowLayoutParams);  
	}
	
	/**
	 * 从界面上面移动拖动镜像
	 */
	private void removeDragImage(){
		if(mDragImageView != null){
			mWindowManager.removeView(mDragImageView);
			mDragImageView = null;
		}
	}
	
	/**
	 * 拖动item，在里面实现了item镜像的位置更新，item的相互交换以及GridView的自行滚动
	 * @param x
	 * @param y
	 */
	public void onDragItem(int moveX, int moveY){
		if (mWindowLayoutParams == null || mDragImageView == null) {
			return;
		}
		mWindowLayoutParams.x = moveX - mPoint2ItemLeft + mOffset2Left;
		mWindowLayoutParams.y = moveY - mPoint2ItemTop + mOffset2Top - mStatusHeight;
		mWindowManager.updateViewLayout(mDragImageView, mWindowLayoutParams); //更新镜像的位置
		onSwapItem(moveX, moveY);
		
//		Log.v("mandy", "on DragItem: " + moveX + "moveY:  " + moveY);
		//GridView自动滚动
		mHandler.post(mScrollRunnable);
	}
	
	/**
	 * 当moveY的值大于向上滚动的边界值，触发GridView自动向上滚动
	 * 当moveY的值小于向下滚动的边界值，触犯GridView自动向下滚动
	 * 否则不进行滚动
	 */
	public Runnable mScrollRunnable = new Runnable() {
		
		@Override
		public void run() {
			int scrollY;
			if(moveX > mLeftScrollBorder){
				 scrollY = speed;
				 mHandler.postDelayed(mScrollRunnable, 25);
			}else if(moveX < mRightScrollBorder){
				scrollY = -speed;
				 mHandler.postDelayed(mScrollRunnable, 25);
			}else{
				scrollY = 0;
				mHandler.removeCallbacks(mScrollRunnable);
			}
			
			//当我们的手指到达GridView向上或者向下滚动的偏移量的时候，可能我们手指没有移动，但是DragGridView在自动的滚动
			//所以我们在这里调用下onSwapItem()方法来交换item
//			onSwapItem(moveX, moveY);
			dragGridView.onSwapItem(0,0);
			
//			handler.sendEmptyMessage(2);
			
//			smoothScrollBy(moveX, scrollY);
			
			smoothScrollBy(scrollY, 10);
		}
	};
	
	
	/**
	 * 交换item,并且控制item之间的显示与隐藏效果
	 * @param moveX
	 * @param moveY
	 */
	private void onSwapItem(int moveX, int moveY){
		//获取我们手指移动到的那个item的position
		int tempPosition = dragGridView.pointToPosition(moveX, moveY);
		
//		Log.v("mandy", "temp position: " + tempPosition);
		
		//假如tempPosition 改变了并且tempPosition不等于-1,则进行交换
		if(tempPosition != mDragPosition && tempPosition != AdapterView.INVALID_POSITION){
			if(onChanageListener != null){
				onChanageListener.onChange(mDragPosition, tempPosition);
			}
			dragGridView.getChildAt(tempPosition).setVisibility(View.INVISIBLE);//拖动到了新的item,新的item隐藏掉
			dragGridView.getChildAt(mDragPosition).setVisibility(View.VISIBLE);//之前的item显示出来
			
			mDragPosition = tempPosition;
		}
	}
	
//	@Override
//	public boolean dispatchDragEvent(android.view.DragEvent event) {
//		
//		Log.v("mandy", "event action: " + event.getAction() + " " + event.getLocalState() );
//		Log.v("mandy", "event x y: " + event.getX() + " " + event.getY() );
//		return super.dispatchDragEvent(event);
//		
//	};
	

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
//			 Log.v("mandy", "action down......*********************************");
			// 使用Handler延迟dragResponseMS执行mLongClickRunnable
//			mHandler.postDelayed(mLongClickRunnable,
//					dragResponseMS);
//
//			mDownX = (int) ev.getX();
//			mDownY = (int) ev.getY();
//
//			// 根据按下的X,Y坐标获取所点击item的position
//			mDragPosition = dragGridView.pointToPosition(mDownX, mDownY);
//			
//			Log.v("mandy", "mDragPosition: " + mDragPosition);
//
//			if (mDragPosition == AdapterView.INVALID_POSITION) {
//				return super.dispatchTouchEvent(ev);
//			}
//
//			// Log.v("mandy", "drag position: " + mDragPosition +
//			// " fristVisible position: " + getFirstVisiblePosition());
//			// 根据position获取该item所对应的View
//			mStartDragItemView = dragGridView.getChildAt(mDragPosition 
//					);
//
//		
//			mPoint2ItemTop = mDownY - mStartDragItemView.getTop();
//			mPoint2ItemLeft = mDownX - mStartDragItemView.getLeft();
//
//			mOffset2Top = (int) (ev.getRawY() - mDownY);
//			mOffset2Left = (int) (ev.getRawX() - mDownX);
//
//			// 获取DragGridView自动向右滚动的偏移量，小于这个值，DragGridView向右滚动
			mRightScrollBorder = getWidth() / 4;
//
//			// 获取DragGridView自动向右滚动的偏移量，大于这个值，DragGridView向左滚动
			mLeftScrollBorder = getWidth() * 3 / 4;
//
////			Log.v("mandy", "gridview width: " + getWidth());
//
//			// 开启mDragItemView绘图缓存
//			mStartDragItemView.setDrawingCacheEnabled(true);
//			// 获取mDragItemView在缓存中的Bitmap对象
//			mDragBitmap = Bitmap.createBitmap(mStartDragItemView
//					.getDrawingCache());
//			// 这一步很关键，释放绘图缓存，避免出现重复的镜像
//			mStartDragItemView.destroyDrawingCache();

//			break;
//		case MotionEvent.ACTION_MOVE:
//			// Log.v("mandy", "action move......");
//			int moveX = (int) ev.getX();
//			int moveY = (int) ev.getY();
//
//			// Log.v("mandy", "x: " + moveX + "Y: " + moveY);
//
//			// 如果我们在按下的item上面移动，只要不超过item的边界我们就不移除mRunnable
//			if (!isTouchInItem(mStartDragItemView, moveX, moveY)) {
//				// Log.v("mandy", "remove call backs ...............");
//				mHandler.removeCallbacks(mLongClickRunnable);
//			}
			break;
		case MotionEvent.ACTION_UP:
			dragGridView.dispatchTouchEvent(ev);
			
//			handler.sendEmptyMessage(0);
//			Log.v("mandy", "action up......");
//			mHandler.removeCallbacks(mLongClickRunnable);
//			mHandler.removeCallbacks(mScrollRunnable);
			break;
		}
		return super.dispatchTouchEvent(ev);
	}

	public void scorllLocation (int moveX, int moveY) {
		
		smoothScrollBy(moveX, moveY);
		
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		
//		Log.v("mandy", "c moveX: " + ev.getX()  + "  moveY: " + ev.getY() );
//		Log.v("mandy", "gridView" + dragGridView.getWidth() + " : " + ev.getRawX());
//////		
//		int position = dragGridView.pointToPosition((int)ev.getX() * 2, (int)ev.getY());
//////		
//		Log.v("mandy", " position  " + position);
		
		
//		if(isDrag && mDragImageView != null){
			switch(ev.getAction()){
			case MotionEvent.ACTION_MOVE:
////				Log.v("mandy", "ontouchevent ACTION_MOVE");
				moveX = (int) ev.getX();
				moveY = (int) ev.getY();
//				
//				Log.v("mandy", "custom moveX: " + moveX + " custom moveY: " + moveY);
//				//拖动item
			
			Location location = new Location(moveX,moveY);
			
			Message message = new Message();
			message.obj = location;
			message.what = 0;
			handler.sendMessage(message);
//			mHandler.post(mScrollRunnable);
		
//				onDragItem(moveX, moveY);
				break;
			case MotionEvent.ACTION_UP:
				
				handler.sendEmptyMessage(1);
////				Log.v("mandy", "ontouchevent action up");
			
				break;
			}
//			return true;
//			return dragGridView.onTouchEvent(ev);
//		}
		return super.onTouchEvent(ev);
	}
	
	/**
	 * 停止拖拽我们将之前隐藏的item显示出来，并将镜像移除
	 */
	public void onStopDrag(){
		View view = getChildAt(mDragPosition);
		if(view != null){
			view.setVisibility(View.VISIBLE);
		}
		removeDragImage();
	}
	
	/**
	 * 是否点击在GridView的item上面
	 * @param itemView
	 * @param x
	 * @param y
	 * @return
	 */
	private boolean isTouchInItem(View dragView, int x, int y){
		
		if (dragView == null) {
		  return false;
		
		}
//		Log.v("mandy", "is touch in item, left: "+ dragView.getLeft()
//	'\'			+" right: " + dragView.getRight() + "top: " + dragView.getTop() + "bottom: " + dragView.getBottom());
		int leftOffset = dragView.getLeft();
		int topOffset = dragView.getTop();
		
		//判断是否是在item view 上
		if(x< leftOffset || x > leftOffset + dragView.getWidth()){
			return false;
		}
		
		if(y < topOffset || y > topOffset + dragView.getHeight()){
			return false;
		}
		
		return true;
	}
	
	
	
	public static class Location {
	   public int moveX;
	   public int moveY;
	   public Location (int moveX, int moveY) {
		   this.moveX = moveX;
		   this.moveY = moveY;
		   
	   }	
	}
	
	/**
	 * 获取状态栏的高度
	 * @param context
	 * @return
	 */
	private static int getStatusHeight(Context context){
        int statusHeight = 0;
        Rect localRect = new Rect();
        ((Activity) context).getWindow().getDecorView().getWindowVisibleDisplayFrame(localRect);
        statusHeight = localRect.top;
        if (0 == statusHeight){
            Class<?> localClass;
            try {
                localClass = Class.forName("com.android.internal.R$dimen");
                Object localObject = localClass.newInstance();
                int i5 = Integer.parseInt(localClass.getField("status_bar_height").get(localObject).toString());
                statusHeight = context.getResources().getDimensionPixelSize(i5);
            } catch (Exception e) {
                e.printStackTrace();
            } 
        }
        return statusHeight;
    }

}
