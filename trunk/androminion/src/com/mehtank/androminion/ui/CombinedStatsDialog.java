package com.mehtank.androminion.ui;

import com.mehtank.androminion.Androminion;
import com.mehtank.androminion.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.TabHost;

public class CombinedStatsDialog {
    public CombinedStatsDialog(Context top) {
    	final Achievements achievements = new Achievements((Androminion) top);

        LayoutInflater inflator = ((Androminion)top).getLayoutInflater();
        View view = inflator.inflate(R.layout.combinedstatsfragment, null);

        TabHost th=(TabHost)view;
        th.setup();
    	
        // create tab 1
        TabHost.TabSpec spec1 = th.newTabSpec("tab1");
        spec1.setIndicator(top.getResources().getString(R.string.win_loss_menu), top.getResources().getDrawable(android.R.drawable.ic_menu_myplaces));
        spec1.setContent(R.id.statstab1);
        th.addTab(spec1);
        //create tab2
        TabHost.TabSpec spec2 = th.newTabSpec("tab2");
        spec2.setIndicator(top.getResources().getString(R.string.achievements_menu), top.getResources().getDrawable(android.R.drawable.ic_menu_agenda));
        spec2.setContent(R.id.statstab2);
        th.addTab(spec2);
        
        ListView list = (ListView) view.findViewById(R.id.statstab2);
        list.setAdapter(achievements.getNewAchievementsAdapter());
        list.setBackgroundColor(0x66000000);

        AlertDialog.Builder builder = new AlertDialog.Builder(top)
        	.setView(th)
        	.setPositiveButton(android.R.string.ok, null);
        
        WinLossView winloss = (WinLossView) th.findViewById(R.id.statstab1);
        if(!winloss.statsEmpty) {
        	builder = builder.setNegativeButton(Strings.getString(top, R.string.reset), new OnClickListener() {
        		@Override
        		public void onClick(DialogInterface dialog, int i) {
        			achievements.resetStats();
        		}
        	});
        }
        
        AlertDialog d = builder.create();
        d.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
	    d.show();
        
	    WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
	    lp.copyFrom(d.getWindow().getAttributes());
	    lp.width = WindowManager.LayoutParams.FILL_PARENT;
	    lp.height = WindowManager.LayoutParams.FILL_PARENT;
	    d.getWindow().setAttributes(lp);
    }
}
