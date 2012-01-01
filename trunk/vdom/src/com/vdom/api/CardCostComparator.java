package com.vdom.api;

import java.util.Comparator;

import com.vdom.core.Cards;

public class CardCostComparator implements Comparator<Card> {
    public int compare(Card cardOne, Card cardTwo) {
        if (cardOne.getCost(null) == cardTwo.getCost(null)) {
            if (cardOne.costPotion() || cardTwo.costPotion()) {
                if (cardOne.costPotion() && cardTwo.costPotion()) {
                    return 0;
                } else if (cardOne.costPotion()) {
                    return -1;
                } else {
                    return 1;
                }
            } else {
                if (cardOne.equals(Cards.curse)) {
                    return 1;
                } else if (cardTwo.equals(Cards.curse)) {
                    return -1;
                } else {
                    return 0;
                }
            }
        } else if (cardOne.getCost(null) > cardTwo.getCost(null)) {
            return -1;
        } else {
            return 1;
        }
    }
}
