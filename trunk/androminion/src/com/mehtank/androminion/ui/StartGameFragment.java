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
import android.widget.TextView;

public class StartGameFragment extends Fragment implements OnCheckedChangeListener, OnClickListener {
	
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
	Spinner mPlayer2;
	Spinner mPlayer3;
	Spinner mPlayer4;
	
	Button mStartGame;
	
	//Options & Co
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
        mPlayer2 = (Spinner) mView.findViewById(R.id.spPlayer2);
        mPlayer3 = (Spinner) mView.findViewById(R.id.spPlayer3);
        mPlayer4 = (Spinner) mView.findViewById(R.id.spPlayer4);
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
			mGameType = TypeOptions.valueOf(mPrefs.getString("gameType", TypeOptions.PRESET.name()));
		} else {
			mCardsPassOnStartup = getArguments().getStringArray("cards");
			mGameType = TypeOptions.SPECIFIED;
		}
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
		
		ArrayAdapter<String> adapter = createArrayAdapter(randoms);
		mRandomSpinner.setAdapter(adapter);
		mRandomSpinner.setSelection(adapter.getPosition(mPrefs.getString("randomPref", "Random")));

		adapter = createArrayAdapter(presets);
		mPresetSpinner.setAdapter(adapter);
		mPresetSpinner.setSelection(adapter.getPosition(mPrefs.getString("presetPref", "First Game (Base)")));
		
		//Fill player spinners
		ArrayList<String> players = new ArrayList<String>(PLAYERTYPES.length + 1);
		for(String s: PLAYERTYPES) {
			players.add(s);
		}
		if(!mMultiplayer){
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
		mPlayer4.setPrompt(player+2);
		mPlayer4.setAdapter(adapter);
		mPlayer4.setSelection(adapter.getPosition(mPrefs.getString("gamePref4", getString(R.string.none_game_start))));

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
  		edit.commit();
  		
  		try {
  			((OnStartGameListener) getActivity()).onStartGameClick(strs);
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + " must implement OnStartGameListener");
        }
	}
	
	// Container Activity must implement this interface
	public interface OnStartGameListener {
		public void onStartGameClick(ArrayList<String> values);
	}
}