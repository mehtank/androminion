package com.mehtank.androminion.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;

public class DragNDropListView extends ListView {

	boolean mDragMode;

	int mStartPosition;
	int mCurrentPosition;
	int mEndPosition;
	int mDragPointYOffset;		//Used to adjust drag view location
	int mDragPointXOffset;		//Used to adjust drag view location
	int mOrigX;
	
	ImageView mDragView;
	View mHiddenView;
	GestureDetector mGestureDetector;
	
	DragListener mDragListener;

	public DragNDropListView(Context context) {
		super(context);
	}

	public DragNDropListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public void setDropListener(DragListener l) {
		mDragListener = l;
	}

	private void debug(String s) {
		Log.e("DragNDrop", s);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		final int action = ev.getAction();
		final int x = (int) ev.getX();
		final int y = (int) ev.getY();	
		

		if (action == MotionEvent.ACTION_DOWN ||
			action == MotionEvent.ACTION_MOVE) {
			mDragMode = true;
		}

		if (!mDragMode) 
			return super.onTouchEvent(ev);

		switch (action) {
			case MotionEvent.ACTION_MOVE:
				if (mDragView != null) {
					drag(x,y);
					break;
				}
			case MotionEvent.ACTION_DOWN:
				mStartPosition = pointToPosition(x,y);
				if (mStartPosition != INVALID_POSITION) {
					mOrigX = x;
					int mItemPosition = mStartPosition - getFirstVisiblePosition();
                    mDragPointYOffset = y - getChildAt(mItemPosition).getTop();
                    mDragPointYOffset -= ((int)ev.getRawY()) - y;
                    mDragPointXOffset = x - getChildAt(mItemPosition).getLeft();
                    mDragPointXOffset -= ((int)ev.getRawX()) - x;
					startDrag(mItemPosition,y);
					mCurrentPosition = mItemPosition;
					drag(x,y);
				}	
				break;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
			default:
				notifyDrag(x, y);
				stopDrag();
				mDragMode = false;
				break;
		}
		return true;
	}

	private void notifyDrag(int x, int y) {
		mEndPosition = pointToPosition(mOrigX, y);

		if (mHiddenView != null) 
			mHiddenView.setVisibility(VISIBLE);
		mHiddenView = getChildAt(mCurrentPosition);
		if (mHiddenView != null) 
			mHiddenView.setVisibility(INVISIBLE);

		if (mCurrentPosition == mEndPosition) 
			return;
		if (mDragListener != null && mCurrentPosition != INVALID_POSITION && mEndPosition != INVALID_POSITION) {
			if (mHiddenView != null) 
				mHiddenView.setVisibility(VISIBLE);

			mDragListener.onDrag(mCurrentPosition, mEndPosition);
			mCurrentPosition = mEndPosition;

		}
	}	
	
	// move the drag view
	private void drag(int x, int y) {
		WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams)mDragView.getLayoutParams();
		layoutParams.x = x - mDragPointXOffset;   
		layoutParams.y = y - mDragPointYOffset;
        WindowManager mWindowManager = (WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE);
        mWindowManager.updateViewLayout(mDragView, layoutParams);
        
		notifyDrag(x, y);
	}

	// enable the drag view for dragging
	private void startDrag(int itemIndex, int y) {
		stopDrag();

		View item = getChildAt(itemIndex);
		if (item == null) return;
		item.setDrawingCacheEnabled(true);
		
        // Create a copy of the drawing cache so that it does not get recycled
        // by the framework when the list tries to clean up memory
        Bitmap bitmap = Bitmap.createBitmap(item.getDrawingCache());
        
        WindowManager.LayoutParams mWindowParams = new WindowManager.LayoutParams();
        mWindowParams.gravity = Gravity.TOP;
        mWindowParams.x = 0 - mDragPointXOffset;
        mWindowParams.y = y - mDragPointYOffset;

        mWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        mWindowParams.format = PixelFormat.TRANSLUCENT;
        mWindowParams.windowAnimations = 0;
        
        Context context = getContext();
        ImageView v = new ImageView(context);
        v.setImageBitmap(bitmap);      

        WindowManager mWindowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        mWindowManager.addView(v, mWindowParams);
        mDragView = v;
        mHiddenView = item;
		mHiddenView.setVisibility(INVISIBLE);
	}

	// destroy drag view
	private void stopDrag() {
		if (mHiddenView != null) {
			mHiddenView.setVisibility(VISIBLE);
			mHiddenView = null;
		}
		if (mDragView != null) {
            mDragView.setVisibility(GONE);
            WindowManager wm = (WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE);
            wm.removeView(mDragView);
            mDragView.setImageDrawable(null);
            mDragView = null;
        }
	}

	public interface DragListener {
		void onDrag(int from, int to);
	}

}
