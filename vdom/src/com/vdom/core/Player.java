package com.vdom.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import com.vdom.api.Card;
import com.vdom.api.GameEvent;

public abstract class Player {

    Random rand = new Random(System.currentTimeMillis());

    public static final String RANDOM_AI = "Random AI";
    public static final String DISTINCT_CARDS = "Distinct Cards";
    public static final String ONE_COPY_CARDS = "One Copy Cards";
    public static final String THREE_PLUS_COPY_ACTION_CARDS = "Three Plus Copy Action Cards";
    public static final String NON_VICTORY_EMPTY_SUPPLY_PILE_CARDS = "Non Victory Empty Supply Pile Cards";
    public static final String SECOND_MOST_COMMON_ACTION_CARDS = "Second Most Common Action Cards";
    public static final String VICTORY_TOKENS = "Victory Tokens";

    // Only used by InteractivePlayer currently
    private String name;
    public int playerNumber;
    public int shuffleCount = 0;
    protected int turnCount = 0;
    public int vps;
    public boolean win = false;
    public int pirateShipTreasure;

    // The number of coin tokens held by the player
    private int guildsCoinTokenCount;
    private int debtTokenCount;

    private Card checkLeadCard;
    private int victoryTokens;
    private Map<Card, Integer> victoryTokensSource = new TreeMap<Card, Integer>();
    private boolean journeyTokenFaceUp;
    private boolean minusOneCoinTokenOn;
    private boolean minusOneCardTokenOn;
    protected CardList hand;
    protected CardList deck;
    protected CardList discard;
    protected CardList playedCards;
    protected CardList nextTurnCards;
    protected CardList playedByPrince;
    protected CardList nativeVillage;
    protected CardList tavern;
    protected CardList prince;
    protected CardList summon;
    protected CardList island;
    protected CardList haven;
    protected ArrayList<ArrayList<Card>> gear;
    protected ArrayList<ArrayList<Card>> archive;
    protected CardList horseTraders;
    protected Card inheritance;
    protected Card save;
    protected CardList encampment;
    protected Map<Player, Map<Cards.Kind, Integer>> attackDurationEffectsOnOthers;
    public Game game;
    public Player controlPlayer = this;
    public boolean controlled = false;

    public boolean isPossessed() {
        return !controlled && !this.equals(controlPlayer);
    }
    
    public boolean isControlled() {
    	return controlled;
    }
    
    public void stopBeingControlled() {
    	controlPlayer = this;
    	controlled = false;
    }
    
    public void startBeingControlled(Player player) {
    	controlPlayer = player;
    	controlled = true;
    }

    public boolean achievementSingleCardFailed;
    public Card achievementSingleCardFirstKingdomCardBought;

    public void addVictoryTokens(MoveContext context, int vt, Card responsible) {
    	if (vt == 0) return;
        Player p = (!Game.errataPossessedTakesTokens && isPossessed()) ? controlPlayer : this;
        p.victoryTokens += vt;

        responsible = responsible.getTemplateCard();
        if (p.victoryTokensSource.containsKey(responsible))
            p.victoryTokensSource.put(responsible, p.victoryTokensSource.get(responsible) + vt);
        else
            p.victoryTokensSource.put(responsible, vt);

        GameEvent event = new GameEvent(GameEvent.EventType.VPTokensObtained, isPossessed() ? new MoveContext(context.game, p) : context);
        event.setAmount(vt);
        context.game.broadcastEvent(event);

    	//How to track vp gained in a turn by possessing player?
    	if (!isPossessed())
    		context.vpsGainedThisTurn += vt;
    }

    public int getTotalCardsBoughtThisTurn(MoveContext context) {
        return context.getTotalCardsBoughtThisTurn();
    }

    public int getTotalEventsBoughtThisTurn(MoveContext context) {
        return context.getTotalEventsBoughtThisTurn();
    }

    public boolean isAi() {
        return true;
    }

    public void setName(String name) {
        this.name = name.replace("_", " ");
    }

    public int getCurrencyTotal(MoveContext context) {
        int coin = 0;
        for (Card card : getAllCards()) {
            if (card.is(Type.Treasure, context.getPlayer())) {
                coin += card.getAddGold();
                if (card.getKind() == Cards.Kind.Bank) {
                   coin += 1;
                }
            }
        }
        return coin;
    }

    public int getMyCardCount(Card card) {
        if (card.isPlaceholderCard()) {
            int count = 0;
            for (Card template : game.getPile(card).getTemplateCards()) {
                count += Util.getCardCount(getAllCards(), template);
            }
            return count;
        }
        return Util.getCardCount(getAllCards(), card);
    }
    public int getTurnCount() {
        return turnCount;
    }

    public void newTurn() {
        turnCount++;
    }

    public ArrayList<Card> getActionCards(Card[] cards, Player player) {
        ArrayList<Card> actionCards = new ArrayList<Card>();
        for (Card card : cards) {
            if (card.is(Type.Action, player)) {
                actionCards.add(card);
            }
        }

        return actionCards;
    }

    public int getActionCardCount(Card[] cards, Player player) {
        return getActionCards(cards, player).size();
    }

    public int getMyAddActionCardCount() {
        int addActionsCards = 0;
        for (Card card : getAllCards()) {
            if (card.is(Type.Action, this)) {
                if (card.getAddActions() > 0) {
                    addActionsCards++;
                }
            }
        }

        return addActionsCards;
    }

    public int getMyAddCardCardCount() {
        int addCards = 0;
        for (Card card : getAllCards()) {
            if (card.is(Type.Action, this)) {
                if (card.getAddCards() > 0) {
                    addCards++;
                }
            }
        }

        return addCards;
    }

    public int getMyAddActions() {
        int addActions = 0;
        for (Card card : getAllCards()) {
            if (card.is(Type.Action, this)) {
                addActions += card.getAddActions();
            }
        }

        return addActions;
    }

    public int getMyAddCards() {
        int addCards = 0;
        for (Card card : getAllCards()) {
            if (card.is(Type.Action, this)) {
                addCards += card.getAddCards();
            }
        }

        return addCards;
    }

    public int getMyAddBuys() {
        int addBuys = 0;
        for (Card card : getAllCards()) {
            if (card.is(Type.Action, this)) {
                addBuys += card.getAddBuys();
            }
        }

        return addBuys;
    }

    public boolean inHand(Card card) {
        for (Card thisCard : hand) {
            if (thisCard.equals(card)) {
                return true;
            }
        }

        return false;
    }
    
    public boolean inPlay(Card card) {
    	return playedCards.contains(card) || nextTurnCards.contains(card); 
    }

    public boolean isInCardArray(Card card, Card[] list) {
        for (Card thisCard : list) {
            if (thisCard.equals(card)) {
                return true;
            }
        }

        return false;
    }

    public int mineableCards(Card[] hand) {
        int mineableCards = 0;
        for (Card card : hand) {
            if (card.equals(Cards.potion) || card.equals(Cards.loan) || card.equals(Cards.masterpiece) || card.equals(Cards.illGottenGains) || card.equals(Cards.copper) || card.equals(Cards.silver) || card.equals(Cards.gold)) {
                mineableCards++;
            }
        }

        return mineableCards;
    }

    public int inHandCount(Card card) {
        return Util.getCardCount(getHand(), card);
    }

    public Card fromHand(Card card) {
        for (Card thisCard : getHand()) {
            if (thisCard.equals(card)) {
                return thisCard;
            }
        }

        return null;
    }

    public boolean getWin() {
        return win;
    }

    public void initCards() {
        hand = new CardList(this, "Hand");
        deck = new CardList(this, "Deck");
        discard = new CardList(this, "Discard");
        playedCards = new CardList(this, "InPlay");
        nextTurnCards = new CardList(this, "Duration");
        nativeVillage = new CardList(this, "Native Village");
        tavern = new CardList(this, "Tavern");
        prince = new CardList(this, "Prince");
        summon = new CardList(this, "Summon");
        playedByPrince = new CardList(this, "PlayedByPrince");
        island = new CardList(this, "Island");
        haven = new CardList(this, "Haven");
        gear = new ArrayList<ArrayList<Card>>();
        archive = new ArrayList<ArrayList<Card>>();
        horseTraders = new CardList(this, "Horse Traders");
        inheritance = null;
        encampment = new CardList(this, "Encampment");
        attackDurationEffectsOnOthers = new HashMap<Player,Map<Cards.Kind,Integer>>();
    }

    private List<PutBackOption> getPutBackOptions(MoveContext context, int actionsPlayed) {

        // Determine if criteria were met for certain action cards
        boolean victoryBought = context.getVictoryCardsBoughtThisTurn() > 0;
        boolean potionPlayed   = context.countCardsInPlay(Cards.potion) > 0;
        boolean treasurePlayed = context.countTreasureCardsInPlay() > 0;
        int actionsInPlay = context.countActionCardsInPlay();

        List<PutBackOption> options = new ArrayList<PutBackOption>();

        for (Card c: playedCards) {
            if (c.behaveAsCard().equals(Cards.treasury) && !victoryBought) {
                options.add(PutBackOption.Treasury);
            } else if (c.behaveAsCard().equals(Cards.alchemist) && potionPlayed) {
                options.add(PutBackOption.Alchemist);
            } else if (c.behaveAsCard().equals(Cards.walledVillage) && actionsPlayed <= 2) {
                options.add(PutBackOption.WalledVillage);
            } else if (c.behaveAsCard().equals(Cards.herbalist) && treasurePlayed) {
                options.add(PutBackOption.Coin);
            }
        }
        if (actionsInPlay > 0) {
            for (int i = 0; i < context.schemesPlayed; i++) {
                options.add(PutBackOption.Action);
            }
        }
        /* check if all options are simple
         * (Alchemist, Treasury or WalledVillage) */   
        boolean allSimple = true;
        for (PutBackOption o: options) {
            if (   o.equals(PutBackOption.Alchemist)
                || o.equals(PutBackOption.Treasury)
                || o.equals(PutBackOption.WalledVillage))
            {
                continue;
            }
            allSimple = false;
            break;
        }
        if (allSimple && options.size() > 1)
        {
            options.add(PutBackOption.All);
        }
        return options;
    }

