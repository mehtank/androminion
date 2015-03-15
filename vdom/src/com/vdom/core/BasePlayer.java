package com.vdom.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

import com.vdom.api.ActionCard;
import com.vdom.api.Card;
import com.vdom.api.CardCostComparator;
import com.vdom.api.CardValueComparator;
import com.vdom.api.CurseCard;
import com.vdom.api.DurationCard;
import com.vdom.api.GameEvent;
import com.vdom.api.GameEventListener;
import com.vdom.api.TreasureCard;
import com.vdom.api.VictoryCard;
import com.vdom.comms.SelectCardOptions;
import com.vdom.comms.SelectCardOptions.ActionType;
import com.vdom.core.Player.DoctorOverpayOption;

public abstract class BasePlayer extends Player implements GameEventListener {
    //trash in this order!
    protected static final Card[] EARLY_TRASH_CARDS = new Card[] { Cards.curse, Cards.rats, Cards.overgrownEstate, Cards.ruinedVillage, Cards.ruinedMarket, Cards.hovel, Cards.survivors, Cards.ruinedLibrary, Cards.abandonedMine, Cards.virtualRuins, Cards.estate };
    protected static final Card[] LATE_TRASH_CARDS = new Card[] { Cards.curse, Cards.rats, Cards.overgrownEstate, Cards.ruinedVillage, Cards.ruinedMarket, Cards.survivors, Cards.ruinedLibrary, Cards.abandonedMine, Cards.virtualRuins, Cards.estate, Cards.copper, Cards.masterpiece };
    
    protected Random rand = new Random(System.currentTimeMillis());
    protected static final int COST_MAX = 11;
    protected int actionCardCount = 0;
    protected int throneRoomAndKingsCourtCount = 0;
    protected int potionCount = 0;
    protected int midGame;
    
    protected HashSet<Card> reactedSet = new HashSet<Card>();

    @Override
    public void newGame(MoveContext context) {
        // When multiple games are played in one session, the same Player object
        // is used, so reset any fields in this method.
        turnCount = 0;
        throneRoomAndKingsCourtCount = 0;
        potionCount = 0;
        actionCardCount = 0;
        // All GameListeners are removed after every game, so we have to add ourselves back. Not very
        // Java-like, but does prevent a player from keeping around listeners that should be gone.
        context.addGameListener(this);
        
        midGame = 12;
    }

    public void gameEvent(GameEvent event) {
        // There are quite a few event types, found in the GameEvent.Type enum, that
        // are broadcast.
        if (event.getType() == GameEvent.Type.PlayingAction) {
            reactedSet.clear();
        }
        if(event.getPlayer() == this && (event.getType() == GameEvent.Type.CardObtained || event.getType() == GameEvent.Type.BuyingCard)) {
            if(event.getCard() instanceof ActionCard) {
                actionCardCount++;
            }
            
            if(event.getCard().equals(Cards.throneRoom) || event.getCard().equals(Cards.kingsCourt)) {
                throneRoomAndKingsCourtCount++;
            }
            if(event.getCard().equals(Cards.potion)) {
                potionCount++;
            }
        }
    }

    public Card[] actionCardsToPlayInOrder(MoveContext context) {
        // Should never be called
        return null;
    }

    @Override
    public abstract Card doAction(MoveContext context);
    
    @Override
    public abstract Card doBuy(MoveContext context);
    
	@Override
	public Card getAttackReaction(MoveContext context, Card responsible, boolean defended, Card lastCard) {
		Card[] reactionCards = getReactionCards(defended);
		for (Card c : reactionCards) 
		{
			if (!c.equals(Cards.marketSquare) && !c.equals(Cards.watchTower))
			{
    			if (c.equals(Cards.secretChamber) || c.equals(Cards.moat) || c.equals(Cards.horseTraders) || c.equals(Cards.beggar)) 
    			{
    				if (!reactedSet.contains(c)) 
    				{
    					reactedSet.add(c);
    					return c;
    				} 
    			} 
    			else 
    			{
    				return c;
    			}
			}
		}
		return null;
	}

    // ////////////////////////
    // Helper Methods
    // ////////////////////////

    //taken from MoveContext.getCoinForStatus()
    protected int getCoinEstimate(MoveContext context) {
       int coin = 0;
       int treasurecards = 0;
       int foolsgoldcount = 0;
       int bankcount = 0;
       int venturecount = 0;
       for (Card card : context.player.getHand()) {
           if (card instanceof TreasureCard) {
               coin += ((TreasureCard) card).getValue();
               if (card.getType() != Cards.Type.Spoils) {
                   treasurecards++;
               }
           if (card.getType() == Cards.Type.FoolsGold) {
                   foolsgoldcount++;
                   if (foolsgoldcount > 1) {
                       coin += 3;
                   }
               }
               if (card.getType() == Cards.Type.PhilosophersStone) {
                   coin += (context.player.getDeckSize() + context.player.getDiscardSize()) / 5;
               }
               if (card.getType() == Cards.Type.Bank) {
                   bankcount++;
               }
               if (card.getType() == Cards.Type.Venture) {
                   venturecount++;
                   coin += 1; //estimate: could draw potion or hornOfPlenty but also platinum
                              //Patrick estimates in getCurrencyTotal(list) coin += 1
               }
           }
       }
       coin += bankcount * (treasurecards + venturecount) - (bankcount*bankcount + bankcount) / 2;
       coin += context.player.getGuildsCoinTokenCount();
       
       return coin;
    }
    
    protected Card bestCardInPlay(MoveContext context, int maxCost, boolean mustPick) {
        return bestCardInPlay(context, maxCost, false, false, mustPick);
    }
    
    protected Card bestCardInPlay(MoveContext context, int maxCost, boolean exactCost, boolean mustPick) {
        return bestCardInPlay(context, maxCost, exactCost, false, mustPick);
    }

    protected Card bestCardInPlay(MoveContext context, int maxCost, boolean exactCost, boolean potion, boolean mustPick) {
        return bestCardInPlay(context, maxCost, exactCost, potion, false, true, mustPick);
    }
    
    protected Card bestCardInPlay(MoveContext context, int maxCost, boolean exactCost, boolean potion, boolean actionOnly, boolean victoryCardAllowed, boolean mustPick) {
        return bestCardInPlay(context, maxCost, exactCost, potion, actionOnly, victoryCardAllowed, maxCost, mustPick);
    }

    protected Card bestCardInPlay(MoveContext context, int maxCost, boolean exactCost, boolean potion, boolean actionOnly, boolean victoryCardAllowed, int maxCostWithoutPotion, boolean mustPick) {
        boolean isBuy = (maxCost == -1);
        if (isBuy) {
            maxCost = maxCostWithoutPotion = COST_MAX;
        }
        
        Card[] cards = context.getCardsInGame();
        ArrayList<Card> cardList = new ArrayList<Card>();
        ArrayList<Card> cardListGood = new ArrayList<Card>();
        ArrayList<Card> cardListBad = new ArrayList<Card>();
        for (int i = 0; i < cards.length; i++) {
            Card card = cards[i];
            if (   card.isShelter()
                || card.equals(Cards.abandonedMine) /*choose only virtualRuins*/
                || card.equals(Cards.ruinedLibrary)
                || card.equals(Cards.ruinedMarket)
                || card.equals(Cards.ruinedVillage)
                || card.equals(Cards.survivors)
                || !Cards.isSupplyCard(card)
                || (actionOnly && !(card instanceof ActionCard)) 
                || (!victoryCardAllowed && (card instanceof VictoryCard) && !card.equals(Cards.curse))
                ) {
                /*card not allowed*/
            } else if (   card.equals(Cards.curse) 
                       || isTrashCard(card) 
                       || (card.equals(Cards.potion) && !shouldBuyPotion())
                      ) {
                /*card allowed, but not wanted*/
                cardListBad.add(card);
            } else {
                cardListGood.add(card);
            }
        }
        
        /* two tries: first good cards, then (if forced) bad cards */
        for (int i = 0; i < (mustPick ? 2:1); i++)
        {
            if (i==0)
                cardList = cardListGood;
            else
                cardList = cardListBad;

            if (cardList.isEmpty()) {
                continue;
            }
            
            int cost = maxCostWithoutPotion;
            int highestCost = 0;
            ArrayList<Card> randList = new ArrayList<Card>();
            
            while (cost >= 0) {
                for (Card card : cardList) {
                    int cardCost = card.getCost(context);
                    if (cardCost == cost && Cards.isSupplyCard(card) && context.getCardsLeftInPile(card) > 0) {
                        if ((!exactCost && potion) || (card.costPotion() && potion) || (!card.costPotion() && !potion)) {
                            if ((cardCost <= maxCostWithoutPotion && !card.costPotion()) || (cardCost <= maxCost)) {
                                if (!isBuy || context.canBuy(card)) {
                                    if (highestCost == 0) {
                                        highestCost = cardCost;
                                    }
                                }
                                randList.add(card);
                            }
                        }
                    }
                }
                
                if(exactCost) {
                    break;
                }

                // We return cards within 1 cost to add variety...
                if(--cost < highestCost - 1) {
                    break;
                }
            }
            
            if (randList.size() > 0) {
                Card card = randList.get(this.rand.nextInt(randList.size()));

                if (card.equals(Cards.virtualRuins) || card.equals(Cards.curse))
                {
                    if (!actionOnly) {
                        if (randList.contains(Cards.estate) && victoryCardAllowed)
                            card = Cards.estate;
                        else if (randList.contains(Cards.masterpiece))
                            card = Cards.masterpiece;
                        else if (randList.contains(Cards.copper))
                            card = Cards.copper;
                        else if (randList.contains(Cards.poorHouse))
                            card = Cards.poorHouse;
                        else if (randList.contains(Cards.virtualRuins))
                            card = Cards.virtualRuins;
                    }
                }

                return card;
            }
        }
        
        return null;
    }
    
    public Card lowestCard(MoveContext context, CardList cards, boolean discard) {
        Card[] ret = lowestCards(context, cards, 1, discard);
        if(ret == null) {
            return null;
        }
        
        return ret[0];
    }
    
    public Card[] lowestCards(MoveContext context, CardList cards, int num, boolean discard) {
        return lowestCards(context, cards.toArrayList(), num, discard);
    }
    
    public Card[] lowestCards(MoveContext context, ArrayList<Card> cards, int num, boolean discard/*discard or trash?*/) {
        if (cards == null) {
            return null;
        }
        
        if (cards.size() == 0) {
            return null;
        }
        
        if (cards.size() <= num) {
            return cards.toArray(new Card[0]);
        }

        CardList cardArray = new CardList(controlPlayer, getPlayerName(false));
        for(Card c : cards) {
            cardArray.add(c);
        }

        ArrayList<Card> ret = new ArrayList<Card>();
        
        //if discard victory cards first
        if(discard) {
            //Tunnel first
            while (cardArray.contains(Cards.tunnel)) {
                ret.add(Cards.tunnel);
                cardArray.remove(Cards.tunnel);
                if (ret.size() == num) {
                    return ret.toArray(new Card[ret.size()]);
                }
            }
            for (int i = cardArray.size() - 1; i >= 0; i--) {
                Card card = cardArray.get(i);
                if (isOnlyVictory(card)) {
                    ret.add(card);
                    cardArray.remove(i);
                    if (ret.size() == num) {
                        return ret.toArray(new Card[ret.size()]);
                    }
                }
            }
        }
        //next trash cards
        Card[] trashCards = pickOutCards(cardArray, (num - ret.size()), getTrashCards());
        if (trashCards != null) {
            for(Card c : trashCards) {
                ret.add(c);
                cardArray.remove(c);
                if (ret.size() == num) {
                    return ret.toArray(new Card[ret.size()]);
                }
            }
        }

        // By cost...
        int cost = 1; //start with 1 because all cards left with cost=0 should be good (prizes, Madman, spoils, ...)
        while(cost <= COST_MAX) {
            for (int i = cardArray.size() - 1; i >= 0; i--) {
                Card card = cardArray.get(i);
                if (   card.getCost(context) == cost
                    || card.getCost(context) == 0 && cost == 7) { //prizes are worth 7, see http://boardgamegeek.com/thread/651180/if-prizes-were-part-supply-how-much-would-they-cos
                    ret.add(card);
                    cardArray.remove(i);
                    
                    if(ret.size() == num) {
                        return ret.toArray(new Card[ret.size()]);
                    }
                }
            }
            cost++;
        }

        // Add all...
        for (int i = cardArray.size() - 1; i >= 0; i--) {
            Card card = cardArray.get(i);
            ret.add(card);
            cardArray.remove(i);
            
            if(ret.size() == num) {
                return ret.toArray(new Card[ret.size()]);
            }
        }
        
        // Should never get here, but just in case...
        return ret.toArray(new Card[0]);
    }
    
