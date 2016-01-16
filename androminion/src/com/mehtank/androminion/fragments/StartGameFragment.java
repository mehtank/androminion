package com.mehtank.androminion.fragments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.actionbarsherlock.app.SherlockFragment;
import com.mehtank.androminion.R;
import com.vdom.api.GameType;
import com.vdom.core.Cards;
import com.vdom.core.Expansion;

public class StartGameFragment extends SherlockFragment implements OnClickListener, OnItemSelectedListener {
    @SuppressWarnings("unused")
    private static final String TAG = "StartGameFragment";
    
    private static final String RANDOM_ALL_CARDS_PREF = "randomAllCards";
    private static final String RANDOM_NUM_EVENTS_PREF = "randomNumEvents";
    private static final String RANDOM_MAX_EVENTS_PREF = "randomMaxEvents";
    private static final String RANDOM_USE_SET_PREFIX = "randomUse";
    private static final int DEFAULT_MAX_RANDOM_EVENTS = 2;
    

    //Views
    View mView;

    Spinner mCardsetSpinner;
    Spinner mPresetSpinner;
    CheckBox mRandomAllCheckbox;
    LinearLayout mRandomEventsLayout;
    Spinner mRandomEventsSpinner;
    LinearLayout mRandomOptionsLayout;
    ToggleButton mRandomBase;
    ToggleButton mRandomIntrigue;
    ToggleButton mRandomSeaside;
    ToggleButton mRandomAlchemy;
    ToggleButton mRandomProsperity;
    ToggleButton mRandomCornucopia;
    ToggleButton mRandomHinterlands;
    ToggleButton mRandomDarkAges;
    ToggleButton mRandomGuilds;
    ToggleButton mRandomAdventures;
    Map<Expansion, ToggleButton> completeSets;
    ToggleButton mRandomPromo;
    
    Spinner mPlayer2;
    Spinner mPlayer3;
    Spinner mPlayer4;
    Spinner mPlayer5;
    Spinner mPlayer6;

    Button mStartGame;

    // Options & Co
    SharedPreferences mPrefs;
    boolean mMultiplayer = false;
    String[] mLastCards;
    String[] mCardsPassOnStartup;
    
    //TODO: find a better solution for these
    static final String HUMANPLAYER = "Human Player";
    static final String[] PLAYERTYPES =  {
        "Human Player",
        "Drew (AI)",
        "Earl (AI)",
        "Mary (AI)",
        "Chuck (AI)",
        "Sarah (AI)",
        "Patrick (AI)",
        "Random AI",
    };

    enum TypeOptions {
        RANDOM, PRESET, LAST, SPECIFIED
    };

    TypeOptions mGameType;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        com.mehtank.androminion.ui.Strings.initContext(getActivity().getApplicationContext());

        mView = inflater.inflate(R.layout.fragment_startgame, null);

