package com.vdom.core;

import com.vdom.api.Card;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

public abstract class PileCreator implements Serializable {
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
        for (Card ruin : Cards.ruinsCards) {
            cards.put(ruin, 0);
        }

        ArrayList<Card> ruins = new ArrayList<Card>();
        for (int i = 0; i < 10; i++) {
            ruins.add(Cards.abandonedMine);
            ruins.add(Cards.ruinedLibrary);
            ruins.add(Cards.ruinedMarket);
            ruins.add(Cards.ruinedVillage);
            ruins.add(Cards.survivors);
        }
        Collections.shuffle(ruins);

        int i = 0;
        for (Card c : ruins) {
            cards.put(c, cards.get(c) + 1);
            if (++i >= count) {
                break;
            }
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

        cards.put(Cards.humbleCastle,    count == 8 ? 1 : 2);
        cards.put(Cards.crumblingCastle, 1);
        cards.put(Cards.smallCastle,     count == 8 ? 1 : 2);
        cards.put(Cards.hauntedCastle,   1);
        cards.put(Cards.opulentCastle,   count == 8 ? 1 : 2);
        cards.put(Cards.sprawlingCastle, 1);
        cards.put(Cards.grandCastle,     1);
        cards.put(Cards.kingsCastle,     count == 8 ? 1 : 2);

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