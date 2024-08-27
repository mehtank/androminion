package com.mehtank.androminion.activities;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.mehtank.androminion.R;
import com.mehtank.androminion.fragments.AboutFragment;
import com.mehtank.androminion.fragments.CreditsFragment;
import com.mehtank.androminion.fragments.WhatsnewFragment;
import com.mehtank.androminion.fragments.ConnectionsFragment;
import com.mehtank.androminion.util.ThemeSetter;
import com.mehtank.androminion.util.compat.TabInfo;
import com.mehtank.androminion.util.compat.ViewPagerAdapter;

import java.util.ArrayList;

/**
 * This activity just shows four tabs: about, connections, what's new and credits.
 * 
 * Rewrite to support actionbar, tabs and swipe gestures (backwards compatible
 * to API7).
 */
public class AboutActivity extends AppCompatActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		ThemeSetter.setTheme(this, true);
		ThemeSetter.setLanguage(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tabs);

		ArrayList<TabInfo> list = new ArrayList<>();
		list.add(new TabInfo(AboutFragment.class, android.R.drawable.ic_menu_info_details, R.string.about_menu));
		list.add(new TabInfo(ConnectionsFragment.class, android.R.drawable.ic_menu_share, R.string.connections_menu));
		list.add(new TabInfo(WhatsnewFragment.class, android.R.drawable.ic_menu_view, R.string.whatsnew_menu));
		list.add(new TabInfo(CreditsFragment.class, android.R.drawable.ic_menu_my_calendar, R.string.contrib_menu));

		ViewPager2 mViewPager = findViewById(R.id.viewpager);
		mViewPager.setAdapter(new ViewPagerAdapter(this, list));

		TabLayout mTabLayout = findViewById(R.id.tablayout);
		new TabLayoutMediator(mTabLayout, mViewPager, (tab, position) -> {
			tab.setIcon(list.get(position).getIconId());
			tab.setText(list.get(position).getTextId());
		}).attach();

		ActionBar bar = getSupportActionBar();
		if (bar != null) {
			bar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
			bar.setDisplayHomeAsUpEnabled(true);
			bar.setDisplayShowTitleEnabled(true);
			bar.setTitle(R.string.aboutactivity_title);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onResume() {
		super.onResume();
		ThemeSetter.setTheme(this, true);
		ThemeSetter.setLanguage(this);
	}
}
