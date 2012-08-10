package com.mehtank.androminion.ui;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.mehtank.androminion.R;

/**
 * Displays score etc upon end of game.
 *
 */
public class FinalView extends FrameLayout implements OnClickListener {
	@SuppressWarnings("unused")
	private static final String TAG = "FinalView";
	
	private GameTable gt;
	private int[] cardCounts, embargos;

	private TextView name;
	private Button showCards;

	public FinalView(Context context, GameTable gt, String nameStr, int numTurns, int[] embargos, int numCards, int[] cardCounts, int vp, boolean winner) {
		super(context);

		this.gt = gt;
		this.embargos = embargos;
		this.cardCounts = cardCounts;

		LayoutInflater.from(context).inflate(R.layout.view_final, this, true);
		name = (TextView) findViewById(R.id.name);
		name.setText(context.getString(R.string.final_view_text, nameStr, "" + vp, "" + numTurns, "" + numCards));
		if (winner) {
			name.setTextColor(Color.BLACK);
			name.setBackgroundColor(Color.YELLOW);
		}
		
		showCards = (Button) findViewById(R.id.showCards);
        showCards.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		gt.setSupplySizes(cardCounts, embargos);
	}
}
