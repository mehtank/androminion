package com.vdom.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.vdom.api.Card;
import com.vdom.api.GameEvent;
import com.vdom.core.Player.SentryOption;

public class CardImplBase extends CardImpl {
	private static final long serialVersionUID = 1L;

	public CardImplBase(CardImpl.Builder builder) {
		super(builder);
	}

	protected CardImplBase() { }

	@Override
	protected void additionalCardActions(Game game, MoveContext context, Player currentPlayer) {
		switch (this.getKind()) {
			case Adventurer:
	            adventurer(game, context, currentPlayer);
	            break;
			case Artisan:
				artisan(game, context, currentPlayer);
				break;
			case Bandit:
				bandit(game, context, currentPlayer);
				break;
			case Bureaucrat:
                bureaucrat(game, context, currentPlayer);
                break;
            case Cellar:
                cellar(game, context, currentPlayer);
                break;
			case Chancellor:
	            chancellor(game, context, currentPlayer);
	            break;
			case Chapel:
				chapel(context, currentPlayer);
				break;
            case CouncilRoom:
                councilRoom(game, context);
                break;
			case Feast:
				feast(context, currentPlayer);
				break;
			case Harbinger:
				harbinger(game, context, currentPlayer);
				break;
			case Library:
                library(game, context, currentPlayer);
                break;
			case Merchant:
				merchant(game, context, currentPlayer);
				break;
			case Militia:
                militia(game, context, currentPlayer);
                break;
			case Mine:
                mine(context, currentPlayer);
                break;
			case Moneylender:
	            moneyLender(context, currentPlayer);
	            break;
			case Poacher:
				poacher(game, context, currentPlayer);
				break;
			case Remodel:
                remodel(context, currentPlayer);
                break;
			case Sentry:
				sentry(game, context, currentPlayer);
				break;
			case Spy:
                spyAndScryingPool(game, context, currentPlayer);
                break;
			case Thief:
				thief(game, context, currentPlayer);
				break;
            case ThroneRoom:
                throneRoomKingsCourt(game, context, currentPlayer);
                break;
            case Vassal:
            	vassal(game, context, currentPlayer);
            	break;
            case Witch:
                witchFamiliar(game, context, currentPlayer);
                break;
            case Workshop:
	            workshop(currentPlayer, context);
	            break;
		default:
			break;
		}
	}
	
	private void adventurer(Game game, MoveContext context, Player currentPlayer) {
        ArrayList<Card> toDiscard = new ArrayList<Card>();
        int treasureCardsRevealed = 0;

        while (treasureCardsRevealed < 2) {
            Card draw = game.draw(context, Cards.adventurer, -1);
            if (draw == null) {
                break;
            }
            currentPlayer.reveal(draw, this.getControlCard(), context);

            if (draw.is(Type.Treasure, currentPlayer)) {
                treasureCardsRevealed++;
                currentPlayer.hand.add(draw);
            } else {
                toDiscard.add(draw);
            }
        }

        while (!toDiscard.isEmpty()) {
            currentPlayer.discard(toDiscard.remove(0), this.getControlCard(), context);
        }
    }
	
	private void artisan(Game game, MoveContext context, Player currentPlayer) {
		Card card = currentPlayer.controlPlayer.artisan_cardToObtain(context);
        if (card != null) {
            if (card.getCost(context) <= 5 && card.getDebtCost(context) == 0 && !card.costPotion()) {
                currentPlayer.gainNewCard(card, this.getControlCard(), context);
            }
        }
        CardList hand = currentPlayer.getHand();
        if(hand.size() > 0) {
            Card toTopOfDeck = currentPlayer.controlPlayer.artisan_cardToReplace(context);
            if (toTopOfDeck == null || !hand.contains(toTopOfDeck)) {
                Util.playerError(currentPlayer, "No valid card selected for Artisan, returning random card to the top of the deck.");
                toTopOfDeck = Util.randomCard(hand);
            }
            for (int i = 0; i < hand.size(); ++i) {
            	Card c = hand.get(i);
            	if (c.equals(toTopOfDeck)) {
            		hand.remove(i);
            		currentPlayer.putOnTopOfDeck(c);
            		GameEvent event = new GameEvent(GameEvent.EventType.CardOnTopOfDeck, context);
                    game.broadcastEvent(event);
            		break;
            	}
            }
        }
	}
	
