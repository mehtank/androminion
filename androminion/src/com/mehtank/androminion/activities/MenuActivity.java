package com.mehtank.androminion.activities;

import java.util.ArrayList;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.mehtank.androminion.R;
import com.mehtank.androminion.fragments.StartGameFragment;
import com.mehtank.androminion.fragments.StartGameFragment.OnStartGameListener;
import com.mehtank.androminion.util.ThemeSetter;

/**
 * Start screen of the application, showing a menu.
 * 
 */
public class MenuActivity extends SherlockFragmentActivity implements
		OnStartGameListener {
	private static final String TAG = "MenuActivity";

	private boolean mTwoColums = false; // Two-Column-Layout, possibly tablet
	private int mState = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		ThemeSetter.setTheme(this, true);
		ThemeSetter.setLanguage(this);
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		// Fix so Androminion doesn't crash when updating
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(this);
		Log.d(TAG,
				"Theme is set to "
						+ pref.getString("theme", "androminion-dark"));
		if (pref.getString("theme", "androminion-dark").equals("androminion")) {
			// Settings from previous Androminion version exist
			Log.d(TAG, "Resetting theme setting to default value");
			Editor editor = pref.edit();
			editor.remove("theme");
			editor.commit();
		}

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_menu);

		if (findViewById(R.id.fragment_content) != null) {
			mTwoColums = true;
			if (savedInstanceState == null
					|| getSupportFragmentManager().findFragmentById(
							R.id.fragment_content) == null) {
				getSupportFragmentManager().beginTransaction()
						.add(R.id.fragment_content, createStartGameFragment())
						.commit();
				mState = R.id.but_start;
			}
			if (savedInstanceState != null) {
				mState = savedInstanceState.getInt("mState");
			}
		}
	}

	private SherlockFragment createStartGameFragment() {
		SherlockFragment f = new StartGameFragment();
		if (getIntent().hasExtra("cards")) {
			f.setArguments(getIntent().getExtras());
		}
		return f;
	}

	@Override
	protected void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putInt("mState", mState);
		super.onSaveInstanceState(savedInstanceState);
	}

	public void onClickStartGame(View view) {
		if (mTwoColums) {
			if (mState != R.id.but_start) {
				mState = R.id.but_start;
				changeFragment(createStartGameFragment());
			}
		} else {
			Intent i = new Intent(this, StartGameActivity.class);
			if (getIntent().hasExtra("cards")) {
				i.putExtras(getIntent());
			}
			startActivityForResult(i, 0);
		}
	}

	public void onClickStats(View view) {
		// if (mTwoColums) {
		// if (mState != R.id.but_stats) {
		// mState = R.id.but_stats;
		// changeFragment(new CombinedStatsFragment());
		// }
		// } else {
		startActivity(new Intent(this, StatisticsActivity.class));
		// }
	}

	public void onClickSettings(View view) {
		startActivity(new Intent(this, SettingsActivity.class));
	}

	public void onClickAbout(View view) {
		// if(mTwoColums){
		// if(mState != R.id.but_about) {
		// mState = R.id.but_about;
		// changeFragment(new AboutFragment());
		// }
		// } else {
		startActivity(new Intent(this, AboutActivity.class));
		// }
	}

	private void changeFragment(SherlockFragment newFragment) {
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.fragment_content, newFragment).commit();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 0 && resultCode == RESULT_OK) {
			Intent i = new Intent(this, GameActivity.class);
			i.putExtras(data);
			startActivity(i);
		}
	}

	@Override
	public void onStartGameClick(ArrayList<String> values) {
		Intent i = new Intent(this, GameActivity.class);
		i.putStringArrayListExtra("command", values);
		startActivity(i);
	}

	@Override
	public void onResume() {
		super.onResume();
		ThemeSetter.setTheme(this, true);
		ThemeSetter.setLanguage(this);
	}
}
