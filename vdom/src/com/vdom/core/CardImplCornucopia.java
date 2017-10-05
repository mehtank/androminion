package com.vdom.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import com.vdom.api.Card;
import com.vdom.api.GameEvent;
import com.vdom.core.Player.JesterOption;
import com.vdom.core.Player.TournamentOption;
import com.vdom.core.Player.TrustySteedOption;

public class CardImplCornucopia extends CardImpl {
	private static final long serialVersionUID = 1L;

	public CardImplCornucopia(CardImpl.Builder builder) {
		super(builder);
	}
	
	protected CardImplCornucopia() { }

	@Override
	protected void additionalCardActions(Game game, MoveContext context, Player currentPlayer) {
		switch(getKind()) {
		case BagofGold:
            currentPlayer.gainNewCard(Cards.gold, this.getControlCard(), context);
            break;
		case Diadem:
			context.addCoins(context.getActionsLeft());
			break;
		case FarmingVillage:
            farmingVillage(game, context, currentPlayer);
            break;
		case Followers:
            followers(game, context, currentPlayer);
            break;
        case FortuneTeller:
            fortuneTeller(game, context, currentPlayer);
            break;
        case Hamlet:
            hamlet(context, currentPlayer);
            break;
        case Harvest:
            harvest(game, context, currentPlayer);
            break;
        case HornofPlenty:
        	hornOfPlenty(context, currentPlayer, game);
        	break;
        case HorseTraders:
            discardMultiple(context, currentPlayer, 2);
            break;
        case HuntingParty:
            huntingParty(game, context, currentPlayer);
            break;
        case Jester:
            jester(game, context, currentPlayer);
            break;
        case Menagerie:
            menagerie(game, context, currentPlayer);
            break;
        case Remake:
            remake(context, currentPlayer);
            break;
        case Tournament:
            tournament(game, context, currentPlayer);
            break;
        case TrustySteed:
            trustySteed(game, context, currentPlayer);
            break;
        case YoungWitch:
            youngwitch(game, context, currentPlayer);
            break;
		default:
			break;
        
		}
	}
	
	private void farmingVillage(Game game, MoveContext context, Player currentPlayer) {
        Card draw = null;

        ArrayList<Card> toDiscard = new ArrayList<Card>();

        while ((draw = game.draw(context, Cards.farmingVillage, -1)) != null && !(draw.is(Type.Action, currentPlayer)) && !(draw.is(Type.Treasure, currentPlayer))) {
            toDiscard.add(draw);
        }

        while (!toDiscard.isEmpty()) {
            Card c = toDiscard.remove(0);
            currentPlayer.reveal(c, this.getControlCard(), context);
            currentPlayer.discard(c, this.getControlCard(), context);
        }

        if (draw != null) {
            currentPlayer.reveal(draw, this.getControlCard(), context);
            currentPlayer.hand.add(draw);
        }
    }
	
	private void followers(Game game, MoveContext context, Player currentPlayer) {
        currentPlayer.gainNewCard(Cards.estate, this.getControlCard(), context);

        for (Player player : game.getPlayersInTurnOrder()) {
            if (player != currentPlayer && !Util.isDefendedFromAttack(game, player, this)) {
                player.attacked(this.getControlCard(), context);
                MoveContext playerContext = new MoveContext(game, player);
                playerContext.attackedPlayer = player;
                player.gainNewCard(Cards.curse, this.getControlCard(), playerContext);

                int keepCardCount = 3;
                if (player.hand.size() > keepCardCount) {
                    Card[] cardsToKeep = player.controlPlayer.followers_attack_cardsToKeep(playerContext);
                    player.discardRemainingCardsFromHand(context, cardsToKeep, this.getControlCard(), keepCardCount);
                }
            }
        }
    }

    private void fortuneTeller(Game game, MoveContext context, Player currentPlayer) {
        for (Player player : game.getPlayersInTurnOrder()) {
            if (player != currentPlayer && !Util.isDefendedFromAttack(game, player, this)) {
                player.attacked(this.getControlCard(), context);
                MoveContext playerContext = new MoveContext(game, player);
                playerContext.attackedPlayer = player;
                ArrayList<Card> cardsToDiscard = new ArrayList<Card>();

                Card draw = null;
                while ((draw = game.draw(playerContext, Cards.fortuneTeller, -1)) != null && !(draw.is(Type.Victory, player)) && !(draw.is(Type.Curse, player))) {
                    player.reveal(draw, this.getControlCard(), playerContext);
                    cardsToDiscard.add(draw);
                }

                if (draw != null) {
                    player.reveal(draw, this.getControlCard(), playerContext);
                    player.putOnTopOfDeck(draw, playerContext, true);
                }

                for(Card card : cardsToDiscard) {
                    player.discard(card, this.getControlCard(), playerContext);
                }
            }
        }
    }

