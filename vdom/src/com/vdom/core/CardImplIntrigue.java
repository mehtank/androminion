package com.vdom.core;

import java.util.ArrayList;
import java.util.Collections;

import com.vdom.api.Card;
import com.vdom.api.GameEvent;
import com.vdom.api.GameEventListener;

public class CardImplIntrigue extends CardImpl {
	private static final long serialVersionUID = 1L;

	public CardImplIntrigue(CardImpl.Builder builder) {
		super(builder);
	}

	protected CardImplIntrigue() { }

	@Override
	protected void additionalCardActions(Game game, MoveContext context, Player currentPlayer) {
		switch(getKind()) {
		case Baron:
            baron(context, currentPlayer);
            break;
		case Bridge:
	        context.cardCostModifier -= 1;
	        break;
        case Conspirator:
            conspirator(game, context, currentPlayer);
            break;
        case Coppersmith:
            copperSmith(context);
            break;
        case Courtyard:
            courtyard(context, currentPlayer);
            break;
        case Ironworks:
            ironworks(game, context, currentPlayer);
            break;
        case Masquerade:
            masquerade(game, context, currentPlayer);
            break;
        case MiningVillage:
            miningVillage(context, currentPlayer);
            break;
        case Minion:
            minion(game, context, currentPlayer);
            break;
        case Nobles:
            nobles(game, context, currentPlayer);
            break;
        case Pawn:
            pawn(game, context, currentPlayer);
            break;
        case Saboteur:
            saboteur(game, context, currentPlayer);
            break;
        case Scout:
            scout(game, context, currentPlayer);
            break;
        case SecretChamber:
            secretChamber(context, currentPlayer);
            break;
        case ShantyTown:
            shantyTown(game, context, currentPlayer);
            break;
        case Steward:
            steward(game, context, currentPlayer);
            break;
        case Swindler:
            swindler(game, context, currentPlayer);
            break;
        case Torturer:
            torturer(game, context, currentPlayer);
            break;
        case TradingPost:
            tradingPost(context, currentPlayer);
            break;
        case Tribute:
            tribute(game, context, currentPlayer);
            break;
        case Upgrade:
            upgrade(context, currentPlayer);
            break;
        case WishingWell:
            wishingWell(game, context, currentPlayer);
            break;
		default:
			break;
		}
	}
	
	private void baron(MoveContext context, Player currentPlayer) {
        boolean discard = false;
        for (Card cardToCheck : currentPlayer.hand) {
            if (cardToCheck.equals(Cards.estate)) {
                discard = currentPlayer.controlPlayer.baron_shouldDiscardEstate(context);
                break;
            }
        }

        if (discard) {
            Card card = currentPlayer.hand.get(Cards.estate);
            currentPlayer.hand.remove(Cards.estate);
            currentPlayer.discard(card, this.controlCard, context);
            context.addCoins(4);
        } else {
            currentPlayer.gainNewCard(Cards.estate, this.controlCard, context);
        }
    }
	
    private void conspirator(Game game, MoveContext context, Player currentPlayer) {
        if (context.actionsPlayedSoFar >= 3) {
            context.actions++;
            game.drawToHand(context, this, 1);
        }
    }
    
    private void copperSmith(MoveContext context) {
        context.coppersmithsPlayed++;
    }
    
    private void courtyard(MoveContext context, Player currentPlayer) {
        // TODO do this.controlCard check at the top of the block for EVERY Util...
        if (currentPlayer.getHand().size() > 0) {
            Card card = currentPlayer.controlPlayer.courtyard_cardToPutBackOnDeck(context);

            if (card == null || !currentPlayer.hand.contains(card)) {
                Util.playerError(currentPlayer, "Courtyard error, just putting back a random card.");
                card = Util.randomCard(currentPlayer.hand);
            }

            currentPlayer.putOnTopOfDeck(currentPlayer.hand.remove(currentPlayer.hand.indexOf(card)));
        }
    }
   
    private void ironworks(Game game, MoveContext context, Player currentPlayer) {
        Card card = currentPlayer.controlPlayer.ironworks_cardToObtain(context);
        if (card != null && card.getCost(context) <= 4 && card.getDebtCost(context) == 0 && !card.costPotion()) {
            if (currentPlayer.gainNewCard(card, this.controlCard, context).equals(card)) {
                //note these could be wrong if Watchtower is used to trash a gained inherited Estate
                if (card.is(Type.Action, currentPlayer)) {
                    context.actions++;
                }
                if (card.is(Type.Treasure, currentPlayer)) {
                    context.addCoins(1);
                }
                if (card.is(Type.Victory, currentPlayer)) {
                    game.drawToHand(context, this, 1);
                }
            }
        }
    }

