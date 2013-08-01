package com.example.moveitem;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

public class BaseView extends RelativeLayout{


	
	public BaseView(Context context) {
	        super(context);
	        // TODO Auto-generated constructor stub
        }
	
	public BaseView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	Rect rect;
	
	boolean hasFriend;
}
