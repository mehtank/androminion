package com.mehtank.androminion.ui;

import android.app.AlertDialog;
import android.content.Context;
import com.mehtank.androminion.R;

public class AboutDialog {

	public AboutDialog(Context top) {
		AboutView about = new AboutView (top);
        
		new AlertDialog.Builder(top)
			.setIcon(R.drawable.logo)
			.setTitle(" ")
			.setView(about)
			.setPositiveButton(android.R.string.ok, null)
			.show();		

	}

}