    private void hamlet(MoveContext context, Player currentPlayer) {
        Card forAction = currentPlayer.controlPlayer.hamlet_cardToDiscardForAction(context);
        if (forAction != null) {
            currentPlayer.hand.remove(forAction);
            currentPlayer.discard(forAction, this.getControlCard(), context);
            context.actions++;
        }

        Card forBuy = currentPlayer.controlPlayer.hamlet_cardToDiscardForBuy(context);
        if (forBuy != null) {
            currentPlayer.hand.remove(forBuy);
            currentPlayer.discard(forBuy, this.getControlCard(), context);
            context.buys++;
        }
    }

    private void harvest(Game game, MoveContext context, Player currentPlayer) {
        HashSet<String> cardNames = new HashSet<String>();
        List<Card> cardToDiscard = new ArrayList<Card>();
        for (int i = 0; i < 4; i++) {
            Card draw = game.draw(context, Cards.harvest, 4 - i);
            if (draw == null) {
                break;
            }

            cardNames.add(draw.getName());
            currentPlayer.reveal(draw, this.getControlCard(), context);
            cardToDiscard.add(draw);
        }
        for (Card c: cardToDiscard) {
            currentPlayer.discard(c, this.getControlCard(), context);
        }

        context.addCoins(cardNames.size());
    }
    
    private void hornOfPlenty(MoveContext context, Player player, Game game) {
        GameEvent event;
        int maxCost = context.countUniqueCardsInPlay();
        Card toObtain = player.controlPlayer.hornOfPlenty_cardToObtain(context, maxCost);
        if (toObtain != null) {
            // check cost
            if (toObtain.getCost(context) <= maxCost && toObtain.getDebtCost(context) == 0 && !toObtain.costPotion()) {
                toObtain = game.takeFromPile(toObtain);
                // could still be null here if the pile is empty.
                if (toObtain != null) {
                    event = new GameEvent(GameEvent.EventType.CardObtained, context);
                    event.card = toObtain;
                    event.responsible = this;
                    game.broadcastEvent(event);
                    
                    if (toObtain.is(Type.Victory, player)) {
                    	player.playedCards.remove(this);
                        player.trash(this, toObtain, context);
                        event = new GameEvent(GameEvent.EventType.CardTrashed, context);
                        event.card = this;
                        game.broadcastEvent(event);
                    }
                }
            }
        }
    }
    
    private void huntingParty(Game game, MoveContext context, Player currentPlayer) {
        HashSet<String> cardNames = new HashSet<String>();

        for (int i = 0; i < currentPlayer.hand.size(); i++) {
            Card card = currentPlayer.hand.get(i);
            cardNames.add(card.getName());
            currentPlayer.revealFromHand(card, this.getControlCard(), context);
        }

        ArrayList<Card> toDiscard = new ArrayList<Card>();

        Card draw = null;
        while ((draw = game.draw(context, Cards.huntingParty, -1)) != null && cardNames.contains(draw.getName())) {
            currentPlayer.reveal(draw, this.getControlCard(), context);
            toDiscard.add(draw);
        }

        if (draw != null) {
            currentPlayer.reveal(draw, this.getControlCard(), context);
            currentPlayer.hand.add(draw);
        }

        while (!toDiscard.isEmpty()) {
            currentPlayer.discard(toDiscard.remove(0), this.getControlCard(), null);
        }
    }

