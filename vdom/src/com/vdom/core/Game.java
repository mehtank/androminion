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
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Random;

import com.vdom.api.ActionCard;
import com.vdom.api.Card;
import com.vdom.api.CardCostComparator;
import com.vdom.api.EventCard;
import com.vdom.api.FrameworkEvent;
import com.vdom.api.FrameworkEventHelper;
import com.vdom.api.GameEvent;
import com.vdom.api.GameEvent.Type;
import com.vdom.api.GameEventListener;
import com.vdom.api.GameType;
import com.vdom.api.TreasureCard;
import com.vdom.api.VictoryCard;
import com.vdom.core.Player.ExtraTurnOption;
import com.vdom.core.Player.WatchTowerOption;

public class Game {
    public static boolean junit = false;
    public static boolean debug = false;
    public static Integer cardSequence = 1;
    public static HashMap<String, Double> GAME_TYPE_WINS = new HashMap<String, Double>();

    public static HashMap<String, Integer> winStats = new HashMap<String, Integer>();
    public static final String QUICK_PLAY = "(QuickPlay)";
    public static final String BANE = "bane+";
    public static final String BLACKMARKET = "blackMarket+";

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
    public static List<Expansion> randomExpansions = null;
    public static String gameTypeStr = null;
    public static boolean showUsage = false;

    public static boolean sheltersNotPassedIn = false;
    public static boolean sheltersPassedIn = false;
    public static boolean platColonyNotPassedIn = false;
    public static boolean platColonyPassedIn = false;
    public static double chanceForPlatColony = -1;
    public static double chanceForShelters = 0.0;
    
    public static int blackMarketCount = 25;
    
    public static boolean randomIncludesEvents = false;
    public static int numRandomEvents = 0;

    public static boolean quickPlay = false;
    public static boolean sortCards = false;
    public static boolean actionChains = false;
    public static boolean suppressRedundantReactions = false;
    public static boolean equalStartHands = false;
    public static boolean startGuildsCoinTokens = false; //only for testing
    public static boolean lessProvinces = false; //only for testing
    public static boolean maskPlayerNames = false;

    public static final HashSet<GameEvent.Type> showEvents = new HashSet<GameEvent.Type>();
    public static final HashSet<String> showPlayers = new HashSet<String>();
    static boolean test = false;
    static boolean ignoreAllPlayerErrors = false;
    static boolean ignoreSomePlayerErrors = false;
    static HashSet<String> ignoreList = new HashSet<String>();

    static ArrayList<GameStats> gameTypeStats = new ArrayList<GameStats>();

    static int numGames = -1;
    static int gameCounter = 0;
    public ArrayList<GameEventListener> listeners = new ArrayList<GameEventListener>();
    public GameEventListener gameListener;
    static boolean forceDownload = false;
    static HashMap<String, Double> overallWins = new HashMap<String, Double>();

    public static Random rand = new Random(System.currentTimeMillis());
    public HashMap<String, AbstractCardPile> piles = new HashMap<String, AbstractCardPile>();
    public HashMap<String, Integer> embargos = new HashMap<String, Integer>();
    private HashMap<String, HashMap<Player, List<PlayerSupplyToken>>> playerSupplyTokens = new HashMap<String, HashMap<Player,List<PlayerSupplyToken>>>();
    public ArrayList<Card> trashPile = new ArrayList<Card>();
    public ArrayList<Card> possessedTrashPile = new ArrayList<Card>();
    public ArrayList<Card> possessedBoughtPile = new ArrayList<Card>();
    public ArrayList<Card> blackMarketPile = new ArrayList<Card>();
    public ArrayList<Card> blackMarketPileShuffled = new ArrayList<Card>();

    public int tradeRouteValue = 0;
    public Card baneCard = null;

    public boolean bakerInPlay = false;
    public boolean journeyTokenInPlay = false;

    private static final int kingdomCardPileSize = 10;
    public static int victoryCardPileSize = 12;

    ArrayList<Card>[] cardsObtainedLastTurn;
    static int playersTurn;

    //    public UI ui;
    int turnCount = 0;
    int consecutiveTurnCounter = 0;

    public static HashMap<String, Player> cachedPlayers = new HashMap<String, Player>();
    public static HashMap<String, Class<?>> cachedPlayerClasses = new HashMap<String, Class<?>>();
    public static Player[] players;

    public boolean sheltersInPlay = false;

    public int possessionsToProcess = 0;
    public Player possessingPlayer = null;
    public int nextPossessionsToProcess = 0;
    public Player nextPossessingPlayer = null;

    public static int numPlayers;
    boolean gameOver = false;

    private static HashMap<String, Player> playerCache = new HashMap<String, Player>();

    public static void main(String[] args) {
        try {
            go(args, false);
        } catch (ExitException e) {
            // This is what we would correctly need to do.
            // May break some code though which relies on vdom being less strict
            System.exit(-1);
        }
    }

