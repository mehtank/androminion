package com.mehtank.androminion.fragments;

import com.mehtank.androminion.R;
import com.mehtank.androminion.ui.Achievements;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

        Button butReset = (Button)mView.findViewById(R.id.but_reset);
        butReset.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				buildResetDialog(getActivity()).show();
			}});
        return mView;
	}
	
    private AlertDialog buildResetDialog(final Context context) {
    	final boolean[] choices = {true, true};
    	class choiceListenerClass implements DialogInterface.OnMultiChoiceClickListener, OnClickListener{
    	   	private boolean resetStats = choices[0];
        	private boolean resetAchievements = choices[1];
        	private AlertDialog mDialog;
        	
			@Override
			public void onClick(DialogInterface dialog, int which, boolean isChecked) {
				if(which == 0){
					resetStats = isChecked;
				} else if(which == 1){
					resetAchievements = isChecked;
				}
				Button ResetButton = mDialog.getButton(AlertDialog.BUTTON_POSITIVE);
				if(resetStats || resetAchievements) {
					ResetButton.setEnabled(true);
				} else {
					ResetButton.setEnabled(false);
				}
			}
			
    		@Override
    		public void onClick(DialogInterface dialog, int i) {
    	    	final Achievements achievements = new Achievements(context);
    			if(resetStats) {
    				achievements.resetStats();
    			}
    			if(resetAchievements) {
    				achievements.resetAchievements();
    			}
    		}
    		
    		public void setDialog(AlertDialog dialog) {
    			mDialog = dialog;
    		}
		};
		final choiceListenerClass choiceListener = new choiceListenerClass();
    	AlertDialog.Builder builder = new AlertDialog.Builder(context)
		.setTitle(R.string.reset)
		.setNegativeButton(android.R.string.cancel, null)
		.setMultiChoiceItems(R.array.reset_choices, choices, choiceListener)
		.setPositiveButton(R.string.reset, choiceListener);
    	AlertDialog dialog = builder.create();
    	choiceListener.setDialog(dialog); 
    	return dialog;
    }
}
