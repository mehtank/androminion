package com.mehtank.androminion.util;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.Toast;

import com.mehtank.androminion.Androminion;
import com.mehtank.androminion.R;

public class Achievements {
    public static final String[] KEYS = {
        "2players8provinces",
        "3or4players10provinces",
        "score100",
        "score1more",
        "singlecard",
        "score50more",
        "skunk",
        "trash5inaturn",
        "gainmorethan30inaturn",
        "win10inarow",
        };
    private static final int[] ids = {
    	R.string.achievements_2players8provinces,
    	R.string.achievements_3or4players10provinces,
    	R.string.achievements_score100,
    	R.string.achievements_score1more,
    	R.string.achievements_singlecard,
    	R.string.achievements_score50more,
    	R.string.achievements_skunk,
    	R.string.achievements_trash5inaturn,
    	R.string.achievements_gainmorethan30inaturn,
    	R.string.achievements_win10inarow
    };
    public String[] text = new String[KEYS.length];
    boolean[] achievementsDone = new boolean[KEYS.length];

    public static final String WIN_STREAK_PLAYER_KEY = "win_streak_player";
    public static final String WIN_STREAK_COUNT_KEY = "win_streak_count";

    SharedPreferences prefs;
    Context context;

    public Achievements(Context context) {
        this.context = context;
        this.prefs = PreferenceManager.getDefaultSharedPreferences(context);

        for(int i=0; i < Achievements.KEYS.length; i++) {
        	text[i] = context.getString(ids[i]);
            achievementsDone[i] = hasAchieved(i);
        }
    }

    public void resetStats() {
        ArrayList<String> prefsToClear = new ArrayList<String>();

        Map<String, ?> all = prefs.getAll();
        Iterator<String> keys = all.keySet().iterator();
        while(keys.hasNext()) {
            String key = keys.next();
            if(key.startsWith("wins_") || key.startsWith("losses_")) {
                prefsToClear.add(key);
            }
            if(key.startsWith("p2wins_") || key.startsWith("p2losses_")) {
                prefsToClear.add(key);
            }
            if(key.startsWith("p3wins_") || key.startsWith("p3losses_")) {
                prefsToClear.add(key);
            }
            if(key.startsWith("p4wins_") || key.startsWith("p4losses_")) {
                prefsToClear.add(key);
            }
        }

        if(prefsToClear.size() > 0) {
            Editor editor = prefs.edit();
            editor.remove(WIN_STREAK_COUNT_KEY);
            editor.remove(WIN_STREAK_PLAYER_KEY);
            for(String pref : prefsToClear)
                editor.remove(pref);
            editor.commit();
        }
    }

    public void resetAchievements() {
    	Editor editor = prefs.edit();
        for(int i=0; i < Achievements.KEYS.length; i++) {
        	editor.remove(Achievements.KEYS[i]);
            achievementsDone[i] = false;
        }
        editor.commit();
    }

    private int achievementIndex(String achievement) {
        int index = -1;
        for(int i=0; i < KEYS.length; i++) {
            if(KEYS[i].equals(achievement)) {
                index = i;
                break;
            }
        }

        return index;
    }

    public boolean hasAchieved(String achievement) {
    	int index = achievementIndex(achievement);
        if(index == -1) {
            Log.d("Androminion","ERROR: Requested Achievement not found:" + achievement);
            return false;
        }
    	return hasAchieved(index);
    }
    public boolean hasAchieved(int index) {
        if(index < 0 || index >= KEYS.length) {
            Log.d("Androminion","ERROR: Requested Achievement not found:" + index);
            return false;
        }
        return prefs.getBoolean(KEYS[index], false);
    }


    public void achieved(String achievement) {
        int index = achievementIndex(achievement);
        if(index == -1) {
            Log.d("Androminion","ERROR: Acquired Achievement not found:" + achievement);
            return;
        }
        if(!achievementsDone[index]) {
            achievementsDone[index] = true;
            Editor editor = prefs.edit();
            editor.putBoolean(KEYS[index], true);
            editor.commit();
            if (!Androminion.NOTOASTS) Toast.makeText(context, context.getString(R.string.achievements_menu)+ "!!!\n" + text[index], Toast.LENGTH_SHORT).show();
        }
    }

