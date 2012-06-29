package com.mehtank.androminion.ui;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mehtank.androminion.Androminion;
import com.mehtank.androminion.R;

public abstract class BottomInputView extends RelativeLayout implements OnClickListener{
	protected Androminion top;
	private TextView title;
	private ImageView arrow;
	private View content;
	private boolean hidden = false;

	public BottomInputView (Androminion top, String header) {
		super(top);
		this.top = top;
		
		LayoutInflater.from(top).inflate(R.layout.bottominputview, this, true);
		setBackgroundResource(R.drawable.solidround);
	    setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL));
		title = (TextView) findViewById(R.id.title);
		title.setText(header);
		title.setOnClickListener(this);
		arrow = (ImageView) findViewById(R.id.arrow);
		content = makeContentView(top);
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		lp.addRule(BELOW, R.id.title);
		content.setLayoutParams(lp);
		addView(content);
		//content = (FrameLayout) findViewById(R.id.content);
		//content.addView(makeContentView(top));
		top.addView(this);
	}

	abstract protected View makeContentView(Androminion activity);

	public void toggle() {
		if (hidden) {
			content.setVisibility(VISIBLE);
			arrow.setImageResource(android.R.drawable.arrow_down_float);
		} else {
			content.setVisibility(GONE);
			arrow.setImageResource(android.R.drawable.arrow_up_float);
		}
		hidden = !hidden;
	}
	
	@Override
	public void onClick(View v) {
		toggle();
	}
}