package com.mehtank.androminion.ui;

import java.util.ArrayList;

import com.mehtank.androminion.comms.MyCard;

import android.content.Context;
import android.view.View;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GameTableViews {
    static double textScale = 0.8;
    static ArrayList<MyCard> cardsInPlay = new ArrayList<MyCard>();

    static void clearCards() {
    	cardsInPlay.clear();
    }
    static void addCard(int id, MyCard c) {
    	cardsInPlay.add(id, c);
    }
	static GridView makeGV(Context top, CardGroup a, int n) {
		GridView gv = new GridView(top);
		gv.setAdapter(a);
		gv.setNumColumns(n);
		if (n == 1) {
			gv.setColumnWidth(CardView.WIDTH);
			gv.setStretchMode(GridView.NO_STRETCH);
		} else {
			gv.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
		}
		return gv;
	}
	static LinearLayout myCardSet(Context top, String header, View v, TextView title) {
    	LinearLayout ll = new LinearLayout(top);
    	ll.setOrientation(LinearLayout.VERTICAL);
    	
    	if(title == null) 
    	    title = new TextView(top);
    	title.setTextSize((float) (title.getTextSize() * textScale));
    	title.setText(header);
    	
    	ll.addView(title);
    	ll.addView(v);

    	return ll;
	}

	public static void newCardGroup(CardGroup cg, int[] cards) {
		cg.clear();
		for (int c : cards) {
			cg.addCard(cardsInPlay.get(c));
		}
	}
	
	public static CardView getCardView(Context context, GameTable gt, int card) {
		return new CardView(context, gt, gt, null, cardsInPlay.get(card));
	}
}
