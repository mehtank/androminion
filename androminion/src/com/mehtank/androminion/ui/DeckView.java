package com.mehtank.androminion.ui;

import com.mehtank.androminion.R;

import android.content.Context;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DeckView extends RelativeLayout {
	private TextView name;
	private TextView pirates;
	private TextView victoryTokens;
	private TextView counts;

	private boolean showCardCounts = true;

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
}
