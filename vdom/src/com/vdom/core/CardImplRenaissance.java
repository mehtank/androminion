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
		case ActingTroupe:
			actingTroupe(game, context, currentPlayer);
			break;
		case Cathedral:
			cathedral(game, context, currentPlayer);
			break;
		case CityGate:
			cityGate(game, context, currentPlayer);
			break;
		case CropRotation:
			cropRotation(game, context, currentPlayer);
			break;
		case Experiment:
			experiment(game, context, currentPlayer);
			break;
		case Hideout:
			hideout(game, context, currentPlayer);
			break;
		case Inventor:
			inventor(game, context, currentPlayer);
			break;
		case Key:
			context.addCoins(1);
			break;
		case MountainVillage:
			mountainVillage(game, context, currentPlayer);
			break;
		case OldWitch:
			oldWitch(game, context, currentPlayer);
			break;
		case Piazza:
			piazza(game, context, currentPlayer);
			break;
		case Priest:
			priest(game, context, currentPlayer);
			break;
		case Recruiter:
			recruiter(game, context, currentPlayer);
			break;
		case Scholar:
			scholar(game, context, currentPlayer);
			break;
		case Sculptor:
			sculptor(game, context, currentPlayer);
			break;
		case Seer:
			seer(game, context, currentPlayer);
			break;
		case Sewers:
			sewers(game, context, currentPlayer);
			break;
		case Silos:
			silos(game, context, currentPlayer);
			break;
		case Swashbuckler:
			swashbuckler(game, context, currentPlayer);
			break;
		case Treasurer:
			treasurer(game, context, currentPlayer);
			break;
		case Villain:
			villain(game, context, currentPlayer);
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
    	case SilkMerchant:
    		player.gainGuildsCoinTokens(1, context, Cards.silkMerchant);
    		player.takeVillagers(1, context, Cards.silkMerchant);
    		break;
    	case FlagBearer:
    		context.game.takeSharedState(context, Cards.flag);
    		break;
        default:
        	break;
    	}
    	
    	// card left play - stop any impersonations
	    this.getControlCard().stopImpersonatingCard();
	    this.getControlCard().stopInheritingCardAbilities();
	}
		
	private void actingTroupe(Game game, MoveContext context, Player player) {
		player.trashSelfFromPlay(getControlCard(), context);
	}
	
	private void cathedral(Game game, MoveContext context, Player player) {
		if (player.getHand().size() == 0) {
			return;
		}
        Card cardToTrash = player.getHand().size() == 1 ? player.getHand().get(0) : player.controlPlayer.cathedral_cardToTrash(context);
        if (cardToTrash != null) {
        	if (!player.getHand().contains(cardToTrash)) {
        		Util.playerError(player, "Cathedral error, invalid card to trash, trashing random card.");
        		cardToTrash = Util.randomCard(player.getHand());
        	} else {
        		player.trashFromHand(cardToTrash, this.getControlCard(), context);
        	}
        }
	}
	
	private void cityGate(Game game, MoveContext context, Player player) {
		game.drawToHand(context, this.getControlCard(), 1);
		if (player.getHand().size() == 0) 
			return;
		Card card = player.getHand().size() == 1 ? player.getHand().get(0) : player.controlPlayer.cityGate_cardToPutBackOnDeck(context);
		if (card == null || !player.hand.contains(card)) {
			Util.playerError(player, "City Gate put back card error, putting back first card");
			card = player.hand.get(0);
		}
		player.putOnTopOfDeck(player.hand.removeCard(card));
		GameEvent topDeckEvent = new GameEvent(GameEvent.EventType.CardOnTopOfDeck, context);
		topDeckEvent.card = card;
		topDeckEvent.setPlayer(player);
		topDeckEvent.setPrivate(true);
		game.broadcastEvent(topDeckEvent);
	}
	
	private void cropRotation(Game game, MoveContext context, Player player) {
		int numVictories = 0;
		for (Card c : player.getHand()) {
			if (c.is(Type.Victory, player))
				numVictories++;
		}
		if (numVictories == 0)
			return;
		
        Card card = player.controlPlayer.cropRotation_cardToDiscard(context);
        if (card == null)
        	return;
        
    	if (!card.is(Type.Victory, player)) {
    		Util.playerError(player, "Crop Rotation choice error, trying to discard non-victory card, ignoring.");
    		card = null;
    	}
        if (card != null) {
            int numberOfCards = 0;
            for (int i = 0; i < player.hand.size(); i++) {
                Card playersCard = player.hand.get(i);
                if (playersCard.equals(card)) {                	
                	player.discard(player.hand.remove(i), this.getControlCard(), context);
                    numberOfCards++;
                    break;
                }
            }

            if (numberOfCards != 1) {
                Util.playerError(player, "Crop Rotation discard error, trying to discard card not in hand, ignoring.");
            }
            
            int numToDraw = 2 * numberOfCards;
            for (int i = 0; i < numToDraw; ++i) {
            	game.drawToHand(context, Cards.cropRotation, numToDraw - i);
            }
        }
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
	
	private void hideout(Game game, MoveContext context, Player player) {
		if (player.getHand().size() == 0)
			return;
        Card cardToTrash = player.controlPlayer.hideout_cardToTrash(context);
        if (cardToTrash != null) {
        	if (!player.getHand().contains(cardToTrash)) {
        		Util.playerError(player, "Hideout error, invalid card to trash, picking first.");
        		cardToTrash = player.getHand().get(0);
        	} 
        	cardToTrash = player.hand.get(cardToTrash);
    		player.trashFromHand(cardToTrash, this.getControlCard(), context);
    		
    		if (cardToTrash.is(Type.Victory)) {
    			context.getPlayer().gainNewCard(Cards.curse, this.getControlCard(), context);
    		}
        }
	}
	
	private void inventor(Game game, MoveContext context, Player player) {
		Card card = player.controlPlayer.inventor_cardToObtain(context);
        if (card != null) {
            if (card.getCost(context) <= 4 && card.getDebtCost(context) == 0 && !card.costPotion()) {
            	player.gainNewCard(card, this.getControlCard(), context);
            } else {
            	Util.playerError(player, "Inventor error, invalid card to gain, ignoring");
            }
        }
        context.cardCostModifier -= 1;
	}
	
	private void mountainVillage(Game game, MoveContext context, Player currentPlayer) {
		if (currentPlayer.getDiscardSize() > 0)
        {
			if (currentPlayer.getDiscardSize() == 1) {
            	currentPlayer.hand.add(currentPlayer.discard.remove(0));
            	return;
			}
			
            Card card = currentPlayer.controlPlayer.mountainVillage_cardToPutInHand(context);

            if (card != null) {
            	int idx = currentPlayer.discard.indexOf(card);
                if (idx >= 0) {
                	card = currentPlayer.discard.remove(idx);
                	currentPlayer.hand.add(card);
                	GameEvent event = new GameEvent(GameEvent.EventType.CardAddedToHand, context);
                    game.broadcastEvent(event);
                } else {
                	Util.playerError(currentPlayer, "Mountain Village card not in discard, picking one.");
                	card = currentPlayer.discard.remove(0);
                	currentPlayer.hand.add(card);
                }
            }
        } else {
        	game.drawToHand(context, this, 0);
        }
	}
	
	private void oldWitch(Game game, MoveContext context, Player currentPlayer) {
		ArrayList<Player> attackedPlayers = new ArrayList<Player>();
    	for (Player player : context.game.getPlayersInTurnOrder()) {
            if (player != currentPlayer && !Util.isDefendedFromAttack(context.game, player, this)) {
            	attackedPlayers.add(player);
            }
    	}
		
    	for (Player player : attackedPlayers) {
			player.attacked(this.getControlCard(), context);
            MoveContext playerContext = new MoveContext(game, player);
            playerContext.attackedPlayer = player;
            player.gainNewCard(Cards.curse, this.getControlCard(), playerContext);
            if (player.getHand().contains(Cards.curse)) {
            	if (player.controlPlayer.oldWitch_shouldTrashCurse(playerContext)) {
            		player.trashFromHand(Cards.curse, this.getControlCard(), playerContext);
            	}
            }
        }
	}
	
	private void piazza(Game game, MoveContext context, Player player) {
		Card c = game.draw(context, Cards.piazza, 1);
        if (c != null) {
        	player.reveal(c, this.getControlCard(), context);
            if (c.is(Type.Action, player)) {
            	context.freeActionInEffect++;
                c.play(game, context, false);
                context.freeActionInEffect--;
            } else {
            	player.putOnTopOfDeck(c, context, true);
            }
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
	
	private void recruiter(Game game, MoveContext context, Player player) {
		if (player.hand.size() == 0) {
            return;
        }

        Card card = player.controlPlayer.recruiter_cardToTrash(context);

        if (card == null || !player.hand.contains(card)) {
            Util.playerError(player, "Recruiter trash error, trashing first card.");
            card = player.hand.get(0);
        }

        player.trashFromHand(card, this.getControlCard(), context);
        int cost = card.getCost(context);
        player.takeVillagers(cost, context, Cards.recruiter);
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
	
	private void sculptor(Game game, MoveContext context, Player player) {
		Card cardToGain = player.controlPlayer.sculptor_cardToObtain(context);
        if (cardToGain != null) {
            if (cardToGain.getCost(context) <= 4 && cardToGain.getDebtCost(context) == 0 && !cardToGain.costPotion()) {
            	Card cardGained = player.gainNewCard(cardToGain, getControlCard(), context);
            	if (cardGained != null && cardGained.is(Type.Treasure, player)) {
            		player.takeVillagers(1, context, Cards.sculptor);
            	}
            } else {
            	Util.playerError(player, "Sculptor error, invalid card to gain, ignoring");
            }
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
	
	private void sewers(Game game, MoveContext context, Player player) {
		CardList hand = player.getHand();
		Card cardToTrash = player.controlPlayer.sewers_cardToTrash(context);
        if (cardToTrash == null)
        	return;
        if (!hand.contains(cardToTrash)) {
    		Util.playerError(player, "Sewers error, invalid card to trash, ignoring.");
    	} else {
    		cardToTrash = hand.get(cardToTrash);
    		player.trashFromHand(cardToTrash, getControlCard(), context);
    	}
	}
	
	private void silos(Game game, MoveContext context, Player player) {
		int numCoppers = 0;
		ArrayList<Card> coppers = new ArrayList<Card>();
		for (Card c : player.getHand()) {
			if (c.equals(Cards.copper)) {
				numCoppers++;
				coppers.add(c);
			}
		}
		if (numCoppers == 0) return;
		
		int numCoppersToDiscard = player.controlPlayer.silos_numCoppersToDiscard(context, numCoppers);
		if (numCoppersToDiscard < 0  || numCoppersToDiscard > numCoppers) {
			Util.playerError(player, "Silos error, trying to discard invalid number of Copper cards, ignoring.");
			return;
		}
		if (numCoppersToDiscard == 0) return;
		
		int numDiscarded = 0;
		for (Card c : coppers) {
			if (!player.hand.remove(c)) break;
			player.reveal(c, this.getControlCard(), context);
			player.discard(c, this.getControlCard(), context);
			numDiscarded++;
			if (numDiscarded == numCoppersToDiscard)
				break;
		}
                    
        for (int i = 0; i < numDiscarded; ++i) {
        	game.drawToHand(context, this.getControlCard(), numDiscarded - i);
        }
	}
	
	private void swashbuckler(Game game, MoveContext context, Player player) {
		if (player.getDiscardSize() == 0)
			return;
		player.gainGuildsCoinTokens(1, context, this);
		if (player.getGuildsCoinTokenCount() >= 4) {
			game.takeSharedState(context, Cards.treasureChest);
		}
	}
	
	private void treasurer(Game game, MoveContext context, Player player) {
		switch (player.controlPlayer.treasurer_chooseOption(context)) {
		case TrashTreasure: {
			Card firstTreasure = null;
			for (Card c : player.hand) {
                if (c.is(Type.Treasure, player)) {
                	firstTreasure = c;
                    break;
                }
            }
			if (firstTreasure == null) return;
			Card card = player.controlPlayer.treasurer_treasureToTrash(context);
            if (card == null || !card.is(Type.Treasure, player) || !player.hand.contains(card)) {
                Util.playerError(player, "Treasurer card to trash invalid, choosing one.");
                card = firstTreasure;
                return;
            }

            player.trashFromHand(card, this.getControlCard(), context);
			break;
		}
		case GainTreasureFromTrash:
			Card firstTreasure = null;
			for (Card c : game.GetTrashPile()) {
                if (c.is(Type.Treasure, player)) {
                	firstTreasure = c;
                    break;
                }
            }
			if (firstTreasure == null) return;
			Card card = player.controlPlayer.treasurer_treasureToGainFromTrash(context);
            if (card == null || !card.is(Type.Treasure, player) || !game.GetTrashPile().contains(card)) {
                Util.playerError(player, "Treasurer card to gain invalid, choosing one.");
                card = firstTreasure;
                return;
            }
            card = game.trashPile.remove(game.trashPile.indexOf(card));
            player.gainCardAlreadyInPlay(card, this.getControlCard(), context);
			break;
		case TakeKey:
			game.takeSharedState(context, Cards.key);
			break;
		}
	}
	
	private void villain(Game game, MoveContext context, Player currentPlayer) {
		ArrayList<Player> attackedPlayers = new ArrayList<Player>();
    	for (Player player : context.game.getPlayersInTurnOrder()) {
            if (player != currentPlayer && !Util.isDefendedFromAttack(context.game, player, this)) {
            	attackedPlayers.add(player);
            }
    	}
    	        
    	for (Player player : attackedPlayers) {
    		if (player.hand.size() <= 4) {
    			continue;
    		}
    		MoveContext playerContext = new MoveContext(game, player);
            playerContext.attackedPlayer = player;
            player.attacked(this.getControlCard(), context);
            
            ArrayList<Card> discardCards = new ArrayList<Card>();
            for (Card card : player.hand) {
            	if (card.getCost(playerContext) >= 2) {
            		discardCards.add(card);
            	}
            }
            if (discardCards.size() == 0) {
            	for (Card card : player.hand){
            		player.reveal(card, getControlCard(), playerContext);
            	}
            	return;
            }
            if (discardCards.size() == 1) {
            	int idx = player.hand.indexOf(discardCards.get(0));
        		player.discard(player.hand.remove(idx), this.getControlCard(), context);
            	return;
            }
            Card toDiscard = player.controlPlayer.villain_cardToDiscard(playerContext, discardCards.toArray(new Card[0]));
            if (toDiscard == null || !discardCards.contains(toDiscard)) {
            	Util.playerError(player, "Villain discard error, invalid card, chosing first");
            	toDiscard = discardCards.get(0);
            }
            int idx = player.hand.indexOf(toDiscard);
    		player.discard(player.hand.remove(idx), this.getControlCard(), context);
    	}
	}
}