    private void jester(Game game, MoveContext context, Player currentPlayer) {
        for (Player targetPlayer : game.getPlayersInTurnOrder()) {
            if (targetPlayer != currentPlayer && !Util.isDefendedFromAttack(game, targetPlayer, this)) {
                targetPlayer.attacked(this.getControlCard(), context);
                MoveContext targetContext = new MoveContext(game, targetPlayer);
                targetContext.attackedPlayer = targetPlayer;

                Card draw = game.draw(targetContext, Cards.jester, 1);

                if (draw == null) 
                {
                    continue;
                }
                
                targetPlayer.reveal(draw, this.getControlCard(), targetContext);
                targetPlayer.discard(draw, this.getControlCard(), targetContext);

                if (draw.is(Type.Victory, targetPlayer)) {
                    targetPlayer.gainNewCard(Cards.curse, this.getControlCard(), targetContext);
                }
                else if (Cards.isSupplyCard(draw))
                {
                    CardPile pile;
                    pile = game.getPile(draw);

                    MoveContext toGainContext = null;

                    if (!game.isPileEmpty(draw) &&
                        (draw.equals(pile.topCard())))
                    {
                        JesterOption option = currentPlayer.controlPlayer.controlPlayer.jester_chooseOption(context, targetPlayer, draw);
                        toGainContext = JesterOption.GainCopy.equals(option) ? context : targetContext;
                        toGainContext.getPlayer().gainNewCard(draw, this.getControlCard(), toGainContext);
                    }
                }
            }
        }
    }
    
    private void menagerie(Game game, MoveContext context, Player currentPlayer) {
        HashSet<String> cardNames = new HashSet<String>();
        boolean distinct = true;

        for (int i = 0; i < currentPlayer.hand.size(); i++) {
            Card card = currentPlayer.hand.get(i);
            currentPlayer.reveal(card, this.getControlCard(), context);
            distinct &= cardNames.add(card.getName());
        }

        int numCards = distinct ? 3 : 1;
        for (int i = 0; i < numCards; i++) {
            game.drawToHand(context, this, numCards - i);
        }
    }

    private void remake(MoveContext context, Player currentPlayer) {
        for (int i = 0; i < 2; i++) {
            if (currentPlayer.getHand().size() == 0) {
                return;
            }

            Card card = currentPlayer.controlPlayer.remake_cardToTrash(context);
            if (card == null || !currentPlayer.hand.contains(card)) {
                Util.playerError(currentPlayer, "Remake trash error, remaking a random card.");
                card = Util.randomCard(currentPlayer.hand);
            }

            int value = card.getCost(context) + 1;
            int debt = card.getDebtCost(context);
            boolean potion = card.costPotion();
            currentPlayer.hand.remove(card);
            currentPlayer.trash(card, this.getControlCard(), context);

            card = currentPlayer.controlPlayer.remake_cardToObtain(context, value, debt, potion);
            if (card != null) {
                if (card.getCost(context) != value || card.getDebtCost(context) != debt || card.costPotion() != potion) {
                    Util.playerError(currentPlayer, "Remake error, new card must cost exactly " + value + ".");
                } else {
                    if(currentPlayer.gainNewCard(card, this.getControlCard(), context) == null) {
                        Util.playerError(currentPlayer, "Remake error, pile is empty or card is not in the game.");
                    }
                }
            }
        }
    }
    
    private void tournament(Game game, MoveContext context, Player currentPlayer) {
        boolean opponentsRevealedProvince = false;
        boolean currentPlayerRevealed = false;

        for (Player player : game.getPlayersInTurnOrder()) {
            Card province = null;
            for (Card card : player.hand) {
                if (card.equals(Cards.province)) {
                    province = card;
                    break;
                }
            }

            if (province != null) {
                MoveContext playerContext = new MoveContext(context, game, player);

                if (player == currentPlayer) {
                    currentPlayerRevealed = currentPlayer.controlPlayer.tournament_shouldRevealProvince(playerContext);
                    if (currentPlayerRevealed) {
                        currentPlayer.reveal(province, this.getControlCard(), playerContext);
                        player.hand.remove(province);
                        currentPlayer.discard(province, this.getControlCard(), playerContext);
                    }
                } else {
                    if (player.controlPlayer.tournament_shouldRevealProvince(playerContext)) {
                        player.reveal(province, this.getControlCard(), playerContext);
                        opponentsRevealedProvince = true;
                    }
                }
            }
        }

        MoveContext playerContext = new MoveContext(context, game, currentPlayer);
        
        if (currentPlayerRevealed) {
            TournamentOption option = currentPlayer.controlPlayer.tournament_chooseOption(playerContext);

            if (option == TournamentOption.GainPrize) {
                Card prize = currentPlayer.controlPlayer.tournament_choosePrize(playerContext);
                if (prize != null && prize.is(Type.Prize, null)) {
                    currentPlayer.gainNewCard(prize, this, playerContext);
                } else {
                    Util.playerError(currentPlayer, "Tournament error, invalid prize");
                }
            } else if (option == TournamentOption.GainDuchy) {
                currentPlayer.gainNewCard(Cards.duchy, this, playerContext);
            }
        }

        if (!opponentsRevealedProvince) {
        	game.drawToHand(context, this, 1);
            context.addCoins(1);
        }
    }

