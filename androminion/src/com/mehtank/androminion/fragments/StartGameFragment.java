package com.mehtank.androminion.fragments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
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
import com.mehtank.androminion.ui.Strings;
import com.vdom.api.GameType;
import com.vdom.core.CardSet;
import com.vdom.core.Cards;
import com.vdom.core.Expansion;
import com.vdom.core.CardSet.UseOptionalCards;

public class StartGameFragment extends SherlockFragment implements OnClickListener, OnItemSelectedListener {
    @SuppressWarnings("unused")
    private static final String TAG = "StartGameFragment";
    
    private static final String PLAT_COLONY_PREF = "platinumColonyChance";
    private static final String SHELTERS_PREF = "sheltersChance";
    private static final String RANDOM_ALL_CARDS_PREF = "randomAllCards";
    private static final String RANDOM_NUM_EVENTS_PREF = "randomNumEvents";
    private static final String RANDOM_MAX_EVENTS_PREF = "randomMaxEvents";
    private static final String RANDOM_NUM_LANDMARKS_PREF = "randomNumLandmarks";
    private static final String RANDOM_MAX_LANDMARKS_PREF = "randomMaxLandmarks";
    private static final String RANDOM_USE_SET_PREFIX = "randomUse";
    private static final int DEFAULT_MAX_RANDOM_EVENTS = 2;
    private static final int DEFAULT_MAX_RANDOM_LANDMARKS = 2;
    

    //Views
    View mView;

    Spinner mCardsetSpinner;
    Spinner mPresetSpinner;
    CheckBox mRandomAllCheckbox;
    TextView mGameCards;
    LinearLayout mPlatColonyLayout;
    Spinner mPlatColonySpinner;
    LinearLayout mSheltersLayout;
    Spinner mSheltersSpinner;
    LinearLayout mRandomEventsLayout;
    Spinner mRandomEventsSpinner;
    LinearLayout mRandomLandmarksLayout;
    Spinner mRandomLandmarksSpinner;
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
    ToggleButton mRandomEmpires;
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
    
    private static class GameTypeItem {
    	private final GameType gameType;
    	private final String displayString;
    	
    	public GameTypeItem(GameType gameType, String displayString) {
    		this.gameType = gameType;
    		this.displayString = displayString;
    	}
    	
    	public GameType getGameType() {
    		return gameType;
    	}
    	
    	public String getDisplayString() {
    		return displayString;
    	}
    	
    	@Override
    	public boolean equals(Object o) {
    		if (!(o instanceof GameTypeItem))
    			return false;
    		return gameType.equals(((GameTypeItem)o).getGameType());
    	}
    	
    	@Override
    	public String toString() {
    		return displayString;
    	}
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        com.mehtank.androminion.ui.Strings.initContext(getActivity().getApplicationContext());

        mView = inflater.inflate(R.layout.fragment_startgame, null);

        mCardsetSpinner = (Spinner) mView.findViewById(R.id.spinnerCardset);
        mCardsetSpinner.setOnItemSelectedListener(this);  
        mPresetSpinner = (Spinner) mView.findViewById(R.id.spinnerPreset);
        mRandomAllCheckbox = (CheckBox) mView.findViewById(R.id.checkboxRandomAll);
        mPlatColonyLayout = (LinearLayout) mView.findViewById(R.id.linearLayoutPlatinumColony);
        mPlatColonySpinner = (Spinner) mView.findViewById(R.id.spinnerPlatinumColony);
        mSheltersLayout = (LinearLayout) mView.findViewById(R.id.linearLayoutShelters);
        mSheltersSpinner = (Spinner) mView.findViewById(R.id.spinnerShelters);
        mRandomOptionsLayout = (LinearLayout) mView.findViewById(R.id.linearLayoutRandomOptions);
        mRandomEventsLayout = (LinearLayout) mView.findViewById(R.id.linearLayoutRandomEvents);
        mRandomEventsSpinner = (Spinner) mView.findViewById(R.id.spinnerRandomEvents);
        mRandomLandmarksLayout = (LinearLayout) mView.findViewById(R.id.linearLayoutRandomLandmarks);
        mRandomLandmarksSpinner = (Spinner) mView.findViewById(R.id.spinnerRandomLandmarks);
        mGameCards = (TextView) mView.findViewById(R.id.gameCards);
        
