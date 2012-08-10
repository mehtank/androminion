package com.mehtank.androminion.activities;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.mehtank.androminion.R;
import com.mehtank.androminion.fragments.AboutFragment;
import com.mehtank.androminion.fragments.CreditsFragment;
import com.mehtank.androminion.fragments.WhatsnewFragment;
import com.mehtank.androminion.util.ThemeSetter;
import com.mehtank.androminion.util.compat.TabsAdapter;

/**
 * This activity just shows three tabs: about, what's new and credits.
 * 
 * Rewrite to support actionbar, tabs and swipe gestures (backwards compatible
 * to API7).
 */
public class AboutActivity extends SherlockFragmentActivity {
	@SuppressWarnings("unused")
	private static final String TAG = "AboutActivity";

	private ViewPager mViewPager;
	private TabsAdapter mTabsAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		ThemeSetter.setTheme(this, true);
		ThemeSetter.setLanguage(this);
		super.onCreate(savedInstanceState);

		mViewPager = new ViewPager(this);
		mViewPager.setId(R.id.about_pager);

		setContentView(mViewPager);

		ActionBar bar = getSupportActionBar();
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		bar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
		bar.setDisplayHomeAsUpEnabled(true);
		bar.setDisplayShowTitleEnabled(true);
		bar.setTitle(R.string.aboutactivity_title);

		mTabsAdapter = new TabsAdapter(this, mViewPager);

		// About tab
		ActionBar.Tab aboutTab = bar.newTab().setText(R.string.about_menu)
				.setIcon(android.R.drawable.ic_menu_info_details);
		mTabsAdapter.addTab(aboutTab, AboutFragment.class, null);

		// What's New tab
		ActionBar.Tab whatsnewTab = bar.newTab()
				.setText(R.string.whatsnew_menu)
				.setIcon(android.R.drawable.ic_menu_view);
		mTabsAdapter.addTab(whatsnewTab, WhatsnewFragment.class, null);

		// Credits tab
		ActionBar.Tab creditsTab = bar.newTab().setText(R.string.contrib_menu)
				.setIcon(android.R.drawable.ic_menu_my_calendar);
		mTabsAdapter.addTab(creditsTab, CreditsFragment.class, null);
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