    public void gameOver(ArrayList<String> allPlayers, ArrayList<Integer> winners) {
        String winStreakPlayer = prefs.getString(WIN_STREAK_PLAYER_KEY, "");
        int winStreakCount = prefs.getInt(WIN_STREAK_COUNT_KEY, 0);

        String numPlayersPrefix = "p" + allPlayers.size();
        for(int i=0; i < allPlayers.size(); i++) {
            String name = getSafeName(allPlayers.get(i));
            int totalWins = prefs.getInt("wins_" + name, 0);
            int totalLosses = prefs.getInt("losses_" + name, 0);
            int numPlayersWins = prefs.getInt(numPlayersPrefix + "wins_" + name, 0);
            int numPlayersLosses = prefs.getInt(numPlayersPrefix + "losses_" + name, 0);
            boolean won = false;
            for(int j=0; j < winners.size(); j++) {
                if(winners.get(j).intValue() == i) {
                    won = true;
                    totalWins++;
                    numPlayersWins++;
                    break;
                }
            }
            if(!won) {
                if(allPlayers.get(i).equals(winStreakPlayer)) {
                    winStreakPlayer = "";
                    winStreakCount = 0;
                    Editor editor = prefs.edit();
                    editor.putString(WIN_STREAK_PLAYER_KEY, winStreakPlayer);
                    editor.putInt(WIN_STREAK_COUNT_KEY, winStreakCount);
                    editor.commit();
                }
                totalLosses++;
                numPlayersLosses++;
            }

            Editor editor = prefs.edit();
            editor.putInt("wins_" + name, totalWins);
            editor.putInt("losses_" + name, totalLosses);
            editor.putInt(numPlayersPrefix + "wins_" + name, numPlayersWins);
            editor.putInt(numPlayersPrefix + "losses_" + name, numPlayersLosses);
            editor.commit();
        }

        for(int j=0; j < winners.size(); j++) {
            String player = allPlayers.get(winners.get(j).intValue());
            if(isHumanPlayer(player)) {
                if(!player.equals(winStreakPlayer)) {
                    winStreakPlayer = player;
                    winStreakCount = 0;
                }
                winStreakCount++;

                Editor editor = prefs.edit();
                editor.putString(WIN_STREAK_PLAYER_KEY, winStreakPlayer);
                editor.putInt(WIN_STREAK_COUNT_KEY, winStreakCount);
                editor.commit();
                break;
            }
        }

        if(winStreakCount >= 10) {
            achieved("win10inarow");
        }
    }

    public ArrayList<String> getAllPlayers() {
        ArrayList<String> players = new ArrayList<String>();

        Map<String, ?> all = prefs.getAll();
        Iterator<String> keys = all.keySet().iterator();
        while(keys.hasNext()) {
            String key = keys.next();
            if(key.startsWith("wins_")) {
                String name = key.substring("wins_".length());
                if(!players.contains(name)) {
                    players.add(name);
                }
            }
            if(key.startsWith("losses_")) {
                String name = key.substring("losses_".length());
                if(!players.contains(name)) {
                    players.add(name);
                }
            }
        }

        Collections.sort(players, Collator.getInstance());
        return players;
    }

    public int getTotalWins(String player) {
        return prefs.getInt("wins_" + player, 0);
    }

    public int getPlayerWins(String player, int numPlayers) {
        String numPlayersPrefix = "p" + numPlayers;
        return prefs.getInt(numPlayersPrefix + "wins_" + player, 0);
    }

    public int getTotalLosses(String player) {
        return prefs.getInt("losses_" + player, 0);
    }

    public int getPlayerLosses(String player, int numPlayers) {
        String numPlayersPrefix = "p" + numPlayers;
        return prefs.getInt(numPlayersPrefix + "losses_" + player, 0);
    }

    public int getWinStreak(String player) {
    	if (prefs.getString(WIN_STREAK_PLAYER_KEY, "").equals(player))
    		return prefs.getInt(WIN_STREAK_COUNT_KEY, 0);
    	return 0;
    }


    public static String getSafeName(String name) {
        if(name == null)
            name = "";

        StringBuilder sb = new StringBuilder();
        for(char c : name.toCharArray()) {
            if(Character.isLetterOrDigit(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static boolean isHumanPlayer(String s) {
        if(!s.equals("Mary") && !s.equals("Sarah") && !s.equals("Earl") && !s.equals("Drew") && !s.equals("Chuck")) {
            return true;
        }
        return false;
    }

    public AchievementsAdapter getNewAchievementsAdapter() {
    	return new AchievementsAdapter();
    }

    public class AchievementsAdapter extends ArrayAdapter<String> {
    	private final static int ROWLAYOUT = android.R.layout.simple_list_item_checked;
    	public AchievementsAdapter() {
    		super(context, ROWLAYOUT, text);
    	}

    	@Override
    	public View getView(int position, View convertView, ViewGroup parent) {
    		View rowView = convertView;
    		if (rowView == null) {
    			LayoutInflater inflater = LayoutInflater.from(context);
    			rowView = inflater.inflate(ROWLAYOUT, null);
    			CheckedTextView cbx = (CheckedTextView) rowView.findViewById(android.R.id.text1);
    			rowView.setTag(cbx);
    		}

    		CheckedTextView cbx = (CheckedTextView) rowView.getTag();
    		cbx.setText(text[position]);
    		cbx.setChecked(achievementsDone[position]);
    		return rowView;
    	}
    }
}
