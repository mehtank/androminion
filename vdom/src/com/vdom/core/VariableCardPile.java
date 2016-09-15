package com.vdom.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.vdom.api.Card;

public class VariableCardPile extends AbstractCardPile {

	public VariableCardPile(Card placeholder, Map<Card, Integer> cardList, boolean ordered, boolean allCardsVisible) {
		this.cards = new ArrayList<Card>();
		this.templateCards = new ArrayList<Card>();

		this.placeholderCard = placeholder.instantiate();
		this.placeholderCard.setPlaceholderCard();

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

	@Override
	public Card topCard() {
		if (cards.isEmpty()) return null;
		return cards.get(0);
	}

}
