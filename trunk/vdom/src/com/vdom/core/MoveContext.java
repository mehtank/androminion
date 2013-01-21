package com.vdom.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import com.vdom.api.ActionCard;
import com.vdom.api.Card;
import com.vdom.api.CardCostComparator;
import com.vdom.api.DurationCard;
import com.vdom.api.GameEventListener;
import com.vdom.api.GameType;
import com.vdom.api.TreasureCard;

public class MoveContext {
    public int actions = 1;
    public int buys = 1;
    public int addGold = 0;

    public int gold;
    public int potions;
    public int actionsPlayedSoFar = 0;
    public int treasuresPlayedSoFar = 0;
    public int goldAvailable;
    public int coppersmithsPlayed = 0;
    public int schemesPlayed = 0;
    
    public int foolsGoldPlayed = 0;

    public int golemInEffect = 0;
    public int freeActionInEffect = 0;
    public int cardCostModifier = 0;
    public int victoryCardsBoughtThisTurn = 0;
    public int totalCardsBoughtThisTurn = 0;
    public boolean buyPhase = false;
    public ArrayList<Card> cantBuy = new ArrayList<Card>();
    public int beggarSilverIsOnTop = 0;

    public enum PileSelection {DISCARD,HAND,DECK,ANY};
    public PileSelection hermitTrashCardPile = PileSelection.ANY;

    // For checking Achievements
    public int vpsGainedThisTurn = 0;
    public int cardsTrashedThisTurn = 0;

    public String message;
//    public ArrayList<Card> playedCards = new ArrayList<Card>();
//    public CardList playedCards;
    public Player player;
    public Game game;
    
    public Player attackedPlayer;

    public MoveContext(Game game, Player player) {
        this.game = game;
        this.player = player;
//        this.playedCards = player.playedCards;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public boolean isQuickPlay() {
        return Game.quickPlay;
    }
    
    public int getPotions() {
        return potions;
    }
    
    public ArrayList<Card> getCantBuy() {
        return cantBuy;
    }

	public CardList getPlayedCards() {
        return player.playedCards;
    }

	public int countCardsInPlay(Card card) {
        int cardsInPlay = 0;
        for(Card c : getPlayedCards()) {
            if(c.behaveAsCard().equals(card)) {
            	cardsInPlay++;
            }
        }
        return cardsInPlay;
	}

    public boolean isRoyalSealInPlay() {
    	return (countCardsInPlay(Cards.royalSeal) > 0);
	}

    public int countGoonsInPlayThisTurn() {
    	return countCardsInPlay(Cards.goons);
    }
    
    public int countTreasureCardsInPlayThisTurn() {
        int treasuresInPlay = 0;
        for(Card c : getPlayedCards()) {
            if(c instanceof TreasureCard) {
            	treasuresInPlay++;
            }
        }

        return treasuresInPlay;
    }
    
    public int countActionCardsInPlayThisTurn() {
        int actionsInPlay = 0;
        for(Card c : getPlayedCards()) {
            if(c.behaveAsCard() instanceof ActionCard) {
                actionsInPlay++;
            }
        }
        for(Card c : player.nextTurnCards) {
            if(c.behaveAsCard() instanceof DurationCard) {
                actionsInPlay++;
            }
        }

        return actionsInPlay;
    }
    
    public int countUniqueCardsInPlayThisTurn() {
        HashSet<String> distinctCardsInPlay = new HashSet<String>();

        for (Card cardInPlay : player.playedCards) {
            distinctCardsInPlay.add(cardInPlay.behaveAsCard().getName());
        }
        for (Card cardInPlay : player.nextTurnCards) {
            distinctCardsInPlay.add(cardInPlay.behaveAsCard().getName());
        }

        return distinctCardsInPlay.size();
    }
    
    public int getVictoryCardsBoughtThisTurn() {
        return victoryCardsBoughtThisTurn;
    }

    public int getTotalCardsBoughtThisTurn() {
        return totalCardsBoughtThisTurn;
    }

    public boolean buyWouldEndGame(Card card) {
        return game.buyWouldEndGame(card);
    }

    public int countThroneRoomsInEffect() {
        return freeActionInEffect;
    }

	public int getPileSize(Card card) {
        return game.pileSize(card);
    }

    public int emptyPileCount() {
        return game.emptyPiles();
    }

    public int getEmbargos(Card card) {
        return game.getEmbargos(card);
    }

