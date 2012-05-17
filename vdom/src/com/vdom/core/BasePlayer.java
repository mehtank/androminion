package com.vdom.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.vdom.api.ActionCard;
import com.vdom.api.Card;
import com.vdom.api.CardValueComparator;
import com.vdom.api.CurseCard;
import com.vdom.api.GameEvent;
import com.vdom.api.GameEventListener;
import com.vdom.api.TreasureCard;
import com.vdom.api.VictoryCard;

public abstract class BasePlayer extends Player implements GameEventListener {
    protected static final Card[] EARLY_TRASH_CARDS = new Card[] { Cards.curse, Cards.estate };
    protected static final Card[] LATE_TRASH_CARDS = new Card[] { Cards.curse, Cards.copper, Cards.estate };
    
    protected Random rand = new Random(System.currentTimeMillis());
    protected static final int COST_MAX = 11;
    protected int actionCardCount = 0;
    protected int throneRoomAndKingsCourtCount = 0;
    protected int potionCount = 0;
    protected int midGame;
    
    protected boolean reactedMoat = false;
    protected boolean reactedSecretChamber = false;
    //private boolean Card;

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
        if (event.getType() == GameEvent.Type.TurnBegin && event.getPlayer() == this) {
            if (game.consecutiveTurnCounter == 1)
                turnCount++;
        }
        
