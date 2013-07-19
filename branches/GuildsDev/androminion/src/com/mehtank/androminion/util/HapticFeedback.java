package com.mehtank.androminion.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Vibrator;
import android.preference.PreferenceManager;

public class HapticFeedback {
	@SuppressWarnings("unused")
	private static final String TAG = "HapticFeedback";
	
	public static enum AlertType {
		CHAT, TURNBEGIN, SELECT, CLICK, LONGCLICK, FINAL,
	};

	public static void vibrate(Context context, AlertType t) {
		SharedPreferences prefs;
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
		
		if (!prefs.getBoolean("allvibeson",false))
			return;
		Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
		
		switch (t) {
		case CHAT:
			if (prefs.getBoolean("chatvibeon", false))
				v.vibrate(new long[] { 0, 40, 100, 40 }, -1);
			break;
		case TURNBEGIN:
			if (prefs.getBoolean("turnvibeon", false))
				v.vibrate(new long[] { 0, 50, 20, 40, 20, 30 }, -1);
			break;
		case SELECT:
			if (prefs.getBoolean("actionvibeon", false))
				v.vibrate(new long[] {1, 75}, -1);
			break;
		case CLICK:
			if (prefs.getBoolean("clickvibeon", false))
				v.vibrate(new long[] {1, 20}, -1);
			break;
		case LONGCLICK:
			if (prefs.getBoolean("clickvibeon", false))
				v.vibrate(new long[] {1, 40}, -1);
			break;
		case FINAL:
			if (prefs.getBoolean("gamevibeon", false))
				v.vibrate(new long[] {1, 250}, -1);
			break;
		}
	}
}
