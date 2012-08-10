package com.mehtank.androminion.ui;

import java.util.ArrayList;

import com.mehtank.androminion.R;
import com.mehtank.androminion.util.CardGroup;
import com.vdom.comms.MyCard;

import android.content.Context;
import android.view.View;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Keeps track of all the cards in play
 */
public class GameTableViews {
	@SuppressWarnings("unused")
	private static final String TAG = "GameTableViews";
	
    static double textScale = 0.8;
    static ArrayList<MyCard> cardsInPlay = new ArrayList<MyCard>();

    static void clearCards() {
    	cardsInPlay.clear();
    }
    static void addCard(int id, MyCard c) {
    	cardsInPlay.add(id, c);
    }
    /**
     * Generate a GridView
     * @param top GameActivity
     * @param a Will be taken as the Adapter
     * @param n Number of columns. If this is one, the card width will be fixed, otherwise the table will stretch to fit
     * @return the generated GridView
     */
	static GridView makeGV(Context top, CardGroup a, int n) {
		GridView gv = new GridView(top);
		gv.setAdapter(a);
		gv.setNumColumns(n);
		if (n == 1) {
			gv.setColumnWidth((int) top.getResources().getDimension(R.dimen.cardWidth));
			gv.setStretchMode(GridView.NO_STRETCH);
		} else {
			gv.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
		}
		return gv;
	}
	
	/**
	 * Generate LinearLayout with given title
	 * 
	 * @param top GameActivity object
	 * @param header Header string
	 * @param v View to add to LinearLayout
	 * @param title Header TextView, will be showing header
	 * @return the LinearLayout
	 */
	static LinearLayout myCardSet(Context top, String header, View v, TextView title) {
    	LinearLayout ll = new LinearLayout(top);
    	ll.setOrientation(LinearLayout.VERTICAL);
    	
    	if(title == null) 
    	    title = new TextView(top);
    	title.setTextSize(10.0f);
    	title.setText(header);
    	
    	ll.addView(title);
    	ll.addView(v);

    	return ll;
	}

	/**
	 * Fill the CardGroup object
	 * @param cg CardGroup to fill
	 * @param cards Array of Card-IDs
	 */
	public static void newCardGroup(CardGroup cg, int[] cards) {
		cg.clear();
		for (int c : cards) {
			cg.addCard(cardsInPlay.get(c));
		}
	}
	
	public static CardView getCardView(Context context, GameTable gt, int card) {
		return new CardView(context, null, cardsInPlay.get(card));
	}
}