    private Card findCard(MoveContext context, Card template) {
        for (Card c: playedCards) {
            if (c.behaveAsCard().equals(template)) {
                return c;
            }
        }
        return null;
    }
    
    protected void cleanupOutOfTurn(MoveContext context) {
    	//So far now, only cards that can happen here are Duplicate 
    	// and Coin of the Realm (when called after playing a Caravan Guard as part of a Reaction)
    	for (Card card : playedCards) {
            CardImpl actualCard = (CardImpl) card;
            actualCard.cloneCount = 1;
        }
    	while (!playedCards.isEmpty()) {
            discard(playedCards.remove(0), null, context, false, true);
        }
    }

    protected void cleanup(MoveContext context) {
        // /////////////////////////////////
        // Discard played cards
        // /////////////////////////////////

        // reset any lingering CloneCounts
        for (Card card : playedCards) {
            CardImpl actualCard = (CardImpl) card;
            actualCard.cloneCount = 1;
        }

        // Check for return-to-deck options
        List<PutBackOption> putBackOptions;
        ArrayList<Card> putBackCards = new ArrayList<Card>();
        int actionsPlayed = context.countActionCardsInPlay();
        
        //return Encampments
        while (!encampment.isEmpty()) {
        	Card toReturn = encampment.removeLastCard();
            game.getGamePile(toReturn).addCard(toReturn);
        }

        while (!(putBackOptions = controlPlayer.getPutBackOptions(context, actionsPlayed)).isEmpty()) {
            PutBackOption putBackOption = controlPlayer.selectPutBackOption(context, putBackOptions);
            if (putBackOption == PutBackOption.None || (isPossessed() && controlPlayer.isAi())) {
                break;
            } else {
                if (putBackOption == PutBackOption.All) {
                    for (PutBackOption p : putBackOptions) {
                        if (p == PutBackOption.Treasury)
                        {
                            Card treasury = findCard(context, Cards.treasury);
                            playedCards.remove(treasury);
                            putBackCards.add(treasury);
                        }
                        if (p == PutBackOption.Alchemist)
                        {
                            Card alchemist = findCard(context, Cards.alchemist);
                            playedCards.remove(alchemist);
                            putBackCards.add(alchemist);
                        }
                        if (p == PutBackOption.WalledVillage) {
                            Card walledVillage = findCard(context, Cards.walledVillage);
                            playedCards.remove(walledVillage);
                            putBackCards.add(walledVillage);
                        }
                    }
                } else if (putBackOption == PutBackOption.Treasury) {
                    Card treasury = findCard(context, Cards.treasury);
                    playedCards.remove(treasury);
                    putBackCards.add(treasury);
                } else if (putBackOption == PutBackOption.Alchemist) {
                    Card alchemist = findCard(context, Cards.alchemist);
                    playedCards.remove(alchemist);
                    putBackCards.add(alchemist);
                } else if (putBackOption == PutBackOption.WalledVillage) {
                    Card walledVillage = findCard(context, Cards.walledVillage);
                    playedCards.remove(walledVillage);
                    putBackCards.add(walledVillage);
                } else if (putBackOption == PutBackOption.Coin) {
                    Card herbalist = findCard(context, Cards.herbalist);
                    playedCards.remove(herbalist);
                    discard(herbalist, null, null, false, true);
                    ArrayList<Card> treasureCards = new ArrayList<Card>();
                    //TODO: selecting card from right place - now there's a difference between treasures in play
                    //      vs treasures that are in nextTurnCards (and a difference from next turn and permanent ones)
                    for(Card card : playedCards) {
                        if(card.is(Type.Treasure, this)) {
                            treasureCards.add(card);
                        }
                    }
                    //TODO: make sure this works right with fake cards here and all
                    for(Card card : nextTurnCards) {
                        if(card.is(Type.Treasure, this)) {
                            treasureCards.add(card);
                        }
                    }

                    if(treasureCards.size() > 0) {
                        Card treasureCard = controlPlayer.herbalist_backOnDeck(context, treasureCards.toArray(new Card[0]));
                        if(treasureCard != null && treasureCard.is(Type.Treasure, this)) {
                        	if (nextTurnCards.contains(treasureCard)) {
                        		nextTurnCards.remove(treasureCard);
                        		putBackCards.add(treasureCard);
                        	} else if (playedCards.contains(treasureCard)) {
                        		playedCards.remove(treasureCard);
                        		putBackCards.add(treasureCard);
                        	}
                        }
                    }
                } else if (putBackOption == PutBackOption.Action) {
                    context.schemesPlayed --;
                    ArrayList<Card> actions = new ArrayList<Card>();
                    for(Card c : playedCards) {
                        if(c.is(Type.Action, context.player)) {
                            actions.add(c);
                        }
                    }
                    if(actions.size() == 0) {
                        break;
                    }

                    Card actionToPutBack = controlPlayer.scheme_actionToPutOnTopOfDeck(((MoveContext) context), actions.toArray(new Card[0]));
                    if(actionToPutBack == null) {
                        break;
                    }
                    int index = playedCards.indexOf((Card) actionToPutBack);
                    if(index == -1) {
                        Util.playerError(this, "Scheme returned invalid card to put back on top of deck, ignoring");
                        break;
                    }

                    Card card = playedCards.remove(index);
                    if (card.behaveAsCard().equals(Cards.hermit) &&
                        (context != null) && 
                        (context.totalCardsBoughtThisTurn == 0)) {
                        controlPlayer.gainNewCard(Cards.madman, card, context);
                    }
                    putBackCards.add(card);
                }
            }
        }

        if (!putBackCards.isEmpty()) {
            // reset any lingering Impersonations
            for (Card card : putBackCards) {
                ((CardImpl) card).stopImpersonatingCard();
            }

            if (putBackCards.size() == 1) {
                putOnTopOfDeck(putBackCards.get(0), context, true);
            } else {
                Card[] orderedCards = controlPlayer.topOfDeck_orderCards(context, putBackCards.toArray(new Card[0]));

                for (int i = orderedCards.length - 1; i >= 0; i--) {
                    Card card = orderedCards[i];
                    putOnTopOfDeck(card, context, true);
                }
            }
        }
        
        Collections.sort(playedCards.toArrayList(), new Util.CardTravellerComparator());
        while (!playedCards.isEmpty()) {
            discard(playedCards.remove(0), null, context, false, true);
            princeCardLeftThePlay(this); // princed travellers may be exchanged
        }
        
        playedByPrince.clear();

        // /////////////////////////////////
        // Discard hand
        // /////////////////////////////////

        while (getHand().size() > 0) {
            discard(hand.remove(0, false), null, null, false, false);
        }

        // /////////////////////////////////
        // Double check that deck/discard/hand all have valid cards.
        // /////////////////////////////////
        checkCardsValid();

    }

    public void debug(String msg) {
        Util.debug(this, msg, false);
    }

    public CardList getHand() {
        return hand;
    }
    
    public int getStashesInHand() {
    	if (hand.size() == 0)
    		return 0;
    	int result = 0;
    	for (Card c : hand) {
    		if (c.equals(Cards.stash)) {
    			result++;
    		}
    	}
    	return result;
    }

    public CardList getDiscard() {
        return discard;
    }

    public int getDeckSize() {
        return deck.size();
    }
    
    public boolean isStashOnDeck() {
    	if (deck.size() == 0)
    		return false;
    	return deck.get(0).equals(Cards.stash);
    }

    public int getDiscardSize() {
        return discard.size();
    }

    public CardList getNativeVillage() {
        return nativeVillage;
    }

    public CardList getTavern() {
        return tavern;
    }

    public CardList getIsland() {
        return island;
    }

    public CardList getPrince() {
        return prince;
    }
    
    public CardList getSummon() {
        return summon;
    }
    
    public Card getInheritance() {
    	return inheritance;
    }

    public CardList getPlayedByPrince() {
        return playedByPrince;
    }
    
    public int getPirateShipTreasure() {
        return pirateShipTreasure;
    }

    public int getGuildsCoinTokenCount()
    {
        return guildsCoinTokenCount;
    }
    
    public int getDebtTokenCount()
    {
        return debtTokenCount;
    }

    public int getMiserTreasure() {
        return  Util.getCardCount(this.tavern, Cards.copper);
    }

    public boolean getMinusOneCoinToken()
    {
        return minusOneCoinTokenOn;
    }
    
    public void setMinusOneCoinToken(boolean state, MoveContext context)
    {
    	if(minusOneCoinTokenOn != state) {
	    	minusOneCoinTokenOn = state;
	        if (context != null) {
	            GameEvent event = new GameEvent(state ? GameEvent.EventType.MinusOneCoinTokenOn : GameEvent.EventType.MinusOneCoinTokenOff , context);
	            context.game.broadcastEvent(event);
	        }
    	}
    }

    public boolean getMinusOneCardToken()
    {
        return minusOneCardTokenOn;
    }
    
    public void setMinusOneCardToken(boolean state, MoveContext context)
    {
    	if(minusOneCardTokenOn != state) {
	    	minusOneCardTokenOn = state;
	        if (context != null) {
	            GameEvent event = new GameEvent(state ? GameEvent.EventType.MinusOneCardTokenOn : GameEvent.EventType.MinusOneCardTokenOff , context);
	            context.game.broadcastEvent(event);
	        }
    	}
    }

    public boolean getJourneyToken()
    {
        return journeyTokenFaceUp;
    }
    
    public boolean flipJourneyToken(MoveContext context)
    {
        journeyTokenFaceUp = !journeyTokenFaceUp;
        if (context != null) {
            GameEvent event = new GameEvent(journeyTokenFaceUp ? GameEvent.EventType.TurnJourneyTokenFaceUp : GameEvent.EventType.TurnJourneyTokenFaceDown , context);
            context.game.broadcastEvent(event);
        }
        return journeyTokenFaceUp;
    }

    public void gainGuildsCoinTokens(int tokenCount)
    {
    	if (tokenCount == 0) return;
    	if (Game.errataPossessedTakesTokens) {
    		guildsCoinTokenCount += tokenCount;
    	} else {
    		controlPlayer.guildsCoinTokenCount += tokenCount;
    	}
    }

