package com.vdom.core;

import java.util.ArrayList;
import java.util.List;

import com.vdom.api.Card;

public enum Expansion {
	Base,
	Base2E,
	BaseAll(true),
	Intrigue,
	Intrigue2E,
	IntrigueAll(true),
	Seaside,
	Alchemy,
	Prosperity,
	Cornucopia,
	Hinterlands,
	DarkAges,
	Guilds,
	Adventures,
	Empires,
	Nocturne,
	Renaissance,
	Promo;
	
	private List<Card> kingdomCards = new ArrayList<Card>(0);
	private List<Card> eventCards = new ArrayList<Card>(0);
	private List<Card> projectCards = new ArrayList<Card>(0);
	private List<Card> landmarkCards = new ArrayList<Card>(0);
	private final boolean isAggregate;

	private Expansion() {
		isAggregate = false;
	}
	
	private Expansion(boolean isAggregate) {
		this.isAggregate = isAggregate;
	}
	
	public List<Card> getKingdomCards() {
		return kingdomCards;
	}
	
	public void setKingdomCards(List<Card> kingdomCards) {
		this.kingdomCards = kingdomCards;
	}

	public void setEventCards(List<Card> eventCards) {
		this.eventCards = eventCards;
	}

	public void setProjectCards(List<Card> projectCards) {
		this.projectCards = projectCards;
	}

	public void setLandmarkCards(List<Card> landmarkCards) {
		this.landmarkCards = landmarkCards;
	}

	public List<Card> getEventCards() {
		return eventCards;
	}
	
	public List<Card> getProjectCards() {
		return projectCards;
	}
	
	public List<Card> getLandmarkCards() {
		return landmarkCards;
	}
	
	public boolean isAggregate() {
		return isAggregate;
	}
}