        mCardsetSpinner = (Spinner) mView.findViewById(R.id.spinnerCardset);
        mCardsetSpinner.setOnItemSelectedListener(this);        
        mPresetSpinner = (Spinner) mView.findViewById(R.id.spinnerPreset);
        mRandomAllCheckbox = (CheckBox) mView.findViewById(R.id.checkboxRandomAll);
        mRandomOptionsLayout = (LinearLayout) mView.findViewById(R.id.linearLayoutRandomOptions);
        mRandomEventsLayout = (LinearLayout) mView.findViewById(R.id.linearLayoutRandomEvents);
        mRandomEventsSpinner = (Spinner) mView.findViewById(R.id.spinnerRandomEvents);
        completeSets = new HashMap<Expansion, ToggleButton>();
        completeSets.put(Expansion.Base, mRandomBase = (ToggleButton) mView.findViewById(R.id.toggleButtonBaseSet));
        completeSets.put(Expansion.Intrigue, mRandomIntrigue = (ToggleButton) mView.findViewById(R.id.toggleButtonIntrigue));
        completeSets.put(Expansion.Seaside, mRandomSeaside = (ToggleButton) mView.findViewById(R.id.toggleButtonSeaside));
        completeSets.put(Expansion.Alchemy, mRandomAlchemy = (ToggleButton) mView.findViewById(R.id.toggleButtonAlchemy));
        completeSets.put(Expansion.Prosperity, mRandomProsperity = (ToggleButton) mView.findViewById(R.id.toggleButtonProsperity));
        completeSets.put(Expansion.Cornucopia, mRandomCornucopia = (ToggleButton) mView.findViewById(R.id.toggleButtonCornucopia));
        completeSets.put(Expansion.Hinterlands, mRandomHinterlands = (ToggleButton) mView.findViewById(R.id.toggleButtonHinterlands));
        completeSets.put(Expansion.DarkAges, mRandomDarkAges = (ToggleButton) mView.findViewById(R.id.toggleButtonDarkAges));
        completeSets.put(Expansion.Guilds, mRandomGuilds = (ToggleButton) mView.findViewById(R.id.toggleButtonGuilds));
        completeSets.put(Expansion.Adventures, mRandomAdventures = (ToggleButton) mView.findViewById(R.id.toggleButtonAdventures));
        mRandomPromo = (ToggleButton) mView.findViewById(R.id.toggleButtonPromo);
                
        mPlayer2 = (Spinner) mView.findViewById(R.id.spPlayer2);
        mPlayer3 = (Spinner) mView.findViewById(R.id.spPlayer3);
        mPlayer4 = (Spinner) mView.findViewById(R.id.spPlayer4);
        mPlayer5 = (Spinner) mView.findViewById(R.id.spPlayer5);
        mPlayer6 = (Spinner) mView.findViewById(R.id.spPlayer6);
        
        // TODO: Set listeners for top spinner

        mStartGame = (Button) mView.findViewById(R.id.butStart);
        mStartGame.setOnClickListener(this);

        //Init prefs
        mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        getLastCards();

        // Fill cardset spinner with values
        ArrayList<String> cardspinnerlist = new ArrayList<String>();
        cardspinnerlist.add(getResources().getString(R.string.game_type_random));
        cardspinnerlist.add(getResources().getString(R.string.game_type_preset));
        if (mLastCards != null) {
            cardspinnerlist.add(getResources().getString(R.string.game_type_last));
        }
        if (getArguments() != null && getArguments().containsKey("cards")){
            cardspinnerlist.add(getResources().getString(R.string.game_type_specified));
            mCardsPassOnStartup = getArguments().getStringArray("cards");
            mGameType = TypeOptions.SPECIFIED;
        } else {
            mGameType = TypeOptions.valueOf(mPrefs.getString("gameType", TypeOptions.PRESET.name()));
        }

        ArrayAdapter<String> cardsetAdapter = createArrayAdapter(cardspinnerlist);
        mCardsetSpinner.setAdapter(cardsetAdapter);
        
        // Change preselected Radio button / spinner value
        switch (mGameType) {
            case RANDOM:
                mCardsetSpinner.setSelection(cardspinnerlist.indexOf(getResources().getString(R.string.game_type_random)));
                break;
            case PRESET:
                mCardsetSpinner.setSelection(cardspinnerlist.indexOf(getResources().getString(R.string.game_type_preset)));
                break;
            case LAST:
                mCardsetSpinner.setSelection(cardspinnerlist.indexOf(getResources().getString(R.string.game_type_last)));
                break;
            case SPECIFIED:
                mCardsetSpinner.setSelection(cardspinnerlist.indexOf(getResources().getString(R.string.game_type_specified)));
                break;
        }

        //Fill card set spinners
        ArrayList<String> presets = new ArrayList<String>();
        ArrayList<String> randoms = new ArrayList<String>();