    public void spendGuildsCoinTokens(int tokenCount)
    {
        if (tokenCount <= guildsCoinTokenCount)
        {
            guildsCoinTokenCount -= tokenCount;
        }
        else
        {
            Util.playerError(this, "spendGuildsCoinTokens() - Can't spend " + tokenCount + " coin tokens, only have " + guildsCoinTokenCount);
        }
    }
    
    public void gainDebtTokens(int tokenCount)
    {
        controlPlayer.debtTokenCount += tokenCount;
    }
    
    public void payOffDebtTokens(int tokenCount) {
    	if (tokenCount <= debtTokenCount) {
    		debtTokenCount -= tokenCount;
    	} else {
    		Util.playerError(this, "payOffDebtTokens() - Can't pay off " + tokenCount + " debt tokens, only have " + debtTokenCount);
    	}
    }

    public int getVictoryTokens() {
        return victoryTokens;
    }

    public int getAllCardCount() {
        return this.getAllCards().size();
    }

    public ArrayList<Card> getAllCards() {
        ArrayList<Card> allCards = new ArrayList<Card>();
        for (Card card : playedCards) {
            allCards.add(card);
        }
        for (Card card : hand) {
            allCards.add(card);
        }
        for (Card card : discard) {
            allCards.add(card);
        }
        for (Card card : deck) {
            allCards.add(card);
        }
        for (Card card : nextTurnCards) {
            allCards.add(card);
        }
        for (Card card : nativeVillage) {
            allCards.add(card);
        }
        for (Card card : haven) {
            allCards.add(card);
        }
        for (ArrayList<Card> curGear : gear) {
        	allCards.addAll(curGear);
        }
        for (ArrayList<Card> curArchive : archive) {
        	allCards.addAll(curArchive);
        }
        for (Card card : tavern) {
            allCards.add(card);
        }
        for (Card card : island) {
            allCards.add(card);
        }
        for (Card card : prince) {
            allCards.add(card);
        }
        for (Card card : summon) {
            allCards.add(card);
        }
        for (Card card : horseTraders) {
            allCards.add(card);
        }
        if (inheritance != null)
        	allCards.add(inheritance);
        for (Card card : encampment) {
            allCards.add(card);
        }
        if (checkLeadCard != null) {
            allCards.add(checkLeadCard);
        }
        return allCards;
    }

    public Set<Card> getDistinctCards() {
        return new HashSet<Card>(getAllCards());
    }

    public Map<Object, Integer> getVictoryCardCounts() {
        final HashSet<String> distinctCards = new HashSet<String>();
        final Map<Object, Integer> allCardCounts = new HashMap<Object, Integer>();
        final Map<Object, Integer> cardCounts = new HashMap<Object, Integer>();

        // seed counts with all victory cards in play
        for (CardPile pile : this.game.piles.values()) {
        	for(Card card : pile.getTemplateCards()) {
        		if(card.is(Type.Victory, this) || card.is(Type.Curse, this)) {
                    cardCounts.put(card, 0);
                }
        	}
        }

        for(Card card : this.getAllCards()) {
            distinctCards.add(card.getName());
            if (card.is(Type.Victory, this) || card.is(Type.Curse, this)) {
                if(cardCounts.containsKey(card)) {
                    cardCounts.put(card, cardCounts.get(card) + 1);
                } else {
                    cardCounts.put(card, 1);
                }
            }
            if(allCardCounts.containsKey(card)) {
            	allCardCounts.put(card, allCardCounts.get(card) + 1);
            } else {
            	allCardCounts.put(card, 1);
            }
        }
        
        int oneCopyCards = 0;
        for (int copies : allCardCounts.values()) {
        	if (copies == 1)
        		oneCopyCards++;
        }
        
        int threePlusCopyActionCards = 0;
        int highestActionCardCount = 0;
        int secondHighestActionCardCount = 0;
        int nonVictoryEmptySupplyPileCards = 0;
        for (Object o : allCardCounts.keySet()) {
        	if (!(o instanceof Card)) continue;
        	Card c = (Card)o;
        	if (c.is(Type.Action, this)) {
        		int actionCount = allCardCounts.get(c); 
        		if (actionCount >= 3) {
        			threePlusCopyActionCards++;
        		}
			if (actionCount >= highestActionCardCount) {
				secondHighestActionCardCount = highestActionCardCount;
				highestActionCardCount = actionCount;
			} else if (actionCount > secondHighestActionCardCount) {
				secondHighestActionCardCount = actionCount;
			}
        	}
        	if ((!c.is(Type.Victory, this)) && Cards.isSupplyCard(c) && this.game.isPileEmpty(c)) {
        		nonVictoryEmptySupplyPileCards += allCardCounts.get(c);
        	}
        }

        cardCounts.put(DISTINCT_CARDS, distinctCards.size());
        cardCounts.put(ONE_COPY_CARDS, oneCopyCards);
        cardCounts.put(THREE_PLUS_COPY_ACTION_CARDS, threePlusCopyActionCards);
        cardCounts.put(SECOND_MOST_COMMON_ACTION_CARDS, secondHighestActionCardCount);
        cardCounts.put(NON_VICTORY_EMPTY_SUPPLY_PILE_CARDS, nonVictoryEmptySupplyPileCards);

        return cardCounts;
    }

    public Map<Object, Integer> getAllCardCounts() {
        final HashSet<String> distinctCards = new HashSet<String>();
        final Map<Object, Integer> cardCounts = new HashMap<Object, Integer>();

        for(Card card : this.getAllCards()) {
            distinctCards.add(card.getName());
            if(cardCounts.containsKey(card)) {
                cardCounts.put(card, cardCounts.get(card) + 1);
            } else {
                cardCounts.put(card, 1);
            }
        }

        cardCounts.put(DISTINCT_CARDS, distinctCards.size());
        return cardCounts;
    }
    
    public Map<Card, Integer> getTreasureCardCounts() {
    	final Map<Card, Integer> cardCounts = new HashMap<Card, Integer>();
    	for (CardPile pile : this.game.placeholderPiles.values()) {
            for (Card card : pile.getTemplateCards()) {
                if (card.is(Type.Treasure, this)) {
                    cardCounts.put(card, 0);
                }
            }
        }

        for(Card card : this.getAllCards()) {
            if (card.is(Type.Treasure, this)) {
                if(cardCounts.containsKey(card)) {
                    cardCounts.put(card, cardCounts.get(card) + 1);
                } else {
                    cardCounts.put(card, 1);
                }
            }
        }
        return cardCounts;
    }

    public int getCardCount(final Type cardType) {
        return this.getCardCount(cardType, getAllCards());
    }

    public int getCardCount(final Type cardType, ArrayList<Card> cards) {
        int cardCount = 0;

        for (Card card : cards) {
            if (card.is(cardType, this)) {
                cardCount++;
            }
        }

        return cardCount;
    }

    public int getActionCardCount(Player player) {
    	return getActionCardCount(getAllCards(), player);
    }

    public int getActionCardCount(ArrayList<Card> cards, Player player) {
    	int cardCount = 0;
        for (Card c : cards) {
            if (c.is(Type.Action, player)) {
                cardCount++;
            }
        }
        return cardCount;
    }

    public int getVictoryCardCount() {
        return this.getCardCount(Type.Victory);
    }

    public int getDistinctCardCount() {
        return getDistinctCardCount(null);
    }

    public int getDistinctCardCount(ArrayList<Card> cards) {
        if (cards==null) cards = this.getAllCards();
        //        int cardCount = 0;

        final HashSet<String> distinctCards = new HashSet<String>();
        for(Card card : cards) {
            distinctCards.add(card.getName());
        }

        return distinctCards.size();
    }
    
    public int getCastleCardCount(Player player) {
    	return getCastleCardCount(getAllCards(), player);
    }
    
    public int getCastleCardCount(ArrayList<Card> cards, Player player) {
    	int cardCount = 0;
        for (Card c : cards) {
            if (c.is(Type.Castle, player)) {
                cardCount++;
            }
        }
        return cardCount;
    }

    public int calculateLead(Card card) {
        checkLeadCard = card;
        int lead = getVPs();
        checkLeadCard = null;
        return lead;
    }

    public int getVPs() {
        return getVPs(null);
    }

    public int getVPs(Map<Card, Integer> totals) {
        if (totals==null) totals = this.getVictoryPointTotals();
        int vp = 0;
        for(Integer total : totals.values())
            vp += total;
        return vp;
    }

    public Map<Card, Integer> getVictoryPointTotals() {
        return getVictoryPointTotals(null);
    }