	private void bandit(Game game, MoveContext context, Player currentPlayer) {
		ArrayList<Player> attackedPlayers = new ArrayList<Player>();
		for (Player targetPlayer : game.getPlayersInTurnOrder()) {
            if (targetPlayer != currentPlayer && !Util.isDefendedFromAttack(game, targetPlayer, this)) {
            	attackedPlayers.add(targetPlayer);
            }
        }
		currentPlayer.gainNewCard(Cards.gold, this, context);
		for(Player targetPlayer : attackedPlayers) {
			targetPlayer.attacked(this.getControlCard(), context);
			MoveContext targetContext = new MoveContext(game, targetPlayer);
            targetContext.attackedPlayer = targetPlayer;
            ArrayList<Card> treasures = new ArrayList<Card>();
            List<Card> cardsToDiscard = new ArrayList<Card>();
            for (int i = 0; i < 2; i++) {
                Card card = game.draw(targetContext, Cards.bandit, 2 - i);
                if (card != null) {
                    targetPlayer.reveal(card, this.getControlCard(), targetContext);
                    if (card.is(Type.Treasure, targetPlayer) && !Cards.copper.equals(card)) {
                        treasures.add(card);
                    } else {
                        cardsToDiscard.add(card);
                    }
                }
            }
            
            Card cardToTrash = null;
            if (treasures.size() == 1) {
                cardToTrash = treasures.get(0);
            } else if (treasures.size() == 2) {
                if (treasures.get(0).equals(treasures.get(1))) {
                    cardToTrash = treasures.get(0);
                    cardsToDiscard.add(treasures.remove(1));
                } else {
                    cardToTrash = targetPlayer.controlPlayer.bandit_treasureToTrash(context, treasures.toArray(new Card[]{}));
                    cardToTrash = treasures.get(0).equals(cardToTrash) ? treasures.get(0) : treasures.get(1);
                    cardsToDiscard.add(treasures.get(0).equals(cardToTrash) ? treasures.get(1) : treasures.get(0));
                }
            }
            if (cardToTrash != null) {
                targetPlayer.trash(cardToTrash, this.getControlCard(), targetContext);
            }
            for (Card c: cardsToDiscard) {
                targetPlayer.discard(c, this.getControlCard(), targetContext);
            }
        }
	}
	
	private void bureaucrat(Game game, MoveContext context, Player currentPlayer) {
        currentPlayer.gainNewCard(Cards.silver, this, context);

        for (Player player : game.getPlayersInTurnOrder()) {
            if (player != currentPlayer && !Util.isDefendedFromAttack(game, player, this)) {
                player.attacked(this.getControlCard(), context);
                MoveContext playerContext = new MoveContext(game, player);
                playerContext.attackedPlayer = player;

                ArrayList<Card> victoryCards = new ArrayList<Card>();

                for (Card card : player.hand) {
                    if (card.is(Type.Victory, player)) {
                        victoryCards.add(card);
                    }
                }

                if (victoryCards.size() == 0) {
                    for (int i = 0; i < player.hand.size(); i++) {
                        Card card = player.hand.get(i);
                        player.reveal(card, this.getControlCard(), playerContext);
                    }
                } else {
                    Card toTopOfDeck = null;

                    if (victoryCards.size() == 1) {
                        toTopOfDeck = victoryCards.get(0);
                    } else if (Collections.frequency(victoryCards, victoryCards.get(0)) ==
                                victoryCards.size() /*all the same*/) {
                        toTopOfDeck = victoryCards.get(0);
                    } else {
                        toTopOfDeck = (player).controlPlayer.bureaucrat_cardToReplace(playerContext);

                        if (toTopOfDeck == null || !toTopOfDeck.is(Type.Victory, player)) {
                            Util.playerError(player, "No Victory Card selected for Bureaucrat, using first Victory Card in hand");
                            toTopOfDeck = victoryCards.get(0);
                        }
                    }

                    player.reveal(toTopOfDeck, this.getControlCard(), playerContext);
                    player.hand.remove(toTopOfDeck);
                    player.putOnTopOfDeck(toTopOfDeck);
                }
            }
        }
    }