        GameType[] types = GameType.values();
        String type;
        for (int i = 0; i < types.length; i++) {
            if (types[i] == GameType.Specified) {
                continue;
            }
            type = com.mehtank.androminion.ui.Strings.getGameTypeName(types[i]);
            if (types[i].name().startsWith("Random")) {
                randoms.add(type);
            } else {
                presets.add(type);
            }
        }

        ArrayAdapter<String> adapter = createArrayAdapter(presets);
        mPresetSpinner.setAdapter(adapter);
        mPresetSpinner.setSelection(adapter.getPosition(mPrefs.getString("presetPref", "First Game (Base)")));
        
        // Fill random game options
        mRandomAllCheckbox.setChecked(mPrefs.getBoolean(RANDOM_ALL_CARDS_PREF, true));
        
        int numEvents = mPrefs.getInt(RANDOM_NUM_EVENTS_PREF, -1);
        int totalEvents = Cards.eventsCards.size();
        numEvents = Math.min(totalEvents, numEvents);
        numEvents = Math.max(numEvents, -1);
                
        ArrayList<String> eventsSpinnerList = new ArrayList<String>();
        eventsSpinnerList.add(getResources().getString(R.string.random_events_none));
        eventsSpinnerList.add(getResources().getString(R.string.random_events_random));
        for (int i = 0; i < totalEvents; ++i) {
        	eventsSpinnerList.add((i + 1) + "");
        }
        ArrayAdapter<String> numEventsAdapter = createArrayAdapter(eventsSpinnerList);
        mRandomEventsSpinner.setAdapter(numEventsAdapter);
        mRandomEventsSpinner.setSelection(numEventsToPos(numEvents));
        
        for (Expansion set : completeSets.keySet()) {
        	completeSets.get(set).setChecked(mPrefs.getBoolean(RANDOM_USE_SET_PREFIX + set, set == Expansion.Base));
        }
        mRandomPromo.setChecked(mPrefs.getBoolean(RANDOM_USE_SET_PREFIX + Expansion.Promo, false));
        
        // ensure we always have enough cards selected to make a valid Kingdom
        int numChecked = getNumChecked();
        if (numChecked == 0) {
        	mRandomBase.setChecked(true);
        	mRandomBase.setEnabled(false);
        } else if (numChecked == 1) {
        	disableFirstSelected();
        }

        initListeners();

        // Fill player spinners
        ArrayList<String> players = new ArrayList<String>(PLAYERTYPES.length + 1);
        for (String s : PLAYERTYPES) {
            players.add(s);
        }
        if (!mMultiplayer) {
            players.remove("Human Player");
        }
        String player = getString(R.string.player);
        adapter = createArrayAdapter(players);

        ((TextView) mView.findViewById(R.id.txtPlayer1))
                .setText(player + "1:  ");

        players = new ArrayList<String>(players);
        players.add(getString(R.string.none_game_start));
        adapter = createArrayAdapter(players);

        ((TextView) mView.findViewById(R.id.txtPlayer2))
                .setText(player + "2:  ");
        mPlayer2.setPrompt(player+"2");
        mPlayer2.setAdapter(adapter);
        mPlayer2.setSelection(adapter.getPosition(mPrefs.getString("gamePref2", getString(R.string.none_game_start))));

        ((TextView) mView.findViewById(R.id.txtPlayer3))
                .setText(player + "3:  ");
        mPlayer3.setPrompt(player+"3");
        mPlayer3.setAdapter(adapter);
        mPlayer3.setSelection(adapter.getPosition(mPrefs.getString("gamePref3", getString(R.string.none_game_start))));

        ((TextView) mView.findViewById(R.id.txtPlayer4))
                .setText(player + "4:  ");
        mPlayer4.setPrompt(player+"4");
        mPlayer4.setAdapter(adapter);
        mPlayer4.setSelection(adapter.getPosition(mPrefs.getString("gamePref4", getString(R.string.none_game_start))));