    private void trustySteed(Game game, MoveContext context, Player currentPlayer) {
        TrustySteedOption[] options = currentPlayer.controlPlayer.trustySteed_chooseOptions(context);
        if (options == null || options.length != 2 || options[0] == options[1]) {
            Util.playerError(currentPlayer, "Trusty Steed options error, ignoring.");
        } else {
            // Trusty Steed must do options in the order listed.
            Arrays.sort(options);
            for (TrustySteedOption option : options) {
                if (option == TrustySteedOption.AddActions) {
                    context.actions += 2;
                } else if (option == TrustySteedOption.AddCards) {
                    for (int i = 0; i < 2; i++) {
                        game.drawToHand(context, this, 2 - i);
                    }
                } else if (option == TrustySteedOption.AddGold) {
                    context.addCoins(2);
                } else if (option == TrustySteedOption.GainSilvers) {
                    for (int i = 0; i < 4; i++) {
                        if(currentPlayer.gainNewCard(Cards.silver, this.getControlCard(), context) == null) {
                            break;
                        }
                    }
                    GameEvent event = new GameEvent(GameEvent.EventType.DeckPutIntoDiscardPile, (MoveContext) context);
                    game.broadcastEvent(event);

                    while (currentPlayer.getDeckSize() > 0) {
                        currentPlayer.discard(currentPlayer.deck.remove(0), this.getControlCard(), null);
                    }
                }
            }
        }
    }
    
    private void youngwitch(Game game, MoveContext context, Player currentPlayer) {
        Card[] cardsToDiscard = currentPlayer.controlPlayer.youngWitch_cardsToDiscard(context);
        boolean bad = false;

        if (cardsToDiscard == null) {
            bad = true;
        } else if (currentPlayer.hand.size() < 2 && cardsToDiscard.length != currentPlayer.hand.size()) {
            bad = true;
        } else if (cardsToDiscard.length != 2) {
            bad = true;
        } else {
            ArrayList<Card> copy = Util.copy(currentPlayer.hand);
            for (Card cardToKeep : cardsToDiscard) {
                if (!copy.remove(cardToKeep)) {
                    bad = true;
                    break;
                }
            }
        }

        if (bad) {
            if (currentPlayer.hand.size() >= 2) {
                Util.playerError(currentPlayer, "Young Witch discard error, just discarding the first 2.");
            }
            cardsToDiscard = new Card[Math.min(2, currentPlayer.hand.size())];
            for (int i = 0; i < cardsToDiscard.length; i++) {
                cardsToDiscard[i] = currentPlayer.hand.get(i);
            }
        }

        for (Card card : cardsToDiscard) {
            currentPlayer.discard(card, this.getControlCard(), null);
            currentPlayer.hand.remove(card);
        }

        for (Player targetPlayer : game.getPlayersInTurnOrder()) {
            if (targetPlayer != currentPlayer && !Util.isDefendedFromAttack(game, targetPlayer, this)) {
				
				Card baneCard = game.baneCard;
				CardPile banePile = game.getPile(baneCard);
				ArrayList<Card> baneCards = banePile.getTemplateCards();

				if (baneCards != null) {
					for (Card card : targetPlayer.hand) {
						if (baneCards.contains(card)) {
							baneCard = card;
							
						}
					}
				}
				
                if (targetPlayer.hand.contains(baneCard) && game.pileSize(Cards.curse) > 0 && targetPlayer.revealBane(context)) {
                    targetPlayer.reveal(baneCard, this.getControlCard(), new MoveContext(game, targetPlayer));
                } else {
                    targetPlayer.attacked(this.getControlCard(), context);
                    MoveContext targetContext = new MoveContext(game, targetPlayer);
                    targetContext.attackedPlayer = targetPlayer;
                    targetPlayer.gainNewCard(Cards.curse, this.getControlCard(), targetContext);
                }
            }
        }
    }
}
