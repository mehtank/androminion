package com.mehtank.androminion.ui;

import java.io.File;
import java.util.StringTokenizer;

import com.mehtank.androminion.Androminion;
import com.mehtank.androminion.R;
import com.mehtank.androminion.util.HapticFeedback;
import com.mehtank.androminion.util.HapticFeedback.AlertType;
import com.vdom.comms.MyCard;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class CardView extends FrameLayout implements OnLongClickListener {

	public static final int SHOWTOGGLE = 0;
	public static final int SHOWCOUNT = 1;
	public static final int SHOWCOIN = 2;
	
	public static final int WIDTH = 110;
	TextView tv, colorBox;
	TextView cost, countLeft, embargos;
	TextView checked;
	TextView nomore;
	
	MyCard c;
	OnClickListener gt;
	CardGroup parent;
	boolean opened = false;

	public CardView(Context context, OnClickListener gt, CardGroup parent, MyCard c) {
		super(context);
		
		this.gt = gt;
		this.parent = parent;

		colorBox = new TextView(context);
		FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.FILL_PARENT,
				FrameLayout.LayoutParams.FILL_PARENT,
				Gravity.CENTER);
		colorBox.setLayoutParams(p);
		addView(colorBox);

		tv = new TextView(context);
		tv.setSingleLine();
		tv.setTextSize(8.0f);
		p = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.WRAP_CONTENT,
				FrameLayout.LayoutParams.WRAP_CONTENT,
				Gravity.CENTER);
		tv.setLayoutParams(p);
		addView(tv);
		
		if (c != null) {
			p = new FrameLayout.LayoutParams(
					FrameLayout.LayoutParams.WRAP_CONTENT,
					FrameLayout.LayoutParams.WRAP_CONTENT,
					Gravity.LEFT + Gravity.CENTER_VERTICAL);
			if(!c.isPrize) {
    			cost = new TextView(context);
    			cost.setText("0");
    			cost.setTextSize(12.0f);
    			cost.setTextColor(Color.BLACK);
    			cost.setBackgroundResource(R.drawable.coin);
    			cost.setLayoutParams(p);
    			addView(cost);
			}
	
			p = new FrameLayout.LayoutParams(
					FrameLayout.LayoutParams.WRAP_CONTENT,
					FrameLayout.LayoutParams.WRAP_CONTENT,
					Gravity.BOTTOM + Gravity.CENTER_HORIZONTAL);
			countLeft = new TextView(context);
			countLeft.setText("0");
			countLeft.setTextSize(8.0f);
			// countLeft.setTextColor(Color.BLACK);
			// countLeft.setBackgroundColor(Color.WHITE);
			countLeft.setLayoutParams(p);
			countLeft.setVisibility(INVISIBLE);
			addView(countLeft);

			p = new FrameLayout.LayoutParams(
					FrameLayout.LayoutParams.WRAP_CONTENT,
					FrameLayout.LayoutParams.WRAP_CONTENT,
					Gravity.RIGHT + Gravity.CENTER_VERTICAL);
			embargos = new TextView(context);
			embargos.setText("0");
			embargos.setTextSize(12.0f);
			embargos.setTextColor(Color.WHITE);
			embargos.setBackgroundResource(R.drawable.embargos);
			embargos.setLayoutParams(p);
			embargos.setVisibility(INVISIBLE);
			addView(embargos);
			
			setCard(c);
	
			checked = new TextView(context);
			checked.setTextColor(Color.RED);
			checked.setTypeface(Typeface.DEFAULT_BOLD);
            checked.setBackgroundResource(android.R.drawable.arrow_down_float);
			if (opened)
				checked.setVisibility(VISIBLE);
			else
				checked.setVisibility(INVISIBLE);
			
			p = new FrameLayout.LayoutParams(
					FrameLayout.LayoutParams.WRAP_CONTENT,
					FrameLayout.LayoutParams.WRAP_CONTENT,
					Gravity.TOP + Gravity.RIGHT);
			
			checked.setLayoutParams (p);
	
			addView(checked);
			
			nomore = new TextView(context);
			p = new FrameLayout.LayoutParams(
					FrameLayout.LayoutParams.FILL_PARENT,
					FrameLayout.LayoutParams.FILL_PARENT,
					Gravity.CENTER);
			nomore.setLayoutParams(p);
			nomore.setBackgroundColor(Color.BLACK);
			nomore.setVisibility(INVISIBLE);
			nomore.getBackground().setAlpha(156);
			addView(nomore);

			if (c.isBane)
				setBackgroundResource(R.drawable.baneborder);
			else
				setBackgroundResource(R.drawable.roundborder);

	    	setOnLongClickListener(this);
		}
		
		setOnClickListener( new OnClickListener (){
			@Override
			public void onClick(View v) {
				click(v);
			} 
		}); 
	}
	
	public void setCard(MyCard c) {
		this.c = c;

		tv.setText(c.name, TextView.BufferType.SPANNABLE);
		if(cost != null) {
    		cost.setText(" " + c.cost + " ");
    		if (c.costPotion)
    			cost.setBackgroundResource(R.drawable.coinpotion);
		}
		
		int fgColor = Color.WHITE;
		int countColor = Color.WHITE;
		int bgColor = Color.BLACK;

		if (c.isReaction) {
			bgColor = Color.rgb(0x00, 0x70, 0xcc);
		    if (c.isVictory) {
	            fgColor = (Color.BLACK);
		        tv.setBackgroundColor(Color.rgb(0x32, 0xcd, 0x32));
		    }
		    else if (c.isTreasure) {
				bgColor = Color.rgb(0xdb, 0xdb, 0x70);
	            fgColor = Color.WHITE;
				countColor = Color.BLACK;
                tv.setBackgroundColor(Color.rgb(0x00, 0x70, 0xcc));
            }
		}
		else if (c.isDuration) {
			fgColor = (Color.BLACK);
			countColor = Color.BLACK;
			bgColor = (Color.rgb(0xff, 0x8c, 0x00));
		}
		else if (c.isAttack) {
			fgColor = (Color.rgb(0xff, 0x80, 0x60));
			countColor = (Color.rgb(0xff, 0x80, 0x60));
		} else if (c.isTreasure && c.isVictory) {
			bgColor = (Color.rgb(0xc0, 0xc0, 0xc0));
			fgColor = (Color.BLACK);
			countColor = Color.BLACK;
			tv.setBackgroundColor(Color.rgb(0x32, 0xcd, 0x32));
		}
		else if (c.isAction && c.isVictory) {
			bgColor = (Color.BLACK);
			fgColor = (Color.BLACK);
			tv.setBackgroundColor(Color.rgb(0x32, 0xcd, 0x32));
		}
		else if (c.isTreasure) {
			fgColor = (Color.BLACK);
			countColor = Color.BLACK;
			if (c.isPotion)
				bgColor = (Color.rgb(0x33, 0xcc, 0xff));				
			else if (c.gold == 1) 
				bgColor = (Color.rgb(0xcf, 0xb5, 0x3b));
			else if (c.gold == 2) 
				bgColor = (Color.rgb(0xc0, 0xc0, 0xc0));
			else if (c.gold == 3) 
				bgColor = (Color.YELLOW);
			else if (c.gold == 5)
				bgColor = (Color.WHITE);
			else
				bgColor = (Color.rgb(0xdb, 0xdb, 0x70));
			/*
			if ("Copper".equals(c.name))
				bgColor = (Color.rgb(0xb8, 0x73, 0x33));
			else if ("Silver".equals(c.name))
				bgColor = (Color.rgb(0xc0, 0xc0, 0xc0));
			else if ("Gold".equals(c.name))
				bgColor = (Color.rgb(0xff, 0xd7, 0x00));
			else if ("Platinum".equals(c.name))
                bgColor = (Color.rgb(0xe5, 0xe4, 0xe2));
			else  
				bgColor = (Color.rgb(0xdb, 0xdb, 0x70));
			 */
		}
		else if (c.isCurse)
			bgColor = (Color.rgb(0x94, 0, 0xd3));
		else if (c.isVictory) {
			fgColor = (Color.BLACK);
			countColor = Color.BLACK;
			bgColor = (Color.rgb(0x32, 0xcd, 0x32));
		}
		
		tv.setTextColor(fgColor);
		countLeft.setTextColor(countColor);
		if (bgColor != 0)
			colorBox.setBackgroundColor(bgColor);
	}

	protected void click(View arg0) {
		gt.onClick(this);
	}

    public void setOpened(boolean o, String indicator) {
        setOpened(o, -1, indicator);
    }

    public void setOpened(boolean o, int order, String indicator) {
		opened = o;
		if (order > 0)
			checked.setText(" " + (order+1));
		else
            checked.setText(indicator);
		
        if (opened)
            checked.setVisibility(VISIBLE);
        else
            checked.setVisibility(INVISIBLE);
	}
	
	public void setSize(int s) {
		countLeft.setText(" " + s + " ");
		if (s == 0) 
			nomore.setVisibility(VISIBLE);
		else
			nomore.setVisibility(INVISIBLE);
	}

	public void setEmbargos(int s) {
		embargos.setText(" " + s + " ");
		if (s != 0) 
			embargos.setVisibility(VISIBLE);
	}

	public void swapNum(int mode) {
		if (mode == SHOWCOIN) {
			// cost.setVisibility(VISIBLE);
			countLeft.setVisibility(INVISIBLE);
			FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(
					FrameLayout.LayoutParams.WRAP_CONTENT,
					FrameLayout.LayoutParams.WRAP_CONTENT,
					Gravity.CENTER);
			tv.setLayoutParams(p);
			return;
		} else if (mode == SHOWCOUNT) {
			// cost.setVisibility(INVISIBLE);
			countLeft.setVisibility(VISIBLE);
			FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(
					FrameLayout.LayoutParams.WRAP_CONTENT,
					FrameLayout.LayoutParams.WRAP_CONTENT,
					Gravity.TOP + Gravity.CENTER_HORIZONTAL);
			tv.setLayoutParams(p);
			return;
		} else {			
			if (countLeft.getVisibility() == TextView.VISIBLE) {
				swapNum(SHOWCOIN);
			} else {
				swapNum(SHOWCOUNT);
			}
		}
	}
	
	public boolean onLongClick(View view) {
		CardView cardView = (CardView) view;
		
		HapticFeedback.vibrate(getContext(),AlertType.LONGCLICK);
		String str = cardView.c.name;
		str = str.toLowerCase();
		
		StringTokenizer st = new StringTokenizer(str," ",false);
		String filename = "";
		while (st.hasMoreElements()) filename += st.nextElement();
		
		View v;

        // int resID =
        // getResources().getIdentifier("com.mehtank.androminion:drawable/" +
        // filename, null, null);
        // if (resID != 0) {
        // ImageView im = new ImageView(top);
        // im.setBackgroundResource(resID);
        // im.setScaleType(ImageView.ScaleType.FIT_CENTER);
        // v = im;
        // } else {
			str = Androminion.BASEDIR + "/images/full/" + filename + ".jpg";
			File f = new File(str);
			if (f.exists()) {
				Uri u = Uri.parse(str);
				ImageView im = new ImageView(view.getContext());
	            im.setImageURI(u);  
	            im.setScaleType(ImageView.ScaleType.FIT_CENTER);
	            v = im;
			} else {
				TextView tv = new TextView(view.getContext());
				tv.setPadding(15, 0, 15, 5);
				String text = ""; //cardView.c.name;
				if(cardView.c.expansion != null && cardView.c.expansion.length() != 0) {
				    text += "(" + cardView.c.expansion + ")\n";
				}
				text += cardView.c.desc;
				tv.setText( text );
				v = tv;
			}
        // }
			String title = cardView.c.name;
			if(PreferenceManager.getDefaultSharedPreferences(view.getContext()).getBoolean("showenglishnames", false)) {
				title += " (" + cardView.c.originalName + ")";
			}
		new AlertDialog.Builder(view.getContext())
			.setTitle(title)
			.setView(v)
			.setPositiveButton(android.R.string.ok, null)
			.show();

		return true;
	}

}
