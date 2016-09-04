package com.vdom.core;

import com.vdom.api.Card;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

public abstract class PileCreator {
    public abstract AbstractCardPile create(Card template, int count);
}

class DefaultCardPileCreator extends PileCreator {
    public AbstractCardPile create(Card template, int count) {
        return new SingleCardPile(template, count);
    }
}

class RuinsPileCreator extends PileCreator {
    public AbstractCardPile create(Card template, int count) {

        Map<Card, Integer> cards = new HashMap<Card, Integer>();

        Random rand = new Random();
        ArrayList<Integer> counts = new ArrayList<Integer>();

        //Generate n-1 numbers between 0 and count
        for (int i = 0; i < Cards.ruinsCards.size()-1; i++) {
            counts.add(rand.nextInt(count));
        }
        // Add 0 and count. Then sort the list.
        counts.add(0);
        counts.add(count);
        Collections.sort(counts);
        //Then sort the list and use the adjacent numbers difference as counts for the particular ruins.
        for (int i = 0; i < Cards.ruinsCards.size(); i++) {
            cards.put(Cards.ruinsCards.get(i), counts.get(i+1) - counts.get(i));
        }

        return new VariableCardPile(template, cards, false, false);
    }
}

class KnightsPileCreator extends PileCreator {

    public AbstractCardPile create(Card template, int count) {
        Map<Card, Integer> cards = new HashMap<Card, Integer>();
        //Currently count is ignored because there should always be ten knights.
        for (Card c: Cards.knightsCards) {
            cards.put(c, 1);
        }
        return new VariableCardPile(template, cards, false, false);

    }
}

class CastlesPileCreator extends PileCreator {
    public AbstractCardPile create(Card template, int count) {
        Map<Card, Integer> cards = new LinkedHashMap<Card, Integer>(); //LinkedHashMap preserves insertion order when iterating
        if (count != 8 && count != 12) {
            //TODO SPLITPILES What to do now?
            if (count < 8) count = 8;
            if (count > 8) count = 12;
        }
        int multiCastles = (count == 8 ? 1 : 2);
        boolean multi = true;
        for (Card c: Cards.castleCards) {
            cards.put(c, multi ? multiCastles : 1);
            multi = !multi;
        }

        return new VariableCardPile(template, cards, true, true);

    }
}

class SplitPileCreator extends PileCreator {
    private Card topCard;
    private Card bottomCard;

    public SplitPileCreator(Card topCard, Card bottomCard) {
        this.topCard = topCard;
        this.bottomCard = bottomCard;
    }

    public AbstractCardPile create(Card template, int count) {
        Map<Card, Integer> cards = new LinkedHashMap<Card, Integer>();
        cards.put(topCard, count / 2);
        cards.put(bottomCard, count / 2 + (count % 2 == 1 ? 1 : 0)); //If count is not even put the extra card on bottom
        return new VariableCardPile(template, cards, true, true);

    }
}