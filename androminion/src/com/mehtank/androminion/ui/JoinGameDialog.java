package com.mehtank.androminion.ui;

import com.mehtank.androminion.Androminion;
import com.mehtank.androminion.comms.Event;
import com.mehtank.androminion.comms.Event.EType;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class JoinGameDialog implements DialogInterface.OnClickListener {
	LinearLayout vg;
	EditText name;
	Androminion top;
	AlertDialog a;
	SharedPreferences prefs;
	
	public JoinGameDialog(Androminion top, Event e) {
		this.top = top;
		
		prefs = PreferenceManager.getDefaultSharedPreferences(top);
		
		vg = new LinearLayout(top);
		vg.setOrientation(LinearLayout.VERTICAL);
		
		String[] strs = e.o.ss;
		boolean canConnect = false;
		for (String s : strs)
			if (s.contains("||"))
				canConnect = true;

		name = new EditText(top);
		name.setSingleLine();

		if (canConnect) {
			name.setText(prefs.getString("name", Androminion.DEFAULT_NAME));
			TextView tv = new TextView(top);
			tv.setText("\nEnter your name:");
			tv.setTextSize((float) (tv.getTextSize() * 1.5));
			vg.addView(tv);
			vg.addView(name);
		} 

		int numOptions = 0;
		int port = 0;
		
		for (String s : strs) {
			String[] parts = s.split("\\|\\|");
			if (parts.length == 1) {
				TextView tv = new TextView(top);
				tv.setText(parts[0]);
				tv.setTextSize((float) (tv.getTextSize() * 1.5));
				vg.addView(tv);
			} else if (parts.length == 2) {
				try {
					port = Integer.parseInt(parts[1]);
				} catch (NumberFormatException e1) {
					port = 0;
				}
				if (port == 0) {
					TextView tv = new TextView(top);
					tv.setText(parts[0]);
					tv.setTextSize((float) (tv.getTextSize() * 1.5));
					vg.addView(tv);
				} else {
					numOptions++;
					Button tv = new Button(top);
					tv.setText("Join: " + parts[0]);
					tv.setId(port);
					tv.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							joinGame(v.getId());
						}
					});
					vg.addView(tv);
				}
			}
		}
		
		if (numOptions == 1 && port != 0) 
			joinGame(port, prefs.getString("name", Androminion.DEFAULT_NAME));
		else
			a = new AlertDialog.Builder(top)
				.setTitle("Game " + e.s + " running")
				.setView(vg)
				.setPositiveButton("Refresh", this)
				.setNegativeButton(android.R.string.cancel, this)
				.show();		
	}

	private void joinGame(int port, String name) {
		top.handle(new Event(Event.EType.JOINGAME)
			.setInteger(port)
			.setString(name));
		
		if (!Androminion.NOTOASTS) Toast.makeText(top, "Loading game...", Toast.LENGTH_SHORT).show();
	}

	private void joinGame(int port) {
		SharedPreferences.Editor edit = prefs.edit();

		edit.putString("name", name.getText().toString());
		edit.commit();
		
		joinGame(port, name.getText().toString());
		a.dismiss();
	}
	
	public void onClick(DialogInterface dialog, int whichButton) {
		if (whichButton == DialogInterface.BUTTON_POSITIVE)
			top.handle(new Event(EType.HELLO));

		a.dismiss();
	}
}