package com.vdom.core;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.vdom.api.ActionCard;
import com.vdom.api.Card;
import com.vdom.api.CardCostComparator;
import com.vdom.api.CurseCard;
import com.vdom.api.DurationCard;
import com.vdom.api.FrameworkEvent;
import com.vdom.api.FrameworkEventHelper;
import com.vdom.api.GameEvent;
import com.vdom.api.GameEventListener;
import com.vdom.api.GameType;
import com.vdom.api.TreasureCard;
import com.vdom.api.VictoryCard;
import com.vdom.core.Cards;
import com.vdom.core.Player.WatchTowerOption;

public class Game {
    public static Integer cardSequence = 1;
    public static HashMap<String, Double> GAME_TYPE_WINS = new HashMap<String, Double>();
    
    public static HashMap<String, Integer> winStats = new HashMap<String, Integer>();
    public static final String QUICK_PLAY = "(QuickPlay)";
    public static final String BANE = "bane+";
    
    public static String[] cardsSpecifiedAtLaunch;
    public static ArrayList<String> unfoundCards = new ArrayList<String>();
    String cardListText = "";
    String unfoundCardText = "";
    int totalCardCountGameBegin = 0;
    int totalCardCountGameEnd = 0;
    
    /**
     * Player classes and optionally the url to the jar on the web that the class is located in. Player class
     * is in index [0] of the array and the jar is in index [1]. Index [1] is null (but still there) if the
     * default class loader should be used.
     */
    static ArrayList<String[]> playerClassesAndJars = new ArrayList<String[]>();

    /**
     * The card set to use for the game.
     * 
     * @see com.vdom.api.GameType
     */
    public static GameType gameType = GameType.Random;
    public static String gameTypeStr = null;
    public static boolean showUsage = false;

    public static boolean alwaysIncludePlatColony = false; 
    public static boolean platColonyPassedIn = false; 
    public static boolean platColonyNotPassedIn = false;
    
    public static boolean quickPlay = false;
    
    public static boolean debug = false;
    public static final HashSet<GameEvent.Type> showEvents = new HashSet<GameEvent.Type>();
    public static final HashSet<String> showPlayers = new HashSet<String>();
    static boolean test = false;
    static boolean ignoreAllPlayerErrors = false;
    static boolean ignoreSomePlayerErrors = false;
    static HashSet<String> ignoreList = new HashSet<String>();

    static ArrayList<GameStats> gameTypeStats = new ArrayList<GameStats>();

    static int numGames = -1;
    public ArrayList<GameEventListener> listeners = new ArrayList<GameEventListener>();
    public GameEventListener gameListener;
    static boolean forceDownload = false;
    static HashMap<String, Double> overallWins = new HashMap<String, Double>();

    public static Random rand = new Random(System.currentTimeMillis());
    public HashMap<String, CardPile> piles = new HashMap<String, CardPile>();
    public HashMap<String, Integer> embargos = new HashMap<String, Integer>();
    public ArrayList<Card> trashPile = new ArrayList<Card>();
    public Card baneCard = null;
    double chanceForPlatColony = 0;

    private static final int kingdomCardPileSize = 10;
    private static int victoryCardPileSize = 12;

    ArrayList<Card>[] cardsObtainedLastTurn;
    static int playersTurn;

//    public UI ui;
    int turnCount = 0;
    int consecutiveTurns = 0;

    public static HashMap<String, Player> cachedPlayers = new HashMap<String, Player>();
    public static HashMap<String, Class<?>> cachedPlayerClasses = new HashMap<String, Class<?>>();
    public static Player[] players;

    public boolean platInPlay = false;
    public boolean colonyInPlay = false;

    public int possessionsToProcess = 0;
    public Player possessingPlayer = null;
    
    static int numPlayers;
    boolean gameOver = false;

    private static HashMap<String, Player> playerCache = new HashMap<String, Player>();


    public static void main(String[] args) {
        go(args, false);
    }

    public static void go(String[] args, boolean html) {

        try {
            processArgs(args);

            checkForInteractive();

            Util.debug("");

            // Start game(s)
            if (gameTypeStr != null) {
                gameType = GameType.fromName(gameTypeStr);
                new Game().start();
            } else {
                for (String[] className : playerClassesAndJars) {
                    overallWins.put(className[0], 0.0);
                }

                for (GameType p : GameType.values()) {
                    gameType = p;
                    new Game().start();
                }

                if (test) {
                    for (int i = 0; i < 5; i++) {
                        gameType = GameType.Random;
                        new Game().start();
                    }
                    for (int i = 0; i < 5; i++) {
                        gameType = GameType.RandomBaseGame;
                        new Game().start();
                    }
                    for (int i = 0; i < 5; i++) {
                        gameType = GameType.RandomIntrigue;
                        new Game().start();
                    }
                    for (int i = 0; i < 5; i++) {
                        gameType = GameType.RandomSeaside;
                        new Game().start();
                    }
                }
                if (!debug && !test) {
                    Util.log("----------------------------------------------------");
                }
                printStats(overallWins, numGames * GameType.values().length, "Total");
                printStats(GAME_TYPE_WINS, GameType.values().length, "Types");
            }
            if (test) {
                printGameTypeStats();
            }
        } catch (ExitException e) {
            // Ignore...
        }

        FrameworkEvent frameworkEvent = new FrameworkEvent(FrameworkEvent.Type.AllDone);
        FrameworkEventHelper.broadcastEvent(frameworkEvent);
    }

    void start() throws ExitException {
        HashMap<String, Double> gameTypeSpecificWins = new HashMap<String, Double>();

        for (String[] className : playerClassesAndJars) {
            gameTypeSpecificWins.put(className[0], 0.0);
            if (!GAME_TYPE_WINS.containsKey(className[0])) {
                GAME_TYPE_WINS.put(className[0], 0.0);
            }
        }

        long turnCountTotal = 0;
        long vpTotal = 0;
        long numCardsTotal = 0;

        // if (test) {
        // System.out.print(gameType + " ");
        // }
        for (int gameCount = 0; gameCount < numGames; gameCount++) {
            // if (test) {
            // System.out.print(gameCount + " ");
            // System.out.flush();
            // }
            Util.debug("---------------------", false);

            Util.debug("New Game:" + gameType);
            initGameBoard();
            if (test) {
                Util.log(gameType.toString());
                totalCardCountGameBegin = totalCardCount();
            }

            gameOver = false;
            playersTurn = 0;
            turnCount = 1;
            Util.debug("Turn " + turnCount);

            consecutiveTurns = 0;
            while (!gameOver) {
                Player player = players[playersTurn];
                MoveContext context = new MoveContext(this, player);

                turnBegin(player, context);

                // /////////////////////////////////
                // Actions
                // /////////////////////////////////
                playerAction(player, context);

                
                // /////////////////////////////////
                // Select Treasure for Buy
                // /////////////////////////////////
                playTreasures(player, context);

                // /////////////////////////////////
                // Buy Phase
                // /////////////////////////////////
                playerBuy(player, context);
                if (context.totalCardsBoughtThisTurn == 0) {
                    GameEvent event = new GameEvent(GameEvent.Type.NoBuy, context);
                    broadcastEvent(event);
                    Util.debug(player.getPlayerName() + " did not buy a card with coins:" + context.getCoinAvailableForBuy());
                }

                // /////////////////////////////////
                // Discard phase
                // /////////////////////////////////
                player.cleanup(context);

                // /////////////////////////////////
                // Draw new hand
                // /////////////////////////////////
                boolean takeAnotherTurn = playerEndTurn(player, context);

                gameOver = checkGameOver();
                
                if (!gameOver) {
                    if (!takeAnotherTurn) {

                        if(possessionsToProcess > 0) {
                            if(--possessionsToProcess == 0) {
                                possessingPlayer = null;
                            }
                        }
                        else if(possessionsToProcess == 0) {
                            playersTurn++;
                            possessionsToProcess = context.possessionsToProcess;
                            if(possessionsToProcess > 0) {
                                possessingPlayer = player;
                            }
                        }
                        
                        consecutiveTurns = 0;
                        if (playersTurn >= numPlayers) {
                            playersTurn = 0;
                            Util.debug("Turn " + ++turnCount, true);
                        }
                    }
                }
            }

            int vps[] = gameOver(gameTypeSpecificWins);
            if (test) {
                // Compute game stats
                turnCountTotal += turnCount;
                for (int i = 0; i < vps.length; i++) {
                    vpTotal += vps[i];
                    numCardsTotal += players[i].getAllCards().size();
                }
                totalCardCountGameEnd = totalCardCount();
                // assert (totalCardCountGameBegin == totalCardCountGameEnd);
            }

        }

        // Java program ending
        if (!debug) {
            markWinner(gameTypeSpecificWins);
            printStats(gameTypeSpecificWins, numGames, gameType.toString());

            // Util.log("---------------------");
        }

        if (test) {
            // System.out.println();
            ArrayList<Card> gameCards = new ArrayList<Card>();
            for (CardPile pile : piles.values()) {
                Card card = pile.card;
                if (!card.equals(Cards.copper) && !card.equals(Cards.silver) && !card.equals(Cards.gold) && !card.equals(Cards.platinum)
                    && !card.equals(Cards.estate) && !card.equals(Cards.duchy) && !card.equals(Cards.province) && !card.equals(Cards.colony)
                    && !card.equals(Cards.curse)) {
                    gameCards.add(card);
                }
            }

            GameStats stats = new GameStats();
            stats.gameType = gameType;
            stats.cards = gameCards.toArray(new Card[0]);
            stats.aveTurns = (int) (turnCountTotal / numGames);
            stats.aveNumCards = (int) (numCardsTotal / (numGames * numPlayers));
            stats.aveVictoryPoints = (int) (vpTotal / (numGames * numPlayers));

            gameTypeStats.add(stats);
        }

        FrameworkEvent frameworkEvent = new FrameworkEvent(FrameworkEvent.Type.GameTypeOver);
        frameworkEvent.setGameType(gameType);
        frameworkEvent.setGameTypeWins(gameTypeSpecificWins);
        FrameworkEventHelper.broadcastEvent(frameworkEvent);
    }

    protected void playTreasures(Player player, MoveContext context) {
        // Set the turn gold to the correct amount
        context.gold = context.addGold;
        context.addGold = 0;
        context.potions = 0;
        context.buyPhase = true;

        boolean selectingCoins = playerShouldSelectCoinsToPlay(context, player.getHand());
        ArrayList<TreasureCard> treasures = null;
        treasures = (selectingCoins) ? player.treasureCardsToPlayInOrder(context) : player.getTreasuresInHand();

        while (treasures != null && !treasures.isEmpty()) {
            while (!treasures.isEmpty()) {
                TreasureCard card = treasures.remove(0);
                card.playTreasure(context);
                }
            treasures = (selectingCoins) ? player.treasureCardsToPlayInOrder(context) : player.getTreasuresInHand();
        }
    }

    private void markWinner(HashMap<String, Double> gameTypeSpecificWins) {
        double highWins = 0;
        int winners = 0;

        for (String player : gameTypeSpecificWins.keySet()) {
            if (gameTypeSpecificWins.get(player) > highWins) {
                highWins = gameTypeSpecificWins.get(player);
            }
        }

        for (String player : gameTypeSpecificWins.keySet()) {
            if (gameTypeSpecificWins.get(player) == highWins) {
                winners++;
            }
        }

        double points = 1.0 / winners;
        for (String player : gameTypeSpecificWins.keySet()) {
            if (gameTypeSpecificWins.get(player) == highWins) {
                double playerWins = 0;
                if (GAME_TYPE_WINS.containsKey(player)) {
                    playerWins = GAME_TYPE_WINS.get(player);
                }
                playerWins += points;

                GAME_TYPE_WINS.put(player, playerWins);
            }
        }

        GAME_TYPE_WINS.toString();
    }

