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

public class CardImplNocturne extends CardImpl {

	private static final long serialVersionUID = 1L;

	public CardImplNocturne(CardImpl.Builder builder) {
		super(builder);
	}

	protected CardImplNocturne() { }
	
	protected void additionalCardActions(Game game, MoveContext context, Player currentPlayer, boolean isThronedEffect) {
		switch(getKind()) {
		case BadOmens:
			badOmens(game, context, currentPlayer);
			break;
		case Bard:
			bard(game, context, currentPlayer);
			break;
		case Bat:
			bat(game, context, currentPlayer);
			break;
		case Changeling:
            changeling(game, context, currentPlayer);
            break;
		case Cobbler:
            cobbler(game, context, currentPlayer, isThronedEffect);
            break;
		case Conclave:
            conclave(game, context, currentPlayer);
            break;
		case Crypt:
            crypt(game, context, currentPlayer, isThronedEffect);
            break;
		case CursedGold:
            cursedGold(game, context, currentPlayer);
            break;
		case CursedVillage:
            cursedVillage(game, context, currentPlayer);
            break;
		case Delusion:
            delusion(game, context, currentPlayer);
            break;
		case DevilsWorkshop:
            devilsWorkshop(game, context, currentPlayer);
            break;
		case Druid:
            druid(game, context, currentPlayer);
            break;
		case Envy:
			envy(game, context, currentPlayer);
			break;
		case Exorcist:
			exorcist(game, context, currentPlayer);
			break;
		case Famine:
			famine(game, context, currentPlayer);
			break;
		case Fear:
			fear(game, context, currentPlayer);
			break;
		case Fool:
			fool(game, context, currentPlayer);
			break;
		case Ghost:
			ghost(game, context, currentPlayer);
			break;
		case Goat:
			goat(game, context, currentPlayer);
			break;
		case Greed:
			greed(game, context, currentPlayer);
			break;
		case Guardian:
			guardian(game, context, currentPlayer);
			break;
		case Haunting:
			haunting(game, context, currentPlayer);
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
		case Locusts:
			locusts(game, context, currentPlayer);
			break;
		case LostInTheWoods:
			lostInTheWoods(game, context, currentPlayer);
			break;
		case LuckyCoin:
			luckyCoin(game, context, currentPlayer);
			break;
		case MagicLamp:
			magicLamp(game, context, currentPlayer);
			break;
		case Misery:
			misery(game, context, currentPlayer);
			break;
		case Monastery:
			monastery(game, context, currentPlayer);
			break;
		case Necromancer:
			necromancer(game, context, currentPlayer);
			break;
		case NightWatchman:
			nightWatchman(game, context, currentPlayer);
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
		case Poverty:
			poverty(game, context, currentPlayer);
			break;
		case SacredGrove:
			sacredGrove(game, context, currentPlayer);
			break;
		case SecretCave:
			secretCave(game, context, currentPlayer, isThronedEffect);
			break;
		case Shepherd:
			shepherd(game, context, currentPlayer);
			break;
		case Raider:
			raider(game, context, currentPlayer);
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
		case Tormentor:
			tormentor(game, context, currentPlayer);
			break;
		case Tracker:
			tracker(game, context, currentPlayer);
			break;
		case TragicHero:
			tragicHero(game, context, currentPlayer);
			break;
		case Vampire:
			vampire(game, context, currentPlayer);
			break;
		case War:
			war(game, context, currentPlayer);
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
		case ZombieApprentice:
			zombieApprentice(game, context, currentPlayer);
			break;
		case ZombieMason:
			zombieMason(game, context, currentPlayer);
			break;
		case ZombieSpy:
			zombieSpy(game, context, currentPlayer);
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
    		if (toDiscard != null && (!player.getHand().contains(toDiscard) || !toDiscard.is(Type.Action, player))) {
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
		player.deckToDiscard(context, getControlCard());
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
            
            GameEvent event = new GameEvent(GameEvent.EventType.CardOnTopOfDeck, context);
            event.card = c;
            game.broadcastEvent(event);
        }
    }
	
	private void bard(Game game, MoveContext context, Player player) {
		game.receiveNextBoon(context, getControlCard());
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
					context.player.trashFromHand(context.player.hand.get(i), this.getControlCard(), context);
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
	
	private void changeling(Game game, MoveContext context, Player player) {
		player.trashSelfFromPlay(getControlCard(), context);
		
		HashSet<Card> validCards = new HashSet<Card>();
		for (Card c : player.playedCards) {
			if (Cards.isSupplyCard(c) && context.isCardOnTop(c)) {
				validCards.add(c);
			}
		}
		if (validCards.isEmpty()) return;
		
		Card cardToGain = null;
		if (player.playedCards.size() == 1) {
			cardToGain = player.playedCards.get(0);
		}
		if (cardToGain == null)
			cardToGain = player.controlPlayer.changeling_cardToGain(context, validCards.toArray(new Card[0]));
		if (!validCards.contains(cardToGain)) {
			Util.playerError(player, "Changeling error, invalid card to gain, choosing first");
			cardToGain = validCards.iterator().next();
		}
		player.gainNewCard(cardToGain, getControlCard(), context);
	}
	
	private void cobbler(Game game, MoveContext context, Player player, boolean isThronedEffect) {
		player.addStartTurnDurationEffect(this, 1, isThronedEffect);
	}
	
	private void conclave(Game game, MoveContext context, Player player) {
		ArrayList<Card> validCards = new ArrayList<Card>();
		for (Card c : player.hand) {
			if (c.is(Type.Action, player)) {
				if (!player.hasCopyInPlay(c)) {
					validCards.add(c);
				};
			}
		}
		if (validCards.isEmpty()) return;
		Card card = player.controlPlayer.conclave_cardToPlay(context);
		if (card == null) return;
		if (!validCards.contains(card)) {
			Util.playerError(player, "Conclave error, invalid card selected, ignoring");
			return;
		}
		context.freeActionInEffect++;
        card.play(game, context, true);
        context.freeActionInEffect--;
        context.actions++;
	}
	
	private void crypt(Game game, MoveContext context, Player player, boolean isThronedEffect) {
        int numTreasures = context.countTreasureCardsInPlay();
        Card[] treasuresToCrypt = numTreasures > 0 ? player.controlPlayer.crypt_cardsToSetAside(context) : new Card[0];
        if (treasuresToCrypt == null) treasuresToCrypt = new Card[0];
        ArrayList<Card> inPlay = new ArrayList<Card>();
        for(Card c : player.playedCards) if (c.is(Type.Treasure, player, context)) inPlay.add(c);
        if (!Util.areCardsInList(treasuresToCrypt, inPlay)) {
        	Util.playerError(player, "Crypt set aside error, ignoring");
        	return;
        }
        ArrayList<Card> cryptCards = new ArrayList<Card>();
        boolean setAsideCards = false;
        for (Card c : treasuresToCrypt) {
        	int idx = player.playedCards.indexOf(c);
    		cryptCards.add(player.playedCards.remove(idx));
        	if (!setAsideCards) {
            	player.crypt.add(cryptCards);
            	setAsideCards = true;
            }
            GameEvent event = new GameEvent(GameEvent.EventType.CardSetAsidePrivate, context);
	        event.card = c;
	        event.responsible = this;
	        event.setPrivate(true);
	        context.game.broadcastEvent(event);
        }
        
        if (cryptCards.size() > 0) {
        	player.addStartTurnDurationEffect(this, cryptCards.size(), isThronedEffect);
        }
    }
	
	public static void cryptSelect(Game game, MoveContext context, Player currentPlayer, ArrayList<Card> cards) {
		if (cards.size() == 0) {
        	return;
        }
        
        Card toHand = cards.get(0);
        if (cards.size() > 1) {
        	toHand = currentPlayer.controlPlayer.crypt_cardIntoHand(context, cards.toArray(new Card[0]));
        	if (!cards.contains(toHand)) {
        		Util.playerError(currentPlayer, "Crypt - invalid card selected.");
        		toHand = cards.get(0);
        	}
        }
        
        cards.remove(toHand);
        currentPlayer.hand.add(toHand);
	}
	
	private void cursedGold(Game game, MoveContext context, Player player) {
        player.gainNewCard(Cards.curse, this.getControlCard(), context);
    }
	
	private void cursedVillage(Game game, MoveContext context, Player player) {
		int cardsToDraw = 6 - player.hand.size();
    	if (cardsToDraw > 0 && player.getMinusOneCardToken()) {
        	game.drawToHand(context, Cards.cursedVillage, -1);
        }
    	for (int i = 0; i < cardsToDraw; ++i) {
    		if(!game.drawToHand(context, Cards.cursedVillage, cardsToDraw - i))
                break;
    	}
	}
	
	private void delusion(Game game, MoveContext context, Player player) {
		if (game.hasState(player, Cards.deluded) || game.hasState(player, Cards.envious)) return;
		game.takeState(context, Cards.deluded);
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
	
	private void druid(Game game, MoveContext context, Player player) {
		Card boon = player.controlPlayer.druid_pickBoon(context);
		if (boon == null || !game.druidBoons.contains(boon)) {
			Util.playerError(player, "Druid error, invalid boon, picking first.");
			boon = game.druidBoons.get(0);
		}
		game.recieveBoon(context, boon, this.getControlCard());
	}
	
	private void envy(Game game, MoveContext context, Player player) {
		if (game.hasState(player, Cards.deluded) || game.hasState(player, Cards.envious)) return;
		game.takeState(context, Cards.envious);
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
        		player.trashFromHand(cardToTrash, this.getControlCard(), context);
        		
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
	
	private void famine(Game game, MoveContext context, Player player) {
		ArrayList<Card> cards = new ArrayList<Card>();
        for (int i = 0; i < 3; i++) {
            Card card = context.game.draw(context, Cards.famine, 3 - i);
            if (card == null) {
                break;
            }
            cards.add(card);
        }

        if (cards.size() == 0) {
            return;
        }
        
        ArrayList<Card> toDiscard = new ArrayList<Card>();
        for (Card c : cards) {
        	player.reveal(c, this.getControlCard(), context);
        	if (c.is(Type.Action, player)) {
        		toDiscard.add(c);
        	}
        }
        for (Card c : toDiscard) {
        	cards.remove(c);
        	player.discard(c, this.getControlCard(), context);
        }
        for (Card c : cards) {
        	player.deck.add(c);
        }
        player.shuffleDeck(context, this.getControlCard());
    }
	
	private void fear(Game game, MoveContext context, Player player) {
		if (player.hand.size() < 5) return;
		ArrayList<Card> validCards = new ArrayList<Card>();
		for (Card card : player.hand) {
			if (card.is(Type.Action, player) || card.is(Type.Treasure, player, context)) {
				validCards.add(card);
			}
		}
		if (validCards.size() == 0) {
			for (Card card : player.hand) {
				player.reveal(card, getControlCard(), context);
			}
			return;
		}
		Card toDiscard = validCards.get(0);
		if (validCards.size() > 1) {
			toDiscard = player.controlPlayer.fear_cardToDiscard(context);
			if (toDiscard == null || !validCards.contains(toDiscard)) {
				Util.playerError(player, "Fear error, invalid card, selecting first");
				toDiscard = validCards.get(0);
			}
		}
		int idx = player.hand.indexOf(toDiscard);
		player.discard(player.hand.remove(idx), this.getControlCard(), context);        
    }
	
	private void fool(Game game, MoveContext context, Player player) {
		if (game.hasState(player, Cards.lostInTheWoods)) return;
		game.takeSharedState(context, Cards.lostInTheWoods);
		ArrayList<Card> boons = new ArrayList<Card>();
		for (int i = 0; i < 3; ++i) {
			Card boon = game.takeNextBoon(context, getControlCard());
			if (boon != null)
				boons.add(boon);
		}
		if (boons.size() == 0) return;
		if (boons.size() == 1) {
			game.recieveBoonAndDiscard(context, boons.get(0), getControlCard());
			return;
		}
		//TODO: does the order of Boons to receive on Fool have to be chosen before receiving any of them?
		while (!boons.isEmpty()) {
			Card boonToReceive = player.controlPlayer.fool_boonToReceive(context, boons.toArray(new Card[0]));
			if (!boons.contains(boonToReceive)) {
				Util.playerError(player, "Fool error, invalid Boon chosen, choosing first Boon");
				boonToReceive = boons.get(0);
			}
			int idx = boons.indexOf(boonToReceive);
			boonToReceive = boons.remove(idx);
			
			game.recieveBoonAndDiscard(context, boonToReceive, getControlCard());
		}
	}
	
	private void ghost(Game game, MoveContext context, Player player) {
        ArrayList<Card> toDiscard = new ArrayList<Card>();

        Card draw = null;
        while ((draw = game.draw(context, Cards.ghost, -1)) != null && !draw.is(Type.Action, player)) {
            player.reveal(draw, this.getControlCard(), context);
            toDiscard.add(draw);
        }

        if (draw != null) {
            player.reveal(draw, this.getControlCard(), context);
            player.ghost.add(draw);
            GameEvent event = new GameEvent(GameEvent.EventType.CardSetAside, context);
	        event.card = draw;
	        event.responsible = this;
	        context.game.broadcastEvent(event);
	        player.addStartTurnDurationEffect(this, 1, false);
        }
        
        while (!toDiscard.isEmpty()) {
            player.discard(toDiscard.remove(0), this.getControlCard(), context);
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
        		player.trashFromHand(cardToTrash, this.getControlCard(), context);
        	}
        }
    }
	
	private void greed(Game game, MoveContext context, Player player) {
		context.getPlayer().gainNewCard(Cards.copper, this.getControlCard(), context);
    }
	
	private void guardian(Game game, MoveContext context, Player player) {
		player.guardianEffect = true;
	}
	
	private void haunting(Game game, MoveContext context, Player player) {
		if (player.hand.size() < 4) return;
		
		Card card = player.controlPlayer.haunting_cardToPutBackOnDeck(context);
		if (card == null || !player.hand.contains(card)) {
			Util.playerError(player, "Haunting put back card error, putting back first card");
			card = player.hand.get(0);
		}
		player.putOnTopOfDeck(player.hand.removeCard(card));
		GameEvent topDeckEvent = new GameEvent(GameEvent.EventType.CardOnTopOfDeck, context);
		topDeckEvent.card = card;
		topDeckEvent.setPlayer(player);
		topDeckEvent.setPrivate(true);
		game.broadcastEvent(topDeckEvent);		
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
	
	private void locusts(Game game, MoveContext context, Player player) {
		Card trashCard = game.draw(context, getControlCard(), 1);
		player.trash(trashCard, getControlCard(), context);
		if (trashCard.equals(Cards.copper) || trashCard.equals(Cards.estate)) {
			player.gainNewCard(Cards.curse, getControlCard(), context);
			return;
		}
		
		int cost = trashCard.getCost(context);
        int debt = trashCard.getDebtCost(context);
        boolean potion = trashCard.costPotion();
        int potionCost = potion ? 1 : 0;
        Type[] types = trashCard.getTypes();
        
        ArrayList<Card> validCards = new ArrayList<Card>();
        for (Card card : game.getCardsInGame(GetCardsInGameOptions.TopOfPiles, true)) {
            int gainCardCost = card.getCost(context);
            int gainCardPotionCost = card.costPotion() ? 1 : 0;
            int gainCardDebt = card.getDebtCost(context);
            
            if (!sharesType(trashCard, card))
            	continue;

            if ((gainCardCost < cost || gainCardDebt < debt || gainCardPotionCost < potionCost) && 
            		(gainCardCost <= cost && gainCardDebt <= debt && gainCardPotionCost <= potionCost)) {
                validCards.add(card);
            }
        }
        if (validCards.size() == 0)
        	return;
        
    	Card toGain = context.getPlayer().controlPlayer.locusts_cardToObtain(context, cost, debt, potion, types);
        if (toGain == null || !validCards.contains(toGain)) {
            Util.playerError(context.getPlayer(), "Invalid card returned from Locusts, picking one.");
            toGain = validCards.get(0);
        }
        context.getPlayer().gainNewCard(toGain, Cards.locusts, context);
	}
	
	private boolean sharesType(Card a, Card b) {
		for(Type at : a.getTypes()) {
			for (Type bt : b.getTypes()) {
				if (at == bt) return true;
			}
		}
		return false;
	}
	
	private void lostInTheWoods(Game game, MoveContext context, Player player) {
		Card toDiscard = player.controlPlayer.lostInTheWoods_cardToDiscard(context);
		if (toDiscard == null) return;
		if (!player.hand.contains(toDiscard)) {
			Util.playerError(player, "Lost in the Woods error, invalid card, ignoring");
		}
		int idx = player.hand.indexOf(toDiscard);
		player.discard(player.hand.remove(idx), this.getControlCard(), context);
		game.receiveNextBoon(context, this);
	}
	
	private void luckyCoin(Game game, MoveContext context, Player player) {
		player.gainNewCard(Cards.silver, this.getControlCard(), context);
	}
	
	private void magicLamp(Game game, MoveContext context, Player player) {
		Map<Cards.Kind, Integer> cardKindsInPlay = new HashMap<Cards.Kind, Integer>();
		for (Card c : player.playedCards) {
			Cards.Kind kind = c.getControlCard().equals(Cards.estate) ? Cards.Kind.Estate : c.behaveAsCard().getKind();
			if (cardKindsInPlay.containsKey(kind)) {
				cardKindsInPlay.put(kind, cardKindsInPlay.get(kind) + 1);
			} else {
				cardKindsInPlay.put(kind, 1);
			}
		}
		int kindsOfCardExactlyOneInPlay = 0;
		for (Cards.Kind kind : cardKindsInPlay.keySet()) {
			if (cardKindsInPlay.get(kind) == 1)
				kindsOfCardExactlyOneInPlay++;
		}
		if (kindsOfCardExactlyOneInPlay < 6) return;
		if (player.trashSelfFromPlay(getControlCard(), context)) {
			for (int i = 0; i < 3; ++i)
				player.gainNewCard(Cards.wish, getControlCard(), context);
		}
	}
	
	private void misery(Game game, MoveContext context, Player player) {
		if (game.hasState(player, Cards.miserable)) {
			//This is our way to represent flipping over the state card
			context.player.states.remove(Cards.miserable);
			game.takeState(context, Cards.twiceMiserable);
		} else if (!game.hasState(player, Cards.twiceMiserable)) {
			game.takeState(context, Cards.miserable);
		}
	}
	
	private void monastery(Game game, MoveContext context, Player player) {
		int numGained = context.getNumCardsGainedThisTurn();
		if (numGained == 0) return;
		
		CardList hand = player.getHand();
		int numCoppersInPlay = context.countCardsInPlay(Cards.copper);
		int handSize = hand.size();
		
		for (int i = 0; i < numGained; ++i) {
			if (numCoppersInPlay == 0 && handSize == 0) return;
			if (numCoppersInPlay == 0) {
				//only have to ask to trash cards from hand
				monasteryTrashFromHand(context, player);
			} else if (handSize == 0) {
				//only have to ask if we want to trash a copper from play or pass
				if (!player.controlPlayer.monastery_shouldTrashCopperFromPlay(context)) return;
				monastaryTrashCopperFromPlay(context, player);
			} else {
				// have to ask both or pass
				switch (player.controlPlayer.monastery_chooseOption(context)) {
				case Pass:
					return;
				case TrashCopperFromPlay:
					monastaryTrashCopperFromPlay(context, player);
					break;
				case TrashFromHand:
					monasteryTrashFromHand(context, player);
					break;
				}
			}
			handSize = hand.size();
			numCoppersInPlay = context.countCardsInPlay(Cards.copper);
		}
	}
	
	private void monasteryTrashFromHand(MoveContext context, Player player) {
		CardList hand = player.getHand();
		Card cardToTrash = player.controlPlayer.monastery_cardToTrash(context);
        if (cardToTrash == null)
        	return;
        if (!hand.contains(cardToTrash)) {
    		Util.playerError(player, "Monastery error, invalid card to trash, ignoring.");
    	} else {
    		cardToTrash = hand.get(cardToTrash);
    		player.trashFromHand(cardToTrash, getControlCard(), context);
    	}
	}
	
	private void monastaryTrashCopperFromPlay(MoveContext context, Player player) {
		for (Iterator<Card> it = player.playedCards.iterator(); it.hasNext();) {
            Card playedCard = it.next();
            if (playedCard.equals(Cards.copper)) {
                context.player.trashFromPlay(playedCard, this.getControlCard(), context);
                break;
            }
        }
	}
	
	private void necromancer(Game game, MoveContext context, Player player) {
		ArrayList<Card> faceUpActions = new ArrayList<Card>();
		for (Card card : game.GetTrashPile()) {
			if (!card.is(Type.Action) || card.is(Type.Duration)) continue;
			boolean isFaceUp = true;
			for (Card faceDownCard : game.trashPileFaceDown) {
				if (faceDownCard.getId() == card.getId())
					isFaceUp = false;
			}
			if (isFaceUp) {
				faceUpActions.add(card);
			}
		}
		if (faceUpActions.isEmpty()) return;
		Card actionToPlay = faceUpActions.get(0);
		if (faceUpActions.size() > 1) {
			actionToPlay = player.controlPlayer.necromancer_cardToPlay(context, faceUpActions.toArray(new Card[0]));
			if (actionToPlay == null || !faceUpActions.contains(actionToPlay)) {
				Util.playerError(player, "Necromancer error, invalid card to play, picking first");
				actionToPlay = faceUpActions.get(0);
			}
		}
		game.trashPileFaceDown.add(actionToPlay);
		context.freeActionInEffect++;
		actionToPlay.play(game, context, false, false, true, false, false);
		context.freeActionInEffect--;
	}
	
	private void nightWatchman(Game game, MoveContext context, Player player) {
		ArrayList<Card> topOfTheDeck = new ArrayList<Card>();
        for (int i = 0; i < 5; i++) {
            Card card = game.draw(context, Cards.nightWatchman, 5 - i);
            if (card != null) {
                topOfTheDeck.add(card);
            }
        }

        if (topOfTheDeck.size() > 0) {
            Card[] cardsToDiscard = player.controlPlayer.nightWatchman_cardsFromTopOfDeckToDiscard(context, topOfTheDeck.toArray(new Card[topOfTheDeck.size()]));
            if(cardsToDiscard != null) {
                for(Card toDiscard : cardsToDiscard) {
                    if(topOfTheDeck.remove(toDiscard)) {
                    	player.discard(toDiscard, this.getControlCard(), context);
                    }
                    else {
                        Util.playerError(player, "Night Watchman returned invalid card to discard, ignoring");
                    }
                }
            }
            if (topOfTheDeck.size() > 0) {
                Card[] order;

                if(topOfTheDeck.size() == 1) {
                    order = topOfTheDeck.toArray(new Card[topOfTheDeck.size()]);
                }
                else {
                    order = player.controlPlayer.cartographer_cardOrder(context, topOfTheDeck.toArray(new Card[topOfTheDeck.size()]));
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
                        Util.playerError(player, "Night Watchman order cards error, ignoring.");
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
	
	private void pixie(Game game, MoveContext context, Player player) {
		Card topBoon = game.discardNextBoon(context, this);
		if (player.isInPlay(this) && player.controlPlayer.pixie_shouldTrashPixie(context, topBoon, getControlCard())) {
			if (player.trashSelfFromPlay(getControlCard(), context)) {
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
                if (c.is(Type.Treasure, player, context) && !c.equals(Cards.cursedGold)) {
                    hasValidTreasure = true;
                    break;
                }
            }
            if (hasValidTreasure) {
                Card card = player.controlPlayer.pooka_treasureToTrash(context);
                if (card == null)
                	return;
                if (!card.is(Type.Treasure, player, context) || card.equals(Cards.cursedGold) || !player.hand.contains(card)) {
                    Util.playerError(player, "Pooka card to trash invalid, ignoring");
                    return;
                }

                player.trashFromHand(card, this.getControlCard(), context);
                for (int i = 0; i < 4; ++i) {
                	game.drawToHand(context, Cards.pooka, 4 - i);
                }
            }
        }
	}
	
	private void poverty(Game game, MoveContext context, Player player) {
		int keepCardCount = 3;
        if (player.hand.size() > keepCardCount) {
            Card[] cardsToKeep = player.controlPlayer.poverty_attack_cardsToKeep(context);
            player.discardRemainingCardsFromHand(context, cardsToKeep, this.getControlCard(), keepCardCount);
        }
	}
	
	private void sacredGrove(Game game, MoveContext context, Player currentPlayer) {
		Card boon = game.discardNextBoon(context, getControlCard());
		game.recieveBoon(context, boon, getControlCard());
		if (boon.getAddGold() == 1) return;
    	for (Player player : context.game.getPlayersInTurnOrder()) {
			if (player == currentPlayer) continue;
			MoveContext playerContext = new MoveContext(game, player);
			if (!player.controlPlayer.sacredGrove_shouldReceiveBoon(playerContext, boon)) return;
			game.recieveBoon(playerContext, boon, getControlCard());
    	}
	}
	
	private void secretCave(Game game, MoveContext context, Player player, boolean isThronedEffect) {
		if (player.getHand().size() == 0) {
            return;
        }
		
        Card[] cardsToDiscard = player.controlPlayer.secretCave_cardsToDiscard(context);
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
            player.addStartTurnDurationEffect(this, 1, isThronedEffect);
        }
	}
		
	private void shepherd(Game game, MoveContext context, Player currentPlayer) {
		int numVictories = 0;
		for (Card c : currentPlayer.getHand()) {
			if (c.is(Type.Victory, currentPlayer))
				numVictories++;
		}
		if (numVictories == 0)
			return;
		
        Card[] cards = currentPlayer.controlPlayer.shepherd_cardsToDiscard(context);
        if (cards == null || cards.length == 0)
        	return;
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
            	game.drawToHand(context, Cards.shepherd, numToDraw - i);
            }
        }
    }
	
	private void raider(Game game, MoveContext context, Player currentPlayer) {
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
            	if (currentPlayer.hasCopyInPlay(card)) {
            		discardCards.add(card);
            	}
            }
            if (discardCards.size() == 0) {
            	for (Card card : player.hand){
            		player.reveal(card, getControlCard(), playerContext);
            	}
            	continue;
            }
            if (discardCards.size() == 1) {
            	int idx = player.hand.indexOf(discardCards.get(0));
        		player.discard(player.hand.remove(idx), this.getControlCard(), context);
            	continue;
            }
            Card toDiscard = player.controlPlayer.raider_cardToDiscard(playerContext, discardCards.toArray(new Card[0]));
            if (toDiscard == null || !discardCards.contains(toDiscard)) {
            	Util.playerError(player, "Raider discard error, invalid card, chosing first");
            	toDiscard = discardCards.get(0);
            }
            int idx = player.hand.indexOf(toDiscard);
    		player.discard(player.hand.remove(idx), this.getControlCard(), context);
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
            if(c.is(Type.Treasure, currentPlayer, context)) {
                hasTreasure = true;
            }
        }
        if (!hasTreasure)
        	return;

        Card toDiscard = currentPlayer.controlPlayer.theEarthsGift_treasureToDiscard(context);
        if (toDiscard == null || !currentPlayer.hand.contains(toDiscard) || !toDiscard.is(Type.Treasure, currentPlayer, context))
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
    		player.trashFromHand(cardToTrash, Cards.theFlamesGift, context);
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
                    	player.discard(toDiscard, this.getControlCard(), context);
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
	
	private void tormentor(Game game, MoveContext context, Player currentPlayer) {
		ArrayList<Player> attackedPlayers = new ArrayList<Player>();
    	for (Player player : context.game.getPlayersInTurnOrder()) {
            if (player != currentPlayer && !Util.isDefendedFromAttack(context.game, player, this)) {
            	attackedPlayers.add(player);
            }
    	}
		
		if (context.countCardsInPlay() == 1) {
			currentPlayer.gainNewCard(Cards.imp, this.getControlCard(), context);
		} else {
			game.othersReceiveNextHex(context, attackedPlayers, this.getControlCard());
		}
	}
	
	private void tracker(Game game, MoveContext context, Player player) {
		game.receiveNextBoon(context, getControlCard());
	}
	
	private void tragicHero(Game game, MoveContext context, Player player) {
		if (player.getHand().size() < 8) return;
		player.trashSelfFromPlay(getControlCard(), context);
		
		int numTreasuresAvailable = 0;
    	for (Card treasureCard : context.getCardsInGame(GetCardsInGameOptions.TopOfPiles, true, Type.Treasure)) {
    		if (Cards.isSupplyCard(treasureCard) && context.isCardOnTop(treasureCard)) {
    			numTreasuresAvailable++;
    		}
    	}
    	if (numTreasuresAvailable == 0)
    		return;
    	
    	Card newCard = player.controlPlayer.tragicHero_treasureToObtain(context);
    	
        if (!(newCard != null && newCard.is(Type.Treasure, null, context) && Cards.isSupplyCard(newCard) && context.isCardOnTop(newCard))) {
            Util.playerError(player, "Tragic Hero treasure to obtain was invalid, picking random treasure from table.");
            for (Card treasureCard : context.getCardsInGame(GetCardsInGameOptions.TopOfPiles, true, Type.Treasure)) {
                if (Cards.isSupplyCard(treasureCard) && context.getCardsLeftInPile(treasureCard) > 0) {
                    newCard = treasureCard;
                    break;
                }
            }
        }
        
        player.gainNewCard(newCard, this.getControlCard(), context);
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
	
	private void war(Game game, MoveContext context, Player currentPlayer) {
	        ArrayList<Card> toDiscard = new ArrayList<Card>();

	        Card draw = null;
	        while ((draw = game.draw(context, Cards.war, -1)) != null && !costs3or4(draw, context)) {
	            currentPlayer.reveal(draw, this.getControlCard(), context);
	            toDiscard.add(draw);
	        }

	        if (draw != null) {
	            currentPlayer.reveal(draw, this.getControlCard(), context);
	            currentPlayer.trash(draw, this.getControlCard(), context);
	        }

	        while (!toDiscard.isEmpty()) {
	            currentPlayer.discard(toDiscard.remove(0), this.getControlCard(), context);
	        }
	}
	
	private boolean costs3or4(Card c, MoveContext context) {
		int cost = c.getCost(context);
		int debtCost = c.getDebtCost(context);
		boolean costPotion = c.costPotion();
		return (cost == 3 || cost == 4) && debtCost == 0 && !costPotion;
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
            	game.drawToHand(context, Cards.werewolf, 3 - i);
            }
		}
	}
	
	private void wish(Game game, MoveContext context, Player player) {
		if (player.isInPlay(this)) {
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
	
	private void zombieApprentice(Game game, MoveContext context, Player player) {
		if (player.getHand().size() == 0)
			return;
		ArrayList<Card> validActions = new ArrayList<Card>();
		for (Card c : player.hand) {
			if (c.is(Type.Action, player)) {
				validActions.add(c);
			}
		}
		Card cardToTrash = player.controlPlayer.zombieApprentice_cardToTrash(context);
		if (cardToTrash == null) return;
		if (!validActions.contains(cardToTrash)) {
			Util.playerError(player, "Zombie Apprentice error - invalid card to trash, ignoring");
			return;
		}
		cardToTrash = player.hand.get(cardToTrash);
		player.trashFromHand(cardToTrash, this.getControlCard(), context);
		
		for (int i = 0; i < 3; ++i) {
        	game.drawToHand(context, this, 3 - i);
        }
		
		context.actions += 1;
	}
	
	private void zombieMason(Game game, MoveContext context, Player player) {
		Card cardToTrash = game.draw(context, getControlCard(), 1);
		if (cardToTrash == null) return;
		player.trash(cardToTrash, this.getControlCard(), context);
		
		int value = cardToTrash.getCost(context) + 1;
        int debtValue = cardToTrash.getDebtCost(context);
        boolean potion = cardToTrash.costPotion();
        
        Card card = player.controlPlayer.zombieMason_cardToObtain(context, value, debtValue, potion);
        if (card != null) {
            if (card.getCost(context) > value || card.getDebtCost(context) > debtValue || (card.costPotion() && !potion)) {
                Util.playerError(player, "Zombie Mason error, new card does not cost value of the old card +1 or less.");
            } else if (game.isPileEmpty(card)) {
            	Util.playerError(player, "Zombie Mason error, new card pile is empty.");
            } else if (!game.isCardOnTop(card)) {
            	Util.playerError(player, "Zombie Mason error, new card not in game.");
            } else {
            	player.gainNewCard(card, this.getControlCard(), context);
            }
        }
	}
	
	private void zombieSpy(Game game, MoveContext context, Player player) {
		Card card = game.draw(context, this, 1);
		if (card == null) return;
		boolean discard = player.controlPlayer.zombieSpy_shouldDiscard(context, card);
        if (discard) {
            player.discard(card, this.getControlCard(), context);
        } else {
            player.putOnTopOfDeck(card, context, true);
        }
	}
}
