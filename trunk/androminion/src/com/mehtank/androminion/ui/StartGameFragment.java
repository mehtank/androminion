package com.mehtank.androminion.ui;

import java.util.ArrayList;
import com.mehtank.androminion.R;
import com.vdom.api.GameType;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

public class StartGameFragment extends Fragment implements OnCheckedChangeListener, OnClickListener {
	int port;

	public static final String CARDS_PASSED = "<Specified cards>";
	public static final String CARDS_LAST = "<Last played>";

	
	//Views
	View mView;
	RadioGroup mRandomPreset;
	RadioGroup mLastSpecified;
	RadioButton mRandom;
	RadioButton mPreset;
	RadioButton mLast;
	RadioButton mSpecified;
	
	Spinner mRandomSpinner;
	Spinner mPresetSpinner;
	Spinner mHumanPlayer;
	ArrayList<Spinner> mPlayers = new ArrayList<Spinner>(3);
	
	Button mStartGame;
	
	//Options & Co
	SharedPreferences mPrefs;
	String[] mLastCards;
	String[] mCardsPassOnStartup;
	
	enum TypeOptions {RANDOM, PRESET, LAST, SPECIFIED};
	TypeOptions mGameType;
	
	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		com.mehtank.androminion.server.Strings.context = getActivity().getApplicationContext();
		
        mView = inflater.inflate(R.layout.startgamefragment, null);
        
        //Init all views
        mRandomPreset = (RadioGroup) mView.findViewById(R.id.rgRandomPreset);
        mLastSpecified = (RadioGroup) mView.findViewById(R.id.rgLastSpecified);
        mRandom = (RadioButton) mView.findViewById(R.id.radRandom);
        mRandom.setOnCheckedChangeListener(this);
        mPreset = (RadioButton) mView.findViewById(R.id.radPreset);
        mPreset.setOnCheckedChangeListener(this);
        mLast = (RadioButton) mView.findViewById(R.id.radLast);
        mLast.setOnCheckedChangeListener(this);
        mSpecified = (RadioButton) mView.findViewById(R.id.radSpecified);
        mSpecified.setOnCheckedChangeListener(this);
        mRandomSpinner = (Spinner) mView.findViewById(R.id.spinnerRandom);
        mPresetSpinner = (Spinner) mView.findViewById(R.id.spinnerPreset);
        mPlayers.add((Spinner) mView.findViewById(R.id.spPlayer2));
        mPlayers.add((Spinner) mView.findViewById(R.id.spPlayer3));
        mPlayers.add((Spinner) mView.findViewById(R.id.spPlayer4));
        mStartGame = (Button) mView.findViewById(R.id.butStart);
        mStartGame.setOnClickListener(this);

        //Init prefs
        mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		getLastCards();
        
		//Set radio button state
		if(mLastCards == null){
			mLast.setVisibility(RadioButton.GONE);
		}
		if(getArguments() == null || !getArguments().containsKey("cards")) {
			mSpecified.setVisibility(RadioButton.GONE);
		} else {
			mCardsPassOnStartup = getArguments().getStringArray("cards");
		}
        mGameType = TypeOptions.valueOf(mPrefs.getString("gameType", TypeOptions.PRESET.name()));
        switch(mGameType) {
        case RANDOM:
        	mRandom.setChecked(true); break;
        case PRESET:
        	mPreset.setChecked(true); break;
        case LAST:
        	mLast.setChecked(true); break;
        case SPECIFIED:
        	mSpecified.setChecked(true); break;
        }
        
        //Fill card set spinners
        ArrayList<String> presets = new ArrayList<String>();
        ArrayList<String> randoms = new ArrayList<String>();
        GameType[] types = GameType.values();
        String type;
		for (int i=0; i < types.length; i++) {
			if(types[i] == GameType.Specified) {
				continue;
			}
			type = com.mehtank.androminion.server.Strings.getGameTypeName(types[i]);
			if (types[i].name().startsWith("Random")) {
				randoms.add(type);
			} else {
				presets.add(type);
			}
		}
		mRandomSpinner.setAdapter(createArrayAdapter(randoms));
		mPresetSpinner.setAdapter(createArrayAdapter(presets));
		
		//Fill player spinners
		
