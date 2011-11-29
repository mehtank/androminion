package com.vdom.core;

import java.util.ArrayList;

import com.vdom.api.GameType;
import com.vdom.api.InteractivePlayer;

public class Play {
    String[][] playerNamesAndClasses = new String[][]{
        { "Sarah", "com.vdom.players.VDomPlayerSarah" },   
        { "Mary", "com.vdom.players.VDomPlayerMary" },   
        { "Earl", "com.vdom.players.VDomPlayerEarl" },   
        { "Drew", "com.vdom.players.VDomPlayerDrew" },   
        { "Chuck", "com.vdom.players.VDomPlayerChuck" },   
        { HUMAN, "com.vdom.api.InteractivePlayer" },   
        { HUMAN + " (Quick Play)", "com.vdom.api.InteractivePlayer" + Game.QUICK_PLAY },   
    };
    
    public static final String HUMAN = "Human";
    
    public static void main(String[] args) {
        new Play().go();
    }
    
    public void go() {
        
        ArrayList<String> args = new ArrayList<String>();

        args.add("-type" + pickGameType());
        
        int players = numPlayers();
        for(int i=0; i < players; i++) {
            int index = getPlayerIndex("Choose player " + (i + 1));
            String c = playerNamesAndClasses[index][1];
            if(playerNamesAndClasses[index][0].startsWith(HUMAN)) {
                String name = getName();
                if(name != null && !name.trim().equals("")) {
                    c += "*" + name.replace(" ", "_");
                }
            }
            args.add(c);
        }
        
        Game.main(args.toArray(new String[0]));
    }
    
    protected String pickGameType() {
        int num = -1;
        String gameType = null;
        GameType[] ta = GameType.values();
        while (num == -1) {
            Util.log("~~VDom~~ (http://code.google.com/p/vdom/)\nAn unofficial implementation of Dominion, \na game created by Donald X. Vaccarino\nand published by Rio Grande Games.\n");
            Util.hitEnter(null);
            
            Util.log("");
            
            for (int i = 0; i < ta.length; i++) {
                Util.log("" + (i + 1) + "-" + ta[i].getName());
            }

            Util.log("");
            Util.log("Select game type");
            
            num = InteractivePlayer.getInputAsInt(null, 1, ta.length);

            if (num > 0) {
                gameType = ta[num - 1].name();
            }
        }

        return gameType;
    }
    
    protected int numPlayers() {
        int num = -1;
        while (num == -1) {
            Util.log("Number of players (2-4)");
            num = InteractivePlayer.getInputAsInt(null, 2, 4);
        }

        return num;
    }

    protected int getPlayerIndex(String prompt) {
        int num = -1;
        while (num == -1) {
            for (int i = 0; i < playerNamesAndClasses.length; i++) {
                Util.log("" + (i + 1) + "-" + playerNamesAndClasses[i][0]);
            }

            num = InteractivePlayer.getInputAsInt(null, 1, playerNamesAndClasses.length);

            if (num > 0) {
                return num - 1;
            }
        }

        return -1;
    }

    protected String getName() {
        Util.log("Enter player name (return for none)");
        System.out.print(">");
        return Util.readString();
    }
}