    public Map<Card, Integer> getVictoryPointTotals(Map<Object, Integer> counts) {
        if (counts == null) counts = this.getVictoryCardCounts();
        Map<Card, Integer> totals = new TreeMap<Card, Integer>();

        for(Map.Entry<Object, Integer> entry : counts.entrySet()) {
            if(entry.getKey() instanceof Card && ((Card)entry.getKey()).is(Type.Victory, this)) {
                Card victoryCard = (Card) entry.getKey();
                totals.put(victoryCard, victoryCard.getVictoryPoints() * entry.getValue());
            } else if((entry.getKey() instanceof Card) && ((Card)entry.getKey()).is(Type.Curse, null)) {
                Card curseCard = (Card) entry.getKey();
                totals.put(curseCard, curseCard.getVictoryPoints() * entry.getValue());
            }
        }
        
        ArrayList<Card> allCards = this.getAllCards();

        if(counts.containsKey(Cards.gardens))
            totals.put(Cards.gardens, counts.get(Cards.gardens) * (allCards.size() / 10));
        if(counts.containsKey(Cards.duke))
            totals.put(Cards.duke, counts.get(Cards.duke) * counts.get(Cards.duchy));
        if(counts.containsKey(Cards.fairgrounds))
            totals.put(Cards.fairgrounds, counts.get(Cards.fairgrounds) * ((counts.get(DISTINCT_CARDS) / 5) * 2));
        if(counts.containsKey(Cards.vineyard))
            totals.put(Cards.vineyard, counts.get(Cards.vineyard) * (this.getActionCardCount(this) / 3));
        if(counts.containsKey(Cards.silkRoad))
            totals.put(Cards.silkRoad, counts.get(Cards.silkRoad) * (this.getVictoryCardCount() / 4));
        if(counts.containsKey(Cards.feodum))
            totals.put(Cards.feodum, counts.get(Cards.feodum) * (Util.getCardCount(allCards, Cards.silver)  / 3));
        if(counts.containsKey(Cards.distantLands)) {
        	// counts only if on tavern
            counts.put(Cards.distantLands, Util.getCardCount(this.tavern, Cards.distantLands));
            totals.put(Cards.distantLands, counts.get(Cards.distantLands) * 4);
        }
        if (counts.containsKey(Cards.humbleCastle))
            totals.put(Cards.humbleCastle, counts.get(Cards.humbleCastle) * this.getCastleCardCount(this));
        if (counts.containsKey(Cards.kingsCastle))
            totals.put(Cards.kingsCastle, counts.get(Cards.kingsCastle) * this.getCastleCardCount(this) * 2);

        // landmarks
        if (this.game.cardInGame(Cards.banditFort)) {
    		totals.put(Cards.banditFort, (Util.getCardCount(allCards, Cards.silver) + Util.getCardCount(allCards, Cards.gold)) * -2);
        }
        if (this.game.cardInGame(Cards.fountain)) {
    		totals.put(Cards.fountain, (Util.getCardCount(allCards, Cards.copper) >= 10) ? 15 : 0);
        }
        if (this.game.cardInGame(Cards.museum)) {
        	totals.put(Cards.museum, counts.get(DISTINCT_CARDS) * 2);
        }
        if (this.game.cardInGame(Cards.obelisk)) {
        	totals.put(Cards.obelisk, game.obeliskCard != null ? Util.countCardsOfSamePile(game, allCards, game.obeliskCard) * 2 : 0);
        }
        if (this.game.cardInGame(Cards.orchard)) {
        	totals.put(Cards.orchard, counts.get(THREE_PLUS_COPY_ACTION_CARDS) * 4);
        }
        if (this.game.cardInGame(Cards.palace)) {
    		totals.put(Cards.palace, (Math.min(Util.getCardCount(allCards, Cards.copper), 
    				Math.min(Util.getCardCount(allCards, Cards.silver), Util.getCardCount(allCards, Cards.gold)))) * 3);
        }
        if (this.game.cardInGame(Cards.tower)) {
        	totals.put(Cards.tower, counts.get(NON_VICTORY_EMPTY_SUPPLY_PILE_CARDS));
        }
        if (this.game.cardInGame(Cards.triumphalArch)) {
        	totals.put(Cards.triumphalArch, counts.get(SECOND_MOST_COMMON_ACTION_CARDS) * 3);
        }
        if (this.game.cardInGame(Cards.wall)) {
        	totals.put(Cards.wall, allCards.size() > 15 ? 15 - allCards.size() : 0);
        }
        if (this.game.cardInGame(Cards.wolfDen)) {
        	totals.put(Cards.wolfDen, counts.get(ONE_COPY_CARDS) * -3);
        }
        if (this.game.cardInGame(Cards.keep)) {
        	Map<Card, Integer> myWinningTreasures = getTreasureCardCounts();
        	for (Iterator<Map.Entry<Card, Integer>> it = myWinningTreasures.entrySet().iterator(); it.hasNext(); ) {
        		if (it.next().getValue() == 0)
        			it.remove();
        	}
        	for(Player p : game.getPlayersInTurnOrder()) {
        		if (p == this) continue;
        		Map<Card, Integer> otherTreasureCounts = p.getTreasureCardCounts();
        		for (Iterator<Map.Entry<Card, Integer>> it = myWinningTreasures.entrySet().iterator(); it.hasNext(); ) {
        			Map.Entry<Card, Integer> entry = it.next();
        			if (otherTreasureCounts.containsKey(entry.getKey()) && otherTreasureCounts.get(entry.getKey()) > entry.getValue())
            			it.remove();
            	}
        	}
        	totals.put(Cards.keep, myWinningTreasures.size() * 5);
        }
        
        // victory tokens
        totals.put(Cards.victoryTokens, this.getVictoryTokens());

        return totals;
    }

    public Map<Card, Integer> getVictoryTokensTotals() {
        return victoryTokensSource;
    }

    public void clearDurationEffectsOnOtherPlayers() {
    	attackDurationEffectsOnOthers.clear();
    }
    
    public void addDurationEffectOnOtherPlayer(Player player, Cards.Kind effectType) {
    	if (!attackDurationEffectsOnOthers.containsKey(player)) {
    		attackDurationEffectsOnOthers.put(player, new HashMap<Cards.Kind, Integer>());
    	}
    	Map<Cards.Kind, Integer> otherPlayerEffects = attackDurationEffectsOnOthers.get(player); 
    	if (!otherPlayerEffects.containsKey(effectType)) {
    		otherPlayerEffects.put(effectType, 0);
    	}
    	otherPlayerEffects.put(effectType, otherPlayerEffects.get(effectType) + 1);
    }
    
    public int getDurationEffectsOnOtherPlayer(Player player, Cards.Kind effectType) {
    	if (attackDurationEffectsOnOthers.containsKey(player)
			&& attackDurationEffectsOnOthers.get(player).containsKey(effectType)) {
    		return attackDurationEffectsOnOthers.get(player).get(effectType);
    	}
		return 0;
    }

    public Card peekAtDeckBottom() {
        return deck.get(deck.size() - 1);
    }

    public void removeFromDeckBottom() {
        deck.remove(deck.size() - 1);
    }

    public void putOnTopOfDeck(Card card, MoveContext context, boolean UI) {
        putOnTopOfDeck(card);
        if (UI) {
            GameEvent event = new GameEvent(GameEvent.EventType.CardOnTopOfDeck, context);
            event.card = card;
            event.setPlayer(this);
            context.game.broadcastEvent(event);
        }
    }

    public void putOnTopOfDeck(Card card) {
        deck.add(0, card);
    }

    public void replenishDeck(MoveContext context, Card responsible, int cardsLeftToDraw) {
        shuffleCount++;
        shuffleIntoDeck(context, responsible, discard, cardsLeftToDraw);
    }
    
    private void shuffleIntoDeck(MoveContext context, Card responsible, CardList source, int cardsLeftToDraw) {
    	ArrayList<Card> stashes = new ArrayList<Card>();
    	int positionAll = -1;
    	if (context != null) {
	        int numStashes = 0;
	        for (Card c : source) {
	        	if (c.equals(Cards.stash)) {
	        		numStashes++;
	        	}
	        }
	        boolean shuffleNormally = false;
	        if (source.size() == numStashes) {
	        	shuffleNormally = true;
	        } else if (numStashes > 1) {
	        	int afterCardsToDraw = cardsLeftToDraw;
	        	if (afterCardsToDraw < 1)
	        		afterCardsToDraw = source.size() - numStashes;
	        	positionAll = this.controlPlayer.stash_chooseDeckPosition(context, responsible, source.size() - numStashes, numStashes, afterCardsToDraw);
	        	// -1 -> Pass (shuffle normally)
	        	// -2 or below -> (choose individually)
	        	if (positionAll > source.size() - numStashes)
	        		positionAll = source.size() - numStashes;
	        	if (positionAll == -1) {
	        		shuffleNormally = true;
	        	}
	        }
	        if (!shuffleNormally) {
	        	//pull out the Stash cards
	        	for (int i = 0; i < source.size(); ++i) {
        			if (source.get(i).equals(Cards.stash)) {
        				stashes.add(source.remove(i--));
        			}
	        	}
	        }
        }
    	
    	// shuffle
    	while (source.size() > 0) {
            deck.add(source.remove(Game.rand.nextInt(source.size())));
        }
    	
    	while (stashes.size() > 0) {
    		source.add(stashes.remove(0));
    	}
    	
        // add pulled Stash cards back into deck
    	if (source.size() > 0) {
	    	int numStashes = source.size();
	    	for (int i = 0; i < numStashes; ++i) {
	    		int position;
	    		if (positionAll >= 0) {
	    			position = positionAll;
	    			position = Math.min(position, deck.size());
	    		} else {
	    			int afterCurrentCards = cardsLeftToDraw;
	    			if (cardsLeftToDraw < 1) {
	    				afterCurrentCards = deck.size();
	    			}
	    			position = controlPlayer.stash_chooseDeckPosition(context, responsible, deck.size(), 1, afterCurrentCards);
	    			if (position < 0)
	    				position = Game.rand.nextInt(deck.size() + 1);
	    			position = Math.min(position, deck.size());
	    		}
	    		deck.add(position, source.remove(0));
	    	}
    	}
    }

    public void shuffleDeck(MoveContext context, Card responsible) {
        CardList tempDeck = new CardList(this, name);
        while (deck.size() > 0) {
            tempDeck.add(deck.remove(0));
        }
        shuffleIntoDeck(context, responsible, tempDeck, 0);
    }

    public void checkCardsValid() {
        hand.checkValid();
        discard.checkValid();
        deck.checkValid();
    }

    protected void discardRemainingCardsFromHand(MoveContext context, Card[] cardsToKeep, Card responsibleCard, int keepCardCount) {
        ArrayList<Card> keepCards = new ArrayList<Card>(Arrays.asList(cardsToKeep));

        if (keepCardCount > 0) {
            boolean bad = false;
            if (cardsToKeep == null || cardsToKeep.length != keepCardCount) {
                bad = true;
            } else {
                ArrayList<Card> handCopy = Util.copy(hand);
                for (Card cardToKeep : cardsToKeep) {
                    if (!handCopy.remove(cardToKeep)) {
                        bad = true;
                        break;
                    }
                }
            }

            if (bad) {
                Util.playerError(this, responsibleCard.getName() + " discard error, just keeping first " + keepCardCount);
                cardsToKeep = new Card[keepCardCount];
                for (int i = 0; i < keepCardCount; i++) {
                    cardsToKeep[i] = hand.get(i);
                }
            }


        }

        // Discard remaining cards
        for (int i = hand.size(); i > 0; ) {
            Card card = hand.get(--i);
            if (keepCards.contains(card)) {
                keepCards.remove(card);
            } else {
                hand.remove(i, false);
                discard(card, responsibleCard, context);
            }
        }
    }

    public void discard(Card card, Card responsible, MoveContext context) {
        discard(card, responsible, context, true, false);
    }

