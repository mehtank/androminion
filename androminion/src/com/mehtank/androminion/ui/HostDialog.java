package com.mehtank.androminion.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.method.DigitsKeyListener;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.mehtank.androminion.Androminion;
import com.mehtank.androminion.R;
import com.vdom.comms.Event;

public class HostDialog implements DialogInterface.OnClickListener {
	LinearLayout vg;
	
	RadioGroup gamehost;
	RadioButton local;
	RadioButton remote;
	
	TextView prompt;
	EditText host;
	EditText port;

	Androminion top;
	AlertDialog a;
	
	public HostDialog(Androminion top, String hostString, int portNum) {
		this.top = top;

		gamehost = new RadioGroup(top);
		gamehost.setOrientation(LinearLayout.HORIZONTAL);

		local = new RadioButton(top);
		local.setText(R.string.host_local);
		remote = new RadioButton(top);
		remote.setText(R.string.host_remote);
		
		gamehost.addView(local);
		gamehost.addView(remote);
		local.setChecked(false);
		remote.setChecked(true);
		
		gamehost.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				toggleHost();
			}
		});
		
		vg = new LinearLayout(top);
		vg.setOrientation(LinearLayout.VERTICAL);
		vg.addView(gamehost);
		
		prompt = new TextView(top);
		prompt.setText(R.string.host_prompt);
		vg.addView(prompt);
		
		host = new EditText(top);
		host.setText(hostString);
		host.setSingleLine();
		vg.addView(host);
		
		port = new EditText(top);
		port.setText("" + portNum);
		port.setKeyListener(new DigitsKeyListener());
		port.setSingleLine();
		// vg.addView(port);
		
		if (hostString.trim().length() == 0) {
			local.setChecked(true);
			host.setVisibility(View.INVISIBLE);
			prompt.setVisibility(View.INVISIBLE);
		}

		a = new AlertDialog.Builder(top)
			.setTitle(R.string.host_title)
			.setMessage(R.string.host_message)
			.setView(vg)  
			.setPositiveButton(android.R.string.ok, this)
			.setNegativeButton(android.R.string.cancel, this)
			.show();		
	}
	
	public void toggleHost() {
		if (gamehost.getCheckedRadioButtonId() == local.getId()) {
			host.setVisibility(View.INVISIBLE);
			prompt.setVisibility(View.INVISIBLE);
		} else {
			host.setVisibility(View.VISIBLE);
			prompt.setVisibility(View.VISIBLE);
		}
	}
	
	public void onClick(DialogInterface dialog, int whichButton) {
		if (whichButton == DialogInterface.BUTTON_POSITIVE) {
			int p = Androminion.DEFAULT_PORT;
			String h = host.getText().toString();

			if (gamehost.getCheckedRadioButtonId() == local.getId() || h.trim().length() == 0) {
				h = "localhost";
			} else {
				try {
					p = Integer.parseInt(port.getText().toString());
				} catch (NumberFormatException e) {}
			}

			top.handle(new Event(Event.EType.SETHOST)
								.setString(h)
								.setInteger(p));
		}
	}
}