    public Card pickOutCard(CardList cards, Card[] cardsToMatch) {
        Card[] ret = pickOutCards(cards, 1, cardsToMatch);
        if(ret == null) {
            return null;
        }
        
        return ret[0];
    }

    public Card[] pickOutCards(CardList cards, int num, Card[] cardsToMatch) {
        if(cards == null) {
            return null;
        }
        
        if(cards.size() == 0) {
            return null;
        }
        
        ArrayList<Card> ret = new ArrayList<Card>();
        for(Card match : cardsToMatch) {
            for(Card c : cards) {
                if(c.equals(match)) {
                    ret.add(c);
                }
                
                if(ret.size() == num) {
                    return ret.toArray(new Card[0]);
                }
            }
        }
        
        if(ret.size() == 0) {
            return null;
        }
        
        return ret.toArray(new Card[0]);
    }
    
    public Card[] getTrashCards() {
        if(turnCount < midGame) {
            return EARLY_TRASH_CARDS;
        }
        else {
            return LATE_TRASH_CARDS;
        }
    }
    
    protected Card[] getReactionCards(boolean defended) {
        ArrayList<Card> reactionCards = new ArrayList<Card>();
        boolean moatSelected = false;
        boolean secretChamberSelected = false;

        for (Card c : getHand()) {
            if (c.equals(Cards.moat) && !defended && !moatSelected) {
                reactionCards.add(c);
                moatSelected = true;
            } else if (c.equals(Cards.secretChamber) && !secretChamberSelected) {
                reactionCards.add(c);
                secretChamberSelected = true;
            } else if (c.equals(Cards.horseTraders) || c.equals(Cards.watchTower) || c.equals(Cards.beggar) || c.equals(Cards.marketSquare)) {
                reactionCards.add(c);
            }
        }
        return reactionCards.toArray(new Card[0]);
    }

    @Override
    public Card[] topOfDeck_orderCards(MoveContext context, Card[] cards) {
        return cards;
    }

    // //////////////////
    // Card interactions
    // //////////////////
    @Override
    public Card workshop_cardToObtain(MoveContext context) {
        return bestCardInPlay(context, 4, true);
    }

    @Override
    public Card feast_cardToObtain(MoveContext context) {
        return bestCardInPlay(context, 5, true);
    }

    @Override
    public Card remodel_cardToTrash(MoveContext context) {
        //TODO: better logic
        if (context.getPlayer().getHand().size() == 0) {
            return null;
        }
        
        for (Card c : context.getPlayer().getHand()) {
            if(isTrashCard(c)) {
                return c;
            }
        }
        
        for(int i=0; i < 3; i++) {
            Card c = Util.randomCard(context.getPlayer().getHand());
            if(!(c instanceof VictoryCard)) {
                return c;
            }
        }
        
        return Util.randomCard(context.getPlayer().getHand());
    }

    @Override
    public Card remodel_cardToObtain(MoveContext context, int maxCost, boolean potion) {
        return bestCardInPlay(context, maxCost, false, potion, true);
    }

    @Override
    public Card[] militia_attack_cardsToKeep(MoveContext context) {
        ArrayList<Card> keepers = new ArrayList<Card>();
        ArrayList<Card> discards = new ArrayList<Card>();
        
        // Just add in the non-victory cards...
        for (Card card : context.attackedPlayer.getHand()) {
            if (!shouldDiscard(card)) {
                keepers.add(card);
            } else {
                discards.add(card);
            }
        }

        while (keepers.size() < 3) {
            keepers.add(discards.remove(0));
        }

        // Still more than 3? Remove all but one action...
        while (keepers.size() > 3) {
            int bestAction = -1;
            boolean removed = false;
            for (int i = 0; i < keepers.size(); i++) {
                if (keepers.get(i) instanceof ActionCard) {
                    if (bestAction == -1) {
                        bestAction = i;
                    } else {
                        if(keepers.get(i).getCost(context) > keepers.get(bestAction).getCost(context)) {
                            keepers.remove(bestAction);
                            bestAction = i;
                        }
                        else {
                            keepers.remove(i);
                        }
                        removed = true;
                        break;
                    }
                }
            }
            if (!removed) {
                break;
            }
        }

        // Still more than 3? Start removing copper...
        while (keepers.size() > 3) {
            boolean removed = false;
            for (int i = 0; i < keepers.size(); i++) {
                if (keepers.get(i).equals(Cards.copper)) {
                    keepers.remove(i);
                    removed = true;
                    break;
                }
            }
            if (!removed) {
                break;
            }
        }

        // Still more than 3? Start removing silver...
        while (keepers.size() > 3) {
            boolean removed = false;
            for (int i = 0; i < keepers.size(); i++) {
                if (keepers.get(i).equals(Cards.silver)) {
                    keepers.remove(i);
                    removed = true;
                    break;
                }
            }
            if (!removed) {
                break;
            }
        }

        while (keepers.size() > 3) {
            keepers.remove(0);
        }

        return keepers.toArray(new Card[0]);
    }

    @Override
    public boolean chancellor_shouldDiscardDeck(MoveContext context) {
        return true;
    }

    @Override
    public TreasureCard mine_treasureFromHandToUpgrade(MoveContext context) {
        ArrayList<TreasureCard> handCards = context.getPlayer().getTreasuresInHand();
        Collections.sort(handCards, new CardValueComparator());
        boolean hasSpoils = false;
        while (handCards.remove(Cards.spoils)) {
            hasSpoils = true;
        }

        HashSet<Integer> treasureCardValues = new HashSet<Integer>();
        for (Card card : context.getTreasureCardsInGame()) {
            if (Cards.isSupplyCard(card) && context.getCardsLeftInPile(card) > 0)
                treasureCardValues.add(card.getCost(context));
        }

        for (int i = 0; i < handCards.size(); i++) {
            TreasureCard card = handCards.get(i);
            if (treasureCardValues.contains(card.getCost(context) + 3))
                return card;
        }

        if (handCards.size() > 0)
            return handCards.get(0);
        
        // mine: you MUST trash a treasure
        if (hasSpoils)
            return (TreasureCard) Cards.spoils;

        return null;
    }

    @Override
    public TreasureCard mine_treasureToObtain(MoveContext context, int cost, boolean potion) {
        TreasureCard newCard = null;
        int newCost = -1;
        for (Card card : context.getTreasureCardsInGame()) {
            if (Cards.isSupplyCard(card) && context.getCardsLeftInPile(card) > 0 && card.getCost(context) <= cost && card.getCost(context) >= newCost) {
                if (potion || (!potion && !card.costPotion())) {
                    newCard = (TreasureCard) card;
                    newCost = card.getCost(context);
                }
            }
        }

        return newCard;
    }

    
    @Override
    public Card[] chapel_cardsToTrash(MoveContext context) {
        return pickOutCards(context.getPlayer().getHand(), 4, getTrashCards());
    }

    @Override
    public Card[] cellar_cardsToDiscard(MoveContext context) {
        ArrayList<Card> cards = new ArrayList<Card>();
        for (Card card : context.getPlayer().getHand()) {
            if (context.getActionsLeft() == 0 && card instanceof ActionCard) {
                cards.add(card);
            }
            else if ((!(card instanceof ActionCard) && !(card instanceof TreasureCard)) || card.equals(Cards.cellar) || card.equals(Cards.copper)) {
                cards.add(card);
            }
            else if (shouldDiscard(card)) {
                cards.add(card);
            }
        }

        return cards.toArray(new Card[0]);
    }

