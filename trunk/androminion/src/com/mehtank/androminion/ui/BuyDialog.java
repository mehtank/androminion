package com.mehtank.androminion.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mehtank.androminion.R;

public class BuyDialog {
	Context top;
	
	public BuyDialog(final Context top, String s) {
		this.top = top;
		
		FrameLayout fv = new FrameLayout (top);
		LinearLayout ll = new LinearLayout(top);
		ll.setOrientation(LinearLayout.VERTICAL);
		ll.setBackgroundColor(0x99000000);
		
		ImageView im = new ImageView (top);
        im.setImageResource(R.drawable.bigiconpro);
        
        TextView tv = new TextView(top);

        tv.setTextColor(Color.WHITE);
        tv.setTextSize(tv.getTextSize() * 1.5f);
        tv.setText(s);
        ll.addView(tv);

        tv = new TextView(top);        
        tv.setTextColor(Color.YELLOW);
        tv.setTextSize(tv.getTextSize() * 1.3f);
        tv.setText("\nUpgrade to Androminion Pro now!");
        ll.addView(tv);
        
        tv = new TextView(top);        
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(tv.getTextSize() * 1.2f);
        tv.setText( "\nBuy Androminion Pro from the market and get the following bonuses:\n"
        		+ " - Choose your supply cards\n"
        		+ " - Play 2-4 player games\n"
        		+ " - Customize your interface settings\n"
        		+ " - Get first access to new features\n"
        		+ " - Support further development\n"
        		);
        ll.addView(tv);
                
		FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.WRAP_CONTENT,
				FrameLayout.LayoutParams.WRAP_CONTENT,
				Gravity.CENTER);

        fv.addView(im, p);
        fv.addView(ll);
        
		new AlertDialog.Builder(top)
			// .setIcon(R.drawable.logopro)
			.setTitle("Game ended!")
			.setView(fv)  
			.setPositiveButton("Take me to the market!", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent goToMarket = null;
					goToMarket = new Intent(Intent.ACTION_VIEW,Uri.parse("market://details?id=com.mehtank.androminionpro"));
					top.startActivity(goToMarket);					
				}
			})
			.setNegativeButton("No thanks, return me to the game", null)
			.show();		

	}

}
