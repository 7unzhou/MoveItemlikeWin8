package com.example.moveitem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

import com.example.moveitem.DragView.Listener;
import com.example.moveitem.view.DoubleView;
import com.example.moveitem.view.FourView;
import com.example.moveitem.view.ScrollContent;
import com.example.moveitem.view.SingleView;

import android.R.integer;
import android.R.layout;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;

public class MainActivity extends Activity {
	private static final String TAG = "MainActivity";

	/** Called when the activity is first created. */

	private SingleView oneview1, oneview2, oneview3, oneview4;
	private DoubleView twoview1, twoview2;
	private FourView fview1, fview2;
	private DragView dv;
	private ArrayList<BaseView> viewLIst = new ArrayList<BaseView>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.main);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);// 去掉信息栏
		Configure.init(this);

		initItemView();

		setContentView(R.layout.main);

		dv = new DragView(this);
		dv.setItemListener(new Listener() {
			@Override
			public void onItemClick(View view) {
				Toast.makeText(MainActivity.this,
						view.getId() + " is onclick ", 1000).show();
			}
		});

		ScrollContent sv = (ScrollContent) findViewById(R.id.sv_sview);
		dv.setParent(sv);

		sv.addView(dv, Configure.columWidth * Configure.columCount,
				Configure.deviceHeight);
		addViewToContent(dv);
		dv.drawItemView();
	}

	private void initItemView() {

		oneview1 = new SingleView(this);
		oneview1.setId(1001);
		oneview1.setTextViewText("oneview1");
		
		
		oneview2 = new SingleView(this);
		oneview2.setId(1002);
		oneview2.setTextViewText("oneview2");

		oneview3 = new SingleView(this);
		oneview3.setId(1007);
		oneview3.setTextViewText("oneview3");

		oneview4 = new SingleView(this);
		oneview4.setId(1008);
		oneview4.setTextViewText("oneview4");

		twoview1 = new DoubleView(this);
		twoview1.setId(1003);
		twoview1.setTextViewText("twoview1");

		twoview2 = new DoubleView(this);
		twoview2.setId(1004);
		twoview2.setTextViewText("twoview2");

		fview1 = new FourView(this);
		fview1.setId(1005);
		fview1.setTextViewText("fview1");

		fview2 = new FourView(this);
		fview2.setId(1006);
		fview2.setTextViewText("fview2");

	}

	
	
	private void addViewToContent(DragView dv) {
		dv.addView(1, 1, oneview1);
		dv.addView(1, 2, oneview2);
		dv.addView(1, 3, oneview3);
		dv.addView(1, 4, oneview4);
		dv.addView(1, 5, twoview1);
		dv.addView(1, 6, twoview2);
		dv.addView(2, 1, fview1);
		dv.addView(2, 2, fview2);
		
	}

	@Override
	protected void onStart() {
		super.onStart();

	};
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		dv.saveItemLayout();
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

}