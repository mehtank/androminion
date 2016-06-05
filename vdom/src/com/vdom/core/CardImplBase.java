package com.vdom.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.vdom.api.Card;
import com.vdom.api.GameEvent;
import com.vdom.api.VictoryCard;

public class CardImplBase extends CardImpl {
	private static final long serialVersionUID = 1L;

	public CardImplBase(CardImpl.Builder builder) {
		super(builder);
	}

	@Override
	protected void additionalCardActions(Game game, MoveContext context, Player currentPlayer) {
		switch (this.getKind()) {
			case Adventurer:
	            adventurer(game, context, currentPlayer);
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
			case Library:
                library(game, context, currentPlayer);
                break;
			case Militia:
                militia(game, context, currentPlayer);
                break;
			case Mine:
                mine(context, currentPlayer);
                break;
			case MoneyLender:
	            moneyLender(context, currentPlayer);
	            break;
			case Remodel:
                remodel(context, currentPlayer);
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
            currentPlayer.reveal(draw, this.controlCard, context);

            if (draw.is(Type.Treasure, currentPlayer)) {
                treasureCardsRevealed++;
                currentPlayer.hand.add(draw);
            } else {
                toDiscard.add(draw);
            }
        }

        while (!toDiscard.isEmpty()) {
            currentPlayer.discard(toDiscard.remove(0), this.controlCard, context);
        }
    }
	
