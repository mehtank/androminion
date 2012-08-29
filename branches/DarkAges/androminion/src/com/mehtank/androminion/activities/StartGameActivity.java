package com.mehtank.androminion.activities;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.mehtank.androminion.R;
import com.mehtank.androminion.fragments.StartGameFragment;
import com.mehtank.androminion.fragments.StartGameFragment.OnStartGameListener;
import com.mehtank.androminion.util.ThemeSetter;

/**
 * This activity shows the start game screen where players can be selected. The
 * actual content is in StartGameFragment.
 * 
 * Rewrite to support actionbar (backwards compatible to API7).
 */
public class StartGameActivity extends SherlockFragmentActivity implements
		OnStartGameListener {
	@SuppressWarnings("unused")
	private static final String TAG = "StartGameActivity";

	private Fragment mStartGameFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		ThemeSetter.setTheme(this, true);
		ThemeSetter.setLanguage(this);
		super.onCreate(savedInstanceState);

		ActionBar bar = getSupportActionBar();
		bar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
		bar.setDisplayHomeAsUpEnabled(true);
		bar.setDisplayShowTitleEnabled(true);
		bar.setTitle(R.string.startgameactivity_title);

		if (savedInstanceState == null) {
			mStartGameFragment = new StartGameFragment();

			if (getIntent().hasExtra("cards")) {
				mStartGameFragment.setArguments(getIntent().getExtras());
			}

			getSupportFragmentManager().beginTransaction()
					.add(android.R.id.content, mStartGameFragment).commit();
		} else {
			mStartGameFragment = getSupportFragmentManager().findFragmentById(
					android.R.id.content);
		}
	}

	@Override
	public void onStartGameClick(ArrayList<String> values) {
		Intent data = new Intent();
		data.putStringArrayListExtra("command", values);
		setResult(RESULT_OK, data);
		finish();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		ThemeSetter.setTheme(this, true);
		ThemeSetter.setLanguage(this);
	}
}