    private Card traveller_exchange(MoveContext context, Card card) {
    	Card exchange = null;
    	if (card.behaveAsCard().equals(Cards.peasant)) exchange = Cards.soldier;
    	if (card.equals(Cards.soldier)) exchange = Cards.fugitive;
    	if (card.equals(Cards.fugitive)) exchange = Cards.disciple;
    	if (card.equals(Cards.disciple)) exchange = Cards.teacher;
    	if (card.behaveAsCard().equals(Cards.page)) exchange = Cards.treasureHunter;
    	if (card.equals(Cards.treasureHunter)) exchange = Cards.warrior;
    	if (card.equals(Cards.warrior)) exchange = Cards.hero;
    	if (card.equals(Cards.hero)) exchange = Cards.champion;
    	if (!context.isCardOnTop(exchange))
    		exchange = null;
    	if (exchange != null) {
            if(!controlPlayer.traveller_shouldExchange(context, card.behaveAsCard(), exchange))
            	exchange = null;
    	}
    	return exchange;
    }
    
    // TODO make similar way to put cards back on the deck (remove as well?)
    public void discard(Card card, Card responsible, MoveContext context, boolean commandedDiscard, boolean cleanup) { // See rules explanation of Tunnel for what commandedDiscard means.
        boolean willDiscard = false;
        Card exchange = null;
        
        if (card.behaveAsCard().equals(Cards.hermit)) {
            if (!commandedDiscard && 
                (context != null) && 
                (context.totalCardsBoughtThisTurn == 0))
            {
                /* A Hermit that is set aside again by Prince on a turn
                 * where no cards were bought will fail to trash itself,
                 * but you will still gain a Madman
                 */
                if (cleanup && playedByPrince.contains(card)) {
                    willDiscard = true;
                }
                else {
                    trash(card, card, context);
                }
                controlPlayer.gainNewCard(Cards.madman, card, context);
            }
            else
            {
                willDiscard = true;
            }
        }
        else
        {
            willDiscard = true;
        }

        if (willDiscard) {
        	if (!commandedDiscard && cleanup && card.equals(Cards.capital)) {
        		context.getPlayer().controlPlayer.gainDebtTokens(6);
            	GameEvent event = new GameEvent(GameEvent.EventType.DebtTokensObtained, context);
            	event.setAmount(6);
                context.game.broadcastEvent(event);
        		context.game.playerPayOffDebt(this, context);
        	}
        	
            //if discarding during cleanup put the card back on prince
            if (!commandedDiscard && cleanup && playedByPrince.contains(card)) {
                prince.add(card);
                playedByPrince.remove(card);
                if(context != null) {
                    GameEvent event = new GameEvent(GameEvent.EventType.CardSetAside, context);
                    event.card = card;
                    context.game.broadcastEvent(event);
                }
            }
            else if(!commandedDiscard && cleanup && card.is(Type.Traveller, this)) {
                exchange = traveller_exchange(context, card);
                if (exchange != null) {
    				// Return to the pile
    	            game.getPile(card).addCard(card);
    	            discard.add(takeFromPile(exchange));
                }
                else {
                    discard.add(card);
                }
            }
            else {
                discard.add(card);
            }
        }
        if (willDiscard) {
        	if(commandedDiscard && card.equals(Cards.tunnel)) {

                MoveContext tunnelContext = new MoveContext(game, this);

                if(game.pileSize(Cards.gold) > 0 && controlPlayer.tunnel_shouldReveal(tunnelContext)) {
                    reveal(card, card, tunnelContext);
                    gainNewCard(Cards.gold, card, tunnelContext);
                }
            }
        }

        // card left play - stop impersonations
        ((CardImpl) card).stopImpersonatingCard();

        // XXX making game slow; is this necessary?  For that matter, are discarded cards public?
        if(context != null && (commandedDiscard || exchange != null)) {
            GameEvent event;
        	if(exchange != null) {
        		event = new GameEvent(GameEvent.EventType.TravellerExchanged, context);
        		event.card = exchange;
        		event.responsible = card;
        	} else {
            	event = new GameEvent(GameEvent.EventType.CardDiscarded, context);
            	event.card = (card);
            	event.responsible = responsible;
        	}
            
            event.setPlayer(this);
            context.game.broadcastEvent(event);
        }
    }

    public Card gainNewCard(Card cardToGain, Card responsible, MoveContext context) {
        Card card = game.takeFromPileCheckTrader(cardToGain, context);
        if (card != null) {
            GameEvent gainEvent = new GameEvent(GameEvent.EventType.CardObtained, (MoveContext) context);
            gainEvent.card = card;
            gainEvent.responsible = responsible;
            gainEvent.newCard = true;

            // Check if Trader swapped the card, so it can be made responsible, putting the card in the discard
            // pile rather than were it would go otherwise (according to faq)
            if(!cardToGain.equals(card) && card.equals(Cards.silver)) {
                gainEvent.responsible = Cards.trader;
            }

            context.game.broadcastEvent(gainEvent);

            // invoke different actions on gain
            //cardToGain.isGained(context);
        }
        return card;
    }

    public void gainCardAlreadyInPlay(Card card, Card responsible, MoveContext context) {
        if (context != null) {
            GameEvent event = new GameEvent(GameEvent.EventType.CardObtained, context);
            event.card = card;
            event.responsible = responsible;
            event.newCard = false;
            context.game.broadcastEvent(event);
        }
    }

    // test if any prince card left the play
    public void princeCardLeftThePlay(Player currentPlayer) {
        if (currentPlayer.playedByPrince.size() > 0) {
            ArrayList<Card> playedByPrince = new ArrayList<Card>();
            for (int i = 0; i < currentPlayer.playedByPrince.size(); i++) {
                playedByPrince.add(currentPlayer.playedByPrince.remove(i));
            }
            ArrayList<Card> playedCards = new ArrayList<Card>();
            for (int i = 0; i < currentPlayer.playedCards.size(); i++) {
                playedCards.add(currentPlayer.playedCards.get(i));
            }
            for (Card card : playedByPrince) {
                if (playedCards.contains(card)) {
                    playedCards.remove(card);
                    currentPlayer.playedByPrince.add(card);
                }
                else {
                    Util.log("Prince card has left the play:" + card.getName());
                }
            }
        }
    }

    public void broadcastEvent(GameEvent event) {
        game.broadcastEvent(event);
    }

    public Card takeFromPile(Card card) {
        return game.takeFromPile(card);
    }

    public void trash(Card card, Card responsible, MoveContext context) {
        if(context != null) {
            // TODO: Track in main game event listener instead
            context.cardsTrashedThisTurn++;
        }

        GameEvent event = new GameEvent(GameEvent.EventType.CardTrashed, context);
        event.card = card;
        event.responsible = responsible;
        context.game.broadcastEvent(event);
        
        // Add to trash pile
        if (isPossessed()) {
            context.game.possessedTrashPile.add(card);
        } else {
            context.game.trashPile.add(card);
        }
        
        //Add VP token if Tomb is in play
        if (context.game.cardInGame(Cards.tomb)) {
        	addVictoryTokens(context, 1, Cards.tomb);
        }

        // Execute special card logic when the trashing occurs
        if (card.equals(Cards.estate) && getInheritance() != null) {
            getInheritance().behaveAsCard().isTrashed(context);
        } else {
            card.behaveAsCard().isTrashed(context);
        }

        // Market Square trashing reaction
        boolean hasInheritedMarketSquare = Cards.marketSquare.equals(context.getPlayer().getInheritance()) && context.getPlayer().hand.contains(Cards.estate);
        boolean hasMarketSquare = context.getPlayer().hand.contains(Cards.marketSquare);
        if (hasMarketSquare || hasInheritedMarketSquare) {
            ArrayList<Card> marketSquaresInHand = new ArrayList<Card>();

            for (Card c : hand) {
                if (c.getKind() == Cards.Kind.MarketSquare || (hasInheritedMarketSquare && c.equals(Cards.estate))) {
                    marketSquaresInHand.add(c);
                }
            }

            for (Card c : marketSquaresInHand) {
                if (controlPlayer.marketSquare_shouldDiscard(context, c)) {
                    hand.remove(c);
                    discard(c, card, context);
                    gainNewCard(Cards.gold, Cards.marketSquare, context);
                }
            }
        }

    }

    public abstract HuntingGroundsOption huntingGrounds_chooseOption(MoveContext context);

    public abstract Card catacombs_cardToObtain(MoveContext context, int maxCost);

    public void namedCard(Card card, Card responsible, MoveContext context) {
        GameEvent event = new GameEvent(GameEvent.EventType.CardNamed, context);
        event.card = card;
        event.responsible = responsible;
        context.game.broadcastEvent(event);
    }

    public void revealFromHand(Card card, Card responsible, MoveContext context) {
        GameEvent event = new GameEvent(GameEvent.EventType.CardRevealedFromHand, context);
        event.card = card;
        event.responsible = responsible;
        context.game.broadcastEvent(event);
    }

    public void reveal(Card card, Card responsible, MoveContext context) {
        GameEvent event = new GameEvent(GameEvent.EventType.CardRevealed, context);
        event.card = card;
        event.responsible = responsible;
        context.game.broadcastEvent(event);
    }

    public void attacked(Card card, MoveContext context) {
        context.attackedPlayer = this;
        GameEvent event = new GameEvent(GameEvent.EventType.PlayerAttacking, context);
        event.attackedPlayer = this;
        event.card = card.behaveAsCard();
        context.game.broadcastEvent(event);
    }
    
    public static enum SentryOption {
    	Trash,
    	Discard,
    	PutBack
    }

    public static enum CourtierOption {
        AddAction,
        AddBuy,
        AddCoins,
        GainGold
    }

    public static enum LurkerOption {
        TrashActionFromSupply,
        GainFromTrash
    }

    public static enum NoblesOption {
        AddCards,
        AddActions
    }

    public static enum TorturerOption {
        TakeCurse,
        DiscardTwoCards
    }

    public static enum MinionOption {
        AddGold,
        RolloverCards
    }

    public static enum PawnOption {
        AddCard,
        AddAction,
        AddBuy,
        AddGold
    }

    public static enum StewardOption {
        AddCards,
        AddGold,
        TrashCards
    }

    public static enum WatchTowerOption {
        TopOfDeck,
        Trash,
        Normal
    }

