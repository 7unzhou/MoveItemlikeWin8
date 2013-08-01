package com.example.moveitem.view;

import com.example.moveitem.Configure;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View.MeasureSpec;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

public class ScrollContent extends HorizontalScrollView {

	private final String TAG = "ScrollContent";

	public ScrollContent(Context context, AttributeSet attrs) {

		super(context, attrs);

		Log.d(TAG, TAG);

	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {

		int action = ev.getAction();

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			//Log.d(TAG, "onInterceptTouchEvent action:ACTION_DOWN");
			//return true;
			break;

		case MotionEvent.ACTION_MOVE:
			//Log.d(TAG, "onInterceptTouchEvent action:ACTION_MOVE");
			break;

		case MotionEvent.ACTION_UP:
			//Log.d(TAG, "onInterceptTouchEvent action:ACTION_UP");
			break;

		case MotionEvent.ACTION_CANCEL:
			//Log.d(TAG, "onInterceptTouchEvent action:ACTION_CANCEL");
			break;

		}
		return false;
	}

//	@Override
//	public boolean onTouchEvent(MotionEvent ev) {
//		int action = ev.getAction();
//		switch (action) {
//		case MotionEvent.ACTION_DOWN:
//			Log.d(TAG, "onTouchEvent action:ACTION_DOWN");
//			break;
//		case MotionEvent.ACTION_MOVE:
//			Log.d(TAG, "onTouchEvent action:ACTION_MOVE");
//			break;
//		case MotionEvent.ACTION_UP:
//			Log.d(TAG, "onTouchEvent action:ACTION_UP");
//			break;
//		case MotionEvent.ACTION_CANCEL:
//			Log.d(TAG, "onTouchEvent action:ACTION_CANCEL");
//			break;
//		}
//		return true;
//	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		super.onLayout(changed, l, t, r, b);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		//Log.i(TAG, "--- start onMeasure --");
		// 设置该ScrollContent的大小
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
//		int width = MeasureSpec.getSize(widthMeasureSpec);
//		int height = MeasureSpec.getSize(heightMeasureSpec);
//		int width = Configure.columCount*7;
//		setMeasuredDimension(width, height);
//		System.out.println("width:"+width+"  heigth:" + height);
		
	}

}
