package com.vdom.core;

import com.vdom.api.*;
import java.util.ArrayList;

public class CardPile {
    public Card card;
    private ArrayList<Card> cards = new ArrayList<Card>();

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
    public int getCount() {
        return cards.size();
    }

    public void addCard(Card card) {
        cards.add(card);
    }

    public Card removeCard() {
        return cards.remove(cards.size() - 1);
        }
    }

