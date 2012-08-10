package com.mehtank.androminion.ui;

import java.util.ArrayList;

import com.mehtank.androminion.R;

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


public class CardAnimator {
	@SuppressWarnings("unused")
	private static final String TAG = "CardAnimator";
	
	public static enum ShowCardType {OBTAINED, TRASHED, REVEALED};
	
	private ViewGroup rootView;
	private int left;
	private int top;
	private int height;
	
	public void init(View anchor) {
		rootView = (ViewGroup) anchor.getRootView();
		int[] location = new int[2];
		anchor.getLocationOnScreen(location);
		left = location[0];
		top = location[1];
		height = anchor.getHeight();
		/* ViewParent vp = anchor.getParent();
		while (vp != rootView) {
			left += ((View)vp).getLeft();
			top += ((View)vp).getTop();
			vp = vp.getParent();
		} */
	}
	
	
	public void showCard(CardView c, ShowCardType type) {
		AlphaAnimation alpha;
		TranslateAnimation trans;
		AnimationSet anims = new AnimationSet(true);
		anims.setInterpolator(new LinearInterpolator());
		int cardWidth = (int) c.getResources().getDimension(R.dimen.cardWidth);
		switch (type) {
		case OBTAINED:
			alpha = new AlphaAnimation(0, 1);
			trans = new TranslateAnimation(
					Animation.ABSOLUTE, left,
					Animation.ABSOLUTE, left,
					Animation.ABSOLUTE, top - height*.5f,
					Animation.ABSOLUTE, top );
			anims.setInterpolator(new DecelerateInterpolator());
			break;
		case TRASHED:
			alpha = new AlphaAnimation(1, 0);
			trans = new TranslateAnimation(
					Animation.ABSOLUTE, left,
					Animation.ABSOLUTE, left,
					Animation.ABSOLUTE, top,
					Animation.ABSOLUTE, top + height*.5f);
			anims.setInterpolator(new AccelerateInterpolator());
			break;
		default: //  REVEALED
			alpha = new AlphaAnimation(1, 0.5f);
			trans = new TranslateAnimation(
					Animation.ABSOLUTE, left + cardWidth,
					Animation.ABSOLUTE, left + cardWidth,
					Animation.ABSOLUTE, top,
					Animation.ABSOLUTE, top + height*.3f);
		}
		alpha.setDuration(500);
		trans.setDuration(500);
		anims.addAnimation(alpha);
		anims.addAnimation(trans);
		trans = new TranslateAnimation(0, 0, 0, 0);
		trans.setDuration(2000); //
		anims.addAnimation(trans);


		anims.setAnimationListener(new CVAnimListener(c));

		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(cardWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
		c.setLayoutParams(lp);
		rootView.addView(c);
		c.startAnimation(anims);

		cvs.add(c);
		runningAnims.add(anims);
	}

	private static ArrayList<AnimationSet> runningAnims = new ArrayList<AnimationSet>();
	private static ArrayList<CardView> cvs = new ArrayList<CardView>();

	private class CVAnimListener implements AnimationListener {
		CardView v;
		public CVAnimListener(CardView v) {
			this.v = v;
		}
		@Override
		public void onAnimationEnd(Animation animation) {
			v.setVisibility(View.GONE);
			runningAnims.remove(animation);
			if (runningAnims.size() == 0) {
				for (CardView c : cvs)
					rootView.removeView(c);
				cvs.clear();
			}
		}
		@Override public void onAnimationRepeat(Animation animation) {}
		@Override public void onAnimationStart(Animation animation) {}
	}
}
