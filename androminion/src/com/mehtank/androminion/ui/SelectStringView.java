package com.mehtank.androminion.ui;

import android.graphics.Color;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.mehtank.androminion.Androminion;
import com.mehtank.androminion.R;
import com.mehtank.androminion.comms.Event;

public class SelectStringView extends BottomInputView implements AdapterView.OnItemClickListener {
	ListView lv;
	
	public SelectStringView (Androminion top, String header, String[] options) {
		super(top, header);
		lv.setAdapter(new ArrayAdapter<String>(top, R.layout.selectstring, options));
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
	protected View makeContentView(Androminion top) {
		lv = new ListView(top);
		lv.setOnItemClickListener(this);
		lv.setBackgroundColor(Color.LTGRAY);
		
		return lv;
	}
}