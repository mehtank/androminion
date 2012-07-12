package com.mehtank.androminion.util;

public class PlayerSummary {
	public String name;
	public int deckSize;
	public int handSize;
	public int numCards;
	public int pt;
	public int vt;
	public boolean highlight = false;
	public int turns;
	
	public PlayerSummary(String name) {
		this.name = name;
	}
	
	public void set(String name, int turns, int deckSize, int handSize, int numCards, int pt, int vt, boolean highlight){
		this.name = name;
		this.turns = turns;
		this.deckSize = deckSize;
		this.handSize = handSize;
		this.numCards = numCards;
		this.pt = pt;
		this.vt = vt;
		this.highlight = highlight;
	}
	
	@Override
	public String toString() {
		return name; 
	}
}