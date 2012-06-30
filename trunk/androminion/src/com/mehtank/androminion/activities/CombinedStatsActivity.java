package com.mehtank.androminion.activities;

import com.mehtank.androminion.R;
import com.mehtank.androminion.util.ThemeSetter;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Window;
import android.view.WindowManager;

public class CombinedStatsActivity extends FragmentActivity {
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		ThemeSetter.set(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.combinedstatsactivity);
	}

}