    public ArrayList<Card> getCardsObtainedByLastPlayer() {
        return game.getCardsObtainedByLastPlayer();
    }

    public HashMap<String, Integer> getCardCounts() {
        HashMap<String, Integer> cardCounts = new HashMap<String, Integer>();
        for (String cardName : game.piles.keySet()) {
            int count = game.piles.get(cardName).getCount();
            if (count > 0) {
                cardCounts.put(cardName, count);
            }
        }
        return cardCounts;
    }

    public Card[] getBuyableCards() {
        ArrayList<Card> buyableCards = new ArrayList<Card>();
        for (Card card : getCardsInGame()) {
            if (canBuy(card)) {
                buyableCards.add(card);
            }
        }

        Collections.sort(buyableCards, new CardCostComparator());
        return buyableCards.toArray(new Card[0]);
    }

    public void addGameListener(GameEventListener listener) {
        if (listener != null && !game.listeners.contains(listener)) {
            game.listeners.add(listener);
        }
    }

    public void removeGameListener(GameEventListener listener) {
        if (listener != null && game.listeners.contains(listener)) {
            game.listeners.remove(listener);
        }
    }

    public boolean cardsSpecifiedOnStartup() {
        return Game.cardsSpecifiedAtLaunch != null && Game.cardsSpecifiedAtLaunch.length > 0;
    }

    public GameType getGameType() {
        return Game.gameType;
    }

    public boolean canPlay(Card card) {
        if (card instanceof ActionCard) {
            return game.isValidAction(this, (ActionCard) card);
        } else {
            return false;
        }
    }

    public boolean canBuy(Card card) {
        return game.isValidBuy(this, card);
    }

    public boolean canBuy(Card card, int gold) {
        return game.isValidBuy(this, card, gold);
    }

    public int getActionsLeft() {
        return actions;
    }

    public int getBuysLeft() {
        return buys;
    }

    public int getCoinAvailableForBuy() {
        return gold + addGold;
    }

    public int getCoinForStatus() {
        return getCoinAvailableForBuy();

        /*
        if(player.playedCards.size() > 0) {
            return getCoinAvailableForBuy();
        }

        int coin = 0;
        int foolsgoldcount = 0;
        for (Card card : player.getHand()) {
            if (card instanceof TreasureCard) {
                coin += ((TreasureCard) card).getValue();
                 if (card.getType() == Cards.Type.FoolsGold) {
                 foolsgoldcount++;
                 if (foolsgoldcount > 1) {
                 coin += 3;
                 }
                 }
            }
        }

        return coin;
        */
    }

    public int getPotionsForStatus(Player p) {
        if(p.playedCards.size() > 0) {
            return potions;
        }

        int count = 0;
        for (Card card : player.getHand()) {
            if (card.equals(Cards.potion)) {
                count++;
            }
        }

        return count;
    }
    
    public void debug(String msg) {
        debug(msg, true);
    }

    private void debug(String msg, boolean prefixWithPlayerName) {
        if (!prefixWithPlayerName || player == null) {
            Util.debug(msg);
        } else {
            player.debug(msg);
        }
    }
    
    public String getAttackedPlayer() {
        return (attackedPlayer == null)?null:attackedPlayer.getPlayerName();
    }
    
    public String getMessage() {
        return message;
    }

    // Delegate Cards in play to game
    public Card[] getCardsInGame() {
		return game.getCardsInGame();
	}
    
    public boolean cardInGame(Card card) {
		return game.cardInGame(card);
    }

    public int getCardsLeftInPile(Card card) {
		return game.getCardsLeftInPile(card);
    }

    public Card[] getTreasureCardsInGame() {
        return game.getTreasureCardsInGame();
    }

    public Card[] getVictoryCardsInGame() {
        return game.getVictoryCardsInGame();
    }

    protected boolean isNewCardAvailable(int cost, boolean potion) {
        for(Card c : getCardsInGame()) {
            if(c.getCost(this) == cost && c.costPotion() == potion && getCardsLeftInPile(c) > 0) {
                return true;
            }
        }
        
        return false;
    }
    
    protected Card[] getAvailableCards(int cost, boolean potion) {
        ArrayList<Card> cards = new ArrayList<Card>();
        for(Card c : getCardsInGame()) {
            if(c.getCost(this) == cost && c.costPotion() == potion && getCardsLeftInPile(c) > 0) {
                cards.add(c);
            }
        }
        
        return cards.toArray(new Card[0]);
    }

}
