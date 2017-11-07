package com.mehtank.androminion.util;

import com.vdom.comms.GameStatus.JourneyTokenState;

/**
 * Information about one player
 * 
 * deck, hand size, total number of cards etc.
 */

public class PlayerSummary {
	@SuppressWarnings("unused")
	private static final String TAG = "PlayerSummary";
	
	public String name;
	public String realName;
	public int deckSize;
	public boolean stashOnDeck;
	public int handSize;
	public int stashesInHand;
	public int numCards;
	public int pt;
	public int vt;
	public int dt;
	public int gct; // Guilds Coin Tokens
	public JourneyTokenState journeyToken;
	public boolean minusOneCoinTokenOn;
	public boolean minusOneCardTokenOn;
	public boolean highlight = false;
	public boolean showColor = false;
	public int color;
	public int turns;
	
	public PlayerSummary(String name) {
		this.name = name;
	}
	
	public void set(String name, int turns, int deckSize, boolean stashOnDeck, int handSize, int stashesInHand, int numCards, int pt, int vt, int dt, int gct, boolean minusOneCoinTokenOn, boolean minusOneCardTokenOn, JourneyTokenState journeyToken, boolean highlight, boolean showColor, int color){
		this.name = name;
		this.turns = turns;
		this.deckSize = deckSize;
		this.stashOnDeck = stashOnDeck;
		this.handSize = handSize;
		this.stashesInHand = stashesInHand;
		this.numCards = numCards;
		this.pt = pt;
		this.vt = vt;
		this.dt = dt;
		this.gct = gct;
		this.journeyToken = journeyToken;
		this.minusOneCoinTokenOn = minusOneCoinTokenOn;
		this.minusOneCardTokenOn = minusOneCardTokenOn;
		this.highlight = highlight;
		this.showColor = showColor;
		this.color = color;
	}
	
	@Override
	public String toString() {
		return name; 
	}
}