    protected int[] gameOver(HashMap<String, Double> gameTypeSpecificWins) {
        if (debug)
            printPlayerTurn();

        int[] vps = calculateVps();

        for (int i = 0; i < numPlayers; i++) {
            int tieCount = 0;
            boolean loss = false;
            for (int j = 0; j < numPlayers; j++) {
                if (i == j) {
                    continue;
                }
                if (vps[i] < vps[j]) {
                    loss = true;
                    break;
                }
                if (vps[i] == vps[j]) {
                    tieCount++;
                }
            }

            if (!loss) {
                double num = gameTypeSpecificWins.get(players[i].getClass().getName());
                Double overall = overallWins.get(players[i].getClass().getName());
                boolean trackOverall = (overall != null);
                if (tieCount == 0) {
                    num += 1.0;
                    if (trackOverall) {
                        overall += 1.0;
                    }
                } else {
                    num += 1.0 / (tieCount + 1);
                    if (trackOverall) {
                        overall += 1.0 / (tieCount + 1);
                    }
                }
                gameTypeSpecificWins.put(players[i].getClass().getName(), num);
                if (trackOverall) {
                    overallWins.put(players[i].getClass().getName(), overall);
                }
            }

            Player player = players[i];
            player.vps = vps[i];
            player.win = !loss;
            MoveContext context = new MoveContext(this, player);
            broadcastEvent(new GameEvent(GameEvent.Type.GameOver, context));

        }
        int index = 0;
        for (Player player : players) {
            int vp = vps[index++];
            Util.debug(player.getPlayerName() + ":Victory Points=" + vp, true);
            GameEvent event = new GameEvent(GameEvent.Type.VictoryPoints, null);
            event.setPlayer(player);
            event.setComment(":" + vp);
            broadcastEvent(event);
        }
        return vps;

    }

    protected void printPlayerTurn() {
            for (Player player : players) {
                Util.debug("", true);
                ArrayList<Card> allCards = player.getAllCards();
                StringBuilder msg = new StringBuilder();
                msg.append(" " + allCards.size() + " Cards: ");

                final HashMap<String, Integer> cardCounts = new HashMap<String, Integer>();
                for (Card card : allCards) {
                    String key = card.getName() + " -> " + card.getDescription();
                    Integer count = cardCounts.get(key);
                    if (count == null) {
                        cardCounts.put(key, 1);
                    } else {
                        cardCounts.put(key, count + 1);
                    }
                }

                ArrayList<Card> removeDuplicates = new ArrayList<Card>();
                for (Card card : allCards) {
                    if (!removeDuplicates.contains(card)) {
                        removeDuplicates.add(card);
                    }
                }
                allCards = removeDuplicates;

                Collections.sort(allCards, new Comparator<Card>() {
                    public int compare(Card o1, Card o2) {
                        String keyOne = o1.getName() + " -> " + o1.getDescription();
                        String keyTwo = o2.getName() + " -> " + o2.getDescription();
                        return cardCounts.get(keyTwo) - cardCounts.get(keyOne);
                    }
                });

                boolean first = true;
                for (Card card : allCards) {
                    String key = card.getName() + " -> " + card.getDescription();
                    if (first) {
                        first = false;
                    } else {
                        msg.append(", ");
                    }
                    msg.append("" + cardCounts.get(key) + " " + card.getName());
                }

                Util.debug(player.getPlayerName() + ":" + msg, true);
            }
            Util.debug("", true);
        }

    protected boolean playerEndTurn(Player player, MoveContext context) {
        int handCount = 5;

        boolean takeAnotherTurn = false;
        // Can only have at most two consecutive turns
        for (Card card : player.nextTurnCards) {
            if ((card instanceof DurationCard) && ((DurationCard) card).takeAnotherTurn()) {
                handCount = ((DurationCard) card).takeAnotherTurnCardCount();
                if (consecutiveTurns == 1) {
                    takeAnotherTurn = true;
                    break;
                }
            }
        }

        for (int i = 0; i < handCount; i++) {
            drawToHand(player, null, false);
        }

        // /////////////////////////////////
        // Reset context for status update
        // /////////////////////////////////
        context.actionsPlayedSoFar = 0;
        context.actions = 1;
        context.buys = 1;
        context.addGold = 0;
        context.coppersmithsPlayed = 0;
        context.gold = context.getCoinAvailableForBuy();

        GameEvent event = new GameEvent(GameEvent.Type.NewHand, context);
        broadcastEvent(event);
        event = null;

        // /////////////////////////////////
        // Turn End
        // /////////////////////////////////
        event = new GameEvent(GameEvent.Type.TurnEnd, context);
        broadcastEvent(event);
        return takeAnotherTurn;
    }

