package com.mehtank.androminion.ui;

import java.util.ArrayList;

import com.mehtank.androminion.comms.MyCard;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.BaseAdapter;

public class CardGroup extends BaseAdapter {
	Context top;
	OnClickListener gt;
	OnLongClickListener lc;
	boolean onTable = false;
	boolean suppressNewCardViews = false;
	ArrayList<MyCard> cards = new ArrayList<MyCard>();
	
	CardView nullCV;
	int nullPos = Integer.MAX_VALUE;
	
	public CardGroup(Context top, OnClickListener gt, OnLongClickListener lc, boolean onTable) {
		this.top = top;
		this.onTable = onTable;
		this.gt = gt;		
		this.lc = lc;
	}
	
	public CardGroup(Context top, OnClickListener gt, OnLongClickListener lc, boolean onTable, int nullPos) {
		this.top = top;
		this.onTable = onTable;
		this.gt = gt;
		this.lc = lc;

		nullCV = new CardView(top, gt, lc, this, null);
		this.nullPos = nullPos;
	}
	
	public void addCard(MyCard c) {
		if (onTable) { // sort
			int i = 0;
			for (i=0; i < cards.size(); i++) {
				if (cards.get(i).cost > c.cost) 
					break;
			}
			cards.add(i, c);
		} else
			cards.add(c);
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
		int s = cards.size();
		if (s >= nullPos)
			s++;
		return s;
	}

	@Override
	public Object getItem(int pos) {
		if (pos > nullPos)
			pos--;
		return cards.get(pos);
	}

	@Override
	public long getItemId(int pos) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int pos, View origView, ViewGroup parent) {
		CardView cv;
		if (origView != null) {
			cv = (CardView) origView;
			if (pos == nullPos) 
				return nullCV;
			if (cv.c == cards.get((pos > nullPos) ? pos-1 : pos))
				return cv;
		}
		
		if (pos < nullPos)
			cv = new CardView(top, gt, lc, this, cards.get(pos));
		else if (pos == nullPos)
			cv = nullCV;
		else
			cv = new CardView(top, gt, lc, this, cards.get(pos-1));
        return cv;
	}

}
