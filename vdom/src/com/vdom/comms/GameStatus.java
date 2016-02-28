package com.vdom.comms;

import java.io.Serializable;

import com.vdom.api.Card;

public class GameStatus implements Serializable {
    
	private static final long serialVersionUID = -1192478381540599643L;
	
	public enum JourneyTokenState {
		FACE_UP, FACE_DOWN
	}
	
	public int whoseTurn;
    public String name;
    public String[] realNames;
    public boolean isFinal;
    public boolean isPossessed;
    public int[] turnCounts;
    public int[] myHand;
    public int[] playedCards;
    public int[] myTavern;
    public int[] myPrince;
    public int[] myIsland;
    public int[] myVillage;
    public int myInheritance = -1;
    public int[] trashPile;
    public int[] blackMarketPile;
    public int[] blackMarketPileShuffled;
    public int[] turnStatus;
    public int[] supplySizes;
    public int[] embargos;
    public int[][][] tokens;
    public int[] costs;
    public int[] deckSizes;
    public boolean[] stashOnDeck;
    public int[] handSizes;
    public int[] stashesInHand;
    public int[] numCards;
    public int[] pirates;
    public int[] victoryTokens;
    public int[] guildsCoinTokens;
    public JourneyTokenState[] journeyTokens;
    public boolean[] minusOneCoinTokenOn;
    public boolean[] minusOneCardTokenOn;
    public boolean hauntedWoodsAttacks;
    public int swampHagAttacks;
    public int cardCostModifier;
    public int potions;
    public Card ruinsTopCard;
    public int ruinsID;
    public Card knightsTopCard;
    public int knightsTopCardCost;
    public boolean knightsTopCardIsVictory;
    public int knightsID;

    public GameStatus setFinal(boolean b) {isFinal = b; return this;}
    public GameStatus setPossessed(boolean b) {isPossessed = b; return this;}
    public GameStatus setTurnCounts(int[] is) {turnCounts = is; return this;};
    public GameStatus setRealNames(String[] is) {realNames = is; return this;};
    public GameStatus setCurPlayer(int i) {whoseTurn = i; return this;}
    public GameStatus setCurName(String s) {name = s; return this;}
    public GameStatus setHand(int[] is) {myHand = is; return this;};
    public GameStatus setPlayedCards(int[] is) {playedCards = is; return this;};
    public GameStatus setTavern(int[] is) {myTavern = is; return this;};
    public GameStatus setPrince(int[] is) {myPrince = is; return this;};
    public GameStatus setIsland(int[] is) {myIsland = is; return this;};
    public GameStatus setVillage(int[] is) {myVillage = is; return this;};
    public GameStatus setInheritance(int is) {myInheritance = is; return this;};
    public GameStatus setTurnStatus(int[] is) {turnStatus = is; return this;};
    public GameStatus setSupplySizes(int[] is) {supplySizes = is; return this;};
    public GameStatus setEmbargos(int[] is) {embargos = is; return this;};
    public GameStatus setTokens(int[][][] is) {tokens = is; return this;};
    public GameStatus setCosts(int[] is) {costs = is; return this;};
    public GameStatus setDeckSizes(int[] is) {deckSizes = is; return this;};
    public GameStatus setStashOnDeck(boolean[] is) {stashOnDeck = is; return this;};
    public GameStatus setHandSizes(int[] is) {handSizes = is; return this;};
    public GameStatus setStashesInHand(int[] is) {stashesInHand = is; return this;};
    public GameStatus setNumCards(int[] is) {numCards = is; return this;}
    public GameStatus setPirates(int[] is) {pirates = is; return this;}
    public GameStatus setVictoryTokens(int[] is) {victoryTokens = is; return this;}
    public GameStatus setGuildsCoinTokens(int[] is) {guildsCoinTokens = is; return this;}
    public GameStatus setJourneyToken(JourneyTokenState[] is) {journeyTokens = is; return this;}
    public GameStatus setMinusOneCoinToken(boolean[] is) {minusOneCoinTokenOn = is; return this;}
    public GameStatus setMinusOneCardToken(boolean[] is) {minusOneCardTokenOn = is; return this;}
    public GameStatus setHauntedWoodsAttacks(boolean i) {hauntedWoodsAttacks = i; return this;}
    public GameStatus setSwampHagAttacks(int i) {swampHagAttacks = i; return this;}
    public GameStatus setCardCostModifier(int i) {cardCostModifier = i; return this;}
    public GameStatus setPotions(int i) {potions = i; return this;}
    public GameStatus setTrash(int[] is) {trashPile = is; return this;}
    public GameStatus setBlackMarket(int[] is) {blackMarketPile = is; return this;}
    public GameStatus setRuinsTopCard(int i, Card c) {ruinsTopCard = c; ruinsID = i; return this;}
    public GameStatus setKnightTopCard(int i, Card c, int cost, boolean isVictory) {knightsTopCard = c; knightsID = i; knightsTopCardCost = cost; knightsTopCardIsVictory = isVictory; return this;}

    public String toString() {
        String str = name + "(" + whoseTurn + ")";
        return str;
    }
}
