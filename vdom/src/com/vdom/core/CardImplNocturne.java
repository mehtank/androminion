package com.vdom.core;

import java.util.ArrayList;
import java.util.List;

import com.vdom.api.Card;
import com.vdom.api.GameEvent;
import com.vdom.core.MoveContext.TurnPhase;

public class CardImplNocturne extends CardImpl {

	private static final long serialVersionUID = 1L;

	public CardImplNocturne(CardImpl.Builder builder) {
		super(builder);
	}

	protected CardImplNocturne() { }
	
	protected void additionalCardActions(Game game, MoveContext context, Player currentPlayer) {
		switch(getKind()) {
		case BadOmens:
			badOmens(game, context, currentPlayer);
			break;
		case Bat:
			bat(game, context, currentPlayer);
			break;
		case CursedGold:
            cursedGold(game, context, currentPlayer);
            break;
		case CursedVillage:
            cursedVillage(game, context, currentPlayer);
            break;
		case DevilsWorkshop:
            devilsWorkshop(game, context, currentPlayer);
            break;
		case Exorcist:
			exorcist(game, context, currentPlayer);
			break;
		case Goat:
			goat(game, context, currentPlayer);
			break;
		case Idol:
			idol(game, context, currentPlayer);
			break;
		case Imp:
			imp(game, context, currentPlayer);
			break;
		case Leprechaun:
			leprechaun(game, context, currentPlayer);
			break;
		case LuckyCoin:
			luckyCoin(game, context, currentPlayer);
			break;
		case Pixie:
			pixie(game, context, currentPlayer);
			break;
		case Plague:
			plague(game, context, currentPlayer);
			break;
		case Pooka:
			pooka(game, context, currentPlayer);
			break;
		case Shepherd:
			shepherd(game, context, currentPlayer);
			break;
		case Skulk:
			skulk(game, context, currentPlayer);
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
		case Vampire:
			vampire(game, context, currentPlayer);
			break;
		case Werewolf:
			werewolf(game, context, currentPlayer);
			break;
		case Wish:
			wish(game, context, currentPlayer);
			break;
		case WillOWisp:
			willOWisp(game, context, currentPlayer);
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
	
	private void badOmens(Game game, MoveContext context, Player player) {
		GameEvent event = new GameEvent(GameEvent.EventType.DeckPutIntoDiscardPile, (MoveContext) context);
        game.broadcastEvent(event);
        while (player.getDeckSize() > 0)
        {
        	player.discard(game.draw(context, Cards.badOmens, 0), this.getControlCard(), null, false, false);
        }
        ArrayList<Card> coppers = new ArrayList<Card>();
        for (Card c : player.getDiscard()) {
        	if (c.equals(Cards.copper)) {
        		coppers.add(c);
        		if (coppers.size() == 2)
        			break;
        	}
        }
        for (Card c : coppers) {
        	player.discard.remove(c);
        	player.putOnTopOfDeck(c);
            
            event = new GameEvent(GameEvent.EventType.CardOnTopOfDeck, context);
            event.card = c;
            game.broadcastEvent(event);
        }
    }
	
	private void bat(Game game, MoveContext context, Player player) {
		Card[] cards = context.player.controlPlayer.bat_cardsToTrash(context);
		if (cards == null || cards.length == 0) return;
		if (cards.length > 2 || !Util.areCardsInHand(cards, context)) {
			Util.playerError(context.player, "Bat trash error, trying to trash invalid cards, ignoring.");
			return;
		}
		for (Card card : cards) {
			for (int i = 0; i < context.player.hand.size(); i++) {
				Card inHand = context.player.hand.get(i);
				if (inHand.equals(card)) {
					context.player.trash(context.player.hand.remove(i, false), this.getControlCard(), context);
					break;
				}
			}
		}
		// exchange for Vampire
    	if (!context.isCardOnTop(Cards.vampire))
    		return;
    	CardPile pile = game.getPile(this);
        pile.addCard(player.playedCards.remove(player.playedCards.indexOf(this.getId())));
        player.discard.add(game.takeFromPile(Cards.vampire));
        GameEvent event = new GameEvent(GameEvent.EventType.TravellerExchanged, context);
		event.card = Cards.vampire;
		event.responsible = this;
		event.setPlayer(player);
        context.game.broadcastEvent(event);
    }
	
	private void cursedGold(Game game, MoveContext context, Player player) {
        context.getPlayer().gainNewCard(Cards.curse, this.getControlCard(), context);
    }
	
	private void cursedVillage(Game game, MoveContext context, Player player) {
		int cardsToDraw = 6 - player.hand.size();
    	if (cardsToDraw > 0 && player.getMinusOneCardToken()) {
        	game.drawToHand(context, this, -1);
        }
    	for (int i = 0; i < cardsToDraw; ++i) {
    		if(!game.drawToHand(context, this, cardsToDraw - i))
                break;
    	}
	}
	
	private void devilsWorkshop(Game game, MoveContext context, Player player) {
        int numGained = context.getNumCardsGainedThisTurn();
        if (numGained == 0) {
        	context.getPlayer().gainNewCard(Cards.gold, this.getControlCard(), context);
        } else if (numGained == 1) {
        	Card card = player.controlPlayer.devilsWorkshop_cardToObtain(context);
            if (card != null) {
                if (card.getCost(context) <= 4 && card.getDebtCost(context) == 0 && !card.costPotion()) {
                	player.gainNewCard(card, this.getControlCard(), context);
                } else {
                	Util.playerError(player, "Devil's Workshop error, invalid card to gain, ignoring");
                }
            }
        } else {
        	context.getPlayer().gainNewCard(Cards.imp, this.getControlCard(), context);
        }
    }
	
	private void exorcist(Game game, MoveContext context, Player player) {
		if (player.getHand().size() == 0)
			return;
        Card cardToTrash = player.controlPlayer.exorcist_cardToTrash(context);
        if (cardToTrash != null) {
        	if (!player.getHand().contains(cardToTrash)) {
        		Util.playerError(player, "Exorcist error, invalid card to trash, ignoring.");
        	} else {
        		cardToTrash = player.hand.get(cardToTrash);
        		player.hand.remove(cardToTrash);
        		player.trash(cardToTrash, this.getControlCard(), context);
        		
        		//Gain a cheaper Spirit card
        		int cost = cardToTrash.getCost(context);
        		int debt = cardToTrash.getDebtCost(context);
        		boolean potion = cardToTrash.costPotion();
        		int potionCost = potion ? 1 : 0;
        		List<Card> validCards = new ArrayList<Card>();
        		for (Card card : game.getCardsInGame(GetCardsInGameOptions.TopOfPiles, false)) {
                    if (card.is(Type.Spirit)) { //TODO?: also check if pile is a Spirit pile (doesn't matter yet)
                        int gainCardCost = card.getCost(context);
                        int gainCardPotionCost = card.costPotion() ? 1 : 0;
                        int gainCardDebt = card.getDebtCost(context);

                        if ((gainCardCost < cost || gainCardDebt < debt || gainCardPotionCost < potionCost) && 
                        		(gainCardCost <= cost && gainCardDebt <= debt && gainCardPotionCost <= potionCost)) {
                            validCards.add(card);
                        }
                    }
                }
        		
        		if (validCards.size() > 0) {
                    Card toGain = context.getPlayer().controlPlayer.exorcist_cardToObtain(context, cost, debt, potion);
                    if (toGain == null || !validCards.contains(toGain)) {
                        Util.playerError(context.getPlayer(), "Invalid card returned from Exorcist, choosing one.");
                        toGain = validCards.get(0);
                    }
                	context.getPlayer().gainNewCard(toGain, this.getControlCard(), context);
                }
        	}
        }
	}
      
	
	private void goat(Game game, MoveContext context, Player player) {
		if (player.getHand().size() == 0)
			return;
        Card cardToTrash = player.controlPlayer.goat_cardToTrash(context);
        if (cardToTrash != null) {
        	if (!player.getHand().contains(cardToTrash)) {
        		Util.playerError(player, "Goat error, invalid card to trash, ignoring.");
        	} else {
        		cardToTrash = player.hand.get(cardToTrash);
        		player.hand.remove(cardToTrash);
        		player.trash(cardToTrash, this.getControlCard(), context);
        	}
        }
    }
	
	private void idol(Game game, MoveContext context, Player currentPlayer) {
		ArrayList<Player> attackedPlayers = new ArrayList<Player>();
    	for (Player player : context.game.getPlayersInTurnOrder()) {
            if (player != currentPlayer && !Util.isDefendedFromAttack(context.game, player, this)) {
            	attackedPlayers.add(player);
            }
    	}
		
        boolean isNumIdolsOdd = context.countCardsInPlayByName(Cards.idol) % 2 == 1;
        if (isNumIdolsOdd) {
        	game.receiveNextBoon(context, this.getControlCard());
        } else {
        	for (Player player : attackedPlayers) {
				player.attacked(this.getControlCard(), context);
	            MoveContext playerContext = new MoveContext(game, player);
	            playerContext.attackedPlayer = player;
	            player.gainNewCard(Cards.curse, this.getControlCard(), playerContext);
	        }
        }
    }
	
	private void imp(Game game, MoveContext context, Player player) {
		ArrayList<Card> validCards = new ArrayList<Card>();
		for (Card c : player.hand) {
			if (c.is(Type.Action, player)) {
				if (!player.hasCopyInPlay(c)) {
					validCards.add(c);
				};
			}
		}
		if (validCards.isEmpty()) return;
		Card card = player.controlPlayer.imp_cardToPlay(context);
		if (card == null) return;
		if (!validCards.contains(card)) {
			Util.playerError(player, "Imp error, invalid card selected, ignoring");
			return;
		}
		context.freeActionInEffect++;
        card.play(game, context, true);
        context.freeActionInEffect--;
    }
	
	private void leprechaun(Game game, MoveContext context, Player player) {
		player.gainNewCard(Cards.gold, this.getControlCard(), context);
		if (context.countCardsInPlay() == 7) {
			player.gainNewCard(Cards.wish, this.getControlCard(), context);
		} else {
			game.receiveNextHex(context, getControlCard());
		}
	}
	
	private void luckyCoin(Game game, MoveContext context, Player player) {
		player.gainNewCard(Cards.silver, this.getControlCard(), context);
	}
	
	private void pixie(Game game, MoveContext context, Player player) {
		Card topBoon = game.discardNextBoon(context, this);
		if (!this.getControlCard().movedToNextTurnPile) {
			if (player.controlPlayer.pixie_shouldTrashPixie(context, topBoon, getControlCard())) {
				this.getControlCard().movedToNextTurnPile = true;
				player.trash(this.getControlCard(), null, context);
                player.playedCards.remove(player.playedCards.lastIndexOf(this.getControlCard()));
				game.recieveBoon(context, topBoon, getControlCard());
				game.recieveBoon(context, topBoon, getControlCard());
			}
        }
	}
	
	private void plague(Game game, MoveContext context, Player player) {
		player.gainNewCard(Cards.curse, this.getControlCard(), context);
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
	
	private void skulk(Game game, MoveContext context, Player currentPlayer) {
		ArrayList<Player> attackedPlayers = new ArrayList<Player>();
    	for (Player player : context.game.getPlayersInTurnOrder()) {
            if (player != currentPlayer && !Util.isDefendedFromAttack(context.game, player, this)) {
            	attackedPlayers.add(player);
            }
    	}
        game.othersReceiveNextHex(context, attackedPlayers, this.getControlCard());
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
	
	private void vampire(Game game, MoveContext context, Player currentPlayer) {
		ArrayList<Player> attackedPlayers = new ArrayList<Player>();
    	for (Player player : context.game.getPlayersInTurnOrder()) {
            if (player != currentPlayer && !Util.isDefendedFromAttack(context.game, player, this)) {
            	attackedPlayers.add(player);
            }
    	}
    	game.othersReceiveNextHex(context, attackedPlayers, this.getControlCard());
    	
    	Card card = currentPlayer.controlPlayer.vampire_cardToObtain(context);
        if (card != null) {
            if (card.getCost(context) <= 5 && card.getDebtCost(context) == 0 && !card.costPotion() && !card.equals(Cards.vampire)) {
                currentPlayer.gainNewCard(card, this.getControlCard(), context);
            } else {
            	Util.playerError(currentPlayer, "Vampire error, invalid card to gain, ignoring");
            }
        }
        // exchange for bat
    	if (!context.isCardOnTop(Cards.bat))
    		return;
    	CardPile pile = game.getPile(this);
        pile.addCard(currentPlayer.playedCards.remove(currentPlayer.playedCards.indexOf(this.getId())));
        currentPlayer.discard.add(game.takeFromPile(Cards.bat));
        GameEvent event = new GameEvent(GameEvent.EventType.TravellerExchanged, context);
		event.card = Cards.bat;
		event.responsible = this;
		event.setPlayer(currentPlayer);
        context.game.broadcastEvent(event);
	}
	
	private void werewolf(Game game, MoveContext context, Player currentPlayer) {
		ArrayList<Player> attackedPlayers = new ArrayList<Player>();
    	for (Player player : context.game.getPlayersInTurnOrder()) {
            if (player != currentPlayer && !Util.isDefendedFromAttack(context.game, player, this)) {
            	attackedPlayers.add(player);
            }
    	}
		if (context.phase == TurnPhase.Night) {
			game.othersReceiveNextHex(context, attackedPlayers, this.getControlCard());	
		} else {
			for (int i = 0; i < 3; ++i) {
            	game.drawToHand(context, this, 3 - i);
            }
		}
	}
	
	private void wish(Game game, MoveContext context, Player player) {
		if (isInPlay(player)) {
            CardPile pile = game.getPile(this);
            pile.addCard(player.playedCards.remove(player.playedCards.indexOf(this.getId())));
            Card card = player.controlPlayer.wish_cardToObtain(context);
            if (card != null) {
            	pile = game.getPile(card);
                if (pile.isSupply() && !pile.isEmpty() && card.getCost(context) <= 6 && card.getDebtCost(context) == 0 && !card.costPotion()) {
                	player.gainNewCard(card, this.getControlCard(), context);
                } else {
                	Util.playerError(player, "Wish error, ignoring");
                }
            }
    	}
	}
	
	private void willOWisp(Game game, MoveContext context, Player player) {
		Card c = game.draw(context, Cards.willOWisp, 1);
        if (c != null) {
        	player.reveal(c, this.getControlCard(), context);
            if (c.getCost(context) <= 2 && c.getDebtCost(context) == 0 && !c.costPotion()) {
            	player.hand.add(c);
            } else {
            	player.putOnTopOfDeck(c, context, true);
            }
        }
	}
}
