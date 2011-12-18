package com.mehtank.androminion.ui;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.mehtank.androminion.Androminion;
import com.mehtank.androminion.R;

public class Achievements {
    public static String[] keys = new String[]{ 
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
    public static String[] text = new String[]{ 
        "Have all 8 Provinces at the end of a 2 player game", 
        "Have 10 or more Provinces at the end of a 3 or 4 player game", 
        "Win a game with 100 or more VP", 
        "Win a game by exactly 1 VP", 
        "Win a game buying no more than one unique Kingdom Card",
        "Win a game by more than 50 VP",
        "Win a game with at least one opponent having 0 or less VP",
        "Trash more than 5 cards in a single turn",
        "Gain more than 30 VP in a single turn",
        "Win 10 games in a row",
     };
    boolean[] achievementsDone = new boolean[keys.length];
    
    public static final String WIN_STREAK_PLAYER_KEY = "win_streak_player";
    public static final String WIN_STREAK_COUNT_KEY = "win_streak_count";
    
    SharedPreferences prefs;
    Androminion top;
    
    public Achievements(Androminion top) {
        this.top = top;
        this.prefs = PreferenceManager.getDefaultSharedPreferences(top);

        for(int i=0; i < Achievements.keys.length; i++) {
            achievementsDone[i] = hasAchieved(Achievements.keys[i]);
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

    private int achievementIndex(String achievement) {
        int index = -1;
        for(int i=0; i < keys.length; i++) {
            if(keys[i].equals(achievement)) {
                index = i;
                break;
            }
        }

        return index;
    }
    
    public boolean hasAchieved(String achievement) {
        int index = achievementIndex(achievement);
        if(index == -1) {
            System.out.println("ERROR: Requested Achievement not found:" + achievement);
            return false;
        }
        
        return prefs.getBoolean(keys[index], false);
    }
    
    public void achieved(String achievement) {
        int index = achievementIndex(achievement);
        if(index == -1) {
            System.out.println("ERROR: Acquired Achievement not found:" + achievement);
            return;
        }
        if(!achievementsDone[index]) {
            achievementsDone[index] = true;
            Editor editor = prefs.edit();
            editor.putBoolean(keys[index], true);              
            editor.commit();
            if (!Androminion.NOTOASTS) Toast.makeText(top, top.getString(R.string.achievements_menu)+ "!!!\n" + text[index], Toast.LENGTH_SHORT).show();
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
}
