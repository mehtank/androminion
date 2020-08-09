package com.vdom.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.vdom.api.Card;
import com.vdom.api.GameEvent;
import com.vdom.core.Player.ScrapOption;

public class CardImplMenagerie extends CardImpl {
	private static final long serialVersionUID = 1L;

	public CardImplMenagerie(CardImpl.Builder builder) {
		super(builder);
	}

	protected CardImplMenagerie() { }

	@Override
	protected void additionalCardActions(Game game, MoveContext context, Player currentPlayer, boolean isThronedEffect) {
		switch(getKind()) {
		case AnimalFair:
			animalFair(game, context, currentPlayer);
			break;
		case Barge:
			barge(game, context, currentPlayer, isThronedEffect);
			break;
		case BlackCat:
			blackCat(game, context, currentPlayer);
			break;
		case BountyHunter:
			bountyHunter(game, context, currentPlayer);
			break;
		case CamelTrain:
			camelTrain(game, context, currentPlayer);
			break;
		case Cardinal:
			cardinal(game, context, currentPlayer);
			break;
		case Cavalry:
			cavalry(game, context, currentPlayer);
			break;
		case Coven:
			coven(game, context, currentPlayer);
			break;
		case Displace:
			displace(game, context, currentPlayer);
			break;
		case Gatekeeper:
			durationAttack(game, context, currentPlayer);
			break;
		case Groom:
			groom(game, context, currentPlayer);
			break;
		case Horse:
			horse(game, context, currentPlayer);
			break;
		case HuntingLodge:
			huntingLodge(game, context, currentPlayer);
			break;
		case Kiln:
			kiln(game, context, currentPlayer);
			break;
		case Livery:
			livery(game, context, currentPlayer);
			break;
		case Mastermind:
			mastermind(game, context, currentPlayer, isThronedEffect);
			break;
		case Paddock:
			paddock(game, context, currentPlayer);
			break;
		case Sanctuary:
			sanctuary(game, context, currentPlayer);
			break;
		case Scrap:
			scrap(game, context, currentPlayer);
			break;
		case Sleigh:
			sleigh(game, context, currentPlayer);
			break;
		case SnowyVillage:
			snowyVillage(game, context, currentPlayer);
			break;
		case Stockpile:
			stockpile(game, context, currentPlayer);
			break;
		case Supplies:
			supplies(game, context, currentPlayer);
			break;
		case Wayfarer:
			wayfarer(game, context, currentPlayer);
			break;
		case WayOfTheCamel:
			wayOfTheCamel(game, context, currentPlayer);
			break;
		case WayOfTheHorse:
			wayOfTheHorse(game, context, currentPlayer);
			break;
		case WayOfTheMole:
			wayOfTheMole(game, context, currentPlayer);
			break;
		case WayOfTheOwl:
			wayOfTheOwl(game, context, currentPlayer);
			break;
		case WayOfTheSeal:
			wayOfTheSeal(game, context, currentPlayer);
			break;
		case WayOfTheSquirrel:
			wayOfTheSquirrel(game, context, currentPlayer);
			break;
		case WayOfTheWorm:
			wayOfTheWorm(game, context, currentPlayer);
			break;
		case VillageGreen:
			villageGreen(game, context, currentPlayer, isThronedEffect);
			break;
		default:
			break;
		}
	}
	
	public void isBuying(MoveContext context) {
		super.isBuying(context);
        switch (this.getControlCard().getKind()) {
        case Alliance:
        	alliance(context);
        	break;
        case Bargain:
        	bargain(context);
        	break;
        case Commerce:
        	commerce(context);
        	break;
        case Demand:
        	demand(context);
        	break;
        case Desperation:
        	desperation(context);
        	break;
        case Enclave:
        	enclave(context);
        	break;
		case Enhance:
			enhance(context);
			break;
        case Gamble:
        	gamble(context);
        	break;
        case March:
        	march(context);
        	break;
        case Populate:
        	populate(context);
        	break;
        case Pursue:
        	pursue(context);
        	break;
        case Reap:
        	reap(context);
        	break;
        case Ride:
        	ride(context);
        	break;
        case SeizeTheDay:
        	seizeTheDay(context);
        	break;
        case Stampede:
        	stampede(context);
        	break;
        case Toil:
        	toil(context);
        	break;
        case Transport:
        	transport(context);
        	break;
        default:
            break;
        }
        
        // test if prince lost track of any cards
        context.player.princeCardLeftThePlay(context.player, context);
    }
	
