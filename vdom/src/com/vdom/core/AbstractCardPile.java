package com.vdom.core;

import java.util.ArrayList;

import com.vdom.api.Card;

public abstract class AbstractCardPile {

	protected ArrayList<Card> cards;
	protected ArrayList<Card> templateCards;
    protected boolean isSupply = true;
    protected boolean isBlackMarket = false;
    protected boolean tradeRouteToken = false;

	protected boolean allCardsVisible = true;

	protected Card placeholderCard = null;

	public boolean isEmpty() {
        return cards.isEmpty();
    }

	public AbstractCardPile notInSupply() {
    	this.isSupply = false;
    	return this;
    }

	public AbstractCardPile inBlackMarket() {
    	this.isBlackMarket = true;
    	return this;
    }

	public boolean isSupply() {
    	return this.isSupply;
    }

	public boolean isBlackMarket() {
    	return this.isBlackMarket;
    }

	public int getCount() {
        return cards.size();
    }

	@Override
	public String toString() {
		return cards.toString();
	}
	
	public abstract Card topCard();
	public Card placeholderCard() {
		return placeholderCard;
	}

	public boolean cardAllowedOnPile(Card card) {
		if (card.isTemplateCard()) return false; //No template card allowed on the pile

		for (Card template : this.templateCards) {
			if (template.equals(card)) {
				return true;
			}
		}
		return false;
	}
	
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

	public boolean isAllCardsVisible() { return this.allCardsVisible; }

	public void addCard(Card card) {
		if (cardAllowedOnPile(card)) {
				cards.add(0, card);
		}
	}

	public abstract Card removeCard();

}
