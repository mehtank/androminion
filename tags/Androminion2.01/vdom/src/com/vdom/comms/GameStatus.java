package com.vdom.comms;

import java.io.Serializable;

public class GameStatus implements Serializable {
	private static final long serialVersionUID = -5928579898003313213L;

	public int whoseTurn;
	public String name;
	public boolean isFinal;
	public int[] myHand;
	public int[] playedCards;
	public int[] myIsland;
	public int[] myVillage;
	public int[] turnStatus;
	public int[] supplySizes;
	public int[] embargos;
	public int[] deckSizes;
	public int[] handSizes;
	public int[] numCards;
	public int[] pirates;
	public int[] victoryTokens;
	public int cardCostModifier;
	public int potions;

	public GameStatus setFinal(boolean b) {isFinal = b; return this;}
	public GameStatus setCurPlayer(int i) {whoseTurn = i; return this;}
	public GameStatus setCurName(String s) {name = s; return this;}
	public GameStatus setHand(int[] is) {myHand = is; return this;};
	public GameStatus setPlayedCards(int[] is) {playedCards = is; return this;};
	public GameStatus setIsland(int[] is) {myIsland = is; return this;};
	public GameStatus setVillage(int[] is) {myVillage = is; return this;};
	public GameStatus setTurnStatus(int[] is) {turnStatus = is; return this;};
	public GameStatus setSupplySizes(int[] is) {supplySizes = is; return this;};
	public GameStatus setEmbargos(int[] is) {embargos = is; return this;};
	public GameStatus setDeckSizes(int[] is) {deckSizes = is; return this;};
	public GameStatus setHandSizes(int[] is) {handSizes = is; return this;};
	public GameStatus setNumCards(int[] is) {numCards = is; return this;}
	public GameStatus setPirates(int[] is) {pirates = is; return this;}
    public GameStatus setVictoryTokens(int[] is) {victoryTokens = is; return this;}
	public GameStatus setCardCostModifier(int i) {cardCostModifier = i; return this;}
    public GameStatus setPotions(int i) {potions = i; return this;}
	public String toString() {
		String str = name + "(" + whoseTurn + ")";
		return str;
	}
}