	@Override
	public void isTrashed(MoveContext context) {
		Cards.Kind trashKind = this.getControlCard().getKind();
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
	
	private void animalFair(Game game, MoveContext context, Player player) {
		context.buys += game.emptyPiles();
	}
	
	private void bargain(MoveContext context) {
		Game game = context.game;
		Player currentPlayer = context.player;
		Card card = currentPlayer.controlPlayer.bargain_cardToObtain(context);
        if (card == null) return;
        if (card.is(Type.Victory) || card.getCost(context) > 5 || card.getDebtCost(context) > 0 || card.costPotion()) {
        	Util.playerError(currentPlayer, "Bargain new card invalid, ignoring.");
        } else {
        	currentPlayer.gainNewCard(card, this.getControlCard(), context);
        }
        
        ArrayList<Player> otherPlayers = new ArrayList<Player>();
    	for (Player player : context.game.getPlayersInTurnOrder()) {
            if (player != currentPlayer) {
            	otherPlayers.add(player);
            }
    	}
		
    	for (Player player : otherPlayers) {
            MoveContext playerContext = new MoveContext(game, player);
            player.gainNewCard(Cards.horse, getControlCard(), playerContext);
        }
	}
	
	private void barge(Game game, MoveContext context, Player player, boolean isThronedEffect) {
		if (player.controlPlayer.barge_shouldReceiveNow(context)) {
			for (int i = 0; i < 3; ++i) {
            	game.drawToHand(context, this, 3 - i);
            }
			context.buys += 1;
			return;
	    }
		player.addStartTurnDurationEffect(this, 1, isThronedEffect);
	}
	
	private void blackCat(Game game, MoveContext context, Player currentPlayer) {
		if (game.getCurrentPlayer() == context.player) return;

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
        }
	}
	
	private void bountyHunter(Game game, MoveContext context, Player player) {
		CardList hand = player.getHand();
		if(hand.size() == 0)
			return;
        Card exileCard = player.controlPlayer.bountyHunter_cardToExile(context);
        if (exileCard == null || !player.hand.contains(exileCard)) {
            Util.playerError(player, "Bounty Hunter card to exile invalid, picking one");
            exileCard = hand.get(0);
        }
        boolean hasCopyInExile = player.hasCopyInExile(exileCard);
        player.exileFromHand(exileCard, this.getControlCard(), context);
        if (!hasCopyInExile) {
        	context.addCoins(3, Cards.bountyHunter);
        }
	}
	
	private void camelTrain(Game game, MoveContext context, Player player) {
		Card[] possibleCards = game.getCardsInGame(GetCardsInGameOptions.TopOfPiles, true, Type.Victory, true);
		if (possibleCards.length == 0) return;
		
		Card cardToExile = player.controlPlayer.camelTrain_cardToExile(context);
        CardPile pile = null;
        if (cardToExile == null) {
        	Util.playerError(player, "Camel Train exile null error, picking first card.");
        	cardToExile = possibleCards[0];
        }
        pile = game.getPile(cardToExile);
        if (pile == null || !cardToExile.equals(pile.topCard()) || !pile.isSupply() || cardToExile.is(Type.Victory)) {
            Util.playerError(player, "Camel Train exile error, picking first card.");
            cardToExile = possibleCards[0];
        }
        player.exileFromSupply(cardToExile, getControlCard(), context);
	}
	
