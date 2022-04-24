package com.vdom.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import com.vdom.api.Card;
import com.vdom.api.GameEvent;
import com.vdom.core.MoveContext.TurnPhase;
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
    public void followInstructions(Game game, MoveContext context, Card responsible, Player currentPlayer, boolean isThronedEffect, PlayContext playContext) {
        super.followInstructions(game, context, responsible, currentPlayer, isThronedEffect, playContext);
		switch(getKind()) {
		case BagofGold:
            currentPlayer.gainNewCard(Cards.gold, this, context);
            break;
		case Diadem:
			diadem(game, context, currentPlayer);
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
            harvest(game, context, currentPlayer, playContext);
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
            menagerie(game, context, currentPlayer, playContext);
            break;
        case Remake:
            remake(context, currentPlayer);
            break;
        case Tournament:
            tournament(game, context, currentPlayer, playContext);
            break;
        case TrustySteed:
            trustySteed(game, context, currentPlayer, playContext);
            break;
        case YoungWitch:
            youngwitch(game, context, currentPlayer);
            break;
		default:
			break;
        
		}
	}
	
	private void diadem(Game game, MoveContext context, Player currentPlayer) {
		// allow using Villagers first if in action phase for extra actions to feed Diadem if needed
		if (context.phase == TurnPhase.Action) {
			game.useVillagersForActions(currentPlayer, context);
		}
		context.addCoins(context.getActionsLeft());
	}
	
	private void farmingVillage(Game game, MoveContext context, Player currentPlayer) {
        Card draw = null;

        ArrayList<Card> toDiscard = new ArrayList<Card>();

        while ((draw = game.draw(context, Cards.farmingVillage, -1)) != null && !(draw.is(Type.Action, currentPlayer)) && !(draw.is(Type.Treasure, currentPlayer, context))) {
            toDiscard.add(draw);
        }

        while (!toDiscard.isEmpty()) {
            Card c = toDiscard.remove(0);
            currentPlayer.reveal(c, this, context);
            currentPlayer.discard(c, this, context);
        }

        if (draw != null) {
            currentPlayer.reveal(draw, this, context);
            currentPlayer.hand.add(draw);
        }
    }
	
	private void followers(Game game, MoveContext context, Player currentPlayer) {
        currentPlayer.gainNewCard(Cards.estate, this, context);

        for (Player player : game.getPlayersInTurnOrder()) {
            if (player != currentPlayer && !Util.isDefendedFromAttack(game, player, this)) {
                player.attacked(this, context);
                MoveContext playerContext = new MoveContext(game, player);
                playerContext.attackedPlayer = player;
                player.gainNewCard(Cards.curse, this, playerContext);

                int keepCardCount = 3;
                if (player.hand.size() > keepCardCount) {
                    Card[] cardsToKeep = player.controlPlayer.followers_attack_cardsToKeep(playerContext);
                    player.discardRemainingCardsFromHand(context, cardsToKeep, this, keepCardCount);
                }
            }
        }
    }

    private void fortuneTeller(Game game, MoveContext context, Player currentPlayer) {
        for (Player player : game.getPlayersInTurnOrder()) {
            if (player != currentPlayer && !Util.isDefendedFromAttack(game, player, this)) {
                player.attacked(this, context);
                MoveContext playerContext = new MoveContext(game, player);
                playerContext.attackedPlayer = player;
                ArrayList<Card> cardsToDiscard = new ArrayList<Card>();

                Card draw = null;
                while ((draw = game.draw(playerContext, Cards.fortuneTeller, -1)) != null && !(draw.is(Type.Victory, player)) && !(draw.is(Type.Curse, player))) {
                    player.reveal(draw, this, playerContext);
                    cardsToDiscard.add(draw);
                }

                if (draw != null) {
                    player.reveal(draw, this, playerContext);
                    player.putOnTopOfDeck(draw, playerContext, true);
                }

                for(Card card : cardsToDiscard) {
                    player.discard(card, this, playerContext);
                }
            }
        }
    }

    private void hamlet(MoveContext context, Player currentPlayer) {
        Card forAction = currentPlayer.controlPlayer.hamlet_cardToDiscardForAction(context);
        if (forAction != null) {
            currentPlayer.hand.remove(forAction);
            currentPlayer.discard(forAction, this, context);
            context.addActions(1, this);
        }

        Card forBuy = currentPlayer.controlPlayer.hamlet_cardToDiscardForBuy(context);
        if (forBuy != null) {
            currentPlayer.hand.remove(forBuy);
            currentPlayer.discard(forBuy, this, context);
            context.buys++;
        }
    }

    private void harvest(Game game, MoveContext context, Player currentPlayer, PlayContext playContext) {
        HashSet<String> cardNames = new HashSet<String>();
        List<Card> cardToDiscard = new ArrayList<Card>();
        for (int i = 0; i < 4; i++) {
            Card draw = game.draw(context, Cards.harvest, 4 - i);
            if (draw == null) {
                break;
            }

            cardNames.add(draw.getName());
            currentPlayer.reveal(draw, this, context);
            cardToDiscard.add(draw);
        }
        for (Card c: cardToDiscard) {
            currentPlayer.discard(c, this, context);
        }

        context.addCoins(cardNames.size(), this, playContext);
    }
    
    private void hornOfPlenty(MoveContext context, Player player, Game game) {
        int maxCost = context.countUniqueCardsInPlay();
        Card toObtain = player.controlPlayer.hornOfPlenty_cardToObtain(context, maxCost);
        if (toObtain != null) {
            // check cost
            if (toObtain.getCost(context) <= maxCost && toObtain.getDebtCost(context) == 0 && !toObtain.costPotion()) {
            	Card gained = player.gainNewCard(toObtain, this, context);
            	if (gained != null && gained.equals(gained) && gained.is(Type.Victory, player)) {
            		player.trashSelfFromPlay(this, context);
            	}
            }
        }
    }
    
    private void huntingParty(Game game, MoveContext context, Player currentPlayer) {
        HashSet<String> cardNames = new HashSet<String>();

        for (int i = 0; i < currentPlayer.hand.size(); i++) {
            Card card = currentPlayer.hand.get(i);
            cardNames.add(card.getName());
            currentPlayer.reveal(card, this, context);
        }

        ArrayList<Card> toDiscard = new ArrayList<Card>();

        Card draw = null;
        while ((draw = game.draw(context, Cards.huntingParty, -1)) != null && cardNames.contains(draw.getName())) {
            currentPlayer.reveal(draw, this, context);
            toDiscard.add(draw);
        }

        if (draw != null) {
            currentPlayer.reveal(draw, this, context);
            currentPlayer.hand.add(draw);
        }

        while (!toDiscard.isEmpty()) {
            currentPlayer.discard(toDiscard.remove(0), this, context);
        }
    }

    private void jester(Game game, MoveContext context, Player currentPlayer) {
        for (Player targetPlayer : game.getPlayersInTurnOrder()) {
            if (targetPlayer != currentPlayer && !Util.isDefendedFromAttack(game, targetPlayer, this)) {
                targetPlayer.attacked(this, context);
                MoveContext targetContext = new MoveContext(game, targetPlayer);
                targetContext.attackedPlayer = targetPlayer;

                Card draw = game.draw(targetContext, Cards.jester, 1);

                if (draw == null) 
                {
                    continue;
                }
                
                targetPlayer.reveal(draw, this, targetContext);
                targetPlayer.discard(draw, this, targetContext);

                if (draw.is(Type.Victory, targetPlayer)) {
                    targetPlayer.gainNewCard(Cards.curse, this, targetContext);
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
                        toGainContext.getPlayer().gainNewCard(draw, this, toGainContext);
                    }
                }
            }
        }
    }
    
    private void menagerie(Game game, MoveContext context, Player currentPlayer, PlayContext playContext) {
        HashSet<String> cardNames = new HashSet<String>();
        boolean distinct = true;

        for (int i = 0; i < currentPlayer.hand.size(); i++) {
            Card card = currentPlayer.hand.get(i);
            currentPlayer.reveal(card, this, context);
            distinct &= cardNames.add(card.getName());
        }

        int numCards = distinct ? 3 : 1;
        for (int i = 0; i < numCards; i++) {
            game.drawToHand(context, this, numCards - i, playContext);
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
            currentPlayer.trashFromHand(card, this, context);

            card = currentPlayer.controlPlayer.remake_cardToObtain(context, value, debt, potion);
            if (card != null) {
                if (card.getCost(context) != value || card.getDebtCost(context) != debt || card.costPotion() != potion) {
                    Util.playerError(currentPlayer, "Remake error, new card must cost exactly " + value + ".");
                } else {
                    if(currentPlayer.gainNewCard(card, this, context) == null) {
                        Util.playerError(currentPlayer, "Remake error, pile is empty or card is not in the game.");
                    }
                }
            }
        }
    }
    
    private void tournament(Game game, MoveContext context, Player currentPlayer, PlayContext playContext) {
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
                        currentPlayer.reveal(province, this, playerContext);
                        player.hand.remove(province);
                        currentPlayer.discard(province, this, playerContext);
                    }
                } else {
                    if (player.controlPlayer.tournament_shouldRevealProvince(playerContext)) {
                        player.reveal(province, this, playerContext);
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
        	game.drawToHand(context, this, 1, playContext);
            context.addCoins(1, this, playContext);
        }
    }

    private void trustySteed(Game game, MoveContext context, Player currentPlayer, PlayContext playContext) {
        TrustySteedOption[] options = currentPlayer.controlPlayer.trustySteed_chooseOptions(context);
        if (options == null || options.length != 2 || options[0] == options[1]) {
            Util.playerError(currentPlayer, "Trusty Steed options error, ignoring.");
        } else {
            // Trusty Steed must do options in the order listed.
            Arrays.sort(options);
            for (TrustySteedOption option : options) {
                if (option == TrustySteedOption.AddActions) {
                    context.addActions(2, this);
                } else if (option == TrustySteedOption.AddCards) {
                    for (int i = 0; i < 2; i++) {
                        game.drawToHand(context, this, 2 - i, playContext);
                    }
                } else if (option == TrustySteedOption.AddGold) {
                    context.addCoins(2, this, playContext);
                } else if (option == TrustySteedOption.GainSilvers) {
                    for (int i = 0; i < 4; i++) {
                        if(currentPlayer.gainNewCard(Cards.silver, this, context) == null) {
                            break;
                        }
                    }
                    GameEvent event = new GameEvent(GameEvent.EventType.DeckPutIntoDiscardPile, (MoveContext) context);
                    game.broadcastEvent(event);

                    currentPlayer.deckToDiscard(context, this);
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
            currentPlayer.discard(card, this, context);
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
                    targetPlayer.reveal(baneCard, this, new MoveContext(game, targetPlayer));
                } else {
                    targetPlayer.attacked(this, context);
                    MoveContext targetContext = new MoveContext(game, targetPlayer);
                    targetContext.attackedPlayer = targetPlayer;
                    targetPlayer.gainNewCard(Cards.curse, this, targetContext);
                }
            }
        }
    }
}
