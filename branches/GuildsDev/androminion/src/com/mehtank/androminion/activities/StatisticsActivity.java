package com.mehtank.androminion.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.widget.Button;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.mehtank.androminion.R;
import com.mehtank.androminion.fragments.AchievementsFragment;
import com.mehtank.androminion.fragments.WinlossFragment;
import com.mehtank.androminion.util.Achievements;
import com.mehtank.androminion.util.ThemeSetter;
import com.mehtank.androminion.util.compat.TabsAdapter;

/**
 * This activity just shows two tabs: statistics and achievements.
 * 
 * Rewrite to support actionbar, tabs and swipe gestures (backwards compatible
 * to API8).
 */
public class StatisticsActivity extends SherlockFragmentActivity {
	@SuppressWarnings("unused")
	private static final String TAG = "StatisticsActivity";

	private ViewPager mViewPager;
	private TabsAdapter mTabsAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		ThemeSetter.setTheme(this, true);
		ThemeSetter.setLanguage(this);
		super.onCreate(savedInstanceState);

		mViewPager = new ViewPager(this);
		mViewPager.setId(R.id.combinedstats_pager);
		setContentView(mViewPager);

		ActionBar bar = getSupportActionBar();
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		bar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
		bar.setDisplayHomeAsUpEnabled(true);
		bar.setDisplayShowTitleEnabled(true);
		bar.setTitle(R.string.statisticsactivity_title);

		mTabsAdapter = new TabsAdapter(this, mViewPager);

		ActionBar.Tab statsTab = bar.newTab().setText(R.string.win_loss_menu)
				.setIcon(android.R.drawable.ic_menu_myplaces);
		mTabsAdapter.addTab(statsTab, WinlossFragment.class, null);

		ActionBar.Tab achievementsTab = bar.newTab()
				.setText(R.string.achievements_menu)
				.setIcon(android.R.drawable.ic_menu_agenda);
		mTabsAdapter.addTab(achievementsTab, AchievementsFragment.class, null);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.activity_statistics, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    int id = item.getItemId();
	    if (id == android.R.id.home){
			NavUtils.navigateUpFromSameTask(this);
	    } else if (id == R.id.resetstatistics_menu) {
			buildResetDialog(this).show();
	    } else {
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

	private AlertDialog buildResetDialog(final Context context) {
		final boolean[] choices = { true, true };
		class choiceListenerClass implements
				DialogInterface.OnMultiChoiceClickListener, OnClickListener {
			private boolean resetStats = choices[0];
			private boolean resetAchievements = choices[1];
			private AlertDialog mDialog;

			@Override
			public void onClick(DialogInterface dialog, int which,
					boolean isChecked) {
				if (which == 0) {
					resetStats = isChecked;
				} else if (which == 1) {
					resetAchievements = isChecked;
				}
				Button ResetButton = mDialog
						.getButton(DialogInterface.BUTTON_POSITIVE);
				if (resetStats || resetAchievements) {
					ResetButton.setEnabled(true);
				} else {
					ResetButton.setEnabled(false);
				}
			}

			@Override
			public void onClick(DialogInterface dialog, int i) {
				final Achievements achievements = new Achievements(context);
				if (resetStats) {
					achievements.resetStats();
				}
				if (resetAchievements) {
					achievements.resetAchievements();
				}
			}

			public void setDialog(AlertDialog dialog) {
				mDialog = dialog;
			}
		}
		;
		final choiceListenerClass choiceListener = new choiceListenerClass();
		AlertDialog.Builder builder = new AlertDialog.Builder(context)
				.setTitle(R.string.reset)
				.setNegativeButton(android.R.string.cancel, null)
				.setMultiChoiceItems(R.array.reset_choices, choices,
						choiceListener)
				.setPositiveButton(R.string.reset, choiceListener);
		AlertDialog dialog = builder.create();
		choiceListener.setDialog(dialog);
		return dialog;
	}

	@Override
	public void onResume() {
		super.onResume();
		ThemeSetter.setTheme(this, true);
		ThemeSetter.setLanguage(this);
	}
}
