package com.vdom.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.vdom.api.Card;

public class VariableCardPile extends AbstractCardPile {

	private Card placeholderCard = null;

	public VariableCardPile(Card placeholder, Map<Card, Integer> cardList, boolean ordered, boolean allCardsVisible) {
		this.cards = new ArrayList<Card>();
		this.templateCards = new ArrayList<Card>();
		for (Map.Entry<Card, Integer> entry : cardList.entrySet())
		{
			Card card = entry.getKey();
			Integer count = entry.getValue();
			if (!templateCards.contains(card)) {
				templateCards.add(card);
			}
			for (int i = 0; i < count; i++) {
				cards.add(card.instantiate());
			}
		}

		if (!ordered) {
			Collections.shuffle(cards);
		}
	}

	@Override
	public Card removeCard() {
		if (cards.isEmpty()) {
			return null;
		}
		return cards.remove(0);
	}
	
    private ArrayList<Card> generateRuinsPile(int count) {
    	ArrayList<Card> ruins = new ArrayList<Card>();
    	ArrayList<Card> ret = new ArrayList<Card>();
    	
    	for (int i = 0; i < 10; i++) {
    		ruins.add(Cards.abandonedMine);
    		ruins.add(Cards.ruinedLibrary);
    		ruins.add(Cards.ruinedMarket);
    		ruins.add(Cards.ruinedVillage);
    		ruins.add(Cards.survivors);
    	}
    	
    	Collections.shuffle(ruins);
    	
    	for (Card c : ruins) {
    		ret.add(c);
    		if (ret.size() >= count) break;
    	}
    	
    	return ret;
    }

	@Override
	public Card topCard() {
		if (cards.isEmpty()) return null;
		return cards.get(0);
	}

	@Override
	public Card placeholderCard() {
		return placeholderCard;
	}
}