        return mView;
	}
	
	private void getLastCards() {
		int count = mPrefs.getInt("LastCardCount", 0);
		if (count > 0) {
			mLastCards = new String[count];
			for (int i = 0; i < count; i++) {
				mLastCards[i] = mPrefs.getString("LastCard" + i, null);
			}
		}
	}
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if(isChecked) {
		if(buttonView == mRandom) {
			mGameType = TypeOptions.RANDOM;
			mLastSpecified.clearCheck();
			mRandomSpinner.setVisibility(Spinner.VISIBLE);
			mPresetSpinner.setVisibility(Spinner.GONE);
		} else if (buttonView == mPreset) {
			mGameType = TypeOptions.PRESET;
			mLastSpecified.clearCheck();
			mRandomSpinner.setVisibility(Spinner.GONE);
			mPresetSpinner.setVisibility(Spinner.VISIBLE);
		} else if (buttonView == mLast) {
			mGameType = TypeOptions.LAST;
			mRandomPreset.clearCheck();
			mPresetSpinner.setVisibility(Spinner.GONE);
			mRandomSpinner.setVisibility(Spinner.GONE);
		} else if (buttonView == mSpecified) {
			mGameType = TypeOptions.SPECIFIED;
			mRandomPreset.clearCheck();
			mPresetSpinner.setVisibility(Spinner.GONE);
			mRandomSpinner.setVisibility(Spinner.GONE);
		}
		}
	}
	
	@Override
	public void onClick(View arg0) {
		SharedPreferences.Editor edit = mPrefs.edit();

		String[] cardsSpecified = null;
		ArrayList<String> strs = new ArrayList<String>();
		String spinnerStr;
		GameType g;
		
		switch (mGameType) {
		case RANDOM:
			edit.putString("gameType", TypeOptions.RANDOM.name());

            spinnerStr = (String) mRandomSpinner.getSelectedItem();
			g = com.mehtank.androminion.server.Strings.getGameTypefromName(spinnerStr);
			if (g != null)
				strs.add((String) g.getName());
			edit.putString("randomPref", spinnerStr);
			break;
		case PRESET:
			edit.putString("gameType", TypeOptions.PRESET.name());

            spinnerStr = (String) mPresetSpinner.getSelectedItem();
			g = com.mehtank.androminion.server.Strings.getGameTypefromName(spinnerStr);
			if (g != null)
				strs.add((String) g.getName());
			edit.putString("presetPref", spinnerStr);
			break;
		case LAST:
			edit.putString("gameType", TypeOptions.LAST.name());

		    cardsSpecified = mLastCards;
		    strs.add("Random");					
			break;
		case SPECIFIED:
		    cardsSpecified = mCardsPassOnStartup;
		    strs.add("Random");
			break;
		}
		

		int i = 0;
		for (Spinner s : mPlayers) {
            String str = (String) s.getSelectedItem();
            strs.add(str);
            edit.putString("gamePref" + (i++), str);
		}
		
		if(mPrefs.getBoolean("plat_colony", false)) {
		    strs.add("-platcolony");
		}
		
        if(mPrefs.getBoolean("quick_play", false)) {
            strs.add("-quickplay");
        }

        if (mPrefs.getBoolean("action_chains", false)) {
            strs.add("-actionchains");
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
        
		//top.handle(new Event(Event.EType.STARTGAME)
		//	.setObject(new EventObject(strs.toArray(new String[0]))));
		//if (!Androminion.NOTOASTS) Toast.makeText(top, top.getString(R.string.toast_starting), Toast.LENGTH_SHORT).show();
		edit.commit();
	}
	
	
	
	private ArrayAdapter<String> createArrayAdapter(ArrayList<String> list) {
	    ArrayAdapter<String> adapter = new ArrayAdapter<String>
	    	(getActivity(), android.R.layout.simple_spinner_item, list);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    return adapter;
	}

/*
	@SuppressWarnings("unchecked")
	public void showDialog(Androminion top, Event e, boolean multiplayer) {
	    if(e == null || e.o == null)
	    {
	        top.debug("Start game called without proper data in event.");
	        return;
	    }
		String[] playerTypes = new String[strs.length - e.i];
        
		for (; at < strs.length; at++)
			playerTypes[at - e.i] = strs[at];

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
				
				ArrayList<String> pts = new ArrayList<String>(Arrays.asList(playerTypes));
				if (!multiplayer) { // disable additional human players
					pts.remove("Human Player");
				}
				pts.add(Player.RANDOM_AI);
				playerTypes = pts.toArray(new String[0]);
	    	} else
	    		hv.addView(playerSpinner, LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
	    	ll.addView(hv);
	    }
	
	}
*/
}