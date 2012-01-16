package com.mehtank.androminion.ui;

import com.mehtank.androminion.R;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AboutView extends LinearLayout {
	public AboutView(Context context) {
		super(context);
		init(context);
	}
	
	public AboutView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	private void init(Context top) {
		setOrientation(VERTICAL);
		
		ImageView im = new ImageView (top);
		im.setImageResource(R.drawable.logo);
		addView(im);

		FrameLayout fv = new FrameLayout(top);
		
		LinearLayout ll = new LinearLayout(top);
		ll.setOrientation(LinearLayout.VERTICAL);
		ll.setBackgroundColor(0x99000000);		
		
		im = new ImageView(top);
        im.setImageResource(R.drawable.bigicon);
        
        TextView tv = new TextView(top);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(tv.getTextSize() * 1.5f);
        String vname = "";
        try {
			vname = Strings.format(top, R.string.version, top.getPackageManager().getPackageInfo(top.getPackageName(), 0).versionName);
		} catch (NameNotFoundException e) {}; 

        tv.setText( vname + Strings.getString(top, R.string.copyright));
        tv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				 Uri uri = Uri.parse("http://android.mehtank.com");
				 Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				 v.getContext().startActivity(intent);				
			}
        });
        ll.addView(tv);
        
        tv = new TextView(top);        
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(tv.getTextSize() * 1.3f);
        tv.setText( Strings.getString(top, R.string.dom_copyright) );
        ll.addView(tv);
        
		FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.WRAP_CONTENT,
				FrameLayout.LayoutParams.WRAP_CONTENT,
				Gravity.CENTER);

		fv.addView(im, p);
        fv.addView(ll);
        
        addView(fv);
	}

}
