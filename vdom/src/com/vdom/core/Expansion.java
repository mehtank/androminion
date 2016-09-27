package com.vdom.core;

import java.util.List;

import com.vdom.api.Card;

public enum Expansion {
	Base(Cards.actionCardsBaseGame),
	Base2E(Cards.actionCardsBaseGame2E),
	BaseAll(Cards.actionCardsBaseGameAll),
	Intrigue(Cards.actionCardsIntrigue),
	Intrigue2E(Cards.actionCardsIntrigue2E),
	IntrigueAll(Cards.actionCardsIntrigueAll),
	Seaside(Cards.actionCardsSeaside),
	Alchemy(Cards.actionCardsAlchemy),
	Prosperity(Cards.actionCardsProsperity),
	Cornucopia(Cards.actionCardsCornucopia),
	Hinterlands(Cards.actionCardsHinterlands),
	DarkAges(Cards.actionCardsDarkAges),
	Guilds(Cards.actionCardsGuilds),
	Adventures(Cards.actionCardsAdventures, Cards.eventCardsAdventures),
	Empires(Cards.actionCardsEmpires, Cards.eventCardsEmpires),
	Promo(Cards.actionCardsPromo, Cards.eventCardsPromo);
	
	private List<Card> kingdomCards;
	private List<Card> eventCards;

	private Expansion(List<Card> kingdomCards) {
		this(kingdomCards, null);
	}

	private Expansion(List<Card> kingdomCards, List<Card> eventCards) {
		this.kingdomCards = kingdomCards;
		this.eventCards = eventCards;
	}

	public List<Card> getKingdomCards() {
		return kingdomCards;
	}
	
	public List<Card> getEventCards() {
		return eventCards;
	}
}
