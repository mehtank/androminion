package com.vdom.core;

import java.util.ArrayList;

import com.vdom.api.*;

public class SingleCardPile extends AbstractCardPile {
    private Card templateCard;
    
    public SingleCardPile(Card card, int count) {
        this.templateCard = card;
        this.type = PileType.SingleCardPile;
        this.cards = new ArrayList<Card>();

        for (int i = 0; i < count; i++) {
            cards.add(templateCard.instantiate());
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
            return cards.remove(0);
        } else {
            return null;
        }
    }

	@Override
	public Card card() {
		return templateCard;
	}

}
