package com.vdom.core;

import java.util.ArrayList;

import com.vdom.api.Card;
import com.vdom.api.GameEvent;

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
		case TheEarthsGift:
			theEarthsGift(game, context, currentPlayer);
			break;
		case TheFlamesGift:
			theFlamesGift(game, context, currentPlayer);
			break;
		case TheMoonsGift:
			theMoonsGift(game, context, currentPlayer);
			break;
		case TheMountainsGift:
			theMountainsGift(game, context, currentPlayer);
			break;
		case TheRiversGift:
			theRiversGift(game, context, currentPlayer);
			break;
		case TheSkysGift:
			theSkysGift(game, context, currentPlayer);
			break;
		case TheSunsGift:
			theSunsGift(game, context, currentPlayer);
			break;
		case TheSwampsGift:
			theSwampsGift(game, context, currentPlayer);
			break;
		case TheWindsGift:
			discardMultiple(context, currentPlayer, 2);
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
	
	private void theEarthsGift(Game game, MoveContext context, Player currentPlayer) {
		boolean hasTreasure = false;
        for(Card c : currentPlayer.hand) {
            if(c.is(Type.Treasure, currentPlayer)) {
                hasTreasure = true;
            }
        }
        if (!hasTreasure)
        	return;

        Card toDiscard = currentPlayer.controlPlayer.theEarthsGift_treasureToDiscard(context);
        if (toDiscard == null || !currentPlayer.hand.contains(toDiscard) || !toDiscard.is(Type.Treasure, currentPlayer))
        	return;
        
        currentPlayer.hand.remove(toDiscard);
        currentPlayer.discard(toDiscard, this.getControlCard(), context);
        
        Card card = currentPlayer.controlPlayer.theEarthsGift_cardToObtain(context);
        if (card != null) {
            if (card.getCost(context) <= 4 && card.getDebtCost(context) == 0 && !card.costPotion()) {
                currentPlayer.gainNewCard(card, this.getControlCard(), context);
            }
        }		
	}
	
	private void theFlamesGift(Game game, MoveContext context, Player player) {
		CardList hand = player.getHand();
		if (hand.isEmpty())
			return;
		
        Card cardToTrash = player.controlPlayer.theFlamesGift_cardToTrash(context);
        if (cardToTrash == null)
        	return;
        if (!hand.contains(cardToTrash)) {
    		Util.playerError(player, "The Flame's gift error, invalid card to trash, ignoring.");
    	} else {
    		cardToTrash = hand.get(cardToTrash);
    		hand.remove(cardToTrash);
    		player.trash(cardToTrash, Cards.theFlamesGift, context);
    	}
	}
	
	private void theMoonsGift(Game game, MoveContext context, Player player) {
		if (player.getDiscardSize() == 0)
			return;
        Card card = player.controlPlayer.theMoonsGift_cardToPutBackOnDeck(context);

        if (card != null) {
        	int idx = player.discard.indexOf(card);
            if (idx >= 0) {
            	card = player.discard.remove(idx);
            	player.putOnTopOfDeck(card);
            	GameEvent event = new GameEvent(GameEvent.EventType.CardOnTopOfDeck, context);
                game.broadcastEvent(event);
            } else {
            	Util.playerError(player, "The Moon's Gift card not in discard, ignoring.");
            }
        }
	}
	
	private void theMountainsGift(Game game, MoveContext context, Player player) {
		player.gainNewCard(Cards.silver, this.getControlCard(), context);
	}
	
	private void theRiversGift(Game game, MoveContext context, Player player) {
		player.theRiversGiftDraw += 1;
	}
	
	private void theSkysGift(Game game, MoveContext context, Player player) {
		if (player.getHand().size() == 0) {
            return;
        }
		
        Card[] cardsToDiscard = player.controlPlayer.theSkysGift_cardsToDiscard(context);
        if (cardsToDiscard == null || !(cardsToDiscard.length == 3 || (cardsToDiscard.length > 0 && cardsToDiscard.length < 3 && player.getHand().size() == cardsToDiscard.length))) {
            return;
        }

        ArrayList<Card> copy = Util.copy(player.hand);
        for (Card cardToKeep : cardsToDiscard) {
            if (!copy.remove(cardToKeep)) {
                return;
            }
        }

        for (Card card : cardsToDiscard) {
        	player.discard(card, this.getControlCard(), context);
        	player.hand.remove(card);
        }
        if (cardsToDiscard.length == 3) {
            player.gainNewCard(Cards.gold, this.getControlCard(), context);
        }
	}
	
	private void theSunsGift(Game game, MoveContext context, Player player)
	{
		ArrayList<Card> topOfTheDeck = new ArrayList<Card>();
        for (int i = 0; i < 4; i++) {
            Card card = game.draw(context, Cards.theSunsGift, 4 - i);
            if (card != null) {
                topOfTheDeck.add(card);
            }
        }

        if (topOfTheDeck.size() > 0) {
            Card[] cardsToDiscard = player.controlPlayer.theSunsGift_cardsFromTopOfDeckToDiscard(context, topOfTheDeck.toArray(new Card[topOfTheDeck.size()]));
            if(cardsToDiscard != null) {
                for(Card toDiscard : cardsToDiscard) {
                    if(topOfTheDeck.remove(toDiscard)) {
                    	player.discard(toDiscard, this.getControlCard(), null);
                    }
                    else {
                        Util.playerError(player, "The Sun's Gift returned invalid card to discard, ignoring");
                    }
                }
            }
            if (topOfTheDeck.size() > 0) {
                Card[] order;

                if(topOfTheDeck.size() == 1) {
                    order = topOfTheDeck.toArray(new Card[topOfTheDeck.size()]);
                }
                else {
                    order = player.controlPlayer.theSunsGift_cardOrder(context, topOfTheDeck.toArray(new Card[topOfTheDeck.size()]));
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
                        Util.playerError(player, "The Sun's Gift order cards error, ignoring.");
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
	
	private void theSwampsGift(Game game, MoveContext context, Player player) {
		player.gainNewCard(Cards.willOWisp, this.getControlCard(), context);
	}
}