        //Init prefs
        mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        
        completeSets = new HashMap<Expansion, ToggleButton>();
        completeSets.put(getBaseEdition(), mRandomBase = (ToggleButton) mView.findViewById(R.id.toggleButtonBaseSet));
        completeSets.put(getIntrigueEdition(), mRandomIntrigue = (ToggleButton) mView.findViewById(R.id.toggleButtonIntrigue));
        completeSets.put(Expansion.Seaside, mRandomSeaside = (ToggleButton) mView.findViewById(R.id.toggleButtonSeaside));
        completeSets.put(Expansion.Alchemy, mRandomAlchemy = (ToggleButton) mView.findViewById(R.id.toggleButtonAlchemy));
        completeSets.put(Expansion.Prosperity, mRandomProsperity = (ToggleButton) mView.findViewById(R.id.toggleButtonProsperity));
        completeSets.put(Expansion.Cornucopia, mRandomCornucopia = (ToggleButton) mView.findViewById(R.id.toggleButtonCornucopia));
        completeSets.put(Expansion.Hinterlands, mRandomHinterlands = (ToggleButton) mView.findViewById(R.id.toggleButtonHinterlands));
        completeSets.put(Expansion.DarkAges, mRandomDarkAges = (ToggleButton) mView.findViewById(R.id.toggleButtonDarkAges));
        completeSets.put(Expansion.Guilds, mRandomGuilds = (ToggleButton) mView.findViewById(R.id.toggleButtonGuilds));
        completeSets.put(Expansion.Adventures, mRandomAdventures = (ToggleButton) mView.findViewById(R.id.toggleButtonAdventures));
        completeSets.put(Expansion.Empires, mRandomEmpires = (ToggleButton) mView.findViewById(R.id.toggleButtonEmpires));
        mRandomPromo = (ToggleButton) mView.findViewById(R.id.toggleButtonPromo);
                
        mPlayer2 = (Spinner) mView.findViewById(R.id.spPlayer2);
        mPlayer3 = (Spinner) mView.findViewById(R.id.spPlayer3);
        mPlayer4 = (Spinner) mView.findViewById(R.id.spPlayer4);
        mPlayer5 = (Spinner) mView.findViewById(R.id.spPlayer5);
        mPlayer6 = (Spinner) mView.findViewById(R.id.spPlayer6);
        
        // TODO: Set listeners for top spinner

        mStartGame = (Button) mView.findViewById(R.id.butStart);
        mStartGame.setOnClickListener(this);

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
        ArrayList<GameTypeItem> presets = new ArrayList<GameTypeItem>();
        ArrayList<GameTypeItem> randoms = new ArrayList<GameTypeItem>();
        ArrayList<Expansion> blackListedExpansions = new ArrayList<Expansion>();
        switch (getBaseEdition()) {
        case Base:
        	blackListedExpansions.add(Expansion.Base2E);
        	break;
        case Base2E:
        	blackListedExpansions.add(Expansion.Base);
        	break;
        default:
        	break;
        }
        switch (getIntrigueEdition()) {
        case Intrigue:
        	blackListedExpansions.add(Expansion.Intrigue2E);
        	break;
        case Intrigue2E:
        	blackListedExpansions.add(Expansion.Intrigue);
        	break;
        default:
        	break;
        }