	private void cardinal(Game game, MoveContext context, Player currentPlayer) {
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
            ArrayList<Card> canExile = new ArrayList<Card>();

            List<Card> cardsToDiscard = new ArrayList<Card>();
            for (int i = 0; i < 2; i++) {
                Card card = context.game.draw(playerContext, this, 2 - i);

                if (card != null) {
                    currentPlayer.reveal(card, this.getControlCard(), playerContext);
                    int cardCost = card.getCost(context);

                    if (card.getDebtCost(context) == 0 && !card.costPotion() && cardCost >= 3 && cardCost <= 6) {
                        canExile.add(card);
                    } else {
                        cardsToDiscard.add(card);
                    }
                }
            }
            Card cardToExile = null;
            if (canExile.size() == 1) {
                cardToExile = canExile.get(0);
            } else if (canExile.size() == 2) {
                if (canExile.get(0).equals(canExile.get(1))) {
                    cardToExile = canExile.get(0);
                    cardsToDiscard.add(canExile.remove(1));
                } else {
                    cardToExile = player.cardinal_cardToExile(playerContext, canExile);
                }

                for (Card card : canExile) {
                    if (!card.equals(cardToExile)) {
                    	cardsToDiscard.add(card);
                    }
                }
            }          
            if (cardToExile != null) {
            	player.exile(cardToExile, this.getControlCard(), playerContext);
            }
            for (Card c: cardsToDiscard) {
                player.discard(c, this.getControlCard(), playerContext);
            }
    	}
	}
	
	private void cavalry(Game game, MoveContext context, Player player) {
		player.gainNewCard(Cards.horse, getControlCard(), context);
		player.gainNewCard(Cards.horse, getControlCard(), context);
	}
	
	private void coven(Game game, MoveContext context, Player currentPlayer) {
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
            if (!player.exileFromSupply(Cards.curse, this.getControlCard(), playerContext)) {
            	ArrayList<Card> toDiscard = new ArrayList<Card>();
            	Iterator<Card> iterator = player.exile.iterator();
            	while(iterator.hasNext()) {
            		Card c = iterator.next();
            		if (c.equals(Cards.curse)) {
            			iterator.remove();
            			toDiscard.add(c);
            		}
            	}
            	for(Card card: toDiscard) {
            		player.discard(card, Cards.coven, playerContext);
            	}
            }
        }
	}
	
	private void displace(Game game, MoveContext context, Player player) {
		if (player.getHand().size() == 0)
			return;
		Card toExile = player.controlPlayer.displace_cardToExile(context);
		if (toExile == null || !player.getHand().contains(toExile)) {
    		Util.playerError(player, "Displace error, invalid card to exile, picking first card.");
    		toExile = player.hand.get(0);
    	}
		toExile = player.hand.get(toExile);
		player.exileFromHand(toExile, this, context);
		
		int cost = toExile.getCost(context);
		int debt = toExile.getDebtCost(context);
		boolean potion = toExile.costPotion();
		
        cost += 2;

        Card card = player.controlPlayer.displace_cardToObtain(context, toExile, cost, debt, potion);
        if (card == null || card.equals(toExile) || card.getCost(context) > cost || card.getDebtCost(context) > debt || card.costPotion() && !potion) {
        	Util.playerError(player, "Displace new card invalid, ignoring.");
        	return;
        }
        player.gainNewCard(card, this.getControlCard(), context);
	}
	
	private void groom(Game game, MoveContext context, Player player) {
		Card card = player.controlPlayer.groom_cardToObtain(context);
        if (card != null && card.getCost(context) <= 4 && card.getDebtCost(context) == 0 && !card.costPotion()) {
            if (player.gainNewCard(card, this.getControlCard(), context).equals(card)) {
                if (card.is(Type.Action, player)) {
                	player.gainNewCard(Cards.horse, this.getControlCard(), context);
                }
                if (card.is(Type.Treasure, player)) {
                	player.gainNewCard(Cards.silver, this.getControlCard(), context);
                }
                if (card.is(Type.Victory, player)) {
                	game.drawToHand(context, this, 1);
                	context.addActions(1, this);
                }
            }
        }
	}
	
	private void horse(Game game, MoveContext context, Player player) {
		Card card = this.getControlCard();
    	int idx = player.playedCards.indexOf(card.getId());
    	if (idx == -1) return;
    	card = player.playedCards.remove(idx); 
    	CardPile pile = game.getGamePile(card);
        pile.addCard(card);	
	}
	
	private void huntingLodge(Game game, MoveContext context, Player player) {
		if (player.getHand().size() == 0) return;
		if (!player.controlPlayer.huntingLodge_shouldDiscardHand(context)) {
			return;
		}
        while (!player.getHand().isEmpty()) {
            player.discard(player.getHand().remove(0), this.getControlCard(), context);
        }
		for (int i = 0; i < 5; ++i) {
			game.drawToHand(context, this.getControlCard(), 5 - i);
		}
	}
	
	private void kiln(Game game, MoveContext context, Player player) {
		context.kilnEffect = true;
	}
	
	private void livery(Game game, MoveContext context, Player player) {
		context.liveryEffects++;
	}
	
	private void mastermind(Game game, MoveContext context, Player player, boolean isThronedEffect) {
		player.addStartTurnDurationEffect(this, 1, isThronedEffect);
	}
	
	private void paddock(Game game, MoveContext context, Player player) {
		player.gainNewCard(Cards.horse, getControlCard(), context);
		player.gainNewCard(Cards.horse, getControlCard(), context);
		context.addActions(game.emptyPiles(), this);
	}
	
	private void sanctuary(Game game, MoveContext context, Player player) {
		if (player.getHand().size() == 0)
			return;
		Card toExile = player.controlPlayer.sanctuary_cardToExile(context);
		if (toExile == null) return;
		if (!player.getHand().contains(toExile)) {
    		Util.playerError(player, "Sanctuary error, invalid card to exile, ignoring.");
    	} else {
    		toExile = player.hand.get(toExile);
    		player.exileFromHand(toExile, this, context);
    	}
	}
	
	private void scrap(Game game, MoveContext context, Player player) {
		if (player.getHand().size() == 0) return;

        Card card = player.controlPlayer.scrap_cardToTrash(context);
        if (card == null) {
            Util.playerError(player, "Scrap errror, trash random card.");
            card = Util.randomCard(player.hand);
        }
        player.trashFromHand(card, this.getControlCard(), context);
        int numOptions = Math.min(6, card.getCost(context));
        if (numOptions > 0) {
            ScrapOption[] options = null;

            if (numOptions >= 6) {
                options = ScrapOption.values();
            } else {
                options = player.controlPlayer.scrap_chooseOptions(context, ScrapOption.values(), numOptions);
            }

            if (options == null || options.length != numOptions /*TODO CHECK THAT THERE ARE NO DUPLICATES */) {
                Util.playerError(player, "Scrap Error, Ignoring");
            } else {
            	ArrayList<ScrapOption> optionsList = new ArrayList<Player.ScrapOption>(Arrays.asList(options));
                for (ScrapOption option : ScrapOption.values()) {
                	if (!optionsList.contains(option)) continue;
                    switch (option) {
                        case AddAction:
                            context.addActions(1, this);
                            break;
                        case AddBuy:
                            context.buys++;
                            break;
                        case AddCoin:
                            context.addCoins(1, this.getControlCard());
                            break;
                        case AddCard:
                        	game.drawToHand(context, this.getControlCard(), 1);
                        	break;
                        case GainSilver:
                            player.gainNewCard(Cards.silver, this.getControlCard(), context);
                            break;
                        case GainHorse:
                            player.gainNewCard(Cards.horse, this.getControlCard(), context);
                            break;
                    }
                }
            }
        }
	}
	
	private void sleigh(Game game, MoveContext context, Player player) {
		player.gainNewCard(Cards.horse, getControlCard(), context);
		player.gainNewCard(Cards.horse, getControlCard(), context);
	}
		
	private void snowyVillage(Game game, MoveContext context, Player player) {
		context.ignorePlusActions = true;
	}
	
	private void stockpile(Game game, MoveContext context, Player player) {
		player.exileFromPlay(this, Cards.stockpile, context);
	}
	
	private void supplies(Game game, MoveContext context, Player player) {
		player.gainNewCard(Cards.horse, getControlCard(), context);
	}
	
	private void wayfarer(Game game, MoveContext context, Player player) {
		if (game.getPile(Cards.silver).isEmpty()) return;
		if (player.controlPlayer.wayfarer_shouldGainSilver(context))
			player.gainNewCard(Cards.silver, getControlCard(), context);
	}
	
	private void villageGreen (Game game, MoveContext context, Player player, boolean isThronedEffect) {
		if (player.controlPlayer.villageGreen_shouldReceiveNow(context)) {
        	game.drawToHand(context, this, 1);
            context.addActions(2);
			return;
	    }
		player.addStartTurnDurationEffect(this, 1, isThronedEffect);
	}
	
	
	private void alliance(MoveContext context) {
		context.getPlayer().gainNewCard(Cards.province, this.getControlCard(), context);
		context.getPlayer().gainNewCard(Cards.duchy, this.getControlCard(), context);
		context.getPlayer().gainNewCard(Cards.estate, this.getControlCard(), context);
		context.getPlayer().gainNewCard(Cards.gold, this.getControlCard(), context);
		context.getPlayer().gainNewCard(Cards.silver, this.getControlCard(), context);
		context.getPlayer().gainNewCard(Cards.copper, this.getControlCard(), context);
	}
		
	private void commerce(MoveContext context) {
		int numGolds = new HashSet<Card>(context.game.getCardsObtainedByPlayer()).size();
		for (int i = 0; i < numGolds; ++i)
			context.getPlayer().gainNewCard(Cards.gold, this.getControlCard(), context);
	}
	
	private void demand(MoveContext context) {
		Player currentPlayer = context.player;
		currentPlayer.gainNewCard(Cards.horse, Cards.demand, context);
		Card card = currentPlayer.controlPlayer.demand_cardToObtain(context);
        if (card == null) return;
        if (card.getCost(context) > 4 || card.getDebtCost(context) > 0 || card.costPotion()) {
        	Util.playerError(currentPlayer, "Demand new card invalid, ignoring.");
        } else {
        	currentPlayer.gainNewCard(card, Cards.demand, context);
        }
	}
		
	private void desperation(MoveContext context) {
		context.cantBuy.add(this); // once per turn
		if (context.game.getPile(Cards.curse).isEmpty()) return;
		if (context.player.controlPlayer.desperation_shouldGainCurse(context)) {
			if (Cards.curse.equals(context.player.gainNewCard(Cards.curse, Cards.desperation, context))) {
				context.buys++;
				context.addCoins(2, Cards.desperation);
			}
		}
	}
	
	private void enclave(MoveContext context) {
		context.player.gainNewCard(Cards.gold, Cards.enclave, context);
		context.player.exileFromSupply(Cards.duchy, Cards.enclave, context);
	}
	
	private void enhance(MoveContext context) {
		Player player = context.player;
		if (player.getHand().size() == 0)
			return;
		int nonVictoryCards = 0;
		for (Card c : player.getHand()) {
			if (!c.is(Type.Victory))
				nonVictoryCards++;
		}
		if (nonVictoryCards == 0) return;
		Card toTrash = player.controlPlayer.enhance_cardToTrash(context);
		if (toTrash == null) return;
		if (!player.getHand().contains(toTrash) || toTrash.is(Type.Victory)) {
    		Util.playerError(player, "Enhance error, invalid card to trash, skipping.");
    		return;
    	}
		toTrash = player.hand.get(toTrash);
		player.trashFromHand(toTrash, Cards.enhance, context);
		
		int cost = toTrash.getCost(context);
		int debt = toTrash.getDebtCost(context);
		boolean potion = toTrash.costPotion();
		
        cost += 2;

        Card card = player.controlPlayer.enhance_cardToObtain(context, cost, debt, potion);
        if (card == null || card.getCost(context) > cost || card.getDebtCost(context) > debt || card.costPotion() && !potion) {
        	Util.playerError(player, "Enhance new card invalid, ignoring.");
        	return;
        }
        player.gainNewCard(card, this.getControlCard(), context);
	}
	
	private void gamble(MoveContext context) {
		Game game = context.game;
		Player player = context.player;
		Card c = game.draw(context, Cards.gamble, 1);
        if (c != null) {
        	player.reveal(c, this.getControlCard(), context);
            if ((c.is(Type.Action, player) || c.is(Type.Treasure)) && player.controlPlayer.gamble_shouldPlayCard(context, c)) {
            	context.freeActionInEffect++;
                c.play(game, context, false);
                context.freeActionInEffect--;
            } else {
            	player.discard(c, this.getControlCard(), context);
            }
            
        }
	}
	private void march(MoveContext context) {
		Player player = context.player;
		boolean hasActions = false;
		for (Card c : player.discard) {
			if (c.is(Type.Action)) {
				hasActions = true;
				break;
			}
		}
		if (!hasActions)
			return;
	    Card card = player.controlPlayer.march_actionToPlay(context);
	    if (card == null) return;
	    
	    int idx = player.discard.indexOf(card);
        if (idx >= 0) {
        	card = player.discard.remove(idx);
        	context.freeActionInEffect++;
        	card.play(context.game, context, false, false);
        	context.freeActionInEffect--;
        } else {
        	Util.playerError(player, "March card not in discard, ignoring.");
        }
	}
	
	private void populate(MoveContext context) {
		Card[] actionPiles = context.game.getCardsInGame(GetCardsInGameOptions.Placeholders, true, Type.Action);
		for(Card card : actionPiles)
			context.getPlayer().gainNewCard(card, this.getControlCard(), context);
	}
	
	private void pursue(MoveContext context) {
		Player player = context.player;
		List<Card> options = new ArrayList<Card>(player.getDistinctCards());
		if (options.size() == 0) return;
        Collections.sort(options, new Util.CardNameComparator());
        Card named = player.controlPlayer.pursue_cardToPick(context, options);
        player.namedCard(named, this.getControlCard(), context);
        
        ArrayList<Card> matches = new ArrayList<Card>();
        ArrayList<Card> toDiscard = new ArrayList<Card>();
        for (int i = 0; i < 4; ++i) {
			Card c = context.game.draw(context, Cards.pursue, 4 - i);
			if (c == null) break;
			player.reveal(c, Cards.pursue, context);
			if (c.equals(named)) {
				matches.add(c);
			} else {
				toDiscard.add(c);
			}
		}
        for (Card c : matches) {
        	player.putOnTopOfDeck(c, context, false);
        	GameEvent event = new GameEvent(GameEvent.EventType.CardOnTopOfDeck, context);
            event.card = c;
            event.responsible = Cards.pursue;
            context.game.broadcastEvent(event);
        }
        for (Card c : toDiscard) {
        	player.discard(c, Cards.pursue, context);
        }
	}
	
	private void reap(MoveContext context) {
		context.player.gainNewCard(Cards.gold, Cards.reap, context);
	}
	
	private void ride(MoveContext context) {
		context.getPlayer().gainNewCard(Cards.horse, this.getControlCard(), context);
	}
	
	private void seizeTheDay(MoveContext context) {
		context.seizeTheDayBought = true;
		context.cantBuy.add(this); // once per game
		context.player.boughtSeizeTheDay = true;
	}
	
	private void stampede(MoveContext context) {
		if (context.countCardsInPlay() <= 5) {
			for (int i = 0; i < 5; ++i)
				context.getPlayer().gainNewCard(Cards.horse, Cards.stampede, context);
		}
	}
	
	private void toil(MoveContext context) {
		Player player = context.getPlayer();
		ArrayList<Card> validCards = new ArrayList<Card>();
		for (Card c : player.hand) {
			if (c.is(Type.Action, player)) {
				validCards.add(c);
			}
		}
		if (validCards.isEmpty()) return;
		Card card = player.controlPlayer.toil_cardToPlay(context);
		if (card == null) return;
		if (!validCards.contains(card)) {
			Util.playerError(player, "Toil error, invalid card selected, ignoring");
			return;
		}
		context.freeActionInEffect++;
        card.play(context.game, context, true);
        context.freeActionInEffect--;
	}
	
	private void transport(MoveContext context) {
		Player player = context.getPlayer();
		Player.TransportOption option = player.controlPlayer.transport_selectChoice(context, Player.TransportOption.values());
        if (option == null) {
            Util.playerError(player, "Transport option error, choosing automatically");
            option = Player.TransportOption.ExileActionFromSupply;
        }
        switch (option) {
        case ExileActionFromSupply:
        	if (context.game.getCardsInGame(GetCardsInGameOptions.TopOfPiles, true, Type.Action).length == 0)
        		return;
            Card cardToExile = player.controlPlayer.transport_cardToExile(context);
            CardPile pile = null;
            if (cardToExile != null) {
                pile = context.game.getPile(cardToExile);
                if (pile == null || !cardToExile.equals(pile.topCard()) || !pile.isSupply() || !cardToExile.is(Type.Action)) {
                    Util.playerError(player, "Transport exile error, exiling nothing.");
                    return;
                }
                player.exileFromSupply(cardToExile, this.getControlCard(), context);
            }
            break;
        case TopdeckActionFromExile:
        	Set<Card> possibleCards = new HashSet<Card>();
        	for(Card c : player.exile) {
        		if (c.is(Type.Action)) {
        			possibleCards.add(c);
        		}
        	}
        	if (possibleCards.isEmpty())
        		return;
            Card cardToTopdeck = player.controlPlayer.transport_cardToTopdeckFromExile(context, possibleCards.toArray(new Card[possibleCards.size()]));
            if (cardToTopdeck == null || !player.exile.contains(cardToTopdeck) || !cardToTopdeck.is(Type.Action)) {
                Util.playerError(player, "Transport top deck card choice error, doing nothing");
                return;
            }

            cardToTopdeck = player.exile.remove(player.exile.indexOf(cardToTopdeck));
            player.putOnTopOfDeck(cardToTopdeck);
            GameEvent event = new GameEvent(GameEvent.EventType.CardOnTopOfDeck, context);
            event.card = cardToTopdeck;
            event.responsible = this.getControlCard();
            context.game.broadcastEvent(event);
            break;
        }
	}
	
	private void wayOfTheCamel(Game game, MoveContext context, Player player) {
		player.exileFromSupply(Cards.gold, Cards.wayOfTheCamel, context);
	}
	
	private void wayOfTheHorse(Game game, MoveContext context, Player player) {
		Card card = this.getControlCard();
    	int idx = player.playedCards.indexOf(card.getId());
    	if (idx == -1) return;
    	CardPile pile = game.getGamePile(card);
    	if (!pile.isRealPile()) return;
		card = player.playedCards.remove(idx);
		pile.addCard(card);
	}
	
	private void wayOfTheMole(Game game, MoveContext context, Player player) {
		if (player.getHand().size() > 0) {
            while (!player.getHand().isEmpty()) {
                player.discard(player.getHand().remove(0), this.getControlCard(), context);
            }
        }
		for (int i = 0; i < 3; ++i) {
			game.drawToHand(context, this.getControlCard(), 3 - i);
		}
	}
	
	private void wayOfTheOwl(Game game, MoveContext context, Player player) {
		int cardsToDraw = 6 - player.hand.size();
    	if (cardsToDraw > 0 && player.getMinusOneCardToken()) {
        	game.drawToHand(context, Cards.wayOfTheOwl, -1);
        }
    	for (int i = 0; i < cardsToDraw; ++i) {
    		if(!game.drawToHand(context, Cards.wayOfTheOwl, cardsToDraw - i))
                break;
    	}
	}
	
	private void wayOfTheSeal(Game game, MoveContext context, Player player) {
		context.wayOfTheSealPlayed = true;
	}
	
	private void wayOfTheSquirrel(Game game, MoveContext context, Player player) {
		player.wayOfTheSquirrelDraw += 2;
	}
	
	private void wayOfTheWorm(Game game, MoveContext context, Player player) {
		player.exileFromSupply(Cards.estate, Cards.wayOfTheWorm, context);
	}
}
