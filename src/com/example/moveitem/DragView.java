package com.example.moveitem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

import com.example.moveitem.view.SingleView;

import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

public class DragView extends RelativeLayout {
	private static final String TAG = "DragView";

	View parent;

	private HashMap culomMap = new HashMap<Integer, TreeMap<Integer, BaseView>>();
	private ArrayList<BaseView> viewIdList = new ArrayList<BaseView>();

	private int mLastMotionX, mLastMotionY;
	private int mLastRawX, mLastRawY;

	private WindowManager windowManager = null;
	private WindowManager.LayoutParams windowParams = null;

	private ImageView dragImageView = null;

	// 是否移动了
	private boolean isMoved;
	// 长按的runnable
	private Runnable mLongPressRunnable;
	// 移动的阈值
	private static final int TOUCH_SLOP = 5;

	protected static final int SCROLL_CONTENT = 400;

	private ArrayList<BaseView> mViews;

	private Listener mListener;

	private BaseView currentItem;

	private Vibrator mVibrator;
	private int[] xy = new int[2];

	protected static final int SETVIEWPARAMS = 300;
	protected static final int SAVE_DATE = 301;
	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case SCROLL_CONTENT:
				// getSaveView();
				parent.scrollTo(Configure.columWidth, 0);
				break;
			case SETVIEWPARAMS:
				HashMap prefMap = getSaveView() ;
				if (null != prefMap && prefMap.size() > 0) {
					Log.d(TAG, "draw item by preference");
					drawItemView(prefMap,true);
				}else{
					Log.d(TAG, "draw item by init");
					drawItemView(culomMap,false);
				}
				break;
			case SAVE_DATE:
					saveView();
				break;
			default:
				break;
			}
		};
	};

	public DragView(Context context) {
		super(context);

		mViews = new ArrayList<BaseView>();

		mVibrator = (Vibrator) context
				.getSystemService(Service.VIBRATOR_SERVICE);
		mLongPressRunnable = new Runnable() {
			@Override
			public void run() {
				// performLongClick();
				Log.d(TAG, "+++++++++++++++perform LongClick ++++++++++++++");
				isMoved = true;
				long[] l = new long[] { 0, 100 };
				// 周期振动,索引0或偶数为间隔时间，索引为单数为振动时间（毫秒）
				mVibrator.vibrate(l, -1);

				// 移除长按选中的view在队列中的存储
				removeView(checkColumIndex(mLastMotionX, mLastMotionY),
						mLastMotionX, mLastMotionY);

				startDrap(currentItem, mLastRawX, mLastRawY);
			}

		};

	}

	public void setItemListener(Listener listener) {
		this.mListener = listener;
	}

//	@Override
//	public boolean onInterceptTouchEvent(MotionEvent ev) {
//		int action = ev.getAction();
//		int x = (int) ev.getX();
//		int y = (int) ev.getY();
//		switch (action) {
//		case MotionEvent.ACTION_DOWN:
//			//if (null != getTouchItem(x, y));
//			onTouchEvent(ev);
//			return true;
//		}
//		
//		return false;
//	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int x = (int) event.getX();
		int y = (int) event.getY();
		int rawX = (int) event.getRawX();
		int rawY = (int) event.getRawY();
		int action = event.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			currentItem = getTouchItem(x, y);

			if (currentItem == null) {
				return false;
			}

			isMoved = false;
			postDelayed(mLongPressRunnable,
					ViewConfiguration.getLongPressTimeout());

			RelativeLayout.LayoutParams params = (LayoutParams) currentItem
					.getLayoutParams();
			xy[0] = params.leftMargin;
			xy[1] = params.topMargin;

			mLastMotionX = x;
			mLastMotionY = y;

			mLastRawX = rawX;
			mLastRawY = rawY;
			break;
		case MotionEvent.ACTION_MOVE:
			if (!isMoved) {
				break;
			}
			System.out.println("Math.abs(mLastMotionX - x):"+Math.abs(mLastMotionX - x));
			System.out.println("Math.abs(mLastMotionY - y):"+Math.abs(mLastMotionY - y));
