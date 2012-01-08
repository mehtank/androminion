package com.mehtank.androminion.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabWidget;

import com.mehtank.androminion.R;

public class AboutDialog {

	public AboutDialog(Context top) {
		showDialog(top, false);
	}
	public AboutDialog(Context top, boolean showNew) {
		showDialog(top, showNew);
	}
	
	private void showDialog(Context top, boolean showNew) {
    	TabHost th = new TabHost(top);
    	LinearLayout ll = new LinearLayout(top);
    	ll.setOrientation(LinearLayout.VERTICAL);
    	
    	TabWidget tw = new TabWidget(top);
    	tw.setId(android.R.id.tabs);
    	
    	FrameLayout fl = new FrameLayout(top);
    	fl.setId(android.R.id.tabcontent);
    	
		AboutView about = new AboutView (top);
		WhatsNewView whatsnew = new WhatsNewView(top);
		CreditsView credits = new CreditsView(top);
		
    	about.setId(R.id.abouttab1);
    	whatsnew.setId(R.id.abouttab2);
    	credits.setId(R.id.abouttab3);
    	
    	fl.addView(about);
    	fl.addView(whatsnew);
    	fl.addView(credits);
    	
    	ll.addView(tw);
    	ll.addView(fl);
    	
    	th.addView(ll);
    	th.setup();
    	
        // create tab 1
        TabHost.TabSpec spec1 = th.newTabSpec("tab1");
        spec1.setIndicator(top.getResources().getString(R.string.about_menu), top.getResources().getDrawable(android.R.drawable.ic_menu_info_details));
        spec1.setContent(about.getId());
        th.addTab(spec1);
        //create tab2
        TabHost.TabSpec spec2 = th.newTabSpec("tab2");
        spec2.setIndicator(top.getResources().getString(R.string.whatsnew_menu), top.getResources().getDrawable(android.R.drawable.ic_menu_view));
        spec2.setContent(whatsnew.getId());
        th.addTab(spec2);
        //create tab3
        TabHost.TabSpec spec3 = th.newTabSpec("tab3");
        spec3.setIndicator(top.getResources().getString(R.string.contrib_menu), top.getResources().getDrawable(android.R.drawable.ic_menu_my_calendar));
        spec3.setContent(credits.getId());
        th.addTab(spec3);

        if (showNew) 
        	th.setCurrentTab(1);
        
		AlertDialog d = new AlertDialog.Builder(top)
			.setView(th)
			.setPositiveButton(android.R.string.ok, null)
			.create();
		
	    WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
	    lp.copyFrom(d.getWindow().getAttributes());
	    lp.width = WindowManager.LayoutParams.FILL_PARENT;
	    lp.height = WindowManager.LayoutParams.FILL_PARENT;
	    d.show();
	    d.getWindow().setAttributes(lp);
	}

}