	private void bureaucrat(Game game, MoveContext context, Player currentPlayer) {
        currentPlayer.gainNewCard(Cards.silver, this, context);

        for (Player player : game.getPlayersInTurnOrder()) {
            if (player != currentPlayer && !Util.isDefendedFromAttack(game, player, this)) {
                player.attacked(this.controlCard, context);
                MoveContext playerContext = new MoveContext(game, player);
                playerContext.attackedPlayer = player;

                ArrayList<VictoryCard> victoryCards = new ArrayList<VictoryCard>();

                for (Card card : player.hand) {
                    if (card instanceof VictoryCard) {
                        victoryCards.add((VictoryCard) card);
                    }
                }

                if (victoryCards.size() == 0) {
                    for (int i = 0; i < player.hand.size(); i++) {
                        Card card = player.hand.get(i);
                        player.reveal(card, this.controlCard, playerContext);
                    }
                } else {
                    VictoryCard toTopOfDeck = null;

                    if (victoryCards.size() == 1) {
                        toTopOfDeck = victoryCards.get(0);
                    } else if (Collections.frequency(victoryCards, victoryCards.get(0)) ==
                                victoryCards.size() /*all the same*/) {
                        toTopOfDeck = victoryCards.get(0);
                    } else {
                        toTopOfDeck = (player).controlPlayer.bureaucrat_cardToReplace(playerContext);

                        if (toTopOfDeck == null) {
                            Util.playerError(player, "No Victory Card selected for Bureaucrat, using first Victory Card in hand");
                            toTopOfDeck = victoryCards.get(0);
                        }
                    }

                    player.reveal(toTopOfDeck, this.controlCard, playerContext);
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
                        currentPlayer.discard(currentPlayer.hand.remove(i), this.controlCard, context);
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
                currentPlayer.discard(game.draw(context, Cards.chancellor, 0), this.controlCard, null, false, false);
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
                            currentPlayer.trash(thisCard, this.controlCard, context);
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
            if (card.getCost(context) <= 5) {
                currentPlayer.gainNewCard(card, this.controlCard, context);
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
            if (draw.isAction(currentPlayer)) {
                shouldKeep = currentPlayer.controlPlayer.library_shouldKeepAction(context, draw);
            }

            if (shouldKeep) {
                currentPlayer.hand.add(draw);
            } else {
                toDiscard.add(draw);
            }
        }

        while (!toDiscard.isEmpty()) {
            currentPlayer.discard(toDiscard.remove(0), this.controlCard, null);
        }
    }
    
    private void militia(Game game, MoveContext context, Player currentPlayer) {
        for (Player player : game.getPlayersInTurnOrder()) {
            if (player != currentPlayer && !Util.isDefendedFromAttack(game, player, this)) {
                player.attacked(this.controlCard, context);
                MoveContext playerContext = new MoveContext(game, player);
                playerContext.attackedPlayer = player;

                int keepCardCount = 3;
                if (player.hand.size() > keepCardCount) {
                    Card[] cardsToKeep = player.controlPlayer.militia_attack_cardsToKeep(playerContext);
                    player.discardRemainingCardsFromHand(playerContext, cardsToKeep, this.controlCard, keepCardCount);
                }
            }
        }
    }
    
    private void mine(MoveContext context, Player currentPlayer) {
        Card cardToUpgrade = currentPlayer.controlPlayer.mine_treasureFromHandToUpgrade(context);
        if (cardToUpgrade == null || !cardToUpgrade.is(Type.Treasure, currentPlayer)) {
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
                    currentPlayer.trash(thisCard, this.controlCard, context);

                    Card newCard = currentPlayer.controlPlayer.mine_treasureToObtain(context, card.getCost(context) + 3, card.costPotion());
                    if (!(newCard != null && newCard.is(Type.Treasure, null) && Cards.isSupplyCard(newCard) && newCard.getCost(context) <= card.getCost(context) + 3 && context.getCardsLeftInPile(newCard) > 0)) {
                        Util.playerError(currentPlayer, "Mine treasure to obtain was invalid, picking random treasure from table.");
                        for (Card treasureCard : context.getTreasureCardsInGame()) {
                            if (Cards.isSupplyCard(treasureCard) && context.getCardsLeftInPile(treasureCard) > 0 && treasureCard.getCost(context) <= card.getCost(context) + 3)
                                if (!treasureCard.costPotion() || card.costPotion())
                                    newCard = treasureCard;
                        }
                    }

                    if (newCard != null && newCard.getCost(context) <= card.getCost(context) + 3 && Cards.isSupplyCard(newCard) && context.getCardsLeftInPile(newCard) > 0)
                        currentPlayer.gainNewCard(newCard, this.controlCard, context);
                    break;
                }
            }
        }
    }

    private void moneyLender(MoveContext context, Player currentPlayer) {
        for (int i = 0; i < currentPlayer.hand.size(); i++) {
            Card card = currentPlayer.hand.get(i);
            if (card.equals(Cards.copper)) {
                Card thisCard = currentPlayer.hand.remove(i);
                context.addCoins(3);
                currentPlayer.trash(thisCard, this.controlCard, context);
                break;
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
            boolean potion = false;
            for (int i = 0; i < currentPlayer.hand.size(); i++) {
                Card playersCard = currentPlayer.hand.get(i);
                if (playersCard.equals(cardToTrash)) {
                    cost = playersCard.getCost(context);
                    potion = playersCard.costPotion();
                    playersCard = currentPlayer.hand.remove(i);

                    currentPlayer.trash(playersCard, this.controlCard, context);
                    break;
                }
            }

            if (cost == -1) {
                Util.playerError(currentPlayer, "Remodel returned invalid card, ignoring.");
                return;
            }

            cost += 2;

            Card card = currentPlayer.controlPlayer.remodel_cardToObtain(context, cost, potion);
            if (card != null) {
                // check cost
                if (card.getCost(context) > cost) {
                    Util.playerError(currentPlayer, "Remodel new card costs too much, ignoring.");
                }
                else if (card.costPotion() && !potion) {
                    Util.playerError(currentPlayer, "Remodel new card costs potion, ignoring.");
                }
                else
                {
                    if(currentPlayer.gainNewCard(card, this.controlCard, context) == null) {
                        Util.playerError(currentPlayer, "Remodel new card is invalid, ignoring.");
                    }
                }
            }
        }
    }
    
    private void thief(Game game, MoveContext context, Player currentPlayer) {
        ArrayList<Card> trashed = new ArrayList<Card>();

        for (Player targetPlayer : game.getPlayersInTurnOrder()) {
            if (targetPlayer != currentPlayer && !Util.isDefendedFromAttack(game, targetPlayer, this)) {
                targetPlayer.attacked(this.controlCard, context);
                MoveContext targetContext = new MoveContext(game, targetPlayer);
                targetContext.attackedPlayer = targetPlayer;
                ArrayList<Card> treasures = new ArrayList<Card>();

                List<Card> cardsToDiscard = new ArrayList<Card>();
                for (int i = 0; i < 2; i++) {
                    Card card = game.draw(targetContext, Cards.thief, 2 - i);

                    if (card != null) {
                        targetPlayer.reveal(card, this.controlCard, targetContext);

                        if (card.is(Type.Treasure, targetPlayer)) {
                            treasures.add(card);
                        } else {
                            cardsToDiscard.add(card);
                        }
                    }
                }

                for (Card c: cardsToDiscard) {
                    targetPlayer.discard(c, this.controlCard, targetContext);
                }

                Card cardToTrash = null;

                if (treasures.size() == 1) {
                    cardToTrash = treasures.get(0);
                } else if (treasures.size() == 2) {
                    if (treasures.get(0).equals(treasures.get(1))) {
                        cardToTrash = treasures.get(0);
                        targetPlayer.discard(treasures.remove(1), this.controlCard, targetContext);
                    } else {
                        cardToTrash = currentPlayer.controlPlayer.thief_treasureToTrash(context, treasures.toArray(new Card[] {}));
                    }

                    for (Card treasure : treasures) {
                        if (!treasure.equals(cardToTrash)) {
                            targetPlayer.discard(treasure, this.controlCard, targetContext);
                        }
                    }
                }

                if (cardToTrash != null) {
                    targetPlayer.trash(cardToTrash, this.controlCard, targetContext);
                    trashed.add(cardToTrash);
                }
            }
        }

        if (trashed.size() > 0) {
            Card[] treasuresToGain = currentPlayer.controlPlayer.thief_treasuresToGain(context, trashed.toArray(new Card[] {}));

            if (treasuresToGain != null) {
                for (Card treasure : treasuresToGain) {
                    currentPlayer.gainCardAlreadyInPlay(treasure, this.controlCard, context);
                    game.trashPile.remove(treasure);
                }
            }
        }
    }

    private void workshop(Player currentPlayer, MoveContext context) {
        Card card = currentPlayer.controlPlayer.workshop_cardToObtain(context);
        if (card != null) {
            // check cost
            if (card.getCost(context) <= 4) {
                currentPlayer.gainNewCard(card, this.controlCard, context);
            }
        }
    }
}
