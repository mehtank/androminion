package com.vdom.core;

import java.util.ArrayList;

import com.vdom.api.Card;
import com.vdom.api.EventCard;
import com.vdom.api.TreasureCard;

public class EventCardImpl extends CardImpl implements EventCard {
    protected int addBuys;

	protected EventCardImpl(Builder builder) {
        super(builder);
        addBuys = builder.addBuys;
    }

    public static class Builder extends CardImpl.Builder {
        protected int addBuys;
        public Builder(Cards.Type type, int cost) {
            super(type, cost);
        }

        public Builder addBuys(int val) {
            addBuys = val;
            return this;
        }
        
        public CardImpl build() {
            return new EventCardImpl(this);
        }
    }

    public int getAddBuys() {
        return addBuys;
    }

    @Override
    public CardImpl instantiate() {
        checkInstantiateOK();
        EventCardImpl c = new EventCardImpl();
        copyValues(c);
        return c;
    }

    protected void copyValues(EventCardImpl c) {
        super.copyValues(c);
        c.addBuys = addBuys;
    }

    protected EventCardImpl() {
    }
    
    @Override
    public void isBuying(MoveContext context) {
        context.buys += addBuys;
        switch (this.controlCard.getType()) {
	        case Alms:
	        	alms(context);
                break;
	        case Bonfire:
	        	bonfire(context);
                break;
	        case Borrow:
	        	borrow(context);
                break;
	        case Expedition:
	        	context.totalExpeditionBoughtThisTurn += 2;
                break;
	        case Raid:
	        	raid(context);
                break;
	        case Save:
	        	save(context);
                break;
            case ScoutingParty:
            	scoutingParty(context);
                break;
            default:
                break;
        }
    }

    public void alms(MoveContext context) {
    	boolean noTreasureCard = true;
        for(Card card : context.player.playedCards) {
            if (card instanceof TreasureCard) {
            	noTreasureCard = false;
            	break;
            }
        }
        if (noTreasureCard) {
	        Card card = context.player.controlPlayer.workshop_cardToObtain(context); /*frr18 todo right name*/
	        if (card != null) {
	            if (card.getCost(context) <= 4) {
	            	context.player.gainNewCard(card, this.controlCard, context);
	            }
	        }
        }
        context.cantBuy.add(this); //once per turn
    }
    
    private void bonfire(MoveContext context) {
        Card[] cards = context.player.controlPlayer.bonfire_cardsToTrash(context);
        if (cards != null) {
            if (cards.length > 2) {
                Util.playerError(context.player, "Bonfire trash error, trying to trash too many cards, ignoring.");
            } else {
                for (Card card : cards) {
                    for (int i = 0; i < context.player.playedCards.size(); i++) {
                        Card playedCard = context.player.playedCards.get(i);
                        if (playedCard.equals(card)) {
                            context.player.trash(context.player.playedCards.remove(i, false), this.controlCard, context);
                            break;
                        }
                    }
                }
            }
        }
    }
    
    protected void borrow(MoveContext context) {
    	if (!context.player.getMinusOneCardToken()) {
    		context.player.setMinusOneCardToken(true, context);
    		context.addCoins(1);
    	}
        context.cantBuy.add(this); //once per turn
    }    
    
    protected void raid(MoveContext context) {
        for(Card card : context.player.playedCards) {
            if (card.equals(Cards.silver)) {
                context.player.gainNewCard(Cards.silver, this, context);
            }
        }
        for (Player targetPlayer : context.game.getPlayersInTurnOrder()) {
            if (targetPlayer != context.player) {
                MoveContext targetContext = new MoveContext(context.game, targetPlayer);
            	targetPlayer.setMinusOneCardToken(true, targetContext);
            }
        }
    }    
    
    private void save(MoveContext context) {
        Card card = context.player.controlPlayer.haven_cardToSetAside(context); /*frr18 todo right name*/
        if ((card == null && context.player.hand.size() > 0) || (card != null && !context.player.hand.contains(card))) {
            Util.playerError(context.player, "Save set aside card error, setting aside the first card in hand.");
            card = context.player.hand.get(0);
        }

        if (card != null) {
        	context.player.hand.remove(card);
        	context.player.save = card;
        }
        context.cantBuy.add(this); //once per turn
    }
    
    private void scoutingParty(MoveContext context) {
        ArrayList<Card> cards = new ArrayList<Card>();
        for (int i = 0; i < 5; i++) {
            Card card = context.game.draw(context.player);
            if (card != null) {
                cards.add(card);
            }
        }

        if (cards.size() == 0) {
            return;
        }

        for (int i = 0; i < 3; i++) {
        	if(cards.size() > 0) {
	        	Card toDiscard = null;
	        	if(cards.size() > 3-i) {
	        		toDiscard = context.player.scoutingParty_cardToDiscard(context, cards.toArray(new Card[cards.size()]));
		        } else {
		            toDiscard = cards.get(0);
		        }
	        	
		        if (toDiscard == null || !cards.contains(toDiscard)) {
		            Util.playerError(context.player, "ScoutingParty discard error, just picking the first card.");
		            toDiscard = cards.get(0);
		        }
		
		        context.player.discard(toDiscard, this.controlCard, context);
		
		        cards.remove(toDiscard);
        	}
        }

        if (cards.size() > 0) {
        	Card[] order = context.player.controlPlayer.survivors_cardOrder(context, cards.toArray(new Card[cards.size()]));

	        // Check that they returned the right cards
	        boolean bad = false;
	
	        if (order == null) {
	            bad = true;
	        } else {
	            ArrayList<Card> copy = new ArrayList<Card>();
	            for (Card card : cards) {
	                copy.add(card);
	            }
	
	            for (Card card : order) {
	                if (!copy.remove(card)) {
	                    bad = true;
	                    break;
	                }
	            }
	
	            if (!copy.isEmpty()) {
	                bad = true;
	            }
	        }
	
	        if (bad) {
	            Util.playerError(context.player, "Survivors order cards error, ignoring.");
	            order = cards.toArray(new Card[cards.size()]);
	        }
	
	        // Put the cards back on the deck
	        for (int i = order.length - 1; i >= 0; i--) {
	        	context.player.putOnTopOfDeck(order[i]);
	        }
        }        
    }
    
    
    
}
