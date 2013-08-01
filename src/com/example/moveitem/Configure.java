package com.example.moveitem;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;

public class Configure {
	private static final String TAG = "Configure";
	//private static int[] deviceWidthHeight = new int[2];

	public static HashMap<Integer, Rect> columRect = new HashMap<Integer, Rect>();
	public static HashMap<Integer, Point> viewPoint = new HashMap<Integer, Point>();

	public static ViewGroup.LayoutParams dbParams = new ViewGroup.LayoutParams(200, 100);
	public static ViewGroup.LayoutParams fourParams = new ViewGroup.LayoutParams(200, 200);
	public static ViewGroup.LayoutParams singleParams = new ViewGroup.LayoutParams(100,
			100);
	
	public static int deviceWidth =0;
	public static int deviceHeight=0;
	public static int columWidth;
	public static int columCount=6;
	
	public static ArrayList<ArrayList<Rect>>  rectListArray ;
	public static ArrayList<ArrayList<BaseView>>  viewListArray ;
	
	
	public static String PREFERENCE_NAME = "configure";
	public static SharedPreferences sharedPreference;
	
	public static void init(Context context) {
		
		sharedPreference = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
		columCount = sharedPreference.getInt("ColumCount", columCount);
		
		if ((deviceWidth== 0) && (deviceHeight == 0)) {
			Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
			deviceHeight = display.getHeight();
			deviceWidth = display.getWidth();
			
			System.out.println("deviceWidth :"+deviceWidth);
			System.out.println("deviceHeight :"+deviceHeight);
		}
		
		int columWidth = Configure.deviceWidth / 4;
		Configure.columWidth = columWidth;
		
		
		
		// 设置每列的区域
		for (int i = 1; i <= Configure.columCount; i++) {
			Rect columRect = new Rect((i - 1) * Configure.columWidth, 0, i * Configure.columWidth,
					Configure.deviceHeight);
			Configure.columRect.put(i, columRect);
		}
		
		
		
		rectListArray = new ArrayList<ArrayList<Rect>>();
		viewListArray = new ArrayList<ArrayList<BaseView>>();
		
		for(int i =0;i<columCount;i++){
			ArrayList<Rect> columRectList = new ArrayList<Rect>();
			ArrayList<BaseView> columViewList = new ArrayList<BaseView>();
			rectListArray.add(columRectList);
			viewListArray.add(columViewList);
		}
		
	}
	
	public static void delNullList() {
		// Log.d(TAG,   
		// "before del null last row size is :"+Configure.viewListArray.size());
		ArrayList<BaseView> lastRow = Configure.viewListArray
				.get(Configure.viewListArray.size() - 1);

		if (Configure.viewListArray.size() > 6 && lastRow.size() < 1) {
			Configure.viewListArray.remove(lastRow);
			delNullList();
		}
		Configure.columCount = Configure.viewListArray.size();
		// Log.d(TAG,
		// "after del null last row size is :"+Configure.viewListArray.size());
	}
	
	
	
	
	
	
	
}
