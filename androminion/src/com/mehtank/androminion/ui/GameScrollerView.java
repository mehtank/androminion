package com.mehtank.androminion.ui;

import java.util.ArrayList;

import android.content.Context;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.mehtank.androminion.R;

public class GameScrollerView extends HorizontalScrollView {
	Context top;
	LinearLayout gameEventsRow;
	private TextView latestTurn;
	private double textScale;
	private boolean onlyShowOneTurn = false;
	private int numPlayers;
	private ArrayList<View> views = new ArrayList<View>();
	
	public GameScrollerView(Context context, double textScale) {
		super(context);
		this.top = context;
		this.textScale = textScale;
		
    	gameEventsRow = new LinearLayout(top);
    	gameEventsRow.setOrientation(LinearLayout.HORIZONTAL);
    	addView(gameEventsRow);
	}

	public void clear() {
		gameEventsRow.removeAllViews();
	}
	
	public void setNumPlayers(int numPlayers) {
	    this.numPlayers = numPlayers;
        if(PreferenceManager.getDefaultSharedPreferences(top).getBoolean("one_turn_logs", false)) {
            onlyShowOneTurn = true;
        }
	}
	
	public void setGameEvent(String s, boolean b) {
		if (b) {
			latestTurn = new TextView(top);
			latestTurn.setTextSize((float) (latestTurn.getTextSize() * textScale));

			ScrollView sv = new ScrollView(top);
			sv.setBackgroundResource(R.drawable.roundborder);
			sv.addView(latestTurn);
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.FILL_PARENT);
			sv.setLayoutParams(lp);
			gameEventsRow.addView(sv);
			latestTurn.setText(s);
			
	        if(onlyShowOneTurn) {
	            views.add(sv);
	            while(views.size() > numPlayers + 1) {
	                View view = views.remove(0);
	                gameEventsRow.removeView(view);
	            }
	        }
		} else
			latestTurn.setText(latestTurn.getText() + "\n" + s);
		fullScroll(FOCUS_RIGHT);
	}
}
