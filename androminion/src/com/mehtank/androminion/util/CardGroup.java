package com.mehtank.androminion.util;

import java.util.ArrayList;
import java.util.Comparator;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.mehtank.androminion.ui.CardView;
import com.mehtank.androminion.ui.CardView.CardState;
import com.vdom.comms.MyCard;

/**
 * Collection of cards (e.g. hand, row of piles) that is displayed in a row / table
 *
 */
public class CardGroup extends BaseAdapter {
	private static final String TAG = "CardGroup";
	
	private Context top;
	private boolean onTable = false;
	private ArrayList<CardState> cards = new ArrayList<CardState>();
	private Comparator<MyCard> cmp = new MyCard.CardCostNameComparator();
	private boolean sorted = false;
	
	// fix bug that lets item countLeft jump around
	int[] supplySizes = null;
	int[] embargos = null;
	
	public void updateCounts(int[] supplySizes, int[] embargos) {
		this.supplySizes = supplySizes;
		this.embargos = embargos;
		notifyDataSetChanged();
	}


	public CardGroup(Context top, boolean onTable) {
		this.top = top;
		this.onTable = onTable;
	}
	
	public void addCard(MyCard c) {
		addCard(c, true);
	}

	public void addCard(MyCard c, boolean state) {
		CardState ci = new CardState(c, false, "", -1, !state);
		ci.onTable = onTable;
		if (onTable || sorted) { // sort cards that are on the table
			int i = 0;
			for (i=0; i < cards.size(); i++) {
				if(cmp.compare(c, cards.get(i).c) < 0)
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
		try {
			if (supplySizes != null)
				cv.setCountLeft(supplySizes[cs.c.id]);
			if (embargos != null)
				cv.setEmbargos(embargos[cs.c.id]);
		} catch (ArrayIndexOutOfBoundsException e) {
			// TODO See why this is happening?
			Log.w(TAG, "exception", e);
		}

		return cv;
	}
	
	public void updateCardName(int index, String s, String d) {
		updateCardName(index, s, d, -1);
	}
	public void updateCardName(int index, String s, String d, int c) {
		for (CardState cs : cards) {
			if (cs.c.id == index) {
				cs.c.name = s;
				cs.c.desc = d;
				if (c >= 0) cs.c.cost = c;
			}
		}
	}

	public int getPos(int index) {
		for (int i = 0; i < cards.size(); i++) {
			if (cards.get(i).c.id == index) 
				return i;
		}
		return -1;
	}
}
