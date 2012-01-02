package com.vdom.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import com.vdom.api.ActionCard;
import com.vdom.api.Card;
import com.vdom.api.CardCostComparator;
import com.vdom.api.GameEventListener;
import com.vdom.api.GameType;
import com.vdom.api.TreasureCard;
import com.vdom.api.VictoryCard;

public class MoveContext {
    public int actions = 1;
    public int buys = 1;
    public int addGold = 0;

    public int gold;
    public int potions;
    public int actionsPlayedSoFar = 0;
    public int treasuresPlayedSoFar = 0;
    public boolean copperPlayed = false;
    public int coppersmithsPlayed = 0;
    public int goonsPlayed = 0;
    public int hoardsPlayed = 0;
    public int throneRoomsInEffect = 0;
    public int numberTimesAlreadyPlayed = 0;
    public int quarriesPlayed = 0;
    public boolean royalSealPlayed = false;
    public int possessionsToProcess = 0;
    public int talismansPlayed = 0;
    public int foolsGoldPlayed = 0;
    public int schemesPlayed = 0;
    public int cardCostModifier = 0;
    public int victoryCardsBoughtThisTurn = 0;
    public int totalCardsBoughtThisTurn = 0;
    public ArrayList<Card> cantBuy = new ArrayList<Card>();
    public Player getPossessedBy() { return game.possessingPlayer; };
    public ArrayList<Card> possessedTrashPile = new ArrayList<Card>();
    
    // For checking Achievements
    public int vpsGainedThisTurn = 0;
    public int cardsTrashedThisTurn = 0;

    public String message;
    public ArrayList<Card> playedCards = new ArrayList<Card>();
    public Player player;
    public Game game;
    
    public Player attackedPlayer;

    public MoveContext(Game game, Player player) {
        this.game = game;
        this.player = player;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public int calculateLead(Card card) {
        int lead = game.calculateLead(player);
        if (canBuy(card) && card instanceof VictoryCard) {
            lead += ((VictoryCard) card).getVictoryPoints();
        }

        return lead;
    }
    
    public void addToPossessedTrashPile(Card c) {
        possessedTrashPile.add(c);
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

    public int getVictoryCardsBoughtThisTurn() {
        return victoryCardsBoughtThisTurn;
    }

    public int getTotalCardsBoughtThisTurn() {
        return totalCardsBoughtThisTurn;
    }

    public boolean buyWouldEndGame(Card card) {
        return game.buyWouldEndGame(card);
    }

    public int getThroneRoomsInEffect() {
        return throneRoomsInEffect;
    }

    public int getQuarriesPlayed() {
        return quarriesPlayed;
    }
    
    public ArrayList<Card> getPlayedCards() {
        return playedCards;
    }

    public int getPileSize(Card card) {
        return game.pileSize(card);
    }

    public int emptyPileCount() {
        return game.emptyPiles();
    }

    public int getEmbargos(Card card) {
        return game.getEmbargos(card.getName());
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
        for (Card card : getCardsInPlay()) {
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

    public int getTemporaryGoldForThisTurn() {
        return addGold;
    }

    public int getCoinAvailableForBuy() {
//        if (!goldComputed) {
//            int gold = getTemporaryGoldForThisTurn();
//            for (Card card : player.getHand()) {
//                if (card instanceof TreasureCard) {
//                    gold += ((TreasureCard) card).getValue();
//                    if (card.equals(Cards.copper)) {
//                        gold += coppersmithsPlayed;
//                    }
//                }
//            }
//
//            // goldComputed = true;
//            return gold;
//        }
        return gold + addGold;
    }

    public int getCoinForStatus(Player p) {
        if(playedCards.size() > 0) {
            return getCoinAvailableForBuy();
        }

        int coin = 0;
        for (Card card : player.getHand()) {
            if (card instanceof TreasureCard) {
                coin += ((TreasureCard) card).getValue();
            }
        }

        return coin;
    }

    public int getPotionsForStatus(Player p) {
        if(playedCards.size() > 0) {
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
    
    public Card[] getActionsInPlay() {
        ArrayList<Card> actions = new ArrayList<Card>();

        for (CardPile pile : game.piles.values()) {
            if (pile.card instanceof ActionCard) {
                actions.add(pile.card);
            }
        }

        return actions.toArray(new Card[0]);
    }

    public boolean cardInPlay(Card card) {
        boolean cardInPlay = false;
        for (Card thisCard : getCardsInPlay()) {
            if (thisCard.equals(card)) {
                cardInPlay = true;
                break;
            }
        }
        return cardInPlay;
    }

    public Card[] getCardsInPlay() {
        ArrayList<Card> cards = new ArrayList<Card>();

        for (CardPile pile : game.piles.values()) {
            cards.add(pile.card);
        }

//        if (game.banePile != null) {
//            cards.add(game.banePile.card);
//        }
        return cards.toArray(new Card[0]);
    }

    public Card[] getCardsInPlayOrderByCost() {
        Card[] cardsInPlay = getCardsInPlay();
        Arrays.sort(cardsInPlay, new CardCostComparator());
        return cardsInPlay;
    }

    public int getCardsLeft(Card card) {
        CardPile pile = game.piles.get(card.getName());
        if (pile == null || pile.getCount() < 0) {
            return 0;
        }

        return pile.getCount();
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
}
