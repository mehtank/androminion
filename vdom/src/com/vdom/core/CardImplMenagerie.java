package com.vdom.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.vdom.api.Card;
import com.vdom.api.GameEvent;

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
		case BountyHunter:
			bountyHunter(game, context, currentPlayer);
			break;
		case CamelTrain:
			camelTrain(game, context, currentPlayer);
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
		case Horse:
			horse(game, context, currentPlayer);
			break;
		case HuntingLodge:
			huntingLodge(game, context, currentPlayer);
			break;
		case Livery:
			livery(game, context, currentPlayer);
			break;
		case Paddock:
			paddock(game, context, currentPlayer);
			break;
		case Sanctuary:
			sanctuary(game, context, currentPlayer);
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
        case Commerce:
        	commerce(context);
        	break;
        case Desperation:
        	desperation(context);
        	break;
        case Enclave:
        	enclave(context);
        	break;
        case Gamble:
        	gamble(context);
        	break;
        case Populate:
        	populate(context);
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
        boolean cardOk = true;
        if (card == null || card.equals(toExile) || card.getCost(context) > cost || card.getDebtCost(context) > debt || card.costPotion() && !potion) {
        	Util.playerError(player, "Displace new card invalid, ignoring.");
        	return;
        }
        player.gainNewCard(card, this.getControlCard(), context);
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
	
	private void livery(Game game, MoveContext context, Player player) {
		context.liveryEffects++;
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
	
	private void populate(MoveContext context) {
		Card[] actionPiles = context.game.getCardsInGame(GetCardsInGameOptions.Placeholders, true, Type.Action);
		for(Card card : actionPiles)
			context.getPlayer().gainNewCard(card, this.getControlCard(), context);
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
