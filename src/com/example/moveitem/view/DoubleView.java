package com.example.moveitem.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.moveitem.BaseView;
import com.example.moveitem.Configure;
import com.example.moveitem.R;

public class DoubleView extends BaseView {

	private ImageView iv;
    private TextView  tv;
    public DoubleView(Context context) {
        this(context, null);
    }

    public DoubleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // 导入布局
        LayoutInflater.from(context).inflate(R.layout.doubleview, this, true);
        iv = (ImageView) findViewById(R.id.iv_double);
        tv = (TextView) findViewById(R.id.tv_double);
        setLayoutParams(Configure.dbParams);
    }


    /**
     * 设置图片资源
     */
    public void setImageResource(int resId) {
        iv.setImageResource(resId);
    }

    /**
     * 设置显示的文字
     */
    public void setTextViewText(String text) {
        tv.setText(text);
    }


}
