package com.vdom.core;

import java.util.ArrayList;
import java.util.Collections;

import com.vdom.api.ActionCard;
import com.vdom.api.Card;

public class VariableCardPile extends AbstractCardPile {

	public VariableCardPile(PileType piletype) {
		this.type = piletype;
		this.cards = new ArrayList<Card>();

		// TODO: put in checks to make sure template card is never
        // "put into play".
		switch (this.type) {
		case KnightsPile:
			for (Card card : this.generateKnightsPile()) {
	            CardImpl thisCard = ((CardImpl) card).instantiate();
	            cards.add(thisCard);
			}
			break;
		case RuinsPile:
			for (Card card : this.generateRuinsPile(Game.numPlayers)) {
	            CardImpl thisCard = ((CardImpl) card).instantiate();
	            cards.add(thisCard);
			}
			break;
		default:
			break;
		}
	}

	@Override
	public Card card() {
		return cards.get(0);
	}

	@Override
	public void addCard(Card card) {
		ActionCard ac = null;
		if (card instanceof ActionCard) {
			ac = (ActionCard) card;
		} else return;

		switch (type) {
		case KnightsPile:
			if (ac.isKnight()) {
				cards.add(0, card);
			}
			break;
		case RuinsPile:
			if (ac.isRuins()) {
				cards.add(0, card);
			}
			break;
		default:
			break;
		}
		
	}

	@Override
	public Card removeCard() {
		return cards.remove(0);
	}
	
	private ArrayList<Card> generateKnightsPile() {
		return null;
	}
	
    private ArrayList<Card> generateRuinsPile(int players) {
    	ArrayList<Card> ruins = new ArrayList<Card>();
    	ArrayList<Card> ret = new ArrayList<Card>();
    	int size = Math.min(50, (players * 10) - 10); // 
    	
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
    		if (ret.size() >= size) break;
    	}
    	
    	return ret;
    }


}
