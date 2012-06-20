package com.mehtank.androminion.activities;

import java.util.ArrayList;

import com.mehtank.androminion.fragments.StartGameFragment;
import com.mehtank.androminion.fragments.StartGameFragment.OnStartGameListener;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.Window;
import android.view.WindowManager;

public class StartGameActivity extends FragmentActivity implements OnStartGameListener{
	private Fragment mStartGameFragment;

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		super.onCreate(savedInstanceState);

		if(savedInstanceState == null) {
			mStartGameFragment = new StartGameFragment();
			
			if(getIntent().hasExtra("cards")) {
				mStartGameFragment.setArguments(getIntent().getExtras());	
			}
		
			getSupportFragmentManager()
				.beginTransaction()
				.add(android.R.id.content, mStartGameFragment)
				.commit();
		} else {
			mStartGameFragment = getSupportFragmentManager().findFragmentById(android.R.id.content);
		}
	}

	@Override
	public void onStartGameClick(ArrayList<String> values) {
		Intent data = new Intent();
		data.putStringArrayListExtra("command", values);
		setResult(RESULT_OK, data);
		finish();
	}
}
