package com.mehtank.androminion.util;

import java.util.HashMap;

import com.mehtank.androminion.R;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class ThemeSetter {
	@SuppressWarnings("serial")
	private static final HashMap<String, Integer> THEMES = new HashMap<String , Integer>() {{
	    put("androminion", R.style.Androminon);
	    put("androminion-withstatus", R.style.Androminon_WithStatus);
	    put("androminion-light", R.style.Androminon_Light);
	    put("androminion-light-withstatus", R.style.Androminon_Light_WithStatus);
	    // sync with R.array.theme_keys and themes
	}};
	public static void set(Activity act) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(act);
		act.setTheme(THEMES.get(pref.getString("theme", "androminion")));
	}

}
