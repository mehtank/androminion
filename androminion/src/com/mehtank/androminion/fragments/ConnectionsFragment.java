package com.mehtank.androminion.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.mehtank.androminion.R;

public class ConnectionsFragment extends Fragment {
	@SuppressWarnings("unused")
	private static final String TAG = "ConnectionsFragment";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ScrollView scrollView = (ScrollView) inflater.inflate(R.layout.fragment_connections, container, false);

		// inflate doesn't renter HTML, so do this now:
		TextView tv = (TextView) scrollView.findViewById(R.id.connections);
		tv.setText(Html.fromHtml(getString(R.string.connections)));
		tv.setMovementMethod(LinkMovementMethod.getInstance());

		return scrollView;
	}
}