    private void masquerade(Game game, MoveContext context, Player currentPlayer) {
        Card[] passedCards = new Card[Game.players.length];

        for (int i = 0; i < Game.players.length; i++) {
            Player player = Game.players[i];
            if (player.getHand().size() == 0) {
                continue;
            }
            Card card = player.controlPlayer.masquerade_cardToPass(new MoveContext(context, game, player));
            if (card == null || !(player).hand.contains(card)) {
                Util.playerError(player, "Masquerade pass card error, picking random card to pass.");
                card = Util.randomCard(player.getHand());
            }

            // TODO Should this.controlCard send some new type of event, not trashed, but passed maybe?
            if (card != null) {
                (player).hand.remove(card);
                passedCards[i] = card;
            }
        }

        for (int i = 0; i < Game.players.length; i++) {
            int next = i + 1;
            if (next >= Game.players.length) {
                next = 0;
            }

            Player nextPlayer = Game.players[next];

            Card card = passedCards[i];
            if (card != null) {
            	((CardImpl)card).stopInheritingCardAbilities();
                nextPlayer.hand.add(card);
                if (nextPlayer instanceof GameEventListener) {
                    GameEvent event = new GameEvent(GameEvent.EventType.CardObtained, new MoveContext(context, game, nextPlayer));
                    event.card = card;
                    event.responsible = this.controlCard;
                    event.newCard = false;
                    ((GameEventListener) nextPlayer).gameEvent(event);
                }

                // nextPlayer.gainCardAlreadyInPlay(card, this.controlCard, new MoveContext(game, nextPlayer));
            }
        }

        Card toTrash = currentPlayer.controlPlayer.masquerade_cardToTrash(context);
        if (toTrash != null) {
            if (currentPlayer.hand.contains(toTrash)) {
                currentPlayer.hand.remove(toTrash);

                currentPlayer.trash(toTrash, this.controlCard, context);
            } else {
                Util.playerError(currentPlayer, "Masquerade trash error, card not in hand, ignoring.");
            }
        }
    }

    private void miningVillage(MoveContext context, Player currentPlayer) {
        if (!this.controlCard.movedToNextTurnPile) {
            if (currentPlayer.controlPlayer.miningVillage_shouldTrashMiningVillage(context)) {
                context.addCoins(2);
                this.controlCard.movedToNextTurnPile = true;
                currentPlayer.trash(this.controlCard, null, context);
                currentPlayer.playedCards.remove(currentPlayer.playedCards.lastIndexOf(this.controlCard));
            }
        }
    }

    private void minion(Game game, MoveContext context, Player currentPlayer) {
        ArrayList<Player> playersToAttack = new ArrayList<Player>();
        for (Player player : game.getPlayersInTurnOrder()) {
            if (player == currentPlayer || !Util.isDefendedFromAttack(game, player, this)) {
                playersToAttack.add(player);
                if (player != currentPlayer) {
                    player.attacked(this.controlCard, context);
                }
            }
        }

        Player.MinionOption option = currentPlayer.controlPlayer.minion_chooseOption(context);

        if (option == null) {
            Util.playerError(currentPlayer, "Minion option error, choosing to add gold.");
            option = Player.MinionOption.AddGold;
        }

        if (option == Player.MinionOption.AddGold) {
            context.addCoins(2);
        } else if (option == Player.MinionOption.RolloverCards) {
            for (Player player : playersToAttack) {
                if (player == currentPlayer || player.hand.size() >= 5) {
                    MoveContext targetContext = new MoveContext(game, player);
                    targetContext.attackedPlayer = player;
                    while (!player.hand.isEmpty()) {
                        player.discard(player.hand.remove(0), this.controlCard, targetContext);
                    }

                    game.drawToHand(targetContext, this, 4);
                    game.drawToHand(targetContext, this, 3);
                    game.drawToHand(targetContext, this, 2);
                    game.drawToHand(targetContext, this, 1);
                }
            }
        }
    }

