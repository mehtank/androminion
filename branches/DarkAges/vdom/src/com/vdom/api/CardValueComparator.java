package com.vdom.api;

import java.util.Comparator;

public class CardValueComparator implements Comparator<TreasureCard> {
    public int compare(TreasureCard cardOne, TreasureCard cardTwo) {
        if (cardOne.getValue() == cardTwo.getValue()) {
            return 0;
        } else if (cardOne.getValue() > cardTwo.getValue()) {
            return -1;
        } else {
            return 1;
        }
    }
}