	private void cellar(Game game, MoveContext context, Player currentPlayer) {
        Card[] cards = currentPlayer.controlPlayer.cellar_cardsToDiscard(context);
        if (cards != null) {
            int numberOfCards = 0;
            for (Card card : cards) {
                for (int i = 0; i < currentPlayer.hand.size(); i++) {
                    Card playersCard = currentPlayer.hand.get(i);
                    if (playersCard.equals(card)) {
                        currentPlayer.discard(currentPlayer.hand.remove(i), this.getControlCard(), context);
                        numberOfCards++;
                        break;
                    }
                }
            }

            if (numberOfCards != cards.length) {
                Util.playerError(currentPlayer, "Cellar discard error, trying to discard cards not in hand, ignoring extra.");
            }

            for (int i = 0; i < numberOfCards; ++i) {
            	game.drawToHand(context, this, numberOfCards - i);
            }
        }
    }
	
    private void chancellor(Game game, MoveContext context, Player currentPlayer) {
    	if (currentPlayer.getDeckSize() == 0)
    		return;
        boolean discard = currentPlayer.controlPlayer.chancellor_shouldDiscardDeck(context);
        if (discard) {
            GameEvent event = new GameEvent(GameEvent.EventType.DeckPutIntoDiscardPile, (MoveContext) context);
            game.broadcastEvent(event);
            while (currentPlayer.getDeckSize() > 0) {
                currentPlayer.discard(game.draw(context, Cards.chancellor, 0), this.getControlCard(), null, false, false);
            }
        }
    }
	
	private void chapel(MoveContext context, Player currentPlayer) {
        Card[] cards = currentPlayer.controlPlayer.chapel_cardsToTrash(context);
        if (cards != null) {
            if (cards.length > 4) {
                Util.playerError(currentPlayer, "Chapel trash error, trying to trash too many cards, ignoring.");
            } else {
                for (Card card : cards) {
                    for (int i = 0; i < currentPlayer.hand.size(); i++) {
                        Card playersCard = currentPlayer.hand.get(i);
                        if (playersCard.equals(card)) {
                            Card thisCard = currentPlayer.hand.remove(i, false);
                            currentPlayer.trash(thisCard, this.getControlCard(), context);
                            break;
                        }
                    }
                }
            }
        }
    }
	
    private void councilRoom(Game game, MoveContext context) {
        for (Player player : game.getPlayersInTurnOrder()) {
            if (player != context.getPlayer()) {
                game.drawToHand(new MoveContext(game, player), this, 1);
            }
        }
    }
	
	private void feast(MoveContext context, Player currentPlayer) {
        Card card = currentPlayer.controlPlayer.feast_cardToObtain(context);
        if (card != null) {
            // check cost
            if (card.getCost(context) <= 5 && card.getDebtCost(context) == 0 && !card.costPotion()) {
                currentPlayer.gainNewCard(card, this.getControlCard(), context);
            }
        }
    }
	
	private void harbinger(Game game, MoveContext context, Player currentPlayer) {
		if (currentPlayer.getDiscardSize() > 0)
        {
            Card card = currentPlayer.controlPlayer.harbinger_cardToPutBackOnDeck(context);

            if (card != null) {
            	int idx = currentPlayer.discard.indexOf(card);
                if (idx >= 0) {
                	card = currentPlayer.discard.remove(idx);
                	currentPlayer.putOnTopOfDeck(card);
                	GameEvent event = new GameEvent(GameEvent.EventType.CardOnTopOfDeck, context);
                    game.broadcastEvent(event);
                } else {
                	Util.playerError(currentPlayer, "Harbinger card not in discard, ignoring.");
                }
            }
        }
	}

    private void library(Game game, MoveContext context, Player currentPlayer) {
        ArrayList<Card> toDiscard = new ArrayList<Card>();
        // only time a card is "drawn" without being directly drawn to hand
        //  we need to manually remove the minus one card token
        currentPlayer.setMinusOneCardToken(false, context);
        while (currentPlayer.hand.size() < 7) {
        	Card draw = game.draw(context, Cards.library, -1);
            if (draw == null) {
                break;
            }
            
            boolean shouldKeep = true;
            if (draw.is(Type.Action, currentPlayer)) {
                shouldKeep = currentPlayer.controlPlayer.library_shouldKeepAction(context, draw);
            }

            if (shouldKeep) {
                currentPlayer.hand.add(draw);
            } else {
                toDiscard.add(draw);
            }
        }

        while (!toDiscard.isEmpty()) {
            currentPlayer.discard(toDiscard.remove(0), this.getControlCard(), null);
        }
    }
    
