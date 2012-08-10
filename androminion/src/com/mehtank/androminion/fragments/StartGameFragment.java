package com.mehtank.androminion.fragments;

import java.util.ArrayList;

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
import android.widget.Spinner;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.mehtank.androminion.R;
import com.vdom.api.GameType;

public class StartGameFragment extends SherlockFragment implements OnClickListener, OnItemSelectedListener {
	@SuppressWarnings("unused")
	private static final String TAG = "StartGameFragment";
	
	//Views
	View mView;

	Spinner mCardsetSpinner;
	Spinner mRandomSpinner;
	Spinner mPresetSpinner;
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
            "Random AI",
	};

	enum TypeOptions {
		RANDOM, PRESET, LAST, SPECIFIED
	};

	TypeOptions mGameType;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		com.mehtank.androminion.server.Strings.context = getActivity().getApplicationContext();

		mView = inflater.inflate(R.layout.fragment_startgame, null);
		
		mCardsetSpinner = (Spinner) mView.findViewById(R.id.spinnerCardset);
		mCardsetSpinner.setOnItemSelectedListener(this);
		mRandomSpinner = (Spinner) mView.findViewById(R.id.spinnerRandom);
		mPresetSpinner = (Spinner) mView.findViewById(R.id.spinnerPreset);
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
			type = com.mehtank.androminion.server.Strings.getGameTypeName(types[i]);
			if (types[i].name().startsWith("Random")) {
				randoms.add(type);
			} else {
				presets.add(type);
			}
		}

		ArrayAdapter<String> adapter = createArrayAdapter(randoms);
		mRandomSpinner.setAdapter(adapter);
		mRandomSpinner.setSelection(adapter.getPosition(mPrefs.getString("randomPref", "Random")));

		adapter = createArrayAdapter(presets);
		mPresetSpinner.setAdapter(adapter);
		mPresetSpinner.setSelection(adapter.getPosition(mPrefs.getString("presetPref", "First Game (Base)")));

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
			.setText(" - " + player + "1:  ");

		((TextView) mView.findViewById(R.id.txtPlayer2))
		.setText(" - " + player + "2:  ");
		mPlayer2.setPrompt(player+"2");
		mPlayer2.setAdapter(adapter);
		mPlayer2.setSelection(adapter.getPosition(mPrefs.getString("gamePref2", getString(R.string.none_game_start))));

		players = new ArrayList<String>(players);
		players.add(getString(R.string.none_game_start));
		adapter = createArrayAdapter(players);

		((TextView) mView.findViewById(R.id.txtPlayer3))
		.setText(" - " + player + "3:  ");
		mPlayer3.setPrompt(player+"3");
		mPlayer3.setAdapter(adapter);
		mPlayer3.setSelection(adapter.getPosition(mPrefs.getString("gamePref3", getString(R.string.none_game_start))));

		((TextView) mView.findViewById(R.id.txtPlayer4))
		.setText(" - " + player + "4:  ");
		mPlayer4.setPrompt(player+"4");
		mPlayer4.setAdapter(adapter);
		mPlayer4.setSelection(adapter.getPosition(mPrefs.getString("gamePref4", getString(R.string.none_game_start))));
		
		((TextView) mView.findViewById(R.id.txtPlayer5))
		.setText(" - " + player + "5:  ");
		mPlayer5.setPrompt(player+"5");
		mPlayer5.setAdapter(adapter);
		mPlayer5.setSelection(adapter.getPosition(mPrefs.getString("gamePref5", getString(R.string.none_game_start))));
		
		((TextView) mView.findViewById(R.id.txtPlayer6))
		.setText(" - " + player + "6:  ");
		mPlayer6.setPrompt(player+"6");
		mPlayer6.setAdapter(adapter);
		mPlayer6.setSelection(adapter.getPosition(mPrefs.getString("gamePref6", getString(R.string.none_game_start))));

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

	private ArrayAdapter<String> createArrayAdapter(ArrayList<String> list) {
	    ArrayAdapter<String> adapter = new ArrayAdapter<String>
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

            spinnerStr = (String) mRandomSpinner.getSelectedItem();
			g = com.mehtank.androminion.server.Strings.getGameTypefromName(spinnerStr);
			if (g != null)
				strs.add(g.getName());
			edit.putString("randomPref", spinnerStr);
			break;
		case PRESET:
			edit.putString("gameType", TypeOptions.PRESET.name());

            spinnerStr = (String) mPresetSpinner.getSelectedItem();
			g = com.mehtank.androminion.server.Strings.getGameTypefromName(spinnerStr);
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

        if(mPrefs.getBoolean("quick_play", false)) {
            strs.add("-quickplay");
        }

        if (mPrefs.getBoolean("action_chains", false)) {
            strs.add("-actionchains");
        }

        if (mPrefs.getBoolean("suppress_redundant_reactions", true)) {
            strs.add("-suppressredundantreactions");
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
			mRandomSpinner.setVisibility(View.VISIBLE);
			mPresetSpinner.setVisibility(View.GONE);
		} else if (parent.getItemAtPosition(pos) == getResources().getString(R.string.game_type_preset)){
			mGameType = TypeOptions.PRESET;
			mRandomSpinner.setVisibility(View.GONE);
			mPresetSpinner.setVisibility(View.VISIBLE);
		} else if (parent.getItemAtPosition(pos) == getResources().getString(R.string.game_type_last)){
			mGameType = TypeOptions.LAST;
			mPresetSpinner.setVisibility(View.GONE);
			mRandomSpinner.setVisibility(View.GONE);
		} else if (parent.getItemAtPosition(pos) == getResources().getString(R.string.game_type_specified)){
			mGameType = TypeOptions.SPECIFIED;
			mPresetSpinner.setVisibility(View.GONE);
			mRandomSpinner.setVisibility(View.GONE);
		}
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
