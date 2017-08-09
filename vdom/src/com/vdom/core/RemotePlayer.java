package com.vdom.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vdom.api.Card;
import com.vdom.api.GameEvent;
import com.vdom.api.GameEvent.EventType;
import com.vdom.api.GameEventListener;
import com.vdom.comms.Comms;
import com.vdom.comms.Event;
import com.vdom.comms.Event.EType;
import com.vdom.comms.Event.EventObject;
import com.vdom.comms.EventHandler;
import com.vdom.comms.GameStatus;
import com.vdom.comms.GameStatus.JourneyTokenState;
import com.vdom.comms.MyCard;
import com.vdom.comms.NewGame;
import com.vdom.comms.SelectCardOptions;

/**
 * Class that you can use to play remotely.
 * This seems to be the human player
 */
public class RemotePlayer extends IndirectPlayer implements GameEventListener, EventHandler {
    @SuppressWarnings("unused")
    private static final String TAG = "RemotePlayer";

    static int nextPort = 2255;
    static final int NUM_RETRIES = 3; // times to try anything before giving up.
    static int maxPause = 300000; // Maximum time to wait for new player to connect = 5 minutes in ms;
    private static VDomServer vdomServer = null; // points to the VDomServer object

    // private static final String DISTINCT_CARDS = "Distinct Cards";

    Comms comm = null;
    // communication thread handled internally now
    // Thread commThread;
    private int myPort = 0;

    protected String name;
    private HashMap<String, Integer> cardNamesInPlay = new HashMap<String, Integer>();
    private ArrayList<Card> cardsInPlay = new ArrayList<Card>();
    private ArrayList<Player> allPlayers = new ArrayList<Player>();
    private MyCard[] myCardsInPlay;

    private ArrayList<Card> playedCards = new ArrayList<Card>();
    private ArrayList<Boolean> playedCardsNew = new ArrayList<Boolean>();

    private boolean hasJoined = false;
    private Object hasJoinedMonitor;

    long whenStarted = 0;

    private Thread gameThread = null; // vdom-engine-thread
    private int dieTries = 0; // How often we tried to kill the vdom-thread