    public static void go(String[] args, boolean html) throws ExitException {

        /*
         * Don't catch ExitException here. If someone throws an ExitException, it means the game is over, which should be handled by whoever owns us.
         */

        processArgs(args);

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
                for (int i = 0; i < 5; i++) {
                    gameType = GameType.RandomDarkAges;
                    new Game().start();
                }
                for (int i = 0; i < 5; i++) {
                    gameType = GameType.Random;
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


        FrameworkEvent frameworkEvent = new FrameworkEvent(FrameworkEvent.Type.AllDone);
        FrameworkEventHelper.broadcastEvent(frameworkEvent);
    }
    
    private static class ExtraTurnInfo {
		public ExtraTurnInfo() {}
		public ExtraTurnInfo(boolean canBuyCards) {
			this.canBuyCards = canBuyCards;
		}
		public boolean canBuyCards = true;
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
            gameCounter++;
            playersTurn = 0;
            turnCount = 1;
            Util.debug("Turn " + turnCount);
            
            Queue<ExtraTurnInfo> extraTurnsInfo = new LinkedList<ExtraTurnInfo>();

            while (!gameOver) {
                Player player = players[playersTurn];
                boolean canBuyCards = extraTurnsInfo.isEmpty() ? true : extraTurnsInfo.remove().canBuyCards;
                MoveContext context = new MoveContext(this, player, canBuyCards);
                context.startOfTurn = true;
                playerBeginTurn(player, context);
                context.startOfTurn = false;

                // /////////////////////////////////
                // Actions
                // /////////////////////////////////
                playerAction(player, context);


                // /////////////////////////////////
                // Select Treasure for Buy
                // /////////////////////////////////
                playTreasures(player, context, -1, null);

                // Spend Guilds coin tokens if applicable
                playGuildsTokens(player, context);

                // /////////////////////////////////
                // Buy Phase
                // /////////////////////////////////
                playerBuy(player, context);

                if (context.totalCardsBoughtThisTurn + context.totalEventsBoughtThisTurn == 0) {
                    GameEvent event = new GameEvent(GameEvent.Type.NoBuy, context);
                    broadcastEvent(event);
                    Util.debug(player.getPlayerName() + " did not buy a card with coins:" + context.getCoinAvailableForBuy());
                }

                // /////////////////////////////////
                // Discard, draw new hand
                // /////////////////////////////////
                player.cleanup(context);
                
                //clean up other players cards in play without future duration effects, e.g. Duplicate
                for (Player otherPlayer : getPlayersInTurnOrder()) {
                	if (otherPlayer != player) {
                		otherPlayer.cleanupOutOfTurn(new MoveContext(this, otherPlayer));
                	}
                }
                
                extraTurnsInfo.addAll(playerEndTurn(player, context));
                gameOver = checkGameOver();

                if (!gameOver) {
                    setPlayersTurn(!extraTurnsInfo.isEmpty());
                }
            }

            // unmask players
            maskPlayerNames = false;
            int vps[] = gameOver(gameTypeSpecificWins);
            if (test) {
                // Compute game stats
                turnCountTotal += turnCount;
                for (int i = 0; i < vps.length; i++) {
                    vpTotal += vps[i];
                    numCardsTotal += players[i].getAllCards().size();
                }
                totalCardCountGameEnd = totalCardCount();
                assert (totalCardCountGameBegin == totalCardCountGameEnd);
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
            for (AbstractCardPile pile : piles.values()) {
                Card card = pile.card();
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

    protected void setPlayersTurn(boolean takeAnotherTurn) {
        if (!takeAnotherTurn && consecutiveTurnCounter > 0) {
            // next player
            consecutiveTurnCounter = 0;
            if (++playersTurn >= numPlayers) {
                playersTurn = 0;
                Util.debug("Turn " + ++turnCount, true);
            }
        }
    }

    public int cardsInLowestPiles (int numPiles) {
        int[] ips = new int[piles.size() - 1 - (isColonyInGame() ? 1 : 0)];
        int count = 0;
        for (AbstractCardPile pile : piles.values()) {
            if (pile.card() != Cards.province && pile.card() != Cards.colony)
                ips[count++] = pile.getCount();
        }
        Arrays.sort(ips);
        count = 0;
        for (int i = 0; i < numPiles; i++) {
            count += ips[i];
        }
        return count;
    }

    protected void playTreasures(Player player, MoveContext context, int maxCards, Card responsible) {
    	// storyteller sets maxCards != -1
        if (!context.blackMarketBuyPhase && maxCards == -1) {
            context.buyPhase = true;
        }

        boolean selectingCoins = playerShouldSelectCoinsToPlay(context, player.getHand());
        if (maxCards != -1) selectingCoins = true;// storyteller
        ArrayList<TreasureCard> treasures = null;
        treasures = (selectingCoins) ? player.controlPlayer.treasureCardsToPlayInOrder(context, maxCards, responsible) : player.getTreasuresInHand();

        while (treasures != null && !treasures.isEmpty() && maxCards != 0) {
            while (!treasures.isEmpty() && maxCards != 0) {
                TreasureCard card = treasures.remove(0);
                if (player.hand.contains(card)) {// this is needed due to counterfeit which trashes cards during this loop
                    card.playTreasure(context);
                    maxCards--;
                }
            }
            if (maxCards != 0)
            	treasures = (selectingCoins) ? player.controlPlayer.treasureCardsToPlayInOrder(context, maxCards, responsible) : player.getTreasuresInHand();
        }
    }

    protected void playGuildsTokens(Player player, MoveContext context)
    {
        int coinTokenTotal = player.getGuildsCoinTokenCount();

        if (coinTokenTotal > 0)
        {
            // Offer the player the option of "spending" Guilds coin tokens prior to buying cards
            int numTokensToSpend = player.controlPlayer.numGuildsCoinTokensToSpend(context, coinTokenTotal, false/*!butcher*/);

            if (numTokensToSpend > 0 && numTokensToSpend <= coinTokenTotal)
            {
                player.spendGuildsCoinTokens(numTokensToSpend);
                context.addCoins(numTokensToSpend);
                if(numTokensToSpend > 0)
                {
                    GameEvent event = new GameEvent(GameEvent.Type.GuildsTokenSpend, context);
                    event.setComment(": " + numTokensToSpend);
                    context.game.broadcastEvent(event);
                }
                Util.debug(player, "Spent " + numTokensToSpend + " Guilds coin tokens");
            }
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

    protected List<ExtraTurnInfo> playerEndTurn(Player player, MoveContext context) {
        int handCount = 5;

        List<ExtraTurnInfo> result = new ArrayList<Game.ExtraTurnInfo>();
        // Can only have at most two consecutive turns
        for (Card card : player.nextTurnCards) {
            Card behaveAsCard = card.behaveAsCard();
            if (behaveAsCard.takeAnotherTurn()) {
                handCount = behaveAsCard.takeAnotherTurnCardCount();
                if (consecutiveTurnCounter <= 1) {
                	result.add(new ExtraTurnInfo());
                    break;
                }
            }
        }
        
        handCount += context.totalExpeditionBoughtThisTurn;

        // draw next hand
        for (int i = 0; i < handCount; i++) {
            drawToHand(context, null, handCount - i, false);
        }

        if (player.save != null) {
            player.hand.add(player.save);
            player.save = null;
        }
        	        	
        // /////////////////////////////////
        // Reset context for status update
        // /////////////////////////////////
        context.actionsPlayedSoFar = 0;
        context.actions = 1;
        context.buys = 1;
        context.coppersmithsPlayed = 0;
        
        GameEvent event = new GameEvent(GameEvent.Type.NewHand, context);
        broadcastEvent(event);
        event = null;

        // /////////////////////////////////
        // Turn End
        // /////////////////////////////////
        if (player.isPossessed()) {
            if (--possessionsToProcess == 0)
                player.controlPlayer = player;
        } else if (nextPossessionsToProcess > 0) {
            possessionsToProcess = nextPossessionsToProcess;
            possessingPlayer = nextPossessingPlayer;
            nextPossessionsToProcess = 0;
            nextPossessingPlayer = null;
        }
        event = new GameEvent(GameEvent.Type.TurnEnd, context);
        broadcastEvent(event);
        
        if (context.missionBought && consecutiveTurnCounter <= 1) {
			if (!result.isEmpty()) {
				//ask player if they want to do mission turn with three cards or do outpost turn first then mission turn
				// player not possessed because we are between turns
				
				//TODO: dominionator - integrate this with Possession turn logic
				ExtraTurnOption[] options = new ExtraTurnOption[]{ExtraTurnOption.OutpostFirst, ExtraTurnOption.MissionFirst};
				switch(player.extraTurn_chooseOption(context, options)) {
				case MissionFirst:
					result.get(0).canBuyCards = false;
					break;
				case OutpostFirst:
					result.add(new ExtraTurnInfo(false));
					break;
				}
			} else {
				result.add(new ExtraTurnInfo(false));
			}
		}
        
        return result;
    }

    protected void playerAction(Player player, MoveContext context) {
        // TODO move this check to action and buy (and others?)
        // if(player.hand.size() > 0)
        Card action = null;
        do {
            action = null;
            ArrayList<Card> actionCards = null;
            if (!actionChains || player.controlPlayer.isAi()) {
                action = player.controlPlayer.doAction(context);
                if (action != null) {
                    actionCards = new ArrayList<Card>();
                    actionCards.add(action);
                }
            } else {
                Card[] cs = player.controlPlayer.actionCardsToPlayInOrder(context);
                if (cs != null && cs.length != 0) {
                    actionCards = new ArrayList<Card>();
                    for (int i = 0; i < cs.length; i++) {
                        actionCards.add(cs[i]);
                    }
                }
            }

            while (context.actions > 0 && actionCards != null && !actionCards.isEmpty()) {
                action = actionCards.remove(0);
                if (action != null) {
                    if (isValidAction(context, action)) {
                        GameEvent event = new GameEvent(GameEvent.Type.Status, (MoveContext) context);
                        broadcastEvent(event);

                        try {
                            action.play(this, (MoveContext) context, true);
                        } catch (RuntimeException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Util.debug("Error:Invalid action selected");
                        // action = null;
                    }
                }
            }
        } while (context.actions > 0 && action != null);
    }

    protected void playerBuy(Player player, MoveContext context) {
        Card buy = null;
        do {
            try {
                buy = player.controlPlayer.doBuy(context);
            } catch (Throwable t) {
                Util.playerError(player, t);
            }

            if (buy != null) {
                if (isValidBuy(context, buy)) {
                	if(buy.isEvent()) {
                        context.totalEventsBoughtThisTurn++;
                	}
                	else
                	{
                        context.totalCardsBoughtThisTurn++;
                	}
                    GameEvent statusEvent = new GameEvent(GameEvent.Type.Status, (MoveContext) context);
                    broadcastEvent(statusEvent);

                    playBuy(context, buy);

                } else {
                    // TODO report?
                    buy = null;
                }
            }
        } while (context.buys > 0 && buy != null);
        

        //Discard Wine Merchants from Tavern
        if(context.getCoinAvailableForBuy() >= 2) {
        	int wineMerchants = 0;
            for (Card card : player.getTavern()) {
                if (Cards.wineMerchant.equals(card)) {
                	wineMerchants++;
                }
            }
            if (wineMerchants > 0) {
	            int wineMerchantsTotal = player.controlPlayer.cleanup_wineMerchantToDiscard(context, wineMerchants);
	            if (wineMerchants < 0 || wineMerchantsTotal > wineMerchants) {
	                Util.playerError(player, "Wine Merchant discard error, invalid number of Wine Merchants. Discarding all Wine Merchants.");
	                wineMerchantsTotal = wineMerchants;
	            }
	            if(wineMerchantsTotal > 0) {
	            	for (int i = 0; i < wineMerchantsTotal; i++) {
	            	    Card card = player.getTavern().get(Cards.wineMerchant);
	            	    player.getTavern().remove(card);
	                    player.discard(card, null, context, true, false); //set commandedDiscard=true and cleanup=false to force GameEvent 
	            	}
	            }
            }
        }
        
      //Discard Wine Merchants Estates from Tavern
        if(context.getCoinAvailableForBuy() >= 2) {
        	int wineMerchants = 0;
            for (Card card : player.getTavern()) {
                if (Cards.estate.equals(card) && Cards.wineMerchant.equals(card.behaveAsCard())) {
                	wineMerchants++;
                }
            }
            if (wineMerchants > 0) {
	            int wineMerchantsTotal = player.controlPlayer.cleanup_wineMerchantEstateToDiscard(context, wineMerchants);
	            if (wineMerchants < 0 || wineMerchantsTotal > wineMerchants) {
	                Util.playerError(player, "Wine Merchant estate discard error, invalid number of Wine Merchants. Discarding all Wine Merchants.");
	                wineMerchantsTotal = wineMerchants;
	            }
	            if(wineMerchantsTotal > 0) {
	            	for (int i = 0; i < wineMerchantsTotal; i++) {
	            	    Card card = player.getTavern().get(Cards.estate);
	            	    player.getTavern().remove(card);
	                    player.discard(card, null, context, true, false); //set commandedDiscard=true and cleanup=false to force GameEvent 
	            	}
	            }
            }
        }
        
        context.buyPhase = false;
    }

    @SuppressWarnings("unchecked")
	protected void playerBeginTurn(Player player, MoveContext context) {
        if (context.game.possessionsToProcess > 0) {
            player.controlPlayer = context.game.possessingPlayer;
        } else {
            player.controlPlayer = player;
            consecutiveTurnCounter++;
        }
        
        cardsObtainedLastTurn[playersTurn].clear();
        if (consecutiveTurnCounter == 1)
            player.newTurn();
        
        player.clearDurationEffectsOnOtherPlayers();
        
        GameEvent gevent = new GameEvent(GameEvent.Type.TurnBegin, context);
        broadcastEvent(gevent);

        /* Duration cards, horse traders, cards on prince*/
        
        /* selectOption() must know if horse traders are set aside by reaction or by haven/gear or by prince.
         * We put 2 cards in list durationEffects to differentiate:
         * Examples (Curse is here a dummy card):
         * HorseTrader - (Curse)
         * Haven - Card set aside by haven or gear
         * Gear - Card set aside by haven or gear
         * Prince - Card set aside by prince
         * other Durations like Wharf - (Curse)
         */
        boolean allDurationAreSimple = true;
        ArrayList<Object> durationEffects = new ArrayList<Object>();
        ArrayList<Boolean> durationEffectsAreCards = new ArrayList<Boolean>();
        for (Card card : player.nextTurnCards) {
            Card thisCard = card.behaveAsCard();
            if (thisCard.isDuration(player)) {
                /* Wiki:
                 * Effects that resolve at the start of your turn can be resolved in any order;
                 * this includes multiple plays of the same Duration card by a Throne Room variant.
                 * For example, if you played a Wharf and then a Throne Room on an Amulet last turn,
                 * on this turn you could choose to first gain a Silver from the first Amulet play,
                 * then draw 2 cards from Wharf (perhaps triggering a reshuffle and maybe drawing
                 * that Silver), and then choose to trash a card from the second Amulet play,
                 * now that you have more cards to choose from. 
                 */
            	int cloneCount = ((CardImpl) card).controlCard.cloneCount;
                for (int clone = cloneCount; clone > 0; clone--) {
                    if(   thisCard.equals(Cards.amulet)
                       || thisCard.equals(Cards.dungeon)) {
                        allDurationAreSimple = false;
                    }
                    if(thisCard.equals(Cards.haven)) {
                    	if(player.haven != null && player.haven.size() > 0) {
                    		durationEffects.add(card);
                    		durationEffects.add(player.haven.remove(0));
                    		durationEffectsAreCards.add(clone == cloneCount 
                    				&& !((CardImpl)card.behaveAsCard()).trashAfterPlay);
                    		durationEffectsAreCards.add(false);
                		}
                    } else if (thisCard.equals(Cards.gear)) {
                    	if(player.gear.size() > 0) {
                    		durationEffects.add(card);
                    		durationEffects.add(player.gear.remove(0));
                    		durationEffectsAreCards.add(clone == cloneCount 
                    				&& !((CardImpl)card.behaveAsCard()).trashAfterPlay);
                    		durationEffectsAreCards.add(false);
                    	}
                    } else {
                    	durationEffects.add(card);
                    	durationEffects.add(Cards.curse); /*dummy*/
                    	durationEffectsAreCards.add(clone == cloneCount
                    			&& !((CardImpl)card.behaveAsCard()).trashAfterPlay);
                		durationEffectsAreCards.add(false);
                    }
                }
            } else if(isModifierCard(thisCard.behaveAsCard())) {
                GameEvent event = new GameEvent(GameEvent.Type.PlayingDurationAction, context);
                event.card = card;
                event.newCard = true;
                broadcastEvent(event);
            }
        }
        for (Card card : player.horseTraders) {
            durationEffects.add(card);
            durationEffects.add(Cards.curse); /*dummy*/
            durationEffectsAreCards.add(true);
    		durationEffectsAreCards.add(false);
        }
        for (Card card : player.prince) {
            if (!card.equals(Cards.prince) && !card.equals(Cards.estate)) {
                allDurationAreSimple = false;
                durationEffects.add(Cards.prince);
                durationEffects.add(card);
                durationEffectsAreCards.add(true);
        		durationEffectsAreCards.add(false);
            }
        }
        for (Card card : player.summon) {
            if (!card.equals(Cards.summon)) {
                allDurationAreSimple = false;
                durationEffects.add(Cards.summon);
                durationEffects.add(card);
                durationEffectsAreCards.add(true);
        		durationEffectsAreCards.add(false);
            }
        }
        while (!player.haven.isEmpty()) {
        	/*gear could set 2 cards aside*/
        	// BUG: both cards set aside by Gear need to be added to hand at the same time
            durationEffects.add(Cards.gear);
            durationEffects.add(player.haven.remove(0));
            durationEffectsAreCards.add(false);
    		durationEffectsAreCards.add(false);
        }
        int numOptionalItems = 0;
        ArrayList<Card> callableCards = new ArrayList<Card>();
        for (Card c : player.tavern) {
        	if (c.behaveAsCard().isCallableWhenTurnStarts()) {
        		callableCards.add((Card) c);
        	}
        }
        if (!callableCards.isEmpty()) {
         	Collections.sort(callableCards, new Util.CardCostComparator());
 	        for (Card c : callableCards) {
 	        	if (c.behaveAsCard().equals(Cards.guide)
 	        		|| c.behaveAsCard().equals(Cards.ratcatcher)
 	        		|| c.behaveAsCard().equals(Cards.transmogrify)) {
 	        		allDurationAreSimple = false;
 	        	}
 	        }
        }
        if (!allDurationAreSimple) {
        	// Add cards callable at start of turn
        	for (Card c : callableCards) {
 	        	durationEffects.add(c);
 	        	durationEffects.add(Cards.curse);
 	        	durationEffectsAreCards.add(false);
 	    		durationEffectsAreCards.add(false);
 	        	numOptionalItems += 2;
 	        }
        }
        
        while (durationEffects.size() > numOptionalItems) {
        	int selection=0;
            if(allDurationAreSimple) {
            	selection=0;
            } else {
            	selection = 2*player.controlPlayer.duration_cardToPlay(context, durationEffects.toArray(new Object[durationEffects.size()]));
            }
            Card card = (Card) durationEffects.get(selection);
            boolean isRealCard = durationEffectsAreCards.get(selection);
            if (card == null) {
                Util.log("ERROR: duration_cardToPlay returned " + selection);
                selection=0;
                card = (Card) durationEffects.get(selection);
            }
            Card card2 = null;
            if (durationEffects.get(selection+1) instanceof Card) {
            	card2 = (Card) durationEffects.get(selection+1);
            }
            ArrayList<Card> gearCards = null;
            if (durationEffects.get(selection+1) instanceof ArrayList<?>) {
            	gearCards = (ArrayList<Card>) durationEffects.get(selection+1);
            }
            if (card2 == null) {
                Util.log("ERROR: duration_cardToPlay returned " + selection);
                card2 = card;
            }

            durationEffects.remove(selection+1);
            durationEffects.remove(selection);
            durationEffectsAreCards.remove(selection+1);
            durationEffectsAreCards.remove(selection);
            
            if(card.equals(Cards.prince)) {
                if (!(card2.isDuration(player))) {
                    player.playedByPrince.add(card2);
                }
                player.prince.remove(card2);
                
                context.freeActionInEffect++;
                try {
                    card2.play(this, context, false);
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
                context.freeActionInEffect--;
            } else if(card.equals(Cards.summon)) {
                player.summon.remove(card2);
                
                context.freeActionInEffect++;
                try {
                    card2.play(this, context, false);
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
                context.freeActionInEffect--;
            } else if(card.behaveAsCard().equals(Cards.horseTraders)) {
            	//BUG: this doesn't let you call estates inheriting horse trader differently
                Card horseTrader = player.horseTraders.remove(0);
                player.hand.add(horseTrader);
                drawToHand(context, horseTrader, 1);
            } else if(card.behaveAsCard().isDuration(player)) {
                if(card.behaveAsCard().equals(Cards.haven)) {
                    player.hand.add(card2);
                }
                if(card.behaveAsCard().equals(Cards.gear)) {
                	for (Card c : gearCards)
                		player.hand.add(c);
                }
                
                
                Card thisCard = card.behaveAsCard();
                
                GameEvent event = new GameEvent(GameEvent.Type.PlayingDurationAction, context);
                event.card = card;
                event.newCard = isRealCard;
                broadcastEvent(event);

                context.actions += thisCard.getAddActionsNextTurn();
                context.addCoins(thisCard.getAddGoldNextTurn());
                context.buys += thisCard.getAddBuysNextTurn();
                int addCardsNextTurn = thisCard.getAddCardsNextTurn();

                /* addCardsNextTurn are displayed like addCards but sometimes the text differs */
                if (thisCard.getType() == Cards.Type.Tactician) {
                    context.actions += 1;
                    context.buys += 1;
                    addCardsNextTurn = 5;
                }
                if (thisCard.getType() == Cards.Type.Dungeon) {
                    addCardsNextTurn = 2;
                }
                if (thisCard.getType() == Cards.Type.Hireling) {
                    addCardsNextTurn = 1;
                }

                for (int i = 0; i < addCardsNextTurn; i++) {
                    drawToHand(context, thisCard, addCardsNextTurn - i, true);
                }
                
                if (   thisCard.getType() == Cards.Type.Amulet
                	|| thisCard.getType() == Cards.Type.Dungeon ) {
                    context.freeActionInEffect++;
                    try {
                        ((ActionCardImpl) thisCard).additionalCardActions(context.game, context, player);
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                    }
                    context.freeActionInEffect--;
                }                
            } else if(card.behaveAsCard().isCallableWhenTurnStarts()) {
            	numOptionalItems -= 2;
            	card.behaveAsCard().callAtStartOfTurn(context);
            } else {
                Util.log("ERROR: nextTurnCards contains " + card);
            }
        }
        
        ArrayList<Card> staysInPlayCards = new ArrayList<Card>();
        while (!player.nextTurnCards.isEmpty()) {
            Card card = player.nextTurnCards.remove(0);
        	if(isModifierCard(card.behaveAsCard())) {
                if(!player.nextTurnCards.isEmpty()) {
                  	Card nextCard = player.nextTurnCards.get(0);
                  	int additionalModifierCards = 0;
                  	while (nextCard != null && isModifierCard(nextCard.behaveAsCard())) {
                  		additionalModifierCards++;
                  		if (player.nextTurnCards.size() > additionalModifierCards)
                  			nextCard = player.nextTurnCards.get(additionalModifierCards);
                  		else
                  			nextCard = null;
                  	}
                    if(nextCard != null && (nextCard.behaveAsCard().equals(Cards.hireling) || nextCard.behaveAsCard().equals(Cards.champion))) {
                    	staysInPlayCards.add(card);
                    	for (int i = 0; i < additionalModifierCards; ++i) {
                    		staysInPlayCards.add(player.nextTurnCards.remove(0));
                    	}
                        player.nextTurnCards.remove(0);
                        staysInPlayCards.add(nextCard);
                    	continue;
                    }
                }
            }
        	
        	if(card.behaveAsCard().equals(Cards.hireling) || card.behaveAsCard().equals(Cards.champion)) {
        		staysInPlayCards.add(card);
            } else {
	            CardImpl behaveAsCard = (CardImpl) card.behaveAsCard();
	            behaveAsCard.cloneCount = 1;
	            ((CardImpl)card).cloneCount = 1;
	            if (!(behaveAsCard.trashAfterPlay || ((CardImpl)card).trashAfterPlay)) {
	                player.playedCards.add(card);
	            } else {
	                behaveAsCard.trashAfterPlay = false;
	                ((CardImpl)card).trashAfterPlay = false;
	            }
            }
        }
        while (!staysInPlayCards.isEmpty()) {
            player.nextTurnCards.add(staysInPlayCards.remove(0));
        }
        
        //TODO: Dominionator - Will require tracking duration effects independent of cards
        //       to do correctly or replacing real card with a dummy card - do this later.
        
        //TODO: integrate this into the main action selection UI if possible to make it more seamless
        //check for start-of-turn callable cards
        callableCards = new ArrayList<Card>();
        Card toCall = null;
        for (Card c : player.tavern) {
        	if (c.behaveAsCard().isCallableWhenTurnStarts()) {
        		callableCards.add(c);
        	}
        }
        if (!callableCards.isEmpty()) {
        	Collections.sort(callableCards, new Util.CardCostComparator());
	        do {
	        	toCall = null;
	        	// we want null entry at the end for None
	        	Card[] cardsAsArray = callableCards.toArray(new Card[callableCards.size() + 1]);
	        	//ask player which card to call
	        	toCall = player.controlPlayer.call_whenTurnStartCardToCall(context, cardsAsArray);
	        	if (toCall != null && callableCards.contains(toCall)) {
	        		toCall = callableCards.remove(callableCards.indexOf(toCall));
	        		toCall.behaveAsCard().callAtStartOfTurn(context);
	        	}
		        // loop while we still have cards to call
	        } while (toCall != null && !callableCards.isEmpty());
        }
    }

    public static boolean isModifierCard(Card card) {
		return card.equals(Cards.throneRoom)
	               || card.equals(Cards.disciple)
	               || card.equals(Cards.kingsCourt)
	               || card.equals(Cards.procession)
	               || card.equals(Cards.royalCarriage);
	}

	private static void printStats(HashMap<String, Double> wins, int gameCount, String gameType) {
        if (!test || gameCount == 1) {
            return;
        }

        double totalGameCount = 0;
        Iterator<Entry<String, Double>> it = wins.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, Double> e = it.next();
            totalGameCount += e.getValue();
        }
        gameCount = (int) totalGameCount;

        StringBuilder sb = new StringBuilder();

        String s = gameType + ":";

        String start = "" + gameCount;

        if (gameCount > 1) {
            s = start + (gameType.equals("Types") ? " types " : " games ") + s;
        }

        if (!debug) {
            while (s.length() < 30) {
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
            Player pclass = playerCache.get(className);
            if (pclass != null) {
                name = pclass.getPlayerName();
            } else {
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
            sb.append(name + " " + numStr + "%");

            winStats.put(name, Integer.parseInt(numStr.trim()));
        }

        Util.log(sb.toString());
    }

    private static void printGameTypeStats() {
        for (int i=0; i < gameTypeStats.size(); i++) {
            GameStats stats = gameTypeStats.get(i);
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
        int playerVictoryPoints = -999;
        int otherHigh = -999;

        int[] vps = calculateVps();
        for (int i = 0; i < vps.length; i++) {
            if (players[i].equals(player)) {
                playerVictoryPoints = vps[i];
            } else if (vps[i] > otherHigh) {
                otherHigh = vps[i];
            }
        }

        return playerVictoryPoints - otherHigh;
    }

    private static int[] calculateVps() {
        int[] vps = new int[numPlayers];
        for (int i = 0; i < players.length; i++) {
            vps[i] = players[i].getVPs();
        }

        return vps;
    }

    protected static void processArgs(String[] args) throws ExitException {
        // dont remove tabs in following to keep easy upstream-mergeability
        processNewGameArgs(args);
        processUserPrefArgs(args);
    }

    protected static void processNewGameArgs(String[] args) throws ExitException {
        numPlayers = 0;
        cardsSpecifiedAtLaunch = null;
        overallWins.clear();
        GAME_TYPE_WINS.clear();
        gameTypeStats.clear();
        playerClassesAndJars.clear();
        playerCache.clear();
        numRandomEvents = 0;
        randomIncludesEvents = false;
        randomExpansions = null;
        
        String gameCountArg = "-count";
        String debugArg = "-debug";
        String showEventsArg = "-showevents";
        String gameTypeArg = "-type";
        String numRandomEventsArg = "-eventcards";
        String gameTypeStatsArg = "-test";
        String ignorePlayerErrorsArg = "-ignore";
        String showPlayersArg = "-showplayers";
        String siteArg = "-site=";
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
                } else if (arg.toLowerCase().startsWith(numRandomEventsArg)) {
                    try {
                        int num = Integer.parseInt(arg.substring(numRandomEventsArg.length()));
                        if (num != 0) {
                        	randomIncludesEvents = true;
                        	numRandomEvents = num;
                        }
                    } catch (Exception e) {
                        Util.log(e);
                        throw new ExitException();
                    }
                } else if (arg.toLowerCase().startsWith(gameTypeArg)) {
                    try {
                        gameTypeStr = arg.substring(gameTypeArg.length());
                        String[] parts = gameTypeStr.split("-");
                        if (parts.length > 0) {
                        	gameTypeStr = parts[0];
                        	randomExpansions = new ArrayList<Expansion>();
                    		for (int i = 1; i < parts.length; ++i) {
                        		randomExpansions.add(Expansion.valueOf(parts[i]));
                        	}
                        }
                    } catch (Exception e) {
                        Util.log(e);
                        throw new ExitException();
                    }
                    //                } else {
                    //                    Util.log("Invalid arg:" + arg);
                    //                    showUsage = true;
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

        if (numPlayers < 1 || numPlayers > 6 || showUsage) {
            Util.log("Usage: [-debug][-ignore(playername)][-count(# of Games)][-type(Game type)] class1 class2 [class3] [class4]");
            throw new ExitException();
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

    public static void processUserPrefArgs(String[] args) throws ExitException {
        quickPlay = false;
        sortCards = false;
        maskPlayerNames = false;
        actionChains = false;
        suppressRedundantReactions = false;
        chanceForPlatColony = -1;
        chanceForShelters = -1;
        equalStartHands = false;
        startGuildsCoinTokens = false; //only for testing
        lessProvinces = false; //only for testing

        String quickPlayArg = "-quickplay";
        String maskPlayerNamesArg = "-masknames";
        String sortCardsArg = "-sortcards";
        String actionChainsArg = "-actionchains";
        String suppressRedundantReactionsArg = "-suppressredundantreactions";
        String platColonyArg = "-platcolony";
        String useSheltersArg = "-useshelters";
        String blackMarketCountArg = "-blackmarketcount";
        String equalStartHandsArg = "-equalstarthands";
        String startGuildsCoinTokensArg = "-startguildscointokens"; //only for testing
        String lessProvincesArg = "-lessprovinces"; //only for testing

        for (String arg : args) {
            if (arg == null) {
                continue;
            }

            if (arg.startsWith("#")) {
                continue;
            }

            if (arg.startsWith("-")) {
                if (arg.toLowerCase().equals(quickPlayArg)) {
                    quickPlay = true;
                } else if (arg.toLowerCase().equals(sortCardsArg)) {
                    sortCards = true;
                } else if (arg.toLowerCase().equals(maskPlayerNamesArg)) {
                    maskPlayerNames = true;
                } else if (arg.toLowerCase().equals(actionChainsArg)) {
                    actionChains = true;
                } else if (arg.toLowerCase().equals(suppressRedundantReactionsArg)) {
                    suppressRedundantReactions = true;
                } else if (arg.toLowerCase().startsWith(platColonyArg)) {
                	chanceForPlatColony = Integer.parseInt(arg.toLowerCase().substring(platColonyArg.length())) / 100.0;
                } else if (arg.toLowerCase().startsWith(useSheltersArg)) {
                	chanceForShelters = Integer.parseInt(arg.toLowerCase().substring(useSheltersArg.length())) / 100.0;
                } else if (arg.toLowerCase().startsWith(blackMarketCountArg)) {
                    blackMarketCount = Integer.parseInt(arg.toLowerCase().substring(blackMarketCountArg.length()));
                } else if (arg.toLowerCase().equals(equalStartHandsArg)) {
                    equalStartHands = true;
                } else if (arg.toLowerCase().equals(startGuildsCoinTokensArg)) {
                    startGuildsCoinTokens = true; //only for testing
                } else if (arg.toLowerCase().equals(lessProvincesArg)) {
                    lessProvinces = true; //only for testing
                }
            }
        }
    }

    public boolean isValidAction(MoveContext context, Card action) {
        if (action == null) {
            return false;
        }

        if (!(action.isAction(context.player))) {
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

        AbstractCardPile thePile = getPile(card);
        if (thePile == null) {
            return false;
        }
        if (!context.canBuyCards && !card.isEvent()) {
        	return false;
        }
        if (context.blackMarketBuyPhase) {
            if (thePile.isBlackMarket() == false) {
                return false;
            }
            if (Cards.isSupplyCard(card)) {
                return false;
            }
        }
        else if (card.isEvent() && !context.buyPhase) {
        	return false;
        }
        else if (!card.isEvent()) {
            if (thePile.isSupply() == false) {
                return false;
            }
            if (!Cards.isSupplyCard(card)) {
                return false;
            }
        }

        if (isPileEmpty(card)) {
            return false;
        }

        if (context.cantBuy.contains(card)) {
            return false;
        }

        if (card.equals(Cards.grandMarket) && (context.countCardsInPlay(Cards.copper) > 0)) {
            return false;
        }

        int cost = card.getCost(context, !context.blackMarketBuyPhase);

        int potions = context.getPotions();
        if (cost <= gold && (!card.costPotion() || potions > 0)) {
            return true;
        }

        return false;
    }

    Card playBuy(MoveContext context, Card buy) {
        Player player = context.getPlayer();
        if (!context.blackMarketBuyPhase) {
            context.buys--;
        }

        int embargos = getEmbargos(buy);
        for (int i = 0; i < embargos; i++) {
            player.gainNewCard(Cards.curse, Cards.embargo, context);
        }        
        
        Card card = buy;
        if(!buy.isEvent()) {
            card = takeFromPileCheckTrader(buy, context);
        }

        /* GameEvent.Type.BuyingCard must be after overpaying! */
        
        // cost adjusted based on any cards played or card being bought
        int cost = buy.getCost(context);

        // If card can be overpaid for, do so now
        if (buy.isOverpay(player))
        {
            int coinOverpay = player.amountToOverpay(context, buy, cost);
            coinOverpay = Math.max(0,  coinOverpay);
            coinOverpay = Math.min(coinOverpay, context.getCoinAvailableForBuy());
            context.overpayAmount = coinOverpay;

            if (context.potions > 0)
            {
            	int potionOverpay = player.overpayByPotions(context, context.potions);
            	potionOverpay = Math.max(0, potionOverpay);
            	potionOverpay = Math.min(potionOverpay, context.getPotions());
                context.overpayPotions = potionOverpay;
                context.potions -= context.overpayPotions;
            }

            if (context.overpayAmount > 0 || context.overpayPotions > 0)
            {
                GameEvent event = new GameEvent(GameEvent.Type.OverpayForCard, (MoveContext) context);
                event.card = card;
                event.newCard = true;
                broadcastEvent(event);
            }
        }
        else
        {
            context.overpayAmount  = 0;
            context.overpayPotions = 0;
        }
        
        buy.isBuying(context);
        
        if(!buy.isEvent()) {
        	if (player.getHand().size() > 0 && isPlayerSupplyTokenOnPile(buy, player, PlayerSupplyToken.Trashing)) {
                Card cardToTrash = player.controlPlayer.trashingToken_cardToTrash((MoveContext) context);
                if (cardToTrash != null) {
                	if (!player.getHand().contains(cardToTrash)) {
                		Util.playerError(player, "Trashing token error, invalid card to trash, ignoring.");
                	} else {
                		player.hand.remove(cardToTrash);
                		player.trash(cardToTrash, null, context);
                	}
                }
        	}
        	
	        for (int i=0; i < swampHagAttacks(player); i++) {
	            player.gainNewCard(Cards.curse, Cards.swampHag, context);                    	
	        }
	        
	        if (hauntedWoodsAttacks(player)) {
	            if(player.hand.size() > 0) {
	                Card[] order;
	                if (player.hand.size() == 1)
	                    order = player.hand.toArray();
	                else
	                    order = player.controlPlayer.mandarin_orderCards(context, player.hand.toArray());
	
	                for (int i = order.length - 1; i >= 0; i--) {
	                    Card c = order[i];
	                    player.putOnTopOfDeck(c);
	                    player.hand.remove(c);
	                }                            
	            }
	        }
        }
        
        if (card != null) {
            GameEvent event = new GameEvent(GameEvent.Type.BuyingCard, (MoveContext) context);
            event.card = card;
            event.newCard = true;
            broadcastEvent(event);

            // Swap in the real knight
            if (buy.equals(Cards.virtualKnight)) {
                buy = card;
            }
        }

        context.spendCoins(buy.getCost(context) + context.overpayAmount);

        if (buy.costPotion()) {
            context.potions--;
        } else if (!(buy instanceof VictoryCard) && !buy.isKnight(null) && cost < 5 && !buy.isEvent()) {
            for (int i = 1; i <= context.countCardsInPlay(Cards.talisman); i++) {
                if (!buy.isRuins(null) || (card != null && card.equals(getTopRuinsCard()))) {
                    context.getPlayer().gainNewCard(buy, Cards.talisman, context);
                }
            }
        }

        if(!buy.isEvent()) {
            player.addVictoryTokens(context, context.countGoonsInPlayThisTurn());
        }

        if (!buy.isEvent() && context.countMerchantGuildsInPlayThisTurn() > 0)
        {
            player.gainGuildsCoinTokens(context.countMerchantGuildsInPlayThisTurn());
            GameEvent event   = new GameEvent(GameEvent.Type.GuildsTokenObtained, context);
            broadcastEvent(event);
        }

        if (buy instanceof VictoryCard) {
            context.victoryCardsBoughtThisTurn++;
            for (int i = 1; i <= context.countCardsInPlay(Cards.hoard); i++) {
                player.gainNewCard(Cards.gold, Cards.hoard, context);
            }
        }

        buy.isBought(context);
        if(!buy.isEvent()) {
        	haggler(context, buy);
        }
        
        return card;
    }

    private void haggler(MoveContext context, Card cardBought) {
        if(!context.game.piles.containsKey(Cards.haggler.getName()))
            return;
        int hagglers = context.countCardsInPlay(Cards.haggler);

        int cost = cardBought.getCost(context);
        boolean potion = cardBought.costPotion();
        List<Card> validCards = new ArrayList<Card>();

        for (int i = 0; i < hagglers; i++) {
            validCards.clear();
            for (Card card : getCardsInGame()) {
                if (!(card instanceof VictoryCard) && Cards.isSupplyCard(card) && getCardsLeftInPile(card) > 0) {
                    int gainCardCost = card.getCost(context);
                    boolean gainCardPotion = card.costPotion();

                    if (gainCardCost < cost || (gainCardCost == cost && !gainCardPotion && potion)) {
                        validCards.add(card);
                    }
                }
            }

            if (validCards.size() > 0) {
                Card toGain = context.getPlayer().controlPlayer.haggler_cardToObtain(context, cost - 1, potion);
                if(toGain != null) {
                    if (!validCards.contains(toGain)) {
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
        if (isColonyInGame() && card.equals(Cards.colony)) {
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
        if (isColonyInGame() && isPileEmpty(Cards.colony)) {
            return true;
        }

        if (isPileEmpty(Cards.province)) {
            return true;
        }

        switch (numPlayers) {
            case 1:
            case 2:
            case 3:
            case 4:
                /* Ends game for 1, 2, 3 or 4 players */
                if (emptyPiles() >= 3) {
                    return true;
                }
                break;
            case 5:
            case 6:
                /* Ends game for 5 or 6 players */
                if (emptyPiles() >= 4) {
                    return true;
                }
        }

        return false;
    }

    // Use drawToHand when "drawing" or "+ X cards" when -1 Card token could be drawn instead
    boolean drawToHand(MoveContext context, Card responsible, int cardsLeftToDraw) {
        return drawToHand(context, responsible, cardsLeftToDraw, true);
    }

    boolean drawToHand(MoveContext context, Card responsible, int cardsLeftToDraw, boolean showUI) {
    	Player player = context.player;
    	if (player.getMinusOneCardToken()) {
    		player.setMinusOneCardToken(false, context);
    		return true;
    	}
        Card card = draw(context, responsible, cardsLeftToDraw);
        if (card == null)
            return false;

        if (responsible != null) {
            Util.debug(player, responsible.getName() + " draw:" + card.getName(), true);
        }

        player.hand.add(card, showUI);

        return true;
    }

    // Use draw when removing a card from the top of the deck without "drawing" it (e.g. look at or reveal)
    Card draw(MoveContext context, Card responsible, int cardsLeftToDraw) {
    	if (context.player.deck.isEmpty()) {
            if (context.player.discard.isEmpty()) {
                return null;
            } else {
                replenishDeck(context, responsible, cardsLeftToDraw);
            }
        }

        return context.player.deck.remove(0);
    }

    public void replenishDeck(MoveContext context, Card responsible, int cardsLeftToDraw) {
        context.player.replenishDeck(context, responsible, cardsLeftToDraw);
        
        GameEvent event = new GameEvent(GameEvent.Type.DeckReplenished, context);
        broadcastEvent(event);
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

    @SuppressWarnings("unchecked")
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
        playersTurn = 0;

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
                //String options = playerStartupInfo[3];
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
            
            if (platColonyPassedIn || chanceForPlatColony > 0.9999) {
                s += "Platinum/Colony included...\n";
            } else if (platColonyNotPassedIn || Math.round(chanceForPlatColony * 100) == 0) {
                s += "Platinum/Colony not included...\n";
            } else {
                s += "Chance for Platinum/Colony\n   " + (Math.round(chanceForPlatColony * 100)) + "% ... " + (isPlatInGame() ? "included\n" : "not included\n");
            }

            if (baneCard != null) {
                s += "Bane card: " + baneCard.getName() + "\n";
            }

            // When Baker is included in the game, each Player starts with 1 coin token
            if (bakerInPlay)
            {
                players[i].gainGuildsCoinTokens(1);
            }
            //only for testing
            if (startGuildsCoinTokens && !players[i].isAi())
            {
                players[i].gainGuildsCoinTokens(99);
            }

            /* The journey token is face up at the start of a game.
             * It can be turned over by Ranger, Giant and Pilgrimage.
             */
            if (journeyTokenInPlay)
            {
                players[i].flipJourneyToken(null);
            }

            if (sheltersPassedIn || chanceForShelters > 0.9999) {
                s += "Shelters included...\n";
            } else if (sheltersNotPassedIn || Math.round(chanceForShelters * 100) == 0) {
                s += "Shelters not included...\n";
            }
            else {
                s += "Chance for Shelters\n   " + (Math.round(chanceForShelters * 100)) + "% ... " + (sheltersInPlay ? "included\n" : "not included\n");
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
            
            if (sheltersInPlay)
            {
                player.discard(takeFromPile(Cards.necropolis), null, null);
                player.discard(takeFromPile(Cards.overgrownEstate), null, null);
                player.discard(takeFromPile(Cards.hovel), null, null);

                // Also need to remove the Estates that were put in the pile prior to
                // determining if Shelters would be used
                takeFromPile(Cards.estate);
                takeFromPile(Cards.estate);
                takeFromPile(Cards.estate);
            }
            else
            {
                player.discard(takeFromPile(Cards.estate), null, null);
                player.discard(takeFromPile(Cards.estate), null, null);
                player.discard(takeFromPile(Cards.estate), null, null);
            }

            if (!equalStartHands || i == 0) {
                while (player.hand.size() < 5)
                    drawToHand(new MoveContext(this, player), null, 5 - player.hand.size(), false);
            }
            else {
                // make subsequent player hands equal
                for (int j = 0; j < 5; j++) {
                    Card card = players[0].hand.get(j);
                    player.discard.remove(card);
                    player.hand.add(card);
                }
                player.replenishDeck(null, null, 0);
            }
        }

        if (startGuildsCoinTokens) //only for testing
        {
            for (int i = 0; i < numPlayers; i++) {
                player = players[i];
                if (player.isAi())
                {
                    continue;
                }
                player.hand.clear();
                player.deck.clear();
                player.discard.clear();
            }
        }
        
        // Add tradeRoute tokens if tradeRoute in play
        tradeRouteValue = 0;
        if (isCardInGame(Cards.tradeRoute)) {
            for (AbstractCardPile pile : piles.values()) {
                if ((pile.card() instanceof VictoryCard) && !pile.card().isKnight(null) && !pile.isBlackMarket()) {
                    pile.setTradeRouteToken();
                }
            }
        }

    }

    protected void initCards() {
        piles.clear();
        embargos.clear();
        playerSupplyTokens.clear();
        trashPile.clear();
        blackMarketPile.clear();
        blackMarketPileShuffled.clear();

        platColonyNotPassedIn = false;
        platColonyPassedIn = false;
        sheltersNotPassedIn = false;
        sheltersPassedIn = false;

        int provincePileSize = -1;
        int curseCount = -1;
        int treasureMultiplier = 1;

        switch (numPlayers) {
            case 1:
            case 2:
                curseCount = 10;
                provincePileSize = 8;
                victoryCardPileSize = 8;
                break;
            case 3:
                curseCount = 20;
                provincePileSize = 12;
                victoryCardPileSize = 12;
                break;
            case 4:
                curseCount = 30;
                provincePileSize = 12;
                victoryCardPileSize = 12;
                break;
            case 5:
                curseCount = 40;
                provincePileSize = 15;
                victoryCardPileSize = 12;
                treasureMultiplier = 2;
                break;
            case 6:
                curseCount = 50;
                provincePileSize = 18;
                victoryCardPileSize = 12;
                treasureMultiplier = 2;
                break;
        }
        //only for testing
        if (lessProvinces)
        {
            provincePileSize = 1; 
        }

        addPile(Cards.gold, 30 * treasureMultiplier);
        addPile(Cards.silver, 40 * treasureMultiplier);
        addPile(Cards.copper, 60 * treasureMultiplier);

        addPile(Cards.curse, curseCount);
        addPile(Cards.province, provincePileSize);
        addPile(Cards.duchy, victoryCardPileSize);
        addPile(Cards.estate, victoryCardPileSize + (3 * numPlayers));

        unfoundCards.clear();
        int added = 0;

        if(cardsSpecifiedAtLaunch != null) {
            platColonyNotPassedIn = true;
            sheltersNotPassedIn = true;
            for(String cardName : cardsSpecifiedAtLaunch) {
                Card card = null;
                boolean bane = false;
                boolean blackMarket = false;

                if(cardName.startsWith(BANE)) {
                    bane = true;
                    cardName = cardName.substring(BANE.length());
                }
                if(cardName.startsWith(BLACKMARKET)) {
                    blackMarket = true;
                    cardName = cardName.substring(BLACKMARKET.length());
                }
                String s = cardName;
                for (Card c : Cards.actionCards) {
                    if(c.getSafeName().equalsIgnoreCase(s)) {
                        card = c;
                        break;
                    }
                }
                for (Card c : Cards.eventsCards) {
                    if(c.getSafeName().equalsIgnoreCase(s)) {
                        card = c;
                        break;
                    }
                }
                if(card != null && bane) {
                    baneCard = card;
                }
                if(card != null && blackMarket) {
                    blackMarketPile.add(card);
                }
                if (cardName.equalsIgnoreCase("Knights")) {
                    card = Cards.virtualKnight;
                }

                if(card != null) {
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
                } else if (s.equalsIgnoreCase("Shelter") ||
                			s.equalsIgnoreCase("Shelters") ||
                           s.equalsIgnoreCase(Cards.hovel.getSafeName()) ||
                           s.equalsIgnoreCase(Cards.overgrownEstate.getSafeName()) ||
                           s.equalsIgnoreCase(Cards.necropolis.getSafeName()) ) {
                    sheltersPassedIn = true;
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

                if(replacementCost != -1) {
                    ArrayList<Card> cardsWithSameCost = new ArrayList<Card>();
                    for (Card card : Cards.actionCards) {
                        if(card.getCost(null) == replacementCost && !cardInGame(card)) {
                            cardsWithSameCost.add(card);
                        }
                    }

                    if(cardsWithSameCost.size() > 0) {
                        c = cardsWithSameCost.get(rand.nextInt(cardsWithSameCost.size()));
                    }
                }

                while(c == null) {
                    c = Cards.actionCards.get(rand.nextInt(Cards.actionCards.size()));
                    if(cardInGame(c)) {
                        c = null;
                    }
                }

                addPile(c);
                added += 1;
            }

            gameType = GameType.Specified;
        } else {
            CardSet cardSet = CardSet.getCardSet(gameType, -1, randomExpansions, randomIncludesEvents, numRandomEvents, true);
            if(cardSet == null) {
                cardSet = CardSet.getCardSet(CardSet.defaultGameType, -1);
            }

            for(Card card : cardSet.getCards()) {
                this.addPile(card);
            }

            if(cardSet.getBaneCard() != null) {
                this.baneCard = cardSet.getBaneCard();
                //Adding the bane card could probably be done in the CardSet class, but it seems better to call it out explicitly.
                this.addPile(this.baneCard);
            }
        }

        // Black Market
        Cards.blackMarketCards.clear();
        if (piles.containsKey(Cards.blackMarket.getName()))
        {
            // get 10 cards more then needed. Extract the cards in supply
            int count = Math.max(blackMarketCount - blackMarketPile.size(), 0);
            List<Card> allCards = CardSet.getCardSet(GameType.Random, count+10).getCards();
            List<Card> remainingCards = new ArrayList<Card>();
            for (int i = 0; i < allCards.size(); i++) {
                if (!piles.containsKey(allCards.get(i).getName())) {
                	remainingCards.add(allCards.get(i));
                }
            }
            // take count cards from the rest
            List<Card> cards = CardSet.getRandomCardSet(remainingCards, count).getCards();
            for (int i = 0; i < cards.size(); i++) {
            	remainingCards.remove(cards.get(i));
                blackMarketPile.add(cards.get(i));
            }
            if (blackMarketPile.contains(Cards.virtualKnight)) {
                // pick one real knight
                blackMarketPile.remove(Cards.virtualKnight);
                blackMarketPile.add(Cards.knightsCards.get(Game.rand.nextInt(Cards.knightsCards.size())));
            }
            if (this.baneCard == null && blackMarketPile.contains(Cards.youngWitch)) {
            	this.baneCard = CardSet.getBaneCard(remainingCards);
            	this.addPile(this.baneCard);
            }
            // sort
            Collections.sort(blackMarketPile, new Util.CardCostNameComparator());
            // put all in piles
            cards.clear();
            for (int i = 0; i < blackMarketPile.size(); i++) {
                cards.add(blackMarketPile.get(i));
                addPile(blackMarketPile.get(i), 1, false, true);
                Cards.blackMarketCards.add(blackMarketPile.get(i));
            }
            // shuffle
            while (cards.size() > 0) {
                blackMarketPileShuffled.add(cards.remove(Game.rand.nextInt(cards.size())));
            }
        }        
        
        if (piles.containsKey(Cards.virtualKnight.getName())) {
            VariableCardPile kp = (VariableCardPile) this.getPile(Cards.virtualKnight);
            for (Card k : Cards.knightsCards) {
                kp.addLinkedPile((SingleCardPile) addPile(k, 1, false));
            }


        }

        //determine shelters & plat/colony use
        boolean alreadyCountedKnights = false;
        int darkAgesCards = 0;
        int prosperityCards = 0;
        int kingdomCards = 0;
        for (AbstractCardPile pile : piles.values()) {
            if (pile != null &&
            		pile.card() != null &&
            		pile.card().getExpansion() != null &&
            		Cards.isKingdomCard(pile.card())) {
            	kingdomCards++;
            	if (pile.card().isRuins(null) == false &&
                        (pile.card().isKnight(null) == false || !alreadyCountedKnights) &&
                        pile.card().getExpansion().equals("DarkAges")) {
                    darkAgesCards++;
                    if (pile.card().isKnight(null)) {
                        alreadyCountedKnights = true;
                    }
                }
            	if (pile.card().getExpansion().equals("Prosperity")) {
                    prosperityCards++;
                }
            }
        }

        sheltersInPlay = false;
        if (sheltersPassedIn) {
            sheltersInPlay = true;
            chanceForShelters = 1;
        } else if (chanceForShelters > -0.0001) { 
        	sheltersInPlay = rand.nextDouble() < chanceForShelters;
        } else {
            chanceForShelters = darkAgesCards / (double)kingdomCards;

            if (rand.nextDouble() < chanceForShelters)
            {
                sheltersInPlay = true;
            }
        }

        if (sheltersInPlay) {
            addPile(Cards.necropolis, numPlayers, false);
            addPile(Cards.overgrownEstate, numPlayers, false);
            addPile(Cards.hovel, numPlayers, false);
        }


        // Check for PlatColony
        boolean addPlatColony = false;
        if (platColonyPassedIn) {
            addPlatColony = true;
        } else if (chanceForPlatColony > -0.0001) {
            addPlatColony = rand.nextDouble() < chanceForPlatColony;
        } else {
            chanceForPlatColony = prosperityCards / (double)kingdomCards;
            
            if (rand.nextDouble() < chanceForPlatColony) {
                addPlatColony = true;
            }
        }

        if (addPlatColony) {
            addPile(Cards.platinum, 12);
            addPile(Cards.colony);
        }

        // Add the potion if there are any cards that need them.
        for (AbstractCardPile pile : piles.values()) {
            if (pile.card().costPotion()) {
                addPile(Cards.potion, 16);
                break;
            }
        }

        // We have to add one "invisible" pile for each ruins card and a "virtual" visible pile
        boolean looter = false;
        for (AbstractCardPile pile : piles.values()) {
            if (pile.card().isLooter()) {
                looter = true;
            }
        }
        if (looter) {
            VariableCardPile rp = (VariableCardPile) this.addPile(Cards.virtualRuins, (numPlayers * 10) - 10);
            for (Card r : Cards.ruinsCards) {
                rp.addLinkedPile((SingleCardPile) this.addPile(r, 10, false));
            }
        }


        if (piles.containsKey(Cards.tournament.getName()) && !piles.containsKey(Cards.bagOfGold.getName())) {
            addPile(Cards.bagOfGold, 1, false);
            addPile(Cards.diadem, 1, false);
            addPile(Cards.followers, 1, false);
            addPile(Cards.princess, 1, false);
            addPile(Cards.trustySteed, 1, false);
        }

        // If Bandit Camp, Pillage, or Marauder is in play, we'll need Spoils (non-supply)
        if (piles.containsKey(Cards.banditCamp.getName()) ||
            piles.containsKey(Cards.pillage.getName()) ||
            piles.containsKey(Cards.marauder.getName()))
        {
            addPile(Cards.spoils, 15, false);
        }

        // If Urchin is in play, we'll need Mercenary (non-supply)
        if (piles.containsKey(Cards.urchin.getName()))
        {
            addPile(Cards.mercenary, 10, false);
        }

        // If Hermit is in play, we'll need Madman (non-supply)
        if (piles.containsKey(Cards.hermit.getName()))
        {
            addPile(Cards.madman, 10, false);
        }

        // If Page is in play, we'll need treasureHunter, warrior, hero, champion (non-supply)
        if (piles.containsKey(Cards.page.getName()))
        {
            addPile(Cards.treasureHunter, 5, false);
            addPile(Cards.warrior, 5, false);
            addPile(Cards.hero, 5, false);
            addPile(Cards.champion, 5, false);
        }

        // If Peasant is in play, we'll need soldier, fugitive, disciple, teacher (non-supply)
        if (piles.containsKey(Cards.peasant.getName()))
        {
            addPile(Cards.soldier, 5, false);
            addPile(Cards.fugitive, 5, false);
            addPile(Cards.disciple, 5, false);
            addPile(Cards.teacher, 5, false);
        }

        // If Baker is in play, each player starts with one coin token
        if (piles.containsKey(Cards.baker.getName()))
        {
            bakerInPlay = true;
        }

        // If Ranger, Giant or Pilgrimage are in play, each player starts with a journey token faced up
        if (   piles.containsKey(Cards.ranger.getName())
            || piles.containsKey(Cards.giant.getName())
            || piles.containsKey(Cards.pilgrimage.getName()))
        {
            journeyTokenInPlay = true;
        } else {
        	journeyTokenInPlay = false;
        }

        boolean oldDebug = debug;
        if (!debug && !showEvents.isEmpty()) {
            debug = true;
        }
        Util.debug("");
        Util.debug("Cards in Play", true);
        Util.debug("---------------", true);
        cardListText += "Cards in play\n---------------\n";

        int cost = 0;
        while (cost < 10) {
            for (AbstractCardPile pile : piles.values()) {
                if (Cards.isKingdomCard(pile.card())) {
                    if (pile.card().getCost(null) == cost) {
                        Util.debug(Util.getShortText(pile.card()), true);
                        cardListText += Util.getShortText(pile.card()) + "\n";
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

                if ((event.getType() == GameEvent.Type.CardObtained || event.getType() == GameEvent.Type.BuyingCard) &&
                		!event.card.isEvent()) {
                	
                    MoveContext context = event.getContext();
                    Player player = context.getPlayer();

                    if (player.isPossessed()) {
                        possessedBoughtPile.add(event.card);
                        MoveContext controlContext = new MoveContext(context.game, context.getPlayer().controlPlayer);
                        controlContext.getPlayer().gainCardAlreadyInPlay(event.card, Cards.possession, controlContext);
                        return;
                    }

                    if (context != null && event.card instanceof VictoryCard) {
                        context.vpsGainedThisTurn += ((VictoryCard) event.card).getVictoryPoints();
                    }

                    if (Cards.inn.equals(event.responsible))
                        Util.debug((String.format("discard pile: %d", player.discard.size())), true);

                    // See rules explanation of Tunnel for what commandedDiscard means.
                    boolean commandedDiscard = true;
                    if(event.getType() == GameEvent.Type.BuyingCard
                       || event.getType() == GameEvent.Type.CardObtained) {
                        commandedDiscard = false;
                    } else if(event.responsible != null) {
                        Card r = event.responsible;
                        if (r.equals(Cards.estate) && player.getInheritance() != null) {
                        	r = player.getInheritance();
                        }
                        if(r.equals(Cards.borderVillage) ||
                           r.equals(Cards.feast) ||
                           r.equals(Cards.remodel) ||
                           r.equals(Cards.swindler) ||
                           r.equals(Cards.ironworks) ||
                           r.equals(Cards.saboteur) ||
                           r.equals(Cards.upgrade) ||
                           r.equals(Cards.ambassador) ||
                           r.equals(Cards.smugglers) ||
                           r.equals(Cards.talisman) ||
                           r.equals(Cards.expand) ||
                           r.equals(Cards.forge) ||
                           r.equals(Cards.remake) ||
                           r.equals(Cards.hornOfPlenty) ||
                           r.equals(Cards.jester) ||
                           r.equals(Cards.develop) ||
                           r.equals(Cards.haggler) ||
                           r.equals(Cards.workshop) ||
                           r.equals(Cards.hermit) ||
                           r.equals(Cards.dameNatalie))
                        {
                            commandedDiscard = false;
                        }
                    }
                    boolean handled = false;

                    //Not sure if this is exactly right for the Trader, but it seems to be based on detailed card explanation in the rules
                    //The handling for new cards is done before taking the card from the pile in a different method below.
                    if(!event.newCard) {
                    	boolean hasInheritedTrader = Cards.trader.equals(context.getPlayer().getInheritance()) && context.getPlayer().hand.contains(Cards.estate);
                        boolean hasTrader = context.getPlayer().hand.contains(Cards.trader);
                        Card traderCard = hasTrader ? Cards.trader : Cards.estate;
                        if(hasTrader || hasInheritedTrader) {
                            if(player.controlPlayer.trader_shouldGainSilverInstead((MoveContext) context, event.card)) {
                                player.reveal(traderCard, null, context);
                                player.trash(event.card, Cards.trader, (MoveContext) context);
                                event.card = Cards.silver;
                                player.gainNewCard(Cards.silver, Cards.trader, context);
                                return;
                            }
                        }
                    }

                    if (event.getPlayer() == players[playersTurn]) {
                        cardsObtainedLastTurn[playersTurn].add(event.card);
                    }
                    
                    boolean hasInheritedWatchtower = Cards.watchTower.equals(player.getInheritance()) && player.hand.contains(Cards.estate);
                    boolean hasWatchtower = player.hand.contains(Cards.watchTower);
                    Card watchTowerCard = hasWatchtower ? Cards.watchTower : Cards.estate;
                    if (hasWatchtower || hasInheritedWatchtower) {
                        WatchTowerOption choice = context.player.controlPlayer.watchTower_chooseOption((MoveContext) context, event.card);

                        if (choice == WatchTowerOption.TopOfDeck) {
                            handled = true;
                            GameEvent watchTowerEvent = new GameEvent(GameEvent.Type.CardRevealed, context);
                            watchTowerEvent.card = watchTowerCard;
                            watchTowerEvent.responsible = null;
                            context.game.broadcastEvent(watchTowerEvent);

                            player.putOnTopOfDeck(event.card, context, true);
                        } else if (choice == WatchTowerOption.Trash) {
                            handled = true;
                            GameEvent watchTowerEvent = new GameEvent(GameEvent.Type.CardRevealed, context);
                            watchTowerEvent.card = watchTowerCard;
                            watchTowerEvent.responsible = null;
                            context.game.broadcastEvent(watchTowerEvent);

                            player.trash(event.card, Cards.watchTower, context);
                        }
                    }
                    
                    Card gainedCardAbility = event.card;
                    if (gainedCardAbility.equals(Cards.estate) && player.getInheritance() != null) {
                    	gainedCardAbility = player.getInheritance();
                    }

                    if(!handled) {
                    	if (context.isRoyalSealInPlay() && context.player.controlPlayer.royalSealTravellingFair_shouldPutCardOnDeck((MoveContext) context, Cards.royalSeal, event.card)) {
                            player.putOnTopOfDeck(event.card, context, true);
                    	} else if (context.travellingFairBought && context.player.controlPlayer.royalSealTravellingFair_shouldPutCardOnDeck((MoveContext) context, Cards.travellingFair, event.card)) {
                    		player.putOnTopOfDeck(event.card, context, true);
                        } else if (event.responsible != null && event.responsible.equals(Cards.summon)
                        		&& (!event.card.equals(Cards.inn))
                        		&& (!event.card.equals(Cards.borderVillage) || (event.card.equals(Cards.borderVillage) && Cards.borderVillage.getCost(context) == 0))
                        		&& (!event.card.equals(Cards.deathCart) || (event.card.equals(Cards.deathCart) && context.game.isPileEmpty(Cards.virtualRuins)))
                        				) {
                            //TODO: figure out better way to handle not Summoning Death Cart or Border Village (or other cards) due to lose track rule
                        	//      may have missed some esoteric cases here (e.g. Inn's when-gain ability doesn't have to have Summon lose track)
                        	context.player.summon.add(event.card);
        					GameEvent summonEvent = new GameEvent(GameEvent.Type.CardSetAsideSummon, context);
        					summonEvent.card = event.card;
        					context.game.broadcastEvent(summonEvent);
                        } else if (gainedCardAbility.equals(Cards.nomadCamp)) {
                            player.putOnTopOfDeck(event.card, context, true);
                        } else if (event.responsible != null) {
                            Card r = event.responsible;
                            if (r.equals(Cards.estate) && player.getInheritance() != null) {
                            	r = player.getInheritance();
                            }
                            
                            if (r.equals(Cards.armory)
                                || r.equals(Cards.artificer)
                                || r.equals(Cards.bagOfGold)
                                || r.equals(Cards.bureaucrat)
                                || r.equals(Cards.develop)
                                || r.equals(Cards.foolsGold)
                                || r.equals(Cards.graverobber) && context.graverobberGainedCardOnTop == true
                                || r.equals(Cards.seaHag)
                                || r.equals(Cards.taxman)
                                || r.equals(Cards.tournament)
                                || r.equals(Cards.treasureMap)) {
                                player.putOnTopOfDeck(event.card, context, true);
                            } else if (r.equals(Cards.beggar)) {
                                if (event.card.equals(Cards.copper)) {
                                    player.hand.add(event.card);
                                } else if (event.card.equals(Cards.silver) && context.beggarSilverIsOnTop++ == 0) {
                                    player.putOnTopOfDeck(event.card, context, true);
                                } else if (event.card.equals(Cards.silver)) {
                                    player.discard.add(event.card);
                                }
                            } else if (r.equals(Cards.tradingPost) || r.equals(Cards.mine) || r.equals(Cards.explorer) || r.equals(Cards.torturer) || r.equals(Cards.transmogrify)) {
                                player.hand.add(event.card);
                            } else if (r.equals(Cards.illGottenGains) && event.card.equals(Cards.copper)) {
                                player.hand.add(event.card);
                            } else {
                                player.discard(event.card, null, null, commandedDiscard, false);
                            }
                        } else {
                            player.discard(event.card, null, null, commandedDiscard, false);
                        }
                    }
                    
                    // check for when-gain callable cards
                    // NOTE: Technically this should be done in a loop, as you can call multiple cards for one when-gain.
                    //   However, since the only card here, Duplicate, will trigger another on-gain anyway
                    //   we don't need to.
                    ArrayList<Card> callableCards = new ArrayList<Card>();
                    for (Card c : player.tavern) {
                    	if (c.behaveAsCard().isCallableWhenCardGained()) {
                    		int callCost = c.behaveAsCard().getCallableWhenGainedMaxCost();
                    		if (callCost == -1 || (event.card.getCost(context) <= callCost && !event.card.costPotion())) {
                    			callableCards.add(c);
                    		}
                    	}
                    }
                    if (!callableCards.isEmpty()) {
                    	//ask player which card to call
                    	Collections.sort(callableCards, new Util.CardCostComparator());
                    	// we want null entry at the end for None
                    	Card[] cardsAsArray = callableCards.toArray(new Card[callableCards.size() + 1]);
                    	Card toCall = player.controlPlayer.call_whenGainCardToCall(context, event.card, cardsAsArray);
                    	if (toCall != null || callableCards.contains(toCall)) {
                    		toCall.behaveAsCard().callWhenCardGained(context, event.card);
                    	}
                    }
                    
                    if (gainedCardAbility.equals(Cards.illGottenGains)) {
                        for(Player targetPlayer : getPlayersInTurnOrder()) {
                            if(targetPlayer != player) {
                                MoveContext targetContext = new MoveContext(Game.this, targetPlayer);
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
                                    if(targetPlayer.controlPlayer.foolsGold_shouldTrash(targetContext)) {
                                        targetPlayer.hand.remove(Cards.foolsGold);
                                        targetPlayer.trash(Cards.foolsGold, Cards.foolsGold, targetContext);
                                        targetPlayer.gainNewCard(Cards.gold, Cards.foolsGold, targetContext);
                                    }
                                }
                            }
                        }
                    } else if(event.card.equals(Cards.duchy)) {
                        if (Cards.isSupplyCard(Cards.duchess) && getCardsLeftInPile(Cards.duchess) > 0) {
                            if(player.controlPlayer.duchess_shouldGainBecauseOfDuchy((MoveContext) context)) {
                                player.gainNewCard(Cards.duchess, Cards.duchess, context);
                            }
                        }
                    } else if(gainedCardAbility.equals(Cards.embassy)) {
                        for(Player targetPlayer : getPlayersInTurnOrder()) {
                            if(targetPlayer != player) {
                                MoveContext targetContext = new MoveContext(Game.this, targetPlayer);
                                targetPlayer.gainNewCard(Cards.silver, event.card, targetContext);
                            }
                        }
                    } else if(gainedCardAbility.equals(Cards.cache)) {
                        for(int i=0; i < 2; i++) {
                            player.gainNewCard(Cards.copper, event.card, context);
                        }
                    } else if(gainedCardAbility.equals(Cards.inn)) {
                        ArrayList<Card> cards = new ArrayList<Card>();
                        int actionCardsFound = 0;
                        for(int i=player.discard.size() - 1; i >= 0; i--) {
                            Card c = player.discard.get(i);
                            if(c.isAction(player)) {
                                actionCardsFound++;
                                if(player.controlPlayer.inn_shuffleCardBackIntoDeck(event.getContext(), c)) {
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
                        }
                        player.shuffleDeck(context, Cards.inn);
                    } else if (gainedCardAbility.equals(Cards.borderVillage)) {
                        boolean validCard = false;
                        int gainedCardCost = event.card.getCost(context);
                        for(Card c : event.context.getCardsInGame()) {
                            if(Cards.isSupplyCard(c) && c.getCost(context) < gainedCardCost && !c.costPotion() && event.context.getCardsLeftInPile(c) > 0) {
                                validCard = true;
                                break;
                            }
                        }

                        if(validCard) {
                            Card card = context.player.controlPlayer.borderVillage_cardToObtain(context, gainedCardCost - 1);
                            if (card != null) {
                                if(card.getCost(context) < gainedCardCost && !card.costPotion()) {
                                    player.controlPlayer.gainNewCard(card, event.card, (MoveContext) context);
                                }
                                else {
                                    Util.playerError(player, "Border Village returned invalid card, ignoring.");
                                }
                            }
                        }
                    } else if (gainedCardAbility.equals(Cards.mandarin)) {
                        CardList playedCards = ((MoveContext) context).getPlayedCards();
                        ArrayList<Card> treasureCardsInPlay = new ArrayList<Card>();

                        for(Card c : playedCards) {
                            if(c instanceof TreasureCard) {
                                treasureCardsInPlay.add(c);
                            }
                        }

                        if(treasureCardsInPlay.size() > 0) {
                            Card[] order ;
                            if (treasureCardsInPlay.size() == 1)
                                order = treasureCardsInPlay.toArray(new Card[treasureCardsInPlay.size()]);
                            else
                                order = player.controlPlayer.mandarin_orderCards(context, treasureCardsInPlay.toArray(new Card[treasureCardsInPlay.size()]));

                            for (int i = order.length - 1; i >= 0; i--) {
                                Card c = order[i];
                                player.putOnTopOfDeck(c);
                                playedCards.remove(c);
                            }
                        }
                    } else if (gainedCardAbility.equals(Cards.deathCart)) {
                        context.player.controlPlayer.gainNewCard(Cards.virtualRuins, event.card, context);
                        context.player.controlPlayer.gainNewCard(Cards.virtualRuins, event.card, context);
                    } else if (gainedCardAbility.equals(Cards.lostCity)) {
                        for(Player targetPlayer : getPlayersInTurnOrder()) {
                            if(targetPlayer != player) {
                                drawToHand(new MoveContext(Game.this, targetPlayer), Cards.lostCity, 1, true);
                            }
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

                boolean shouldShow = (debug || junit);
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
                        if (event.card.getControlCard() != event.card) {
                            msg.append(" <" + event.card.getControlCard().getName() + ">");
                        }
                        if (event.card.isImpersonatingAnotherCard()) {
                            msg.append(" (as " + event.card.behaveAsCard().getName() + ")");
                        }
                    }
                    if (event.getType() == GameEvent.Type.TurnBegin && event.getPlayer().isPossessed()) {
                        msg.append(" possessed by " + event.getPlayer().controlPlayer.getPlayerName() + "!");
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
            if (card.behaveAsCard().equals(Cards.lighthouse) && !((CardImpl) card).trashAfterPlay)
                return true;
        }

        return false;
    }
    
    int countChampionsInPlay(Player player) {
    	int count = 0;
        for (Card card : player.nextTurnCards) {
            if (card.behaveAsCard().equals(Cards.champion))
                count += ((CardImpl) card).cloneCount;
        }

        return count;
    }


    /*
       Note that any cards in the supply can have Embargo coins added.
       This includes the basic seven cards (Victory, Curse, Treasure),
       any of the 10 game piles, and Colony/Platinum when included.
       However, this does NOT include any Prizes from Cornucopia.
       */

    AbstractCardPile addEmbargo(Card card) {
        if (isValidEmbargoPile(card)) {
            String name = card.getName();
            embargos.put(name, getEmbargos(card) + 1);
            return piles.get(name);
        }
        return null;
    }

    public boolean isValidEmbargoPile(Card card) {
        return !(card == null || !cardInGame(card) || !Cards.isSupplyCard(card) );
    }

    public int getEmbargos(Card card) {
        Integer count = embargos.get(card.getName());
        return (count == null) ? 0 : count;
    }
    
    public List<PlayerSupplyToken> getPlayerSupplyTokens(Card card, Player player) {
    	card = card.getTemplateCard();
    	if (card.isKnight(null)) card = Cards.virtualKnight;
        if (card.isRuins(null)) card = Cards.virtualRuins;
    	if (player == null || !playerSupplyTokens.containsKey(card.getName()))
    		return new ArrayList<PlayerSupplyToken>();
    	
    	if (!playerSupplyTokens.get(card.getName()).containsKey(player)) {
        	playerSupplyTokens.get(card.getName()).put(player, new ArrayList<PlayerSupplyToken>());
    	}
    	return playerSupplyTokens.get(card.getName()).get(player);
    }
    
    public boolean isPlayerSupplyTokenOnPile(Card card, Player player, PlayerSupplyToken token) {
    	return getPlayerSupplyTokens(card, player).contains(token);
    }
    
    public void movePlayerSupplyToken(Card card, Player player, PlayerSupplyToken token) {
    	removePlayerSupplyToken(player, token);
    	getPlayerSupplyTokens(card, player).add(token);
    	
        MoveContext context = new MoveContext(this, player);
        GameEvent.Type eventType = null;
        if (token == PlayerSupplyToken.PlusOneCard) {
        	eventType = Type.PlusOneCardTokenMoved;
        } else if (token == PlayerSupplyToken.PlusOneAction) {
        	eventType = Type.PlusOneActionTokenMoved;
        } else if (token == PlayerSupplyToken.PlusOneBuy) {
        	eventType = Type.PlusOneBuyTokenMoved;
        } else if (token == PlayerSupplyToken.PlusOneCoin) {
        	eventType = Type.PlusOneCoinTokenMoved;
        } else if (token == PlayerSupplyToken.MinusTwoCost) {
        	eventType = Type.MinusTwoCostTokenMoved;
        } else if (token == PlayerSupplyToken.Trashing) {
        	eventType = Type.TrashingTokenMoved;
        }
        GameEvent event = new GameEvent(eventType, context);
        event.card = card;
        broadcastEvent(event);
    }
    
    private void removePlayerSupplyToken(Player player, PlayerSupplyToken token) {
		for (String cardName : playerSupplyTokens.keySet()) {
			if (playerSupplyTokens.get(cardName).containsKey(player)) {
				playerSupplyTokens.get(cardName).get(player).remove(token);
			}
		}
	}

	// Only is valid for cards in play...
    //    protected Card readCard(String name) {
    //        AbstractCardPile pile = piles.get(name);
    //        if (pile == null || pile.getCount() <= 0) {
    //            return null;
    //        }
    //        return pile.card();
    //    }

    protected Card takeFromPile(Card card) {
        return takeFromPile(card, null);
    }
    
    protected Card takeFromPile(Card card, MoveContext context) {
        if (context == null || !context.blackMarketBuyPhase) {
            if (card.isKnight(null)) card = Cards.virtualKnight;
            if (card.isRuins(null)) card = Cards.virtualRuins;
        }
        
        AbstractCardPile pile = getPile(card);
        if (pile == null || pile.getCount() <= 0) {
            return null;
        }

        Card thisCard;
        tradeRouteValue += pile.takeTradeRouteToken();
        if (card.equals(Cards.virtualRuins) || card.equals(Cards.virtualKnight)) {
            SingleCardPile cp = ((VariableCardPile) pile).getTopLinkedPile();
            if (cp == null) return null;
            thisCard = cp.removeCard();
            pile.removeCard();
        } else {
            thisCard = pile.removeCard();
        }

        return thisCard;
    }

    protected Card takeFromPileCheckTrader(Card cardToGain, MoveContext context) {
    	boolean hasInheritedTrader = Cards.trader.equals(context.getPlayer().getInheritance()) && context.getPlayer().hand.contains(Cards.estate);
        boolean hasTrader = context.getPlayer().hand.contains(Cards.trader);
        Card traderCard = hasTrader ? Cards.trader : Cards.estate;
        if(!isPileEmpty(cardToGain) && (hasTrader || hasInheritedTrader) && !cardToGain.equals(Cards.silver)) {
            if (context.player.controlPlayer.trader_shouldGainSilverInstead((MoveContext) context, cardToGain)) {
                cardToGain = Cards.silver;
                context.player.reveal(traderCard, null, context);
            }
        }

        return takeFromPile(cardToGain, context);
    }

    public int pileSize(Card card) {
        AbstractCardPile pile = getPile(card);
        if (pile == null) {
            return -1;
        }

        return pile.getCount();
    }

    public boolean isPileEmpty(Card card) {
        return pileSize(card) <= 0;
    }

    public int emptyPiles() {
        int emptyPiles = 0;
        for (AbstractCardPile pile : piles.values()) {
            if (pile.getCount() <= 0 && pile.isSupply()) {
                emptyPiles++;
            }
        }
        return emptyPiles;
    }

    public boolean isCardInGame(Card card) {
        AbstractCardPile pile = getPile(card);
        if (pile == null) {
            return false;
        }
        return true;
    }

    public Card[] getCardsInGame() {
        return getCardsInGame(null);
    }

    public Card[] getCardsInGame(Class<?> c) {
        ArrayList<Card> cards = new ArrayList<Card>();
        for (AbstractCardPile pile : piles.values()) {
            if (c == null) {
                if (pile.type.equals(AbstractCardPile.PileType.RuinsPile)) {
                    cards.add(Cards.virtualRuins);
                } else if (pile.type.equals(AbstractCardPile.PileType.KnightsPile)) {
                    cards.add(Cards.virtualKnight);
                } else {
                    cards.add(pile.card());
                }
            } else if (c.isInstance(pile.card()) && pile.isSupply) {
                cards.add(pile.card());
            }
        }
        return cards.toArray(new Card[0]);
    }

    public Card[] getActionsInGame() {
        return getCardsInGame(ActionCard.class);
    }

    public boolean cardInGame(Card c) {
        for (AbstractCardPile pile : piles.values()) {
            if(c.equals(pile.card())) {
                return true;
            }
        }
        return false;
    }

    public boolean isPlatInGame() {
        return cardInGame(Cards.platinum);
    }

    public boolean isColonyInGame() {
        return cardInGame(Cards.colony);
    }

    public Card[] getTreasureCardsInGame() {
        return getCardsInGame(TreasureCard.class);
    }

    public Card[] getVictoryCardsInGame() {
        return getCardsInGame(VictoryCard.class);
    }

    public Card[] getCardsInGameOrderByCost() {
        Card[] cardsInGame = getCardsInGame();
        Arrays.sort(cardsInGame, new CardCostComparator());
        return cardsInGame;
    }

    public int getCardsLeftInPile(Card card) {
        AbstractCardPile pile = getPile(card);
        if (pile == null || pile.getCount() < 0) {
            return 0;
        }

        return pile.getCount();
    }

    public ArrayList<Card> GetTrashPile()
    {
        return trashPile;
    }

    public ArrayList<Card> GetBlackMarketPile()
    {
        return blackMarketPile;
    }

    protected AbstractCardPile addPile(Card card) {
        int count = kingdomCardPileSize;
        if(card instanceof VictoryCard) count = victoryCardPileSize;
        if(card.equals(Cards.rats)) count = 20;
        if(card.equals(Cards.port)) count = 12;
        if(card instanceof EventCard) count = 1;
        return addPile(card, count);
    }

    protected AbstractCardPile addPile(Card card, int count) {
        return addPile(card, count, true);
    }

    protected AbstractCardPile addPile(Card card, int count, boolean isSupply) {
        return addPile(card, count, isSupply, false);
    }

    protected AbstractCardPile addPile(Card card, int count, boolean isSupply, boolean isBlackMarket) {
        AbstractCardPile pile;
        if (card.equals(Cards.virtualRuins)) {
            pile = new VariableCardPile(AbstractCardPile.PileType.RuinsPile, Math.max(10, Math.min(50, (numPlayers * 10) - 10)));
        } else if (card.equals(Cards.virtualKnight)) {
            pile = new VariableCardPile(AbstractCardPile.PileType.KnightsPile, Math.min(Cards.knightsCards.size(), 10));
        } else {
            pile = new SingleCardPile(card, count);
        }

        if (!isSupply) {
            pile.notInSupply();
        }
        if (isBlackMarket) {
            pile.inBlackMarket();
        }

        piles.put(card.getName(), pile);
        
        playerSupplyTokens.put(card.getName(), new HashMap<Player, List<PlayerSupplyToken>>());
        

        return pile;
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
    
    public Player getCurrentPlayer() {
        return players[playersTurn];
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

        AbstractCardPile grandMarket = getPile(Cards.grandMarket);
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

    /**
     * @return Card on top of the Ruins pile
     */
    public Card getTopRuinsCard() {
        AbstractCardPile p = getPile(Cards.virtualRuins);
        if (p == null) return null;
        return p.card();
    }

    public Card getTopKnightCard() {
        AbstractCardPile p = getPile(Cards.virtualKnight);
        if (p == null) return null;
        return p.card();
    }

    public AbstractCardPile getPile(Card card) {
        return piles.get(card.getName());
    }

    public void trashHovelsInHandOption(Player player, MoveContext context, Card responsible)
    {
        // If player has a Hovel (or multiple Hovels), offer the option to trash...
        ArrayList<Card> hovelsToTrash = new ArrayList<Card>();

        for (Card c : player.hand)
        {
            if (c.getType() == Cards.Type.Hovel && player.controlPlayer.hovel_shouldTrash(context))
            {
                hovelsToTrash.add(c);
            }
        }

        if (hovelsToTrash.size() > 0)
        {
            for (Card c : hovelsToTrash)
            {
                player.hand.remove(c);
                player.trash(c, responsible, context);
            }
        }
    }
    
    public boolean hauntedWoodsAttacks(Player player)
    {
        for (Player otherPlayer : players) {
            if (otherPlayer != null && otherPlayer != player) {
            	if (otherPlayer.getDurationEffectsOnOtherPlayer(player, Cards.Type.HauntedWoods) > 0) {
            		return true;
            	}
            }
        }
        return false;
    }

    public int swampHagAttacks(Player player)
    {
    	int swampHags = 0;
        for (Player otherPlayer : players) {
            if (otherPlayer != null && otherPlayer != player) {
            	swampHags += otherPlayer.getDurationEffectsOnOtherPlayer(player, Cards.Type.SwampHag);
            }
        }
        return swampHags;
    }
}