//
//			if (Math.abs(mLastMotionX - x) > TOUCH_SLOP
//					|| Math.abs(mLastMotionY - y) > TOUCH_SLOP
//					&&getTouchItem(x,y)!=currentItem ) 
//			{
//				//
//				// //移动超过阈值，则表示移动了
//				// removeCallbacks(mLongPressRunnable);
//				System.out.println("移动超过阈值，则表示移动了");
//				removeCallbacks(mLongPressRunnable);
//				//return false;
//				super.onTouchEvent(event);
//				break;
//			}


			RelativeLayout.LayoutParams params1 = (LayoutParams) currentItem
					.getLayoutParams();
			params1.leftMargin = xy[0] + (x - mLastMotionX);
			params1.topMargin = xy[1] + (y - mLastMotionY);

			// 拖动view的越界判断
			if (params1.leftMargin < 0) {
				params1.leftMargin = 0;
			}
			if (params1.topMargin < 0) {
				params1.topMargin = 0;
			}
			// if (params1.leftMargin + currentItem.getWidth() >
			// Configure.deviceWidth-10) {
			// System.out.println("raw x:"+(rawX + currentItem.getWidth()/2));

			// 边界溢出处理
			if (x + currentItem.getWidth() / 2 > this.getWidth()) {
				Log.d(TAG, "move item is in the width border");
				params1.leftMargin = this.getWidth() - currentItem.getWidth();
			}

			if (rawX + currentItem.getWidth() / 2 > Configure.deviceWidth - 10) {
				// params1.leftMargin = Configure.deviceWidth
				// - currentItem.getWidth();
				System.out.println("scroll");
				// handler.sendEmptyMessage(SCROLL_CONTENT);
				// i++;
			}

			if (params1.topMargin + currentItem.getHeight() > Configure.deviceHeight) {
				params1.topMargin = Configure.deviceHeight
						- currentItem.getHeight();
			}

			currentItem.setLayoutParams(params1);
			// currentItem.setVisibility(View.INVISIBLE);

			onDrag(rawX, rawY);

			// 做动画

			break;
		case MotionEvent.ACTION_UP:
			Log.i(TAG, "ACTION_UP");
			currentItem.setVisibility(View.VISIBLE);
			removeCallbacks(mLongPressRunnable);
			currentItem.onTouchEvent(event);
			if (mListener != null && !isMoved
					&& currentItem == getTouchItem(x, y)) {
				mListener.onItemClick(currentItem);
			}

			if (isMoved) {
				// Log.i("eeeeees", " view is add to rectList");
				if (y > Configure.deviceHeight) { // 当快速移动的时候y坐标会超过屏幕的高度，需修改y的最大值
					y = Configure.deviceHeight - 1;
				}
				int colum = checkColumIndex(x, y);
				// 检查在列中的那个位置
				int viewIndex = checkIndexInsert(colum, x, y);
				if (viewIndex == -1) {
					addRectToCulom(colum, currentItem);
				} else {
					// Log.i(TAG, "insertToPosition");
					insertToPosition(colum, viewIndex, currentItem);

				}

				stopDrag();
				// currentItem.setLayoutParams(params1);
			}
			isMoved = false;

			mVibrator.cancel();
			break;
		}
		// Log.i("eeeeees", "xxxxxxxxxxxxxxxx");
		return true;
	}

	/**
	 * 开始拖拽，生成一个跟随触摸点的图标
	 * 
	 * @param view
	 *            要拖拽的控件
	 * @param x
	 *            触摸点x
	 * @param y
	 *            触摸点y
	 */
	private void startDrap(View view, int x, int y) {
		windowParams = new WindowManager.LayoutParams();
		windowParams.gravity = Gravity.TOP | Gravity.LEFT;
		windowParams.x = view.getLeft();
		windowParams.y = y - view.getHeight() / 2;
		windowParams.alpha = 0.8f;
		windowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
		windowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;

		view.destroyDrawingCache();
		view.setDrawingCacheEnabled(true);
		view.setDrawingCacheBackgroundColor(0x000000);
		Bitmap bm = Bitmap.createBitmap(view.getDrawingCache(true));
		Bitmap bitmap = Bitmap.createBitmap(bm, 10, 0, bm.getWidth() - 10,
				bm.getHeight());
		ImageView iv = new ImageView(getContext());

		iv.setImageBitmap(bm);

		windowManager = (WindowManager) getContext().getSystemService(
				Context.WINDOW_SERVICE);
		windowManager.addView(iv, windowParams);

		view.setVisibility(View.INVISIBLE);
		dragImageView = iv;

	}

	/**
	 * 拖拽过程中，更改拖拽图标的参数
	 * 
	 * @param x
	 *            移动后的x坐标
	 * @param y
	 *            移动后的y坐标
	 */
	private void onDrag(int x, int y) {
		if (dragImageView != null) {
			windowParams.alpha = 0.8f;
			windowParams.x = x - currentItem.getWidth() / 2 + 8;
			windowParams.y = y - currentItem.getHeight() / 2;
			windowManager.updateViewLayout(dragImageView, windowParams);
		}
	}

	/**
	 * 拖动结束，移除拖拽图标
	 */
	private void stopDrag() {
		if (dragImageView != null) {
			windowManager.removeView(dragImageView);
			dragImageView = null;
		}
	}

	// 在列表的第index位置插入当前移动的view
	private void insertToPosition(int colum, int viewIndex, BaseView insertView) {
		if (viewIndex == -1) {
			addRectToCulom(colum, insertView);
		}
		ArrayList<Rect> rectList = Configure.rectListArray.get(colum - 1);
		ArrayList<BaseView> viewList = Configure.viewListArray.get(colum - 1);

		Rect insertIndexRect = rectList.get(viewIndex);
		BaseView insertIndexView = viewList.get(viewIndex);
		Rect lastListRect = rectList.get(rectList.size() - 1);
 
		// viewList.
		boolean isOutHeight = isAddViewOut(insertIndexRect, insertView)
				|| isAddViewOut(lastListRect, insertView);
		

		// System.out.println("insertIndexRect.right:"+insertIndexRect.right);
		// System.out.println("columWidth*colum :"+Configure.columWidth*colum);
		// 在viewindex的位置插入当前view
		// 如果2个view的宽度超过列的宽度,或者拖动停留在的viewlist中的第一项,则将view置在最后
		if ((isOutCulomWidht(insertView, insertIndexView)) && insertIndexView.hasFriend){
			//当前拖动的view和拖动到的view宽度和大于列宽，且拖动停留的view是在左边，则当前拖动的view添加在下发
			viewIndex = viewIndex+1;
			if(viewIndex >= viewList.size()){
				addRectToCulom(colum, insertView);
			}else{
				addViewToNextLine(rectList, viewList, viewIndex, colum, insertView);  
			}
		}
		else if (isOutCulomWidht(insertView, insertIndexView)) {
			addViewToNextLine(rectList, viewList, viewIndex, colum, insertView);
		} else if (insertIndexRect.right < Configure.columWidth * colum) // 拖动所在在view有个右friend
		{
			Log.d(TAG, "index view has a right friend");
			addViewToNextLine(rectList, viewList, viewIndex, colum, insertView);
		} else {
			// 如果2个view的宽度没有超过列的宽度则将view置在同一行
			addViewInSameLine(rectList, viewList, viewIndex, colum, insertView);

		}
		checkLastItem(rectList, colum);
		
//		if (isOutHeight && isOutCulomWidht(insertView, insertIndexView)) {
//			// 当加上要移动的view后该列超过屏幕且和所在的view超过屏幕的宽度
//			// 将原来的最后一项移动到下一列
//			// Log.d(TAG, "insert to position:" + viewIndex +
//			// " is out of height");
//			System.out.println("before move to next culom:"+viewList.size());
//			moveToNextCulom(rectList.size() - 1, colum);
//			System.out.println("after move to next culom:"+viewList.size());
//		}
	}

	private void addViewInSameLine(ArrayList<Rect> rectList,
			ArrayList<BaseView> viewList, int viewIndex, int colum,
			BaseView insertView) {
		Log.d(TAG, "addViewInSameLine");
		RelativeLayout.LayoutParams params = (LayoutParams) insertView
				.getLayoutParams();
		BaseView insertIndexView = viewList.get(viewIndex);
		Rect insertIndexRect = rectList.get(viewIndex);
		if (insertIndexView.hasFriend
				&& !isOutCulomWidht(insertView, insertIndexView)) { // 如果拖动的图标是single并且是在左边，则下移
			// itemDownLine(rectList, viewList, viewIndex,currentItem);
			Log.d(TAG, "如果拖动的图标是single并且是在左边，则下移");
			RelativeLayout.LayoutParams fParams = (LayoutParams) insertIndexView
					.getLayoutParams();
			fParams.leftMargin = fParams.leftMargin - fParams.width;
			insertIndexView.hasFriend = false;
			params.leftMargin = insertIndexRect.left;
			params.topMargin = insertIndexRect.top;

			if (viewIndex >= rectList.size()) {
				viewIndex = rectList.size() - 1;
			}
			Rect fRect = new Rect(insertIndexRect.left, insertIndexRect.top,
					insertIndexRect.right, insertIndexRect.bottom);
			rectList.add(viewIndex, fRect);

			Log.d(TAG, "current drag view has friend frect :" + fRect);
			insertView.hasFriend = true;
			viewList.add(viewIndex, insertView);

			int index = viewIndex + 1;
			rectList.get(index).left = (colum - 1) * Configure.columWidth;
			for (; index < rectList.size(); index++) {
				itemDownLine(rectList, viewList, index, insertView);
			}
			// insertToPosition(colum, viewIndex, currentItem);
		} else {

			params.leftMargin = insertIndexRect.left
					+ insertIndexView.getWidth();
			params.topMargin = insertIndexRect.top;

			if (viewIndex >= rectList.size()) {
				viewIndex = rectList.size() - 1;
			}
			rectList.add(viewIndex + 1, new Rect(Configure.columWidth
					* (colum - 1) + insertIndexView.getWidth(),
					insertIndexRect.top, Configure.columWidth * colum,
					insertIndexRect.top + insertView.getHeight()));

			rectList.get(viewIndex).right = Configure.columWidth * (colum - 1)
					+ insertView.getWidth();

			Log.d(TAG, "current drag view has friend");
			insertView.hasFriend = true;
			viewList.add(viewIndex + 1, insertView);
		}
		insertView.setLayoutParams(params);

	}

	private void addViewToNextLine(ArrayList<Rect> rectList,
			ArrayList<BaseView> viewList, int viewIndex, int colum,
			BaseView insertView) {
		Log.d(TAG, "addViewToNextLine");
		Rect insertRect = setInserRect(rectList, viewList, viewIndex,
				insertView, colum);		//错误了！！！！！
		RelativeLayout.LayoutParams params = setInsertViewParams(colum,
				viewIndex, insertView, rectList);

		BaseView insertIndexView = viewList.get(viewIndex);
		// 如果该view是拥有friend的则friend 也要下移
		if (insertIndexView.hasFriend && viewIndex != 0) {
			// viewList.get(i-1)
			Log.d(TAG, "view index:" + viewIndex + "has friend");
			itemDownLine(rectList, viewList, viewIndex - 1, insertView);
		}

		if (!(viewIndex == 0 && viewList.get(viewIndex).hasFriend)) {
			Log.d(TAG, " view index is no zero or has no friend");
			int index = viewIndex;
			// 将要viewindex之后的项下移
			for (; index < rectList.size(); index++) {
				itemDownLine(rectList, viewList, index, insertView);
			}
		}
		rectList.add(viewIndex, insertRect);
		viewList.add(viewIndex, insertView);
		insertView.setLayoutParams(params);
	}

	private LayoutParams setInsertViewParams(int colum, int viewIndex,
			BaseView insertView, ArrayList<Rect> rectList) {
		RelativeLayout.LayoutParams params = (LayoutParams) insertView
				.getLayoutParams();
		Rect insertIndexRect = rectList.get(viewIndex);
		if (viewIndex == 0) { // 如果插入的位置为列的第一项
			params.leftMargin = Configure.columRect.get(colum).left;
			params.topMargin = 0;
		} else { // 插入的位置不是为列的第一项
			params.leftMargin = Configure.columRect.get(colum).left;
			params.topMargin = insertIndexRect.top;
		}
		return params;
	}

	private Rect setInserRect(ArrayList<Rect> rectList,
			ArrayList<BaseView> viewList, int viewIndex, BaseView insertView,
			int colum) {
		Rect insertRect;
		Rect insertIndexRect = rectList.get(viewIndex);
		BaseView insertIndexView = viewList.get(viewIndex);
		if (viewIndex == 0) { // 如果插入的位置为列的第一项
			// 来至移动至下一列方法，插入到第一条中去
			if (viewList.get(viewIndex).hasFriend) { // 如果该view是来至移动到下列的，并且有friend
														// view
				insertRect = setFriendViewForMoveNext(rectList, viewList,
						viewIndex, insertView, colum);
			} else {
				insertRect = new Rect(Configure.columWidth * (colum - 1), 0,
						Configure.columWidth * colum, insertView.getHeight());
			}
		} else { // 插入的位置不是为列的第一项
			insertRect = new Rect(Configure.columWidth * (colum - 1),
					insertIndexRect.top, Configure.columWidth * colum,
					insertView.getHeight() + insertIndexRect.top);
		}
		return insertRect;
	}

	private boolean isOutCulomWidht(View view1, View view2) {
		return view1.getLayoutParams().width + view2.getLayoutParams().width > Configure.columWidth;
	}

	/**
	 * 将有friend的view下移
	 * 
	 * @param rectList
	 * @param viewList
	 * @param index
	 * @param view
	 * @param colum
	 * @return
	 */
	private Rect setFriendViewForMoveNext(ArrayList<Rect> rectList,
			ArrayList<BaseView> viewList, int index, BaseView view, int colum) {
		// index =0
		Log.d(TAG, "setFriendViewForMoveNext");
		RelativeLayout.LayoutParams params = (LayoutParams) view
				.getLayoutParams();
		params.leftMargin = Configure.columRect.get(colum).left;
		params.topMargin = 0;
		Rect insertRect = new Rect(Configure.columWidth * (colum - 1), 0,
				Configure.columWidth * (colum - 1) + view.getWidth(),
				view.getHeight());

		rectList.get(index).left = insertRect.right;
		RelativeLayout.LayoutParams friendParams = (LayoutParams) viewList.get(
				index).getLayoutParams();
		friendParams.leftMargin = friendParams.leftMargin + view.getWidth();
		viewList.get(index).setLayoutParams(friendParams);

		return insertRect;
	}

	// }

	/**
	 * 将view下移一个baseview的高度
	 */
	private void itemDownLine(ArrayList<Rect> rectList,
			ArrayList<BaseView> viewList, int index, BaseView baseView) {
		// Log.d(TAG, "将 view index:"+baseView.getId()+"之后的项下移:" +
		// baseView.getHeight());
		LayoutParams params1 = (LayoutParams) viewList.get(index)
				.getLayoutParams();
		// System.out.println("lastrect:"+lastRect.toString());
		// System.out.println(" before lastRect.top:"+lastRect.top);
		params1.topMargin = rectList.get(index).top + baseView.getHeight();
		viewList.get(index).setLayoutParams(params1);

		rectList.get(index).top = rectList.get(index).top
				+ baseView.getHeight();
		rectList.get(index).bottom = rectList.get(index).bottom
				+ baseView.getHeight();
		// Log.d(TAG, "rectList.get("+index+"):"+rectList.get(index));
	}

	/**
	 * 检查当前的触摸点在列中的那个位置
	 * 
	 * @param colum
	 *            列数
	 * @param x
	 *            触摸点X坐标
	 * @param y
	 *            触摸点y坐标
	 * @return
	 */
	private int checkIndexInsert(int colum, int x, int y) {
		Log.d(TAG, "checkinsert colum :" + colum + " x:" + x + " y:" + y);
		if (colum < 1) {
			return -1;
		}
		ArrayList<Rect> rectList = Configure.rectListArray.get(colum - 1);
		int i = -1;
		for (Rect rect : rectList) {
			i++;
			if (rect.contains(x, y)) {
				// System.out.println("is move to colun :" + i);
				return i;
			}
		}
		return -1;

	}

	/**
	 * 从preference中添加进来的view，加入到列中的list中去。
	 * 
	 * @param columIndex
	 *            要插入的列数
	 * @param view
	 *            要插入的view
	 */
	public void addViewByPreferenc(int columIndex, BaseView view) {
		// 从0开始插入
		ArrayList<BaseView> viewList = Configure.viewListArray.get(columIndex);
		ArrayList<Rect> columList = Configure.rectListArray.get(columIndex);

		if (view.hasFriend) {
			columList.get(columList.size() - 1).right = view.rect.left;
		}
		columList.add(view.rect);

		// 将view添加到列表
		viewList.add(view);
	}

	public void addRectToCulom(int columIndex, BaseView view) {
		//Log.d(TAG, "addRect To culm");
		if (columIndex < 1) {
			Log.e(TAG, "addrect to colum index is :" + columIndex);
			return;
		}
		ArrayList<BaseView> viewList = Configure.viewListArray
				.get(columIndex - 1);
		ArrayList<Rect> columList = Configure.rectListArray.get(columIndex - 1);

		if (columList.size() < 1 && viewList.size() < 1) {
			RelativeLayout.LayoutParams params = (LayoutParams) view.getLayoutParams();
			params.leftMargin = Configure.columRect.get(columIndex).left;
			params.topMargin = 0;
			Rect rect = new Rect(Configure.columWidth * (columIndex - 1), 0,
					Configure.columWidth * columIndex, view.getLayoutParams().height);
			columList.add(rect);
			viewList.add(0, view);
			view.setLayoutParams(params);
			return;
		}
		Rect lastRect = columList.get(columList.size() - 1);
		BaseView lastView = viewList.get(viewList.size() - 1);
		
		RelativeLayout.LayoutParams params = (LayoutParams) view.getLayoutParams();
		Rect rect ;
		if (!isOutCulomWidht(view, lastView) && !lastView.hasFriend) {// 如果2个view的宽度没有超过列的宽度,且最好的view没有friend,则将view置在同一行
			params.leftMargin = lastRect.left + lastView.getLayoutParams().width;
			params.topMargin = lastRect.top;
			rect = new Rect(Configure.columWidth * (columIndex - 1)
					+ lastView.getLayoutParams().width, lastRect.top, Configure.columWidth
					* columIndex, lastRect.top + view.getLayoutParams().height);
//			columList.add();

			lastRect.right = Configure.columWidth * (columIndex - 1)
					+ view.getLayoutParams().width;
			Log.d(TAG, "addRectToCulom current drag view has friends ");
			view.hasFriend = true;
		} else { // 如果2个view的宽度超过列的宽度则将view置在最后
			params.leftMargin = Configure.columRect.get(columIndex).left;
			params.topMargin = lastRect.bottom;
			rect = new Rect(Configure.columWidth * (columIndex - 1),
					lastRect.bottom, Configure.columWidth * columIndex, view.getLayoutParams().height + lastRect.bottom);
			//columList.add();

		}
		
		
		view.setLayoutParams(params);
		viewList.add(view);
		columList.add(rect);
		
		if (isAddViewOut(lastRect, view) && isOutCulomWidht(view, lastView)) { // 加上这个view后该列超过屏幕
			//System.out.println("add rect before move to next culom:"+viewList.size());
			moveToNextCulom(columList.size() - 1, columIndex);
			//System.out.println("add rect after move to next culom:"+viewList.size());
		}

	}

	/**
	 * 要检查的rect区域加上view的高度是否超过了屏幕的高度
	 * 
	 * @param rect
	 *            要检查的区域
	 * @param view
	 *            要插入的view
	 * @return
	 */
	private boolean isAddViewOut(Rect rect, View view) {
		if ((rect.bottom + view.getLayoutParams().height) > Configure.deviceHeight) {
			// 該元素加在本列超过了屏幕的高度
			Log.d(TAG, " 該元素加在本列超过了屏幕的高度");
			return true;
		}
		return false;
	}

	/**
	 * 移动columNum的最后一项到下一个列中
	 * 
	 * @param columNum
	 *            所在列数
	 * @param viewIndex
	 *            所在列中viewlist的位置
	 */
	private void moveToNextCulom(int viewIndex, int columNum) {
		Log.d(TAG, "moveToNextCulom");
		if (columNum == Configure.columCount) {
			addOneCulom(columNum);
		}
		ArrayList<Rect> rects = Configure.rectListArray.get(columNum - 1);
		ArrayList<BaseView> views = Configure.viewListArray.get(columNum - 1);

		ArrayList<Rect> nextRects = Configure.rectListArray.get(columNum);
		ArrayList<BaseView> nextViews = Configure.viewListArray.get(columNum);

		ArrayList<View> delView = new ArrayList<View>();
		ArrayList<Rect> delRect = new ArrayList<Rect>();
		// 移动rectCulom
		int index = rects.size() - 1;
		BaseView view = views.get(index);

		delRect.add(rects.get(index));
		delView.add(views.get(index));
		// 如果下一列的rectlist不是为空，则下一列的item都要下移本咧的最后item的高度
		if (null != nextRects && nextRects.size() > 0) {
			Log.d(TAG, "insert to position from move to next");
			// nsertToPosition(columNum + 1, 0, view);
			addViewToNextLine(nextRects, nextViews, 0, columNum + 1, view);
		} else {
			// 要插入到下一列的RECT
			Rect rect = new Rect(columNum * Configure.columWidth, 0,
					(columNum + 1) * Configure.columWidth, view.getHeight());

			nextRects.add(0, rect);
			LayoutParams params = (LayoutParams) view.getLayoutParams();
			params.leftMargin = columNum * Configure.columWidth;
			params.topMargin = 0;

			view.setLayoutParams(params);
			nextViews.add(0, view);
		}
		rects.removeAll(delRect);
		views.removeAll(delView);
		checkLastItem(rects, columNum);
	}

	/**
	 * 如果移动到下一列的时候，列数超过了默认的列数，则增加一列
	 * 
	 * @param columNum
	 *            列数
	 */
	private void addOneCulom(int columNum) {
		Log.d(TAG, "add one colum for dragview");
		Configure.columCount++;
		Rect columRect = new Rect(columNum * Configure.columWidth, 0,
				(columNum + 1) * Configure.columWidth, Configure.deviceHeight);
		Configure.columRect.put(columNum + 1, columRect);
		this.getLayoutParams().width = this.getWidth() + Configure.columWidth;

		ArrayList<Rect> columRectList = new ArrayList<Rect>();
		ArrayList<BaseView> columViewList = new ArrayList<BaseView>();
		Configure.rectListArray.add(columRectList);
		Configure.viewListArray.add(columViewList);
	}

	/**
	 * 检查本列中的最后一项是否超过了屏幕高度，如果超过就移动到下一列去
	 * 
	 * @param rects
	 *            检查的rect列
	 * @param columNum
	 *            所在是列数
	 */
	private void checkLastItem(ArrayList<Rect> rects, int columNum) {
		Rect lastRect = rects.get(rects.size() - 1);
		Log.d(TAG, "checkLastItem item last rect is:" + lastRect);
		if (lastRect.bottom > Configure.deviceHeight) {
			moveToNextCulom(rects.size() - 1, columNum);
		}
	}

