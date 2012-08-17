package com.mehtank.androminion.ui;

import com.mehtank.androminion.Androminion;
import com.mehtank.androminion.R;
import com.vdom.comms.MyCard;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

public class CardView extends FrameLayout {

	public static final int SHOWTOGGLE = 0;
	public static final int SHOWCOUNT = 1;
	public static final int SHOWCOIN = 2;
	
	public static final int WIDTH = 75;
	TextView tv, colorBox;
	TextView cost, countLeft, embargos;
	TextView checked;
	TextView nomore;
	
	MyCard c;
	OnClickListener gt;
	OnLongClickListener lc;
	CardGroup parent;
	boolean opened = false;
	Androminion top;

	public CardView(Context context, OnClickListener gt, OnLongClickListener lc, CardGroup parent, MyCard c) {
		super(context);

		this.top = (Androminion) context;
		this.gt = gt;
		this.lc = lc;
		this.parent = parent;

		colorBox = new TextView(top);
		FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.FILL_PARENT,
				FrameLayout.LayoutParams.FILL_PARENT,
				Gravity.CENTER);
		colorBox.setLayoutParams(p);
		addView(colorBox);

		tv = new TextView(top);
		tv.setSingleLine();
		tv.setTextSize((float) (tv.getTextSize() * 0.6));
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
    			cost = new TextView(top);
    			cost.setText("0");
    			cost.setTextSize((float) (cost.getTextSize() * 0.75));
    			cost.setTextColor(Color.BLACK);
    			cost.setBackgroundResource(R.drawable.coin);
    			cost.setLayoutParams(p);
    			addView(cost);
			}
	
			p = new FrameLayout.LayoutParams(
					FrameLayout.LayoutParams.WRAP_CONTENT,
					FrameLayout.LayoutParams.WRAP_CONTENT,
					Gravity.BOTTOM + Gravity.CENTER_HORIZONTAL);
			countLeft = new TextView(top);
			countLeft.setText("0");
			countLeft.setTextSize((float) (countLeft.getTextSize() * 0.5));
			// countLeft.setTextColor(Color.BLACK);
			// countLeft.setBackgroundColor(Color.WHITE);
			countLeft.setLayoutParams(p);
			countLeft.setVisibility(INVISIBLE);
			addView(countLeft);

			p = new FrameLayout.LayoutParams(
					FrameLayout.LayoutParams.WRAP_CONTENT,
					FrameLayout.LayoutParams.WRAP_CONTENT,
					Gravity.RIGHT + Gravity.CENTER_VERTICAL);
			embargos = new TextView(top);
			embargos.setText("0");
			embargos.setTextSize((float) (embargos.getTextSize() * 0.75));
			embargos.setTextColor(Color.WHITE);
			embargos.setBackgroundResource(R.drawable.embargos);
			embargos.setLayoutParams(p);
			embargos.setVisibility(INVISIBLE);
			addView(embargos);
			
			setCard(c);
	
			checked = new TextView(top);
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
			
			nomore = new TextView(top);
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

	    	setOnLongClickListener( new OnLongClickListener (){
				@Override
				public boolean onLongClick(View v) {
					return longClick(v);
				} 
			}); 
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
    		setCost(GameTable.getCardCost(c));
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

	protected boolean longClick(View arg0) {	
		return lc.onLongClick(this);
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

	public void setCost(int newCost) {
		if (cost == null) return;
		cost.setText(" " + newCost + " ");
		if (c != null && c.costPotion)
			cost.setBackgroundResource(R.drawable.coinpotion);
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

}