    private void nobles(Game game, MoveContext context, Player currentPlayer) {
        Player.NoblesOption option = currentPlayer.controlPlayer.nobles_chooseOptions(context);
        if (option == null) {
            Util.playerError(currentPlayer, "Nobles option error, ignoring.");
        } else {
            if (option == Player.NoblesOption.AddActions) {
                context.actions += 2;
            } else if (option == Player.NoblesOption.AddCards) {
                game.drawToHand(context, this, 3);
                game.drawToHand(context, this, 2);
                game.drawToHand(context, this, 1);
            }
        }
    }

    private void pawn(Game game, MoveContext context, Player currentPlayer) {
        Player.PawnOption[] options = currentPlayer.controlPlayer.pawn_chooseOptions(context);
        if (options == null || options.length != 2 || options[0] == options[1]) {
            Util.playerError(currentPlayer, "Pawn options error, ignoring.");
        } else {
            for (Player.PawnOption option : options) {
                if (option == Player.PawnOption.AddAction) {
                    context.actions++;
                } else if (option == Player.PawnOption.AddBuy) {
                    context.buys++;
                } else if (option == Player.PawnOption.AddCard) {
                    game.drawToHand(context, this, 1);
                } else if (option == Player.PawnOption.AddGold) {
                    context.addCoins(1);
                }
            }
        }
    }

    private void saboteur(Game game, MoveContext context, Player currentPlayer) {
        for (Player player : game.getPlayersInTurnOrder()) {
            if (player != currentPlayer && !Util.isDefendedFromAttack(game, player, this)) {
                player.attacked(this.controlCard, context);
                MoveContext playerContext = new MoveContext(game, player);
                playerContext.attackedPlayer = player;
                playerContext.cardCostModifier = context.cardCostModifier;

                ArrayList<Card> toDiscard = new ArrayList<Card>();
                Card draw;

                while ((draw = game.draw(playerContext, Cards.saboteur, -1)) != null) {
                    if (draw.getCost(context) >= 3) {
                        int value = draw.getCost(context);
                        value -= 2;
                        if (value < 0) {
                            value = 0;
                        }

                        boolean potion = draw.costPotion();
                        int debt = draw.getDebtCost(context);
                        
                        player.trash(draw, this.controlCard, playerContext);

                        Card card = (player).controlPlayer.saboteur_cardToObtain(playerContext, value, debt, potion);
                        if (card != null) {
                            if (card.getCost(context) > value || card.getDebtCost(context) > debt || (card.costPotion() && !potion) || !Cards.isSupplyCard(card)) {
                                Util.playerError(currentPlayer, "Saboteur obtain error, ignoring.");
                            }
                            else {
                                if(player.gainNewCard(card, this.controlCard, playerContext) == null) {
                                    Util.playerError(currentPlayer, "Saboteur obtain error, ignoring.");
                                }
                            }
                        }

                        break;
                    } else {
                        player.reveal(draw, this.controlCard, playerContext);
                        toDiscard.add(draw);
                    }
                }

                while (!toDiscard.isEmpty()) {
                    player.discard(toDiscard.remove(0), this.controlCard, null);
                }
            }
        }
    }

    private void scout(Game game, MoveContext context, Player currentPlayer) {
        ArrayList<Card> cards = new ArrayList<Card>();
        for (int i = 0; i < 4; i++) {
            Card card = game.draw(context, Cards.scout, 4 - i);
            if (card == null) {
                break;
            }
            if (card.is(Type.Victory, currentPlayer)) {
                currentPlayer.hand.add(card);
            } else {
                cards.add(card);
            }
        }

        if (cards.size() == 0) {
            return;
        }

        for (Card card : cards) {
            currentPlayer.reveal(card, this.controlCard, context);
        }

        Card[] order = currentPlayer.controlPlayer.scout_orderCards(context, cards.toArray(new Card[cards.size()]));
        boolean bad = false;
        if (order == null || order.length != cards.size()) {
            bad = true;
        } else {
            ArrayList<Card> orderArray = new ArrayList<Card>();
            for (Card card : order) {
                orderArray.add(card);
                if (!cards.contains(card)) {
                    bad = true;
                }
            }

            for (Card card : cards) {
                if (!orderArray.contains(card)) {
                    bad = true;
                }
            }
        }

        if (bad) {
            Util.playerError(currentPlayer, "Scout order cards error, ignoring.");
            order = cards.toArray(new Card[cards.size()]);
        }

        for (int i = order.length - 1; i >= 0; i--) {
            currentPlayer.putOnTopOfDeck(order[i]);
        }
    }
    
