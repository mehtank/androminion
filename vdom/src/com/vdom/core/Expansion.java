package com.vdom.core;

import java.util.ArrayList;
import java.util.List;

import com.vdom.api.Card;

public enum Expansion {
	Base(Cards.actionCardsBaseGame),
	Base2E(Cards.actionCardsBaseGame2E),
	BaseAll(Cards.actionCardsBaseGameAll, true),
	Intrigue(Cards.actionCardsIntrigue),
	Intrigue2E(Cards.actionCardsIntrigue2E),
	IntrigueAll(Cards.actionCardsIntrigueAll, true),
	Seaside(Cards.actionCardsSeaside),
	Alchemy(Cards.actionCardsAlchemy),
	Prosperity(Cards.actionCardsProsperity),
	Cornucopia(Cards.actionCardsCornucopia),
	Hinterlands(Cards.actionCardsHinterlands),
	DarkAges(Cards.actionCardsDarkAges),
	Guilds(Cards.actionCardsGuilds),
	Adventures(Cards.actionCardsAdventures, Cards.eventCardsAdventures),
	Empires(Cards.actionCardsEmpires, Cards.eventCardsEmpires, Cards.landmarkCardsEmpires),
	Promo(Cards.actionCardsPromo, Cards.eventCardsPromo);
	
	private final List<Card> kingdomCards;
	private final List<Card> eventCards;
	private final List<Card> landmarkCards;
	private final boolean isAggregate;

	private Expansion(List<Card> kingdomCards) {
		this(kingdomCards, new ArrayList<Card>(0));
	}
	
	private Expansion(List<Card> kingdomCards, boolean isAggregate) {
		this(kingdomCards, new ArrayList<Card>(0), new ArrayList<Card>(0), isAggregate);
	}

	private Expansion(List<Card> kingdomCards, List<Card> eventCards) {
		this(kingdomCards, eventCards, new ArrayList<Card>(0), false);
	}
	
	private Expansion(List<Card> kingdomCards, List<Card> eventCards, List<Card> landmarkCards) {
		this(kingdomCards, eventCards, landmarkCards, false);
	}
	
	private Expansion(List<Card> kingdomCards, List<Card> eventCards, List<Card> landmarkCards, boolean isAggregate) {
		this.kingdomCards = kingdomCards;
		this.eventCards = eventCards;
		this.landmarkCards = landmarkCards;
		this.isAggregate = isAggregate;
	}

	public List<Card> getKingdomCards() {
		return kingdomCards;
	}
	
	public List<Card> getEventCards() {
		return eventCards;
	}
	
	public List<Card> getLandmarkCards() {
		return landmarkCards;
	}
	
	public boolean isAggregate() {
		return isAggregate;
	}
}
