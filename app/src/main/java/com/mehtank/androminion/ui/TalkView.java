package com.mehtank.androminion.ui;

import com.mehtank.androminion.activities.GameActivity;
import com.vdom.comms.Event;

import android.content.Context;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import androidx.appcompat.widget.AppCompatEditText;

public class TalkView extends AppCompatEditText implements OnEditorActionListener {
	private static final String TAG = "TalkView";
	
	GameActivity top;

	public TalkView(GameActivity top) {
		super(top);
		this.top = top;
		setSingleLine();
		setOnEditorActionListener(this);
	}

	@Override
	public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
		String txt = arg0.getText().toString().trim();
		if (!txt.equals(""))
			top.handle(new Event(Event.EType.SAY).setString(txt));

		setText("");

		InputMethodManager imm = (InputMethodManager)top.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(getWindowToken(), 0);

		return true;
	}

}
