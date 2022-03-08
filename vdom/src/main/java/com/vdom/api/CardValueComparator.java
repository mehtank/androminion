package com.vdom.api;

import java.util.Comparator;

public class CardValueComparator implements Comparator<Card> {
    public int compare(Card cardOne, Card cardTwo) {
        if (cardOne.getAddGold() == cardTwo.getAddGold()) {
            return 0;
        } else if (cardOne.getAddGold() > cardTwo.getAddGold()) {
            return -1;
        } else {
            return 1;
        }
    }
}