    private void merchant(Game game, MoveContext context, Player currentPlayer) {
        context.merchantsPlayed++;
    }
    
    private void militia(Game game, MoveContext context, Player currentPlayer) {
        for (Player player : game.getPlayersInTurnOrder()) {
            if (player != currentPlayer && !Util.isDefendedFromAttack(game, player, this)) {
                player.attacked(this.getControlCard(), context);
                MoveContext playerContext = new MoveContext(game, player);
                playerContext.attackedPlayer = player;

                int keepCardCount = 3;
                if (player.hand.size() > keepCardCount) {
                    Card[] cardsToKeep = player.controlPlayer.militia_attack_cardsToKeep(playerContext);
                    player.discardRemainingCardsFromHand(playerContext, cardsToKeep, this.getControlCard(), keepCardCount);
                }
            }
        }
    }
    
    private void mine(MoveContext context, Player currentPlayer) {
        Card cardToUpgrade = currentPlayer.controlPlayer.mine_treasureFromHandToUpgrade(context);
        if ((Game.errataMineForced && cardToUpgrade == null) || !cardToUpgrade.is(Type.Treasure, currentPlayer)) {
            Card[] cards = currentPlayer.getTreasuresInHand().toArray(new Card[] {});
            if (cards.length != 0) {
                Util.playerError(currentPlayer, "Mine card to upgrade was invalid, picking treasure from hand.");
                cardToUpgrade = Util.randomCard(cards);
            }
        }

        if (cardToUpgrade != null) {
            CardList hand = currentPlayer.getHand();
            for (int i = 0; i < hand.size(); i++) {
                Card card = hand.get(i);

                if (cardToUpgrade.equals(card)) {
                    Card thisCard = currentPlayer.getHand().remove(i);
                    currentPlayer.trash(thisCard, this.getControlCard(), context);

                    Card newCard = currentPlayer.controlPlayer.mine_treasureToObtain(context, card.getCost(context) + 3, card.getDebtCost(context), card.costPotion());
                    if (!(newCard != null && newCard.is(Type.Treasure, null) && Cards.isSupplyCard(newCard) && 
                    		newCard.getCost(context) <= card.getCost(context) + 3 && 
                    		newCard.getDebtCost(context) <= card.getDebtCost(context) && 
                    		(!newCard.costPotion() || card.costPotion()) 
                    		&& context.isCardOnTop(newCard))) {
                        Util.playerError(currentPlayer, "Mine treasure to obtain was invalid, picking random treasure from table.");
                        for (Card treasureCard : context.getCardsInGame(GetCardsInGameOptions.TopOfPiles, true, Type.Treasure)) {
                            if (Cards.isSupplyCard(treasureCard) && context.isCardOnTop(treasureCard) &&
                            		treasureCard.getCost(context) <= card.getCost(context) + 3 &&
                            		treasureCard.getDebtCost(context) <= card.getCost(context) &&
                            		(!treasureCard.costPotion() || card.costPotion()))
                                newCard = treasureCard;
                        }
                    }

                    if (newCard != null && newCard.getCost(context) <= card.getCost(context) + 3 && Cards.isSupplyCard(newCard) && context.isCardOnTop(newCard))
                        currentPlayer.gainNewCard(newCard, this.getControlCard(), context);
                    break;
                }
            }
        }
    }

    private void moneyLender(MoveContext context, Player currentPlayer) {
        for (int i = 0; i < currentPlayer.hand.size(); i++) {
            Card card = currentPlayer.hand.get(i);
            if (card.equals(Cards.copper)) {
            	boolean choseTrash = false;
            	if (Game.errataMoneylenderForced || (choseTrash = currentPlayer.controlPlayer.moneylender_shouldTrashCopper(context))) {
	                Card thisCard = currentPlayer.hand.remove(i);
	                context.addCoins(3);
	                currentPlayer.trash(thisCard, this.getControlCard(), context);
	                break;
            	}
            	if (!choseTrash)
            		break;
            }
        }
    }
    
