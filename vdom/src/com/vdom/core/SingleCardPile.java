package com.vdom.core;

import java.util.ArrayList;

import com.vdom.api.*;

public class SingleCardPile extends AbstractCardPile {
    
    public SingleCardPile(Card card, int count) {
        this.cards = new ArrayList<Card>();
        this.templateCards = new ArrayList<Card>();

        this.placeholderCard = card.instantiate();
        this.placeholderCard.setPlaceholderCard();
        templateCards.add(card);

        for (int i = 0; i < count; i++) {
            cards.add(card.instantiate());
        }
    }

    @Override
    public Card removeCard() {
        if (!cards.isEmpty()) {
            return cards.remove(0);
        } else {
            return null;
        }
    }

    @Override
    public Card topCard() {
        if (templateCards.size() > 0) return templateCards.get(0);
        return null;
    }

}
