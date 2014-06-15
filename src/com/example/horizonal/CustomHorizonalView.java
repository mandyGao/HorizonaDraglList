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
	 * DragGridView��item������Ӧ��ʱ�䣬 Ĭ����1000���룬Ҳ������������
	 */
	private long dragResponseMS = 1000;
	
	private Handler handler;

	/**
	 * �Ƿ������ק��Ĭ�ϲ�����
	 */
	public boolean isDrag = false;

	private int mDownX;
	private int mDownY;
	private int moveX;
	private int moveY;
	/**
	 * ������ק��position
	 */
	private int mDragPosition;

	/**
	 * �տ�ʼ��ק��item��Ӧ��View
	 */
	private View mStartDragItemView = null;

	/**
	 * ������ק�ľ�������ֱ����һ��ImageView
	 */
	public ImageView mDragImageView;
	
	
	/**
	 * ����
	 */
	private Vibrator mVibrator;
	

	private WindowManager mWindowManager;
	/** 
	 * item����Ĳ��ֲ���
	 */
	private WindowManager.LayoutParams mWindowLayoutParams;

	/**
	 * ������ק��item��Ӧ��Bitmap
	 */
	private Bitmap mDragBitmap;

	/**
	 * ���µĵ㵽����item���ϱ�Ե�ľ���
	 */
	private int mPoint2ItemTop;

	/**
	 * ���µĵ㵽����item�����Ե�ľ���
	 */
	private int mPoint2ItemLeft;

	/**
	 * DragGridView������Ļ������ƫ����
	 */
	private int mOffset2Top;

	/**
	 * DragGridView������Ļ��ߵ�ƫ����
	 */
	private int mOffset2Left;

	/**
	 * ״̬���ĸ߶�
	 */
	private int mStatusHeight;


	private int mRightScrollBorder;

	private int mLeftScrollBorder;

	/**
	 * DragGridView�Զ��������ٶ�
	 */
	private static final int speed = 20;
	/**
	 * item�����仯�ص��Ľӿ�
	 */
	private OnChanageListener onChanageListener;