        GameTypeLoop: for (GameType type : GameType.values()) {
        	if (type == GameType.Specified) {
                continue;
            }
            String typeName = com.mehtank.androminion.ui.Strings.getGameTypeName(type);
            if (type.name().startsWith("Random")) {
                randoms.add(new GameTypeItem(type, typeName));
            } else {
            	for (Expansion expansion : type.getExpansions()) {
            		if (blackListedExpansions.contains(expansion))
            			continue GameTypeLoop;
            	}
            	String suffix = "";
            	if (type.getExpansions().size() > 0) {
	            	suffix = " (";
	            	String separator = "";
	            	for (Expansion expansion : type.getExpansions()) {
	            		suffix += separator;
	            		suffix += com.mehtank.androminion.ui.Strings.getExpansionName(expansion);
	            		separator = ", ";
	            	}
	            	suffix += ")";
            	}
            	presets.add(new GameTypeItem(type, typeName + suffix));
            }
        }
        
        ArrayAdapter<GameTypeItem> gameTypeAdapter = createArrayAdapter(presets);
        mPresetSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				GameType g = ((GameTypeItem)mPresetSpinner.getSelectedItem()).gameType;
				CardSet cardSet = CardSet.getCardSetMap().get(g);
				mGameCards.setText(Strings.format(R.string.card_set_cards, Strings.getCardSetDescription(cardSet)));
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) { }
        	
		});
        mPresetSpinner.setAdapter(gameTypeAdapter);
        String defaultGameType = getBaseEdition().equals(Expansion.Base) ? "FirstGame" : "FirstGame2";
        GameType gameType;
        try {
        	gameType = GameType.valueOf(mPrefs.getString("presetPref", defaultGameType));
        } catch (IllegalArgumentException e) {
        	gameType = GameType.valueOf(defaultGameType);
        }
        
        int idx = gameTypeAdapter.getPosition(new GameTypeItem(gameType, ""));
        if (idx == -1)
        	idx = 0;
        mPresetSpinner.setSelection(idx);
        
        // Fill platinum/colony & shelters spinners
        int platColonyChance = mPrefs.getInt(PLAT_COLONY_PREF, -1);
        ArrayList<String> platColonySpinnerList = new ArrayList<String>();
        platColonySpinnerList.add(getResources().getString(R.string.include_cards_random));
        platColonySpinnerList.add(getResources().getString(R.string.include_cards_yes));
        platColonySpinnerList.add(getResources().getString(R.string.include_cards_no));
        platColonySpinnerList.add(String.format(getResources().getString(R.string.include_cards_percent), 25));
        platColonySpinnerList.add(String.format(getResources().getString(R.string.include_cards_percent), 50));
        platColonySpinnerList.add(String.format(getResources().getString(R.string.include_cards_percent), 75));
        ArrayAdapter<String> platColonyAdapter = createArrayAdapter(platColonySpinnerList);
        mPlatColonySpinner.setAdapter(platColonyAdapter);
        mPlatColonySpinner.setSelection(chanceToPos(platColonyChance));
        
        int sheltersChance = mPrefs.getInt(SHELTERS_PREF, -1);
        ArrayList<String> sheltersSpinnerList = new ArrayList<String>();
        sheltersSpinnerList.add(getResources().getString(R.string.include_cards_random));
        sheltersSpinnerList.add(getResources().getString(R.string.include_cards_yes));
        sheltersSpinnerList.add(getResources().getString(R.string.include_cards_no));
        sheltersSpinnerList.add(String.format(getResources().getString(R.string.include_cards_percent), 25));
        sheltersSpinnerList.add(String.format(getResources().getString(R.string.include_cards_percent), 50));
        sheltersSpinnerList.add(String.format(getResources().getString(R.string.include_cards_percent), 75));
        ArrayAdapter<String> sheltersAdapter = createArrayAdapter(sheltersSpinnerList);
        mSheltersSpinner.setAdapter(sheltersAdapter);
        mSheltersSpinner.setSelection(chanceToPos(sheltersChance));
        
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
        
        int numLandmarks = mPrefs.getInt(RANDOM_NUM_LANDMARKS_PREF, -1);
        int totalLandmarks = Cards.landmarkCards.size();
        numLandmarks = Math.min(totalLandmarks, numLandmarks);
        numLandmarks = Math.max(numLandmarks, -1);
                
        ArrayList<String> landmarksSpinnerList = new ArrayList<String>();
        landmarksSpinnerList.add(getResources().getString(R.string.random_landmarks_none));
        landmarksSpinnerList.add(getResources().getString(R.string.random_landmarks_random));
        for (int i = 0; i < totalLandmarks; ++i) {
        	landmarksSpinnerList.add((i + 1) + "");
        }
        ArrayAdapter<String> numLandmarksAdapter = createArrayAdapter(landmarksSpinnerList);
        mRandomLandmarksSpinner.setAdapter(numLandmarksAdapter);
        mRandomLandmarksSpinner.setSelection(numLandmarksToPos(numLandmarks));
        
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
        ArrayAdapter<String> adapter = createArrayAdapter(players);

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
    
    private Expansion getBaseEdition() {
		return Expansion.valueOf(mPrefs.getString("base_set_edition", "Base2E"));
	}
    
    private Expansion getIntrigueEdition() {
		return Expansion.valueOf(mPrefs.getString("intrigue_edition", "Intrigue2E"));
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
	
	private int chanceToPos(int percentChance) {
    	switch(percentChance) {
    	case -1: return 0;
    	case 100: return 1;
    	case 0: return 2;
    	case 25: return 3;
    	case 50: return 4;
    	case 75: return 5;
    	}
    	return 0;
	}
	
	private int posToChance(int pos) {
    	switch(pos) {
    	case 0: return -1;
    	case 1: return 100;
    	case 2: return 0;
    	case 3: return 25;
    	case 4: return 50;
    	case 5: return 75;
    	}
    	return -1;
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
    
    private int posToNumLandmarks(int pos) {
    	int numLandmarks = 0;
    	if (pos == 1)
        	numLandmarks = -mPrefs.getInt(RANDOM_MAX_LANDMARKS_PREF, DEFAULT_MAX_RANDOM_LANDMARKS);
    	else if (pos > 1) {
        	numLandmarks = pos - 1;
        }
    	return numLandmarks;
	}
    
    private int numLandmarksToPos(int numLandmarks) {
    	int pos = 0;
    	if (numLandmarks < 0)
        	pos = 1;
    	else if (numLandmarks > 0) {
        	pos = numLandmarks + 1;
        }
    	return pos;
	}
    
    private void updateVisibility() {
    	int randomVisibility = mGameType == TypeOptions.RANDOM ? View.VISIBLE : View.GONE;
    	mRandomEventsLayout.setVisibility(randomVisibility);
    	mRandomLandmarksLayout.setVisibility(randomVisibility);
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
    	mGameCards.setVisibility(presetVisibility);
    	
    	int includeCardsVisibility = mGameType == TypeOptions.PRESET || mGameType == TypeOptions.RANDOM ? View.VISIBLE : View.GONE;
    	mPlatColonyLayout.setVisibility(includeCardsVisibility);
    	mSheltersLayout.setVisibility(includeCardsVisibility);
    	
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
        GameType g;

        switch (mGameType) {
            case RANDOM:
                edit.putString("gameType", TypeOptions.RANDOM.name());

                g = GameType.Random;
                String gameTypeString = g.toString();
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
                if (mRandomAllCheckbox.isChecked()) {
                	String exclusions = "";
                	if (getBaseEdition() == Expansion.Base) {
                		exclusions += "-" + Expansion.Base2E;
                	} else if (getBaseEdition() == Expansion.Base2E) {
                		exclusions += "-" + Expansion.Base;
                	}
                	if (getIntrigueEdition() == Expansion.Intrigue) {
                		exclusions += "-" + Expansion.Intrigue2E;
                	} else if (getIntrigueEdition() == Expansion.Intrigue2E) {
                		exclusions += "-" + Expansion.Intrigue;
                	}
                	if (exclusions.length() > 0) {
                		strs.add("-randomexcludes" + exclusions);
                	}
                }
                
                int numEvents = posToNumEvents(mRandomEventsSpinner.getSelectedItemPosition());
                if (numEvents != 0) {
                	strs.add("-eventcards" + numEvents);
                }
                int numLandmarks = posToNumLandmarks(mRandomLandmarksSpinner.getSelectedItemPosition());
                if (numLandmarks != 0) {
                	strs.add("-landmarkcards" + numLandmarks);
                }
                
                edit.putBoolean(RANDOM_ALL_CARDS_PREF, mRandomAllCheckbox.isChecked());
                edit.putInt(RANDOM_NUM_EVENTS_PREF, numEvents);
                edit.putInt(RANDOM_NUM_LANDMARKS_PREF, numLandmarks);
                for (Expansion set : completeSets.keySet()) {
                	edit.putBoolean(RANDOM_USE_SET_PREFIX + set, completeSets.get(set).isChecked());
                }
                edit.putBoolean(RANDOM_USE_SET_PREFIX + Expansion.Promo, mRandomPromo.isChecked());
                
                break;
            case PRESET:
                edit.putString("gameType", TypeOptions.PRESET.name());
                g = ((GameTypeItem) mPresetSpinner.getSelectedItem()).getGameType();
                if (g != null)
                    strs.add(g.toString());
                edit.putString("presetPref", g.toString());
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

        if (mPlatColonySpinner.getVisibility() == View.VISIBLE) {
		    int platColonyChance = posToChance(mPlatColonySpinner.getSelectedItemPosition());
		    if(platColonyChance != -1) {
		        strs.add("-platcolony" + platColonyChance);
		    }
		    edit.putInt(PLAT_COLONY_PREF, platColonyChance);
        }

        if (mSheltersSpinner.getVerticalFadingEdgeLength() == View.VISIBLE) {
	        int sheltersChance = posToChance(mSheltersSpinner.getSelectedItemPosition());
	        if (sheltersChance != -1) {
	            strs.add("-useshelters" + sheltersChance);
	        }
	        edit.putInt(SHELTERS_PREF, sheltersChance);
        }

        strs.add("-blackmarketcount" + mPrefs.getString("black_market_count", "25"));

        if (!mPrefs.getString("bm_split_piles", "None").equals("None")) {
            strs.add("-blackmarketsplitpiles-" + mPrefs.getString("bm_split_piles", "None"));
        }

        if (mPrefs.getBoolean("bmOnlyCardsFromUsedExpansions", false)) {
            strs.add("-blackmarketonlycardsfromusedexpansions");
        }

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
        if (mPrefs.getBoolean("no_cards", false)) {
            strs.add("-nocards");
        }
        if (mPrefs.getBoolean("less_provinces", false)) {
            strs.add("-lessprovinces");
        }
        if (mPrefs.getBoolean("god_mode", false)) {
            strs.add("-godmode");
        }
        if (mPrefs.getBoolean("disable_ai", false)) {
            strs.add("-disableai");
        }
        if (mPrefs.getBoolean("control_ai", false)) {
            strs.add("-controlai");
        }
        if (!mPrefs.getBoolean("errata_masquerade", true)) {
        	strs.add("-erratamasqueradealwaysaffects");
        }
        if (!mPrefs.getBoolean("errata_mine", true)) {
        	strs.add("-erratamineforced");
        }
        if (!mPrefs.getBoolean("errata_moneylender", true)) {
        	strs.add("-erratamoneylenderforced");
        }
        if (!mPrefs.getBoolean("errata_posessor_takes_tokens", true)) {
        	strs.add("-erratapossessedtakestokens");
        }
        if (!mPrefs.getBoolean("errata_throneroom", true)) {
        	strs.add("-erratathroneroomforced");
        }
        if (!mPrefs.getBoolean("errata_shuffling", true)) {
        	strs.add("-erratashuffledeckemptyonly");
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
            Log.d("Cards specified",sb.toString());
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
