package com.mehtank.androminion.ui;

import com.mehtank.androminion.R;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CreditsView extends FrameLayout {

	public CreditsView(Context top) {
		super(top);
		
		LinearLayout ll = new LinearLayout(top);
		ll.setOrientation(LinearLayout.VERTICAL);
		ll.setBackgroundColor(0x66000000);
		
		ImageView im = new ImageView (top);
        im.setImageResource(R.drawable.bigicon);
        
        TextView tv = new TextView(top);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(tv.getTextSize() * 1.5f);
        tv.setText( Strings.getString(top, R.string.contrib_title));
        ll.addView(tv);
        
        tv = new TextView(top);        
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(tv.getTextSize() * 1.3f);
        tv.setText( Strings.getString(top, R.string.contributors) );
        ll.addView(tv);
        
		FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.WRAP_CONTENT,
				FrameLayout.LayoutParams.WRAP_CONTENT,
				Gravity.CENTER);

		addView(im, p);
        addView(ll);
	}


}
