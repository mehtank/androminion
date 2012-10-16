package com.vdom.core;

import java.util.ArrayList;

import com.vdom.api.*;

public class SingleCardPile extends AbstractCardPile {
    private Card card;
    
    public SingleCardPile(Card card, int count) {
        this.card = card;
        this.type = PileType.SingleCardPile;
        this.cards = new ArrayList<Card>();

        for (int i = 0; i < count; i++) {
            // TODO: put in checks to make sure template card is never
            // "put into play".
            CardImpl thisCard = ((CardImpl) card).instantiate();
            cards.add(thisCard);
        }
    }

    @Override
    public SingleCardPile notInSupply() {
    	this.isSupply = false;
    	return this;
    }
    
    @Override
    public boolean isSupply() {
    	return this.isSupply;
    }

    @Override
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

	@Override
	public Card card() {
		return card;
	}

}
