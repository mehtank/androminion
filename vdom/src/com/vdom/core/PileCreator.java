package com.vdom.core;

import com.vdom.api.Card;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class PileCreator implements Serializable {
    public abstract CardPile create(Card template, int count);
}

class DefaultPileCreator extends PileCreator {
    public CardPile create(Card template, int count) {
        List<CardPile.CardMultiplicity> cards = new ArrayList<CardPile.CardMultiplicity>();
        cards.add(new CardPile.CardMultiplicity(template, count));
        return new CardPile(template, cards, true, true);
    }
}

class RuinsPileCreator extends PileCreator {
    public CardPile create(Card template, int count) {
        Map<Card, Integer> cardShuffle = new HashMap<Card, Integer>();
        List<CardPile.CardMultiplicity> cards = new ArrayList<CardPile.CardMultiplicity>();
        for (Card ruin : Cards.ruinsCards) {
            cardShuffle.put(ruin, 0);
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
            cardShuffle.put(c, cardShuffle.get(c) + 1);
            if (++i >= count) {
                break;
            }
        }
        for (Map.Entry<Card, Integer> entry : cardShuffle.entrySet()) {
            cards.add(new CardPile.CardMultiplicity(entry.getKey(), entry.getValue()));
        }
        return new CardPile(template, cards, false, false);
    }
}

class KnightsPileCreator extends PileCreator {

    public CardPile create(Card template, int count) {
        List<CardPile.CardMultiplicity> cards = new ArrayList<CardPile.CardMultiplicity>();
        //Currently count is ignored because there should always be ten knights.
        for (Card c: Cards.knightsCards) {
            cards.add(new CardPile.CardMultiplicity(c, 1));
        }
        return new CardPile(template, cards, false, false);

    }
}

class CastlesPileCreator extends PileCreator {
    public CardPile create(Card template, int count) {
        List<CardPile.CardMultiplicity> cards = new ArrayList<CardPile.CardMultiplicity>();
        if (count != 8 && count != 12) {
            //TODO SPLITPILES What to do now?
            if (count < 8) count = 8;
            if (count > 8) count = 12;
        }

        cards.add(new CardPile.CardMultiplicity(Cards.humbleCastle,    count == 8 ? 1 : 2));
        cards.add(new CardPile.CardMultiplicity(Cards.crumblingCastle, 1));
        cards.add(new CardPile.CardMultiplicity(Cards.smallCastle,     count == 8 ? 1 : 2));
        cards.add(new CardPile.CardMultiplicity(Cards.hauntedCastle,   1));
        cards.add(new CardPile.CardMultiplicity(Cards.opulentCastle,   count == 8 ? 1 : 2));
        cards.add(new CardPile.CardMultiplicity(Cards.sprawlingCastle, 1));
        cards.add(new CardPile.CardMultiplicity(Cards.grandCastle,     1));
        cards.add(new CardPile.CardMultiplicity(Cards.kingsCastle,     count == 8 ? 1 : 2));

        return new CardPile(template, cards, true, true);
    }
}

class SplitPileCreator extends PileCreator {
    private Card topCard;
    private Card bottomCard;

    public SplitPileCreator(Card topCard, Card bottomCard) {
        this.topCard = topCard;
        this.bottomCard = bottomCard;
    }

    public CardPile create(Card template, int count) {
        List<CardPile.CardMultiplicity> cards = new ArrayList<CardPile.CardMultiplicity>();
        cards.add(new CardPile.CardMultiplicity(topCard, count / 2));
        cards.add(new CardPile.CardMultiplicity(bottomCard, count / 2 + (count % 2 == 1 ? 1 : 0))); //If count is not even put the extra card on bottom
        return new CardPile(template, cards, true, true);

    }
}