    public void waitForJoin() {
        synchronized(hasJoinedMonitor) {
            long startTime = System.currentTimeMillis();
            while (!hasJoined ) {
                debug("Waiting for " + maxPause + " ms...");
                try {
                    hasJoinedMonitor.wait(maxPause);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                debug("Done waiting. hasJoined: " + (hasJoined?"True":"False"));
                if ((System.currentTimeMillis() - startTime) > maxPause) {
                    debug("Timed out waiting for player to join.");
                    break;
                }
            }
        }
    }
    public void playerJoined(){
        synchronized(hasJoinedMonitor) {
            hasJoined = true;
            hasJoinedMonitor.notify();
        }
    }

    public static void setVdomserver(VDomServer vdomserver) {
        RemotePlayer.vdomServer = vdomserver;
        maxPause = VDomServer.maxPause;
    }
    public static VDomServer getVdomserver() {
        return vdomServer;
    }
    public int getPort() {
        return myPort;
    }
    public boolean hasJoined() {
        return hasJoined;
    }

    public static MyCard makeMyCard(Card c, int index, boolean isBane, boolean isObeliskCard, boolean isBlackMarket, boolean uniqueCardPile){
        MyCard card = new MyCard(index, c.getName(), c.getSafeName(), c.getName());
        card.desc = c.getDescription();
        card.expansion = c.getExpansion() != null ? c.getExpansion().toString() : "";
        card.originalExpansion = c.getExpansion() != null ? c.getExpansion().toString() : "";
        card.cost = c.getCost(null);
        card.debtCost = c.getDebtCost(null);
        card.costPotion = c.costPotion();
        card.isBane = isBane;
        card.isObeliskCard = isObeliskCard;
        card.isShelter = c.is(Type.Shelter, null);
        card.isLooter = c.is(Type.Looter, null);
        card.isOverpay = c.isOverpay(null);
        card.isEvent = c.is(Type.Event, null);
        card.isReserve = c.is(Type.Reserve, null);
        card.isTraveller = c.is(Type.Traveller, null);
        card.isKnight = c.is(Type.Knight, null);
        card.isCastle = c.is(Type.Castle, null);
        card.isGathering = c.is(Type.Gathering, null);
        card.isLandmark = c.is(Type.Landmark, null);
        card.isAttack = c.is(Type.Attack, null) || c.equals(Cards.virtualKnight);
        if (c.equals(Cards.virtualRuins))
            card.isRuins = true;
        else
            card.isRuins = c.is(com.vdom.core.Type.Ruins, null);


        if (uniqueCardPile) {
            card.pile = MyCard.SUPPLYPILE;
        } else {
            card.pile = MyCard.VARIABLE_CARDS_PILE;
        }

        if (c.is(Type.Prize, null)) {
            card.pile = MyCard.PRIZEPILE;
            card.isPrize = true;
        }

        if (   c.equals(Cards.spoils)
            || c.equals(Cards.mercenary)
            || c.equals(Cards.madman)
            || c.equals(Cards.soldier)
            || c.equals(Cards.fugitive)
            || c.equals(Cards.disciple)
            || c.equals(Cards.teacher)
            || c.equals(Cards.treasureHunter)
            || c.equals(Cards.warrior)
            || c.equals(Cards.hero)
            || c.equals(Cards.champion)
           )
        {
            card.pile = MyCard.NON_SUPPLY_PILE;
        }

        if (c.equals(Cards.necropolis) ||
            c.equals(Cards.overgrownEstate) ||
            c.equals(Cards.hovel))
        {
            card.pile = MyCard.SHELTER_PILES;
        }

        if ((c.equals(Cards.copper)) ||
            (c.equals(Cards.silver)) ||
            (c.equals(Cards.potion)) ||
            (c.equals(Cards.gold)) ||
            (c.equals(Cards.platinum))) card.pile = MyCard.MONEYPILE;

        if ((c.equals(Cards.estate)) ||
            (c.equals(Cards.duchy)) ||
            (c.equals(Cards.province)) ||
            (c.equals(Cards.colony)) ||
            (c.equals(Cards.curse))) card.pile = MyCard.VPPILE;


        if (c.equals(Cards.potion)) card.isPotion = true;
        if (c.equals(Cards.curse)) {
            card.isCurse = true;
            card.vp =  c.getVictoryPoints();
        }
        if (c.is(Type.Victory)) {
            card.isVictory = true;
            card.vp =  c.getVictoryPoints();
        }
        if (c.is(Type.Treasure, null)) {
            card.isTreasure = true;
            card.gold = c.getAddGold();
        }
        if (c.is(Type.Action, null)) {
            card.isAction = true;
            if (c.is(Type.Duration, null)) {
                card.isDuration = true;
            }
        }
        if (c.is(Type.Reaction, null))
            card.isReaction = true;
        if (isBlackMarket) {
            card.isBlackMarket = true;
            card.pile = MyCard.BLACKMARKET_PILE;
        }
        if (c.is(Type.Event, null)) {
            card.isEvent = true;
            card.pile = MyCard.EVENTPILE;
        }
        if (c.is(Type.Landmark, null)) {
            card.isLandmark = true;
            card.pile = MyCard.EVENTPILE;
        }
        if (c.equals(Cards.stash)) {
        	card.isStash = true;
        }

        return card;
    }

    public Card intToCard(int i) {
        return cardsInPlay.get(i);
    }
    public Card[] intArrToCardArr(int[] cards) {
        Card[] cs = new Card[cards.length];
        for (int i = 0; i < cards.length; i++) {
            cs[i] = intToCard(cards[i]);
        }
        return cs;
    }
    public Card nameToCard(String o) {
        return intToCard(cardNamesInPlay.get(o));
    }
    @Override
    public int cardToInt(Card card) {
        // TODO:  NullPointerException for tournament prizes
        if (cardNamesInPlay.containsKey(card.getName()))
            return cardNamesInPlay.get(card.getName());
        else
            return -1;
    }

    public int[] cardArrToIntArr(Card[] cards) {
        int[] is = new int[cards.length];
        for (int i = 0; i < cards.length; i++) {
            is[i] = cardToInt(cards[i]);
        }
        return is;
    }

    public int[] arrayListToIntArr(ArrayList<Card> cards) {
        int[] is = new int[cards.size()];

        for (int i = 0; i < cards.size(); ++i) {
            is[i] = cardToInt((Card)cards.get(i));
        }

        return is;
    }

    public void setupCardsInPlay(MoveContext context) {
        ArrayList<MyCard> myCardsInPlayList = new ArrayList<MyCard>();

        int index = 0;

        // ensure card #0 is a card not to shade, e.g. Curse. See Rev r581
        Card curse = Cards.curse;
        MyCard mc = makeMyCard(curse, index, false, false, false, true);
        myCardsInPlayList.add(mc);
        cardNamesInPlay.put(curse.getName(), index);
        cardsInPlay.add(index, curse);
        index++;

        for (Card c : context.getCardsInGame(GetCardsInGameOptions.All)) {
            if (c.getSafeName().equals(Cards.curse.getSafeName()))
                continue;

            boolean isBlackMarket = false;
            for (Card bm : context.game.blackMarketPile) {
                if (c.getSafeName().equals(bm.getSafeName())) {
                    isBlackMarket = true;
                }
            }
            boolean isBane = context.game.baneCard != null ? c.getSafeName().equals(context.game.baneCard.getSafeName()) : false;
            boolean isObelisk = context.game.obeliskCard != null ? context.game.cardsInSamePile(c, context.game.obeliskCard) : false;
            boolean uniqueCardPile = context.game.getPile(c).placeholderCard().equals(c);
            mc = makeMyCard(c, index, isBane, isObelisk, isBlackMarket, uniqueCardPile);
            myCardsInPlayList.add(mc);

            cardNamesInPlay.put(c.getName(), index);
            cardsInPlay.add(index, c);
            index++;
        }
        myCardsInPlay = myCardsInPlayList.toArray(new MyCard[0]);
    }

    public Event fullStatusPacket(MoveContext context, Player player, boolean isFinal) {
        if (player == null)
            player = context.getPlayer();

        int[] supplySizes = new int[cardsInPlay.size()];
        int[] embargos = new int[cardsInPlay.size()];
        int[] pileVpTokens = new int[cardsInPlay.size()];
        int[] pileDebtTokens = new int[cardsInPlay.size()];
        int[] pileTradeRouteTokens = new int[cardsInPlay.size()];
        int[][][] tokens = new int[cardsInPlay.size()][][];
        int[] costs = new int[cardsInPlay.size()];

        int i_virtualRuins = -1;
        int i_virtualKnight = -1;
        int ruinsSize = 0;
        int knightSize = 0;

        for (int i = 0; i < cardsInPlay.size(); i++) {
            if (!isFinal)
            {
                supplySizes[i] = context.getCardsLeftInPile(intToCard(i));
            }
            else
            {
                supplySizes[i] = player.getMyCardCount(cardsInPlay.get(i));
            }
            embargos[i] = context.getEmbargos(intToCard(i));
            pileVpTokens[i] = context.getPileVpTokens(intToCard(i));
            pileDebtTokens[i] = context.getPileDebtTokens(intToCard(i));
            pileTradeRouteTokens[i] = context.getPileTradeRouteTokens(intToCard(i));
            
            costs[i] = intToCard(i).getCost(context);
        }

        // show opponent hand if possessed
        CardList shownHand = (player.isPossessed() || player.isControlled()) ? player.getHand() : getHand();

        // ArrayList<Card> playedCards = context.getPlayedCards();

        if (!allPlayers.contains(player))
            allPlayers.add(player);
        int numPlayers = allPlayers.size();

        int curPlayerIndex = allPlayers.indexOf(player);

        int numCards[] = new int[numPlayers];
        int turnCounts[] = new int[numPlayers];
        int deckSizes[] = new int[numPlayers];
        boolean stashOnDeck[] = new boolean[numPlayers];
        int discardSizes[] = new int[numPlayers];
        int handSizes[] = new int[numPlayers];
        int stashesInHand[] = new int[numPlayers]; 
        int pirates[] = new int[numPlayers];
        int victoryTokens[] = new int[numPlayers];
        int debtTokens[] = new int[numPlayers];
        int guildsCoinTokens[] = new int[numPlayers];
        JourneyTokenState journeyToken[] = new JourneyTokenState[numPlayers];
        boolean minusOneCoinTokenOn[] = new boolean[numPlayers];
        boolean minusOneCardTokenOn[] = new boolean[numPlayers];
        String realNames[] = new String[numPlayers];

        for (int i=0; i<numPlayers; i++) {
            Player p = allPlayers.get(i);
            if (!isFinal)
                handSizes[i] = p.getHand().size();
            else
                handSizes[i] = p.getVPs();
            stashesInHand[i] = p.getStashesInHand();
            turnCounts[i] = p.getTurnCount();
            deckSizes[i] = p.getDeckSize();
            stashOnDeck[i] = p.isStashOnDeck();
            discardSizes[i] = p.getDiscardSize();
            numCards[i] = p.getAllCards().size();

            pirates[i] = p.getPirateShipTreasure();
            victoryTokens[i] = p.getVictoryTokens();
            debtTokens[i] = p.getDebtTokenCount();
            guildsCoinTokens[i] = p.getGuildsCoinTokenCount();
            journeyToken[i] = context.game.journeyTokenInPlay ? (p.getJourneyToken() ? JourneyTokenState.FACE_UP : JourneyTokenState.FACE_DOWN) : null;
            minusOneCoinTokenOn[i] = p.getMinusOneCoinToken();
            minusOneCardTokenOn[i] = p.getMinusOneCardToken();
            realNames[i] = p.getPlayerName(false);
        }
        
        for (int i = 0; i < cardsInPlay.size(); i++) {
        	Card card = cardsInPlay.get(i);
        	int[][] playersForCard = new int[numPlayers][];
        	for (int j = 0; j < numPlayers; j++) {
        		Player p = allPlayers.get(j);
        		List<PlayerSupplyToken> playerTokensList = context.game.getPlayerSupplyTokens(card, p);
            	int[] playerTokensOnCard = new int[playerTokensList.size()];
            	for (int k = 0; k < playerTokensOnCard.length; ++k) {
            		playerTokensOnCard[k] = playerTokensList.get(k).getId();
            	}
            	playersForCard[j] = playerTokensOnCard;
        	}
        	tokens[i] = playersForCard;
        }

        GameStatus gs = new GameStatus();
        
        matchToCardsInPlay(context, playedCards, playedCardsNew);

        int[] playedArray = new int[playedCards.size()];
        for (int i = 0; i < playedCards.size(); i++) {
            Card c = playedCards.get(i);
            boolean newcard = playedCardsNew.get(i).booleanValue();
            playedArray[i] = (cardToInt(c) * (newcard ? 1 : -1));
        }

        gs.setTurnStatus(new int[] {context.getActionsLeft(),
            context.getBuysLeft(),
                context.getCoinForStatus(),
                context.countThroneRoomsInEffect()
        })
        .setFinal(isFinal)
                .setPossessed(player.isPossessed())
                .setTurnCounts(turnCounts)
                .setSupplySizes(supplySizes)
                .setEmbargos(embargos)
                .setPileVpTokens(pileVpTokens)
                .setPileDebtTokens(pileDebtTokens)
                .setPileTradeRouteTokens(pileTradeRouteTokens)
                .setTokens(tokens)
                .setCosts(costs)
                .setHand(cardArrToIntArr(Game.sortCards ? shownHand.sort(new Util.CardHandComparator()) : shownHand.toArray()))
                .setPlayedCards(playedArray)
                .setCurPlayer(curPlayerIndex)
                .setCurName(player.getPlayerName(!isFinal && Game.maskPlayerNames))
                .setRealNames(realNames)
                .setHandSizes(handSizes)
                .setStashesInHand(stashesInHand)
                .setDeckSizes(deckSizes)
                .setStashOnDeck(stashOnDeck)
                .setNumCards(numCards)
                .setPirates(pirates)
                .setVictoryTokens(victoryTokens)
                .setDebtTokens(debtTokens)
                .setGuildsCoinTokens(guildsCoinTokens)
                .setJourneyToken(journeyToken)
                .setMinusOneCoinToken(minusOneCoinTokenOn)
                .setMinusOneCardToken(minusOneCardTokenOn)
                .setSwampHagAttacks(game.swampHagAttacks(player))
                .setHauntedWoodsAttacks(game.hauntedWoodsAttacks(player))
                .setEnchantressAttacks(!context.enchantressAlreadyAffected && game.enchantressAttacks(player))
                .setCardCostModifier(context.cardCostModifier)
                .setPotions(context.getPotionsForStatus(player))
                .setTavern(cardArrToIntArr(player.getTavern().sort(new Util.CardTavernComparator())))
                .setPrince(cardArrToIntArr(player.getPrince().toArray()))
                .setIsland(cardArrToIntArr(player.getIsland().toArray()))
                .setVillage(player.equals(this) ? cardArrToIntArr(player.getNativeVillage().toArray()) : new int[0]/*show empty Village*/)
                .setInheritance(player.inheritance == null ? -1 : cardToInt(player.inheritance))
                .setArchive(getArchiveColumnCardInts(player.archive))
                .setBlackMarket(arrayListToIntArr(player.game.GetBlackMarketPile()))
                .setTrash(arrayListToIntArr(player.game.GetTrashPile()));

        for (Card card : game.getCardsInGame(GetCardsInGameOptions.Placeholders, false)) {
            CardPile pile = game.getPile(card);
            Card placeholder = pile.placeholderCard();
            Card topCard = pile.topCard();
            int count = -1; //Don't change count unless it's final game view
            boolean showPlaceHolder = false;
            if (isFinal) {
                topCard = placeholder;
                showPlaceHolder = true;
                count = pile.getCount();
            }
            if (topCard == null) {
                topCard = placeholder; //If pile is empty show placeholder card
                showPlaceHolder = true;
            }

            if (!placeholder.equals(topCard) || showPlaceHolder) {
                gs.addUpdatedCard(cardToInt(placeholder), topCard, topCard.getCost(showPlaceHolder ? null : context), topCard.getDebtCost(showPlaceHolder ? null : context), count); //Don't calculate cost reduction if placeholder card is shown
            }
        }

        Event p = new Event(EType.STATUS)
                .setObject(new EventObject(gs));

        return p;
    }

	private void matchToCardsInPlay(MoveContext context, ArrayList<Card> played, ArrayList<Boolean> playedReal) {
    	if (context.startOfTurn)
    		return;
    	Map<Card, Integer> inPlayCounts = new HashMap<Card, Integer>();
    	for (Card c : context.player.playedCards) {
    		if (!inPlayCounts.containsKey(c)) {
    			inPlayCounts.put(c, 1);
    		} else {
    			inPlayCounts.put(c, inPlayCounts.get(c)+1);
    		}
    	}
    	for (Card c : context.player.nextTurnCards) {
    		if (((CardImpl)c).trashAfterPlay)
    			continue;
			if (!inPlayCounts.containsKey(c)) {
    			inPlayCounts.put(c, 1);
    		} else {
    			inPlayCounts.put(c, inPlayCounts.get(c)+1);
    		}
    	}
    	Map<Card, Integer> playedCounts = new HashMap<Card, Integer>();
    	for (int i = 0; i < played.size(); ++i) {
    		if (!playedReal.get(i))
    			continue;
    		Card c = played.get(i);
    		if (!playedCounts.containsKey(c)) {
    			playedCounts.put(c, 1);
    		} else {
    			playedCounts.put(c, playedCounts.get(c)+1);
    		}
    	}
    	
    	for (Card c : playedCounts.keySet()) {
    		int inPlayCount = (inPlayCounts.containsKey(c) ? inPlayCounts.get(c) : 0);
    		if (playedCounts.get(c) > inPlayCount) {
    			int extras = playedCounts.get(c) - inPlayCount;
    			for (int i = played.size() - 1; extras > 0 && i >= 0; --i) {
    				if (played.get(i).equals(c) && playedReal.get(i)) {
    					playedReal.set(i, false);
    					extras--;
    				}
    			}
    		}
    	}
	}
	
    private int[] getArchiveColumnCardInts(ArrayList<ArrayList<Card>> archives) {
    	ArrayList<Integer> cardInts = new ArrayList<Integer>(); 
		for(ArrayList<Card> archive : archives) {
			cardInts.add(-cardToInt(Cards.archive));
			for(Card c : archive) {
				cardInts.add(cardToInt(c));
			}
		}
		int[] result = new int[cardInts.size()];
		int i = 0;
		for (int c : cardInts) {
			result[i++] = c;
		}
		return result;
	}
    
	@Override
    public void newGame(MoveContext context) {
        hasJoinedMonitor = new Object(); // every game needs a different monitor, otherwise we wake up threads that are supposed to be dead.
        context.addGameListener(this);
        setupCardsInPlay(context);
        gameThread = Thread.currentThread();

        allPlayers.clear();
        myPort = connect();
        if (vdomServer != null)
            vdomServer.registerRemotePlayer(this);
        if (myPort == 0)
            quit("Could not create server.");
    }

    public Event sendWithAck(Event tosend, EType resp) throws IOException, NullPointerException {
        Event p;

        for (int i = 0; i < NUM_RETRIES; i++) {
            comm.put_ts(tosend);
            p = comm.get_ts();
            if (p == null)
                throw new IOException();
            else if (p.t == resp)
                return p;
        }

        throw new IOException();
    }

    @Override
    public void sendErrorHandler(Exception e) {
        e.printStackTrace();
        comm.injectNullReceived(); // This causes sendWithAck to receive a null and therefore throw an error, which we want.
    }

    private void achievement(MoveContext context, String achievement) {
        Event status = fullStatusPacket(curContext == null ? context : curContext, curPlayer, false).setString(achievement);
        try {
            sendWithAck(status.setType(EType.ACHIEVEMENT).setString(achievement), EType.Success);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Event query(MoveContext context, Event tosend, EType resp) {
        Event reply;
        for (int connections = 0; connections < NUM_RETRIES; connections++) {
            try {
                //sendWithAck(fullStatusPacket(context, null, false), EType.Success);
                comm.put_ts(fullStatusPacket(context, null, false));
                reply = sendWithAck(tosend, resp);
            } catch (IOException e) {
                reply = null;
            }
            if (reply != null)
                return reply;

            reconnect("Could not complete query.");
            waitForJoin();
            if (!hasJoined)
                quit("Response timed out");
        }
        quit("Could not complete query.");
        return null;
    }

    Player curPlayer = null;
    MoveContext curContext = null;
    boolean gameOver = false;

    @Override
    public void trash(Card card, Card responsible, MoveContext context) {
        //inform about trashed card
        Event p = new Event(EType.INFORM)
            .setString("TRASHED")
            .setCard(card);
        comm.put_ts(p);
        
        super.trash(card, responsible, context);
    }    
    
    @Override
    public void gameEvent(GameEvent event) {
        super.gameEvent(event);

        MoveContext context = event.getContext();

        // First we check for achievements
        checkForAchievements(context, event.getType());

        // Now we set up some variables that we need to send an event.
        boolean sendEvent = true;
        String playerName = "";
        boolean playerNameIncluded = false;
        if (event.getPlayer() != null && event.getPlayer().getPlayerName() != null) {
            playerName += event.getPlayer().getPlayerName() + ": ";
            playerNameIncluded = true;
        }
        boolean newTurn = false;
        boolean isFinal = false;
        Card[] cards = null;
        String playerInt = "" + allPlayers.indexOf(event.getPlayer());

        // Because we push all construction of strings to the client that talks to RemotePlayer, we
        // create the "extras" object that gives the client enough information to know what exactly
        // the event is.  This part of the "extras" object applies to all event types.
        List<Object> extras = new ArrayList<Object>();
        if (event.getPlayer().isPossessed() || event.getPlayer().isControlled()) {
            extras.add(event.getPlayer().controlPlayer.getPlayerName());
        } else {
            extras.add(null);
        }
        if (event.getAttackedPlayer() != null) {
            extras.add(event.getAttackedPlayer().getPlayerName());
        } else {
            extras.add(null);
        }
        if (context != null && context.getMessage() != null) {
            extras.add(context.getMessage());
        } else {
            extras.add(null);
        }


        // Now check for event-type-specific things that we should do.
        if (event.getType() == EventType.VictoryPoints) {
                sendEvent = false;
        } else if (event.getType() == EventType.GameStarting) {
            if (event.getPlayer() == this) {
                waitForJoin();
                if (!hasJoined)
                    quit("Join timed out");
            }
            whenStarted = System.currentTimeMillis();
            playedCards.clear();
            gameOver = false;

            // Only send the event if its the first game starting, which doesn't include the player
            // name, so that the "Chance for plat/colony" shows up only once and so that only one
            // GameStarting event gets shown in the status area.
            if (playerNameIncluded) {
                sendEvent = false;
            }
        } else if (event.getType() == EventType.TurnBegin) {
            curPlayer = event.getPlayer();
            curContext = context;
            newTurn = true;
            playedCards.clear();
            playedCardsNew.clear();
            extras.add(game.swampHagAttacks(event.getPlayer()));
            extras.add(game.hauntedWoodsAttacks(event.getPlayer()));
            extras.add(game.enchantressAttacks(event.getPlayer()));
        } else if (event.getType() == EventType.TurnEnd) {
            playedCards.clear();
            playedCardsNew.clear();
            
            int islandSize = event.player.island.size();
            extras.add(islandSize);
            int nativeVillageSize = event.player.nativeVillage.size();
            extras.add(nativeVillageSize);
        } else if (isPlayersTurn(event) && event.getType() == EventType.PlayingCard || event.getType() == EventType.PlayingDurationAction || event.getType() == EventType.CallingCard) {
            playedCards.add(event.getCard());
            playedCardsNew.add(event.newCard);
        } else if (event.getType() == EventType.GameOver) {
            curPlayer = event.getPlayer();
            curContext = context;
            isFinal = true;
            newTurn = true;
            Map<Object, Integer> counts = curPlayer.getVictoryCardCounts();
            extras.add(curPlayer.getPlayerName());
            extras.add(counts);
            extras.add(curPlayer.getVictoryPointTotals(counts));
            extras.add(curPlayer.getVictoryTokensTotals());
            long duration = System.currentTimeMillis() - whenStarted;
            extras.add(duration);
            if (!event.getContext().cardsSpecifiedOnStartup()) {
                extras.add(event.getContext().getGameType());
            } else {
                extras.add(null);
            }
        } else if (event.getType() == EventType.CantBuy) {
            cards = context.getCantBuy().toArray(new Card[0]);
        } else if (event.getType() == EventType.GuildsTokenSpend) {
            if (event.getComment() != null) {
                extras.add(event.getComment());
            }
        } else if (event.getType() == EventType.DebtTokensObtained || 
        		event.getType() == EventType.DebtTokensPaidOff ||
        		event.getType() == EventType.DebtTokensPutOnPile ||
                event.getType() == EventType.DebtTokensTakenFromPile ||
        		event.getType() == EventType.VPTokensObtained ||
        		event.getType() == EventType.VPTokensPutOnPile ||
        		event.getType() == EventType.VPTokensTakenFromPile ||
        		event.getType() == EventType.MountainPassBid) {
        	extras.add(event.getAmount());
        } else if (event.getType() == EventType.TravellerExchanged) {
            extras.add(event.responsible);
        } else if (event.getType() == EventType.MountainPassWinner) {
        	extras.add(event.getAmount());
        	if (event.getPlayer() != null) {
        		playerName = event.getPlayer().getPlayerName();
        	}
        } else if (event.getType() == EventType.Status) {
            String coin = "" + context.getCoinAvailableForBuy();
            if(context.potions > 0)
                coin += "p";
            coin = "(" + coin + ")"; // <" + String.valueOf(event.player.discard.size()) + ">";
            extras.add("" + context.getActionsLeft());
            extras.add("" + context.getBuysLeft());
            extras.add(coin);
        }
        
        boolean sendCard = (event.isCardPrivate() && event.getPlayer() != null && event.getPlayer() == this) || !event.isCardPrivate();
        // We need to wait until this point to actually create the event, because the logic above
        // modified some of these variables.
        Event status = fullStatusPacket(curContext == null ? context : curContext, curPlayer, isFinal)
                .setGameEventType(event.getType())
                .setString(playerName)
                .setCard(sendCard ? event.getCard() : null)
                .setBoolean(newTurn);
        status.o.os = extras.toArray();
        status.o.cs = cards;

        // Now we actually send the event.
        if (event.getPlayer() != null) {
            switch (event.getType()) {
                case BuyingCard:
                case CardObtained:
                    comm.put_ts(status.setType(EType.CARDOBTAINED).setString(playerInt).setInteger(cardToInt(event.getCard())));

                    break;
                case CardTrashed:
                    comm.put_ts(status.setType(EType.CARDTRASHED).setString(playerInt).setInteger(cardToInt(event.getCard())));

                    break;
                case CardRevealed:
                    comm.put_ts(status.setType(EType.CARDREVEALED).setString(playerInt).setInteger(cardToInt(event.getCard())));

                    break;
                case PlayerDefended:
                    comm.put_ts(status);
                    //comm.put_ts(status.setType(EType.CARDREVEALED).setString(playerInt).setInteger(cardToInt(event.getCard()))); /*causes error*/

                    break;
                default:
                    if(sendEvent)
                        comm.put_ts(status);
            }
            comm.put_ts(new Event(EType.SLEEP).setInteger(100));
        }
    }

    private boolean isPlayersTurn(GameEvent event) {
		return event.context.game.getCurrentPlayer().equals(event.player);
	}
	private void checkForAchievements(MoveContext context, EventType eventType) {
        if (eventType == EventType.GameOver) {
            int provinces = 0;
            int curses = 0;
            for(Card c : getAllCards()) {
                if(c.equals(Cards.province)) {
                    provinces++;
                }
                if(c.equals(Cards.curse)) {
                    curses++;
                }
            }
            if(provinces == 8 && Game.players.length == 2) {
                achievement(context, "2players8provinces");
            }
            if(provinces >= 10 && (Game.players.length == 3 || Game.players.length == 4)) {
                achievement(context, "3or4players10provinces");
            }
            int vp = this.getVPs();
            if(vp >= 100) {
                achievement(context, "score100");
                
                // without Prosperity?
                boolean prosperity = false;
                if (context.game.isColonyInGame() || context.game.isPlatInGame()) {
                    prosperity = true;
                }
                else {
                    Card[] cards = context.getCardsInGame(GetCardsInGameOptions.Placeholders, false);
                    for (Card card : cards) {
                        if (Cards.isSupplyCard(card) && card.getExpansion() == Expansion.Prosperity) {
                            prosperity = true;
                            break;
                        }
                    }
                }
                if (!prosperity) {
                    achievement(context, "score100withoutProsperity");
                }
            }
            if(vp >= 500) {
                achievement(context, "score500");
            }
            boolean beatBy50 = true;
            boolean skunk = false;
            boolean beatBy1 = false;
            boolean equalVp = false;
            boolean mostVp = true;
            for(Player opp : context.game.getPlayersInTurnOrder()) {
                if(opp != this) {
                    int oppVP = opp.getVPs();
                    if(oppVP > vp) {
                        mostVp = false;
                    }

                    if(oppVP <= 0) {
                        skunk = true;
                    }
                    if(vp == oppVP) {
                        equalVp = true;
                    }
                    if(vp == oppVP + 1) {
                        beatBy1 = true;
                    }
                    if(vp < oppVP + 50) {
                        beatBy50 = false;
                    }
                }
            }
            if(mostVp && beatBy50 && !equalVp && Game.numPlayers > 1) {
                achievement(context, "score50more");
            }
            if(mostVp && skunk) {
                achievement(context, "skunk");
            }
            if(mostVp && beatBy1 && !equalVp) {
                achievement(context, "score1more");
            }

            if(mostVp && !achievementSingleCardFailed) {
                achievement(context, "singlecard");
            }
            if(mostVp && curses == 13) {
                achievement(context, "13curses");
            }
            // no cards left in the Supply
            boolean allEmpty = true;
            Card[] cards = context.getCardsInGame(GetCardsInGameOptions.Placeholders, true);
            for (Card card : cards) {
                if (Cards.isSupplyCard(card) && !context.game.isPileEmpty(card)) {
                    allEmpty = false;
                    break;
                }
            }
            if(mostVp && allEmpty) {
                achievement(context, "allEmpty");
            }

        } else if (eventType == EventType.TurnEnd) {
            if(context != null && context.getPlayer() == this && context.vpsGainedThisTurn > 30) {
                achievement(context, "gainmorethan30inaturn");
            }
            if(context != null && context.getPlayer() == this) {
                int tacticians = 0;
                for (int i = 0; i < context.player.nextTurnCards.size(); i++)
                {
                    if (context.player.nextTurnCards.get(i).equals(Cards.tactician))
                        tacticians++;
                }
                if (tacticians >= 2)
                    achievement(context, "2tacticians");
            }
        } else if (eventType == EventType.OverpayForCard) {
            if (context != null && context.overpayAmount >= 10) {
                achievement(context, "overpayby10ormore");
            }
        } else if (eventType == EventType.GuildsTokenObtained) {
            if (context != null && getGuildsCoinTokenCount() >= 50) {
                achievement(context, "stockpile50tokens");
            }
        } else if (eventType == EventType.CardTrashed) {
            if(context != null && context.getPlayer() == this && context.cardsTrashedThisTurn > 5) {
                achievement(context, "trash5inaturn");
            }
        }
    }


    @Override
    public String getPlayerName() {
        return name;
    }

    @Override
    public String getPlayerName(boolean maskName) {
        return getPlayerName();
    }

    @Override
    protected Card[] pickCards(MoveContext context, SelectCardOptions sco, int count, boolean exact) {
        if (sco.allowedCards.size() == 0)
            return null;

        Event p = new Event(EType.GETCARD)
                .setInteger(count)
                .setBoolean(exact)
                .setObject(new EventObject(sco));

        p = query(context, p, EType.CARD);
        if (p == null)
            return null;
        else if (p.i == 0)
            return null;
        else if (p.i == 1 && p.o.is[0] == -1)
            // Hack to notify that "All" was selected
            return new Card[0];
        else
            return intArrToCardArr(p.o.is);
    }

    // If I were designing this from scratch, I may have picked an API that treated this
    // selectBoolean method and the selectOption method the same.  But I'm not designing this from
    // scratch, I'm just trying to cut the strings out of an existing API while minimizing my
    // effort, so we get something that's a little bit disjointed.  Oh well...

    @Override
    public boolean selectBoolean(MoveContext context, Card cardResponsible, Object[] extras) {
        Event p = new Event(EType.GETBOOLEAN)
                .setCard(cardResponsible)
                .setObject(new EventObject(extras));
        p = query(context, p, EType.BOOLEAN);
        if (p == null)
            return false;
        else
            return p.b;
    }

    @Override
    public int selectOption(MoveContext context, Card card, Object[] options) {
        /* choose from options */
        if (options.length == 1) {
            return 0;
        }

        Event p = new Event(EType.GETOPTION)
                .setCard(card)
                .setObject(new EventObject(options));
        p = query(context, p, EType.OPTION);
        if (p == null)
            return -1;
        else
            return p.i;
    }

    @Override
    protected int[] orderCards(MoveContext context, int[] cards) {
        if(cards != null && cards.length == 1) {
            return new int[]{ 0 };
        }

        Event p = new Event(EType.ORDERCARDS)
                .setObject(new EventObject(cards));

        p = query(context, p, EType.CARDORDER);
        if (p == null)
            return null;
        else
            return p.o.is;
    }

    @Override
    public boolean handle(Event e) {
        if (e.t == EType.CARDRANKING) {
            /*TODO frr*/
            //Goal: Sort cards by names in foreign language
            //MyCard[] cardranking = e.o.ng.cards;
        }
        if (e.t == EType.HELLO) {
            name = (e.s == "" ? "Remote player" : e.s);
            debug("Name set: " + name);
            String[] players = new String[allPlayers.size()];
            for (Player p : allPlayers)
                players[allPlayers.indexOf(p)] = p.getPlayerName();

            //	try {
            comm.put_ts(new Event(EType.NEWGAME).setObject(new EventObject(new NewGame(myCardsInPlay, players))));
            playerJoined();
            //	} catch (Exception e1) {
            // TODO:Because put_ts is asynchronous, this will not work the way it was intended. Is that bad?
            // Probably not; if the connection is lost right after receiving a HELLO, we will notice soon enough.
            // Maybe we should implement synchronous sending though.
            //		debug("Could not send NEWGAME -- ignoring, but not setting hasJoined");
            //	}
            return true;
        }
        if (e.t == EType.SAY) {
            vdomServer.say(name + ": " + e.s);
            return true;
        }
        //		if (e.t == EType.DISCONNECT) {
        //			debug("Comms issued disconnect");
        //			comm.doWait(); // clear notification
        //			reconnect("Comms issued disconnect.");
        //		}
        return false;
    }

    private int connect() {
        int port = 0;
        hasJoined = false;
        for (int connections = 0; connections < NUM_RETRIES; connections++) {
            try {
                comm = new Comms(this, nextPort++);
                port = comm.getPort();
                System.out.println("Remote player now listening on port " + port);
                return port;
            } catch (IOException e) {
                // comm = null; // can cause NullPointerExceptions in different threads
                e.printStackTrace();
                debug ("Could not open a server for remote player... attempt " + (connections + 1));
            }
        }
        return port;
    }
    private void disconnect() {
        if (comm != null)
            comm.stop();
        // comm = null; // can cause NullPointerExceptions in different threads
        hasJoined = false;
        myPort = 0;
    }
    private void reconnect(String s) {
        if (vdomServer != null) {
            // TODO reconnect
            debug("Reconnecting... " + s);
            disconnect();
            myPort = connect();
            if (myPort == 0)
                quit(s + "; Could not recreate server");
        } else {
            quit(s);
        }
    }

    public void sendQuit() {
        // There was a string being sent here with QUIT, but it was ignored on the receiving side,
        // so I got rid of it to remove a dependency on ui/Strings.java.
        comm.put_ts(new Event(EType.QUIT));
        disconnect();
    }


    private void quit(String s) {
        debug("!!! Quitting: " + s + " !!!");
        if (vdomServer != null)
            vdomServer.endGame();
        else
            die();
    }

    private void die() {
        if (gameThread == null) {
            debug("die() called, but game thread already dead.");
            kill_game();
            return;
        }
        if (Thread.currentThread() == gameThread) {
            gameThread = null;
            throw new ExitException();
        } else {
            debug("die() called from outside vdom-thread");
            if (dieTries > 4) {
                debug("Could not kill vdom-thread");
                return;
            }
            dieTries++;
            kill_game();
        }
    }

    public void kill_game() {
        // Make main-thread throw an ExitException.
        vdomServer = null;
        playerJoined(); // Hack: Need thread to wake up
        if (comm != null) {
            comm.stop();
        } else {
            try
            {
                Thread.sleep(500); // HACK: wait for comm to be created
            } catch (InterruptedException e) { }
            if (comm != null) {
                comm.stop();
            } else {
                debug("Could not kill vdom thread");
            }
        }
    }
}
