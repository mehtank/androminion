package com.vdom.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vdom.api.Card;

public class CardPile  {
	protected Card placeholderCard = null;
	protected ArrayList<Card> cards;
	protected ArrayList<Card> templateCards;

	protected boolean allCardsVisible = true;
	protected boolean isSupply = true;

	protected boolean isBlackMarket = false;
	protected boolean tradeRouteToken = false;

	public static class CardMultiplicity {
		public Card card;
		public int count;
		CardMultiplicity(Card card, int count) { this.card = card; this.count = count; }
	}

	public CardPile(Card placeholder, List<CardMultiplicity> cardList, boolean ordered, boolean allCardsVisible) {
		this.cards = new ArrayList<Card>();
		this.templateCards = new ArrayList<Card>();

		this.placeholderCard = placeholder.instantiate();
		this.placeholderCard.setPlaceholderCard();

		this.allCardsVisible = allCardsVisible;

		for (CardMultiplicity entry : cardList)
		{
			if (!templateCards.contains(entry.card)) {
				templateCards.add(entry.card);
			}
			for (int i = 0; i < entry.count; i++) {
				cards.add(entry.card.instantiate());
			}
		}

		if (!ordered) {
			Collections.shuffle(cards);
		}
	}

	public Card topCard() {
		if (cards.isEmpty()) return null;
		return cards.get(0);
	}

	public Card placeholderCard() {
		return placeholderCard;
	}

	public ArrayList<Card> getTemplateCards() {
		return templateCards;
	}

	public int getCount() {
		return cards.size();
	}

	public boolean isEmpty() {
		return cards.isEmpty();
	}

	public void addCard(Card card) {
		if (cardAllowedOnPile(card)) {
			cards.add(0, card);
		}
	}

	public Card removeCard() {
		if (cards.isEmpty()) {
			return null;
		}
		return cards.remove(0);
	}

	public boolean areAllCardsVisible() { return this.allCardsVisible; }

	public boolean cardAllowedOnPile(Card card) {
		if (card.isTemplateCard()) return false; //No template card allowed on the pile

		for (Card template : this.templateCards) {
			if (template.equals(card)) {
				return true;
			}
		}
		return false;
	}

	public CardPile notInSupply() {
		this.isSupply = false;
		return this;
	}

	public CardPile inBlackMarket() {
		this.isBlackMarket = true;
		return this;
	}

	public boolean isSupply() {
		return this.isSupply;
	}

	public boolean isBlackMarket() {
		return this.isBlackMarket;
	}

	public void setTradeRouteToken() {
		tradeRouteToken = true;
	}

	public boolean hasTradeRouteToken() {
		return tradeRouteToken;
	}

	public int takeTradeRouteToken() {
		if (tradeRouteToken) {
			tradeRouteToken = false;
			return 1;
		}
		return 0;
	}

	@Override
	public String toString() {
		return cards.toString();
	}

}
