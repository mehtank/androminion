package com.mehtank.androminion.ui;

import android.annotation.SuppressLint;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mehtank.androminion.R;
import com.mehtank.androminion.activities.GameActivity;

/**
 * This class gives the frame and header for the choosing windows at the bottom.
 * 
 * The subclass has to overload makeContentView to generate what is supposed to
 * be shown.
 * 
 */
@SuppressLint("ViewConstructor")
public abstract class BottomInputView extends RelativeLayout implements OnClickListener {
	@SuppressWarnings("unused")
	private static final String TAG = "BottomInputView";

	protected GameActivity top;
	private TextView title;
	private ImageView arrow;
	private View content;
	private boolean hidden = false;

	public BottomInputView(GameActivity top, String header) {
		super(top);
		this.top = top;

		LayoutInflater.from(top).inflate(R.layout.view_bottominput, this, true); // title
		setBackgroundResource(R.drawable.solidround); // frame
		setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL));
		title = (TextView) findViewById(R.id.title);
		title.setText(header);
		title.setOnClickListener(this);
		arrow = (ImageView) findViewById(R.id.arrow);
		content = makeContentView(top);
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		lp.addRule(BELOW, R.id.title);
		content.setLayoutParams(lp);
		addView(content);
		//content = (FrameLayout) findViewById(R.id.content);
		//content.addView(makeContentView(top));
		top.addView(this);
	}

	/**
	 * Is called by the constructor
	 * 
	 * @param activity
	 *            GameActivity object
	 * @return content view
	 */
	abstract protected View makeContentView(GameActivity activity);

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