package com.mehtank.androminion.util;

import java.util.ArrayList;

import com.mehtank.androminion.ui.CardView;
import com.mehtank.androminion.ui.CardView.CardState;
import com.vdom.comms.MyCard;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class CardGroup extends BaseAdapter {
	private Context top;
	private boolean onTable = false;
	private ArrayList<CardState> cards = new ArrayList<CardState>();

	public CardGroup(Context top, boolean onTable) {
		this.top = top;
		this.onTable = onTable;
	}

	public void addCard(MyCard c) {
		CardState ci = new CardState(c);
		ci.onTable = onTable;
		if (onTable) { // sort cards that are on the table
			int i = 0;
			for (i=0; i < cards.size(); i++) {
				if (cards.get(i).c.cost > c.cost)
					break;
			}
			cards.add(i, ci);
		} else
			cards.add(ci);
		notifyDataSetChanged();
	}

	public void updateState(int pos, CardState cs){
		cards.set(pos, cs);
		notifyDataSetChanged();
	}

	public void removeCard(int pos) {
		cards.remove(pos);
		notifyDataSetChanged();
	}

	public void clear() {
		cards.clear();
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return cards.size();
	}

	@Override
	public Object getItem(int pos) {
		return cards.get(pos);
	}

	@Override
	public long getItemId(int pos) {
		return 0;
	}

	@Override
	public View getView(int pos, View origView, ViewGroup parent) {
		CardView cv;
		CardState cs = cards.get(pos);
		if(origView == null) {
			cv = new CardView(top, this, null);
		} else {
			cv = (CardView) origView;
		}
		cv.setState(cs);
		return cv;
	}
}