    public static enum JesterOption {
        GainCopy,
        GiveCopy
    }

    public static enum TournamentOption {
        GainPrize,
        GainDuchy
    }

    public static enum TrustySteedOption {
        AddCards,
        AddActions,
        AddGold,
        GainSilvers
    }

    public static enum SpiceMerchantOption {
        AddCardsAndAction,
        AddGoldAndBuy
    }

    public static enum PutBackOption {
        All,
        Treasury,
        Alchemist,
        WalledVillage,
        Coin,
        Action,
        None
    }

    public static enum SquireOption {
        AddActions,
        AddBuys,
        GainSilver
    }

    public enum CountFirstOption {
        Discard,
        PutOnDeck,
        GainCopper
    }

    public enum CountSecondOption {
        Coins,
        TrashHand,
        GainDuchy
    }

    public enum GraverobberOption {
        GainFromTrash,
        TrashActionCard
    }

    public enum HuntingGroundsOption {
        GainDuchy, GainEstates
    }

    public enum GovernorOption {
        AddCards,
        GainTreasure,
        Upgrade
    }

    public enum DoctorOverpayOption
    {
        TrashIt,
        DiscardIt,
        PutItBack
    }

    public static enum AmuletOption {
        AddGold,
        TrashCard,
        GainSilver
    }
    
    public static enum ExtraTurnOption {
    	OutpostFirst,
    	MissionFirst,
    	PossessionFirst
	}
    
    public static enum QuestOption {
    	DiscardAttack,
    	DiscardTwoCurses,
    	DiscardSixCards
    }
    
    public static enum CharmOption {
    	OneBuyTwoCoins,
    	NextBuyGainDifferentWithSameCost
    }
    
    public static enum EncampmentOption {
    	RevealGold,
    	RevealPlunder
    }
    
    public static enum WildHuntOption {
    	Draw3AndPlaceToken,
    	GainEstateAndTokens
    }

    // Context is passed for the player to add a GameEventListener
    // if they want or to see what cards the game has, etc.
    public void newGame(MoveContext context) {
    }

    public ArrayList<Card> getTreasuresInHand() {
        ArrayList<Card> treasures = new ArrayList<Card>();

        for (Card c : getHand())
            if (c.is(Type.Treasure, this))
                treasures.add(c);

        return treasures;
    }

    public ArrayList<Card> getVictoryInHand() {
        ArrayList<Card> victory = new ArrayList<Card>();

        for (Card c : getHand())
            if (c.is(Type.Victory, this))
                victory.add(c);

        return victory;
    }

    public ArrayList<Card> getActionsInHand(Player player) {
        ArrayList<Card> actions = new ArrayList<Card>();

        for (Card c : getHand())
            if (c.is(Type.Action, player))
                actions.add(c);

        return actions;
    }

    public abstract String getPlayerName();
    public abstract String getPlayerName(boolean maskName);

    public abstract Card doAction(MoveContext context);

    public abstract Card[] actionCardsToPlayInOrder(MoveContext context);

    public abstract Card doBuy(MoveContext context);

    public abstract Card[] topOfDeck_orderCards(MoveContext context, Card[] cards);

    // ////////////////////////////////////////////
    // Card interactions - cards from the base game
    // ////////////////////////////////////////////
    public abstract Card workshop_cardToObtain(MoveContext context);

    public abstract Card feast_cardToObtain(MoveContext context);

    public abstract Card remodel_cardToTrash(MoveContext context);

    public abstract Card remodel_cardToObtain(MoveContext context, int maxCost, int maxDebtCost, boolean potion);

    public abstract Card[] militia_attack_cardsToKeep(MoveContext context);

    public abstract Card thief_treasureToTrash(MoveContext context, Card[] treasures);

    public abstract Card[] thief_treasuresToGain(MoveContext context, Card[] treasures);

    public abstract boolean chancellor_shouldDiscardDeck(MoveContext context);

    public abstract Card mine_treasureFromHandToUpgrade(MoveContext context);

    public abstract Card mine_treasureToObtain(MoveContext context, int maxCost, int maxDebtCost, boolean potion);
    
    public abstract boolean moneylender_shouldTrashCopper(MoveContext context);

    public abstract Card[] chapel_cardsToTrash(MoveContext context);

    public abstract Card[] cellar_cardsToDiscard(MoveContext context);

    public abstract boolean library_shouldKeepAction(MoveContext context, Card action);

    public abstract boolean spy_shouldDiscard(MoveContext context, Player targetPlayer, Card card);

    public abstract Card bureaucrat_cardToReplace(MoveContext context);
    
    public abstract Card artisan_cardToObtain(MoveContext context);
    
    public abstract Card artisan_cardToReplace(MoveContext context);
    
    public abstract Card bandit_treasureToTrash(MoveContext context, Card[] treasures);
    
    public abstract Card harbinger_cardToPutBackOnDeck(MoveContext context);
    
    public abstract Card[] poacher_cardsToDiscard(MoveContext context, int numToDiscard);
    
    public abstract SentryOption sentry_chooseOption(MoveContext context, Card card, Card[] cards);
    
    public abstract Card[] sentry_cardOrder(MoveContext context, Card[] cards);
    
    public abstract boolean vassal_shouldPlayCard(MoveContext context, Card card);

    // ////////////////////////////////////////////
    // Card interactions - cards from Intrigue
    // ////////////////////////////////////////////
    public abstract Card[] secretChamber_cardsToDiscard(MoveContext context);

    public abstract PawnOption[] pawn_chooseOptions(MoveContext context);

    public abstract TorturerOption torturer_attack_chooseOption(MoveContext context);

    public abstract StewardOption steward_chooseOption(MoveContext context);

    public abstract Card swindler_cardToSwitch(MoveContext context, int cost, int debtCost, boolean potion);

    public abstract Card[] steward_cardsToTrash(MoveContext context);

    public abstract Card[] torturer_attack_cardsToDiscard(MoveContext context);

    public abstract Card courtyard_cardToPutBackOnDeck(MoveContext context);

    public abstract boolean baron_shouldDiscardEstate(MoveContext context);

    public abstract Card ironworks_cardToObtain(MoveContext context);

    public abstract Card masquerade_cardToPass(MoveContext context);

    public abstract Card masquerade_cardToTrash(MoveContext context);

    public abstract boolean miningVillage_shouldTrashMiningVillage(MoveContext context, Card responsible);

    public abstract Card saboteur_cardToObtain(MoveContext context, int maxCost, int maxDebtCost, boolean potion);

    public abstract Card[] scoutPatrol_orderCards(MoveContext context, Card[] cards);

    public abstract Card replace_cardToTrash(MoveContext context);

    public abstract Card replace_cardToObtain(MoveContext context, int maxCost, int maxDebtCost, boolean potion);

    public abstract Card secretPassage_cardToPutInDeck(MoveContext context);

    public abstract int secretPassage_positionToPutCard(MoveContext context, Card card);

    public abstract Card[] mandarin_orderCards(MoveContext context, Card[] cards);

    public abstract NoblesOption nobles_chooseOptions(MoveContext context);

    // Either return two cards, or null if you do not want to trash any cards.
    public abstract Card[] tradingPost_cardsToTrash(MoveContext context);

    public abstract Card wishingWell_cardGuess(MoveContext context, ArrayList<Card> cardList);

    public abstract Card upgrade_cardToTrash(MoveContext context);

    public abstract Card upgrade_cardToObtain(MoveContext context, int exactCost, int debtCost, boolean potion);

    public abstract MinionOption minion_chooseOption(MoveContext context);

    public abstract Card[] secretChamber_cardsToPutOnDeck(MoveContext context);

    public abstract Card courtier_cardToReveal(MoveContext context);

    public abstract CourtierOption[] courtier_chooseOptions(MoveContext context, CourtierOption[] options, int numOptions);

    public abstract Card[] diplomat_cardsToDiscard(MoveContext context);

    public abstract LurkerOption lurker_selectChoice(MoveContext context, LurkerOption[] options);

    public abstract Card lurker_cardToTrash(MoveContext context);

    public abstract Card lurker_cardToGainFromTrash(MoveContext context);

    public abstract Card[] mill_cardsToDiscard(MoveContext context);

    // ////////////////////////////////////////////
    // Card interactions - cards from Seaside
    // ////////////////////////////////////////////
    public abstract Card[] ghostShip_attack_cardsToPutBackOnDeck(MoveContext context);

    public abstract Card salvager_cardToTrash(MoveContext context);

    public abstract Card[] warehouse_cardsToDiscard(MoveContext context);

    public abstract boolean pirateShip_takeTreasure(MoveContext context);

    public abstract Card pirateShip_treasureToTrash(MoveContext context, Card[] treasures);

    public abstract boolean nativeVillage_takeCards(MoveContext context);

    public abstract Card smugglers_cardToObtain(MoveContext context);

    public abstract Card island_cardToSetAside(MoveContext context);

    public abstract Card prince_cardToSetAside(MoveContext context);
    public abstract int duration_cardToPlay(MoveContext context, Object[] cards);
    public abstract Card[] prince_cardCandidates(MoveContext context, ArrayList<Card> cardList, boolean onlyBest);

    public abstract Card blackMarket_chooseCard(MoveContext context, ArrayList<Card> cardList);
    public abstract Card[] blackMarket_orderCards(MoveContext context, Card[] cards);
    
    public abstract Card haven_cardToSetAside(MoveContext context);

    public abstract boolean navigator_shouldDiscardTopCards(MoveContext context, Card[] cards);

    public abstract Card[] navigator_cardOrder(MoveContext context, Card[] cards);

    public abstract Card embargo_supplyToEmbargo(MoveContext context);

    // Will be passed all three cards
    public abstract Card lookout_cardToTrash(MoveContext context, Card[] cards);

    // Will be passed the two cards leftover after trashing one
    public abstract Card lookout_cardToDiscard(MoveContext context, Card[] cards);

    public abstract Card ambassador_revealedCard(MoveContext context);

    public abstract int ambassador_returnToSupplyFromHand(MoveContext context, Card card);

    public abstract boolean pearlDiver_shouldMoveToTop(MoveContext context, Card card);

    public abstract boolean explorer_shouldRevealProvince(MoveContext context);

