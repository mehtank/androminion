package com.mehtank.androminion.activities;

import java.util.ArrayList;

import com.mehtank.androminion.Androminion;
import com.mehtank.androminion.R;
import com.mehtank.androminion.fragments.AboutFragment;
import com.mehtank.androminion.fragments.CombinedStatsFragment;
import com.mehtank.androminion.fragments.StartGameFragment;
import com.mehtank.androminion.fragments.StartGameFragment.OnStartGameListener;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class MenuActivity extends FragmentActivity implements OnStartGameListener{
	private boolean mTwoColums = false;
	private int mState = 0;
	
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

	public void onClickStartGame(View view) {
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
	}
	
	public void onClickStats(View view) {
		if(mTwoColums){
			if(mState != R.id.but_stats) {
				mState = R.id.but_stats;
				changeFragment(new CombinedStatsFragment());
			}
		} else {
			startActivity(new Intent(this, CombinedStatsActivity.class));
		}
	}
	
	public void onClickSettings(View view) {
		startActivity(new Intent(this, SettingsActivity.class));
	}
	
	public void onClickAbout(View view) {
		if(mTwoColums){
			if(mState != R.id.but_about) {
				mState = R.id.but_about;
				changeFragment(new AboutFragment());
			}
		} else {
			startActivity(new Intent(this, AboutActivity.class));
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