    @Override
    public boolean library_shouldKeepAction(MoveContext context, ActionCard action) {
        if (context.getActionsLeft() == 0) {
            return false;
        }
        if (action.isRuins()) {
            return false;
        }
        for (Card card : context.getPlayer().getHand()) {
            if (card instanceof ActionCard && !card.isRuins()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean spy_shouldDiscard(MoveContext context, Player targetPlayer, Card card) {
        boolean ret;
        if (   isOnlyVictory(card)
            || card.equals(Cards.copper)
            || card.equals(Cards.curse)
            || card.isShelter()
            || card.isRuins())
            ret = false;
        else {
            ret = true;
        }

        if (targetPlayer == this) {
            ret = !ret;
        }

        return ret;
    }

    @Override
    public boolean scryingPool_shouldDiscard(MoveContext context, Player targetPlayer, Card card) {
        return controlPlayer.spy_shouldDiscard(context,  targetPlayer, card);
    }

    // ////////////////////////////////////////////
    // Card interactions - cards from the Intrigue
    // ////////////////////////////////////////////
    @Override
    public Card[] secretChamber_cardsToDiscard(MoveContext context) {
        // Discard all victory cards
        ArrayList<Card> cards = new ArrayList<Card>();
        for (Card card : context.getPlayer().getHand()) {
            if (shouldDiscard(card)) {
                cards.add(card);
            }
        }
        return cards.toArray(new Card[0]);
    }

    @Override
    public Card[] secretChamber_cardsToPutOnDeck(MoveContext context) {
        if (context.getPlayer().getHand().size() <= 2) {
            return context.getPlayer().getHand().toArray();
        }

        // Just putting back the first two cards, could be quite a bit smarter here...
        Card[] cards = new Card[2];
        cards[0] = context.getPlayer().getHand().get(0);
        cards[1] = context.getPlayer().getHand().get(1);

        return cards;
    }

    @Override
    public PawnOption[] pawn_chooseOptions(MoveContext context) {
        return new PawnOption[] { PawnOption.AddAction, PawnOption.AddGold };
    }

    @Override
    public TorturerOption torturer_attack_chooseOption(MoveContext context) {
        if (game.pileSize(Cards.curse) <= 0) {
            return Player.TorturerOption.TakeCurse;
        }
        CardList h = context.attackedPlayer.getHand();
        for (Card c : h) {
            if(c.equals(Cards.watchTower) || c.equals(Cards.trader)) {
                return Player.TorturerOption.TakeCurse;
            }
        }
        
        if (h.size() < 5) {
            int count = 0;
            for (Card c : h) {
                if (shouldDiscard(c) || c.equals(Cards.copper)) {
                    count++;
                }
            }

            if (count >=  Math.min(2, h.size())) {
                return Player.TorturerOption.DiscardTwoCards;
            }

            return Player.TorturerOption.TakeCurse;
        }
        else {
            int count = 0;
            for (Card c : h) {
                if (shouldDiscard(c)) {
                    count++;
                }
            }

            if (count >= 2) {
                return Player.TorturerOption.DiscardTwoCards;
            }
            
            if(getCoinEstimate(context) >= 8) {
                return Player.TorturerOption.TakeCurse;
            }
            
            return Player.TorturerOption.DiscardTwoCards;
        }
    }

    @Override
    public StewardOption steward_chooseOption(MoveContext context) {
        if (getCoinEstimate(context) + 2 >= Cards.province.getCost(context)) {
            return StewardOption.AddGold;
        }
        int num = Math.min(2, context.getPlayer().getHand().size());
        if (num > 0) {
            Card[] cards = pickOutCards(context.getPlayer().getHand(), num, getTrashCards());
            if (cards != null && cards.length >= num) {
                return StewardOption.TrashCards;        
            }
        }
        return StewardOption.AddGold;
    }

    @Override
    public Card[] steward_cardsToTrash(MoveContext context) {
        return pickOutCards(context.getPlayer().getHand(), 2, getTrashCards());
    }

    @Override
    public Card swindler_cardToSwitch(MoveContext context, int cost, boolean potion) {
        Card[] cards = context.getCardsInGame();
        ArrayList<Card> changeList = new ArrayList<Card>();
        for (Card card : cards) {
            if (Cards.isSupplyCard(card) && card.getCost(context) == cost && context.getCardsLeftInPile(card) > 0 && card.costPotion() == potion) {
                changeList.add(card);
            }
        }

        boolean latest = game.isColonyInGame()? 
 context.getCardsLeftInPile(Cards.province) < Game.numPlayers || context.getCardsLeftInPile(Cards.colony) < Game.numPlayers : context.getCardsLeftInPile(Cards.province) < Game.numPlayers;

        if (changeList.contains(Cards.seaHag) && context.getCardsLeftInPile(Cards.curse) <= Game.numPlayers) {
            return Cards.seaHag;
        } else if (changeList.contains(Cards.illGottenGains) && context.getCardsLeftInPile(Cards.curse) == 0) {
            return Cards.illGottenGains;
        } else if (changeList.contains(Cards.baron) && context.game.sheltersInPlay) {
            return Cards.baron;
        }
        if (!latest) {
            final Card[] VICTORY_CARDS = new Card[] { Cards.estate, Cards.silkRoad, Cards.feodum, Cards.duke, Cards.duchy, Cards.farmland, Cards.fairgrounds };
            for (Card card : VICTORY_CARDS) {
                if (changeList.contains(card)) {
                    return card;
                }
            }
        }
        final Card[] BAD_CARDS = new Card[] { Cards.curse, Cards.virtualRuins, Cards.pearlDiver, Cards.vagrant, Cards.university, Cards.masterpiece, Cards.loan, Cards.wishingWell, Cards.woodcutter, Cards.chancellor, Cards.develop, Cards.sage, Cards.rats, Cards.potion, Cards.scout, Cards.talisman, Cards.borderVillage, Cards.peddler, Cards.prince };
        for (Card card : BAD_CARDS) {
            if (changeList.contains(card)) {
                return card;
            }
        }
        //mean cards
        ArrayList<Card> keyList = new ArrayList<Card>();
        final Card[] KEY_CARDS = new Card[] { Cards.scryingPool, Cards.swindler, Cards.ambassador, Cards.familiar, Cards.militia, Cards.witch, Cards.mountebank, Cards.torturer, Cards.minion, Cards.illGottenGains, Cards.saboteur, Cards.ghostShip, Cards.wharf, Cards.goons };
        for (Card card : KEY_CARDS) {
            if (changeList.contains(card)) {
                changeList.remove(card);
                keyList.add(card);
            }
        }

        if (changeList.size() > 0) {
            return changeList.get(rand.nextInt(changeList.size()));
        }
        if (keyList.size() > 0) {
            return keyList.get(rand.nextInt(keyList.size()));
        }

        return null;
    }

    @Override
    public Card[] torturer_attack_cardsToDiscard(MoveContext context) {
        return lowestCards(context, context.attackedPlayer.getHand(), 2, true);
    }

    public Card courtyard_cardToPutBackOnDeck(MoveContext context) {
        return context.getPlayer().getHand().get(0);
    }

    @Override
    public boolean baron_shouldDiscardEstate(MoveContext context) {
        return true;
    }

    @Override
    public Card ironworks_cardToObtain(MoveContext context) {
        return bestCardInPlay(context, 4, true);
    }

    @Override
    public Card masquerade_cardToPass(MoveContext context) {
        return lowestCard(context, context.getPlayer().getHand(), false);
    }

    @Override
    public Card masquerade_cardToTrash(MoveContext context) {
        return pickOutCard(context.getPlayer().getHand(), getTrashCards());
    }

    @Override
    public boolean miningVillage_shouldTrashMiningVillage(MoveContext context) {
        /* don't trash prince cards */
        if (context.getPlayer().getPlayedByPrince().contains(Cards.miningVillage)) {
            return false;
        }
        if (turnCount >= midGame || getCoinEstimate(context) >= 6) {
            return true;
        }
        
        return false;
    }

    @Override
    public Card saboteur_cardToObtain(MoveContext context, int maxCost, boolean potion) {
        return bestCardInPlay(context, maxCost, false, potion, false);
    }

    @Override
    public Card[] scout_orderCards(MoveContext context, Card[] cards) {
        return cards;
    }

    @Override
    public Card[] mandarin_orderCards(MoveContext context, Card[] cards) {
        return cards;
    }

    @Override
    public NoblesOption nobles_chooseOptions(MoveContext context) {
        int actionCards = 0;

        for (Card card : context.getPlayer().getHand()) {
            if ((card instanceof ActionCard)) {
                actionCards++;
            }
        }

        if ((context.getActionsLeft() == 0) && (actionCards > 0)) {
            return Player.NoblesOption.AddActions;
        }
        return Player.NoblesOption.AddCards;    }

    // Must return two cards if possible.
    @Override
    public Card[] tradingPost_cardsToTrash(MoveContext context) {
        return lowestCards(context, context.getPlayer().getHand(), 2, false);
    }

    @Override
    public Card wishingWell_cardGuess(MoveContext context, ArrayList<Card> cardList) {
        return Cards.silver;
    }

    @Override
    public Card upgrade_cardToTrash(MoveContext context, Card responsible, boolean passable) {
        if (context.getPlayer().getHand().size() == 0) {
            return null;
        }
        
        if (context.getPlayer().getHand().size() == 1 && !passable) {
            return context.getPlayer().getHand().get(0);
        }

        Card card = pickOutCard(context.getPlayer().getHand(), getTrashCards());
        if (card != null) {
            return card;
        }
        for (Card c : context.getPlayer().getHand()) {
            if(c instanceof VictoryCard) {
                continue;
            }
            
            for(Card avail : context.getCardsInGame()) {
                if(Cards.isSupplyCard(avail) && avail.getCost(context) == c.getCost(context) + 1 && context.getCardsLeftInPile(avail) > 0 && !avail.costPotion()) {
                    return c;
                }
            }
        }
        
        if (card == null && !passable) {
            card = lowestCard(context, context.getPlayer().getHand(), false);
        }

        return card;
    }

    @Override
    public Card upgrade_cardToObtain(MoveContext context, Card responsible, int exactCost, boolean potion) {
        return bestCardInPlay(context, exactCost, true, potion, true);
    }

    @Override
    public MinionOption minion_chooseOption(MoveContext context) {
        if (context.getCoinAvailableForBuy() >= 5) {
            return Player.MinionOption.AddGold;
        }

        if (context.getPlayer().getHand().size() <= 3) {
            return Player.MinionOption.RolloverCards;
        }
        return Player.MinionOption.AddGold;
    }

    // ////////////////////////////////////////////
    // Card interactions - cards from the Seaside
    // ////////////////////////////////////////////
    @Override
    public Card[] ghostShip_attack_cardsToPutBackOnDeck(MoveContext context) {
        ArrayList<Card> cards = new ArrayList<Card>();
        for (int i = 0; i < context.attackedPlayer.getHand().size() - 3; i++) {
            cards.add(context.attackedPlayer.getHand().get(i));
        }

        return cards.toArray(new Card[0]);
    }

    @Override
    public Card[] warehouse_cardsToDiscard(MoveContext context) {
        ArrayList<Card> cardsToDiscard = new ArrayList<Card>();

        for (Card card : context.getPlayer().getHand()) {
            if (shouldDiscard(card)) {
                cardsToDiscard.add(card);
            }

            if (cardsToDiscard.size() == 3) {
                break;
            }
        }

        if (cardsToDiscard.size() < 3) {
            ArrayList<Card> handCopy = new ArrayList<Card>();
            for (Card card : context.getPlayer().getHand()) {
                handCopy.add(card);
            }

            for (Card card : cardsToDiscard) {
                handCopy.remove(card);
            }

            while (cardsToDiscard.size() < 3) {

                cardsToDiscard.add(handCopy.remove(0));

                // Card c = pickACard(context, "Warehouse:Card " + (cardsToDiscard.size() + 1) +
                // " to discard", handCopy.toArray(new Card[0]), false);
                // handCopy.remove(c);
                // cardsToDiscard.add(c);
            }
        }

        return cardsToDiscard.toArray(new Card[0]);
    }

    @Override
    public Card salvager_cardToTrash(MoveContext context) {
        if (context.getPlayer().getHand().size() == 0) {
            return null;
        }
        
        return lowestCard(context, context.getPlayer().getHand(), false);
    }

    @Override
    public boolean pirateShip_takeTreasure(MoveContext context) {
        if (getPirateShipTreasure() == 0) {
            return false;
        }

        if (getCoinEstimate(context) >= 8) {
            return false;
        }
        return this.rand.nextFloat() < getPirateShipTreasure() / 5f;
    }

    public boolean nativeVillage_takeCards(MoveContext context) {
        if (getNativeVillage().size() == 0) {
            return false;
        }

        // Half the time take the cards, half the time add one
        return rand.nextBoolean();
    }

    @Override
    public Card smugglers_cardToObtain(MoveContext context) {
        // Find the most expensive card that is still 6 or less
        Card bestCard = null;
        for (Card card : context.getCardsObtainedByLastPlayer()) {
            if (Cards.isSupplyCard(card) && context.getCardsLeftInPile(card) > 0 && card.getCost(context) < 7 && (card.getCost(context) < 6 || !card.costPotion())) {
                if (bestCard == null || card.getCost(context) > bestCard.getCost(context)) {
                    bestCard = card;
                }
            }
        }
        
        return bestCard;
    }

    @Override
    public Card island_cardToSetAside(MoveContext context) {
        for (Card card : context.getPlayer().getHand()) {
            if (isOnlyVictory(card)) {
                return card;
            }
        }
        return lowestCard(context, context.getPlayer().getHand(), true);
    }

    @Override
    public Card prince_cardToSetAside(MoveContext context) {
        ArrayList<Card> cardList = new ArrayList<Card>();
        for (Card c : context.getPlayer().getHand()) {
            cardList.add(c);
        }
        Card[] randList = prince_cardCandidates(context, cardList, true);
        if (randList.length != 0) {
            return Util.randomCard(randList);
        }
        return null;
    }

    @Override
    public int prince_cardToPlay(MoveContext context, Card[] cards) {
        return 0;
    }

    @Override
    public Card[] prince_cardCandidates(MoveContext context, ArrayList<Card> cardList, boolean onlyBest) {
        ArrayList<Card> actionCards = new ArrayList<Card>();
        ArrayList<Card> randList = new ArrayList<Card>();
        int maxCost = -1;
        for (Card card : cardList) {
            if (card instanceof ActionCard && card.getCost(context) <= 4 && !card.costPotion()) {
                if (   !card.isRuins()
                    && !card.equals(Cards.necropolis)
                    && !((ActionCard) card).trashForced()
                    && !((ActionCardImpl) card).trashOnUse
                    && !(card instanceof DurationCard)
                    && !card.equals(Cards.shantyTown)
                    && !card.equals(Cards.island)
                    && !card.equals(Cards.lookout)
                    && !card.equals(Cards.treasureMap)
                    && !card.equals(Cards.deathCart)
                    && !card.equals(Cards.procession)
                    && !card.equals(Cards.madman)
                    && !card.equals(Cards.prince)
                   ) {
                    actionCards.add(card);
                    if (card.isPrize()) {
                        maxCost = 99;
                    }
                    else {
                        maxCost = Math.max(maxCost, card.getCost(context));
                    }
                }
            }
        }
        for (Card card : actionCards) {
            if (   !onlyBest
                || card.getCost(context) == maxCost
                || maxCost == 99 && card.isPrize() ) {
                randList.add(card);
            }
        }
        return randList.toArray(new Card[0]);
    }
    
    @Override
    public Card blackMarket_chooseCard(MoveContext context, ArrayList<Card> cardList) {
        if(context.gold == 8 && cardList.contains(Cards.prince) && turnCount < midGame && context.cardInGame(Cards.colony) && getMyCardCount(Cards.prince) < 2) {
            ArrayList<Card> allCards = new ArrayList<Card>(getAllCards());
            if (prince_cardCandidates(context, allCards, false).length >= 2 + 2*getMyCardCount(Cards.prince))
                return Cards.prince;
        }
        if (context.gold >= 8) {
            return null;
        }
        
        ArrayList<Card> keyList = new ArrayList<Card>();
        final Card[] KEY_CARDS = new Card[] { Cards.scryingPool, Cards.masquerade, Cards.swindler, Cards.familiar, Cards.militia, Cards.seaHag, Cards.tournament, Cards.youngWitch, Cards.golem, Cards.cultist, Cards.mountebank, Cards.torturer, Cards.minion, Cards.governor, Cards.illGottenGains, Cards.saboteur, Cards.ghostShip, Cards.wharf, Cards.witch, Cards.goons, Cards.grandMarket };
        for (Card card : KEY_CARDS) {
            if (cardList.contains(card) && (context.gold - card.getCost(context, false) <= 2 || context.potions > 0 && card.costPotion())) {
                keyList.add(card);
            }
        }
        if (keyList.size() > 0) {
            return keyList.get(rand.nextInt(keyList.size()));
        }

        keyList.clear();
        for (Card card : cardList) {
            if (context.gold - card.getCost(context, false) <= 1) {
                if (   !card.equals(Cards.treasureMap)
                    && !card.equals(Cards.rats) /*AIs don't play rats*/) {
                    keyList.add(card);
                }
            }
        }
        if (keyList.size() > 0 && rand.nextInt(100) < 70) {
            return keyList.get(rand.nextInt(keyList.size()));
        }

        return null;
    }

    @Override
    public Card[] blackMarket_orderCards(MoveContext context, Card[] cards) {
        return cards;
    }
    
    @Override
    public Card haven_cardToSetAside(MoveContext context) {
        //TODO: better logic
        if (context.getPlayer().getHand().size() == 0) {
            return null;
        }

        return context.getPlayer().getHand().get(0);
    }

    @Override
    public boolean navigator_shouldDiscardTopCards(MoveContext context, Card[] cards) {
        // Discard them if there is more than 2 victory cards
        int victoryCount = 0;
        for (Card card : cards) {
            if (shouldDiscard(card)) {
                victoryCount++;
            }
        }
        return (victoryCount > 2);
    }

    @Override
    public Card[] navigator_cardOrder(MoveContext context, Card[] cards) {
        return cards;
    }

    @Override
    public Card embargo_supplyToEmbargo(MoveContext context) {
        // Embargo a random card
        Card card;
        ArrayList<Card> cardList = new ArrayList<Card> (Arrays.asList(context.getCardsInGame()));
        do {
            card = cardList.remove(rand.nextInt(cardList.size() - 1));
        } while (!game.isValidEmbargoPile(card));
        return card;
    }

    public Card lookout_cardToTrash(MoveContext context, Card[] cards) {
        CardList cl = new CardList(context.getPlayer(), context.getPlayer().getPlayerName());
        for(Card c : cards)
            cl.add(c);
        return lowestCard(context, cl, false);
    }

    public Card lookout_cardToDiscard(MoveContext context, Card[] cards) {
        CardList cl = new CardList(context.getPlayer(), context.getPlayer().getPlayerName());
        for(Card c : cards)
            cl.add(c);
        return lowestCard(context, cl, true);
    }

    @Override
    public Card ambassador_revealedCard(MoveContext context) {
        ArrayList<Card> ambassadorCards = new ArrayList<Card>();
        for(Card c : getTrashCards()) {
            if (!c.isShelter()) {
                ambassadorCards.add(c);
            }
        }
        Card card = pickOutCard(context.getPlayer().getHand(), ambassadorCards.toArray(new Card[0]));
        
        if (card == null) {
            card = lowestCard(context, context.getPlayer().getHand(), false);
        }

        return card;
    }

    @Override
    public int ambassador_returnToSupplyFromHand(MoveContext context, Card card) {
        // Return as many as possible
        int count = 0;
        for (Card cardInHand : context.getPlayer().getHand()) {
            if (cardInHand.equals(card)) {
                count++;
            }

            if (count == 2) {
                break;
            }
        }

        return count;
    }

    @Override
    public boolean pearlDiver_shouldMoveToTop(MoveContext context, Card card) {
        if (isOnlyVictory(card) || card.equals(Cards.curse) || card.equals(Cards.copper)) {
            return false;
        }

        return true;
    }

    @Override
    public boolean explorer_shouldRevealProvince(MoveContext context) {
        return true;
    }

    @Override
    public ActionCard university_actionCardToObtain(MoveContext context) {
        //TODO: better logic
        return (ActionCard) bestCardInPlay(context, 5, false, false, true, false, false);
    }

    @Override
    public Card apprentice_cardToTrash(MoveContext context) {
        Card card = pickOutCard(context.getPlayer().getHand(), getTrashCards());
        if (card == null) {
            card = lowestCard(context, context.getPlayer().getHand(), false);
        }

        return card;
    }

    @Override
    public Card transmute_cardToTrash(MoveContext context) {
        Card card = pickOutCard(context.getPlayer().getHand(), getTrashCards());
        if (card == null) {
            card = lowestCard(context, context.getPlayer().getHand(), false);
        }

        return card;
    }

    @Override
    public boolean alchemist_backOnDeck(MoveContext context) {
        return true;
    }

    @Override
    public TreasureCard herbalist_backOnDeck(MoveContext context, TreasureCard[] cards) {
        if(cards == null || cards.length == 0) {
            return null;
        }
        
        int index = 0;
        int cost = cards[0].getCost(context);
        
        for(int i=1; i < cards.length; i++) {
            if(cards[i].getCost(context) > cost) {
                index = i;
                cost = cards[i].getCost(context);
            }
        }
        
        return cards[index];
    }

    @Override
    public ArrayList<Card> apothecary_cardsForDeck(MoveContext context, ArrayList<Card> cards) {
        return cards;
    }

    @Override
    public Card bishop_cardToTrashForVictoryTokens(MoveContext context) {
        Card card = pickOutCard(context.getPlayer().getHand(), getTrashCards());
        if (card == null) {
            card = lowestCard(context, context.getPlayer().getHand(), false);
        }

        return card;
    }

    @Override
    public Card bishop_cardToTrash(MoveContext context) {
        Card card = pickOutCard(context.getPlayer().getHand(), getTrashCards());
        return card;
    }

    @Override
    public int countingHouse_coppersIntoHand(MoveContext context, int coppersTotal) {
        return coppersTotal;
    }
    
    @Override
    public Card expand_cardToTrash(MoveContext context) {
        Card card = pickOutCard(context.getPlayer().getHand(), getTrashCards());
        if (card == null) {
            card = lowestCard(context, context.getPlayer().getHand(), false);
        }

        return card;
    }

    @Override
    public Card expand_cardToObtain(MoveContext context, int maxCost, boolean potion) {
        return bestCardInPlay(context, maxCost, false, potion, true);
    }

    @Override
    public Card[] forge_cardsToTrash(MoveContext context) {
        return pickOutCards(context.getPlayer().getHand(), context.getPlayer().getHand().size(), getTrashCards());
    }

    @Override
    public Card forge_cardToObtain(MoveContext context, int exactCost) {
        return bestCardInPlay(context, exactCost, true, true);
    }

    @Override
    public Card[] goons_attack_cardsToKeep(MoveContext context) {
        return controlPlayer.militia_attack_cardsToKeep(context);
    }

    @Override
    public TreasureCard mint_treasureToMint(MoveContext context) {
        Card cardToMint = null;
        int cost = -1;
        for (Card c : context.getPlayer().getTreasuresInHand()) {
            if (c instanceof TreasureCard && context.game.pileSize(c) > 0 && Cards.isSupplyCard(c)) {
                if(c.getCost(context) > cost) {
                    cardToMint = c;
                    cost = c.getCost(context);
                }
            }
        }
        
        return (TreasureCard) cardToMint;
    }

    @Override
    public boolean mountebank_attack_shouldDiscardCurse(MoveContext context) {
        return true;
    }

    @Override
    public Card[] rabble_attack_cardOrder(MoveContext context, Card[] cards) {
        return cards;
    }

    @Override
    public Card tradeRoute_cardToTrash(MoveContext context) {
        Card card = pickOutCard(context.getPlayer().getHand(), getTrashCards());
        if (card == null) {
            card = lowestCard(context, context.getPlayer().getHand(), false);
        }

        return card;
    }
    
    @Override
    public Card[] vault_cardsToDiscardForGold(MoveContext context) {
        // TODO:: Finish prosperity
        ArrayList<Card> discardCards = context.getPlayer().getHand().toArrayList();
        for (Iterator<Card> it = discardCards.iterator(); it.hasNext();) {
            Card card = it.next();
            if (card instanceof TreasureCard && !card.equals(Cards.copper))
                it.remove();
        }
        return discardCards.toArray(new Card[0]);
    }

    @Override
    public Card[] vault_cardsToDiscardForCard(MoveContext context) {
        // TODO:: Finish prosperity
        return pickOutCards(context.getPlayer().getHand(), 2, getTrashCards());
    }

    @Override
    public Card contraband_cardPlayerCantBuy(MoveContext context) {
        ArrayList<Card> cantBuy = context.getCantBuy();

        if (game.isColonyInGame() && turnCount > midGame && !cantBuy.contains(Cards.colony)) {
            return Cards.colony;
        } else if (game.isColonyInGame() && turnCount < midGame && game.pileSize(Cards.platinum) > 0 && !cantBuy.contains(Cards.platinum)) {
            return Cards.platinum;
        } else if (turnCount > midGame && !cantBuy.contains(Cards.province)) {
            return Cards.province;
        } else if (game.isColonyInGame() && game.pileSize(Cards.platinum) > 0 && !cantBuy.contains(Cards.platinum)) {
            return Cards.platinum;
        } else if (turnCount > midGame && game.pileSize(Cards.duchy) > 0 && !cantBuy.contains(Cards.duchy)) {
            return Cards.duchy;
        } else if (game.pileSize(Cards.gold) > 0 && !cantBuy.contains(Cards.gold)) {
            return Cards.gold;
        } else if (turnCount > midGame && !cantBuy.contains(Cards.duchy)) {
            return Cards.duchy;
        } else {
            return Cards.silver;
        }
    }

    @Override
    public ActionCard kingsCourt_cardToPlay(MoveContext context) {
        //TODO better logic
        for (Card c : context.getPlayer().getHand()) {
            if(c instanceof ActionCard) {
                return (ActionCard) c;
            }
        }
        
        return null;
    }

    @Override
    public ActionCard throneRoom_cardToPlay(MoveContext context) {
        return controlPlayer.kingsCourt_cardToPlay(context);
    }
    
    @Override
    public boolean loan_shouldTrashTreasure(MoveContext context, TreasureCard treasure) {
        // TODO:: Finish prosperity
        int money = getCurrencyTotal(context);
        for (Card trash : getTrashCards()) {
            if (trash.equals(treasure) && money >= 4) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean royalSeal_shouldPutCardOnDeck(MoveContext context, Card card) {
        if(isOnlyVictory(card)) {
            return false;
        }
        
        return true;
    }

    @Override
    public WatchTowerOption watchTower_chooseOption(MoveContext context, Card card) {
        if(isTrashCard(card)) {
            return WatchTowerOption.Trash;
        }
        
        if(isOnlyVictory(card) || card.equals(Cards.copper)) {
            return WatchTowerOption.Normal;
        }
        
        return WatchTowerOption.TopOfDeck;
    }

    @Override
    public ArrayList<TreasureCard> treasureCardsToPlayInOrder(MoveContext context) {
        ArrayList<TreasureCard> ret = new ArrayList<TreasureCard>();
        
        ArrayList<TreasureCard> cardArray = new ArrayList<TreasureCard>();
        for (Card c : context.getPlayer().getHand()) {
            if(c instanceof TreasureCard) {
                cardArray.add((TreasureCard) c);
            }
        }

        for (int i = cardArray.size() - 1; i >= 0; i--) {
            TreasureCard card = cardArray.get(i);
            if(card.equals(Cards.contraband)) {
                ret.add(cardArray.remove(i));
            }
        }

        for (int i = cardArray.size() - 1; i >= 0; i--) {
            TreasureCard card = cardArray.get(i);
            if (card.equals(Cards.royalSeal)) {
                ret.add(cardArray.remove(i));
            }
        }


        for (int i = cardArray.size() - 1; i >= 0; i--) {
            TreasureCard card = cardArray.get(i);
            if(card.equals(Cards.counterfeit)) {
                ret.add(0, cardArray.remove(i));
            }
        }
        
        for (int i = cardArray.size() - 1; i >= 0; i--) {
            TreasureCard card = cardArray.get(i);
            if(!card.equals(Cards.bank) && !card.equals(Cards.venture) && !card.equals(Cards.hornOfPlenty) && !card.equals(Cards.illGottenGains)) {
                ret.add(cardArray.remove(i));
            }
        }

        for (int i = cardArray.size() - 1; i >= 0; i--) {
            TreasureCard card = cardArray.get(i);
            if (card.equals(Cards.illGottenGains)) {
                ret.add(cardArray.remove(i));
            }
        }
        
        for (int i = cardArray.size() - 1; i >= 0; i--) {
            TreasureCard card = cardArray.get(i);
            if(card.equals(Cards.venture)) {
                ret.add(cardArray.remove(i));
            }
        }

        for (int i = cardArray.size() - 1; i >= 0; i--) {
            TreasureCard card = cardArray.get(i);
            if(card.equals(Cards.hornOfPlenty)) {
                ret.add(cardArray.remove(i));
            }
        }
        
        for (int i = cardArray.size() - 1; i >= 0; i--) {
            TreasureCard card = cardArray.get(i);
            if(card.equals(Cards.bank)) {
                ret.add(cardArray.remove(i));
            }
        }
        
        return ret;
    }

    @Override
    public ActionCard[] golem_cardOrder(MoveContext context, ActionCard[] cards) {
        return cards;
    }

    @Override
    public Card hamlet_cardToDiscardForAction(MoveContext context) {
        int actionCards = 0;
        for (Card c : context.getPlayer().getHand()) {
            if(c instanceof ActionCard) {
                actionCards++;
            }
        }
        
        if(actionCards == 0) {
            return null;
        }
        
        return lowestCard(context, context.getPlayer().getHand(), true);
    }

    @Override
    public Card hamlet_cardToDiscardForBuy(MoveContext context) {
        return null;
    }

    @Override
    public Card hornOfPlenty_cardToObtain(MoveContext context, int maxCost) {
        return bestCardInPlay(context, maxCost, true);
    }

    @Override
    public Card[] horseTraders_cardsToDiscard(MoveContext context) {
        return lowestCards(context, context.getPlayer().getHand(), 2, true);
    }

    @Override
    public JesterOption jester_chooseOption(MoveContext context, Player targetPlayer, Card card) {
        if(card.getCost(context) > 2) {
            return JesterOption.GainCopy;
        }
        
        return JesterOption.GiveCopy;
    }

    @Override
    public Card remake_cardToTrash(MoveContext context) {
        Card c = pickOutCard(context.getPlayer().getHand(), new Card[] { Cards.curse, Cards.estate });
        
        if(c == null) {
            for (Card check : context.getPlayer().getHand()) {
                if((check.getCost(context) == 7 && context.canBuy(Cards.province)) 
                   || (turnCount >= midGame && check.getCost(context) == 4 && context.canBuy(Cards.duchy))){
                    c = check;
                    break;
                }
            }
        }
        
        if(c == null) {
            for (Card check : context.getPlayer().getHand()) {
                if(isOnlyVictory(check))
                    continue;
                
                Card best = bestCardInPlay(context, check.getCost(context) + 1, false);
                
                if(best != null) {
                    c = check;
                    break;
                }
            }
        }

        if (c == null) {
            c = Util.randomCard(context.getPlayer().getHand());
        }
        
        return c;
    }

    @Override
    public Card remake_cardToObtain(MoveContext context, int exactCost, boolean potion) {
        return bestCardInPlay(context, exactCost, true, potion, true);
    }

    @Override
    public boolean tournament_shouldRevealProvince(MoveContext context) {
        return true;
    }

    
    @Override
    public TournamentOption tournament_chooseOption(MoveContext context) {
        for(Card c : context.getCardsInGame()) {
            if(c.isPrize() && context.getPileSize(c) > 0) {
                return TournamentOption.GainPrize;
            }
        }
        return TournamentOption.GainDuchy;
    }

    @Override
    public Card tournament_choosePrize(MoveContext context) {
        for(Card c : context.getCardsInGame()) {
            if(c.isPrize() && context.getPileSize(c) > 0) {
                return c;
            }
        }
        return null;
    }

    @Override
    public Card[] youngWitch_cardsToDiscard(MoveContext context) {
        return lowestCards(context, context.getPlayer().getHand(), 2, true);
    }

    @Override
    public Card[] followers_attack_cardsToKeep(MoveContext context) {
        return controlPlayer.militia_attack_cardsToKeep(context);
    }

    @Override
    public TrustySteedOption[] trustySteed_chooseOptions(MoveContext context) {
        TrustySteedOption[] ret;
        do {
            ret = new TrustySteedOption[]{
                TrustySteedOption.values()[rand.nextInt(TrustySteedOption.values().length)],
                TrustySteedOption.values()[rand.nextInt(TrustySteedOption.values().length)],
            };
        } while( ret[0] == ret[1] );
            
        return ret;
    }

    @Override
    public VictoryCard bureaucrat_cardToReplace(MoveContext context) {
        // Not sure on this logic...
        Card[] cards = getVictoryInHand().toArray(new Card[] {});
        if (cards.length == 0) return null;

        int actions = 0;
        for (Card card : cards) {
            if (card instanceof ActionCard) {
                actions++;
            }
        }

        if(actions > 1) {
            for (Card card : cards) {
                if (card instanceof VictoryCard && !isOnlyVictory(card)) {
                    return (VictoryCard) card;
                }
            }
        }
        
        for (Card card : cards) {
            if (card instanceof VictoryCard && isOnlyVictory(card)) {
                return (VictoryCard) card;
            }
        }
        
        for (Card card : cards) {
            if (card instanceof VictoryCard) {
                return (VictoryCard) card;
            }
        }
        
        return null;
    }

    @Override
    public TreasureCard thief_treasureToTrash(MoveContext context, TreasureCard[] treasures) {
        return getBestTreasureCard(context, treasures);
    }

    @Override
    public TreasureCard[] thief_treasuresToGain(MoveContext context, TreasureCard[] treasures) {
        ArrayList<TreasureCard> cards = new ArrayList<TreasureCard>();
        for(TreasureCard c : treasures) {
            if(!isTrashCard(c)) {
                cards.add(c);
            }
        }
        
        return cards.toArray(new TreasureCard[0]);
    }

    @Override
    public TreasureCard pirateShip_treasureToTrash(MoveContext context, TreasureCard[] treasures) {
        return getBestTreasureCard(context, treasures);
    }
        
    public TreasureCard getBestTreasureCard(MoveContext context, TreasureCard[] treasures) {
        if(treasures == null) {
            return null;
        }
        if(treasures.length == 1) {
            return treasures[0];
        }
        int index = 0;
        int cost = treasures[0].getCost(context);
        for(int i=1; i < treasures.length; i++) {
            if(treasures[i].getCost(context) > cost) {
                index = i;
                cost = treasures[i].getCost(context);
            }
        }
        return treasures[index];
    }
    
    public VictoryCard getBestVictoryCard(MoveContext context) {
    	ArrayList<VictoryCard> cards = new ArrayList<VictoryCard>();
    	
    	for (Card c : context.getVictoryCardsInGame()) {
    		cards.add((VictoryCard) c);
    	}
    	
    	return getBestVictoryCard(context, cards.toArray(new VictoryCard[0]));
    }
    
    public VictoryCard getBestVictoryCard(MoveContext context, VictoryCard[] cards) {
        if(cards == null) {
            return null;
        }
        if(cards.length == 1) {
            return cards[0];
        }
        int index = 0;
        int vp = cards[0].getVictoryPoints();
        for(int i=1; i < cards.length; i++) {
            if(cards[i].getVictoryPoints() > vp) {
                index = i;
                vp = cards[i].getVictoryPoints();
            }
        }
        return cards[index];
    }
    public boolean isOnlyTreasure(Card card) {
        if(!(card instanceof TreasureCard)) {
            return false;
        }
        
        if(card instanceof ActionCard || card instanceof VictoryCard) {
            return false;
        }
        
        return true;
    }
    
    public boolean isTrashCard(Card card) {
        for(Card trash : getTrashCards()) {
            if(trash.equals(card)) {
                return true;
            }
        }
        
        return false;
    }
    
    public boolean isAttackCard(Card card) {
        if(card instanceof ActionCard) {
            ActionCard aCard = (ActionCard) card;
            return aCard.isAttack();
        }
        
        return false;
    }
    
    public boolean isOnlyVictory(Card card) {
        if(!(card instanceof VictoryCard)) {
            return false;
        }
        
        if(card instanceof ActionCard || card instanceof TreasureCard) {
            return false;
        }
        
        return true;
    }
    
    public boolean isCurse(Card card) {
        return card instanceof CurseCard;
    }
    
    public boolean shouldDiscard(Card card) {
        return isCurse(card) || isOnlyVictory(card) || card.isShelter() || card.isRuins();
    }
    
    public boolean shouldBuyPotion() {
        if(potionCount > 2) {
            return false;
        }
        else if(potionCount > 1 && rand.nextInt(5) > 0) {
            return false;
        }
        else if(potionCount > 0 && rand.nextInt(3) > 0) {
            return false;
        }
        return true;
    }

    @Override
    public boolean duchess_shouldDiscardCardFromTopOfDeck(MoveContext context, Card card) {
        if(isTrashCard(card)) {
            return true;
        }
        if(isOnlyVictory(card)) {
            return true;
        }
        return false;
    }

    @Override
    public Card oasis_cardToDiscard(MoveContext context) {
        return lowestCard(context, context.getPlayer().getHand(), true);
    }

    @Override
    public Card develop_cardToTrash(MoveContext context) {
        return lowestCard(context, context.getPlayer().getHand(), false);
    }

    @Override
    public Card develop_lowCardToGain(MoveContext context, int cost, boolean potion) {
        return bestCardInPlay(context, cost, true, potion, true);
    }
    
    @Override
    public Card develop_highCardToGain(MoveContext context, int cost, boolean potion) {
        return bestCardInPlay(context, cost, true, potion, true);
    }
    
    @Override
    public Card[] develop_orderCards(MoveContext context, Card[] cards) {
        return cards;
    }
    
    @Override
    public boolean foolsGold_shouldTrash(MoveContext context) {
        return (game.pileSize(Cards.gold) > 0);
    }

    @Override
    public boolean duchess_shouldGainBecauseOfDuchy(MoveContext context) {
        return true;
    }
    
    @Override
    public TreasureCard nobleBrigand_silverOrGoldToTrash(MoveContext context, TreasureCard[] silverOrGoldCards) {
        if(silverOrGoldCards[0].getCost(context) >= silverOrGoldCards[1].getCost(context)) {
            return silverOrGoldCards[0];
        }
        
        return silverOrGoldCards[1];
    }

    @Override
    public boolean jackOfAllTrades_shouldDiscardCardFromTopOfDeck(MoveContext context, Card card) {
        if(isOnlyVictory(card)) {
            return true;
        }
        if(isTrashCard(card) && (jackOfAllTrades_nonTreasureToTrash(context) != null || (card instanceof TreasureCard))) {
            return true;
        }
        return false;
    }
    
    @Override
    public Card jackOfAllTrades_nonTreasureToTrash(MoveContext context) {
        for (Card card : context.getPlayer().getHand()) {
            if(isTrashCard(card) && !(card instanceof TreasureCard)) {
                return card;
            }
        }
        
        return null;
    }

    @Override
    public TreasureCard spiceMerchant_treasureToTrash(MoveContext context) {
        for (Card card : context.getPlayer().getHand()) {
            for(Card trash : getTrashCards()) {
                if(trash.equals(card) && (card instanceof TreasureCard)) {
                    return (TreasureCard) card;
                }
            }
        }

        return null;
    }

    @Override
    public SpiceMerchantOption spiceMerchant_chooseOption(MoveContext context) {
        //TODO: better logic
        if(rand.nextBoolean()) 
            return SpiceMerchantOption.AddGoldAndBuy;
        else
            return SpiceMerchantOption.AddCardsAndAction;
    }

    @Override
    public Card[] embassy_cardsToDiscard(MoveContext context) {
        return controlPlayer.warehouse_cardsToDiscard(context);
    }

    @Override
    public Card[] cartographer_cardsFromTopOfDeckToDiscard(MoveContext context, Card[] cards) {
        ArrayList<Card> cardsToDiscard = new ArrayList<Card>();
        for(Card card : cards) {
            if(isTrashCard(card) || isOnlyVictory(card)) {
                cardsToDiscard.add(card);
            }
        }
        
        return cardsToDiscard.toArray(new Card[0]);
    }

    @Override
    public Card[] cartographer_cardOrder(MoveContext context, Card[] cards) {
        return cards;
    }
    
    @Override
    public boolean tunnel_shouldReveal(MoveContext context) {
        return true;
    }
    
    @Override
    public ActionCard scheme_actionToPutOnTopOfDeck(MoveContext context, ActionCard[] actions) {
        /* don't put prince cards back on top */
        if(actions == null || actions.length == 0) {
            return null;
        }
        
        int i = -1;
        int cost = -1;
        for(int index = 0; index < actions.length; index++) {
            if(actions[index].getCost(context) >= cost && !context.getPlayer().getPlayedByPrince().contains(actions[index])) {
                cost = actions[index].getCost(context);
                i = index;
            }
        }
        
        if (i > 0) {
            return actions[i];
        }
        else {
            return null;
        }
    }
    
    @Override
    public boolean trader_shouldGainSilverInstead(MoveContext context, Card card) {
        if(isTrashCard(card)) {
            return true;
        }
        
        return false;
    }

    @Override
    public Card trader_cardToTrash(MoveContext context) {
        if (context.getPlayer().getHand().size() == 0) {
            return null;
        }

        Card card = pickOutCard(context.getPlayer().getHand(), getTrashCards());
        if (card != null) 
            return card;
        
        return context.getPlayer().getHand().get(0);
    }

    @Override
    public boolean oracle_shouldDiscard(MoveContext context, Player player, ArrayList<Card> cards) {
        boolean discard = true;
        boolean copper = false;
        boolean costThree = false;
        for(Card c : cards) {
            if (c.equals(Cards.copper)) {
                copper = true;
            }

            if(isTrashCard(c) || isOnlyVictory(c)) {
                discard = false;
            }
            else if(   c.getCost(null) >= 5
                    || c.costPotion()
                    || c.isPrize()
                    || c.equals(Cards.madman)
                    || c.equals(Cards.mercenary)
                    || c.equals(Cards.spoils)) {
                discard = true;
                break;
            }
            else if( c.getCost(null) >= 3 ) {
                costThree = true;
            }
        }
        if (copper && costThree) {
            discard = true;
        }
        
        if(player == this) {
            discard = !discard;
        }
        return discard;
    }

    @Override
    public Card[] oracle_orderCards(MoveContext context, Card[] cards) {
        return cards;
    }
    
    @Override
    public boolean illGottenGains_gainCopper(MoveContext context) {
        return false;
    }

    @Override
    public Card haggler_cardToObtain(MoveContext context, int maxCost, boolean potion) {
        if (maxCost < 0)
            return null;
        return bestCardInPlay(context, maxCost, false, potion, false, false, potion ? maxCost + 1 : maxCost, true);
    }
    
    @Override
    public Card[] inn_cardsToDiscard(MoveContext context) {
        ArrayList<Card> cardsToDiscard = new ArrayList<Card>();

        for (Card card : context.getPlayer().getHand()) {
            if (shouldDiscard(card)) {
                cardsToDiscard.add(card);
            }

            if (cardsToDiscard.size() == 2) {
                break;
            }
        }

        if (cardsToDiscard.size() < 2) {
            ArrayList<Card> handCopy = new ArrayList<Card>();
            for (Card card : context.getPlayer().getHand()) {
                handCopy.add(card);
            }

            for (Card card : cardsToDiscard) {
                handCopy.remove(card);
            }

            while (cardsToDiscard.size() < 2) {
                cardsToDiscard.add(handCopy.remove(0));
            }
        }

        return cardsToDiscard.toArray(new Card[0]);
    }
    
    @Override
    public boolean inn_shuffleCardBackIntoDeck(MoveContext context, ActionCard card) {
        return true;
    }

    @Override
    public Card borderVillage_cardToObtain(MoveContext context) {
        return bestCardInPlay(context, Cards.borderVillage.getCost(context) - 1, true);
    }

    @Override
    public Card farmland_cardToTrash(MoveContext context) {
        return controlPlayer.remodel_cardToTrash(context);
    }

    @Override
    public Card farmland_cardToObtain(MoveContext context, int exactCost, boolean potion) {
        return bestCardInPlay(context, exactCost, true, potion, true);
    }

    @Override
    public TreasureCard stables_treasureToDiscard(MoveContext context) {
        return plaza_treasureToDiscard(context);
    }
    
    @Override
    public Card mandarin_cardToReplace(MoveContext context) {
        if (context.getActionsLeft() == 0) {
            for (Card card : context.getPlayer().getHand()) {
                if (card instanceof ActionCard) {
                    return card;
                }
            }
        }
        //TODO: better logic
        return Util.randomCard(context.getPlayer().getHand());
    }
    
    @Override
    public Card[] margrave_attack_cardsToKeep(MoveContext context) {
        return controlPlayer.militia_attack_cardsToKeep(context);
    }    
    
    @Override
    public Card rats_cardToTrash(MoveContext context) 
    {
        ArrayList<Card> nonRatsChoices = new ArrayList<Card>();

        // Look for the low hanging fruit -- cards generally considered trash
        for (Card c : context.getPlayer().getHand()) 
        {
            if (isTrashCard(c)) 
            {
                return c;
            }
            else if (c.getType() != Cards.Type.Rats)
            {
                // Build a list of the cards we can trash
                nonRatsChoices.add(c);
            }
        }
        
        if (nonRatsChoices.size() > 1)
        {
            Collections.sort(nonRatsChoices, new CardCostComparator());
        }
        
        return nonRatsChoices.get(0);
    }
    
    @Override
    public boolean revealBane(MoveContext context) {
        return true;
    }
    
    @Override
    public PutBackOption selectPutBackOption(MoveContext context, List<PutBackOption> options) {
        /* don't put prince cards back on top */
        Collections.sort(options);
        for(int i = 0; i < options.size(); i++) {
            if (!(   options.get(i) == PutBackOption.Treasury && context.getPlayer().getPlayedByPrince().contains(Cards.treasury)
                  || options.get(i) == PutBackOption.WalledVillage && context.getPlayer().getPlayedByPrince().contains(Cards.walledVillage)
                 ) )
            {
                return options.get(i);
            }
        }
        return PutBackOption.None;
    }
    
    @Override
    public SquireOption squire_chooseOption(MoveContext context) {
        return SquireOption.AddActions;
    }
    
    @Override
    public Card altar_cardToTrash(MoveContext context) {
        Card card = pickOutCard(context.getPlayer().getHand(), getTrashCards());
        if (card == null) {
            card = lowestCard(context, context.getPlayer().getHand(), false);
        }

        return card;
    }

    @Override
    public Card altar_cardToObtain(MoveContext context) {
        return bestCardInPlay(context, 5, true);
    }

  @Override
    public boolean beggar_shouldDiscard(MoveContext context, Card responsible) {
        if (responsible.equals(Cards.pirateShip)) {
            return false;
        }
        return true;
    }

    @Override
    public Card armory_cardToObtain(MoveContext context) {
        return bestCardInPlay(context, 4, true);
    }

	@Override
	public Card squire_cardToObtain(MoveContext context) {
		ArrayList<Card> options = new ArrayList<Card>();
		for (AbstractCardPile pile : game.piles.values()) {
			if ((pile.card() instanceof ActionCard) && (pile.getCount() > 0)) {
				ActionCard ac = (ActionCard) pile.card();
				if (ac.isAttack()) {
					options.add(pile.card());
				}
			}
		}
		
		if (options.size() > 0) {
			return Util.randomCard(options);
		} else {
			return null;
		}
	}

	@Override
	public boolean catacombs_shouldDiscardTopCards(MoveContext context, Card[] array) {
		int discards = 0;
		for (Card c : array) 
			if (shouldDiscard(c))
				discards++;
		
		return (discards > 1);
	}

	@Override
	public Card catacombs_cardToObtain(MoveContext context) {
		return bestCardInPlay(context, Math.max(0, game.getPile(Cards.catacombs).card().getCost(context) - 1), true);
	}

	@Override
	public CountFirstOption count_chooseFirstOption(MoveContext context) {
        if (getCurrencyTotal(context) < 6) return Player.CountFirstOption.GainCopper;
        
        return Player.CountFirstOption.PutOnDeck;
	}

	@Override
	public CountSecondOption count_chooseSecondOption(MoveContext context) {
		if (game.pileSize(Cards.colony) > 3 || game.pileSize(Cards.province) > 3)
			return Player.CountSecondOption.Coins;
		else
			return Player.CountSecondOption.GainDuchy;
	}

	@Override
	public Card[] count_cardsToDiscard(MoveContext context) {
        return lowestCards(context, hand, 2, true);
	}

	@Override
	public Card count_cardToPutBackOnDeck(MoveContext context) {
		return Util.randomCard(hand);
	}
	
	@Override
	public Card forager_cardToTrash(MoveContext context) {
		return lowestCard(context, context.getPlayer().getHand(), false);
	}
	@Override
	public Card deathCart_actionToTrash(MoveContext context)
	{
		Card ac = null;
		for (Card c : context.player.hand)
		{
			if (c instanceof ActionCard)
			{
				ac = c;
				break;
			}
		}
		return ac;
	}

	@Override
	public GraverobberOption graverobber_chooseOption(MoveContext context) {
		
		boolean trashContainsValidCard = false;
		
		for (Card c : context.game.trashPile) {
			if (!c.costPotion() && c.getCost(context) >= 3 && c.getCost(context) <= 6) {
				trashContainsValidCard = true;
				break;
			}
		}
		
		if (trashContainsValidCard) {
			return GraverobberOption.GainFromTrash;
		} else {
			return GraverobberOption.TrashActionCard;
		}
	}

	@Override
	public Card graverobber_cardToGainFromTrash(MoveContext context) {
		ArrayList<Card> options = new ArrayList<Card>();
		for (Card c : game.trashPile) {
			if (!c.costPotion() && c.getCost(context) >= 3 && c.getCost(context) <= 6) {
				options.add(c);
			}
		}
		return Util.randomCard(options);
	}

	@Override
	public Card graverobber_cardToTrash(MoveContext context) {
		CardList ac = new CardList(controlPlayer, getPlayerName(false));
		for (Card c : hand) {
			if (c instanceof ActionCard) {
				ac.add(c);
			}
		}
		
		if (ac.size() > 0) {
	    	Card card = pickOutCard(ac, getTrashCards());
	    	if (card == null) {
	    		card = lowestCard(context, ac, false);
	    	}
	    	return card;
		} 
		return null;
	}

	@Override
	public Card graverobber_cardToReplace(MoveContext context, int maxCost, boolean potion) {
		return bestCardInPlay(context, maxCost, false, potion, true);
	}

	@Override
	public HuntingGroundsOption huntingGrounds_chooseOption(MoveContext context) {
		return HuntingGroundsOption.GainDuchy;
	}

	@Override
	public boolean ironmonger_shouldDiscard(MoveContext context, Card card) {
		if (card.equals(Cards.copper) || card.equals(Cards.masterpiece)) {
			return true;
		}
		return this.shouldDiscard(card);
	}

	@Override
	public Card junkDealer_cardToTrash(MoveContext context) {
        if (context.getPlayer().getHand().size() == 0) {
            return null;
        }

        Card card = pickOutCard(context.getPlayer().getHand(), getTrashCards());
    	if (card != null) 
    		return card;
        
        return context.getPlayer().getHand().get(0);
	}

	@Override
	public boolean marketSquare_shouldDiscard(MoveContext context) {
		return true;
	}

	@Override
	public Card mystic_cardGuess(MoveContext context, ArrayList<Card> cardList) {
	    return Cards.silver;
	}

	@Override
	public boolean scavenger_shouldDiscardDeck(MoveContext context) {
		return true;
	}

	@Override
	public Card scavenger_cardToPutBackOnDeck(MoveContext context) {
		return Util.randomCard(discard);
	}

	@Override
	public Card[] storeroom_cardsToDiscardForCards(MoveContext context) {
        ArrayList<Card> cards = new ArrayList<Card>();
        for (Card card : context.getPlayer().getHand()) {
            if ((!(card instanceof ActionCard) && !(card instanceof TreasureCard)) || card.equals(Cards.cellar) || card.equals(Cards.storeroom) || card.equals(Cards.copper)) {
                cards.add(card);
            }
        }

        if (context.getActionsLeft() == 0) {
            for (Card c : context.getPlayer().getHand()) {
                if ((c instanceof ActionCard)) {
                    cards.add(c);
                }
            }
        }
        
        return cards.toArray(new Card[0]);
	}

	@Override
	public Card[] storeroom_cardsToDiscardForCoins(MoveContext context) {
        ArrayList<Card> cards = new ArrayList<Card>();
        for (Card card : context.getPlayer().getHand()) {
            if ((!(card instanceof ActionCard) && !(card instanceof TreasureCard)) || card.equals(Cards.cellar) || card.equals(Cards.storeroom) || card.equals(Cards.copper)) {
                cards.add(card);
            }
        }

        if (context.getActionsLeft() == 0) {
            for (Card c : context.getPlayer().getHand()) {
                if ((c instanceof ActionCard)) {
                    cards.add(c);
                }
            }
        }
        
        return cards.toArray(new Card[0]);
	}

	@Override
	public ActionCard procession_cardToPlay(MoveContext context) {
    return controlPlayer.kingsCourt_cardToPlay(context);
	}

	@Override
	public Card procession_cardToGain(MoveContext context, int maxCost, boolean potion) {
    return bestCardInPlay(context, maxCost, false, potion, true);
	}

	@Override
	public Card rebuild_cardToPick(MoveContext context, ArrayList<Card> cardList) {
		if (context.game.isColonyInGame())
			return Cards.colony;
		else
			return Cards.province;
	}

	@Override
	public Card rebuild_cardToGain(MoveContext context, int maxCost, boolean costPotion) {
		ArrayList<VictoryCard> cards = new ArrayList<VictoryCard>();
		for (Card c: context.getVictoryCardsInGame()) {
			if (c.getCost(context) <= maxCost && !game.isPileEmpty(c) && Cards.isSupplyCard(c)) {
				cards.add((VictoryCard) c);
			}
		}
		return (cards.size() == 0) ? null : this.getBestVictoryCard(context, cards.toArray(new VictoryCard[0]));
	}

	@Override
	public Card rogue_cardToGain(MoveContext context) {
		ArrayList<Card> options = new ArrayList<Card>();
		for (Card c : game.trashPile) {
			if (!c.costPotion() && c.getCost(context) >= 3 && c.getCost(context) <= 6) {
				options.add(c);
			}
		}
		Card ret = Util.randomCard(options);
		if (ret.equals(Cards.masterpiece)) {
			if (options.contains(Cards.silver))
				ret = Cards.silver;
			if (options.contains(Cards.vault))
				ret = Cards.vault;
			if (options.contains(Cards.gold))
				ret = Cards.gold;
		}
		if (ret.equals(Cards.silver) || ret.equals(Cards.loan)) {
			if (options.contains(Cards.vault))
				ret = Cards.vault;
			if (options.contains(Cards.gold))
				ret = Cards.gold;
		}
		if (ret.equals(Cards.contraband) || ret.equals(Cards.cache)) {
			if (options.contains(Cards.gold))
				ret = Cards.gold;
		}
		return ret;
	}

	@Override
	public Card rogue_cardToTrash(MoveContext context, ArrayList<Card> canTrash) {
		return this.lowestCards(context, canTrash, 1, false)[0];
	}

	@Override
	public TreasureCard counterfeit_cardToPlay(MoveContext context) {
		//it is important to return a card from hand. Don't use return (TreasureCard) Cards.spoils;
		if (context.getPlayer().getHand().contains(Cards.spoils)) {
			return (TreasureCard) context.getPlayer().getHand().get(Cards.spoils);
		}
		if (context.getPlayer().getHand().contains(Cards.copper)) {
			return (TreasureCard) context.getPlayer().getHand().get(Cards.copper);
		}
		return null;
	}
	
	@Override
	public Card pillage_opponentCardToDiscard(MoveContext context, ArrayList<Card> handCards)
	{
		Card cardToDiscard = null;

		ArrayList<Card> goodCards = new ArrayList<Card>();
    	for (Card c : handCards)
    		if (c instanceof TreasureCard || c instanceof ActionCard)
    			goodCards.add(c);

    	if (goodCards.size() > 0) {
        	cardToDiscard = Util.getMostExpensiveCard(goodCards.toArray(new Card[0]));
		}

    	if (cardToDiscard == null) {
			cardToDiscard = Util.randomCard(handCards);
		}
		
		return cardToDiscard;
	}
	
	@Override
	public boolean hovel_shouldTrash(MoveContext context)
	{
		return true;
	}
	
	@Override
	public GovernorOption governor_chooseOption(MoveContext context) {
		return GovernorOption.AddCards;
	}
	
	@Override
    public Card governor_cardToTrash(MoveContext context) {
        return controlPlayer.upgrade_cardToTrash(context, Cards.governor, true);
    }

    @Override
    public Card governor_cardToObtain(MoveContext context, int exactCost, boolean potion) {
        return controlPlayer.upgrade_cardToObtain(context, Cards.governor, exactCost, potion);
    }
    
    @Override
    public Card envoy_cardToDiscard(MoveContext context, Card[] cards) {
    	// build list of "good" cards
		  boolean containsCopper = false;
    	CardList cl = new CardList(context.getPlayer(), context.getPlayer().getPlayerName());
    	for(Card card : cards) {
    		if (!isOnlyVictory(card) && !isCurse(card) && !isTrashCard(card) && !(context.actions == 0 && (card instanceof ActionCard))) {
    			cl.add(card);
			}
    		if (card.equals(Cards.copper)) {
    			containsCopper = true;
    		}
    	}

    	// not enough to choose from
    	if (cl.size() == 0) {
    		if (containsCopper) {
    			return Cards.copper;
    		}
    		return cards[0];
    	} else if (cl.size() == 1) {
    		return cl.get(0);
    	}
		cards = cl.sort(new Util.CardCostComparatorDesc());

    	// pick out mean cards and big treasure
    	if (cl.contains(Cards.saboteur)) {
			return cl.get(Cards.saboteur);
		} else if (cl.contains(Cards.platinum)) {
			return cl.get(Cards.platinum);
		} else if (cl.contains(Cards.prince)) {
			return cl.get(Cards.prince);
		} else if (cl.contains(Cards.possession)) {
			return cl.get(Cards.possession);
		} else if (game.pileSize(Cards.curse) > 0 && !this.hand.contains(Cards.moat) && !this.hand.contains(Cards.watchTower)
				&& (cl.contains(Cards.witch) || cl.contains(Cards.seaHag) || cl.contains(Cards.torturer))) {
			if (cl.contains(Cards.witch)) return cl.get(Cards.witch);
			if (cl.contains(Cards.seaHag)) return cl.get(Cards.seaHag);
			if (cl.contains(Cards.torturer)) return cl.get(Cards.torturer);
		}

    	for (Card card : cl) {
			if (card.isPrize()) {
				return card;
			}
		}
    	
    	if (!this.hand.contains(Cards.moat)) {
        	for (Card card : cl) {
    			if (isAttackCard(card)) {
					return card;
				}
    		}
    	}
    	
		if (cl.contains(Cards.gold)) {
			return cl.get(Cards.gold);
		}
		
		Card[] left = cl.sort(new Util.CardCostComparatorDesc());
        return left[0];
    }

	@Override
	public boolean survivors_shouldDiscardTopCards(MoveContext context, Card[] array) {
        ArrayList<Card> cards = new ArrayList<Card>();
        for(int i=0; i < array.length; i++) {
            if(array[i] != null) {
                cards.add(array[i]);
            }
        }
	    return oracle_shouldDiscard(context, this, cards);
/*	    
        // Discard them if there is more than 1 victory card
        int victoryCount = 0;
        for (Card card : array) {
            if (shouldDiscard(card)) {
                victoryCount++;
            }
        }
        return (victoryCount > 1);
*/        
	}

	@Override
	public Card[] survivors_cardOrder(MoveContext context, Card[] array) {
		return array;
	}

	@Override
	public boolean cultist_shouldPlayNext(MoveContext context) {
		return true;
	}
	
	@Override
    public Card[] urchin_attack_cardsToKeep(MoveContext context) 
	{
		// Using the Militia discard logic for now...
		
        ArrayList<Card> keepers = new ArrayList<Card>();
        ArrayList<Card> discards = new ArrayList<Card>();
        
        // Just add in the non-victory cards...
        for (Card card : context.attackedPlayer.getHand()) {
            if (!shouldDiscard(card)) {
                keepers.add(card);
            } else {
            	discards.add(card);
            }
        }

        while (keepers.size() < 4) {
        	keepers.add(discards.remove(0));
        }

        // Still more than 4? Remove all but one action...
        while (keepers.size() > 4) {
            int bestAction = -1;
            boolean removed = false;
            for (int i = 0; i < keepers.size(); i++) {
                if (keepers.get(i) instanceof ActionCard) {
                    if (bestAction == -1) {
                        bestAction = i;
                    } else {
                        if(keepers.get(i).getCost(context) > keepers.get(bestAction).getCost(context)) {
                            keepers.remove(bestAction);
                            bestAction = i;
                        }
                        else {
                            keepers.remove(i);
                        }
                        removed = true;
                        break;
                    }
                }
            }
            if (!removed) {
                break;
            }
        }

        // Still more than 4? Start removing copper...
        while (keepers.size() > 4) {
            boolean removed = false;
            for (int i = 0; i < keepers.size(); i++) {
                if (keepers.get(i).equals(Cards.copper)) {
                    keepers.remove(i);
                    removed = true;
                    break;
                }
            }
            if (!removed) {
                break;
            }
        }

        // Still more than 4? Start removing silver...
        while (keepers.size() > 4) {
            boolean removed = false;
            for (int i = 0; i < keepers.size(); i++) {
                if (keepers.get(i).equals(Cards.silver)) {
                    keepers.remove(i);
                    removed = true;
                    break;
                }
            }
            if (!removed) {
                break;
            }
        }

        while (keepers.size() > 4) {
            keepers.remove(0);
        }

        return keepers.toArray(new Card[0]);
    }
	
	@Override
	public boolean urchin_shouldTrashForMercenary(MoveContext context)
	{
    	/* don't trash prince cards */    	
    	if (context.getPlayer().getPlayedByPrince().contains(Cards.urchin)) {
    		return false;
    	}
		return true;
	}
	
	@Override
	public Card[] mercenary_cardsToTrash(MoveContext context) {
		return pickOutCards(context.getPlayer().getHand(), 2, getTrashCards());
	}
	
    @Override
    public Card[] mercenary_attack_cardsToKeep(MoveContext context) {
        return controlPlayer.militia_attack_cardsToKeep(context);
    }

	@Override
	public Card hermit_cardToTrash(MoveContext context, ArrayList<Card> cardList, int nonTreasureCountInDiscard) 
	{	
		CardList cl = new CardList(controlPlayer, getPlayerName(false));
		for (Card c : cardList) {
			if (c instanceof ActionCard) {
				cl.add(c);
			}
		}
    return pickOutCard(cl, getTrashCards());
	}
	
	@Override
	public Card hermit_cardToGain(MoveContext context)
	{
		return bestCardInPlay(context, 3, false, false, true);
	}
	
	@Override
	public boolean madman_shouldReturnToPile(MoveContext context)
	{
		return true;
	}
	
	@Override
	public Card[] dameAnna_cardsToTrash(MoveContext context) {
		return pickOutCards(context.getPlayer().getHand(), 2, getTrashCards());
	}

	@Override
	public Card knight_cardToTrash(MoveContext context, ArrayList<Card> canTrash) {
		return this.lowestCards(context, canTrash, 1, false)[0];
	}

	@Override
	public Card[] sirMichael_attack_cardsToKeep(MoveContext context) {
		return controlPlayer.militia_attack_cardsToKeep(context);	
	}

	@Override
	public Card dameNatalie_cardToObtain(MoveContext context) {
        return bestCardInPlay(context, 3, false);
	}

    @Override
    public ActionCard bandOfMisfits_actionCardToImpersonate(MoveContext context) {
    	if (context.getPlayer().getHand().contains(Cards.treasureMap)) {
    		return (ActionCard) Cards.treasureMap;
    	}
        return (ActionCardImpl) bestCardInPlay(context, 4, false, false, true, true, true);
    }

    @Override
    public TreasureCard taxman_treasureToTrash(MoveContext context) {
        ArrayList<TreasureCard> handCards = context.getPlayer().getTreasuresInHand();
        Collections.sort(handCards, new CardValueComparator());
        while (handCards.remove(Cards.spoils));

        HashSet<Integer> treasureCardValues = new HashSet<Integer>();
        for (Card card : context.getTreasureCardsInGame()) {
            if (Cards.isSupplyCard(card) && context.getCardsLeftInPile(card) > 0)
                treasureCardValues.add(card.getCost(context));
        }

        ArrayList<TreasureCard> ret = new ArrayList<TreasureCard>();
        for (int i = 0; i < handCards.size(); i++) {
            TreasureCard card = handCards.get(i);
            if (treasureCardValues.contains(card.getCost(context) + 3))
                return card;
            if (treasureCardValues.contains(card.getCost(context) + 2) || treasureCardValues.contains(card.getCost(context) + 1))
            	ret.add(card);
        }

        if (ret.size() > 0)
            return ret.get(0);

        return null;
    }
    
    @Override
    public TreasureCard taxman_treasureToObtain(MoveContext context, int cost, boolean potion) {
        TreasureCard newCard = null;
        int newCost = -1;
        for (Card card : context.getTreasureCardsInGame()) {
            if (   Cards.isSupplyCard(card) && context.getCardsLeftInPile(card) > 0 && card.getCost(context) <= cost && card.getCost(context) >= newCost
                && (potion || !card.costPotion())) {
                    newCard = (TreasureCard) card;
                    newCost = card.getCost(context);
            }
        }

        return newCard;
    }
    
    @Override
    public TreasureCard plaza_treasureToDiscard(MoveContext context) {
        for (Card card : context.getPlayer().getHand()) {
            for(Card trash : getTrashCards()) {
                if(trash.equals(card) && (card instanceof TreasureCard)) {
                    return (TreasureCard) card;
                }
            }
        }
        
        if (context.getPlayer().getHand().contains(Cards.loan))
        {
            return (TreasureCard) context.getPlayer().fromHand(Cards.loan);        	
        }
        if (context.getPlayer().getHand().contains(Cards.illGottenGains))
        {
            return (TreasureCard) context.getPlayer().fromHand(Cards.illGottenGains);        	
        }
        
        if (context.getPlayer().isAi() && Game.rand.nextBoolean() && context.getPlayer().getHand().contains(Cards.silver)) {
            return (TreasureCard) context.getPlayer().fromHand(Cards.silver);
        }

        return null;
    }
    
    @Override
    public int numGuildsCoinTokensToSpend(MoveContext context, int coinTokenTotal, boolean butcher)
    {
    	if (butcher)
            return 0;
    	
    	if (context.player.isPossessed() && controlPlayer.isAi())
    		return coinTokenTotal;
    	
    	int gold = context.getCoinAvailableForBuy();
    	int coinTokenToSpend = 0;
    	
        if(game.isValidBuy(context, Cards.colony, gold + coinTokenTotal)) {
        	coinTokenToSpend = Math.max(0, Cards.colony.getCost(context) - gold);
            if( !(   game.isValidBuy(context, Cards.platinum, gold + coinTokenTotal)
              	  && turnCount < midGame
              	  && coinTokenToSpend == 2) ) {
	            if (coinTokenToSpend <= coinTokenTotal)
	            	return coinTokenToSpend;
            }
        }
        
        if(game.isValidBuy(context, Cards.platinum, gold + coinTokenTotal) && turnCount < midGame) {
        	/* strategy doesn't match to every AI. Sometimes they buy a province with 9 gold, eg. Patrick */  
        	coinTokenToSpend = Math.max(0, Cards.platinum.getCost(context) - gold);
            if (coinTokenToSpend <= coinTokenTotal)
            	return coinTokenToSpend;
        }
        
        if(game.isValidBuy(context, Cards.province, gold + coinTokenTotal)) {
        	coinTokenToSpend = Math.max(0, Cards.province.getCost(context) - gold);
            if (   coinTokenToSpend <= coinTokenTotal
            	&& (coinTokenToSpend == 1 || turnCount >= midGame))
            	return coinTokenToSpend;
        }

        return 0;        
    }
    
    @Override
    public Card butcher_cardToTrash(MoveContext context) {
        if (context.getPlayer().getHand().size() == 0) {
            return null;
        }
        
        if (context.getPlayer().getHand().size() == 1) {
            return context.getPlayer().getHand().get(0);
        }

        for (Card c : context.getPlayer().getHand()) {
            if(c.equals(Cards.curse)) {
                return c;
            }
        }
        
        for (Card c : context.getPlayer().getHand()) {
            if(   isTrashCard(c)
               /*trashing copper doesn't help because you have to take a new one*/
               && (!c.equals(Cards.copper) || context.getPlayer().getHand().contains(Cards.marketSquare))) {
                return c;
            }
        }
        
        /*dead code???*/
        for (Card c : context.getPlayer().getHand()) {
            if(c instanceof VictoryCard) {
                continue;
            }
        }
        
        return null;
    }
    
    @Override
    public Card butcher_cardToObtain(MoveContext context, int maxCost, boolean potion)
    {
        return bestCardInPlay(context, maxCost, false, potion, true);
    }
    
    @Override
    public Card advisor_cardToDiscard(MoveContext context, Card[] cards) 
    {
        return envoy_cardToDiscard(context, cards);
    }
    
    @Override
    public Card journeyman_cardToPick(MoveContext context, List<Card> cardList) 
    {
        // TODO: Implement
        return Cards.estate;
    }
    
    @Override
    public int amountToOverpay(MoveContext context, Card card, int cardCost)
    {
        int availableAmount = context.getCoinAvailableForBuy() - cardCost;
        if (availableAmount <= 0) {
            return 0;
        }
        if (   card.equals(Cards.masterpiece)
            || card.equals(Cards.doctor) ) {
        	return availableAmount;
        }
        // TODO: Implement stonemason, herald
        return 0;
    }
    
    @Override
    public int overpayByPotions(MoveContext context, int availablePotions)
    {
        // TODO: Implement stonemason
        return 0;
    }
    
    @Override
    public Card stonemason_cardToTrash(MoveContext context)
    {
        if (context.getPlayer().getHand().size() == 0) {
            return null;
        }
        
        if (context.getPlayer().getHand().size() == 1) {
            return context.getPlayer().getHand().get(0);
        }

        //trash platinum to get 2 provinces
        for (Card c : context.getPlayer().getHand()) {
        	if(c.getCost(null) > 8 && !c.isVictory(context)) {
        		return c;
        	}
        }
        
        ArrayList<Card> trashCards = new ArrayList<Card>();
        for(Card c : getTrashCards()) {
        	if (c.getCost(null) == 0) {
        		trashCards.add(c);
        	}
        }
        Card card = pickOutCard(context.getPlayer().getHand(), trashCards.toArray(new Card[0]));
        
        if (card == null) {
        	card = lowestCard(context, context.getPlayer().getHand(), false);
        }
        
        return card;
    }
    
    @Override
    public Card stonemason_cardToGain(MoveContext context, int maxCost, boolean potion)
    {
        return bestCardInPlay(context, maxCost, false, potion, true);
    }
    
    @Override
    public Card stonemason_cardToGainOverpay(MoveContext context, int overpayAmount, boolean potion)
    {
        return bestCardInPlay(context, overpayAmount, true, potion, true);
    }
    
    @Override
    public Card doctor_cardToPick(MoveContext context, List<Card> cardList) 
    {
    	if (cardList != null) {
    		if (cardList.contains(Cards.curse)) {
    			return Cards.curse;    		
    		}
	    	if (cardList.contains(Cards.estate)) {
	            return Cards.estate;    		
	    	}
    	}
        return Cards.copper;
    }
    
    @Override
    public ArrayList<Card> doctor_cardsForDeck(MoveContext context, ArrayList<Card> cards)
    {
        return cards;
    }
    
    @Override
    public DoctorOverpayOption doctor_chooseOption(MoveContext context, Card card) 
    {
        DoctorOverpayOption doo = DoctorOverpayOption.PutItBack;
        
        if (shouldDiscard(card)) {
            doo = DoctorOverpayOption.DiscardIt;        
        }
            
        for (Card trashCard : getTrashCards())
        {
            if (trashCard.equals(card))
            {
                doo = DoctorOverpayOption.TrashIt;
            }
        }

        if (card.equals(Cards.fortress) && context.actions > 0) {
            doo = DoctorOverpayOption.TrashIt;
        }
        
        return doo;
    }
    
    public Card herald_cardTopDeck(MoveContext context, Card[] cardList)
    {
        Card cardToReturn = null;
        
        if (cardList.length > 0)
        {
            cardToReturn = cardList[0];
        }
        
        for (Card c : cardList)
        {
            if (!shouldDiscard(c) && !c.equals(Cards.copper))
            {
                cardToReturn = c;
                break;
            }
        }
        
        return cardToReturn;
    }
}
