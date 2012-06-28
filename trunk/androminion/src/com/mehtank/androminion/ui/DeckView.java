package com.mehtank.androminion.ui;

import java.util.ArrayList;

import com.mehtank.androminion.R;

import android.content.Context;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DeckView extends RelativeLayout {
	private TextView name;
	private TextView pirates;
	private TextView victoryTokens;
	private TextView counts;

	private boolean showCardCounts = true;

	public static enum ShowCardType {OBTAINED, TRASHED, REVEALED};

	public DeckView(Context context) {
		this(context, null);
	}

	public DeckView(Context context, AttributeSet attrs) {
		super(context, attrs);

        LayoutInflater.from(context).inflate(R.layout.deckview, this, true);
		name = (TextView) findViewById(R.id.name);
		pirates = (TextView) findViewById(R.id.pirates);
		victoryTokens = (TextView) findViewById(R.id.victoryTokens);
		counts = (TextView) findViewById(R.id.counts);

        if(PreferenceManager.getDefaultSharedPreferences(context).getBoolean("hide_card_counts", false)) {
            showCardCounts = false;
            counts.setVisibility(INVISIBLE);
        }
	}

	public void set(String nameStr, int deckSize, int handSize, int numCards, int pt, int vt, boolean highlight) {
		name.setText(nameStr);
		if (highlight) {
			name.setTextColor(Color.BLACK);
			name.setBackgroundColor(Color.GRAY);
		} else {
			name.setTextColor(Color.WHITE);
			name.setBackgroundColor(Color.BLACK);
		}

		pirates.setText(" " + pt + " ");
		if (pt != 0)
			pirates.setVisibility(VISIBLE);
		else
			pirates.setVisibility(INVISIBLE);

        victoryTokens.setText(" " + vt + " ");
        if (vt != 0)
            victoryTokens.setVisibility(VISIBLE);
        else
            victoryTokens.setVisibility(INVISIBLE);

        if(showCardCounts) {
    		String str = "{ \u2261 " + deckSize +
    					 "    \u261e " + handSize +
    					 "    \u03a3 " + numCards + " }";
    		counts.setText(str);
        }
	}

	public void showCard(CardView c, ShowCardType type) {
		AlphaAnimation alpha;
		TranslateAnimation trans;
		AnimationSet anims = new AnimationSet(true);
		anims.setInterpolator(new LinearInterpolator());

		int left = getLeft();
		int top = getTop();
		ViewParent vp = getParent();
		while (vp != getRootView()) {
			left += ((View)vp).getLeft();
			top += ((View)vp).getTop();
			vp = vp.getParent();
		}

		switch (type) {
		case OBTAINED:
			alpha = new AlphaAnimation(0, 1);
			trans = new TranslateAnimation(
					Animation.ABSOLUTE, left,
					Animation.ABSOLUTE, left,
					Animation.ABSOLUTE, top - getHeight()*2,
					Animation.ABSOLUTE, top);
			anims.setInterpolator(new DecelerateInterpolator());
			break;
		case TRASHED:
			alpha = new AlphaAnimation(1, 0);
			trans = new TranslateAnimation(
					Animation.ABSOLUTE, left,
					Animation.ABSOLUTE, left,
					Animation.ABSOLUTE, top,
					Animation.ABSOLUTE, top + getHeight()*2);
			anims.setInterpolator(new AccelerateInterpolator());
			break;
		default: //  REVEALED
			alpha = new AlphaAnimation(1, 0.5f);
			trans = new TranslateAnimation(
					Animation.ABSOLUTE, left,
					Animation.ABSOLUTE, left,
					Animation.ABSOLUTE, top,
					Animation.ABSOLUTE, top - getHeight()*0.5f);
		}
		anims.addAnimation(alpha);
		anims.addAnimation(trans);
		anims.setDuration(2500L);

		anims.setAnimationListener(new CVAnimListener(c));

		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(CardView.WIDTH, ViewGroup.LayoutParams.WRAP_CONTENT);
		c.setLayoutParams(lp);
		((ViewGroup) getRootView()).addView(c);
		c.startAnimation(anims);

		cvs.add(c);
		runningAnims.add(anims);
	}

	static ArrayList<AnimationSet> runningAnims = new ArrayList<AnimationSet>();
	static ArrayList<CardView> cvs = new ArrayList<CardView>();

	private class CVAnimListener implements AnimationListener {
		CardView v;
		public CVAnimListener(CardView v) {
			this.v = v;
		}
		@Override
		public void onAnimationEnd(Animation animation) {
			v.setVisibility(GONE);
			runningAnims.remove(animation);
			if (runningAnims.size() == 0) {
				for (CardView c : cvs)
					((ViewGroup) getRootView()).removeView(c);
				cvs.clear();
			}
		}
		@Override public void onAnimationRepeat(Animation animation) {}
		@Override public void onAnimationStart(Animation animation) {}
	}
}
