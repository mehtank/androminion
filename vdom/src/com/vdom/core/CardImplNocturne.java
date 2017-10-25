package com.vdom.core;

import com.vdom.api.Card;

public class CardImplNocturne extends CardImpl {

	private static final long serialVersionUID = 1L;

	public CardImplNocturne(CardImpl.Builder builder) {
		super(builder);
	}

	protected CardImplNocturne() { }
	
	protected void additionalCardActions(Game game, MoveContext context, Player currentPlayer) {
		switch(getKind()) {
		case CursedGold:
            cursedGold(game, context, currentPlayer);
            break;
		case Pooka:
			pooka(game, context, currentPlayer);
			break;
		case Shepherd:
			shepherd(game, context, currentPlayer);
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
        case HauntedMirror:
        	boolean hasAction = false;
    		for (Card c : player.getHand()) {
    			if (c.is(Type.Action, player)) {
    				hasAction = true;
    				break;
    			}
    		}
    		if (!hasAction) break;
    		Card toDiscard = player.controlPlayer.hauntedMirror_cardToDiscard(context);
    		if (toDiscard != null && (!player.getHand().contains(toDiscard) || toDiscard.is(Type.Action, player))) {
    			Util.playerError(player, "Haunted Mirror - invalid card specified, ignoring.");
    			toDiscard = null;
    		}
    		if (toDiscard == null) break;
    		player.discard(player.getHand().remove(player.getHand().indexOf(toDiscard)), Cards.hauntedMirror, context);
    		player.gainNewCard(Cards.ghost, Cards.hauntedMirror, context);
            break;
        default:
        	break;
    	}
    	
    	// card left play - stop any impersonations
	    this.getControlCard().stopImpersonatingCard();
	    this.getControlCard().stopInheritingCardAbilities();
	}
	
	private void cursedGold(Game game, MoveContext context, Player player) {
        context.getPlayer().gainNewCard(Cards.curse, this.getControlCard(), context);
    }
	
	
	
	private void pooka(Game game, MoveContext context, Player player) {
		if(player.hand.size() > 0) {
            boolean hasValidTreasure = false;
            for (Card c : player.hand) {
                if (c.is(Type.Treasure, player) && !c.equals(Cards.cursedGold)) {
                    hasValidTreasure = true;
                    break;
                }
            }
            if (hasValidTreasure) {
                Card card = player.controlPlayer.pooka_treasureToTrash(context);
                if (card == null)
                	return;
                if (!card.is(Type.Treasure, player) || card.equals(Cards.cursedGold) || !player.hand.contains(card)) {
                    Util.playerError(player, "Pooka card to trash invalid, ignoring");
                    return;
                }

                player.hand.remove(card);
                player.trash(card, this.getControlCard(), context);
                for (int i = 0; i < 4; ++i) {
                	game.drawToHand(context, this, 4 - i);
                }
            }
        }
	}
	
	private void shepherd(Game game, MoveContext context, Player currentPlayer) {
        Card[] cards = currentPlayer.controlPlayer.shepherd_cardsToDiscard(context);
        for(Card card : cards) {
        	if (!card.is(Type.Victory, currentPlayer)) {
        		Util.playerError(currentPlayer, "Shepherd choice error, trying to discard non-victory cards, ignoring.");
        		cards = null;
        	}
        }
        if (cards != null) {
            int numberOfCards = 0;
            for (Card card : cards) {
                for (int i = 0; i < currentPlayer.hand.size(); i++) {
                    Card playersCard = currentPlayer.hand.get(i);
                    if (playersCard.equals(card)) {
                    	currentPlayer.reveal(playersCard, this.getControlCard(), context);
                        currentPlayer.discard(currentPlayer.hand.remove(i), this.getControlCard(), context);
                        numberOfCards++;
                        break;
                    }
                }
            }

            if (numberOfCards != cards.length) {
                Util.playerError(currentPlayer, "Shepherd discard error, trying to discard cards not in hand, ignoring extra.");
            }
            
            int numToDraw = 2 * numberOfCards;
            for (int i = 0; i < numToDraw; ++i) {
            	game.drawToHand(context, this, numToDraw - i);
            }
        }
    }
}