    private void secretChamber(MoveContext context, Player currentPlayer) {
        Card[] cards = currentPlayer.controlPlayer.secretChamber_cardsToDiscard(context);
        if (cards != null) {
            int numberOfCardsDiscarded = 0;
            for (Card card : cards) {
                if (currentPlayer.hand.remove(card)) {
                    currentPlayer.discard(card, this.controlCard, context);
                    numberOfCardsDiscarded++;
                }
            }

            if (numberOfCardsDiscarded != cards.length) {
                Util.playerError(currentPlayer, "Secret chamber discard error, trying to discard cards not in hand, ignoring extra.");
            }

            context.addCoins(numberOfCardsDiscarded);
        }
    }
    
    private void shantyTown(Game game, MoveContext context, Player currentPlayer) {
        boolean actions = false;
        for (Card card : currentPlayer.hand) {
            currentPlayer.reveal(card, this.controlCard, context);

            if (card.is(Type.Action, currentPlayer)) {
                actions = true;
            }
        }

        if (!actions) {
            game.drawToHand(context, this, 2);
            game.drawToHand(context, this, 1);
        }
    }
    
    private void steward(Game game, MoveContext context, Player currentPlayer) {
        Player.StewardOption option = currentPlayer.controlPlayer.steward_chooseOption(context);

        if (option == null) {
            Util.playerError(currentPlayer, "Steward option error, ignoring.");
        } else {
            if (option == Player.StewardOption.AddGold) {
                context.addCoins(2);
            } else if (option == Player.StewardOption.AddCards) {
                game.drawToHand(context, this, 2);
                game.drawToHand(context, this, 1);
            } else if (option == Player.StewardOption.TrashCards) {
                CardList hand = currentPlayer.getHand();
                if (hand.size() == 0) {
                    return;
                }

                Card[] cards = currentPlayer.controlPlayer.steward_cardsToTrash(context);
                boolean bad = false;
                if (cards == null) {
                    bad = true;
                } else if (cards.length != 2) {
                    if (hand.size() >= 2 || cards.length != hand.size()) {
                        bad = true;
                    }
                } else {
                    ArrayList<Card> copy = Util.copy(currentPlayer.hand);
                    for (Card card : cards) {
                        if (!copy.remove(card)) {
                            bad = true;
                            break;
                        }
                    }
                }

                if (bad) {
                    Util.playerError(currentPlayer, "Steward trash error, picking first two cards.");

                    if (hand.size() >= 2) {
                        cards = new Card[2];
                    } else {
                        cards = new Card[hand.size()];
                    }
                    for (int i = 0; i < cards.length; i++) {
                        cards[i] = hand.get(i);
                    }
                }

                for (Card card : cards) {
                    currentPlayer.hand.remove(card);
                    currentPlayer.trash(card, this.controlCard, context);
                }
            }
        }
    }
    
    private void swindler(Game game, MoveContext context, Player currentPlayer) {
        for (Player player : game.getPlayersInTurnOrder()) {
            if (player != currentPlayer && !Util.isDefendedFromAttack(game, player, this)) {
                player.attacked(this.controlCard, context);
                MoveContext playerContext = new MoveContext(game, player);
                playerContext.attackedPlayer = player;

                Card draw = game.draw(playerContext, Cards.swindler, 1);
                if (draw != null) {
                    player.trash(draw, this.controlCard, playerContext);

                    Card card = currentPlayer.controlPlayer.swindler_cardToSwitch(context, draw.getCost(context), draw.getDebtCost(context), draw.costPotion());

                    boolean bad = false;
                    if (card == null) {
                        // Check that there are no cards that are possible to trade for...
                        for (Card thisCard : context.getCardsInGame()) {
                            if (Cards.isSupplyCard(thisCard) && !game.isPileEmpty(thisCard) && thisCard.getCost(context) == draw.getCost(context) && thisCard.getDebtCost(context) == draw.getDebtCost(context) && thisCard.costPotion() == draw.costPotion()) {
                                bad = true;
                                break;
                            }
                        }
                    } else if (!Cards.isSupplyCard(card) || game.isPileEmpty(card) || card.getCost(context) != draw.getCost(context) || card.getDebtCost(context) != draw.getDebtCost(context) || card.costPotion() != draw.costPotion()) {
                        bad = true;
                    }

                    if (bad) {
                        Util.playerError(currentPlayer, "Swindler swap card error, picking a random card.");

                        ArrayList<Card> possible = new ArrayList<Card>();
                        for (Card thisCard : context.getCardsInGame()) {
                            if (Cards.isSupplyCard(thisCard) && !game.isPileEmpty(thisCard) && thisCard.getCost(context) == draw.getCost(context) && thisCard.getDebtCost(context) == draw.getDebtCost(context) && thisCard.costPotion() == draw.costPotion()) {
                                possible.add(thisCard);
                            }
                        }

                        card = Util.randomCard(possible);
                    }

                    if (card != null) {
                        player.gainNewCard(card, this.controlCard, playerContext);
                    }
                }
            }
        }
    }
    
