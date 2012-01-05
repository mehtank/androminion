package com.mehtank.androminion.ui;

import com.mehtank.androminion.Androminion;
import com.mehtank.androminion.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabWidget;

public class CombinedStatsDialog {
    public CombinedStatsDialog(Context top) {
    	final Achievements achievements = new Achievements((Androminion) top);
    
    	TabHost th = new TabHost(top);
    	LinearLayout ll = new LinearLayout(top);
    	ll.setOrientation(LinearLayout.VERTICAL);
    	
    	TabWidget tw = new TabWidget(top);
    	tw.setId(android.R.id.tabs);
    	
    	FrameLayout fl = new FrameLayout(top);
    	fl.setId(android.R.id.tabcontent);
    	
    	WinLossView winloss = new WinLossView(top, achievements);
    	winloss.setId(R.id.statstab1);
    	AchievementsView about2 = new AchievementsView(top, achievements);
    	about2.setId(R.id.statstab2);
    	
    	fl.addView(winloss);
    	fl.addView(about2);
    	
    	ll.addView(tw);
    	ll.addView(fl);
    	
    	th.addView(ll);
    	th.setup();
    	
        // create tab 1
        TabHost.TabSpec spec1 = th.newTabSpec("tab1");
        spec1.setIndicator(top.getResources().getString(R.string.win_loss_menu), top.getResources().getDrawable(android.R.drawable.ic_menu_myplaces));
        spec1.setContent(winloss.getId());
        th.addTab(spec1);
        //create tab2
        TabHost.TabSpec spec2 = th.newTabSpec("tab2");
        spec2.setIndicator(top.getResources().getString(R.string.achievements_menu), top.getResources().getDrawable(android.R.drawable.ic_menu_agenda));
        spec2.setContent(about2.getId());
        th.addTab(spec2);

        AlertDialog.Builder builder = new AlertDialog.Builder(top)
        	.setView(th)
        	.setPositiveButton(android.R.string.ok, null);
        
        if(!winloss.statsEmpty) {
        	builder = builder.setNegativeButton(Strings.getString(top, R.string.reset), new OnClickListener() {
        		@Override
        		public void onClick(DialogInterface dialog, int i) {
        			achievements.resetStats();
        		}
        	});
        }
        
        AlertDialog d = builder.create();
        
	    WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
	    lp.copyFrom(d.getWindow().getAttributes());
	    lp.width = WindowManager.LayoutParams.FILL_PARENT;
	    lp.height = WindowManager.LayoutParams.FILL_PARENT;
	    d.show();
	    d.getWindow().setAttributes(lp);
    }
}
