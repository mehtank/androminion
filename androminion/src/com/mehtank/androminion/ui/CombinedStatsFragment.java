package com.mehtank.androminion.ui;

import com.mehtank.androminion.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TabHost;

public class CombinedStatsFragment extends Fragment {
	View mView;
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	final Achievements achievements = new Achievements(getActivity());
        mView = inflater.inflate(R.layout.combinedstatsfragment, null);

        TabHost th=(TabHost)mView;
        th.setup();
    	
        // create tab 1
        TabHost.TabSpec spec1 = th.newTabSpec("tab1");
        spec1.setIndicator(getString(R.string.win_loss_menu), getResources().getDrawable(android.R.drawable.ic_menu_myplaces));
        spec1.setContent(R.id.statstab1);
        th.addTab(spec1);
        //create tab2
        TabHost.TabSpec spec2 = th.newTabSpec("tab2");
        spec2.setIndicator(getString(R.string.achievements_menu), getResources().getDrawable(android.R.drawable.ic_menu_agenda));
        spec2.setContent(R.id.statstab2);
        th.addTab(spec2);
        
        ListView list = (ListView) mView.findViewById(R.id.statstab2);
        list.setAdapter(achievements.getNewAchievementsAdapter());
        list.setBackgroundColor(0x66000000);

        WinLossView winloss = (WinLossView) th.findViewById(R.id.statstab1);
        if(!winloss.statsEmpty) {
        	//achievements.resetStats();
        }
        return mView;
	}
}