        ((TextView) mView.findViewById(R.id.txtPlayer5))
                .setText(player + "5:  ");
        mPlayer5.setPrompt(player+"5");
        mPlayer5.setAdapter(adapter);
        mPlayer5.setSelection(adapter.getPosition(mPrefs.getString("gamePref5", getString(R.string.none_game_start))));

        ((TextView) mView.findViewById(R.id.txtPlayer6))
                .setText(player + "6:  ");
        mPlayer6.setPrompt(player+"6");
        mPlayer6.setAdapter(adapter);
        mPlayer6.setSelection(adapter.getPosition(mPrefs.getString("gamePref6", getString(R.string.none_game_start))));

        updateVisibility();
        
        return mView;
    }
    
    private void initListeners() {
    	mRandomAllCheckbox.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				updateVisibility();
			}
		});
    	for (ToggleButton button : completeSets.values()) {
    		button.setOnCheckedChangeListener(coreSetsListener);
    	}
	}

    private int getNumChecked() {
    	int num = 0;
    	for (ToggleButton button : completeSets.values()) {
    		num += button.isChecked() ? 1 : 0;
    	}
		return num;
	}
	
	private OnCheckedChangeListener coreSetsListener = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if (isChecked) {
				enableAll();
			} else if (getNumChecked() == 1) {
				disableFirstSelected();
			}
		}
	};
	

	private void enableAll() {
		for (ToggleButton button : completeSets.values()) {
			if (!button.isEnabled()) { 
				button.setEnabled(true);
			}
    	}
	}

	private void disableFirstSelected() {
		for (ToggleButton button : completeSets.values()) {
			if (button.isChecked()) { 
				button.setEnabled(false);
				break;
			}
    	}
	}

    private int posToNumEvents(int pos) {
    	int numEvents = 0;
    	if (pos == 1)
        	numEvents = -mPrefs.getInt(RANDOM_MAX_EVENTS_PREF, DEFAULT_MAX_RANDOM_EVENTS);
    	else if (pos > 1) {
        	numEvents = pos - 1;
        }
    	return numEvents;
	}
    
    private int numEventsToPos(int numEvents) {
    	int pos = 0;
    	if (numEvents < 0)
        	pos = 1;
    	else if (numEvents > 0) {
        	pos = numEvents + 1;
        }
    	return pos;
	}
    
    private void updateVisibility() {
    	int randomVisibility = mGameType == TypeOptions.RANDOM ? View.VISIBLE : View.GONE;
    	mRandomEventsLayout.setVisibility(randomVisibility);
    	mRandomAllCheckbox.setVisibility(randomVisibility);
    	mRandomOptionsLayout.setVisibility(randomVisibility);
    	
    	if (randomVisibility == View.VISIBLE) {
        	if (mRandomAllCheckbox.isChecked()) {
    			mRandomOptionsLayout.setVisibility(View.GONE);
    		} else {
    			mRandomOptionsLayout.setVisibility(View.VISIBLE);
    		}
    	}
    	
    	int presetVisibility = mGameType == TypeOptions.PRESET ? View.VISIBLE : View.GONE;
    	mPresetSpinner.setVisibility(presetVisibility);
    	
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

    private <T> ArrayAdapter<T> createArrayAdapter(ArrayList<T> list) {
        ArrayAdapter<T> adapter = new ArrayAdapter<T>
                (getActivity(), android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
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

                g = GameType.Random;
                String gameTypeString = g.getName();
                if (!mRandomAllCheckbox.isChecked()) {
	                for (Expansion set : completeSets.keySet()) {
	                	if (completeSets.get(set).isChecked()) {
	                		gameTypeString += "-" + set;
	                	}
	                }
	                if (mRandomPromo.isChecked()) {
	                	gameTypeString += "-" + Expansion.Promo;
	                }
                }
                strs.add(gameTypeString);
                
                int numEvents = posToNumEvents(mRandomEventsSpinner.getSelectedItemPosition());
                if (numEvents != 0) {
                	strs.add("-eventcards" + numEvents);
                }
                
                edit.putBoolean(RANDOM_ALL_CARDS_PREF, mRandomAllCheckbox.isChecked());
                edit.putInt(RANDOM_NUM_EVENTS_PREF, numEvents);
                for (Expansion set : completeSets.keySet()) {
                	edit.putBoolean(RANDOM_USE_SET_PREFIX + set, completeSets.get(set).isChecked());
                }
                edit.putBoolean(RANDOM_USE_SET_PREFIX + Expansion.Promo, mRandomPromo.isChecked());
                
                break;
            case PRESET:
                edit.putString("gameType", TypeOptions.PRESET.name());

                spinnerStr = (String) mPresetSpinner.getSelectedItem();
                g = com.mehtank.androminion.ui.Strings.getGameTypefromName(spinnerStr);
                if (g != null)
                    strs.add(g.getName());
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

        String str = HUMANPLAYER;
        strs.add(str);

        str = (String) mPlayer2.getSelectedItem();
        strs.add(str);
        edit.putString("gamePref2", str);

        str = (String) mPlayer3.getSelectedItem();
        strs.add(str);
        edit.putString("gamePref3", str);

        str = (String) mPlayer4.getSelectedItem();
        strs.add(str);
        edit.putString("gamePref4", str);

        str = (String) mPlayer5.getSelectedItem();
        strs.add(str);
        edit.putString("gamePref5", str);

        str = (String) mPlayer6.getSelectedItem();
        strs.add(str);
        edit.putString("gamePref6", str);

        if(mPrefs.getBoolean("plat_colony", false)) {
            strs.add("-platcolony");
        }

        if (mPrefs.getBoolean("use_shelters", false))
        {
            strs.add("-useshelters");
        }

        strs.add("-blackmarketcount" + mPrefs.getString("black_market_count", "25"));
        
        if(mPrefs.getBoolean("quick_play", false)) {
            strs.add("-quickplay");
        }

        if(mPrefs.getBoolean("mask_names", false)) {
            strs.add("-masknames");
        }

        if(mPrefs.getBoolean("sort_cards", false)) {
            strs.add("-sortcards");
        }

        if (mPrefs.getBoolean("action_chains", false)) {
            strs.add("-actionchains");
        }

        if (mPrefs.getBoolean("suppress_redundant_reactions", true)) {
            strs.add("-suppressredundantreactions");
        }

        if (mPrefs.getBoolean("equal_start_hands", false)) {
            strs.add("-equalstarthands");
        }

        if (mPrefs.getBoolean("start_guilds_coin_tokens", false)) {
            strs.add("-startguildscointokens");
        }
        if (mPrefs.getBoolean("less_provinces", false)) {
            strs.add("-lessprovinces");
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
        edit.commit();

        try {
            ((OnStartGameListener) getActivity()).onStartGameClick(strs);
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + " must implement OnStartGameListener");
        }
    }

	@Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        if (parent.getItemAtPosition(pos) == getResources().getString(R.string.game_type_random)){
            mGameType = TypeOptions.RANDOM;
        } else if (parent.getItemAtPosition(pos) == getResources().getString(R.string.game_type_preset)){
            mGameType = TypeOptions.PRESET;
        } else if (parent.getItemAtPosition(pos) == getResources().getString(R.string.game_type_last)){
            mGameType = TypeOptions.LAST;
        } else if (parent.getItemAtPosition(pos) == getResources().getString(R.string.game_type_specified)){
            mGameType = TypeOptions.SPECIFIED;
        }
        updateVisibility();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Nothing selected - so let's do nothing
    }

    // Container Activity must implement this interface
    public interface OnStartGameListener {
        public void onStartGameClick(ArrayList<String> values);
    }

}
