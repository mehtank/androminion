package com.mehtank.androminion.ui;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.mehtank.androminion.R;

/**
 * Displays score etc upon end of game.
 *
 */
public class FinalView extends FrameLayout implements OnClickListener {
	@SuppressWarnings("unused")
	private static final String TAG = "FinalView";
	
	private GameTable gt;
	private int[] cardCounts, embargos, pileVpTokens, pileDebtTokens, pileTradeRouteTokens;
	private int[][][] tokens;

	private TextView name;
	public ToggleButton showCards;

	public FinalView(Context context, GameTable gt, String nameStr, int numTurns, int[] embargos, int[] pileVpTokens, int[] pileDebtTokens, int[] pileTradeRouteTokens, int[][][] tokens, int numCards, int[] cardCounts, int vp, boolean winner) {
		super(context);

		this.gt = gt;
		this.embargos = embargos;
		this.pileVpTokens = pileVpTokens;
		this.pileDebtTokens = pileDebtTokens;
		this.pileTradeRouteTokens = pileTradeRouteTokens;
		this.tokens = tokens;
		this.cardCounts = cardCounts;

		LayoutInflater.from(context).inflate(R.layout.view_final, this, true);
		name = (TextView) findViewById(R.id.name);
		name.setText(context.getString(R.string.final_view_text, nameStr, "" + vp, "" + numTurns, "" + numCards));
		if (winner) {
			name.setTextColor(Color.BLACK);
			name.setBackgroundColor(Color.YELLOW);
		}
		
		showCards = (ToggleButton) findViewById(R.id.showCards);
        showCards.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (showCards.isChecked()) {
			gt.uncheckAllShowCardsButtons();
			gt.setSupplySizes(cardCounts, embargos, pileVpTokens, pileDebtTokens, pileTradeRouteTokens, tokens);
			showCards.setChecked(true);
		} else
			gt.uncheckAllShowCardsButtons();
	}
}