        if (event.getType() == GameEvent.Type.PlayingAction) {
            reactedMoat = false;
            reactedSecretChamber = false;
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
    
    // ////////////////////////
    // Helper Methods
    // ////////////////////////

    protected Card bestCardInPlay(MoveContext context, int maxCost) {
        return bestCardInPlay(context, maxCost, false, false);
    }
    
    protected Card bestCardInPlay(MoveContext context, int maxCost, boolean exactCost) {
        return bestCardInPlay(context, maxCost, exactCost, false);
    }

    protected Card bestCardInPlay(MoveContext context, int maxCost, boolean exactCost, boolean potion) {
        return bestCardInPlay(context, maxCost, exactCost, potion, false, true);
    }
    
    protected Card bestCardInPlay(MoveContext context, int maxCost, boolean exactCost, boolean potion, boolean actionOnly, boolean victoryCardAllowed) {
        return bestCardInPlay(context, maxCost, exactCost, potion, actionOnly, victoryCardAllowed, maxCost);
    }

    protected Card bestCardInPlay(MoveContext context, int maxCost, boolean exactCost, boolean potion, boolean actionOnly, boolean victoryCardAllowed, int maxCostWithoutPotion) {
        boolean isBuy = (maxCost == -1);
        if (isBuy) {
            maxCost = maxCostWithoutPotion = COST_MAX;
        }
        Card[] cards = context.getCardsInPlay();
        int cost = maxCostWithoutPotion;
        int highestCost = 0;
        ArrayList<Card> randList = new ArrayList<Card>();
        
        while (cost >= 0) {
            for (Card card : cards) {
                int cardCost = card.getCost(context);
                if (cardCost == cost && context.getCardsLeft(card) > 0) {
                    if (card.equals(Cards.curse) || card.isPrize() || isTrashCard(card) || (card.equals(Cards.potion) && !shouldBuyPotion())) {
                        continue;
                    }

                    if ((actionOnly && !(card instanceof ActionCard)) || (!victoryCardAllowed && (card instanceof VictoryCard))) {
                        continue;
                    }

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
            return randList.get(this.rand.nextInt(randList.size()));
        }

        return null;
    }
    
    public Card lowestCard(MoveContext context, CardList cards, boolean anyVictory) {
        Card[] ret = lowestCards(context, cards, 1, anyVictory);
        if(ret == null) {
            return null;
        }
        
        return ret[0];
    }
    
    public Card[] lowestCards(MoveContext context, CardList cards, int num, boolean anyVictory) {
        if (cards == null) {
            return null;
        }
        
        if (cards.size() == 0) {
            return null;
        }
        
        if (cards.size() <= num) {
            return cards.toArray();
        }
        
        ArrayList<Card> cardArray = new ArrayList<Card>();
        for(Card c : cards) {
            cardArray.add(c);
        }

        ArrayList<Card> ret = new ArrayList<Card>();

        for (int i = cardArray.size() - 1; i >= 0; i--) {
            Card card = cardArray.get(i);
            if(isTrashCard(card)) {
                if(card instanceof TreasureCard && anyVictory) {
                    // Add trash treasure cards (copper) later after checking for victory cards below
                    continue;
                }
                
                ret.add(card);
                cardArray.remove(i);

                if(ret.size() == num) {
                    return ret.toArray(new Card[ret.size()]);
                }
            }
        }
        
        if(anyVictory) {
            // Add in the estate cards...
            for (int i = cardArray.size() - 1; i >= 0; i--) {
                Card card = cardArray.get(i);
                if (isOnlyVictory(card)) {
                    ret.add(card);
                    cardArray.remove(i);

                    if(ret.size() == num) {
                        return ret.toArray(new Card[ret.size()]);
                    }
                }
            }

            for (int i = cardArray.size() - 1; i >= 0; i--) {
                Card card = cardArray.get(i);
                if (isTrashCard(card)) {
                    if(card instanceof TreasureCard) {
                        ret.add(card);
                        cardArray.remove(i);
                        
                        if(ret.size() == num) {
                            return ret.toArray(new Card[ret.size()]);
                        }
                    }
                }
            }
        }
        
        // By cost...
        int cost = 0;
        while(cost <= COST_MAX) {
            for (int i = cardArray.size() - 1; i >= 0; i--) {
                Card card = cardArray.get(i);
                if (card.getCost(context) == cost) {
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
            } else if (c.equals(Cards.horseTraders)) {
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
        return bestCardInPlay(context, 4);
    }

    @Override
    public Card feast_cardToObtain(MoveContext context) {
        return bestCardInPlay(context, 5);
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
        return bestCardInPlay(context, maxCost, false, potion);
    }

    @Override
    public Card[] militia_attack_cardsToKeep(MoveContext context) {
        ArrayList<Card> cards = new ArrayList<Card>();

        // Just add in the non-victory cards...
        for (Card card : context.getPlayer().getHand()) {
            if (!shouldDiscard(card)) {
                cards.add(card);
            }
        }

        // Still more than 3? Remove all but one action...
        while (cards.size() > 3) {
            int bestAction = -1;
            boolean removed = false;
            for (int i = 0; i < cards.size(); i++) {
                if (cards.get(i) instanceof ActionCard) {
                    if (bestAction == -1) {
                        bestAction = i;
                    } else {
                        if(cards.get(i).getCost(context) > cards.get(bestAction).getCost(context)) {
                            cards.remove(bestAction);
                            bestAction = i;
                        }
                        else {
                            cards.remove(i);
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
        while (cards.size() > 3) {
            boolean removed = false;
            for (int i = 0; i < cards.size(); i++) {
                if (cards.get(i).equals(Cards.copper)) {
                    cards.remove(i);
                    removed = true;
                    break;
                }
            }
            if (!removed) {
                break;
            }
        }

        // Still more than 3? Start removing silver...
        while (cards.size() > 3) {
            boolean removed = false;
            for (int i = 0; i < cards.size(); i++) {
                if (cards.get(i).equals(Cards.silver)) {
                    cards.remove(i);
                    removed = true;
                    break;
                }
            }
            if (!removed) {
                break;
            }
        }

        while (cards.size() > 3) {
            cards.remove(0);
        }

        if (cards.size() < 3) {
            cards.clear();
            for (int i = 0; i < 3; i++) {
                cards.add(context.getPlayer().getHand().get(i));
            }
        }

        return cards.toArray(new Card[0]);
    }

    @Override
    public boolean chancellor_shouldDiscardDeck(MoveContext context) {
        return true;
    }

    @Override
    public TreasureCard mine_treasureFromHandToUpgrade(MoveContext context) {
        ArrayList<TreasureCard> handCards = context.getPlayer().getTreasuresInHand();
        Collections.sort(handCards, new CardValueComparator());

        HashSet<Integer> treasureCardValues = new HashSet<Integer>();
        for (Card card : context.getTreasureCardsInPlay()) {
            if (context.getCardsLeft(card) > 0)
                treasureCardValues.add(card.getCost(context));
        }

        for (int i = 0; i < handCards.size(); i++) {
            TreasureCard card = handCards.get(i);
            if (treasureCardValues.contains(card.getCost(context) + 3))
                return card;
        }

        return null;
    }

    @Override
    public TreasureCard mine_treasureToObtain(MoveContext context, int cost, boolean potion) {
        TreasureCard newCard = null;
        int newCost = -1;
        for (Card card : context.getTreasureCardsInPlay()) {
            if (context.getCardsLeft(card) > 0 && card.getCost(context) <= cost && card.getCost(context) >= newCost) {
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
            if ((!(card instanceof ActionCard) && !(card instanceof TreasureCard)) || card.equals(Cards.cellar) || card.equals(Cards.copper)) {
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
    public boolean library_shouldKeepAction(MoveContext context, ActionCard action) {
        if (context.getActionsLeft() == 0) {
            return false;
        }
        for (Card card : context.getPlayer().getHand()) {
            if (card instanceof ActionCard) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean spy_shouldDiscard(MoveContext context, Player targetPlayer, Card card) {
        boolean ret;
        if (isOnlyVictory(card) || card.equals(Cards.copper) || card.equals(Cards.curse))
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
        return spy_shouldDiscard(context,  targetPlayer, card);
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
        for (Card c : context.getPlayer().getHand()) {
            if(c.equals(Cards.watchTower) || c.equals(Cards.trader)) {
                return Player.TorturerOption.TakeCurse;
            }
        }
        
        if (context.getPlayer().getHand().size() < 5) {
            int count = 0;
            for (Card c : context.getPlayer().getHand()) {
                if (shouldDiscard(c) || c.equals(Cards.copper)) {
                    count++;
                }
            }

            if (count >= 2) {
                return Player.TorturerOption.DiscardTwoCards;
            }

            return Player.TorturerOption.TakeCurse;
        }
        else {
            int count = 0;
            for (Card c : context.getPlayer().getHand()) {
                if (shouldDiscard(c)) {
                    count++;
                }
            }

            if (count >= 2) {
                return Player.TorturerOption.DiscardTwoCards;
            }
            
            if(context.getCoinAvailableForBuy() >= 8) {
                return Player.TorturerOption.TakeCurse;
            }
            
            return Player.TorturerOption.DiscardTwoCards;
        }
    }

    @Override
    public StewardOption steward_chooseOption(MoveContext context) {
        return StewardOption.AddGold;
    }

    @Override
    public Card[] steward_cardsToTrash(MoveContext context) {
        // This would normally need to return two cards, but since we always return AddGold for the
        // steward_chooseOption(), this should never be called
        return null;
    }

    @Override
    public Card swindler_cardToSwitch(MoveContext context, int cost, boolean potion) {
        Card[] cards = context.getCardsInPlay();
        ArrayList<Card> changeList = new ArrayList<Card>();
        for (Card card : cards) {
            if (!card.isPrize() && card.getCost(context) == cost && context.getCardsLeft(card) > 0 && card.costPotion() == potion) {
                changeList.add(card);
            }
        }

        boolean latest = game.colonyInPlay? 
 context.getCardsLeft(Cards.province) < Game.numPlayers || context.getCardsLeft(Cards.colony) < Game.numPlayers : context.getCardsLeft(Cards.province) < Game.numPlayers;

        if (changeList.contains(Cards.curse)) {
            return Cards.curse;
        } else if (changeList.contains(Cards.estate) && !latest) {
            return Cards.estate;
        } else if (changeList.contains(Cards.duchy) && !latest) {
            return Cards.duchy;
        } else if (changeList.contains(Cards.peddler)) {
            return Cards.peddler;
        } else if (changeList.contains(Cards.potion)) {
            return Cards.potion;
        }

        if (changeList.size() > 0) {
            return changeList.get(rand.nextInt(changeList.size()));
        }

        return null;
    }

    @Override
    public Card[] torturer_attack_cardsToDiscard(MoveContext context) {
        return lowestCards(context, context.getPlayer().getHand(), 2, true);
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
        return bestCardInPlay(context, 4);
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
        if(turnCount >= midGame || context.getCoinAvailableForBuy() >= 6) {
            return true;
        }
        
        return false;
    }

    @Override
    public Card saboteur_cardToObtain(MoveContext context, int maxCost, boolean potion) {
        return bestCardInPlay(context, maxCost, false, potion);
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

    // Either return two cards, or null if you do not want to trash any cards.
    @Override
    public Card[] tradingPost_cardsToTrash(MoveContext context) {
        return null;
    }

    @Override
    public Card wishingWell_cardGuess(MoveContext context) {
        return Cards.silver;
    }

    @Override
    public Card upgrade_cardToTrash(MoveContext context) {
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
            if(isTrashCard(c)) {
                return c;
            }
        }
        
        for (Card c : context.getPlayer().getHand()) {
            if(c instanceof VictoryCard) {
                continue;
            }
            
            for(Card avail : context.getCardsInPlay()) {
                if(avail.getCost(context) == c.getCost(context) + 1 && context.getCardsLeft(avail) > 0 && context.getEmbargos(avail) == 0 && !avail.costPotion()) {
                    return avail;
                }
            }
        }
        
        return null;
    }

    @Override
    public Card upgrade_cardToObtain(MoveContext context, int exactCost, boolean potion) {
        return bestCardInPlay(context, exactCost, true, potion);
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
        for (int i = 0; i < context.getPlayer().getHand().size() - 3; i++) {
            cards.add(context.getPlayer().getHand().get(i));
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
    public int treasury_putBackOnDeck(MoveContext context, int treasuryCardsInPlay) {
        return treasuryCardsInPlay;
    }

    @Override
    public boolean pirateShip_takeTreasure(MoveContext context) {
        if (getPirateShipTreasure() == 0) {
            return false;
        }

        if (context.getCoinAvailableForBuy() >= 8) {
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
            if (card.getCost(context) <= 6 && context.getCardsLeft(card) > 0) {
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
        Card[] cards = context.getCardsInPlay();
        return cards[rand.nextInt(cards.length)];
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
        return lowestCard(context, cl, false);
    }

    @Override
    public Card ambassador_revealedCard(MoveContext context) {
        return pickOutCard(context.getPlayer().getHand(), getTrashCards());
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
        return (ActionCard) bestCardInPlay(context, 5, false, false, true, false);
    }

    @Override
    public Card apprentice_cardToTrash(MoveContext context) {
        return pickOutCard(context.getPlayer().getHand(), getTrashCards());
    }

    @Override
    public Card transmute_cardToTrash(MoveContext context) {
        return pickOutCard(context.getPlayer().getHand(), getTrashCards());
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
        //TODO: better logic...
        return pickOutCard(context.getPlayer().getHand(), getTrashCards());
    }

    @Override
    public Card bishop_cardToTrash(MoveContext context) {
        return pickOutCard(context.getPlayer().getHand(), getTrashCards());
    }

    @Override
    public Card expand_cardToTrash(MoveContext context) {
        //TODO: better logic...
        return pickOutCard(context.getPlayer().getHand(), getTrashCards());
    }

    @Override
    public Card expand_cardToObtain(MoveContext context, int maxCost, boolean potion) {
        return bestCardInPlay(context, maxCost, false, potion);
    }

    @Override
    public Card[] forge_cardsToTrash(MoveContext context) {
        return pickOutCards(context.getPlayer().getHand(), context.getPlayer().getHand().size(), getTrashCards());
    }

    @Override
    public Card forge_cardToObtain(MoveContext context, int exactCost) {
        return bestCardInPlay(context, exactCost, true);
    }

    @Override
    public Card[] goons_attack_cardsToKeep(MoveContext context) {
        return militia_attack_cardsToKeep(context);
    }

    @Override
    public TreasureCard mint_treasureToMint(MoveContext context) {
        Card cardToMint = null;
        int cost = -1;
        for (Card c : context.getPlayer().getTreasuresInHand()) {
            if (c instanceof TreasureCard && context.game.pileSize(c) > 0) {
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
        // TODO:: Finish prosperity
        return pickOutCard(context.getPlayer().getHand(), getTrashCards());
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

        if (game.colonyInPlay && turnCount > midGame && !cantBuy.contains(Cards.colony)) {
            return Cards.colony;
        } else if (game.colonyInPlay && turnCount < midGame && game.pileSize(Cards.platinum) > 0 && !cantBuy.contains(Cards.platinum)) {
            return Cards.platinum;
        } else if (turnCount > midGame && !cantBuy.contains(Cards.province)) {
            return Cards.province;
        } else if (game.colonyInPlay && game.pileSize(Cards.platinum) > 0 && !cantBuy.contains(Cards.platinum)) {
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
        return kingsCourt_cardToPlay(context);
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
            if (card.equals(Cards.illGottenGains)) {
                ret.add(cardArray.remove(i));
            }
        }

        for (int i = cardArray.size() - 1; i >= 0; i--) {
            TreasureCard card = cardArray.get(i);
            if(!card.equals(Cards.bank) && !card.equals(Cards.venture) && !card.equals(Cards.hornOfPlenty)) {
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
        
        for (Card c : context.getPlayer().getHand()) {
            if(c.equals(Cards.curse)) {
                return c;
            }
        }

        
        for (Card c : context.getPlayer().getHand()) {
            if(isTrashCard(c) && isOnlyVictory(c)) {
                return c;
            }
        }
        
        for (Card c : context.getPlayer().getHand()) {
            if(isTrashCard(c)) {
                return c;
            }
        }
        
        return null;
    }

    @Override
    public Card hamlet_cardToDiscardForBuy(MoveContext context) {
        return null;
    }

    @Override
    public Card hornOfPlenty_cardToObtain(MoveContext context, int maxCost) {
        return bestCardInPlay(context, maxCost);
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
                if(check.getCost(context) == 7 && context.canBuy(Cards.province)) {
                    c = check;
                    break;
                }
            }
        }
        
        if(rand.nextBoolean()) {
            
            if(c == null) {
                for (Card check : context.getPlayer().getHand()) {
                    if(isOnlyVictory(check))
                        continue;
                    
                    Card best = bestCardInPlay(context,  check.getCost(context) + 1);
                    
                    if(best != null) {
                        c = check;
                        break;
                    }
                }
            }
            
        }
        
        return c;
    }

    @Override
    public Card remake_cardToObtain(MoveContext context, int exactCost, boolean potion) {
        return bestCardInPlay(context, exactCost, true, potion);
    }

    @Override
    public boolean tournament_shouldRevealProvince(MoveContext context) {
        return true;
    }

    
    @Override
    public TournamentOption tournament_chooseOption(MoveContext context) {
        for(Card c : context.getCardsInPlay()) {
            if(c.isPrize() && context.getPileSize(c) > 0) {
                return TournamentOption.GainPrize;
            }
        }
        return TournamentOption.GainDuchy;
    }

    @Override
    public Card tournament_choosePrize(MoveContext context) {
        for(Card c : context.getCardsInPlay()) {
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
        return militia_attack_cardsToKeep(context);
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
        int actions = 0;
        for (Card card : context.getPlayer().getHand()) {
            if (card instanceof ActionCard) {
                actions++;
            }
        }

        if(actions > 1) {
            for (Card card : context.getPlayer().getHand()) {
                if (card instanceof VictoryCard && !isOnlyVictory(card)) {
                    return (VictoryCard) card;
                }
            }
        }
        
        for (Card card : context.getPlayer().getHand()) {
            if (card instanceof VictoryCard && isOnlyVictory(card)) {
                return (VictoryCard) card;
            }
        }
        
        for (Card card : context.getPlayer().getHand()) {
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
        return isCurse(card) || isOnlyVictory(card);
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
        // TODO: Finish hinterlands
        return lowestCard(context, context.getPlayer().getHand(), false);
    }

    @Override
    public Card develop_lowCardToGain(MoveContext context, int cost, boolean potion) {
        return bestCardInPlay(context, cost, true, potion);
    }
    
    @Override
    public Card develop_highCardToGain(MoveContext context, int cost, boolean potion) {
        return bestCardInPlay(context, cost, true, potion);
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
        if(isTrashCard(card) || isOnlyVictory(card)) {
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
        return warehouse_cardsToDiscard(context);
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
        if(actions == null || actions.length == 0) {
            return null;
        }
        
        int i = 0;
        int cost = actions[0].getCost(context);
        for(int index = 1; index < actions.length; index++) {
            if(actions[index].getCost(context) >= cost) {
                cost = actions[index].getCost(context);
                i = index;
            }
        }
        
        return actions[i];
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
        //TODO: better logic
        if (context.getPlayer().getHand().size() == 0) {
            return null;
        }
        
        for (Card card : context.getPlayer().getHand()) {
            if (isTrashCard(card)) {
                return card;
            }
        }
        
        return context.getPlayer().getHand().get(0);
    }

    @Override
    public boolean oracle_shouldDiscard(MoveContext context, Player player, ArrayList<Card> cards) {
        boolean discard = true;
        for(Card c : cards) {
            if(isTrashCard(c) || isOnlyVictory(c)) {
                discard = false;
                break;
            }
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
        return bestCardInPlay(context, maxCost, false, potion, false, false, potion ? maxCost + 1 : maxCost);
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
        return bestCardInPlay(context, Cards.borderVillage.getCost(context) - 1);
    }

    @Override
    public Card farmland_cardToTrash(MoveContext context) {
        return remodel_cardToTrash(context);
    }

    @Override
    public Card farmland_cardToObtain(MoveContext context, int exactCost, boolean potion) {
        return bestCardInPlay(context, exactCost, true, potion);
    }

    @Override
    public TreasureCard stables_treasureToDiscard(MoveContext context) {
        for (Card card : context.getPlayer().getHand()) {
            for(Card trash : getTrashCards()) {
                if(trash.equals(card) && (card instanceof TreasureCard)) {
                    return (TreasureCard) card;
                }
            }
        }
        
        if (Game.rand.nextBoolean() && context.getPlayer().getHand().contains(Cards.silver)) {
            return (TreasureCard) context.getPlayer().fromHand(Cards.silver);
        }

        return null;
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
        return militia_attack_cardsToKeep(context);
    }    
    
    @Override
    public boolean revealBane(MoveContext context) {
        return true;
    }
    
    @Override
    public PutBackOption selectPutBackOption(MoveContext context, List<PutBackOption> options) {
        Collections.sort(options);
        return options.get(0);
    }
    
}