    private void poacher(Game game, MoveContext context, Player currentPlayer) {
    	if (currentPlayer.getHand().isEmpty()) return;
    	int numToDiscard = game.emptyPiles();
    	if (numToDiscard == 0) return;
    	CardList hand = currentPlayer.getHand(); 
    	Card[] cards = null;
    	if (hand.size() <= numToDiscard) {
    		cards = hand.toArray();
    	} else {
    		cards = currentPlayer.controlPlayer.poacher_cardsToDiscard(context, numToDiscard);
    		if (!(cards != null && cards.length == numToDiscard && Util.areCardsInHand(cards, context))) {
    			Util.playerError(currentPlayer, "Poacher discard error, picking first cards");
    			cards = new Card[numToDiscard];
    			for (int i = 0; i < numToDiscard; ++i) {
    				cards[i] = hand.get(i);
    			}
    		}
    	}
        for (Card card : cards) {
            for (int i = 0; i < currentPlayer.hand.size(); i++) {
                Card playersCard = currentPlayer.hand.get(i);
                if (playersCard.equals(card)) {
                    currentPlayer.discard(currentPlayer.hand.remove(i), this.getControlCard(), context);
                    break;
                }
            }
        }
    }
    
    private void remodel(MoveContext context, Player currentPlayer) {
        if(currentPlayer.getHand().size() > 0) {
            Card cardToTrash = currentPlayer.controlPlayer.remodel_cardToTrash(context);

            if (cardToTrash == null) {
                Util.playerError(currentPlayer, "Remodel did not return a card to trash, trashing random card.");
                cardToTrash = Util.randomCard(currentPlayer.getHand());
            }

            int cost = -1;
            int debt = -1;
            boolean potion = false;
            for (int i = 0; i < currentPlayer.hand.size(); i++) {
                Card playersCard = currentPlayer.hand.get(i);
                if (playersCard.equals(cardToTrash)) {
                    cost = playersCard.getCost(context);
                    debt = playersCard.getDebtCost(context);
                    potion = playersCard.costPotion();
                    playersCard = currentPlayer.hand.remove(i);

                    currentPlayer.trash(playersCard, this.getControlCard(), context);
                    break;
                }
            }

            if (cost == -1) {
                Util.playerError(currentPlayer, "Remodel returned invalid card, ignoring.");
                return;
            }

            cost += 2;

            Card card = currentPlayer.controlPlayer.remodel_cardToObtain(context, cost, debt, potion);
            if (card != null) {
                // check cost
                if (card.getCost(context) > cost) {
                    Util.playerError(currentPlayer, "Remodel new card costs too much, ignoring.");
                }
                else if (card.getDebtCost(context) > debt) {
                    Util.playerError(currentPlayer, "Remodel new card costs too much debt, ignoring.");
                } else if (card.costPotion() && !potion) {
                    Util.playerError(currentPlayer, "Remodel new card costs potion, ignoring.");
                } else {
                    if (currentPlayer.gainNewCard(card, this.getControlCard(), context) == null) {
                        Util.playerError(currentPlayer, "Remodel new card is invalid, ignoring.");
                    }
                }
            }
        }
    }
    
    private void sentry(Game game, MoveContext context, Player currentPlayer) {
    	ArrayList<Card> topOfTheDeck = new ArrayList<Card>();
        for (int i = 0; i < 2; i++) {
            Card card = game.draw(context, Cards.sentry, 2 - i);
            if (card != null) {
                topOfTheDeck.add(card);
            }
        }

        ArrayList<Card> toTrash = new ArrayList<Card>();
        ArrayList<Card> toDiscard = new ArrayList<Card>();
        ArrayList<Card> toReplace = new ArrayList<Card>();
        if (topOfTheDeck.size() > 0) {
        	for (Card c : topOfTheDeck) {
        		SentryOption option = currentPlayer.controlPlayer.sentry_chooseOption(context, c, topOfTheDeck.toArray(new Card[topOfTheDeck.size()]));
        		if (option == null) {
        			Util.playerError(currentPlayer, "Sentry chose null option - trashing card");
        			option = SentryOption.Trash;
        		}
        		switch (option) {
	        		case Trash:
	        			toTrash.add(c);
	        			break;
	        		case Discard:
	        			toDiscard.add(c);
	        			break;
	        		case PutBack:
	        			toReplace.add(c);
	        			break;
	        		default:
	        			toTrash.add(c);
	        			break;
        		}
        	}
        	for (Card c : toTrash) {
        		currentPlayer.trash(c, this.getControlCard(), context);
        	}
        	for (Card c : toDiscard) {
        		currentPlayer.discard(c, this.getControlCard(), context);
        	}
        	if (toReplace.size() > 0) {
	        	Card[] order;
	            if(toReplace.size() == 1) {
	                order = toReplace.toArray(new Card[toReplace.size()]);
	            } else {
	                order = currentPlayer.controlPlayer.sentry_cardOrder(context, toReplace.toArray(new Card[toReplace.size()]));
	                if (!Util.areCardsInList(order, toReplace)) {
	                	Util.playerError(currentPlayer, "Sentry order cards error, ignoring.");
	                	order = toReplace.toArray(new Card[toReplace.size()]);
	                }
	            }
	
	            // Put the cards back on the deck
	            for (int i = order.length - 1; i >= 0; i--) {
	                currentPlayer.putOnTopOfDeck(order[i]);
	            }
        	}
        }
    }
    
