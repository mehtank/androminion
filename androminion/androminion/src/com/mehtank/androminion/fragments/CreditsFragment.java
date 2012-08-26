package com.mehtank.androminion.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import com.actionbarsherlock.app.SherlockFragment;
import com.mehtank.androminion.R;

public class CreditsFragment extends SherlockFragment {
	@SuppressWarnings("unused")
	private static final String TAG = "CreditsFragment";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ScrollView scrollView = (ScrollView) inflater.inflate(R.layout.fragment_credits, container, false);

		return scrollView;
	}
}