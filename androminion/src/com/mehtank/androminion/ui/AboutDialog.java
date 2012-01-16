package com.mehtank.androminion.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TabHost;

import com.mehtank.androminion.R;
import com.mehtank.androminion.Androminion;

public class AboutDialog {

	public AboutDialog(Context top) {
		showDialog(top, false);
	}
	public AboutDialog(Context top, boolean showNew) {
		showDialog(top, showNew);
	}
	
	private void showDialog(Context top, boolean showNew) {
        LayoutInflater inflator = ((Androminion)top).getLayoutInflater();
        View view = inflator.inflate(R.layout.aboutdialog, null);

        TabHost th=(TabHost)view;
        th.setup();

        // create tab 1
        TabHost.TabSpec spec1 = th.newTabSpec("tab1");
        spec1.setIndicator(top.getResources().getString(R.string.about_menu), top.getResources().getDrawable(android.R.drawable.ic_menu_info_details));
        spec1.setContent(R.id.abouttab1);
        th.addTab(spec1);
        //create tab2
        TabHost.TabSpec spec2 = th.newTabSpec("tab2");
        spec2.setIndicator(top.getResources().getString(R.string.whatsnew_menu), top.getResources().getDrawable(android.R.drawable.ic_menu_view));
        spec2.setContent(R.id.abouttab2);
        th.addTab(spec2);
        //create tab3
        TabHost.TabSpec spec3 = th.newTabSpec("tab3");
        spec3.setIndicator(top.getResources().getString(R.string.contrib_menu), top.getResources().getDrawable(android.R.drawable.ic_menu_my_calendar));
        spec3.setContent(R.id.abouttab3);
        th.addTab(spec3);

        if (showNew) 
        	th.setCurrentTab(1);
        
		AlertDialog d = new AlertDialog.Builder(top)
			.setView(th)
			.setPositiveButton(android.R.string.ok, null)
			.create();
		d.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
	    d.show();
		
	    WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
	    lp.copyFrom(d.getWindow().getAttributes());
	    lp.width = WindowManager.LayoutParams.FILL_PARENT;
	    lp.height = WindowManager.LayoutParams.FILL_PARENT;
	    d.getWindow().setAttributes(lp);
	}

}
