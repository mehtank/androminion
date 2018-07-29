package com.mehtank.androminion.util;

import java.util.HashMap;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.util.Log;

import com.mehtank.androminion.R;

/**
 * Helper class for maintaining the same theme throughout the application.
 */
public class ThemeSetter {
	@SuppressWarnings("unused")
	private static final String TAG = "ThemeSetter";
	
	private static final Locale DefaultLocale = java.util.Locale.getDefault();

	private static final HashMap<String, Integer> THEMES_BAR = new HashMap<String, Integer>() {
		private static final long serialVersionUID = 1L;
		{
			put("androminion-dark", R.style.Theme_Androminion);
			put("androminion-light", R.style.Theme_Androminion_Light);
			put("androminion-light-darkbar",
					R.style.Theme_Androminion_Light_DarkActionBar);
			// sync with R.array.theme_keys and themes
		}
	};

	private static final HashMap<String, Integer> THEMES_NOBAR = new HashMap<String, Integer>() {
		private static final long serialVersionUID = 1L;
		{
			put("androminion-dark", R.style.Theme_Androminion_NoActionBar);
			put("androminion-light",
					R.style.Theme_Androminion_Light_NoActionBar);
			put("androminion-light-darkbar",
					R.style.Theme_Androminion_Light_NoActionBar);
			// sync with R.array.theme_keys and themes
		}
	};

	/**
	 * Sets the theme of the passed Activity to the theme chosen in preferences.
	 * 
	 * @param act
	 *            The theme of this Activity will be changed.
	 * @param noActionbar
	 *            true: Set theme without ActionBar. false: Set theme with
	 *            ActionBar.
	 */
	public static void setTheme(Activity act, boolean showActionbar) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(act);
		String themeStr = pref.getString("theme",
				"androminion-dark");
		if (showActionbar) {
			if (!THEMES_BAR.containsKey(themeStr)) {
				themeStr = "androminion-dark";
			}
			act.setTheme(THEMES_BAR.get(themeStr));
		} else {
			if (!THEMES_NOBAR.containsKey(themeStr)) {
				themeStr = "androminion-dark";
			}
			act.setTheme(THEMES_NOBAR.get(themeStr));
		}
	}

	public static void setLanguage(Context context) {
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(context);

		Configuration config = context.getResources()
				.getConfiguration();

		String lang = settings.getString(context.getString(R.string.userlang_pref),
				"default");
		Log.d("AndrominionApplication", "lang set to " + lang);
		if("default".equals(lang)){
			lang = DefaultLocale.getLanguage();
		}
		if (!config.locale.getLanguage().equals(lang)) {
			Locale locale = new Locale(lang);
			Locale.setDefault(locale);
			config.locale = locale;
			context.getResources()
					.updateConfiguration(
							config,
							context.getResources()
									.getDisplayMetrics());
		}
	}
}
