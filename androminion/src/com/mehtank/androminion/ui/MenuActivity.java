package com.mehtank.androminion.ui;

import java.util.ArrayList;

import com.mehtank.androminion.Androminion;
import com.mehtank.androminion.R;
import com.mehtank.androminion.SettingsActivity;
import com.mehtank.androminion.ui.StartGameFragment.OnStartGameListener;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class MenuActivity extends FragmentActivity implements OnStartGameListener{
	boolean mTwoColums = false;
	int mState = 0;
	
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menuactivity);
		
		if(findViewById(R.id.contentfragment) != null)	{
			mTwoColums = true;
			if (savedInstanceState == null || getSupportFragmentManager().findFragmentById(R.id.contentfragment) == null) {
				getSupportFragmentManager()
					.beginTransaction()
					.add(R.id.contentfragment, createStartGameFragment())
					.commit();
				mState = R.id.but_start;
			}
			if(savedInstanceState != null) {
				mState = savedInstanceState.getInt("mState");
			}
		}	
	}
	
	private Fragment createStartGameFragment() {
		Fragment f = new StartGameFragment();
		if(getIntent().hasExtra("cards")) {
			f.setArguments(getIntent().getExtras());	
		}
		return f;
	}

	@Override
	protected void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putInt("mState", mState);
		super.onSaveInstanceState(savedInstanceState);
	}

	public void buttonClick(View view) {
		doAction(view.getId());
	}
		
	public void doAction(int id) {
		switch(id){
		case R.id.but_start:
			if(mTwoColums){
				if(mState != R.id.but_start) {
					mState = R.id.but_start;
					changeFragment(createStartGameFragment());
				}
			} else {
				Intent i = new Intent(this, StartGameActivity.class);
				if(getIntent().hasExtra("cards")) {
					i.putExtras(getIntent());	
				}
				startActivityForResult(i, 0);
			}
			break;
		case R.id.but_stats:
			if(mTwoColums){
				if(mState != R.id.but_stats) {
					mState = R.id.but_stats;
					changeFragment(new CombinedStatsFragment());
				}
			} else {
				startActivity(new Intent(this, CombinedStatsActivity.class));
			}
			break;
		case R.id.but_settings:
				startActivity(new Intent(this, SettingsActivity.class));
			break;
		case R.id.but_about:
			if(mTwoColums){
				if(mState != R.id.but_about) {
					mState = R.id.but_about;
					changeFragment(new AboutFragment());
				}
			} else {
				startActivity(new Intent(this, AboutActivity.class));
			}
			break;
		}
	}
	
	private void changeFragment(Fragment newFragment) {
		getSupportFragmentManager()
			.beginTransaction()
			.replace(R.id.contentfragment, newFragment)
			.commit();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == 0 && resultCode == RESULT_OK) {
			Intent i = new Intent(this, Androminion.class);
			i.putExtras(data);
			startActivity(i);
		}
	}

	@Override
	public void onStartGameClick(ArrayList<String> values) {
		Intent i = new Intent(this, Androminion.class);
		i.putStringArrayListExtra("command", values);
		startActivity(i);
	}
}
