package com.mehtank.androminion.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mehtank.androminion.R;
import com.vdom.comms.GameStatus.JourneyTokenState;

public class DeckView extends RelativeLayout {
	@SuppressWarnings("unused")
	private static final String TAG = "DeckView";
	
	private TextView name;
	private TextView pirates;
	private TextView victoryTokens;
	private TextView guildsCoinTokens;
	private TextView journeyToken;
	private TextView minusOneCoinToken;
	private TextView minusOneCardToken;
	private LinearLayout counts;
	private TextView countsPrefix;
	private TextView countsDeck;
	private TextView countsMiddle;
	private TextView countsStashesInHand;
	private TextView countsSuffix;
	
	private int textColor;
	private int stashColor;

	private boolean showCardCounts = true;

	public DeckView(Context context) {
		this(context, null);
	}

	public DeckView(Context context, AttributeSet attrs) {
		super(context, attrs);

        LayoutInflater.from(context).inflate(R.layout.view_deck, this, true);
		name = (TextView) findViewById(R.id.name);
		pirates = (TextView) findViewById(R.id.pirates);
		victoryTokens = (TextView) findViewById(R.id.victoryTokens);
		guildsCoinTokens = (TextView) findViewById(R.id.guildsCoinTokens);
		journeyToken = (TextView) findViewById(R.id.journeyToken);
		minusOneCoinToken = (TextView) findViewById(R.id.minusOneCoinToken);
		minusOneCardToken = (TextView) findViewById(R.id.minusOneCardToken);
		counts = (LinearLayout) findViewById(R.id.counts);
		countsPrefix = (TextView) findViewById(R.id.countsPrefix);
		countsDeck = (TextView) findViewById(R.id.countsDeck);
		countsMiddle = (TextView) findViewById(R.id.countsMiddle);
		countsStashesInHand = (TextView) findViewById(R.id.countsStashesInHand);
		countsSuffix = (TextView) findViewById(R.id.countsSuffix);

        if(PreferenceManager.getDefaultSharedPreferences(context).getBoolean("hide_card_counts", false)) {
            showCardCounts = false;
            counts.setVisibility(INVISIBLE);
        }
        
        TypedValue typedValue = new TypedValue();
		context.getTheme().resolveAttribute(R.attr.stashTextColor, typedValue, true);
		stashColor = typedValue.data;
		
		textColor = new TextView(context).getTextColors().getDefaultColor();

	}

	public void set(String nameStr, int turns, int deckSize, boolean stashOnDeck, int handSize, int stashesInHand, int numCards, 
			int pt, int vt, int gct, 
			boolean minusOneCoinTokenOn, boolean minusOneCardTokenOn, JourneyTokenState journeyTokenState, 
			boolean highlight, boolean showColor, int color) {
		String txt = nameStr + getContext().getString(R.string.turn_header) + turns;
		name.setText(txt);
		if (highlight) {
//			name.setTextColor(Color.BLACK);
//			name.setBackgroundColor(Color.GRAY);
			name.setTypeface(Typeface.DEFAULT_BOLD);
		} else {
//			name.setTextColor(Color.WHITE);
//			name.setBackgroundColor(Color.TRANSPARENT);
			name.setTypeface(Typeface.DEFAULT);
		}
		if (showColor) {
			name.setBackgroundColor(color);
		} else {
			name.setBackgroundColor(Color.TRANSPARENT);
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
        
        guildsCoinTokens.setText(" " + gct + " ");
        if (gct != 0)
            guildsCoinTokens.setVisibility(VISIBLE);
        else
            guildsCoinTokens.setVisibility(INVISIBLE);

        journeyToken.setTextColor(journeyTokenState == JourneyTokenState.FACE_UP ? Color.BLACK : Color.TRANSPARENT);
        if (journeyTokenState == null)
            journeyToken.setVisibility(INVISIBLE);
        else
            journeyToken.setVisibility(VISIBLE);
        
        minusOneCardToken.setText(" -1 ");
        if (minusOneCardTokenOn)
        	minusOneCardToken.setVisibility(VISIBLE);
        else
        	minusOneCardToken.setVisibility(INVISIBLE);
        
        minusOneCoinToken.setText(" -1 ");
        if (minusOneCoinTokenOn)
        	minusOneCoinToken.setVisibility(VISIBLE);
        else
        	minusOneCoinToken.setVisibility(INVISIBLE);
        
        if(showCardCounts) {
        	countsPrefix.setText("{ ");
        	countsDeck.setText("\u2261 ");
        	countsDeck.setTextColor(stashOnDeck ? stashColor : textColor);
        	countsMiddle.setText(deckSize + "    \u261e " + handSize);
        	countsStashesInHand.setText(stashesInHand == 0 ? "" : " (" + stashesInHand + ")");
        	countsSuffix.setText("    \u03a3 " + numCards + " }");
        }
	}
}
