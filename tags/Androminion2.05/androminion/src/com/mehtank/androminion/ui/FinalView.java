package com.mehtank.androminion.ui;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.mehtank.androminion.Androminion;
import com.mehtank.androminion.R;

public class FinalView extends FrameLayout implements OnClickListener {
	
	Androminion top;
	GameTable gt;

	TextView tv;
	TextView name;
	int[] cardCounts, embargos;
	Button showCards;
		
	public FinalView(Context context, GameTable gt, String nameStr, int numTurns, int[] embargos, int numCards, int[] cardCounts, int vp, boolean winner) {
		super(context);

		this.top = (Androminion) context;
		this.gt = gt;
		this.embargos = embargos;
		this.cardCounts = cardCounts;

		FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.WRAP_CONTENT,
				FrameLayout.LayoutParams.WRAP_CONTENT,
				Gravity.LEFT);

		name = new TextView(top);
		name.setText(Strings.format(top, R.string.final_view_text, nameStr, "" + vp, "" + numTurns, "" + numCards));
		name.setLayoutParams(p);
		if (winner) {
			name.setTextColor(Color.BLACK);
			name.setBackgroundColor(Color.YELLOW);
		}
		addView(name);		

		p = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.WRAP_CONTENT,
				FrameLayout.LayoutParams.WRAP_CONTENT,
				Gravity.RIGHT);
		
		showCards = new Button(top);
		showCards.setLayoutParams(p);		
		showCards.setText(Strings.getString(top, R.string.final_view_card_counts));

        showCards.setOnClickListener(this);
		addView(showCards);
	}

	@Override
	public void onClick(View v) {
		gt.setSupplySizes(cardCounts, embargos);	
		gt.showSupplySizes();
	}
}
