package com.vdom.comms;

import java.io.Serializable;

public class GameStatus implements Serializable {
	private static final long serialVersionUID = -5928579898003313213L;

	public int whoseTurn;
	public String name;
	public String[] realNames;
	public boolean isFinal;
    public boolean isPossessed;
    public int[] turnCounts;
	public int[] myHand;
	public int[] playedCards;
	public int[] myIsland;
	public int[] myVillage;
	public int[] trashPile;
	public int[] turnStatus;
	public int[] supplySizes;
	public int[] embargos;
	public int[] costs;
	public int[] deckSizes;
	public int[] handSizes;
	public int[] numCards;
	public int[] pirates;
	public int[] victoryTokens;
	public int[] guildsCoinTokens;
	public int cardCostModifier;
	public int potions;
	public String ruinsTopCard;
	public String ruinsTopCardDesc;
	public int ruinsID;
	public String knightsTopCard;
	public String knightsTopCardDesc;
	public int knightsTopCardCost;
	public int knightsID;

	public GameStatus setFinal(boolean b) {isFinal = b; return this;}
    public GameStatus setPossessed(boolean b) {isPossessed = b; return this;}
    public GameStatus setTurnCounts(int[] is) {turnCounts = is; return this;};
    public GameStatus setRealNames(String[] is) {realNames = is; return this;};
	public GameStatus setCurPlayer(int i) {whoseTurn = i; return this;}
	public GameStatus setCurName(String s) {name = s; return this;}
	public GameStatus setHand(int[] is) {myHand = is; return this;};
	public GameStatus setPlayedCards(int[] is) {playedCards = is; return this;};
	public GameStatus setIsland(int[] is) {myIsland = is; return this;};
	public GameStatus setVillage(int[] is) {myVillage = is; return this;};
	public GameStatus setTurnStatus(int[] is) {turnStatus = is; return this;};
	public GameStatus setSupplySizes(int[] is) {supplySizes = is; return this;};
	public GameStatus setEmbargos(int[] is) {embargos = is; return this;};
	public GameStatus setCosts(int[] is) {costs = is; return this;};
	public GameStatus setDeckSizes(int[] is) {deckSizes = is; return this;};
	public GameStatus setHandSizes(int[] is) {handSizes = is; return this;};
	public GameStatus setNumCards(int[] is) {numCards = is; return this;}
	public GameStatus setPirates(int[] is) {pirates = is; return this;}
    public GameStatus setVictoryTokens(int[] is) {victoryTokens = is; return this;}
    public GameStatus setGuildsCoinTokens(int[] is) {guildsCoinTokens = is; return this;}
	public GameStatus setCardCostModifier(int i) {cardCostModifier = i; return this;}
    public GameStatus setPotions(int i) {potions = i; return this;}
    public GameStatus setTrash(int[] is) {trashPile = is; return this;}
    public GameStatus setRuinsTopCard(int i, String s, String d) {ruinsTopCard = s; ruinsTopCardDesc = d; ruinsID = i; return this;}
    public GameStatus setKnightTopCard(int i, String s, String d, int c) {knightsTopCard = s; knightsTopCardDesc = d; knightsID = i; knightsTopCardCost = c; return this;}
    
	public String toString() {
		String str = name + "(" + whoseTurn + ")";
		return str;
	}
}