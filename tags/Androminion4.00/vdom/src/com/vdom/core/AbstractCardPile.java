package com.vdom.core;

import java.util.ArrayList;

import com.vdom.api.Card;

public abstract class AbstractCardPile {

    protected ArrayList<Card> cards;
    protected boolean isSupply = true;
    protected boolean tradeRouteToken = false;
    
	static enum PileType {
		RuinsPile, KnightsPile, SingleCardPile
	}
	
	protected PileType type;


	public boolean isEmpty() {
        return cards.isEmpty();
    }

	public AbstractCardPile notInSupply() {
    	this.isSupply = false;
    	return this;
    }

	public boolean isSupply() {
    	return this.isSupply;
    }

	public int getCount() {
        return cards.size();
    }
	
	public PileType getType() {
		return type;
	}

	@Override
	public String toString() {
		return cards.toString();
	}
	
	public abstract Card card();
	
    public void setTradeRouteToken() {
        tradeRouteToken = true;
    }

    public boolean hasTradeRouteToken() {
        return tradeRouteToken;
    }

    public int takeTradeRouteToken() {
    	if (tradeRouteToken) {
	        tradeRouteToken = false;
	        return 1;
	    	}
    	return 0;
    }

	public abstract void addCard(Card card);

	public abstract Card removeCard();

}
