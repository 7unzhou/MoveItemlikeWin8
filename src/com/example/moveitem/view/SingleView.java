package com.example.moveitem.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.moveitem.BaseView;
import com.example.moveitem.Configure;
import com.example.moveitem.R;

public class SingleView extends BaseView {

    private ImageView iv;
    private TextView  tv;
    //private boolean hasFriend;
    public SingleView(Context context) {
        this(context, null);
    }

    public SingleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // 导入布局
        LayoutInflater.from(context).inflate(R.layout.singview, this, true);
        iv = (ImageView) findViewById(R.id.iv);
        tv = (TextView) findViewById(R.id.tv);
        setLayoutParams(Configure.singleParams);
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
