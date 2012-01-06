package com.mehtank.androminion.ui;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.mehtank.androminion.Androminion;
import com.mehtank.androminion.R;

public class GameScrollerView extends HorizontalScrollView {
	Context top;
	LinearLayout gameEventsRow;
	private TextView latestTurn;
	private double textScale;
	private boolean onlyShowOneTurn = false;
	private int numPlayers;
	private ArrayList<View> views = new ArrayList<View>();
	private File logfile;
	
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
		
		if (PreferenceManager.getDefaultSharedPreferences(top).getBoolean("enable_logging", false)) {
			String dir = Androminion.BASEDIR + PreferenceManager.getDefaultSharedPreferences(top).getString("logdir", "");
			String filename = new SimpleDateFormat("'/log_'yyyy-MM-dd_HH-mm-ss'.txt'").format(new Date());
			new File(dir).mkdirs();
			
			logfile = new File(dir + filename);
			Log.e("Logging", dir + filename);
			try {
				logfile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.e("Logging", "Failed");
				logfile = null;
				e.printStackTrace();
			}
		}
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
		
		if (logfile != null && logfile.canWrite()) {
			try {
				FileWriter f = new FileWriter(logfile.getCanonicalPath(), true); // append to file
				if (b)
					f.write(top.getString(R.string.log_turn_separator));
				f.write(s + "\n");
				f.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
