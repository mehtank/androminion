package com.mehtank.androminion.ui;

import com.mehtank.androminion.R;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SplashView extends FrameLayout {

	public SplashView(Context context) {
		super(context);
		
		LinearLayout ll = new LinearLayout(context);
		
        setBackgroundColor(Color.BLACK);
        ll.setOrientation(LinearLayout.VERTICAL);
        
        ImageView im = new ImageView (context);
        im.setImageResource(R.drawable.bigicon);
        ll.addView(im);
        
        im = new ImageView (context);
        im.setImageResource(R.drawable.logo);
        ll.addView(im);
        
        TextView tv = new TextView (context);
        tv.setText(R.string.splash);
        ll.addView(tv);
        
        FrameLayout.LayoutParams fp = new FrameLayout.LayoutParams(
        		FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        addView(ll, fp);        
	}
}