    private void thief(Game game, MoveContext context, Player currentPlayer) {
        ArrayList<Card> trashed = new ArrayList<Card>();

        for (Player targetPlayer : game.getPlayersInTurnOrder()) {
            if (targetPlayer != currentPlayer && !Util.isDefendedFromAttack(game, targetPlayer, this)) {
                targetPlayer.attacked(this.getControlCard(), context);
                MoveContext targetContext = new MoveContext(game, targetPlayer);
                targetContext.attackedPlayer = targetPlayer;
                ArrayList<Card> treasures = new ArrayList<Card>();

                List<Card> cardsToDiscard = new ArrayList<Card>();
                for (int i = 0; i < 2; i++) {
                    Card card = game.draw(targetContext, Cards.thief, 2 - i);

                    if (card != null) {
                        targetPlayer.reveal(card, this.getControlCard(), targetContext);

                        if (card.is(Type.Treasure, targetPlayer)) {
                            treasures.add(card);
                        } else {
                            cardsToDiscard.add(card);
                        }
                    }
                }

                for (Card c: cardsToDiscard) {
                    targetPlayer.discard(c, this.getControlCard(), targetContext);
                }

                Card cardToTrash = null;

                if (treasures.size() == 1) {
                    cardToTrash = treasures.get(0);
                } else if (treasures.size() == 2) {
                    if (treasures.get(0).equals(treasures.get(1))) {
                        cardToTrash = treasures.get(0);
                        targetPlayer.discard(treasures.remove(1), this.getControlCard(), targetContext);
                    } else {
                        cardToTrash = currentPlayer.controlPlayer.thief_treasureToTrash(context, treasures.toArray(new Card[] {}));
                    }

                    for (Card treasure : treasures) {
                        if (!treasure.equals(cardToTrash)) {
                            targetPlayer.discard(treasure, this.getControlCard(), targetContext);
                        }
                    }
                }

                if (cardToTrash != null) {
                    targetPlayer.trash(cardToTrash, this.getControlCard(), targetContext);
                    trashed.add(cardToTrash);
                }
            }
        }

        if (trashed.size() > 0) {
            Card[] treasuresToGain = currentPlayer.controlPlayer.thief_treasuresToGain(context, trashed.toArray(new Card[] {}));

            if (treasuresToGain != null) {
                for (Card treasure : treasuresToGain) {
                    currentPlayer.gainCardAlreadyInPlay(treasure, this.getControlCard(), context);
                    game.trashPile.remove(treasure);
                }
            }
        }
    }
    
    private void vassal(Game game, MoveContext context, Player player) {
    	Card draw = game.draw(context, Cards.warrior, 1);
        if (draw != null) {
        	player.discard(draw, this.getControlCard(), context);
        	int discardIndex = player.discard.size() - 1;
        	if (draw.is(Type.Action, player) && player.controlPlayer.vassal_shouldPlayCard(context, draw)) {
        		//TODO: doesn't apply with current cards, 
        		//      but future card could trigger lose track rule and prevent moving to play area
        		//      but not prevent from playing
        		player.discard.remove(discardIndex);
        		context.freeActionInEffect++;
                draw.play(game, context, false);
                context.freeActionInEffect--;
        	}
        }
    }

    private void workshop(Player currentPlayer, MoveContext context) {
        Card card = currentPlayer.controlPlayer.workshop_cardToObtain(context);
        if (card != null) {
            // check cost
            if (card.getCost(context) <= 4 && card.getDebtCost(context) == 0 && !card.costPotion()) {
                currentPlayer.gainNewCard(card, this.getControlCard(), context);
            }
        }
    }
}