    protected void playerAction(Player player, MoveContext context) {
        // TODO move this check to action and buy (and others?)
        // if(player.hand.size() > 0)
        Card action = null;
        do {
            action = player.doAction(context);

            if (isValidAction(context, action)) {
                if (action != null) {
                    GameEvent event = new GameEvent(GameEvent.Type.Status, (MoveContext) context);
                    broadcastEvent(event);
                    
                    try {
                        ((ActionCardImpl) action).play(this, (MoveContext) context, true);
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                Util.debug("Error:Invalid action selected");
                action = null;
            }
        } while (context.actions > 0 && action != null);
    }

    protected void playerBuy(Player player, MoveContext context) {
        Card buy = null;
        do {
            try {
                buy = player.doBuy(context);
            } catch (Throwable t) {
                Util.playerError(player, t);
            }

            if (buy != null) {
                if (isValidBuy(context, buy)) {
                	context.totalCardsBoughtThisTurn++;
                    GameEvent statusEvent = new GameEvent(GameEvent.Type.Status, (MoveContext) context);
                    broadcastEvent(statusEvent);

                    playBuy(context, buy);

                } else {
                    // TODO report?
                    buy = null;
                }
            }
        } while (context.buys > 0 && buy != null);
        context.buyPhase = false;
    }

    protected void turnBegin(Player player, MoveContext context) {
        consecutiveTurns++;
        cardsObtainedLastTurn[playersTurn].clear();

        GameEvent gevent = new GameEvent(GameEvent.Type.TurnBegin, context);
        broadcastEvent(gevent);

        for (Card card : player.nextTurnCards) {
            if (card instanceof DurationCard) {
                DurationCard thisCard = (DurationCard) card;

                for (int clone = ((CardImpl) card).cloneCount; clone > 0; clone--) {
                    GameEvent event = new GameEvent(GameEvent.Type.PlayingDurationAction, context);
                    event.card = thisCard;
                    broadcastEvent(event);

                    // context.actionsPlayedSoFar++;
                    context.actions += thisCard.getAddActionsNextTurn();
                    context.addGold += thisCard.getAddGoldNextTurn();
                    context.buys += thisCard.getAddBuysNextTurn();

                    for (int i = 0; i < thisCard.getAddCardsNextTurn(); i++) {
                        drawToHand(player, thisCard, true);
                    }

                }
            } else if (!card.equals(Cards.throneRoom) && !card.equals(Cards.kingsCourt)) {
                Util.debug(player, "Bad duration card: " + card);
            }
            ((CardImpl) card).cloneCount = 1;
        }

        while (!player.nextTurnCards.isEmpty()) {
            context.playedCards.add(player.nextTurnCards.remove(0));
        }

        while (!player.haven.isEmpty()) {
            player.hand.add(player.haven.remove(0));
        }

        while (!player.horseTraders.isEmpty()) {
            Card horseTrader = player.horseTraders.remove(0);
            player.hand.add(horseTrader);
            drawToHand(player, horseTrader);
        }

    }

    private static void printStats(HashMap<String, Double> wins, int gameCount, String gameType) {
        if (test) {
            return;
        }

        StringBuilder sb = new StringBuilder();

        String s = gameType + ":";

        String start = "" + gameCount;

        if (gameCount > 1) {
            s = start + (gameType.equals("Types") ? " types " : " games ") + s;
        }

        if (!debug) {
            while (s.length() < 30) { // (24 + start.length())) {
                s += " ";
            }
        }
        sb.append(s);
        String winner = null;
        double high = 0.0;
        Iterator<String> keyIter = wins.keySet().iterator();

        while (keyIter.hasNext()) {
            String className = keyIter.next();
            double num = wins.get(className);
            double val = Math.round((num * 100 / gameCount));
            if (val > high) {
                high = val;
                winner = className;
            }
        }

        keyIter = wins.keySet().iterator();
        while (keyIter.hasNext()) {
            String className = keyIter.next();
            double num = wins.get(className);

            String name;
            try {
                name = playerCache.get(className).getPlayerName();
            } catch (Exception e) {
                name = className;
            }

            double val = Math.round((num * 100 / gameCount));
            String numStr = "" + (int) val;
            while (numStr.length() < 3) {
                numStr += " ";
            }

            sb.append(" ");
            if (className.equals(winner)) {
                sb.append("*");
            } else {
                sb.append(" ");
            }
            sb.append(name + " %" + numStr);

            winStats.put(name, Integer.parseInt(numStr.trim()));
        }

        Util.log(sb.toString());
    }

    private static void printGameTypeStats() {
        Collections.sort(gameTypeStats, new Comparator<GameStats>() {
            public int compare(GameStats gs1, GameStats gs2) {
                return gs1.aveTurns - gs2.aveTurns;
            }
        });

        for (GameStats stats : gameTypeStats) {
            StringBuilder sb = new StringBuilder();
            sb.append(stats.gameType);
            if (stats.gameType.toString().length() < 8) {
                sb.append("\t");
            }
            if (stats.gameType.toString().length() < 16) {
                sb.append("\t");
            }
            sb.append("\tvp=" + stats.aveVictoryPoints + "\t\tcards=" + stats.aveNumCards + "\tturns=" + stats.aveTurns + "\t"
                + Util.cardArrayToString(stats.cards));
            Util.log(sb.toString());
        }
    }

    public int calculateLead(Player player) {
        int playerVictoryPoints = calculateVps(player);

        Integer otherHigh = null;

        int[] vps = calculateVps();
        for (int i = 0; i < vps.length; i++) {
            if (players[i].equals(player)) {
                continue;
            }

            if (otherHigh == null || vps[i] > otherHigh) {
                otherHigh = vps[i];
            }
        }

        return playerVictoryPoints - otherHigh;
    }

    private static int[] calculateVps() {
        int[] vps = new int[numPlayers];

        for (int i = 0; i < players.length; i++) {
            Player player = players[i];
            vps[i] = calculateVps(player);
        }

        return vps;
    }

    static int calculateVps(Player player) {
        ArrayList<Card> allCards = player.getAllCards();
        HashSet<String> distinctNames = new HashSet<String>();

        int vp = player.getVictoryTokens();
        int gardens = 0;
        int dukes = 0;
        int duchys = 0;
        int vineyards = 0;
        int fairgrounds = 0;
        int actionCards = 0;
        int silkRoads = 0;
        int totalCards = 0;
        int victoryCards = 0;
        
        for (Card card : allCards) {
            distinctNames.add(card.getName());
            totalCards++;
            if (card instanceof ActionCard) {
                actionCards++;
            }
            if (card instanceof CurseCard) {
                vp += ((CurseCard) card).getVictoryPoints();
            }
            if (card instanceof VictoryCard) {
                victoryCards++;
                vp += ((VictoryCard) card).getVictoryPoints();
                if (card.equals(Cards.duchy)) {
                    duchys++;
                } else if (card.equals(Cards.duke)) {
                    dukes++;
                } else if (card.equals(Cards.gardens)) {
                    gardens++;
                } else if (card.equals(Cards.vineyard)) {
                    vineyards++;
                } else if (card.equals(Cards.fairgrounds)) {
                    fairgrounds++;
                } else if (card.equals(Cards.silkRoad)) {
                    silkRoads++;
                }
            }
        }

        vp += gardens * (totalCards / 10);
        vp += dukes * duchys;
        vp += vineyards * (actionCards / 3);
        vp += fairgrounds * 2 * (distinctNames.size() / 5);
        vp += silkRoads * (victoryCards / 4);
        
        return vp;
    }

    protected static void processArgs(String[] args) {
        numPlayers = 0;
        cardsSpecifiedAtLaunch = null;
        overallWins.clear();
        GAME_TYPE_WINS.clear();
        gameTypeStats.clear();
        playerClassesAndJars.clear();
        playerCache.clear();

        try {
            String gameCountArg = "-count";
            String debugArg = "-debug";
            String showEventsArg = "-showevents";
            String showPlayersArg = "-showplayers";
            String gameTypeArg = "-type";
            String gameTypeStatsArg = "-test";
            String ignorePlayerErrorsArg = "-ignore";
            String siteArg = "-site=";
            String platColonyArg = "-platcolony";
            String quickPlayArg = "-quickplay";
            String cardArg = "-cards=";

            for (String arg : args) {
                if (arg == null) {
                    continue;
                }

                if (arg.startsWith("#")) {
                    continue;
                }

                if (arg.startsWith("-")) {
                    if (arg.toLowerCase().equals(debugArg)) {
                        debug = true;
                        if (showEvents.isEmpty()) {
                            for (GameEvent.Type eventType : GameEvent.Type.values()) {
                                showEvents.add(eventType);
                            }
                        }
                    } else if (arg.toLowerCase().startsWith(showEventsArg)) {
                        String showEventsString = arg.substring(showEventsArg.length() + 1);
                        for (String event : showEventsString.split(",")) {
                            showEvents.add(GameEvent.Type.valueOf(event));
                        }
                    } else if (arg.toLowerCase().startsWith(showPlayersArg)) {
                        String showPlayersString = arg.substring(showPlayersArg.length() + 1);
                        for (String player : showPlayersString.split(",")) {
                            showPlayers.add(player);
                        }
                    } else if (arg.toLowerCase().startsWith(ignorePlayerErrorsArg)) {
                        if (arg.trim().toLowerCase().equals(ignorePlayerErrorsArg)) {
                            ignoreAllPlayerErrors = true;
                        } else {
                            ignoreSomePlayerErrors = true;

                            try {
                                String player = arg.substring(ignorePlayerErrorsArg.length()).trim();
                                ignoreList.add(player);
                            } catch (Exception e) {
                                Util.log(e);
                                throw new ExitException();
                            }
                        }
                    } else if (arg.toLowerCase().equals(gameTypeStatsArg)) {
                        test = true;
                    } else if (arg.toLowerCase().startsWith(gameCountArg)) {
                        try {
                            numGames = Integer.parseInt(arg.substring(gameCountArg.length()));
                        } catch (Exception e) {
                            Util.log(e);
                            throw new ExitException();
                        }
                    } else if (arg.toLowerCase().startsWith(cardArg)) {
                        try {
                            cardsSpecifiedAtLaunch = arg.substring(cardArg.length()).split("-");
                        } catch (Exception e) {
                            Util.log(e);
                            throw new ExitException();
                        }
                    } else if (arg.toLowerCase().startsWith(siteArg)) {
                        try {
                            // UI.downloadSite =
                            // arg.substring(siteArg.length());
                        } catch (Exception e) {
                            Util.log(e);
                            throw new ExitException();
                        }
                    } else if (arg.toLowerCase().startsWith(gameTypeArg)) {
                        try {
                            gameTypeStr = arg.substring(gameTypeArg.length());
                        } catch (Exception e) {
                            Util.log(e);
                            throw new ExitException();
                        }
                    } else if (arg.toLowerCase().equals(platColonyArg)) {
                        alwaysIncludePlatColony = true;
                    } else if (arg.toLowerCase().equals(quickPlayArg)) {
                        quickPlay = true;
                    } else {
                        Util.log("Invalid arg:" + arg);
                        showUsage = true;
                    }
                } else {
                    String options = "";
                    String name = null;
                    int starIndex = arg.indexOf("*");
                    if (starIndex != -1) {
                        name = arg.substring(starIndex + 1);
                        arg = arg.substring(0, starIndex);
                    }
                    if (arg.endsWith(QUICK_PLAY)) {
                        arg = arg.substring(0, arg.length() - QUICK_PLAY.length());
                        options += "q";
                    }
                    int atIndex = arg.indexOf("@");
                    String className = arg;
                    String jar = null;
                    if (atIndex != -1) {
                        className = className.substring(0, atIndex);
                        jar = arg.substring(atIndex + 1);
                    }
                    playerClassesAndJars.add(new String[] { className, jar, name, options });
                }
            }

            numPlayers = playerClassesAndJars.size();

            if (numPlayers < 2 || numPlayers > 4 || showUsage) {
                Util.log("Usage: [-debug][-ignore(playername)][-count(# of Games)][-type(Game type)] class1 class2 [class3] [class4]");
                throw new ExitException();
            }

        } catch (ExitException e) {
            // Ignore...
        }

        if (gameTypeStr == null) {
            if (debug) {
                gameTypeStr = "FirstGame";
            }
        }

        if (numGames == -1) {
        	numGames = 1;
        }

    }

    public boolean isValidAction(MoveContext context, Card action) {
        if (action == null) {
            return true;
        }

        if (!(action instanceof ActionCard)) {
            return false;
        }

        for (Card card : context.getPlayer().hand) {
            if (action.equals(card)) {
                return true;
            }
        }

        return false;
    }

    public boolean isValidBuy(MoveContext context, Card card) {
        return isValidBuy(context, card, context.getCoinAvailableForBuy());
    }

    public boolean isValidBuy(MoveContext context, Card card, int gold) {
        if (card == null) {
            return true;
        }
        
        // TODO: Temp hack to prevent AI from buying possession, even though human player can, since it only half works 
        //       (AI will make decisions while possessed, but will try to make "good" ones)
//        if(card.equals(Cards.possession) && context != null && context.getPlayer() != null && context.getPlayer().isAi()) {
//            return false;
//        }

        if (card.isPrize()) {
            return false;
        }

        if (isPileEmpty(card)) {
            return false;
        }

        if (context.cantBuy.contains(card)) {
            return false;
        }

        if (context.copperPlayed && card.equals(Cards.grandMarket)) {
            return false;
        }

        int cost = card.getCost(context, true);
        
        int potions = 0;
        for (Card thisCard : context.getPlayedCards()) {
            if (thisCard instanceof TreasureCard && ((TreasureCard) thisCard).providePotion()) {
                potions++;
            }
        }
        if (cost <= gold && (!card.costPotion() || potions > 0)) {
            return true;
        }

        return false;
    }

    void playBuy(MoveContext context, Card buy) {
        Player player = context.getPlayer();
        context.buys--;

        Card card = takeFromPileCheckTrader(buy, context);
        if(card != null) {
	        GameEvent event = new GameEvent(GameEvent.Type.BuyingCard, (MoveContext) context);
	        event.card = card;
	        event.newCard = true;
	        broadcastEvent(event);
        }
        // cost adjusted based on any cards played or card being bought
        int cost = buy.getCost(context);

        context.gold -= buy.getCost(context);

        if (buy.costPotion()) {
        	context.potions--;
        } else if (!(buy instanceof VictoryCard) && context.talismansPlayed > 0 && cost < 5) {
        	for (int i = 0; i < context.talismansPlayed; i++) {
        		context.getPlayer().gainNewCard(buy, Cards.talisman, context);
        	}
        }

        player.addVictoryTokens(context, context.goonsPlayed);

        if (buy instanceof VictoryCard) {
        	context.victoryCardsBoughtThisTurn++;
        	for (int i = 0; i < context.hoardsPlayed; i++) {
        		player.gainNewCard(Cards.gold, Cards.hoard, context);
        	}
        }
        int embargos = getEmbargos(buy.getName());

        for (int i = 0; i < embargos; i++) {
        	player.gainNewCard(Cards.curse, Cards.embargo, context);
        }
        buy.isBought(context);
        haggler(context, buy);
    }
    
    private void haggler(MoveContext context, Card card) {
        List<Card> hagglers = new ArrayList<Card>();
        for(Card c : context.getPlayedCards()) {
            if(c.equals(Cards.haggler)) {
            	hagglers.add(c);
            }
        }                    
        
        for (int i = 0, hagglerSize = hagglers.size(); i < hagglerSize; i++) {
        	int cost = card.getCost(context);
        	boolean potion = card.costPotion();
        	boolean found = false;
        	for(Card cardInPlay : context.getCardsInPlay()) {
        		if(cardInPlay.getCost(context) < cost && (potion || !cardInPlay.costPotion()) && context.getCardsLeft(cardInPlay) > 0 && !(cardInPlay instanceof VictoryCard)) {
        			found = true;
        			break;
        		}
        	}

        	if(found) {
        		Card toGain = context.getPlayer().haggler_cardToObtain(context, cost - 1, potion);
        		if(toGain != null) {
        			if(toGain.getCost(context) >= cost || (!potion && toGain.costPotion()) || context.getCardsLeft(toGain) == 0 || (toGain instanceof VictoryCard)) { 
        				Util.playerError(context.getPlayer(), "Invalid card returned from Haggler, ignoring.");
        			}
        			else {
        				context.getPlayer().gainNewCard(toGain, Cards.haggler, context);
        			}
        		}
        	}
        }

    }
    
    public boolean buyWouldEndGame(Card card) {
        if (colonyInPlay && card.equals(Cards.colony)) {
            if (pileSize(card) <= 1) {
                return true;
            }
        }

        if (card.equals(Cards.province)) {
            if (pileSize(card) <= 1) {
                return true;
            }
        }
        
        if (emptyPiles() >= 2 && pileSize(card) <= 1) {
            return true;
        }

        return false;
    }

    private boolean checkGameOver() {
        if (colonyInPlay && isPileEmpty(Cards.colony)) {
            return true;
        }

        if (isPileEmpty(Cards.province)) {
            return true;
        }

        if (emptyPiles() >= 3) {
            return true;
        }

        return false;
    }

    public int emptyPiles() {
        int emptyPiles = 0;
        for (CardPile pile : piles.values()) {
            if (pile.getCount() <= 0 && !pile.card.isPrize()) {
                emptyPiles++;
            }
        }
        return emptyPiles;
    }

    // TODO: all calls should use this but initial turn draws...
    boolean drawToHand(Player player, Card responsible) {
        return drawToHand(player, responsible, true);
    }

    boolean drawToHand(Player player, Card responsible, boolean showUI) {
        Card card = draw(player);
        if (card != null) {
            player.hand.add(card, showUI);
        }

        if (card != null && responsible != null) {
            Util.debug(player, responsible.getName() + " draw:" + card.getName(), true);
        }
        
        return (card != null);
    }

    Card draw(Player player) {
        if (player.discard.size() > 0 && player.getDeckSize() == 0) {
            replenishDeck(player);
        }

        if (player.getDeckSize() == 0) {
            return null;
        }

        return player.deck.remove(0);
    }

    public void replenishDeck(Player player) {
        player.replenishDeck();

        MoveContext context = new MoveContext(this, player);
        GameEvent event = new GameEvent(GameEvent.Type.DeckReplenished, context);
        broadcastEvent(event);
    }
    
    private boolean cardInPlay(Card c) {
        for (CardPile pile : piles.values()) {
            if(pile.card.equals(c)) {
                return true;
            }
        }
        return false;
    }

    private void handleShowEvent(GameEvent event) {
        if (showEvents.contains(event.getType())) {
            Player player = event.getPlayer();

            if (player == null || (player != null && !showPlayers.isEmpty() && !showPlayers.contains(player.getPlayerName()))) {
                return;
            }

            if (event.getType() == GameEvent.Type.TurnEnd) {
                Util.debug("");
                return;
            }

            StringBuilder msg = new StringBuilder();
            msg.append(player.getPlayerName() + "::" + turnCount + ":" + event.getType());

            if (event.getType() == GameEvent.Type.BuyingCard) {
                msg.append(":" + event.getContext().getCoinAvailableForBuy() + " gold");
                if (event.getContext().getBuysLeft() > 0) {
                    msg.append(", buys remaining: " + event.getContext().getBuysLeft() + ")");
                }
            } else if (event.getType() == GameEvent.Type.PlayingAction || event.getType() == GameEvent.Type.TurnBegin
                || event.getType() == GameEvent.Type.NoBuy) {
                msg.append(":" + getHandString(player));
            } else if (event.attackedPlayer != null) {
                msg.append(":" + event.attackedPlayer.getPlayerName() + " with " + event.card.getName());
            }

            if (event.card != null) {
                msg.append(" -> " + event.card.getName());
            }

            if (event.getComment() != null) {
                msg.append(event.getComment());
            }

            Util.debug(msg.toString());
        }
    }

    int totalCardCount() {
        HashMap<String, Integer> cardCounts = new HashMap<String, Integer>();
        for (String cardName : piles.keySet()) {
            cardCounts.put(cardName, piles.get(cardName).getCount());
            }
        for (Card card : trashPile) {
            cardCounts.put(card.getName(), cardCounts.get(card.getName()) + 1);
        }
        for (int i = 0; i < numPlayers; i++) {
            for (Card card : players[i].getAllCards()) {
                cardCounts.put(card.getName(), cardCounts.get(card.getName()) + 1);
            }            
        }
        Util.log(cardCounts.toString());

        int totalCardCount = 0;
        for (Integer v : cardCounts.values()) {
            totalCardCount += v;
        }

        return totalCardCount;
    }

    @SuppressWarnings("unchecked")
    void initGameBoard() throws ExitException {
        cardSequence = 1;
        baneCard = null;

        initGameListener();

        initCards();
        initPlayers(numPlayers);
        initPlayerCards();


        gameOver = false;
    }

    protected void initPlayers(int numPlayers) throws ExitException {
        initPlayers(numPlayers, true);
    }

    protected void initPlayers(int numPlayers, boolean isRandom) throws ExitException {
        players = new Player[numPlayers];
        cardsObtainedLastTurn = new ArrayList[numPlayers];
        for (int i = 0; i < numPlayers; i++) {
            cardsObtainedLastTurn[i] = new ArrayList<Card>();
        }

        ArrayList<String[]> randomize = new ArrayList<String[]>();
        while (!playerClassesAndJars.isEmpty()) {
            if(isRandom) {
                randomize.add(playerClassesAndJars.remove(rand.nextInt(playerClassesAndJars.size())));
            } else {
                randomize.add(playerClassesAndJars.remove(0));
            }
        }

        playerClassesAndJars = randomize;
        playerCache.clear();

        for (int i = 0; i < numPlayers; i++) {
            try {
                String[] playerStartupInfo = playerClassesAndJars.get(i);
                if (playerStartupInfo[1] == null) {
                    players[i] = (Player) Class.forName(playerStartupInfo[0]).newInstance();
                } else {
                    String key = playerStartupInfo[0] + "::" + playerStartupInfo[1];
                    // players[i] = cachedPlayers.get(key);
                    //
                    // if(players[i] == null) {
                    // URLClassLoader classLoader = new URLClassLoader(new URL[] { new URL(classAndJar[1]) });
                    // players[i] = classLoader.loadClass(classAndJar[0]).newInstance();
                    // cachedPlayers.put(key, players[i]);
                    // }

                    Class<?> playerClass = cachedPlayerClasses.get(key);

                    if (playerClass == null) {
                        URLClassLoader classLoader = new URLClassLoader(new URL[] { new URL(playerStartupInfo[1]) });
                        playerClass = classLoader.loadClass(playerStartupInfo[0]);
                        cachedPlayerClasses.put(key, playerClass);
                    }

                    players[i] = (Player) playerClass.newInstance();
                }
                if(playerStartupInfo[2] != null) {
                    players[i].setName(playerStartupInfo[2]);
                }
                String options = playerStartupInfo[3];
                playerCache.put(playerStartupInfo[0], players[i]);
            } catch (Exception e) {
                Util.log(e);
                throw new ExitException();
            }

            players[i].game = this;
            players[i].playerNumber = i;

            // Interactive player needs this called once for each player on startup so internal counts work properly.
            players[i].getPlayerName();

            MoveContext context = new MoveContext(this, players[i]);
            players[i].newGame(context);
            players[i].initCards();

            context = new MoveContext(this, players[i]);
            String s = cardListText + "\n---------------\n\n";
            if (!alwaysIncludePlatColony && !platColonyPassedIn && !platColonyNotPassedIn) {
                s += "Chance for Platinum/Colony\n   " + (Math.round(chanceForPlatColony * 100)) + "% ... " + (platInPlay ? "included" : "not included" + "\n");
            }
            if (baneCard != null) {
                s += "Bane card: " + baneCard.getName() + "\n";
            }
            s += unfoundCardText;
            context.message = s;
            broadcastEvent(new GameEvent(GameEvent.Type.GameStarting, context));

        }
    }

    protected void initPlayerCards() {
        Player player;
        for (int i = 0; i < numPlayers; i++) {
            player = players[i];

            player.discard(takeFromPile(Cards.copper), null, null);
            player.discard(takeFromPile(Cards.copper), null, null);
            player.discard(takeFromPile(Cards.copper), null, null);
            player.discard(takeFromPile(Cards.copper), null, null);
            player.discard(takeFromPile(Cards.copper), null, null);
            player.discard(takeFromPile(Cards.copper), null, null);
            player.discard(takeFromPile(Cards.copper), null, null);

            player.discard(takeFromPile(Cards.estate), null, null);
            player.discard(takeFromPile(Cards.estate), null, null);
            player.discard(takeFromPile(Cards.estate), null, null);

            while (player.hand.size() < 5) {
                drawToHand(players[i], null, false);
            }
        }
    }

    protected void initCards() {
        piles.clear();
        embargos.clear();
        trashPile.clear();

//        addPile(Cards.platinum, 12);
        addPile(Cards.gold, 30);
        addPile(Cards.silver, 40);
        addPile(Cards.copper, 60);

       if (numPlayers == 2) {
            victoryCardPileSize = 8;
        }
        else {
        	victoryCardPileSize = 12;
        }
//        addPile(Cards.colony);
        addPile(Cards.province);
        addPile(Cards.duchy);
        addPile(Cards.estate, victoryCardPileSize + 3 * numPlayers);

        int curseCount = 10;
        if (numPlayers == 3) {
            curseCount = 20;
        } else if (numPlayers == 4) {
            curseCount = 30;
        }

        addPile(Cards.curse, curseCount);

        unfoundCards.clear();
        int added = 0;

        if(cardsSpecifiedAtLaunch != null) {
        	platColonyNotPassedIn = true;
            for(String cardName : cardsSpecifiedAtLaunch) {
                Card card = null;
                boolean bane = false;

            	if(cardName.startsWith(BANE)) {
                    bane = true;
                    cardName = cardName.substring(BANE.length());
                }            	
            	
                StringBuilder sb = new StringBuilder();
                for(char c : cardName.toCharArray()) {
                    if(Character.isLetterOrDigit(c)) {
                        sb.append(c);
                    }
                }
                String s = sb.toString();
                
                for (Card c : Cards.actionCards) {
                    if(c.getSafeName().equalsIgnoreCase(s)) {
                        card = c;
                        break;
                    }
                }
                if(card != null && bane) {
                    baneCard = card;
                }
                
                if(card != null
//                    && !card.equals(Cards.possession)
                    ) {
                    addPile(card);
                    added += 1;
                } else if ( s.equalsIgnoreCase(Cards.curse.getSafeName()) ||
                			s.equalsIgnoreCase(Cards.estate.getSafeName()) ||
                			s.equalsIgnoreCase(Cards.duchy.getSafeName()) ||
                			s.equalsIgnoreCase(Cards.province.getSafeName()) ||
                			s.equalsIgnoreCase(Cards.copper.getSafeName()) ||
                			s.equalsIgnoreCase(Cards.silver.getSafeName()) ||
                			s.equalsIgnoreCase(Cards.potion.getSafeName()) ||
                			s.equalsIgnoreCase(Cards.gold.getSafeName()) ||
                			s.equalsIgnoreCase(Cards.diadem.getSafeName()) ||
                			s.equalsIgnoreCase(Cards.followers.getSafeName()) ||
                			s.equalsIgnoreCase(Cards.princess.getSafeName()) ||
                			s.equalsIgnoreCase(Cards.trustySteed.getSafeName()) ||
                			s.equalsIgnoreCase(Cards.bagOfGold.getSafeName()) ) {
                	// do nothing
                } else if ( s.equalsIgnoreCase(Cards.platinum.getSafeName()) ||
                			s.equalsIgnoreCase(Cards.colony.getSafeName()) ) {
                    platColonyPassedIn = true;
                } else {
                    unfoundCards.add(s);
                    Util.debug("ERROR::Could not find card:" + s);
                }
            }
            
            for(String s : unfoundCards) {
            	if (added >= 10) 
            		break;
                Card c = null;
                int replacementCost = -1;
                if(s.equalsIgnoreCase("blackmarket")) {
                    replacementCost = 3;
                }
                else if(s.equalsIgnoreCase("envoy")) {
                    replacementCost = 4;
                }
                else if(s.equalsIgnoreCase("stash")) {
                    replacementCost = 5;
                }
                else if(s.equalsIgnoreCase("walledvillage")) {
                    replacementCost = 4;
                }
                else if(s.equalsIgnoreCase("possession")) {
                    // Not exact, since it requires potion as well, but good enough...
                    replacementCost = 6;
                }
                
                if(replacementCost != -1) {
                    ArrayList<Card> cardsWithSameCost = new ArrayList<Card>();
                    for (Card card : Cards.actionCards) {
                        if(card.getCost(null) == replacementCost && !cardInPlay(card)) {
                            cardsWithSameCost.add(card);
                        }
                    }
                    
                    if(cardsWithSameCost.size() > 0) {
                        c = cardsWithSameCost.get(rand.nextInt(cardsWithSameCost.size()));
                    }
                }
            
                while(c == null) {
                    c = Cards.actionCards.get(rand.nextInt(Cards.actionCards.size()));
                    if(cardInPlay(c)) {
                        c = null;
                    }
                }
                
                addPile(c);
                added += 1;
            }

            gameType = GameType.Random;
        }

        if (gameType == GameType.Random) {
            // ///////////////////////
            // To test specific cards.

            // addPile(Cards.warehouse);
            // added++;
            //
            // addPile(Cards.courtyard);
            // added++;
            //
            // addPile(Cards.fishingVillage);
            // added++;

            //
            // ///////////////////////
            while (added < 10) {
                Card card;
                do {
                    card = Cards.actionCards.get(rand.nextInt(Cards.actionCards.size()));
                    if (piles.get(card.getName()) != null) {
                        card = null;
                    }
                } while (card == null);

                addPile(card);
                added++;
            }
        } else if (gameType == GameType.RandomBaseGame) {
            for (int i = 0; i < 10; i++) {
                Card card;
                do {
                    card = Cards.actionCardsBaseGame.get(rand.nextInt(Cards.actionCardsBaseGame.size()));
                    if (piles.get(card.getName()) != null) {
                        card = null;
                    }
                } while (card == null);

                addPile(card);
            }
        } else if (gameType == GameType.RandomIntrigue) {
            for (int i = 0; i < 10; i++) {
                Card card;
                do {
                    card = Cards.actionCardsIntrigue.get(rand.nextInt(Cards.actionCardsIntrigue.size()));
                    if (piles.get(card.getName()) != null) {
                        card = null;
                    }
                } while (card == null);

                addPile(card);
            }
        } else if (gameType == GameType.RandomSeaside) {
            for (int i = 0; i < 10; i++) {
                Card card;
                do {
                    card = Cards.actionCardsSeaside.get(rand.nextInt(Cards.actionCardsSeaside.size()));
                    if (piles.get(card.getName()) != null) {
                        card = null;
                    }
                } while (card == null);

                addPile(card);
            }
        } else if (gameType == GameType.RandomAlchemy) {
            for (int i = 0; i < 10; i++) {
                Card card;
                do {
                    card = Cards.actionCardsAlchemy.get(rand.nextInt(Cards.actionCardsAlchemy.size()));
                    if (piles.get(card.getName()) != null) {
                        card = null;
                    }
                } while (card == null);

                addPile(card);
            }
        } else if (gameType == GameType.RandomProsperity) {
            for (int i = 0; i < 10; i++) {
                Card card;
                do {
                    card = Cards.actionCardsProsperity.get(rand.nextInt(Cards.actionCardsProsperity.size()));
                    if (piles.get(card.getName()) != null) {
                        card = null;
                    }
                } while (card == null);

                addPile(card);
            }
        } else if (gameType == GameType.RandomCornucopia) {
            for (int i = 0; i < 10; i++) {
                Card card;
                do {
                    card = Cards.actionCardsCornucopia.get(rand.nextInt(Cards.actionCardsCornucopia.size()));
                    if (piles.get(card.getName()) != null) {
                        card = null;
                    }
                } while (card == null);

                addPile(card);
            }
        } else if (gameType == GameType.RandomHinterlands) {
            for (int i = 0; i < 10; i++) {
                Card card;
                do {
                    card = Cards.actionCardsHinterlands.get(rand.nextInt(Cards.actionCardsHinterlands.size()));
                    if (piles.get(card.getName()) != null) {
                        card = null;
                    }
                } while (card == null);

                addPile(card);
            }
        } else if (gameType == GameType.ForbiddenArts) {
            addPile(Cards.apprentice);
            addPile(Cards.familiar);
//            addPile(Cards.possession);
            addPile(Cards.university);
            addPile(Cards.cellar);
            addPile(Cards.councilRoom);
            addPile(Cards.gardens);
            addPile(Cards.laboratory);
            addPile(Cards.thief);
            addPile(Cards.throneRoom);
        } else if (gameType == GameType.PotionMixers) {
            addPile(Cards.alchemist);
            addPile(Cards.apothecary);
            addPile(Cards.golem);
            addPile(Cards.herbalist);
            addPile(Cards.transmute);
            addPile(Cards.cellar);
            addPile(Cards.chancellor);
            addPile(Cards.festival);
            addPile(Cards.militia);
            addPile(Cards.smithy);
        } else if (gameType == GameType.ChemistryLesson) {
            addPile(Cards.alchemist);
            addPile(Cards.golem);
            addPile(Cards.philosophersStone);
            addPile(Cards.university);
            addPile(Cards.bureaucrat);
            addPile(Cards.market);
            addPile(Cards.moat);
            addPile(Cards.remodel);
            addPile(Cards.witch);
            addPile(Cards.woodcutter);
        } else if (gameType == GameType.Servants) {
            addPile(Cards.golem);
//            addPile(Cards.possession);
            addPile(Cards.scryingPool);
            addPile(Cards.transmute);
            addPile(Cards.vineyard);
            addPile(Cards.conspirator);
            addPile(Cards.greatHall);
            addPile(Cards.minion);
            addPile(Cards.pawn);
            addPile(Cards.steward);
        } else if (gameType == GameType.SecretResearch) {
            addPile(Cards.familiar);
            addPile(Cards.herbalist);
            addPile(Cards.philosophersStone);
            addPile(Cards.university);
            addPile(Cards.bridge);
            addPile(Cards.masquerade);
            addPile(Cards.minion);
            addPile(Cards.nobles);
            addPile(Cards.shantyTown);
            addPile(Cards.torturer);
        } else if (gameType == GameType.PoolsToolsAndFools) {
            addPile(Cards.apothecary);
            addPile(Cards.apprentice);
            addPile(Cards.golem);
            addPile(Cards.scryingPool);
            addPile(Cards.baron);
            addPile(Cards.coppersmith);
            addPile(Cards.ironworks);
            addPile(Cards.nobles);
            addPile(Cards.tradingPost);
            addPile(Cards.wishingWell);
        } else if (gameType == GameType.FirstGame) {
            addPile(Cards.cellar);
            addPile(Cards.market);
            addPile(Cards.militia);
            addPile(Cards.mine);
            addPile(Cards.moat);
            addPile(Cards.remodel);
            addPile(Cards.smithy);
            addPile(Cards.village);
            addPile(Cards.woodcutter);
            addPile(Cards.workshop);
        } else if (gameType == GameType.BigMoney) {
            addPile(Cards.adventurer);
            addPile(Cards.bureaucrat);
            addPile(Cards.chancellor);
            addPile(Cards.chapel);
            addPile(Cards.feast);
            addPile(Cards.laboratory);
            addPile(Cards.market);
            addPile(Cards.mine);
            addPile(Cards.moneyLender);
            addPile(Cards.throneRoom);
        } else if (gameType == GameType.Interaction) {
            addPile(Cards.bureaucrat);
            addPile(Cards.chancellor);
            addPile(Cards.councilRoom);
            addPile(Cards.festival);
            addPile(Cards.library);
            addPile(Cards.militia);
            addPile(Cards.moat);
            addPile(Cards.spy);
            addPile(Cards.thief);
            addPile(Cards.village);
        } else if (gameType == GameType.SizeDistortion) {
            addPile(Cards.cellar);
            addPile(Cards.chapel);
            addPile(Cards.feast);
            addPile(Cards.gardens);
            addPile(Cards.laboratory);
            addPile(Cards.thief);
            addPile(Cards.village);
            addPile(Cards.witch);
            addPile(Cards.woodcutter);
            addPile(Cards.workshop);
        } else if (gameType == GameType.VillageSquare) {
            addPile(Cards.bureaucrat);
            addPile(Cards.cellar);
            addPile(Cards.festival);
            addPile(Cards.library);
            addPile(Cards.market);
            addPile(Cards.remodel);
            addPile(Cards.smithy);
            addPile(Cards.throneRoom);
            addPile(Cards.village);
            addPile(Cards.woodcutter);
        } else if (gameType == GameType.VictoryDance) {
            addPile(Cards.bridge);
            addPile(Cards.duke);
            addPile(Cards.greatHall);
            addPile(Cards.harem);
            addPile(Cards.ironworks);
            addPile(Cards.masquerade);
            addPile(Cards.nobles);
            addPile(Cards.pawn);
            addPile(Cards.scout);
            addPile(Cards.upgrade);
        } else if (gameType == GameType.SecretSchemes) {
            addPile(Cards.conspirator);
            addPile(Cards.harem);
            addPile(Cards.ironworks);
            addPile(Cards.pawn);
            addPile(Cards.saboteur);
            addPile(Cards.shantyTown);
            addPile(Cards.steward);
            addPile(Cards.swindler);
            addPile(Cards.tradingPost);
            addPile(Cards.tribute);
        } else if (gameType == GameType.BestWishes) {
            addPile(Cards.coppersmith);
            addPile(Cards.courtyard);
            addPile(Cards.masquerade);
            addPile(Cards.scout);
            addPile(Cards.shantyTown);
            addPile(Cards.steward);
            addPile(Cards.torturer);
            addPile(Cards.tradingPost);
            addPile(Cards.upgrade);
            addPile(Cards.wishingWell);
        } else if (gameType == GameType.Deconstruction) {
            addPile(Cards.bridge);
            addPile(Cards.miningVillage);
            addPile(Cards.remodel);
            addPile(Cards.saboteur);
            addPile(Cards.secretChamber);
            addPile(Cards.spy);
            addPile(Cards.swindler);
            addPile(Cards.thief);
            addPile(Cards.throneRoom);
            addPile(Cards.torturer);
        } else if (gameType == GameType.HandMadness) {
            addPile(Cards.bureaucrat);
            addPile(Cards.chancellor);
            addPile(Cards.councilRoom);
            addPile(Cards.courtyard);
            addPile(Cards.mine);
            addPile(Cards.militia);
            addPile(Cards.minion);
            addPile(Cards.nobles);
            addPile(Cards.steward);
            addPile(Cards.torturer);
        } else if (gameType == GameType.Underlings) {
            addPile(Cards.baron);
            addPile(Cards.cellar);
            addPile(Cards.festival);
            addPile(Cards.library);
            addPile(Cards.masquerade);
            addPile(Cards.minion);
            addPile(Cards.nobles);
            addPile(Cards.pawn);
            addPile(Cards.steward);
            addPile(Cards.witch);
        } else if (gameType == GameType.HighSeas) {
            addPile(Cards.bazaar);
            addPile(Cards.caravan);
            addPile(Cards.embargo);
            addPile(Cards.explorer);
            addPile(Cards.haven);
            addPile(Cards.island);
            addPile(Cards.lookout);
            addPile(Cards.pirateShip);
            addPile(Cards.smugglers);
            addPile(Cards.wharf);
        } else if (gameType == GameType.BuriedTreasure) {
            addPile(Cards.ambassador);
            addPile(Cards.cutpurse);
            addPile(Cards.fishingVillage);
            addPile(Cards.lighthouse);
            addPile(Cards.outpost);
            addPile(Cards.pearlDiver);
            addPile(Cards.tactician);
            addPile(Cards.treasureMap);
            addPile(Cards.warehouse);
            addPile(Cards.wharf);
        } else if (gameType == GameType.Shipwrecks) {
            addPile(Cards.ghostShip);
            addPile(Cards.merchantShip);
            addPile(Cards.nativeVillage);
            addPile(Cards.navigator);
            addPile(Cards.pearlDiver);
            addPile(Cards.salvager);
            addPile(Cards.seaHag);
            addPile(Cards.smugglers);
            addPile(Cards.treasury);
            addPile(Cards.warehouse);
        } else if (gameType == GameType.ReachForTomorrow) {
            addPile(Cards.adventurer);
            addPile(Cards.cellar);
            addPile(Cards.councilRoom);
            addPile(Cards.cutpurse);
            addPile(Cards.ghostShip);
            addPile(Cards.lookout);
            addPile(Cards.seaHag);
            addPile(Cards.spy);
            addPile(Cards.treasureMap);
            addPile(Cards.village);
        } else if (gameType == GameType.Repetition) {
            addPile(Cards.caravan);
            addPile(Cards.chancellor);
            addPile(Cards.explorer);
            addPile(Cards.festival);
            addPile(Cards.militia);
            addPile(Cards.outpost);
            addPile(Cards.pearlDiver);
            addPile(Cards.pirateShip);
            addPile(Cards.treasury);
            addPile(Cards.workshop);
        } else if (gameType == GameType.GiveAndTake) {
            addPile(Cards.ambassador);
            addPile(Cards.fishingVillage);
            addPile(Cards.haven);
            addPile(Cards.island);
            addPile(Cards.library);
            addPile(Cards.market);
            addPile(Cards.moneyLender);
            addPile(Cards.salvager);
            addPile(Cards.smugglers);
            addPile(Cards.witch);
        } else if (gameType == GameType.Beginners) {
            addPile(Cards.bank);
            addPile(Cards.countingHouse);
            addPile(Cards.expand);
            addPile(Cards.goons);
            addPile(Cards.monument);
            addPile(Cards.rabble);
            addPile(Cards.royalSeal);
            addPile(Cards.venture);
            addPile(Cards.watchTower);
            addPile(Cards.workersVillage);
        } else if (gameType == GameType.FriendlyInteractive) {
            addPile(Cards.bishop);
            addPile(Cards.city);
            addPile(Cards.contraband);
            addPile(Cards.forge);
            addPile(Cards.hoard);
            addPile(Cards.peddler);
            addPile(Cards.royalSeal);
            addPile(Cards.tradeRoute);
            addPile(Cards.vault);
            addPile(Cards.workersVillage);
        } else if (gameType == GameType.BigActions) {
            addPile(Cards.city);
            addPile(Cards.expand);
            addPile(Cards.grandMarket);
            addPile(Cards.kingsCourt);
            addPile(Cards.loan);
            addPile(Cards.mint);
            addPile(Cards.quarry);
            addPile(Cards.rabble);
            addPile(Cards.talisman);
            addPile(Cards.vault);
        } else if (gameType == GameType.BiggestMoney) {
            addPile(Cards.bank);
            addPile(Cards.grandMarket);
            addPile(Cards.mint);
            addPile(Cards.royalSeal);
            addPile(Cards.venture);
            addPile(Cards.adventurer);
            addPile(Cards.laboratory);
            addPile(Cards.mine);
            addPile(Cards.moneyLender);
            addPile(Cards.spy);
        } else if (gameType == GameType.TheKingsArmy) {
            addPile(Cards.expand);
            addPile(Cards.goons);
            addPile(Cards.kingsCourt);
            addPile(Cards.rabble);
            addPile(Cards.vault);
            addPile(Cards.bureaucrat);
            addPile(Cards.councilRoom);
            addPile(Cards.moat);
            addPile(Cards.spy);
            addPile(Cards.village);
        } else if (gameType == GameType.TheGoodLife) {
            addPile(Cards.contraband);
            addPile(Cards.countingHouse);
            addPile(Cards.hoard);
            addPile(Cards.monument);
            addPile(Cards.mountebank);
            addPile(Cards.bureaucrat);
            addPile(Cards.cellar);
            addPile(Cards.chancellor);
            addPile(Cards.gardens);
            addPile(Cards.village);
        } else if (gameType == GameType.PathToVictory) {
            addPile(Cards.bishop);
            addPile(Cards.countingHouse);
            addPile(Cards.goons);
            addPile(Cards.monument);
            addPile(Cards.peddler);
            addPile(Cards.baron);
            addPile(Cards.harem);
            addPile(Cards.pawn);
            addPile(Cards.shantyTown);
            addPile(Cards.upgrade);
        } else if (gameType == GameType.AllAlongTheWatchtower) {
            addPile(Cards.hoard);
            addPile(Cards.talisman);
            addPile(Cards.tradeRoute);
            addPile(Cards.vault);
            addPile(Cards.watchTower);
            addPile(Cards.bridge);
            addPile(Cards.greatHall);
            addPile(Cards.miningVillage);
            addPile(Cards.pawn);
            addPile(Cards.torturer);
        } else if (gameType == GameType.LuckySeven) {
            addPile(Cards.bank);
            addPile(Cards.expand);
            addPile(Cards.forge);
            addPile(Cards.kingsCourt);
            addPile(Cards.vault);
            addPile(Cards.bridge);
            addPile(Cards.coppersmith);
            addPile(Cards.swindler);
            addPile(Cards.tribute);
            addPile(Cards.wishingWell);
        } else if (gameType == GameType.BountyOfTheHunt) {
            addPile(Cards.harvest);
            addPile(Cards.hornOfPlenty);
            addPile(Cards.huntingParty);
            addPile(Cards.menagerie);
            addPile(Cards.tournament);
            addPile(Cards.cellar);
            addPile(Cards.festival);
            addPile(Cards.militia);
            addPile(Cards.moneyLender);
            addPile(Cards.smithy);
        } else if (gameType == GameType.BadOmens) {
            addPile(Cards.fortuneTeller);
            addPile(Cards.hamlet);
            addPile(Cards.hornOfPlenty);
            addPile(Cards.jester);
            addPile(Cards.remake);
            addPile(Cards.adventurer);
            addPile(Cards.bureaucrat);
            addPile(Cards.laboratory);
            addPile(Cards.spy);
            addPile(Cards.throneRoom);
        } else if (gameType == GameType.TheJestersWorkshop) {
            addPile(Cards.fairgrounds);
            addPile(Cards.farmingVillage);
            addPile(Cards.horseTraders);
            addPile(Cards.jester);
            addPile(Cards.youngWitch);
            addPile(Cards.feast);
            addPile(Cards.laboratory);
            addPile(Cards.market);
            addPile(Cards.remodel);
            addPile(Cards.workshop);
            addPile(baneCard = Cards.chancellor);
        } else if (gameType == GameType.LastLaughs) {
            addPile(Cards.farmingVillage);
            addPile(Cards.harvest);
            addPile(Cards.horseTraders);
            addPile(Cards.huntingParty);
            addPile(Cards.jester);
            addPile(Cards.minion);
            addPile(Cards.nobles);
            addPile(Cards.pawn);
            addPile(Cards.steward);
            addPile(Cards.swindler);
        } else if (gameType == GameType.TheSpiceOfLife) {
            addPile(Cards.fairgrounds);
            addPile(Cards.hornOfPlenty);
            addPile(Cards.remake);
            addPile(Cards.tournament);
            addPile(Cards.youngWitch);
            addPile(Cards.coppersmith);
            addPile(Cards.courtyard);
            addPile(Cards.greatHall);
            addPile(Cards.miningVillage);
            addPile(Cards.tribute);
            addPile(baneCard = Cards.wishingWell);
        } else if (gameType == GameType.SmallVictories) {
            addPile(Cards.fortuneTeller);
            addPile(Cards.hamlet);
            addPile(Cards.huntingParty);
            addPile(Cards.remake);
            addPile(Cards.tournament);
            addPile(Cards.conspirator);
            addPile(Cards.duke);
            addPile(Cards.greatHall);
            addPile(Cards.harem);
            addPile(Cards.pawn);
        } else if (gameType == GameType.HinterlandsIntro) {
            addPile(Cards.cache);
            addPile(Cards.crossroads);
            addPile(Cards.develop);
            addPile(Cards.haggler);
            addPile(Cards.jackOfAllTrades);
            addPile(Cards.margrave);
            addPile(Cards.nomadCamp);
            addPile(Cards.oasis);
            addPile(Cards.spiceMerchant);
            addPile(Cards.stables);
        } else if (gameType == GameType.FairTrades) {
            addPile(Cards.borderVillage);
            addPile(Cards.cartographer);
            addPile(Cards.develop);
            addPile(Cards.duchess);
            addPile(Cards.farmland);
            addPile(Cards.illGottenGains);
            addPile(Cards.nobleBrigand);
            addPile(Cards.silkRoad);
            addPile(Cards.stables);
            addPile(Cards.trader);
        } else if (gameType == GameType.Bargains) {
            addPile(Cards.borderVillage);
            addPile(Cards.cache);
            addPile(Cards.duchess);
            addPile(Cards.foolsGold);
            addPile(Cards.haggler);
            addPile(Cards.highway);
            addPile(Cards.nomadCamp);
            addPile(Cards.scheme);
            addPile(Cards.spiceMerchant);
            addPile(Cards.trader);
        } else if (gameType == GameType.Gambits) {
            addPile(Cards.cartographer);
            addPile(Cards.crossroads);
            addPile(Cards.embassy);
            addPile(Cards.inn);
            addPile(Cards.jackOfAllTrades);
            addPile(Cards.mandarin);
            addPile(Cards.nomadCamp);
            addPile(Cards.oasis);
            addPile(Cards.oracle);
            addPile(Cards.tunnel);
        } else if (gameType == GameType.HighwayRobbery) {
            addPile(Cards.cellar);
            addPile(Cards.library);
            addPile(Cards.moneyLender);
            addPile(Cards.throneRoom);
            addPile(Cards.workshop);
            addPile(Cards.highway);
            addPile(Cards.inn);
            addPile(Cards.margrave);
            addPile(Cards.nobleBrigand);
            addPile(Cards.oasis);
        } else if (gameType == GameType.AdventuresAbroad) {
            addPile(Cards.adventurer);
            addPile(Cards.chancellor);
            addPile(Cards.festival);
            addPile(Cards.laboratory);
            addPile(Cards.remodel);
            addPile(Cards.crossroads);
            addPile(Cards.farmland);
            addPile(Cards.foolsGold);
            addPile(Cards.oracle);
            addPile(Cards.spiceMerchant);
        } else if (gameType == GameType.MoneyForNothing) {
            addPile(Cards.coppersmith);
            addPile(Cards.greatHall);
            addPile(Cards.pawn);
            addPile(Cards.shantyTown);
            addPile(Cards.torturer);
            addPile(Cards.cache);
            addPile(Cards.cartographer);
            addPile(Cards.jackOfAllTrades);
            addPile(Cards.silkRoad);
            addPile(Cards.tunnel);
        } else if (gameType == GameType.TheDukesBall) {
            addPile(Cards.conspirator);
            addPile(Cards.duke);
            addPile(Cards.harem);
            addPile(Cards.masquerade);
            addPile(Cards.upgrade);
            addPile(Cards.duchess);
            addPile(Cards.haggler);
            addPile(Cards.inn);
            addPile(Cards.nobleBrigand);
            addPile(Cards.scheme);
        } else if (gameType == GameType.Travelers) {
            addPile(Cards.cutpurse);
            addPile(Cards.island);
            addPile(Cards.lookout);
            addPile(Cards.merchantShip);
            addPile(Cards.warehouse);
            addPile(Cards.cartographer);
            addPile(Cards.crossroads);
            addPile(Cards.farmland);
            addPile(Cards.silkRoad);
            addPile(Cards.stables);
        } else if (gameType == GameType.Diplomacy) {
            addPile(Cards.ambassador);
            addPile(Cards.bazaar);
            addPile(Cards.caravan);
            addPile(Cards.embargo);
            addPile(Cards.smugglers);
            addPile(Cards.embassy);
            addPile(Cards.farmland);
            addPile(Cards.illGottenGains);
            addPile(Cards.nobleBrigand);
            addPile(Cards.trader);
        } else if (gameType == GameType.SchemesAndDreams) {
            addPile(Cards.apothecary);
            addPile(Cards.apprentice);
            addPile(Cards.herbalist);
            addPile(Cards.philosophersStone);
            addPile(Cards.transmute);
            addPile(Cards.duchess);
            addPile(Cards.foolsGold);
            addPile(Cards.illGottenGains);
            addPile(Cards.jackOfAllTrades);
            addPile(Cards.scheme);
        } else if (gameType == GameType.WineCountry) {
            addPile(Cards.apprentice);
            addPile(Cards.familiar);
            addPile(Cards.golem);
            addPile(Cards.university);
            addPile(Cards.vineyard);
            addPile(Cards.crossroads);
            addPile(Cards.farmland);
            addPile(Cards.haggler);
            addPile(Cards.highway);
            addPile(Cards.nomadCamp);
        } else if (gameType == GameType.InstantGratification) {
            addPile(Cards.bishop);
            addPile(Cards.expand);
            addPile(Cards.hoard);
            addPile(Cards.mint);
            addPile(Cards.watchTower);
            addPile(Cards.farmland);
            addPile(Cards.haggler);
            addPile(Cards.illGottenGains);
            addPile(Cards.nobleBrigand);
            addPile(Cards.trader);
        } else if (gameType == GameType.TreasureTrove) {
            addPile(Cards.bank);
            addPile(Cards.monument);
            addPile(Cards.royalSeal);
            addPile(Cards.tradeRoute);
            addPile(Cards.venture);
            addPile(Cards.cache);
            addPile(Cards.develop);
            addPile(Cards.foolsGold);
            addPile(Cards.illGottenGains);
            addPile(Cards.mandarin);
        } else if (gameType == GameType.BlueHarvest) {
            addPile(Cards.hamlet);
            addPile(Cards.hornOfPlenty);
            addPile(Cards.horseTraders);
            addPile(Cards.jester);
            addPile(Cards.tournament);
            addPile(Cards.foolsGold);
            addPile(Cards.mandarin);
            addPile(Cards.nobleBrigand);
            addPile(Cards.trader);
            addPile(Cards.tunnel);
        } else if (gameType == GameType.TravelingCircus) {
            addPile(Cards.fairgrounds);
            addPile(Cards.farmingVillage);
            addPile(Cards.huntingParty);
            addPile(Cards.jester);
            addPile(Cards.menagerie);
            addPile(Cards.borderVillage);
            addPile(Cards.embassy);
            addPile(Cards.foolsGold);
            addPile(Cards.nomadCamp);
            addPile(Cards.oasis);
        }

        chanceForPlatColony = 0;
        if (alwaysIncludePlatColony || platColonyPassedIn) {
            platInPlay = true;
            colonyInPlay = true;
        } else if (platColonyNotPassedIn) {
            platInPlay = false;
            colonyInPlay = false;
        } else {
            platInPlay = false;
            colonyInPlay = false;

            for (CardPile pile : piles.values()) {
                if (pile != null && pile.card != null && pile.card.getExpansion() != null && pile.card.getExpansion().equals("Prosperity")) {
                    chanceForPlatColony += 0.1;
                }
            }

            if (rand.nextDouble() < chanceForPlatColony) {
                platInPlay = true;
                colonyInPlay = true;
            }
        }

        if (platInPlay)
            addPile(Cards.platinum, 12);
        if (colonyInPlay)
            addPile(Cards.colony);

        if (piles.containsKey(Cards.youngWitch.getName()) && baneCard == null) {
            Card card = null;
            ArrayList<Card> cardList = gameType == GameType.RandomCornucopia ? Cards.actionCardsCornucopia : Cards.actionCards;
            boolean avail = true;

            if (gameType == GameType.RandomCornucopia) {
                avail = false;
                for (Card c : cardList) {
                    if (piles.get(c.getName()) == null && c.getCost(null) <= 3 && c.getCost(null) >= 2 && !c.costPotion()) {
                        avail = true;
                        break;
                    }
                }
                if (!avail) {
                    do {
                        card = cardList.get(rand.nextInt(cardList.size()));
                        // find a bane card that has already been added
                        if (piles.get(card.getName()) == null || card.getCost(null) > 3 || card.getCost(null) < 2 || card.costPotion()) {
                            card = null;
                        }
                    } while (card == null);

                    Card cardToAdd = null;
                    // now add another card
                    do {
                        cardToAdd = cardList.get(rand.nextInt(cardList.size()));
                        if (piles.get(cardToAdd.getName()) != null) {
                            cardToAdd = null;
                        }
                    } while (cardToAdd == null);
                    addPile(cardToAdd);
                }
            }

            if (avail) {
                do {
                    card = cardList.get(rand.nextInt(cardList.size()));
                    if (piles.get(card.getName()) != null || card.getCost(null) > 3 || card.getCost(null) < 2 || card.costPotion()) {
                        card = null;
                    }
                } while (card == null);
                addPile(card);
            }

            baneCard = card;
        }

        // Add the potion if there are any cards that need them.
        for (CardPile pile : piles.values()) {
            if (pile.card.costPotion()) {
                addPile(Cards.potion, 16);
                break;
            }
        }

        if (piles.containsKey(Cards.tournament.getName()) && !piles.containsKey(Cards.bagOfGold.getName())) {
            addPile(Cards.bagOfGold, 1);
            addPile(Cards.diadem, 1);
            addPile(Cards.followers, 1);
            addPile(Cards.princess, 1);
            addPile(Cards.trustySteed, 1);
        }

        boolean oldDebug = debug;
        if (!debug && !showEvents.isEmpty()) {
            debug = true;
        }
        Util.debug("");
        Util.debug("Cards in Play", true);
        Util.debug("---------------", true);
        cardListText += "Cards in play\n---------------\n";
        // Util.debug(Cards.copper.getName());
        // Util.debug(Cards.silver.getName());
        // Util.debug(Cards.gold.getName());
        // Util.debug(Cards.estate.getName());
        // Util.debug(Cards.duchy.getName());
        // Util.debug(Cards.province.getName());
        // Util.debug(Cards.curse.getName());
        // Util.debug("");
        //

        int cost = 0;
        while (cost < 10) {
            for (CardPile pile : piles.values()) {
                if (!Cards.nonKingdomCards.contains(pile.card)) {
                    if (pile.card.getCost(null) == cost) {
                        Util.debug(Util.getShortText(pile.card), true);
                        cardListText += Util.getShortText(pile.card) + "\n";
                    }
                }
            }

            cost++;
        }
        if (baneCard != null) {
            Util.debug("(Bane) " + Util.getShortText(baneCard), true);
        }
        Util.debug("");
        debug = oldDebug;

        if (unfoundCards != null && unfoundCards.size() > 0) {
            unfoundCardText += "\n";
            String cardList = "";
            boolean first = true;
            for (String s : unfoundCards) {
                if (first) {
                    first = false;
                } else {
                    cardList += "\n";
                }
                cardList += s;
            }
            cardList += "\n\n";
            unfoundCardText += "The following cards are not \navailable, so replacements \nhave been used:\n" + cardList;
        }

        // context = new MoveContext(this, null);
        // context.message = "" + ((int) chance * 100) + "% - " +
        // (platInPlay?"Yes":"No");
        // broadcastEvent(new GameEvent(GameEvent.Type.PlatAndColonyChance,
        // context));
    }

    protected void initGameListener() {
        listeners.clear();

        gameListener = new GameEventListener() {

            public void gameEvent(GameEvent event) {
                handleShowEvent(event);

                if (event.getType() == GameEvent.Type.GameStarting || event.getType() == GameEvent.Type.GameOver) {
                    return;
                }
                
                if (event.getType() == GameEvent.Type.CardTrashed && event.context.getPossessedBy() != null) {
                    event.context.addToPossessedTrashPile(event.getCard());
                }

                if (event.getType() == GameEvent.Type.CardObtained || event.getType() == GameEvent.Type.BuyingCard) {
                    
                    MoveContext context = event.getContext();
                    
                    if(context != null && event.card instanceof VictoryCard) {
                        context.vpsGainedThisTurn += ((VictoryCard) event.card).getVictoryPoints();
                    }
                    
                    Player player = context.getPlayer();
                    if(context.getPossessedBy() != null) {
                        player = context.getPossessedBy();
                    }
                    if (Cards.inn.equals(event.responsible))
                        Util.debug((String.format("discard pile: %d", player.discard.size())), true);
                    
                    // See rules explanation of Tunnel for what commandedDiscard means.
                    boolean commandedDiscard = true;
                    if(event.getType() == GameEvent.Type.BuyingCard) {
                        commandedDiscard = false;
                    }
                    if(event.responsible != null) {
                        Card r = event.responsible;
                        if(r.equals(Cards.borderVillage) || 
                           r.equals(Cards.feast) ||
                           r.equals(Cards.remodel) ||
                           r.equals(Cards.swindler) ||
                           r.equals(Cards.ironworks) ||
                           r.equals(Cards.saboteur) ||
                           r.equals(Cards.upgrade) ||
                           r.equals(Cards.ambassador) ||
                           r.equals(Cards.smugglers) ||
//                           r.equals(Cards.possession) ||
                           r.equals(Cards.talisman) ||
                           r.equals(Cards.expand) ||
                           r.equals(Cards.forge) ||
                           r.equals(Cards.remake) ||
                           r.equals(Cards.hornOfPlenty) ||
                           r.equals(Cards.jester) ||
                           r.equals(Cards.develop) ||
                           r.equals(Cards.haggler) ||
                           r.equals(Cards.workshop))
                        {
                            commandedDiscard = false;
                        }
                    }
                    boolean handled = false;
                    
                    boolean masqueradePass = Cards.masquerade.equals(event.responsible); 
                    
                    //Not sure if this is exactly right for the Trader, but it seems to be based on detailed card explanation in the rules
                    //The handling for new cards is done before taking the card from the pile in a different method below.
                    if(!event.newCard && !masqueradePass) {
                        if(player.hand.contains(Cards.trader)) {
                            if((player).trader_shouldGainSilverInstead((MoveContext) context, event.card)) {
                                player.trash(event.card, Cards.trader, (MoveContext) context);
                                event.card = Cards.silver;
                                player.gainNewCard(Cards.silver, Cards.trader, context);
                                return;
                            }
                        }
                    }
                    
                    if (event.getPlayer() == players[playersTurn]) { // || (context.possessedBy != null && context.possessedBy == event.getPlayer())) {
                        cardsObtainedLastTurn[playersTurn].add(event.card);
                    }

                    if (player.hand.contains(Cards.watchTower) && !masqueradePass) {
                        WatchTowerOption choice = context.player.watchTower_chooseOption((MoveContext) context, event.card);
    
                        if (choice == WatchTowerOption.TopOfDeck) {
                            handled = true;
                            player.putOnTopOfDeck(event.card);
                        } else if (choice == WatchTowerOption.Trash) {
                            handled = true;
                            trashPile.add(event.card);
                            context.cardsTrashedThisTurn++;
                            GameEvent gameEvent = new GameEvent(GameEvent.Type.CardTrashed, context);
                            event.card = event.card;
                            event.responsible = Cards.watchTower;
                            broadcastEvent(gameEvent);
                        } 
                    }

                    if(!handled) {
                        if (!masqueradePass && context.royalSealPlayed && context.player.royalSeal_shouldPutCardOnDeck((MoveContext) context, event.card)) {
                            player.putOnTopOfDeck(event.card);
                        } else if (event.card.equals(Cards.nomadCamp) && !masqueradePass) {
                            player.putOnTopOfDeck(event.card);
                        } else if (event.responsible != null) {
                            Card r = event.responsible;
                            if (r.equals(Cards.bagOfGold) || r.equals(Cards.develop) || r.equals(Cards.bureaucrat) || r.equals(Cards.seaHag) || r.equals(Cards.treasureMap) || r.equals(Cards.tournament) || r.equals(Cards.foolsGold)) {
                                player.putOnTopOfDeck(event.card);
                            } else if (r.equals(Cards.masquerade) || r.equals(Cards.tradingPost) || r.equals(Cards.mine) || r.equals(Cards.explorer) || r.equals(Cards.torturer)) {
                                player.hand.add(event.card);
                            } else if (r.equals(Cards.illGottenGains) && event.card.equals(Cards.copper)) {
                                player.hand.add(event.card);
                            } else {
                                player.discard(event.card, null, null, commandedDiscard);
                            }
                        } else {
                            player.discard(event.card, null, null, commandedDiscard);
                        }
                    } 
                    
                    if (masqueradePass) {
                    } else if (event.card.equals(Cards.illGottenGains)) {
                        for(Player targetPlayer : getPlayersInTurnOrder()) {
                            if(targetPlayer != player) {
                                MoveContext targetContext = new MoveContext(Game.this, targetPlayer);
                                // TODO: Is this really not an attack? Doesn't seem to be based on card, but not sure...
                                // targetPlayer.attacked(Cards.illGottenGains, targetContext);
                                targetPlayer.gainNewCard(Cards.curse, event.card, targetContext);
                            }
                        }
                    } else if(event.card.equals(Cards.province)) {
                        for(Player targetPlayer : getPlayersInTurnOrder()) {
                            if(targetPlayer != player) {
                                int foolsGoldCount = 0;
                                // Check all of the cards, not just for existence, since there may be more than 1
                                for(Card c : targetPlayer.hand) {
                                    if(c.equals(Cards.foolsGold)) {
                                        foolsGoldCount++;
                                    }
                                }
                                
                                while(foolsGoldCount-- > 0) {
                                    MoveContext targetContext = new MoveContext(Game.this, targetPlayer);
                                    if((targetPlayer).foolsGold_shouldTrash(targetContext)) {
                                        targetPlayer.hand.remove(Cards.foolsGold);
                                        targetPlayer.trash(Cards.foolsGold, Cards.foolsGold, targetContext);
                                        targetPlayer.gainNewCard(Cards.gold, Cards.foolsGold, targetContext);
                                    }
                                }
                            }
                        }
                    } else if(event.card.equals(Cards.duchy)) {
                        if(((MoveContext) context).getCardsLeft(Cards.duchess) > 0) {
                            if((player).duchess_shouldGainBecauseOfDuchy((MoveContext) context)) {
                                player.gainNewCard(Cards.duchess, Cards.duchess, context);
                            }
                        }
                    } else if(event.card.equals(Cards.embassy)) {
                        for(Player targetPlayer : getPlayersInTurnOrder()) {
                            if(targetPlayer != player) {
                                MoveContext targetContext = new MoveContext(Game.this, targetPlayer);
                                targetPlayer.gainNewCard(Cards.silver, event.card, targetContext);
                            }
                        }
                    } else if(event.card.equals(Cards.cache)) {
                        for(int i=0; i < 2; i++) {
                            player.gainNewCard(Cards.copper, event.card, context);
                        }
                    } else if(event.card.equals(Cards.inn)) {
                        ArrayList<Card> cards = new ArrayList<Card>();
                        int actionCardsFound = 0;
                        for(int i=player.discard.size() - 1; i >= 0; i--) {
                            Card c = player.discard.get(i);
                            if(c instanceof ActionCard) {
                                actionCardsFound++;
                                if((player).inn_shuffleCardBackIntoDeck(event.getContext(), (ActionCard) c)) {
                                    cards.add(c);
                                }
                            }
                        }

                        Util.debug((String.format("Inn: %d action(s) found in %d-card discard pile", actionCardsFound, player.discard.size())), true);
                        if (cards.size() > 0) {
                            for(Card c : cards) {
                                player.discard.remove(c);
                                player.deck.add(c);
                            }
                            player.shuffleDeck();
                        }
                    } else if (event.card.equals(Cards.borderVillage)) {
                        boolean validCard = false;
                        
                        for(Card c : event.context.getCardsInPlay()) {
                            if(c.getCost(context) < Cards.borderVillage.getCost(context) && !c.costPotion() && event.context.getCardsLeft(c) > 0) {
                                validCard = true;
                                break;
                            }
                        }
                        
                        if(validCard) {
                            Card card = context.player.borderVillage_cardToObtain((MoveContext) context);
                            if (card != null) {
                                if(card.getCost(context) < Cards.borderVillage.getCost(context) && !card.costPotion()) {                            
                                    player.gainNewCard(card, event.card, (MoveContext) context);
                                }
                                else {
                                    Util.playerError(player, "Border Village returned invalid card, ignoring.");
                                }
                            }
                        }
                    } else if (event.card.equals(Cards.mandarin)) {
                        ArrayList<Card> playedCards = ((MoveContext) context).getPlayedCards();
                        ArrayList<Card> treasureCardsInPlay = new ArrayList<Card>();
                        
                        for(Card c : playedCards) {
                            if(c instanceof TreasureCard) {
                                treasureCardsInPlay.add(c);
                            }
                        }
                        
                        Card[] order = player.mandarin_orderCards(context, treasureCardsInPlay.toArray(new Card[treasureCardsInPlay.size()]));
                        for (Card c : order) {
                            player.putOnTopOfDeck(c);
                            playedCards.remove(c);
                        }
                    }                    
                    // Achievement check...
                    if(event.getType() == GameEvent.Type.BuyingCard && !player.achievementSingleCardFailed) {
                        if (Cards.isKingdomCard(event.getCard())) {
                            if(player.achievementSingleCardFirstKingdomCardBought == null) {
                                player.achievementSingleCardFirstKingdomCardBought = event.getCard();
                            }
                            else {
                                if(!player.achievementSingleCardFirstKingdomCardBought.equals(event.getCard())) {
                                    player.achievementSingleCardFailed = true;
                                    player.achievementSingleCardFirstKingdomCardBought = null;
                                }
                            }
                        }
                    }
                }
                
                if(event.getType() == GameEvent.Type.BuyingCard && event.getContext() != null) {
                    MoveContext context = event.getContext();
                    Player player = context.getPlayer();
                    if(context.getPossessedBy() != null) {
                        player = context.getPossessedBy();
                    }
                }

                boolean shouldShow = debug;
                if (!shouldShow) {
                    if (event.getType() != GameEvent.Type.TurnBegin && event.getType() != GameEvent.Type.TurnEnd
                        && event.getType() != GameEvent.Type.DeckReplenished && event.getType() != GameEvent.Type.GameStarting) {
                        shouldShow = true;
                    }
                }

                if (!showEvents.contains(event.getType()) && shouldShow) {
                    StringBuilder msg = new StringBuilder();
                    msg.append(event.getPlayer().getPlayerName() + ":" + event.getType());
                    if (event.card != null) {
                        msg.append(":" + event.card.getName());
                    }
                    if (event.attackedPlayer != null) {
                        msg.append(", attacking:" + event.attackedPlayer.getPlayerName());
                    }

                    if (event.getType() == GameEvent.Type.BuyingCard) {
                        msg.append(" (with gold: " + event.getContext().getCoinAvailableForBuy() + ", buys remaining: " + event.getContext().getBuysLeft());
                    }
                    Util.debug(msg.toString(), true);
                }
            }

        };
    }

    boolean hasMoat(Player player) {
        for (Card card : player.hand) {
            if (card.equals(Cards.moat)) {
                return true;
            }
        }

        return false;
    }

    boolean hasLighthouse(Player player) {
        for (Card card : player.nextTurnCards) {
            if (card.equals(Cards.lighthouse)) {
                return true;
            }
        }

        return false;
    }

    // TODO privatize
    public int getEmbargos(String name) {
        Integer count = embargos.get(name);
        if (count == null) {
            return 0;
        }

        return count;
    }

	/*
	Note that any cards in the supply can have Embargo coins added.
	This includes the basic seven cards (Victory, Curse, Treasure),
	any of the 10 game piles, and Colony/Platinum when included.
	However, this does NOT include any Prizes from Cornucopia.
	 */
    void addEmbargo(String name) {
        CardPile pile = piles.get(name);
        // Don't embargo cards not in the game
        if (this.isValidEmbargoPile(pile)) {

			Integer count = embargos.get(name);
			if (count == null) {
				embargos.put(name, 1);
			} else {
				embargos.put(name, count + 1);
			}
		}
    }
	
	private boolean isValidEmbargoPile(final CardPile pile) {
		boolean valid = true;
		
		if(pile == null) {
			valid = false;
		} else {
			final Card card = pile.card;

			if(card == null || Cards.prizeCards.contains(card)) {
				valid = false;
			}
		}

		return valid;
	}

    // Only is valid for cards in play...
    Card readCard(String name) {
        CardPile pile = piles.get(name);
        if (pile == null || pile.getCount() <= 0) {
            return null;
        }
        return pile.card;
    }

    public Card takeFromPile(Card card) {
        CardPile pile = piles.get(card.getName());
        if (pile == null || pile.getCount() <= 0) {
            return null;
        }

        Card thisCard = pile.removeCard();

        return thisCard;
    }
    
    public Card takeFromPileCheckTrader(Card cardToGain, MoveContext context) {
        if(!isPileEmpty(cardToGain) && context.getPlayer().hand.contains(Cards.trader) && !cardToGain.equals(Cards.silver)) {
            if(context.player.trader_shouldGainSilverInstead((MoveContext) context, cardToGain)) {
                cardToGain = Cards.silver;
            }
        }
        
        return takeFromPile(cardToGain);
    }

    public int pileSize(Card card) {
        CardPile pile = piles.get(card.getName());
        if (pile == null) {
            return -1;
        }

        return pile.getCount();
    }

    boolean isPileEmpty(Card card) {
        return pileSize(card) <= 0;
    }

    boolean isCardInGame(Card card) {
        CardPile pile = piles.get(card.getName());
        if (pile == null) {
            return false;
        }
        return true;
    }

    void addPile(Card card) {
        if (card instanceof VictoryCard) {
            addPile(card, victoryCardPileSize);
        } else {
            addPile(card, kingdomCardPileSize);
        }
    }

    void addPile(Card card, int count) {
        CardPile pile = new CardPile(card, count);
        piles.put(card.getName(), pile);
    }
    
    void addPile(CardPile pile) {
        piles.put(pile.card.getName(), pile);
    }

    private ArrayList<Card> getCardsObtainedByPlayer(int PlayerNumber) {
        return cardsObtainedLastTurn[PlayerNumber];
    }

    public ArrayList<Card> getCardsObtainedByPlayer() {
        return getCardsObtainedByPlayer(playersTurn);
    }

    public ArrayList<Card> getCardsObtainedByLastPlayer() {
        int playerOnRight = playersTurn - 1;
        if (playerOnRight < 0) {
            playerOnRight = numPlayers - 1;
        }
        return getCardsObtainedByPlayer(playerOnRight);
    }

    public Player getNextPlayer() {
        int next = playersTurn + 1;
        if (next >= numPlayers) {
            next = 0;
        }

        return players[next];
    }

    public Player[] getPlayersInTurnOrder() {
        Player[] ordered = new Player[numPlayers];

        int at = playersTurn;
        for (int i = 0; i < numPlayers; i++) {
            ordered[i] = players[at];
            at++;
            if (at >= numPlayers) {
                at = 0;
            }
        }

        return ordered;
    }

    public void broadcastEvent(GameEvent event) {
        for (GameEventListener listener : listeners) {
            listener.gameEvent(event);
        }
        // notify this class' listener last for proper action/logging order
        if(gameListener != null)
            gameListener.gameEvent(event);
    }

    String getHandString(Player player) {
        String handString = null;
        Card[] hand = player.getHand().toArray();
        Arrays.sort(hand, new CardCostComparator());
        for (Card card : hand) {
            if (card == null) {
                continue;
            }
            if (handString == null) {
                handString = card.getName();
            } else {
                handString += ", " + card.getName();
            }
        }

        return handString;
    }
    
    public boolean playerShouldSelectCoinsToPlay(MoveContext context, CardList cards) {
        if(!quickPlay) {
            return true;
        }
        
        if(cards == null) {
            return false;
        }

        CardPile grandMarket = piles.get(Cards.grandMarket.getName());
        for(Card card : cards) {
            if (
                card.equals(Cards.philosophersStone) ||
                card.equals(Cards.bank) ||
                card.equals(Cards.contraband) ||
                card.equals(Cards.loan) ||
                card.equals(Cards.quarry) ||
                card.equals(Cards.talisman) ||
                card.equals(Cards.hornOfPlenty) ||
                card.equals(Cards.diadem) || 
                (card.equals(Cards.copper) && grandMarket != null && grandMarket.getCount() > 0)
                ) 
            {
                return true;
            }
        }
        
        return false;
    }

    static boolean checkForInteractive() throws ExitException {
        for (int i = 0; i < numPlayers; i++) {
            Player player;
            try {
                String[] classAndJar = playerClassesAndJars.get(i);
                if (classAndJar[1] == null) {
                    player = (Player) Class.forName(classAndJar[0]).newInstance();
                } else {
                    URLClassLoader classLoader = new URLClassLoader(new URL[] { new URL(classAndJar[1]) });
                    player = (Player) classLoader.loadClass(classAndJar[0]).newInstance();
                }
                if(classAndJar[2] != null) {
                    player.setName(classAndJar[2]);
                }
            } catch (Exception e) {
                Util.log(e);
                throw new ExitException();
            }
        }
        return false;
    }
}
