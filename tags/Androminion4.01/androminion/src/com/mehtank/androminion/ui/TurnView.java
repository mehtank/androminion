package com.mehtank.androminion.ui;

import java.io.File;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mehtank.androminion.R;

public class TurnView extends LinearLayout {
	@SuppressWarnings("unused")
	private static final String TAG = "TurnView";

	static final int MAXICONS = 5;

	Context top;
	static boolean graphical = false;

	LinearLayout.LayoutParams lp;
	TextView tv;
	Uri trURI, actionURI, buyURI, bridgeURI;
	TextView coins;

	@SuppressWarnings("deprecation")
	public TurnView(Context context) {
		super(context);

		this.top = context;

		final String BASEDIR = Environment.getExternalStorageDirectory().getAbsolutePath();
		String str = BASEDIR + "/Dominion/images/icons/throneroom.png";
		File f = new File(str);
		if (f.exists())
			trURI = Uri.parse(str);

		str = BASEDIR + "/Dominion/images/icons/action.png";
		f = new File(str);
		if (f.exists())
			actionURI = Uri.parse(str);

		str = BASEDIR + "/Dominion/images/icons/buy.png";
		f = new File(str);
		if (f.exists())
			buyURI = Uri.parse(str);

		str = BASEDIR + "/Dominion/images/icons/bridge.png";
		f = new File(str);
		if (f.exists())
			bridgeURI = Uri.parse(str);

		if ((trURI != null) && (actionURI != null) && (buyURI != null) && (bridgeURI != null)) {
			graphical = true;
		}

		setOrientation(HORIZONTAL);

		lp = new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.FILL_PARENT);

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

			TextView textView = new TextView(top);
			textView.setText("(" + count + ")");
			textView.setTextSize((float) (textView.getTextSize() * 0.75));
			textView.setLayoutParams(lp);
			addView(textView);

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
	public void setStatus(int[] is, int potions, boolean myTurn) {
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
		        actions = top.getString(R.string.action_single, "" + is[0]);
		    else
                actions = top.getString(R.string.action_multiple, "" + is[0]);
            String buys;
            if(is[1] == 1)
                buys = top.getString(R.string.buy_single, "" + is[1]);
            else
                buys = top.getString(R.string.buy_multiple, "" + is[1]);

//            String coinStr = "" + is[2] + ((potions > 0)?"p":"");
            String coinStr = "" + is[2];
            if (potions == 1) {
            	coinStr += "p";
            } else if (potions > 1) {
            	coinStr += "p" + potions;
            }
//            for(int i=0; i < potions; i++) {
//                coinStr += "p";
//            }
            String coinsStr = top.getString(R.string.coins, coinStr);
            String baseStr = top.getString(R.string.actions_buys_coins, actions, buys, coinsStr);
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

			addView(tv);
		}
	}
}
