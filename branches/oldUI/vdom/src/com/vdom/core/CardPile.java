package com.vdom.core;

import com.vdom.api.*;
import java.util.ArrayList;

public class CardPile {
    public Card card;
    private ArrayList<Card> cards = new ArrayList<Card>();
    public boolean tradeRouteToken = false;
    public CardPile(Card card, int count) {
        this.card = card;

        for (int i = 1; i <= count; i++) {
            // TODO: put in checks to make sure template card is never
            // "put into play".
            CardImpl thisCard = ((CardImpl) card).instantiate();
            cards.add(thisCard);
        }
    }

    /**
     * @return the count
     */
    public boolean isEmpty() {
        return cards.isEmpty();
    }

    public int takeTradeRouteToken() {
    	if (tradeRouteToken) {
	        tradeRouteToken = false;
	        return 1;
	    	}
    	return 0;
    }

    public void setTradeRouteToken() {
        tradeRouteToken = true;
    }

    public boolean hasTradeRouteToken() {
        return tradeRouteToken;
    }

    public int getCount() {
        return cards.size();
    }

    public void addCard(Card card) {
        cards.add(card);
    }

    public Card removeCard() {
        if (!cards.isEmpty()) {
            return cards.remove(cards.size() - 1);
        } else {
            return null;
        }
    }

}

