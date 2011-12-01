package com.mehtank.androminion.ui;

import java.io.File;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mehtank.androminion.Androminion;
import com.mehtank.androminion.R;

public class TurnView extends LinearLayout {

	static final int MAXICONS = 5;
	
	Androminion top;
	static boolean graphical = false;

	LinearLayout.LayoutParams lp;
	TextView tv;
	
	Uri trURI, actionURI, buyURI, bridgeURI;
	TextView coins;
	
	TextView largeRefText;
	
	public TurnView(Context context, TextView largeRefText) {
		super(context);
		
		this.largeRefText = largeRefText;
		this.top = (Androminion) context;
		
		String str = "/sdcard/Dominion/images/icons/throneroom.png";
		File f = new File(str);
		if (f.exists())
			trURI = Uri.parse(str);

		str = "/sdcard/Dominion/images/icons/action.png";
		f = new File(str);
		if (f.exists())
			actionURI = Uri.parse(str);
		
		str = "/sdcard/Dominion/images/icons/buy.png";
		f = new File(str);
		if (f.exists())
			buyURI = Uri.parse(str);
		
		str = "/sdcard/Dominion/images/icons/bridge.png";
		f = new File(str);
		if (f.exists())
			bridgeURI = Uri.parse(str);

		if ((trURI != null) && (actionURI != null) && (buyURI != null) && (bridgeURI != null)) {
			graphical = true;
		}
		
		setOrientation(HORIZONTAL);
		
		lp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.FILL_PARENT);
		
		tv = new TextView(top);
		tv.setLayoutParams(lp);
		addView(tv);

		coins = new TextView(top);
		coins.setTextSize((float) (coins.getTextSize() * 0.75));
		coins.setTextColor(Color.BLACK);
		coins.setBackgroundResource(R.drawable.coin);
		
		setLayoutParams(lp);
	}

	public double getTextSize() {
		return tv.getTextSize();
	}
	public void setTextSize(float f) {
		tv.setTextSize(f);
	}

	public void addIcons(Uri icon, int count, int maxCount) {
		if (count > maxCount) {
			ImageView im = new ImageView(top);
			im.setImageURI(icon);
            im.setScaleType(ImageView.ScaleType.FIT_CENTER);
            im.setLayoutParams(lp);
			addView(im);

			TextView tv = new TextView(top);
			tv.setText("(" + count + ")");
			tv.setTextSize((float) (tv.getTextSize() * 0.75));
			tv.setLayoutParams(lp);
			addView(tv);
			
			return;
		}
		
		for (int i = 0; i < count; i++) {
			ImageView im = new ImageView(top);
			im.setImageURI(icon);
            im.setScaleType(ImageView.ScaleType.FIT_CENTER);
			im.setLayoutParams(lp);
			addView(im);
		}		
	}
	public void setStatus(int[] is, int cardCostModifier, int potions, boolean myTurn) {
		removeAllViews();
		if (graphical) {
			addIcons(trURI, is[3], 3);
			addIcons(actionURI, is[0], 5);
			addIcons(buyURI, is[1], 5);
			coins.setText(" " + is[2] + " ");
			addView(coins);
//			addIcons(bridgeURI, -cardCostModifier, 2);
			
			int d = coins.getHeight();
	    	LinearLayout.LayoutParams p = new LinearLayout.LayoutParams((int) (d*1.5), d);
			
			for (int i = 0; i < getChildCount(); i++) 
				if (getChildAt(i) instanceof ImageView) 
					getChildAt(i).setLayoutParams(p);
		} else {
		    String actions;
		    if(is[0] == 1)
		        actions = Strings.format(top, R.string.action_single, "" + is[0]);
		    else
                actions = Strings.format(top, R.string.action_multiple, "" + is[0]);
            String buys;
            if(is[1] == 1)
                buys = Strings.format(top, R.string.buy_single, "" + is[1]);
            else
                buys = Strings.format(top, R.string.buy_multiple, "" + is[1]);
            String coins = Strings.format(top, R.string.coins, "" + is[2]);
            String baseStr;
            if(potions > 0) {
                String potionString; 
                if(potions == 1)
                    potionString = Strings.format(top, R.string.potion_single, "" + potions);
                else
                    potionString = Strings.format(top, R.string.potion_multiple, "" + potions);
                
                baseStr = Strings.format(top, R.string.actions_buys_coins_potions, actions, buys, coins, potionString);
            }
            else {
                baseStr = Strings.format(top, R.string.actions_buys_coins, actions, buys, coins);
            }

		    String str = baseStr;
//			String str = ((is[3] <= 0) ? "" :
//							"" + is[3] + " TR: ");
//		    
//		    str += baseStr + ".";
//						 
//			if (cardCostModifier != 0)
//				str += "\nCost modifier: " + cardCostModifier;

			tv.setText(str);
			tv.setLayoutParams(lp);

			if(largeRefText != null) {
			    if(myTurn) {
			        largeRefText.setText(baseStr);
			    }
			    else {
//			        largeRefText.setText(" ");
			    }
			}
			
			addView(tv);
		}
	}
}
