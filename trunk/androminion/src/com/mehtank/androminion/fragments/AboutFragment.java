package com.mehtank.androminion.fragments;

import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.mehtank.androminion.R;

public class AboutFragment extends SherlockFragment {
	private static final String TAG = "AboutFragment";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ScrollView scrollView = (ScrollView) inflater.inflate(R.layout.fragment_about, container, false);

		// inflate doesn't care about replacing the version String, let's do this now:
		String version = "";
		try {
			version = getString(R.string.version, getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName);
		} catch (NameNotFoundException e) {
			Log.e(TAG, "Version string not found. Setting dummy version.");
			version = "0.0";
		}
		;
		TextView tv = (TextView) scrollView.findViewById(R.id.version);
		tv.setText(version);

		return scrollView;

	}
}