    // ////////////////////////////////////////////
    // Card interactions - cards from Alchemy
    // ////////////////////////////////////////////

    public abstract Card transmute_cardToTrash(MoveContext context);

    public abstract ArrayList<Card> apothecary_cardsForDeck(MoveContext context, ArrayList<Card> cards);

    public abstract boolean alchemist_backOnDeck(MoveContext context);

    public abstract Card herbalist_backOnDeck(MoveContext context, Card[] cards);

    public abstract Card apprentice_cardToTrash(MoveContext context);

    public abstract Card university_actionCardToObtain(MoveContext context);

    public abstract boolean scryingPool_shouldDiscard(MoveContext context, Player targetPlayer, Card card);

    public abstract Card[] golem_cardOrder(MoveContext context, Card[] cards);

    // ////////////////////////////////////////////
    // Card interactions - cards from Prosperity
    // ////////////////////////////////////////////
    public abstract Card bishop_cardToTrashForVictoryTokens(MoveContext context);

    public abstract Card bishop_cardToTrash(MoveContext context);

    public abstract int countingHouse_coppersIntoHand(MoveContext context, int coppersTotal);
    
    public abstract Card contraband_cardPlayerCantBuy(MoveContext context);

    public abstract Card expand_cardToTrash(MoveContext context);

    public abstract Card expand_cardToObtain(MoveContext context, int maxCost, int maxDebtCost, boolean potion);

    public abstract Card[] forge_cardsToTrash(MoveContext context);

    public abstract Card forge_cardToObtain(MoveContext context, int exactCost);

    public abstract Card[] goons_attack_cardsToKeep(MoveContext context);

    public abstract Card kingsCourt_cardToPlay(MoveContext context);

    public abstract Card throneRoom_cardToPlay(MoveContext context);

    public abstract boolean loan_shouldTrashTreasure(MoveContext context, Card treasure);

    public abstract Card mint_treasureToMint(MoveContext context);

    public abstract boolean mountebank_attack_shouldDiscardCurse(MoveContext context);

    public abstract Card[] rabble_attack_cardOrder(MoveContext context, Card[] cards);

    public abstract boolean royalSealTravellingFair_shouldPutCardOnDeck(MoveContext context, Card responsible, Card card);

    public abstract Card tradeRoute_cardToTrash(MoveContext context);

    public abstract Card[] vault_cardsToDiscardForGold(MoveContext context);

    public abstract Card[] vault_cardsToDiscardForCard(MoveContext context);

    public abstract WatchTowerOption watchTower_chooseOption(MoveContext context, Card card);

    public abstract ArrayList<Card> treasureCardsToPlayInOrder(MoveContext context, int maxCards, Card responsible);

    // ////////////////////////////////////////////
    // Card interactions - cards from Cornucopia
    // ////////////////////////////////////////////
    public abstract Card hamlet_cardToDiscardForAction(MoveContext context);

    public abstract Card hamlet_cardToDiscardForBuy(MoveContext context);

    public abstract Card hornOfPlenty_cardToObtain(MoveContext context, int maxCost);

    public abstract Card[] discardMultiple_cardsToDiscard(MoveContext context, Card responsible, int numToDiscard);

    public abstract JesterOption jester_chooseOption(MoveContext context, Player targetPlayer, Card card);

    public abstract Card remake_cardToTrash(MoveContext context);

    public abstract Card remake_cardToObtain(MoveContext context, int exactCost, int debtCost, boolean potion);

    public abstract boolean tournament_shouldRevealProvince(MoveContext context);

    public abstract TournamentOption tournament_chooseOption(MoveContext context);

    public abstract Card tournament_choosePrize(MoveContext context);

    public abstract Card[] youngWitch_cardsToDiscard(MoveContext context);

    public abstract Card[] followers_attack_cardsToKeep(MoveContext context);

    public abstract TrustySteedOption[] trustySteed_chooseOptions(MoveContext context);

    // ////////////////////////////////////////////
    // Card interactions - cards from Hinterlands
    // ////////////////////////////////////////////
    public abstract Card borderVillage_cardToObtain(MoveContext context, int maxCost);

    public abstract Card farmland_cardToTrash(MoveContext context);

    public abstract Card farmland_cardToObtain(MoveContext context, int cost, int debt, boolean potion);

    public abstract Card stables_treasureToDiscard(MoveContext context);

    public abstract boolean duchess_shouldDiscardCardFromTopOfDeck(MoveContext context, Card card);

    public abstract boolean duchess_shouldGainBecauseOfDuchy(MoveContext context);

    public abstract Card develop_cardToTrash(MoveContext context);

    public abstract Card develop_lowCardToGain(MoveContext context, int cost, int debt, boolean potion);

    public abstract Card develop_highCardToGain(MoveContext context, int cost, int debt, boolean potion);

    public abstract Card[] develop_orderCards(MoveContext context, Card[] cards);

    public abstract Card oasis_cardToDiscard(MoveContext context);

    public abstract boolean foolsGold_shouldTrash(MoveContext context);

    public abstract Card nobleBrigand_silverOrGoldToTrash(MoveContext context, Card[] silverOrGoldCards);

    public abstract boolean jackOfAllTrades_shouldDiscardCardFromTopOfDeck(MoveContext context, Card card);

    public abstract Card jackOfAllTrades_nonTreasureToTrash(MoveContext context);

    public abstract Card spiceMerchant_treasureToTrash(MoveContext context);

    public abstract SpiceMerchantOption spiceMerchant_chooseOption(MoveContext context);

    public abstract Card[] embassy_cardsToDiscard(MoveContext context);

    public abstract Card[] cartographer_cardsFromTopOfDeckToDiscard(MoveContext context, Card[] cards);

    public abstract Card[] cartographer_cardOrder(MoveContext context, Card[] cards);

    public abstract Card scheme_actionToPutOnTopOfDeck(MoveContext context, Card[] actions);

    public abstract boolean tunnel_shouldReveal(MoveContext context);

    public abstract boolean trader_shouldGainSilverInstead(MoveContext context, Card card);

    public abstract Card trader_cardToTrash(MoveContext context);

    public abstract boolean oracle_shouldDiscard(MoveContext context, Player player, ArrayList<Card> cards);

    public abstract Card[] oracle_orderCards(MoveContext context, Card[] cards);

    public abstract boolean illGottenGains_gainCopper(MoveContext context);

    public abstract Card haggler_cardToObtain(MoveContext context, int maxCost, int maxDebtCost, boolean potion);

    public abstract Card[] inn_cardsToDiscard(MoveContext context);

    public abstract boolean inn_shuffleCardBackIntoDeck(MoveContext context, Card card);

    public abstract Card mandarin_cardToReplace(MoveContext context);

    public abstract Card[] margrave_attack_cardsToKeep(MoveContext context);

    public abstract Card getAttackReaction(MoveContext context, Card responsible, boolean defended, Card lastCard);

    public abstract boolean revealBane(MoveContext context);

    public abstract PutBackOption selectPutBackOption(MoveContext context, List<PutBackOption> options);

    // ////////////////////////////////////////////
    // Card interactions - cards from Dark Ages
    // ////////////////////////////////////////////
    public abstract Card rats_cardToTrash(MoveContext context);

    public abstract SquireOption squire_chooseOption(MoveContext context);

    public abstract Card altar_cardToTrash(MoveContext context);

    public abstract Card altar_cardToObtain(MoveContext context);

    public abstract boolean beggar_shouldDiscard(MoveContext context, Card responsible);

    public abstract Card armory_cardToObtain(MoveContext context);

    public abstract Card squire_cardToObtain(MoveContext context);

    public abstract boolean catacombs_shouldDiscardTopCards(MoveContext context, Card[] array);

    public abstract CountFirstOption count_chooseFirstOption(MoveContext context);

    public abstract CountSecondOption count_chooseSecondOption(MoveContext context);

    public abstract Card[] count_cardsToDiscard(MoveContext context);

    public abstract Card count_cardToPutBackOnDeck(MoveContext context);

    public abstract Card forager_cardToTrash(MoveContext context);

    public abstract GraverobberOption graverobber_chooseOption(MoveContext context);

    public abstract Card graverobber_cardToGainFromTrash(MoveContext context);

    public abstract Card graverobber_cardToTrash(MoveContext context);

    public abstract Card graverobber_cardToReplace(MoveContext context, int maxCost, int maxDebt, boolean potion);

    public abstract boolean ironmonger_shouldDiscard(MoveContext context, Card card);

    public abstract Card junkDealer_cardToTrash(MoveContext context);

    public abstract boolean marketSquare_shouldDiscard(MoveContext context, Card reactionCard);

    public abstract Card mystic_cardGuess(MoveContext context, ArrayList<Card> cardList);

    public abstract boolean scavenger_shouldDiscardDeck(MoveContext context);

    public abstract Card scavenger_cardToPutBackOnDeck(MoveContext context);

    public abstract Card[] storeroom_cardsToDiscardForCards(MoveContext context);

    public abstract Card[] storeroom_cardsToDiscardForCoins(MoveContext context);

    public abstract Card procession_cardToPlay(MoveContext context);

    public abstract Card procession_cardToGain(MoveContext context, int exactCost, int debt, boolean potion);

    public abstract Card rebuild_cardToPick(MoveContext context, ArrayList<Card> cardList);

    public abstract Card rebuild_cardToGain(MoveContext context, int maxCost, int maxDebt, boolean costPotion);

    public abstract Card rogue_cardToGain(MoveContext context);

    public abstract Card rogue_cardToTrash(MoveContext context, ArrayList<Card> canTrash);

    public abstract Card counterfeit_cardToPlay(MoveContext context);

    public abstract Card pillage_opponentCardToDiscard(MoveContext context, ArrayList<Card> handCards);

    public abstract boolean hovel_shouldTrash(MoveContext context);

    public abstract Card deathCart_actionToTrash(MoveContext context);

    public abstract Card[] urchin_attack_cardsToKeep(MoveContext context);

    public abstract boolean urchin_shouldTrashForMercenary(MoveContext context, Card responsible);

    public abstract Card[] mercenary_cardsToTrash(MoveContext context);
    public abstract Card[] mercenary_attack_cardsToKeep(MoveContext context);

    public abstract boolean madman_shouldReturnToPile(MoveContext context);

