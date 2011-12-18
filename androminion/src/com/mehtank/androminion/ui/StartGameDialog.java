package com.mehtank.androminion.ui;

import java.util.ArrayList;
import java.util.Arrays;

import com.mehtank.androminion.Androminion;
import com.mehtank.androminion.R;
import com.vdom.comms.Event;
import com.vdom.comms.Event.EventObject;
import com.vdom.api.GameType;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class StartGameDialog implements DialogInterface.OnClickListener {
	LinearLayout vg;
	Androminion top;
	AlertDialog a;
	String[] cardsPassOnStartup;
	int port;
	ArrayList<Spinner> values = new ArrayList<Spinner>();
	SharedPreferences prefs;
	public static final String CARDS_PASSED = "<Specified cards>";
	
	private Spinner slist(Context top, String[] strs) {
	    Spinner s = new Spinner(top);

	    ArrayAdapter<String> adapter = new ArrayAdapter<String>
	    	(top, android.R.layout.simple_spinner_item);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    
	    for (String str : strs)
	    	adapter.add(str);
	    s.setAdapter(adapter);
	    return s;
	}

	public StartGameDialog(Androminion top, Event e) {
		showDialog(top, e, false);
	}

	public StartGameDialog(Androminion top, Event e, boolean multiplayer, String[] cardsPassOnStartup) {
	    this.cardsPassOnStartup = cardsPassOnStartup;
		showDialog(top, e, multiplayer);
	}
	
	@SuppressWarnings("unchecked")
	public void showDialog(Androminion top, Event e, boolean multiplayer) {
	    if(e == null || e.o == null)
	    {
	        System.out.println("Start game called without proper data in event.");
	        return;
	    }
	    
		this.top = top;
		
		prefs = PreferenceManager.getDefaultSharedPreferences(top);
		
		vg = new LinearLayout(top);
		vg.setOrientation(LinearLayout.VERTICAL);
	
		String[] strs = e.o.ss;
		String[] gameTypes;
		String[] playerTypes = new String[strs.length - e.i];

		int i = 0;
        if(cardsPassOnStartup == null) {
            gameTypes = new String[e.i];
        }
        else {
            gameTypes = new String[e.i + 1];
            gameTypes[0] = CARDS_PASSED;
            i++;
        }
        
        int at = 0;
		for (; at < e.i; at++)
			gameTypes[i++] = strs[at];
		for (; at < strs.length; at++)
			playerTypes[at - e.i] = strs[at];
		
		TextView tv = new TextView(top);
		tv.setText(R.string.select_a_game_type_);
		tv.setTextSize(tv.getTextSize() * 1.5f);
		vg.addView(tv);
		
		int prefNum = 0;
		Spinner s = slist(top, gameTypes);
		if(cardsPassOnStartup != null) {
		    s.setSelection(0);
		    prefNum++;
		} else {
		    s.setSelection(((ArrayAdapter<String>) s.getAdapter()).getPosition(prefs.getString("gamePref" + prefNum++, "Random")));
		}
		values.add(s);
		vg.addView(s);

		tv = new TextView(top);
		tv.setText(R.string.select_players);
		tv.setTextSize(tv.getTextSize() * 1.5f);
		vg.addView(tv);
	    
		for (i=0; i<4; i++) {
			LinearLayout hv = new LinearLayout(top);
			hv.setOrientation(LinearLayout.HORIZONTAL);
			tv = new TextView(top);
			String player = top.getString(R.string.player) + (i + 1);
			tv.setText(" - " + player + ":  ");
			tv.setTextSize(tv.getTextSize() * 1.5f);
			hv.addView(tv);
			
		    s = slist(top, playerTypes);
		    s.setPrompt(player);
	    	if (i > 1)
			    ((ArrayAdapter<String>) s.getAdapter()).add(top.getString(R.string.none_game_start));
	    	
			s.setSelection(((ArrayAdapter<String>) s.getAdapter()).getPosition(prefs.getString("gamePref" + prefNum++, top.getString(R.string.none_game_start))));
	    	values.add(s);
	    	
	    	if (i == 0) {
				s.setSelection(((ArrayAdapter<String>) s.getAdapter()).getPosition("Human Player"));
				Button b = new Button(top);
				b.setText(R.string.you);
				b.setClickable(false);
				b.setEnabled(false);
				b.setFocusable(false);
				// tv.setTextSize(tv.getTextSize() * 1.2f);
				hv.addView(b, LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				
				if (!multiplayer) { // disable additional human players
					ArrayList<String> pts = new ArrayList<String>(Arrays.asList(playerTypes));
					pts.remove("Human Player");
					playerTypes = pts.toArray(new String[0]);
				}
	    	} else
	    		hv.addView(s, LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
	    	vg.addView(hv);
	    }

		a = new AlertDialog.Builder(top)
			.setTitle(R.string.start_new_game_on_server)
			.setView(vg)
			.setPositiveButton(R.string.start_game, this)
			.setNegativeButton(android.R.string.cancel, this)
			.show();		
	}
	
	public void onClick(DialogInterface dialog, int whichButton) {
		ArrayList<String> strs = new ArrayList<String>();
		
		if (whichButton == DialogInterface.BUTTON_POSITIVE) {
			SharedPreferences.Editor edit = prefs.edit();

			boolean cardsPassedGameType = false;
			
			int i = 0;
			for (Spinner s : values) {
				String str = (String) s.getSelectedItem();
				if(str.equals(CARDS_PASSED)) {
				    i++;
				    cardsPassedGameType = true;
				    strs.add("Random");
				}
				else {
    				edit.putString("gamePref" + (i++), str);
    				if (!str.equals(R.string.none_game_start))  {
//					    Check if option is a GameType
//						They are translated on the UI, but the server expects the English name
    					GameType g = com.mehtank.androminion.server.Strings.getGameTypefromName((String) s.getSelectedItem());
    					if (g==null)
//    						Obviously no Game Type, just pass string
    						strs.add((String) s.getSelectedItem());
    					else
    						strs.add((String) g.getName());
    				}
    					
				}
			}
			
			if(prefs.getBoolean("plat_colony", false)) {
			    strs.add("-platcolony");
			}
			
            if(prefs.getBoolean("quick_play", false)) {
                strs.add("-quickplay");
            }
            
            if(cardsPassOnStartup != null && cardsPassedGameType) {
                StringBuilder sb = new StringBuilder();
                sb.append("-cards=");
                boolean first = true;
                for(String card : cardsPassOnStartup) {
                    if(first)
                        first = false;
                    else
                        sb.append("-");
                    sb.append(card);
                }
                strs.add(sb.toString());
            }
            
			top.handle(new Event(Event.EType.STARTGAME)
				.setObject(new EventObject(strs.toArray(new String[0]))));
			if (!Androminion.NOTOASTS) Toast.makeText(top, top.getString(R.string.toast_starting), Toast.LENGTH_SHORT).show();
    		
			edit.commit();
		}
			
		a.dismiss();
	}
}