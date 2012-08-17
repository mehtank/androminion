package com.mehtank.androminion.ui;

import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mehtank.androminion.Androminion;
import com.mehtank.androminion.R;

public abstract class BottomInputView extends LinearLayout {
	Androminion top;
	TextView title;
	View content;
	TextView down;
	TextView up;
	boolean hidden = false;
		
	public BottomInputView (Androminion top, String header) {
		super(top);
		
		this.top = top;
		
		FrameLayout fl = new FrameLayout(top);
		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.FILL_PARENT,
				FrameLayout.LayoutParams.WRAP_CONTENT,
				Gravity.CENTER);

		title = new TextView(top);
		title.setText(header);
		title.setBackgroundColor(Color.BLUE);
		title.setTextColor(Color.WHITE);
		title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
		title.setPadding(5, 5, 5, 5);
		title.setOnClickListener( new OnClickListener (){
			@Override
			public void onClick(View v) {
				toggle();
			} 
		}); 
		title.setLayoutParams(lp);
		
		down = new TextView(top);
		down.setVisibility(VISIBLE);
		down.setBackgroundResource(android.R.drawable.arrow_down_float);
		up = new TextView(top);
		up.setVisibility(INVISIBLE);
		up.setBackgroundResource(android.R.drawable.arrow_up_float);
		
		lp = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.WRAP_CONTENT,
				FrameLayout.LayoutParams.WRAP_CONTENT,
				Gravity.CENTER_VERTICAL + Gravity.RIGHT);
		
		down.setLayoutParams (lp);
		up.setLayoutParams(lp);

		fl.addView(title);
		fl.addView(up);
		fl.addView(down);

		content = makeContentView(top);
		
		lp = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.FILL_PARENT,
				FrameLayout.LayoutParams.WRAP_CONTENT,
				Gravity.BOTTOM + Gravity.CENTER_HORIZONTAL);

		setOrientation(VERTICAL);
		addView(fl);
		addView(content);
		setLayoutParams(lp);
		setBackgroundResource(R.drawable.solidround);

		top.addView(this);
	}

	abstract protected View makeContentView(Androminion top);
	
	public void toggle() {
		if (hidden) {
			addView(content);
			title.setText(((String) title.getText()).trim());
			up.setVisibility(INVISIBLE);
			down.setVisibility(VISIBLE);
		} else {
			title.setText(title.getText() + "   ");
			removeView(content);
			up.setVisibility(VISIBLE);
			down.setVisibility(INVISIBLE);
		}
		hidden = !hidden;
	}
}