    public abstract Card hermit_cardToTrash(MoveContext context, ArrayList<Card> cardList, int nonTreasureCountInDiscard);
    public abstract Card hermit_cardToGain(MoveContext context);

    public abstract Card bandOfMisfits_actionCardToImpersonate(MoveContext context, int maxCost);

    // ////////////////////////////////////////////
    // Card interactions - Guilds Expansion
    // ////////////////////////////////////////////
    public abstract int numGuildsCoinTokensToSpend(MoveContext context, int coinTokenTotal, boolean butcher);
    public abstract int amountToOverpay(MoveContext context, Card card);
    public abstract int overpayByPotions(MoveContext context, int availablePotions);
    public abstract Card taxman_treasureToTrash(MoveContext context);
    public abstract Card taxman_treasureToObtain(MoveContext context, int maxCost, int maxDebt, boolean potion);
    public abstract Card plaza_treasureToDiscard(MoveContext context);
    public abstract Card butcher_cardToTrash(MoveContext context);
    public abstract Card butcher_cardToObtain(MoveContext context, int maxCost, int maxDebt, boolean potion);
    public abstract Card advisor_cardToDiscard(MoveContext context, Card[] cards);
    public abstract Card journeyman_cardToPick(MoveContext context, List<Card> cardList);
    public abstract Card stonemason_cardToTrash(MoveContext context);
    public abstract Card stonemason_cardToGain(MoveContext context, int maxCost, int maxDebt, boolean potion);
    public abstract Card stonemason_cardToGainOverpay(MoveContext context, int overpayAmount, boolean potion);
    public abstract Card doctor_cardToPick(MoveContext context, List<Card> cardList);
    public abstract ArrayList<Card> doctor_cardsForDeck(MoveContext context, ArrayList<Card> cards);
    public abstract DoctorOverpayOption doctor_chooseOption(MoveContext context, Card card);
    public abstract Card herald_cardTopDeck(MoveContext context, Card[] cardList);

    // ////////////////////////////////////////////
    // Card interactions - Adventures Expansion
    // ////////////////////////////////////////////
    
    public abstract AmuletOption amulet_chooseOption(MoveContext context);
    public abstract Card amulet_cardToTrash(MoveContext context);
    public abstract Card[] artificer_cardsToDiscard(MoveContext context);
    public abstract Card artificer_cardToObtain(MoveContext context, int cost);
    public abstract Card call_whenGainCardToCall(MoveContext context, Card gainedCard, Card[] possibleCards);
    public abstract Card call_whenActionResolveCardToCall(MoveContext context, Card resolvedAction, Card[] possibleCards);
    public abstract Card call_whenTurnStartCardToCall(MoveContext context, Card[] possibleCards);
    public abstract Card disciple_cardToPlay(MoveContext context);
    public abstract Card fugitive_cardToDiscard(MoveContext context);
    public abstract Card[] gear_cardsToSetAside(MoveContext context);
    public abstract Card hero_treasureToObtain(MoveContext context);
    public abstract boolean traveller_shouldExchange(MoveContext context, Card traveller, Card exchange);
    public abstract Card messenger_cardToObtain(MoveContext context);
    public abstract boolean messenger_shouldDiscardDeck(MoveContext context);
    public abstract boolean miser_shouldTakeTreasure(MoveContext context);
    public abstract Card ratcatcher_cardToTrash(MoveContext context);
    public abstract boolean raze_shouldTrashRazePlayed(MoveContext context, Card responsible);
    public abstract Card raze_cardToTrash(MoveContext context);
	public abstract Card raze_cardToKeep(MoveContext context, Card[] cards);
	public abstract Card soldier_cardToDiscard(MoveContext context);
	public abstract PlayerSupplyToken teacher_tokenTypeToMove(MoveContext context);
	public abstract Card teacher_actionCardPileToHaveToken(MoveContext context, PlayerSupplyToken token);
	public abstract Card transmogrify_cardToTrash(MoveContext context);
	public abstract Card transmogrify_cardToObtain(MoveContext context, int maxCost, int maxDebtCost, boolean potion);
    public abstract int cleanup_wineMerchantToDiscard(MoveContext context, int wineMerchantTotal);
    public abstract int cleanup_wineMerchantEstateToDiscard(MoveContext context, int wineMerchantTotal);
        
    // ///////////////////////////////////////////////
    // Card interactions - Adventures Expansion Events
    // ///////////////////////////////////////////////
    
    public abstract Card alms_cardToObtain(MoveContext context);
    public abstract Card ball_cardToObtain(MoveContext context);
    public abstract Card[] bonfire_cardsToTrash(MoveContext context);
    public abstract Card ferry_actionCardPileToHaveToken(MoveContext context);
    public abstract Card inheritance_actionCardTosetAside(MoveContext context);
    public abstract Card lostArts_actionCardPileToHaveToken(MoveContext context);
    public abstract Card pathfinding_actionCardPileToHaveToken(MoveContext context);
    public abstract Card[] pilgrimage_cardsToGain(MoveContext context);
    public abstract Card plan_actionCardPileToHaveToken(MoveContext context);
    public abstract QuestOption quest_chooseOption(MoveContext context);
    public abstract Card quest_attackCardToDiscard(MoveContext context, Card[] attacks);
    public abstract Card[] quest_cardsToDiscard(MoveContext context);
    public abstract Card save_cardToSetAside(MoveContext context);
    public abstract Card scoutingParty_cardToDiscard(MoveContext context, Card[] revealedCards);
    public abstract Card seaway_cardToObtain(MoveContext context);
    public abstract Card summon_cardToObtain(MoveContext context);
    public abstract Card training_actionCardPileToHaveToken(MoveContext context);
    public abstract Card trashingToken_cardToTrash(MoveContext context);
    public abstract Card[] trade_cardsToTrash(MoveContext context);
    
    public abstract ExtraTurnOption extraTurn_chooseOption(MoveContext context, ExtraTurnOption[] options);
    

    // ////////////////////////////////////////////
    // Card interactions - Empires Expansion
    // ////////////////////////////////////////////
    public abstract int numDebtTokensToPayOff(MoveContext context);
    public abstract Card advance_actionToTrash(MoveContext context);
    public abstract Card advance_cardToObtain(MoveContext context);
    public abstract Card annex_cardToKeepInDiscard(MoveContext context, Card[] cards, int cardsLeft);
    public abstract Card archive_cardIntoHand(MoveContext context, Card[] cards);
    public abstract Card arena_cardToDiscard(MoveContext context);
    public abstract Card banquet_cardToObtain(MoveContext context);
    public abstract boolean bustlingVillage_settlersIntoHand(MoveContext context, int coppers, int settlers);
    public abstract Card catapult_cardToTrash(MoveContext context);
    public abstract Card[] catapult_attack_cardsToKeep(MoveContext context);
    public abstract CharmOption charm_chooseOption(MoveContext context);
    public abstract Card charm_cardToObtain(MoveContext context, Card boughtCard);
    public abstract Card crown_actionToPlay(MoveContext context);
    public abstract Card crown_treasureToPlay(MoveContext context);
    public abstract Card[] donate_cardsToTrash(MoveContext context);
    public abstract EncampmentOption encampment_chooseOption(MoveContext context, EncampmentOption[] options);
    public abstract Card engineer_cardToObtain(MoveContext context);
    public abstract boolean engineer_shouldTrashEngineerPlayed(MoveContext context);
    public abstract Card[] hauntedCastle_gain_cardsToPutBackOnDeck(MoveContext context);
    public abstract Card gladiator_revealedCard(MoveContext context);
    public abstract boolean gladiator_revealCopy(MoveContext context, Player revealingPlayer, Card card);
    public abstract boolean legionary_revealGold(MoveContext context);
    public abstract Card[] legionary_attack_cardsToKeep(MoveContext context);
    public abstract int mountainPass_getBid(MoveContext context, Player highestBidder, int highestBid, int playersLeftToBid);
    public abstract Card[] opulentCastle_cardsToDiscard(MoveContext context);
    public abstract Card overlord_actionCardToImpersonate(MoveContext context);
    public abstract Card ritual_cardToTrash(MoveContext context);
    public abstract Card sacrifice_cardToTrash(MoveContext context);
    public abstract Card saltTheEarth_cardToTrash(MoveContext context);
    public abstract boolean settlers_copperIntoHand(MoveContext context, int coppers, int settlers);
    public abstract boolean smallCastle_shouldTrashSmallCastlePlayed(MoveContext context, Card responsible);
    public abstract Card smallCastle_castleToTrash(MoveContext context);
    public abstract HuntingGroundsOption sprawlingCastle_chooseOption(MoveContext context);
    public abstract Card tax_supplyToTax(MoveContext context);
    public abstract Card[] temple_cardsToTrash(MoveContext context);
    public abstract WildHuntOption wildHunt_chooseOption(MoveContext context);
    
    // ////////////////////////////////////////////
    // Card interactions - Promotional Cards
    // ////////////////////////////////////////////
    public abstract GovernorOption governor_chooseOption(MoveContext context);

    public abstract Card governor_cardToTrash(MoveContext context);

    public abstract Card governor_cardToObtain(MoveContext context, int exactCost, int debt, boolean potion);

    public abstract Card envoy_cardToDiscard(MoveContext context, Card[] revealedCards);
    
    public abstract int stash_chooseDeckPosition(MoveContext context, Card responsible, int deckSize, int numStashes, int cardsToDraw);
    
    public abstract boolean sauna_shouldPlayAvanto(MoveContext context);
    
    public abstract Card sauna_cardToTrash(MoveContext context);
    
    public abstract boolean avanto_shouldPlaySauna(MoveContext context);
    
    public abstract boolean survivors_shouldDiscardTopCards(MoveContext context, Card[] array);

    public abstract Card[] survivors_cardOrder(MoveContext context, Card[] array);

    public abstract boolean cultist_shouldPlayNext(MoveContext context);

    public abstract Card[] dameAnna_cardsToTrash(MoveContext context);

    public abstract Card knight_cardToTrash(MoveContext context, ArrayList<Card> canTrash);

    public abstract Card[] sirMichael_attack_cardsToKeep(MoveContext context);

    public abstract Card dameNatalie_cardToObtain(MoveContext context);
}
