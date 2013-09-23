package com.mehtank.androminion.util;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;

import com.mehtank.androminion.ui.DeckView;

public class PlayerAdapter extends ArrayAdapter<PlayerSummary> {
	@SuppressWarnings("unused")
	private static final String TAG = "PlayerAdapter";
	
	private LinearLayout container = null;

	private ArrayList<View> deckStatusItems = new ArrayList<View>();
	
	
	public void setTurnStatus(View ts) {
		container.addView(ts, 0);
	}
	
	public void setContainer(LinearLayout l) {
		container = l;
	}
	
	public View get(int p) {
		return deckStatusItems.get(p);
	}
	
	public PlayerAdapter(Context context) {
		super(context, 0);
	}
	
	public PlayerAdapter(Context context, List<PlayerSummary> players) {
		super(context, 0, players);
	}
	
	@Override
	public View getView(int pos, View origView, ViewGroup parent) {
		DeckView dv;
		PlayerSummary ps;
		if(origView == null) {
			dv = new DeckView(getContext());
		} else {
			dv = (DeckView) origView;
		}
		ps = getItem(pos);
		dv.set(ps.name, ps.turns, ps.deckSize, ps.handSize, ps.numCards, ps.pt, ps.vt, ps.gct, ps.highlight);
		return dv;
	}
	
	
	@Override
	public void notifyDataSetChanged() {
		if (container == null) {
			return;
		}
		int numItems = this.getCount();
		int realChildCount = container.getChildCount();
		for (int i = 0; i < numItems; i++) {
			View child = null;
			if (realChildCount > i) {
				child = container.getChildAt(i);
			}
			if (child == null) {
				child = getView(i, null, container);
				container.addView(child, i);
			} else {
				getView(i, child, container);
			}
		}
		for (int i = numItems; i < container.getChildCount(); i++) {
			container.removeViewAt(i);
		}
		
	}
}
