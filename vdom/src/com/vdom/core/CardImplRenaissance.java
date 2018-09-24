package com.vdom.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.vdom.api.Card;
import com.vdom.api.GameEvent;
import com.vdom.core.MoveContext.TurnPhase;

public class CardImplRenaissance extends CardImpl {

	private static final long serialVersionUID = 1L;

	public CardImplRenaissance(CardImpl.Builder builder) {
		super(builder);
	}

	protected CardImplRenaissance() { }
	
	protected void additionalCardActions(Game game, MoveContext context, Player currentPlayer, boolean isThronedEffect) {
		switch(getKind()) {
		case Experiment:
			experiment(game, context, currentPlayer);
			break;
		case MountainVillage:
			mountainVillage(game, context, currentPlayer);
			break;
		case Priest:
			priest(game, context, currentPlayer);
			break;
		case Scholar:
			scholar(game, context, currentPlayer);
			break;
		case Seer:
			seer(game, context, currentPlayer);
			break;
		default:
			break;
		}
	}
	
	public void isTrashed(MoveContext context) {
		Cards.Kind trashKind = this.getKind();
		Player player = context.player;
    	if (this.getControlCard().equals(Cards.estate) && context.player.getInheritance() != null) {
    		trashKind = context.player.getInheritance().getKind();
    	}
    	
    	switch (trashKind) {
        
        default:
        	break;
    	}
    	
    	// card left play - stop any impersonations
	    this.getControlCard().stopImpersonatingCard();
	    this.getControlCard().stopInheritingCardAbilities();
	}
	
	private void experiment(Game game, MoveContext context, Player currentPlayer) {
		Card card = this.getControlCard();
    	int idx = currentPlayer.playedCards.indexOf(card.getId());
    	if (idx == -1) return;
    	card = currentPlayer.playedCards.remove(idx); 
    	CardPile pile = game.getGamePile(card);
        if (card.equals(Cards.estate) && currentPlayer.getInheritance() != null) {
            ((CardImpl)card).stopInheritingCardAbilities();
        }
        pile.addCard(card);	
	}
	
	private void mountainVillage(Game game, MoveContext context, Player currentPlayer) {
		if (currentPlayer.getDiscardSize() > 0)
        {
            Card card = currentPlayer.controlPlayer.mountainVillage_cardToPutInHand(context);

            if (card != null) {
            	int idx = currentPlayer.discard.indexOf(card);
                if (idx >= 0) {
                	card = currentPlayer.discard.remove(idx);
                	currentPlayer.hand.add(card);
                	GameEvent event = new GameEvent(GameEvent.EventType.CardAddedToHand, context);
                    game.broadcastEvent(event);
                } else {
                	Util.playerError(currentPlayer, "MountainVillage card not in discard, ignoring.");
                	game.drawToHand(context, this, 0);
                }
            }
        } else {
        	game.drawToHand(context, this, 0);
        }
	}
	
	private void priest(Game game, MoveContext context, Player player) {
		CardList hand = player.getHand();
		if(hand.size() > 0) {
	        Card trashCard = player.controlPlayer.priest_cardToTrash(context);
	        if (trashCard == null || !player.hand.contains(trashCard)) {
	            Util.playerError(player, "Priest card to trash invalid, picking one");
	            trashCard = hand.get(0);
	        }
	        player.trashFromHand(trashCard, this.getControlCard(), context);
		}
		context.coinsWhenTrash += 2;
	}
	
	private void scholar(Game game, MoveContext context, Player player) {
		if (player.getHand().size() > 0) {
            while (!player.getHand().isEmpty()) {
                player.discard(player.getHand().remove(0), this.getControlCard(), context);
            }
        }
		for (int i = 0; i < 7; ++i) {
			game.drawToHand(context, this.getControlCard(), 7 - i);
		}		
	}
	
	private void seer(Game game, MoveContext context, Player player) {
		ArrayList<Card> cards = new ArrayList<Card>();
        for (int i = 0; i < 3; i++) {
            Card card = context.game.draw(context, Cards.seer, 3 - i);
            if (card == null) {
                break;
            }
            cards.add(card);
        }

        if (cards.size() == 0) {
            return;
        }
        
        ArrayList<Card> topOfTheDeck = new ArrayList<Card>();
        for (Card c : cards) {
        	topOfTheDeck.add(c);
        	player.reveal(c, this.getControlCard(), context);
        }
        
        for (Card c : cards) {
        	int coinCost = c.getCost(context);
	        if (coinCost >= 2 && coinCost <=4 && c.getDebtCost(context) == 0 && !c.costPotion()) {
	        	player.hand.add(c);
	        	GameEvent event = new GameEvent(GameEvent.EventType.CardAddedToHand, context);
                game.broadcastEvent(event);
	        	topOfTheDeck.remove(c);
	        }
        }
        
        if (topOfTheDeck.size() > 0) {
            Card[] order;

            if(topOfTheDeck.size() == 1) {
                order = topOfTheDeck.toArray(new Card[topOfTheDeck.size()]);
            }
            else {
                order = player.controlPlayer.seer_cardOrder(context, topOfTheDeck.toArray(new Card[topOfTheDeck.size()]));
                // Check that they returned the right cards
                boolean bad = false;

                if (order == null) {
                    bad = true;
                } else {
                    ArrayList<Card> copy = new ArrayList<Card>();
                    for (Card card : topOfTheDeck) {
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
                    Util.playerError(player, "Seer order cards error, ignoring.");
                    order = topOfTheDeck.toArray(new Card[topOfTheDeck.size()]);
                }
            }

            // Put the cards back on the deck
            for (int i = order.length - 1; i >= 0; i--) {
            	player.putOnTopOfDeck(order[i]);
            }
        }  
	}
}
