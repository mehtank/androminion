package com.mehtank.androminion.ui;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.mehtank.androminion.R;

public class WinLossView extends FrameLayout {

	public boolean statsEmpty = false;
	
    public WinLossView(Context context) {
    	super(context);
    	init(context);
    }
    
	public WinLossView(Context context, AttributeSet attrs) {
		super(context, attrs);
    	init(context);
	}
	
    //TODO: Keep win streak as a stat
    private void init(Context top) {
    	
        ScrollView sv = new ScrollView(top);
        sv.setVerticalScrollBarEnabled(true);
        LinearLayout ll = new LinearLayout(top);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setBackgroundColor(0x66000000);
        
        Achievements achievements = new Achievements(top);
        ArrayList<String> players = achievements.getAllPlayers();

        ArrayList<String> humansFirst = new ArrayList<String>();
        for(String s : players) {
            if(Achievements.isHumanPlayer(s)) {
                humansFirst.add(s);
            }
        }
        
        for(String s : players) {
            if(!humansFirst.contains(s)) {
                humansFirst.add(s);
            }
        }
        
        players = humansFirst;
        
//        boolean first = true;
        if(players.size() == 0) {
            statsEmpty = true;
            players.add("You");
            TextView tv = new TextView(top);
            tv.setText( "  " );
            ll.addView(tv);
        }
        
        for(String player : players) {
//            if(first) {
//                first = false;
//            }
//            else {
//                TextView tv = new TextView(top);
//                tv.setText( "  " );
//                ll.addView(tv);
//            }
            TextView tv = new TextView(top);
            tv.setTextColor(Color.WHITE);
            tv.setTextSize(tv.getTextSize() * 1.5f);

            int wins = achievements.getTotalWins(player);
            int losses= achievements.getTotalLosses(player);
            
            int percent = (int) ((float) wins / (wins + losses) * 100);
            
            String text = "  " + player + " - " + wins + "/" + (wins + losses) + " (" + percent + "%)";
//            try {
//                vname = Strings.format(top, R.string.version, top.getPackageManager().getPackageInfo(top.getPackageName(), 0).versionName);
//            } catch (NameNotFoundException e) {}; 
    
            tv.setText( text );
            ll.addView(tv);
            
            tv = new TextView(top);        
            tv.setTextColor(Color.WHITE);
            tv.setTextSize(tv.getTextSize() * 1.3f);
            int streak = achievements.getWinStreak(player);
            if (streak > 0) {
            	text = "  * " + top.getString(R.string.currentWinStreak) + " " + streak;
                tv.setText( text );
                ll.addView(tv);
            }
            
//            text = "   Total wins " + wins + "/" + (wins + losses);
//            tv.setText( text );
//            ll.addView(tv);

            for(int numPlayers = 2; numPlayers <= 4; numPlayers++) {
                tv = new TextView(top);        
                tv.setTextColor(Color.WHITE);
                tv.setTextSize(tv.getTextSize() * 1.3f);

                wins = achievements.getPlayerWins(player, numPlayers);
                losses = achievements.getPlayerLosses(player, numPlayers);
                
                text = "     " + numPlayers + " player wins " + wins + "/" + (wins + losses);
                tv.setText( text );
                ll.addView(tv);
            }
            
            tv = new TextView(top);
            tv.setText( "  " );
            ll.addView(tv);
        }
        
//        FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(
//                FrameLayout.LayoutParams.WRAP_CONTENT,
//                FrameLayout.LayoutParams.WRAP_CONTENT,
//                Gravity.CENTER);
//
//        fv.addView(im, p);
        sv.addView(ll);
        addView(sv);
        
    }

}
