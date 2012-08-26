package com.mehtank.androminion.ui;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.mehtank.androminion.R;
import com.mehtank.androminion.activities.GameActivity;
import com.vdom.comms.Event;

public class SelectStringView extends BottomInputView implements AdapterView.OnItemClickListener {
	@SuppressWarnings("unused")
	private static final String TAG = "SelectStringView";
	
	ListView lv;

	public SelectStringView (GameActivity top, String header, String[] options) {
		super(top, header);
		lv.setAdapter(new ArrayAdapter<String>(top, R.layout.view_selectstring, options));
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int pos, long id) {
		if (v instanceof TextView) {
			((FrameLayout) this.getParent()).removeView(this);
			String s = (((TextView) v).getText().toString());
			top.handle(new Event(Event.EType.STRING).setString(s));
		}
	}

	@Override
	protected View makeContentView(GameActivity activity) {
		lv = new ListView(top);
		lv.setOnItemClickListener(this);
		lv.setBackgroundDrawable(getResources().getDrawable(R.drawable.bottominputviewborder));

		return lv;
	}
}