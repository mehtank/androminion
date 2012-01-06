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
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class StartGameDialog implements DialogInterface.OnClickListener {
	LinearLayout vg;
	Androminion top;
	AlertDialog a;
	String[] cardsPassOnStartup;
	String[] lastCards;
	int port;

	RadioGroup randomPreset;
	RadioGroup lastSpecified;
	Spinner randomSpinner;
	Spinner presetSpinner;
	ArrayList<Spinner> values = new ArrayList<Spinner>();
	SharedPreferences prefs;

	public static final String CARDS_PASSED = "<Specified cards>";
	public static final String CARDS_LAST = "<Last played>";
	
	enum TypeOptions {RANDOM, PRESET, LAST, SPECIFIED};
	TypeOptions gameType;
	
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
	
	private void getLastCards() {
		int count = prefs.getInt("LastCardCount", 0);
		
		if (count > 0) {
			lastCards = new String[count];
			for (int i = 0; i < count; i++) 
				lastCards[i] = prefs.getString("LastCard" + i, null);
		}
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
		getLastCards();
		
		vg = new LinearLayout(top);
		vg.setOrientation(LinearLayout.VERTICAL);
	
		String[] strs = e.o.ss;
		String[] presetTypes;
		String[] randomTypes;
		String[] playerTypes = new String[strs.length - e.i];

//		int numGameTypes = e.i;
//		if (cardsPassOnStartup != null)
//			numGameTypes++;
//		if (lastCards != null)
//			numGameTypes++;
//		
//        presetTypes = new String[numGameTypes];
//		
//		int i = 0;
//        if(cardsPassOnStartup != null)
//            presetTypes[i++] = CARDS_PASSED;        
//        if(lastCards != null)
//            presetTypes[i++] = CARDS_LAST;
//        
        ArrayList<String> presets = new ArrayList<String>();
        ArrayList<String> randoms = new ArrayList<String>();
        
        int at = 0;
		for (; at < e.i; at++)
			// XXX does this work in all languages? Probly want to make more robust
			if (strs[at].startsWith(top.getString(R.string.Random_gametype)))
				randoms.add(strs[at]);
			else
				presets.add(strs[at]);
		for (; at < strs.length; at++)
			playerTypes[at - e.i] = strs[at];

		randomTypes = randoms.toArray(new String[0]);
		presetTypes = presets.toArray(new String[0]);

		TextView tv = new TextView(top);
		tv.setText(R.string.select_a_game_type_);
		tv.setTextSize(tv.getTextSize() * 1.5f);
		vg.addView(tv);
		
		gameType = TypeOptions.valueOf(prefs.getString("gameType", TypeOptions.PRESET.name()));
		RadioButton toCheck = null;
		
		randomPreset = new RadioGroup(top); //create the RadioGroup
	    randomPreset.setOrientation(RadioGroup.HORIZONTAL);//or RadioGroup.VERTICAL
	    
	    RadioButton rb = new RadioButton(top);
	    rb.setText(R.string.game_type_random);
	    rb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (randomSpinner == null)
					return;
				
				if (isChecked) {
					gameType = TypeOptions.RANDOM;
					randomSpinner.setVisibility(Spinner.VISIBLE);
					if (lastSpecified != null) lastSpecified.clearCheck();
				} else
					randomSpinner.setVisibility(Spinner.GONE);
			}});
	    if (gameType == TypeOptions.RANDOM)
	    	toCheck = rb;
	    randomPreset.addView(rb);

	    rb = new RadioButton(top);
	    rb.setText(R.string.game_type_preset);
	    rb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (presetSpinner == null)
					return;
				
				if (isChecked) {
					gameType = TypeOptions.PRESET;
					presetSpinner.setVisibility(Spinner.VISIBLE);
					if (lastSpecified != null) lastSpecified.clearCheck();
				} else
					presetSpinner.setVisibility(Spinner.GONE);
			}});
	    if (gameType == TypeOptions.PRESET)
	    	toCheck = rb;
	    randomPreset.addView(rb);
	    
	    vg.addView(randomPreset);
	    
	    lastSpecified = new RadioGroup(top); //create the RadioGroup
	    lastSpecified.setOrientation(RadioGroup.HORIZONTAL);//or RadioGroup.VERTICAL

	    if (lastCards != null) {
	    	rb = new RadioButton(top);
	    	rb.setText(R.string.game_type_last);
		    rb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					if (isChecked) {
						gameType = TypeOptions.LAST;
						if (randomPreset != null) randomPreset.clearCheck();
					}
				}});
		    if (gameType == TypeOptions.LAST)
		    	toCheck = rb;
	    	lastSpecified.addView(rb);
	    }
	    if (cardsPassOnStartup != null) {
	    	gameType = TypeOptions.SPECIFIED;
	    	
	    	rb = new RadioButton(top);
	    	rb.setText(R.string.game_type_specified);
		    rb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					if (isChecked) {
						gameType = TypeOptions.SPECIFIED;
						if (randomPreset != null) randomPreset.clearCheck();
					}
				}});
	    	toCheck = rb;
	    	lastSpecified.addView(rb);
	    }
	    vg.addView(lastSpecified);

	    randomSpinner = slist(top, randomTypes);
	    randomSpinner.setSelection(((ArrayAdapter<String>) randomSpinner.getAdapter()).getPosition(prefs.getString("randomPref", "Random")));
		randomSpinner.setVisibility(Spinner.GONE);
		vg.addView(randomSpinner);
		
		presetSpinner = slist(top, presetTypes);
		presetSpinner.setSelection(((ArrayAdapter<String>) presetSpinner.getAdapter()).getPosition(prefs.getString("presetPref", "First Game (Base)")));
		presetSpinner.setVisibility(Spinner.GONE);
		vg.addView(presetSpinner);

	    if (toCheck != null)
	    	toCheck.setChecked(true);

		tv = new TextView(top);
		tv.setText(R.string.select_players);
		tv.setTextSize(tv.getTextSize() * 1.5f);
		vg.addView(tv);
	    
		for (int i=0; i<4; i++) {
			LinearLayout hv = new LinearLayout(top);
			hv.setOrientation(LinearLayout.HORIZONTAL);
			tv = new TextView(top);
			String player = top.getString(R.string.player) + (i + 1);
			tv.setText(" - " + player + ":  ");
			tv.setTextSize(tv.getTextSize() * 1.5f);
			hv.addView(tv);
			
		    Spinner playerSpinner = slist(top, playerTypes);
		    playerSpinner.setPrompt(player);
	    	if (i > 1)
			    ((ArrayAdapter<String>) playerSpinner.getAdapter()).add(top.getString(R.string.none_game_start));
	    	
			playerSpinner.setSelection(((ArrayAdapter<String>) playerSpinner.getAdapter()).getPosition(prefs.getString("gamePref" + i, top.getString(R.string.none_game_start))));
	    	values.add(playerSpinner);
	    	
	    	if (i == 0) {
				playerSpinner.setSelection(((ArrayAdapter<String>) playerSpinner.getAdapter()).getPosition("Human Player"));
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
	    		hv.addView(playerSpinner, LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
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

			String[] cardsSpecified = null;
			
			String spinnerStr;
			GameType g;
			
			switch (gameType) {
			case RANDOM:
				edit.putString("gameType", TypeOptions.RANDOM.name());

                spinnerStr = (String) randomSpinner.getSelectedItem();
				g = com.mehtank.androminion.server.Strings.getGameTypefromName(spinnerStr);
				if (g != null)
					strs.add((String) g.getName());
				edit.putString("randomPref", spinnerStr);
				break;
			case PRESET:
				edit.putString("gameType", TypeOptions.PRESET.name());

                spinnerStr = (String) presetSpinner.getSelectedItem();
				g = com.mehtank.androminion.server.Strings.getGameTypefromName(spinnerStr);
				if (g != null)
					strs.add((String) g.getName());
				edit.putString("presetPref", spinnerStr);
				break;
			case LAST:
				edit.putString("gameType", TypeOptions.LAST.name());

			    cardsSpecified = lastCards;
			    strs.add("Random");					
				break;
			case SPECIFIED:
			    cardsSpecified = cardsPassOnStartup;
			    strs.add("Random");
				break;
			}
			

			int i = 0;
			for (Spinner s : values) {
                String str = (String) s.getSelectedItem();
                strs.add(str);
                edit.putString("gamePref" + (i++), str);
			}
			
			if(prefs.getBoolean("plat_colony", false)) {
			    strs.add("-platcolony");
			}
			
            if(prefs.getBoolean("quick_play", false)) {
                strs.add("-quickplay");
            }
            
            if(cardsSpecified != null) {
                StringBuilder sb = new StringBuilder();
                sb.append("-cards=");
                boolean first = true;
                for(String card : cardsSpecified) {
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