package com.mehtank.androminion.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.mehtank.androminion.R;
import com.mehtank.androminion.util.Achievements;

public class AchievementsFragment extends Fragment {
	@SuppressWarnings("unused")
	private static final String TAG = "AchievementsFragment";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ListView listView = (ListView) inflater.inflate(R.layout.fragment_achievements, container, false);

		final Achievements achievements = new Achievements(getActivity());
		listView.setAdapter(achievements.getNewAchievementsAdapter());

		return listView;
	}
}
