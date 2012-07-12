package com.mehtank.androminion.util;

import java.util.List;

import com.mehtank.androminion.ui.DeckView;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class PlayerAdapter extends ArrayAdapter<PlayerSummary> {

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
		dv.set(ps.name, ps.turns, ps.deckSize, ps.handSize, ps.numCards, ps.pt, ps.vt, ps.highlight);
		return dv;
	}
}
