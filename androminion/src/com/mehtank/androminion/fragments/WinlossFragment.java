package com.mehtank.androminion.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import com.mehtank.androminion.R;
import android.support.v4.app.Fragment;

public class WinlossFragment extends Fragment {
	@SuppressWarnings("unused")
	private static final String TAG = "WinlossFragment";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ScrollView scrollView = (ScrollView) inflater.inflate(R.layout.fragment_winloss, container, false);
		return scrollView;
	}
}