//	private int gridMoveX;
//	private int gridMoveY;
	
	/**
	 * ���ûص��ӿ�
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
		mStatusHeight = getStatusHeight(context); //��ȡ״̬���ĸ߶�
		 
		// mVibrator = (Vibrator)
		// context.getSystemService(Context.VIBRATOR_SERVICE);
		// mWindowManager = (WindowManager)
		// context.getSystemService(Context.WINDOW_SERVICE);
		// mStatusHeight = getStatusHeight(context); //��ȡ״̬���ĸ߶�
		// setOnItemLongClickListener(this);

	}
	
	

	public void setHandler(Handler handler) {
		this.handler = handler;
	}

	public Handler mHandler = new Handler();

	// ���������Ƿ�Ϊ������Runnable
	private Runnable mLongClickRunnable = new Runnable() {

		@Override
		public void run() {
			
			Log.v("mandy", "hide item view");
			isDrag = true; // ���ÿ�����ק
			mVibrator.vibrate(50); // ��һ��
			mStartDragItemView.setVisibility(View.INVISIBLE);// ���ظ�item

			// �������ǰ��µĵ���ʾitem����
			createDragImage(mDragBitmap, mDownX, mDownY);
		}
	};
	
	/**
	 * �����϶��ľ���
	 * @param bitmap 
	 * @param downX
	 * 			���µĵ���Ը��ؼ���X����
	 * @param downY
	 * 			���µĵ���Ը��ؼ���X����
	 */
	private void createDragImage(Bitmap bitmap, int downX , int downY){
		mWindowLayoutParams = new WindowManager.LayoutParams();
		mWindowLayoutParams.format = PixelFormat.TRANSLUCENT; //ͼƬ֮��������ط�͸��
		mWindowLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
		mWindowLayoutParams.x = downX - mPoint2ItemLeft + mOffset2Left;
		mWindowLayoutParams.y = downY - mPoint2ItemTop + mOffset2Top - mStatusHeight;
		mWindowLayoutParams.alpha = 0.55f; //͸����
		mWindowLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;  
		mWindowLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;  
		mWindowLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE  
	                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE ;
		  
		mDragImageView = new ImageView(getContext());  
		mDragImageView.setImageBitmap(bitmap);  
		mWindowManager.addView(mDragImageView, mWindowLayoutParams);  
	}
	
	/**
	 * �ӽ��������ƶ��϶�����
	 */
	private void removeDragImage(){
		if(mDragImageView != null){
			mWindowManager.removeView(mDragImageView);
			mDragImageView = null;
		}
	}
	
	/**
	 * �϶�item��������ʵ����item�����λ�ø��£�item���໥�����Լ�GridView�����й���
	 * @param x
	 * @param y
	 */
	public void onDragItem(int moveX, int moveY){
		if (mWindowLayoutParams == null || mDragImageView == null) {
			return;
		}
		mWindowLayoutParams.x = moveX - mPoint2ItemLeft + mOffset2Left;
		mWindowLayoutParams.y = moveY - mPoint2ItemTop + mOffset2Top - mStatusHeight;
		mWindowManager.updateViewLayout(mDragImageView, mWindowLayoutParams); //���¾����λ��
		onSwapItem(moveX, moveY);
		
//		Log.v("mandy", "on DragItem: " + moveX + "moveY:  " + moveY);
		//GridView�Զ�����
		mHandler.post(mScrollRunnable);
	}
	
	/**
	 * ��moveY��ֵ�������Ϲ����ı߽�ֵ������GridView�Զ����Ϲ���
	 * ��moveY��ֵС�����¹����ı߽�ֵ������GridView�Զ����¹���
	 * ���򲻽��й���
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
			
			//�����ǵ���ָ����GridView���ϻ������¹�����ƫ������ʱ�򣬿���������ָû���ƶ�������DragGridView���Զ��Ĺ���
			//�������������������onSwapItem()����������item
//			onSwapItem(moveX, moveY);
			dragGridView.onSwapItem(0,0);
			
//			handler.sendEmptyMessage(2);
			
//			smoothScrollBy(moveX, scrollY);
			
			smoothScrollBy(scrollY, 10);
		}
	};
	
	
	/**
	 * ����item,���ҿ���item֮�����ʾ������Ч��
	 * @param moveX
	 * @param moveY
	 */
	private void onSwapItem(int moveX, int moveY){
		//��ȡ������ָ�ƶ������Ǹ�item��position
		int tempPosition = dragGridView.pointToPosition(moveX, moveY);
		
//		Log.v("mandy", "temp position: " + tempPosition);
		
		//����tempPosition �ı��˲���tempPosition������-1,����н���
		if(tempPosition != mDragPosition && tempPosition != AdapterView.INVALID_POSITION){
			if(onChanageListener != null){
				onChanageListener.onChange(mDragPosition, tempPosition);
			}
			dragGridView.getChildAt(tempPosition).setVisibility(View.INVISIBLE);//�϶������µ�item,�µ�item���ص�
			dragGridView.getChildAt(mDragPosition).setVisibility(View.VISIBLE);//֮ǰ��item��ʾ����
			
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
			// ʹ��Handler�ӳ�dragResponseMSִ��mLongClickRunnable
//			mHandler.postDelayed(mLongClickRunnable,
//					dragResponseMS);
//
//			mDownX = (int) ev.getX();
//			mDownY = (int) ev.getY();
//
//			// ���ݰ��µ�X,Y�����ȡ�����item��position
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
//			// ����position��ȡ��item����Ӧ��View
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
//			// ��ȡDragGridView�Զ����ҹ�����ƫ������С�����ֵ��DragGridView���ҹ���
			mRightScrollBorder = getWidth() / 4;
//
//			// ��ȡDragGridView�Զ����ҹ�����ƫ�������������ֵ��DragGridView�������
			mLeftScrollBorder = getWidth() * 3 / 4;
//
////			Log.v("mandy", "gridview width: " + getWidth());
//
//			// ����mDragItemView��ͼ����
//			mStartDragItemView.setDrawingCacheEnabled(true);
//			// ��ȡmDragItemView�ڻ����е�Bitmap����
//			mDragBitmap = Bitmap.createBitmap(mStartDragItemView
//					.getDrawingCache());
//			// ��һ���ܹؼ����ͷŻ�ͼ���棬��������ظ��ľ���
//			mStartDragItemView.destroyDrawingCache();

//			break;
//		case MotionEvent.ACTION_MOVE:
//			// Log.v("mandy", "action move......");
//			int moveX = (int) ev.getX();
//			int moveY = (int) ev.getY();
//
//			// Log.v("mandy", "x: " + moveX + "Y: " + moveY);
//
//			// ��������ڰ��µ�item�����ƶ���ֻҪ������item�ı߽����ǾͲ��Ƴ�mRunnable
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
//				//�϶�item
			
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
	 * ֹͣ��ק���ǽ�֮ǰ���ص�item��ʾ���������������Ƴ�
	 */
	public void onStopDrag(){
		View view = getChildAt(mDragPosition);
		if(view != null){
			view.setVisibility(View.VISIBLE);
		}
		removeDragImage();
	}
	
	/**
	 * �Ƿ�����GridView��item����
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
		
		//�ж��Ƿ�����item view ��
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
	 * ��ȡ״̬���ĸ߶�
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
