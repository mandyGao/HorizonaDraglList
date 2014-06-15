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
	 * DragGridView��item������Ӧ��ʱ�䣬 Ĭ����1000���룬Ҳ������������
	 */
	private long dragResponseMS = 1000;

	/**
	 * �Ƿ������ק��Ĭ�ϲ�����
	 */
	private boolean isDrag = false;

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
	private ImageView mDragImageView;

	private CustomHorizonalView customHorizonalView;

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

	/**
	 * DragGridView�Զ����¹����ı߽�ֵ
	 */
	private int mDownScrollBorder;

	/**
	 * DragGridView�Զ����Ϲ����ı߽�ֵ
	 */
	private int mUpScrollBorder;

	/**
	 * DragGridView�Զ��������ٶ�
	 */
	private static final int speed = 20;
	
	private int mRightScrollBorder;
	private int mLeftScrollBorder;
	private int parentMoveX;
	private int ParentMoveY;

	/**
	 * item�����仯�ص��Ľӿ�
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
		mStatusHeight = getStatusHeight(context); // ��ȡ״̬���ĸ߶�
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

	// ���������Ƿ�Ϊ������Runnable
	private Runnable mLongClickRunnable = new Runnable() {

		@Override
		public void run() {
			isDrag = true; // ���ÿ�����ק
			mVibrator.vibrate(50); // ��һ��
			mStartDragItemView.setVisibility(View.INVISIBLE);// ���ظ�item

			// �������ǰ��µĵ���ʾitem����
			createDragImage(mDragBitmap, mDownX, mDownY);
		}
	};

	/**
	 * ���ûص��ӿ�
	 * 
	 * @param onChanageListener
	 */
	public void setOnChangeListener(OnChanageListener onChanageListener) {
		this.onChanageListener = onChanageListener;
	}

	/**
	 * ������Ӧ��ק�ĺ�������Ĭ����1000����
	 * 
	 * @param dragResponseMS
	 */
	public void setDragResponseMS(long dragResponseMS) {
		this.dragResponseMS = dragResponseMS;
	}

	/**
	 * �Ƿ�����GridView��item����
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

		// �ж��Ƿ�����item view ��
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
	// //�϶�item
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
	 * �����϶��ľ���
	 * 
	 * @param bitmap
	 * @param downX
	 *            ���µĵ���Ը��ؼ���X����
	 * @param downY
	 *            ���µĵ���Ը��ؼ���X����
	 */
	private void createDragImage(Bitmap bitmap, int downX, int downY) {
		mWindowLayoutParams = new WindowManager.LayoutParams();
		mWindowLayoutParams.format = PixelFormat.TRANSLUCENT; // ͼƬ֮��������ط�͸��
		mWindowLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
		mWindowLayoutParams.x = downX - mPoint2ItemLeft + mOffset2Left;
		mWindowLayoutParams.y = downY - mPoint2ItemTop + mOffset2Top
				- mStatusHeight;
		mWindowLayoutParams.alpha = 0.55f; // ͸����
		mWindowLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
		mWindowLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
		mWindowLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
				| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;

		mDragImageView = new ImageView(getContext());
		mDragImageView.setImageBitmap(bitmap);
		mWindowManager.addView(mDragImageView, mWindowLayoutParams);
	}

	/**
	 * �ӽ��������ƶ��϶�����
	 */
	private void removeDragImage() {
		if (mDragImageView != null) {
			mWindowManager.removeView(mDragImageView);
			mDragImageView = null;
		}
	}

	/**
	 * �϶�item��������ʵ����item�����λ�ø��£�item���໥�����Լ�GridView�����й���
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
		mWindowManager.updateViewLayout(mDragImageView, mWindowLayoutParams); // ���¾����λ��
		onSwapItem(this.moveX, this.moveY);
		// Log.v("mandy", "on DragItem: " + moveX + "moveY:  " + moveY);
		// GridView�Զ�����
//		customHorizonalView.mHandler.post(customHorizonalView.mScrollRunnable);
		}
	}

	/**
	 * ��moveY��ֵ�������Ϲ����ı߽�ֵ������GridView�Զ����Ϲ��� ��moveY��ֵС�����¹����ı߽�ֵ������GridView�Զ����¹���
	 * ���򲻽��й���
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

			// �����ǵ���ָ����GridView���ϻ������¹�����ƫ������ʱ�򣬿���������ָû���ƶ�������DragGridView���Զ��Ĺ���
			// �������������������onSwapItem()����������item
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
			// ʹ��Handler�ӳ�dragResponseMSִ��mLongClickRunnable
			mHandler.postDelayed(mLongClickRunnable,
					dragResponseMS);

			mDownX = (int) ev.getX();
			mDownY = (int) ev.getY();

			// ���ݰ��µ�X,Y�����ȡ�����item��position
			mDragPosition = pointToPosition(mDownX, mDownY);
			
			Log.v("mandy", "mDragPosition: " + mDragPosition);

			if (mDragPosition == AdapterView.INVALID_POSITION) {
				return super.dispatchTouchEvent(ev);
			}

			// Log.v("mandy", "drag position: " + mDragPosition +
			// " fristVisible position: " + getFirstVisiblePosition());
			// ����position��ȡ��item����Ӧ��View
			mStartDragItemView = getChildAt(mDragPosition 
					);

		
			mPoint2ItemTop = mDownY - mStartDragItemView.getTop();
			mPoint2ItemLeft = mDownX - mStartDragItemView.getLeft();

			mOffset2Top = (int) (ev.getRawY() - mDownY);
			mOffset2Left = (int) (ev.getRawX() - mDownX);

			// ��ȡDragGridView�Զ����ҹ�����ƫ������С�����ֵ��DragGridView���ҹ���
			mRightScrollBorder = customHorizonalView.getWidth() / 4;

			// ��ȡDragGridView�Զ����ҹ�����ƫ�������������ֵ��DragGridView�������
			mLeftScrollBorder = customHorizonalView.getWidth() * 3 / 4;

//			Log.v("mandy", "gridview width: " + getWidth());

			// ����mDragItemView��ͼ����
			mStartDragItemView.setDrawingCacheEnabled(true);
			// ��ȡmDragItemView�ڻ����е�Bitmap����
			mDragBitmap = Bitmap.createBitmap(mStartDragItemView
					.getDrawingCache());
			// ��һ���ܹؼ����ͷŻ�ͼ���棬��������ظ��ľ���
			mStartDragItemView.destroyDrawingCache();

			break;
		case MotionEvent.ACTION_MOVE:
			// Log.v("mandy", "action move......");
			int moveX = (int) ev.getX();
			int moveY = (int) ev.getY();
			
//			Log.v("mandy", "mStartDragItemView...: " + mStartDragItemView);

			// Log.v("mandy", "x: " + moveX + "Y: " + moveY);

			// ��������ڰ��µ�item�����ƶ���ֻҪ������item�ı߽����ǾͲ��Ƴ�mRunnable
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
//	 //�϶�item
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
	 * ����item,���ҿ���item֮�����ʾ������Ч��
	 * 
	 * @param moveX
	 * @param moveY
	 */
	public void onSwapItem(int moveX, int moveY) {
		
		
//		Log.v("mandy", "onSwapItem.....");
		// ��ȡ������ָ�ƶ������Ǹ�item��position
		int tempPosition = pointToPosition(moveX, moveY);
//        Log.v("mandy", "tempPosition:  " + tempPosition);
		// ����tempPosition �ı��˲���tempPosition������-1,����н���
		if (tempPosition != mDragPosition
				&& tempPosition != AdapterView.INVALID_POSITION) {
			if (onChanageListener != null) {
				Log.v("mandy", "on swap item");
				onChanageListener.onChange(mDragPosition, tempPosition);
			}

			getChildAt(tempPosition - getFirstVisiblePosition()).setVisibility(
					View.INVISIBLE);// �϶������µ�item,�µ�item���ص�
//			getChildAt(tempPosition).setVisibility(View.INVISIBLE);
//			getChildAt(mDragPosition).setVisibility(View.VISIBLE);
//			
			getChildAt(mDragPosition - getFirstVisiblePosition())
					.setVisibility(View.VISIBLE);// ֮ǰ��item��ʾ����

			mDragPosition = tempPosition;
		}
	}

	/**
	 * ֹͣ��ק���ǽ�֮ǰ���ص�item��ʾ���������������Ƴ�
	 */
	private void onStopDrag() {
		View view = getChildAt(mDragPosition - getFirstVisiblePosition());
		if (view != null) {
			view.setVisibility(View.VISIBLE);
		}
		removeDragImage();
	}

	/**
	 * ��ȡ״̬���ĸ߶�
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
		 * ��item����λ�õ�ʱ��ص��ķ���������ֻ��Ҫ�ڸ÷�����ʵ�����ݵĽ�������
		 * 
		 * @param form
		 *            ��ʼ��position
		 * @param to
		 *            ��ק����position
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
