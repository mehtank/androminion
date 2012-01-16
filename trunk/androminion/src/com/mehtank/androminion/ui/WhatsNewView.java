package com.mehtank.androminion.ui;

import com.mehtank.androminion.R;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class WhatsNewView extends FrameLayout {
	public WhatsNewView(Context context) {
		super(context);
		init(context);
	}
	
	public WhatsNewView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private void init(Context top) {		
		ScrollView sv = new ScrollView(top);
        sv.setVerticalScrollBarEnabled(true);

        LinearLayout ll = new LinearLayout(top);
		ll.setOrientation(LinearLayout.VERTICAL);
		ll.setBackgroundColor(0x99000000);
		
		sv.addView(ll);
		
		ImageView im = new ImageView (top);
        im.setImageResource(R.drawable.bigicon);
        
        TextView tv = new TextView(top);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(tv.getTextSize() * 1.5f);
        String vname = " ";
        try {
			vname += Strings.format(top, R.string.version, top.getPackageManager().getPackageInfo(top.getPackageName(), 0).versionName);
		} catch (NameNotFoundException e) {}; 

        tv.setText( Strings.getString(top, R.string.whatsnew_title) + vname);
        ll.addView(tv);
        
        tv = new TextView(top);        
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(tv.getTextSize() * 1.2f);
        tv.setText( Html.fromHtml(Strings.getString(top, R.string.whatsnew) ));
        tv.setMovementMethod(LinkMovementMethod.getInstance());

        ll.addView(tv);
        
		FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.WRAP_CONTENT,
				FrameLayout.LayoutParams.WRAP_CONTENT,
				Gravity.CENTER);

		addView(im, p);
        addView(sv);
	}

}