//	 @Override
//	 private void addView(View child) {
//		 mViews.add((BaseView) child);
//		 super.addView(child);
//	 }
//	HashMap culomMap = new HashMap<Integer, TreeMap<Integer, View>>();
//	public void addView(int columNum, int rowNum, BaseView child) {
//		
//		// 将view添加到列表
//		//viewList.add(child);
//
//		mViews.add((BaseView) child);
//		super.addView(child);
//	}

	/**
	 * 返回当前觸摸點是在第几列中
	 * 
	 * @param x
	 *            触摸点x坐标
	 * @param y
	 *            触摸点y坐标
	 * @return 所在列数数，如果为0则不在列中
	 */
	private int checkColumIndex(int x, int y) {
		Log.d(TAG, "checkColumIndex x:" + x + " y:" + y);
		if (null != Configure.columRect) {
			for (int i = 1; i < Configure.columRect.size() + 1; i++) {
				Rect columRect = Configure.columRect.get(i);
				if (columRect.contains(x, y)) {
					// System.out.println("is move to colun :" + i);
					return i;
				}
			}
		}
		return 0;
	}

	/**
	 * 获取当前触摸点的view
	 * 
	 * @param x
	 * @param y
	 * @return 触摸点所在view
	 */
	private BaseView getTouchItem(int x, int y) {
		for (BaseView view : mViews) {
			RelativeLayout.LayoutParams params = (LayoutParams) view
					.getLayoutParams();
			Rect r = new Rect(params.leftMargin, params.topMargin, params.width
					+ params.leftMargin, params.height + params.topMargin);
			if (view.getId() == 1007) {
				System.out.println("params.leftMargin, params.topMargin"
						+ params.leftMargin + "  " + params.topMargin);
			}
			if (r.contains(x, y)) {
				// Log.i(TAG, "-------touch view--------");
				// Log.i(TAG, "view params:" + params.leftMargin);

				return view;
			}
		}
		return null;
	}

	/**
	 * 从当前列中移除要拖动的view和view的触摸区域rect
	 * 
	 * @param culomIndex
	 *            要移除的列数
	 * @param x
	 * @param y
	 */
	private void removeView(int culomIndex, int x, int y) {
		Log.d(TAG, "removeView ：" + culomIndex + " x :" + x + " y:" + y);

		ArrayList<Rect> columList = Configure.rectListArray.get(culomIndex - 1);
		ArrayList<BaseView> columViewList = Configure.viewListArray
				.get(culomIndex - 1);

		int changIndex = -1;
		for (Rect rect : columList) {
			changIndex++;
			if (rect.contains(x, y)) {
				Log.d(TAG, "go to remove drag rect index:" + rect);
				columList.remove(rect);
				View removeView = columViewList.get(changIndex);
				columViewList.remove(changIndex);

				// 一行中有2个图标，当前移动的是右边的图标
				if (rect.left > Configure.columWidth * (culomIndex - 1)) {
					columList.get(changIndex - 1).right = Configure.columWidth
							* (culomIndex);
					if (currentItem.hasFriend) {
						currentItem.hasFriend = false;
					}
					return;
				}

				// 一行中有2个图标，当前移动的是左边的图标
				if (rect.right < Configure.columWidth * (culomIndex)) {

					Log.d(TAG, "has two view and in the left");
					if (columViewList.get(changIndex).hasFriend) {
						columViewList.get(changIndex).hasFriend = false;
					}

					columList.get(changIndex).left = Configure.columWidth
							* (culomIndex - 1);
					View moveView = columViewList.get(changIndex);
					RelativeLayout.LayoutParams params = (LayoutParams) moveView
							.getLayoutParams();
					params.leftMargin = params.leftMargin
							- removeView.getWidth();
					moveView.setLayoutParams(params);
					return;
				}

				for (; changIndex < columList.size(); changIndex++) { // 移除了当前的view
																		// Rect后，该view后的所以Rect都需补进
					columList.get(changIndex).offset(0,
							-(currentItem.getHeight()));

					View moveView = columViewList.get(changIndex);
					RelativeLayout.LayoutParams params = (LayoutParams) moveView
							.getLayoutParams();
					params.topMargin = params.topMargin
							- removeView.getHeight();
					moveView.setLayoutParams(params);

				}
				return;

			}
		}
	}
	
	/**
	 * 在item view初始化之后调用，显示每个view指定的位置
	 */
	public void drawItemView(){
		handler.sendEmptyMessage(SETVIEWPARAMS);
	}
	
	private void drawItemView(HashMap culomMap,boolean isPreferens) {
		
		Set col = culomMap.keySet();
		Iterator iter = col.iterator();
		while (iter.hasNext()) {
			Integer culomkey = (Integer) iter.next();
			TreeMap<Integer, BaseView> value = (TreeMap<Integer, BaseView>) culomMap.get(culomkey);
			Set list = value.keySet();
			//System.out.println("culom num is :"+culomkey+"list size :"+value.size());
			Iterator listiter = list.iterator();
			int sameline = 1;
			while (listiter.hasNext()) {
				Integer listindex = (Integer) listiter.next();
				BaseView view = value.get(listindex);
				//System.out.println("culom num is :"+culomkey+" listindex is :"+listindex);
				if(!isPreferens){
					this.addRectToCulom(culomkey, view);
				}else{
					if (view instanceof SingleView) {
						// Log.d(TAG, " is instance of single view");
						if (view.rect.left > Configure.columWidth * culomkey) {
							// Log.d(TAG, " and it has a friend before");
							view.hasFriend = true;
						}
					}
				// System.out.println(" listindex:" + listindex +
				// " view id:"+view.getId()+" rect:"+view.rect);
					this.addViewByPreferenc(culomkey, view);
				}
				
			}
		}
		culomMap = null;
	}

	
	/**
	 * 将item view添加给DragView中
	 * @param colum 要添加的列数
	 * @param line	要添加在列中的位置，不可重复
	 * @param child	要添加的view
	 */
	public void addView(int colum, int line, BaseView child) {
		super.addView(child);
	 	mViews.add((BaseView) child);
		viewIdList.add(child);
		TreeMap<Integer, BaseView> listMap = (TreeMap<Integer, BaseView>) culomMap.get(colum);
		if (null == listMap || listMap.size() < 1) {
			listMap = new TreeMap<Integer, BaseView>();
		}
		listMap.put(line, child);
		culomMap.put(colum, listMap);
	}
	
	private HashMap getSaveView() {
		Log.i(TAG, "-------------get save  view -----------");
		HashMap culomMap = new HashMap<Integer, TreeMap<Integer, View>>();
		for (BaseView view : viewIdList) {
			int leftMargin = Configure.sharedPreference.getInt(view.getId()
					+ "x", -1);
			int topMargin = Configure.sharedPreference.getInt(view.getId()
					+ "y", -1);
			int culomLine = Configure.sharedPreference.getInt(view.getId()
					+ "colum", -1);
			if (leftMargin == -1 || topMargin == -1 || culomLine == -1) {
				Log.e(TAG, "-------------get save  view is null");
				break;
			}
			int culomNum = culomLine / 100;
			int listIndex = culomLine % 100;

			RelativeLayout.LayoutParams params = (LayoutParams) view.getLayoutParams();
			params.leftMargin = leftMargin;
			params.topMargin = topMargin;
			view.setLayoutParams(params);
			Rect rect = new Rect(params.leftMargin, params.topMargin,
					Configure.columWidth * (culomNum + 1), params.topMargin
							+ params.height);
			view.rect = rect;

			TreeMap<Integer, BaseView> listMap = (TreeMap<Integer, BaseView>) culomMap.get(culomNum);
			if (null == listMap || listMap.size() < 1) {
				listMap = new TreeMap<Integer, BaseView>();

			}
			listMap.put(listIndex, view);
			culomMap.put(culomNum, listMap);
		}
		viewIdList = null;
		return culomMap;
	}
	
	/**
	 * 在activity关闭前调用，保存当前的页面布局<P/> 
	 * 
	 * 一般用 onPause() 来将所有持久性数据（比如用户的编辑结果）写入存储之中
	 */
	public void saveItemLayout(){
		handler.sendEmptyMessage(SAVE_DATE);
	}
	private void saveView() {
		Log.i(TAG, "-------------save view -----------");
		if (Configure.viewListArray != null&& Configure.viewListArray.size() > 0) {
			Editor edit = Configure.sharedPreference.edit();
			int colum = -1;
			for (ArrayList<BaseView> viewList : Configure.viewListArray) {
				colum++;
				int lineIndex = -1;
				for (View view : viewList) {
					lineIndex++;
					edit.putInt(view.getId() + "x", view.getLeft());
					edit.putInt(view.getId() + "y", view.getTop());

					edit.putInt(view.getId() + "colum", colum * 100 + lineIndex);
				}
			}
			Configure.delNullList();
			edit.putInt("ColumCount", Configure.columCount);
			edit.commit();
		}

	}
	
	
	public interface Listener {
		void onItemClick(View view);
	}
	public void setParent(View view) {
		this.parent = view;
	}
	
}
