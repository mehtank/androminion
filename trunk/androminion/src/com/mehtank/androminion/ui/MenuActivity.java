package com.mehtank.androminion.ui;

import com.mehtank.androminion.Androminion;
import com.mehtank.androminion.R;
import com.mehtank.androminion.SettingsActivity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

public class MenuActivity extends FragmentActivity {
	Fragment mRight;
	int mState = R.id.but_about;
	
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menuactivity);
		
		if(findViewById(R.id.contentfragment) != null)	{
			mRight = new AboutFragment();
			getSupportFragmentManager().beginTransaction()
			.add(R.id.contentfragment, mRight).commit();
		}
	}

	public void buttonClick(View view) {
		doAction(view.getId());
	}
		
	public void doAction(int id) {
		switch(id){
		case R.id.but_start:
			startActivity(new Intent(this, Androminion.class));
			break;
		case R.id.but_stats:
			if(mRight != null){
				if(mState != R.id.but_stats) {
					mState = R.id.but_stats;
					changeFragment(new CombinedStatsFragment());
				}
			}
			break;
		case R.id.but_settings:
				startActivity(new Intent(this, SettingsActivity.class));
			break;
		case R.id.but_about:
			if(mRight != null){
				if(mState != R.id.but_about) {
					mState = R.id.but_about;
					changeFragment(new AboutFragment());
				}
			}
			break;
		}
	}
	
	private void changeFragment(Fragment newFragment) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.contentfragment, newFragment);
		ft.commit();
	}
}