    private void torturer(Game game, MoveContext context, Player currentPlayer) {
        for (Player targetPlayer : game.getPlayersInTurnOrder()) {
            if (targetPlayer != currentPlayer && !Util.isDefendedFromAttack(game, targetPlayer, this)) {
                targetPlayer.attacked(this.controlCard, context);
                MoveContext playerContext = new MoveContext(game, targetPlayer);
                playerContext.attackedPlayer = targetPlayer;
                Player.TorturerOption option;
                try {
                    option = (targetPlayer).controlPlayer.torturer_attack_chooseOption(playerContext);
                } catch (NoSuchFieldError e) {
                    Util.playerError(targetPlayer, "'Take three cards' version of torturer attack no longer supported.");
                    option = null;
                }

                if (option == null) {
                    Util.playerError(targetPlayer, "Torturer option error, taking curse card.");
                    option = Player.TorturerOption.TakeCurse;
                }

                if (option == Player.TorturerOption.TakeCurse) {
                    targetPlayer.gainNewCard(Cards.curse, this.controlCard, playerContext);
                } else {
                    ArrayList<Card> handCopy = Util.copy(targetPlayer.getHand());
                    Card[] cardsToDiscard = (targetPlayer).controlPlayer.torturer_attack_cardsToDiscard(playerContext);

                    boolean bad = false;
                    if (cardsToDiscard == null) {
                        bad = true;
                    } else if (handCopy.size() < 2 && cardsToDiscard.length != handCopy.size()) {
                        bad = true;
                    } else if (cardsToDiscard.length != 2) {
                        bad = true;
                    } else {
                        ArrayList<Card> copyForDiscard = Util.copy(targetPlayer.getHand());
                        for (Card cardToKeep : cardsToDiscard) {
                            if (!copyForDiscard.remove(cardToKeep)) {
                                bad = true;
                                break;
                            }
                        }
                    }

                    if (bad) {
                        if (handCopy.size() >= 2) {
                            Util.playerError(targetPlayer, "Torturer discard error, just discarding the first 2.");
                        }
                        cardsToDiscard = new Card[Math.min(2, handCopy.size())];
                        for (int i = 0; i < cardsToDiscard.length; i++) {
                            cardsToDiscard[i] = handCopy.get(i);
                        }
                    }

                    for (Card card : cardsToDiscard) {
                        targetPlayer.hand.remove(card);
                        targetPlayer.discard(card, this.controlCard, playerContext);
                    }
                }
            }
        }
    }
    
    private void tradingPost(MoveContext context, Player currentPlayer) {
        if (currentPlayer.getHand().size() == 0) {
            return;
        }
        ArrayList<Card> handCopy = Util.copy(currentPlayer.getHand());
        Card[] cardsToTrash = currentPlayer.controlPlayer.tradingPost_cardsToTrash(context);
        // Trash forced, pick cards randomly if not selected
        boolean bad = false;
        if (cardsToTrash == null) {
            bad = true;
        } else if (handCopy.size() < 2 && cardsToTrash.length != handCopy.size()) {
            bad = true;
        } else if (handCopy.size() >= 2 && cardsToTrash.length != 2) {
            bad = true;
        } else {
            ArrayList<Card> copyForTrash = Util.copy(currentPlayer.getHand());
            for (Card cardToKeep : cardsToTrash) {
                if (!copyForTrash.remove(cardToKeep)) {
                    bad = true;
                    break;
                }
            }
        }

        if (bad) {
            if (handCopy.size() >= 2) {
                Util.playerError(currentPlayer, "TradingPost trash error, just trashing the first 2.");
            }
            cardsToTrash = new Card[Math.min(2, handCopy.size())];
            for (int i = 0; i < cardsToTrash.length; i++) {
                cardsToTrash[i] = handCopy.get(i);
            }
        }

        for (int i = cardsToTrash.length - 1; i >= 0 ; i--) {
            currentPlayer.hand.remove(cardsToTrash[i]);
            currentPlayer.trash(cardsToTrash[i], this.controlCard, context);
        }
        if (cardsToTrash.length == 2) {
            currentPlayer.gainNewCard(Cards.silver, this.controlCard, context);
        }
    }
    
