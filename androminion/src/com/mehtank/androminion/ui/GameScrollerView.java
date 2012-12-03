package com.mehtank.androminion.ui;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.mehtank.androminion.R;
import com.mehtank.androminion.activities.GameActivity;

/**
 * Display of game log
 * 
 */
public class GameScrollerView extends HorizontalScrollView {
	private static final String TAG = "GameScrollerView";

	private Context top;
	private LinearLayout gameEventsRow;
	private ScrollView latestTurnSV;
	private TextView latestTurn;
	private boolean onlyShowOneTurn = false;
	private int numPlayers;
	private ArrayList<View> views = new ArrayList<View>();
	private File logfile;

	public GameScrollerView(Context context) {
		this(context, null);
	}

	public GameScrollerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.top = context;

		gameEventsRow = new LinearLayout(top);
		gameEventsRow.setOrientation(LinearLayout.HORIZONTAL);

		addView(gameEventsRow);
	}

	public void clear() {
		gameEventsRow.removeAllViews();
		String filename = "/latest.txt";

		if (PreferenceManager.getDefaultSharedPreferences(top).getBoolean("enable_logging", false)) {
			filename = new SimpleDateFormat("'/log_'yyyy-MM-dd_HH-mm-ss'.txt'", Locale.US).format(new Date());
		}
			
		String dir = GameActivity.BASEDIR + PreferenceManager.getDefaultSharedPreferences(top).getString("logdir", "");
		new File(dir).mkdirs();

		logfile = new File(dir + filename);
		logfile.delete();
		Log.e("Logging", dir + filename);
		try {
			logfile.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, "Failed");
			logfile = null;
			e.printStackTrace();
		}
		
		if (logfile != null && logfile.canWrite()) {
			try {
				FileWriter f = new FileWriter(logfile.getCanonicalPath(), true); // append to file
				f.write(new SimpleDateFormat("'New game started on 'yyyy/MM/dd' at 'HH:mm:ss'\n'", Locale.US).format(new Date()));
				f.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public void setNumPlayers(int numPlayers) {
		this.numPlayers = numPlayers;
		if (PreferenceManager.getDefaultSharedPreferences(top).getBoolean("one_turn_logs", false)) {
			onlyShowOneTurn = true;
		}
	}

	public void setGameEvent(String s, boolean b, int turnCount) {
		if (b) {
			latestTurnSV = (ScrollView) LayoutInflater.from(top).inflate(R.layout.view_gamescrollercolumn, gameEventsRow, false);

			@SuppressWarnings("deprecation")
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT);
			layoutParams.setMargins((int) getResources().getDimension(R.dimen.margin_gamelog),
					(int) getResources().getDimension(R.dimen.margin_gamelog),
					(int) getResources().getDimension(R.dimen.margin_gamelog),
					(int) getResources().getDimension(R.dimen.margin_gamelog));
			gameEventsRow.addView(latestTurnSV, layoutParams);

			latestTurn = (TextView) latestTurnSV.findViewById(R.id.latestTurn);
			latestTurn.setText(s + (turnCount > 0 ? (top.getString(R.string.turn_header) + turnCount) : ""));

			latestTurn.setPadding(0, 0, (int) getResources().getDimension(R.dimen.margin_gamelog), 0);
			
			if (onlyShowOneTurn) {
				views.add(latestTurnSV);
				while (views.size() > numPlayers + 1) {
					View view = views.remove(0);
					gameEventsRow.removeView(view);
				}
			}
		} else
			latestTurn.setText(latestTurn.getText() + "\n" + s);

		latestTurnSV.fullScroll(FOCUS_DOWN);
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