    private void tribute(Game game, MoveContext context, Player currentPlayer) {
        Card[] revealedCards = new Card[2];
        Player nextPlayer = game.getNextPlayer();
        MoveContext targetContext = new MoveContext(game, nextPlayer);
        revealedCards[0] = game.draw(targetContext, Cards.tribute, 2);
        revealedCards[1] = game.draw(targetContext, Cards.tribute, 1);

        if (revealedCards[0] != null) {
            nextPlayer.reveal(revealedCards[0], this.controlCard, targetContext);
            (nextPlayer).discard(revealedCards[0], this.controlCard, null);

        }
        if (revealedCards[1] != null) {
            nextPlayer.reveal(revealedCards[1], this.controlCard, targetContext);
            (nextPlayer).discard(revealedCards[1], this.controlCard, null);
        }

        // "For each differently named card revealed..."
        if (revealedCards[0] != null && revealedCards[0].equals(revealedCards[1])) {
            revealedCards[1] = null;
        }

        for (Card card : revealedCards) {
            if (card != null && !card.equals(Cards.curse)) {
                if (card.is(Type.Action, nextPlayer)) {
                    context.actions += 2;
                }
                if (card.is(Type.Treasure, nextPlayer)) {
                    context.addCoins(2);
                }
                if (card.is(Type.Victory, nextPlayer)) {
                    game.drawToHand(context, this, 2);
                    game.drawToHand(context, this, 1);
                }
            }
        }
    }
   
    private void upgrade(MoveContext context, Player currentPlayer) {
        if (currentPlayer.getHand().size() > 0) {
            Card card = currentPlayer.controlPlayer.upgrade_cardToTrash(context);
            if (card == null || !currentPlayer.hand.contains(card)) {
                Util.playerError(currentPlayer, "Upgrade trash error, upgrading a random card.");
                card = Util.randomCard(currentPlayer.hand);
            }

            int value = card.getCost(context) + 1;
            boolean potion = card.costPotion();
            int debt = card.getDebtCost(context);
            currentPlayer.hand.remove(card);
            currentPlayer.trash(card, this.controlCard, context);

            card = currentPlayer.controlPlayer.upgrade_cardToObtain(context, value, debt, potion);
            if (card != null) {
                if (card.getCost(context) != value || card.getDebtCost(context) != value || card.costPotion() != potion) {
                    Util.playerError(currentPlayer, "Upgrade error, new card does not cost value of the old card +1.");
                } else {
                    if(currentPlayer.gainNewCard(card, this.controlCard, context) == null) {
                        Util.playerError(currentPlayer, "Upgrade error, pile is empty or card is not in the game.");
                    }
                }
            }
        }
    }

    private void wishingWell(Game game, MoveContext context, Player currentPlayer) {

        if (currentPlayer.deck.size() > 0 || currentPlayer.discard.size() > 0) {  // Only allow a guess if there are cards in the deck or discard pile

            // Create a list of possible cards to guess, using the player's hand, discard pile, and deck 
            // (even though the player could technically name a card he doesn't have)
            ArrayList<Card> options = new ArrayList<Card>(currentPlayer.getDistinctCards());
            Collections.sort(options, new Util.CardNameComparator());

            if (!options.isEmpty()) {
                Card card = currentPlayer.controlPlayer.wishingWell_cardGuess(context, options);
                currentPlayer.controlPlayer.namedCard(card, this.controlCard, context);
                Card draw = game.draw(context, Cards.wishingWell, 1);
                if (draw != null) {
                    currentPlayer.reveal(draw, this.controlCard, context);

                    if (card != null && card.equals(draw)) {
                        currentPlayer.hand.add(draw, true);
                    } else {
                        currentPlayer.putOnTopOfDeck(draw, context, true);
                    }
                }
            }
        }
    }

}
