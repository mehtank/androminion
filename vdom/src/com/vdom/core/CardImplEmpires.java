package com.vdom.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.vdom.api.Card;
import com.vdom.api.GameEvent;
import com.vdom.core.Cards.Kind;
import com.vdom.core.MoveContext.TurnPhase;
import com.vdom.core.Player.CharmOption;
import com.vdom.core.Player.EncampmentOption;
import com.vdom.core.Player.WildHuntOption;

public class CardImplEmpires extends CardImpl {
	private static final long serialVersionUID = 1L;

	public CardImplEmpires(CardImpl.Builder builder) {
		super(builder);
	}

	protected CardImplEmpires() { }

	@Override
	protected void additionalCardActions(Game game, MoveContext context, Player currentPlayer) {
		switch(getKind()) {
		case Archive:
			archive(game, context, currentPlayer);
			break;
		case BustlingVillage:
        	bustlingVillage(game, context, currentPlayer);
        	break;
        case Catapult:
        	catapult(game, context, currentPlayer);
        	break;
        case ChariotRace:
        	chariotRace(game, context, currentPlayer);
        	break;
        case Charm:
            charm(game, context, currentPlayer);
        	break;
        case CityQuarter:
        	cityQuarter(game, context, currentPlayer);
        	break;
        case Crown:
        	crown(game, context, currentPlayer);
        	break;
        case Encampment:
        	encampment(game, context, currentPlayer);
        	break;
        case Enchantress:
        	durationAttack(game, context, currentPlayer);
        	break;
        case Engineer:
        	engineer(game, context, currentPlayer);
        	break;
        case FarmersMarket:
        	farmersMarket(game, context, currentPlayer);
        	break;
        case Fortune:
        	fortune(context, currentPlayer, game);
        	break;
        case Forum:
        	discardMultiple(context, currentPlayer, 2);
        	break;
        case Gladiator:
        	gladiator(game, context, currentPlayer);
        	break;
        case Legionary:
        	legionary(game, context, currentPlayer);
        	break;
        case OpulentCastle:
        	opulentCastle(game, context, currentPlayer);
        	break;
        case Overlord:
        	overlord(game, context, currentPlayer);
        	break;
        case Patrician:
        	patrician(game, context, currentPlayer);
        	break;
        case RoyalBlacksmith:
        	royalBlacksmith(game, context, currentPlayer);
        	break;
        case Sacrifice:
        	sacrifice(game, context, currentPlayer);
        	break;
        case Settlers:
        	settlers(game, context, currentPlayer);
        	break;
        case SmallCastle:
        	smallCastle(game, context, currentPlayer);
        	break;
        case Temple:
        	temple(game, context, currentPlayer);
        	break;
        case WildHunt:
        	wildHunt(game, context, currentPlayer);
        	break;
		default:
			break;
		}
	}
	
	public void isBuying(MoveContext context) {
		super.isBuying(context);
        switch (this.controlCard.getKind()) {
        case Forum:
        	context.buys++;
        	break;
        //Events
        case Advance:
        	advance(context);
        	break;
        case Annex:
        	annex(context);
        	break;
        case Banquet:
        	banquet(context);
        	break;
        case Conquest:
        	conquest(context);
        	break;
        case Delve:
        	delve(context);
        	break;
        case Dominate:
        	dominate(context);
        	break;
        case Donate:
        	donate(context);
        	break;
        case Ritual:
        	ritual(context);
        	break;
        case SaltTheEarth:
        	saltTheEarth(context);
        	break;
        case Tax:
        	tax(context);
        	break;
        case Triumph:
        	triumph(context);
        	break;
        case Wedding:
        	wedding(context);
        	break;
        case Windfall:
        	windfall(context);
        default:
            break;
        }
        
        // test if prince lost track of any cards
        context.player.princeCardLeftThePlay(context.player);
    }
	
	@Override
	public void isTrashed(MoveContext context) {
		Cards.Kind trashKind = this.controlCard.getKind();
    	if (this.controlCard.equals(Cards.estate) && context.player.getInheritance() != null) {
    		trashKind = context.player.getInheritance().getKind();
    	}
    	
    	switch (trashKind) {
    	case CrumblingCastle:
        	context.player.addVictoryTokens(context, 1);
        	context.player.gainNewCard(Cards.silver, this, context);
        case Rocks:
        	context.player.gainNewCard(Cards.silver, this, context);
        	break;
        default:
            break;
	    }
	    
	    // card left play - stop any impersonations
	    this.controlCard.stopImpersonatingCard();
	    this.controlCard.stopInheritingCardAbilities();
	}
	
	private void archive(Game game, MoveContext context, Player currentPlayer) {
		ArrayList<Card> topOfTheDeck = new ArrayList<Card>();
		boolean setAsideCards = false;
        for (int i = 0; i < 3; i++) {
            Card card = game.draw(context, Cards.archive, 3 - i);
            if (card != null) {
                topOfTheDeck.add(card);
                if (!setAsideCards) {
                	currentPlayer.archive.add(topOfTheDeck);
                	setAsideCards = true;
                }
                GameEvent event = new GameEvent(GameEvent.EventType.CardSetAsideArchive, context);
    	        event.card = card;
    	        event.setPrivate(true);
    	        context.game.broadcastEvent(event);
            }
        }
        
        archiveSelect(game, context, currentPlayer, topOfTheDeck);
        
        if (topOfTheDeck.size() == 0) {
        	for (int i = 0; i < currentPlayer.nextTurnCards.size(); ++i) {
        		if (currentPlayer.nextTurnCards.get(i) == this.controlCard) {
        			currentPlayer.nextTurnCards.remove(i);
        			break;
        		}
        	}
            currentPlayer.playedCards.add(this.controlCard);
        	return;
        }
	}
	
	public static void archiveSelect(Game game, MoveContext context, Player currentPlayer, ArrayList<Card> cards) {
		if (cards.size() == 0) {
        	return;
        }
        
        Card toHand = cards.get(0);
        if (cards.size() > 1) {
        	toHand = currentPlayer.controlPlayer.archive_cardIntoHand(context, cards.toArray(new Card[0]));
        	if (!cards.contains(toHand)) {
        		Util.playerError(currentPlayer, "Archive - invalid card selected.");
        		toHand = cards.get(0);
        	}
        }
        
        cards.remove(toHand);
        currentPlayer.hand.add(toHand);
	}
	
	private void bustlingVillage(Game game, MoveContext context, Player currentPlayer) {
        if (currentPlayer.discard.isEmpty()) return;
        int coppers = 0;
        int settlers = 0;
        for (Iterator<Card> it = currentPlayer.discard.iterator(); it.hasNext();) {
            Card card = it.next();
            if (Cards.copper.equals(card)) {
                coppers++;
            }
            if (Cards.settlers.equals(card)) {
                settlers++;
            }
        }
        if (currentPlayer.controlPlayer.bustlingVillage_settlersIntoHand(context, coppers, settlers)) {
        	for (Iterator<Card> it = currentPlayer.discard.iterator(); it.hasNext();) {
                Card card = it.next();
                if (Cards.settlers.equals(card)) {
                    currentPlayer.reveal(card, this.controlCard, context);
                    it.remove();
                    currentPlayer.hand.add(card);
                    break;
                }
            }
        }
    }
    
    private void catapult(Game game, MoveContext context, Player currentPlayer) {
    	ArrayList<Player> attackedPlayers = new ArrayList<Player>();
    	for (Player player : context.game.getPlayersInTurnOrder()) {
            if (player != currentPlayer && !Util.isDefendedFromAttack(context.game, player, this)) {
            	attackedPlayers.add(player);
            }
    	}
    	
    	if(currentPlayer.getHand().size() == 0) return;
        Card cardToTrash = currentPlayer.controlPlayer.catapult_cardToTrash(context);
        if (cardToTrash == null) {
            Util.playerError(currentPlayer, "Catapult did not return a card to trash, trashing random card.");
            cardToTrash = Util.randomCard(currentPlayer.getHand());
        }
        
        currentPlayer.hand.remove(cardToTrash);
        currentPlayer.trash(cardToTrash, this.controlCard, context);
        boolean isTreasure = cardToTrash.is(Type.Treasure, null);
        int coinCost = cardToTrash.getCost(context);
    	
        if (coinCost >= 3) {
	        for (Player player : attackedPlayers) {
				player.attacked(this.controlCard, context);
	            MoveContext playerContext = new MoveContext(game, player);
	            playerContext.attackedPlayer = player;
	            player.gainNewCard(Cards.curse, this.controlCard, playerContext);
	        }
        }
        
        if (isTreasure) {
	        for (Player player : attackedPlayers) {
				player.attacked(this.controlCard, context);
	            MoveContext playerContext = new MoveContext(game, player);
	            playerContext.attackedPlayer = player;
	            int keepCardCount = 3;
                if (player.hand.size() > keepCardCount) {
                    Card[] cardsToKeep = player.controlPlayer.catapult_attack_cardsToKeep(playerContext);
                    player.discardRemainingCardsFromHand(playerContext, cardsToKeep, this.controlCard, keepCardCount);
                }
	        }
        }
    }
    
    private void chariotRace(Game game, MoveContext context, Player currentPlayer) {
    	Card draw = game.draw(context, Cards.chariotRace, 1);
        if (draw != null) {
            currentPlayer.reveal(draw, this.controlCard, context);
            currentPlayer.hand.add(draw, true);
            Player nextPlayer = game.getNextPlayer();
            MoveContext nextPlayerContext = new MoveContext(game, nextPlayer);
            Card nextPlayerCard = game.draw(nextPlayerContext, this.controlCard, 1);
            if (nextPlayerCard != null) {
            	nextPlayer.reveal(nextPlayerCard, this.controlCard, nextPlayerContext);
            	nextPlayer.putOnTopOfDeck(nextPlayerCard, nextPlayerContext, false);
            	int drawCoinCost = draw.getCost(context); 
            	int nextPlayerCoinCost = nextPlayerCard.getCost(context);
            	int drawDebtCost = draw.getDebtCost(context); 
            	int nextPlayerDebtCost = nextPlayerCard.getDebtCost(context);
            	int drawPotionCost = draw.costPotion() ? 1 : 0;
            	int nextPlayerPotionCost = nextPlayerCard.costPotion() ? 1 : 0;
            	if ((drawCoinCost > nextPlayerCoinCost ||
            			drawDebtCost > nextPlayerDebtCost ||
            			drawPotionCost > nextPlayerPotionCost) && 
            			(drawCoinCost >= nextPlayerCoinCost ||
                    			drawDebtCost >= nextPlayerDebtCost ||
                    			drawPotionCost >= nextPlayerPotionCost)) {
            		context.addCoins(1);
            		currentPlayer.addVictoryTokens(context, 1);
            	}
            }
        }
    }
    
    private void charm(Game game, MoveContext context, Player currentPlayer) {
    	if (currentPlayer.controlPlayer.charm_chooseOption(context) == CharmOption.OneBuyTwoCoins) {
    		context.buys++;
    		context.addCoins(2);
    	} else {
    		context.charmsNextBuy++;
    	}
    }
    
    private void cityQuarter(Game game, MoveContext context, Player currentPlayer) {
    	int actionCards = 0;

        for (int i = 0; i < currentPlayer.hand.size(); i++) {
            Card card = currentPlayer.hand.get(i);
            currentPlayer.reveal(card, this.controlCard, context);
            if (card.is(Type.Action, currentPlayer)) {
            	actionCards++;
            }
        }
        for (int i = 0; i < actionCards; ++i) {
        	game.drawToHand(context, this, actionCards - i);
        }
    }
    
    private void crown(Game game, MoveContext context, Player currentPlayer) {
    	if (context.phase == TurnPhase.Action) {
    		throneRoomKingsCourt(game, context, currentPlayer);
    	} else if (context.phase == TurnPhase.Buy) {
    		multiPlayTreasure(context, game, currentPlayer);
    	}
    }
    
    private void encampment(Game game, MoveContext context, Player currentPlayer) {
    	boolean revealedCard = false;
    	CardList hand = currentPlayer.getHand();
    	ArrayList<EncampmentOption> options = new ArrayList<Player.EncampmentOption>();
    	if (hand.contains(Cards.gold))
    		options.add(EncampmentOption.RevealGold);
    	if (hand.contains(Cards.plunder))
    		options.add(EncampmentOption.RevealPlunder);
    	options.add(null);
    	if (options.size() > 1) {
    		EncampmentOption option = currentPlayer.controlPlayer.encampment_chooseOption(context, options.toArray(new EncampmentOption[0]));
    		if (options.contains(EncampmentOption.RevealGold) && option.equals(EncampmentOption.RevealGold)) {
    			Card c = hand.get(Cards.gold);
    			currentPlayer.reveal(c, this.controlCard, context);
    			revealedCard = true;
    		}
    		if (options.contains(EncampmentOption.RevealPlunder) && option.equals(EncampmentOption.RevealPlunder)) {
    			Card c = hand.get(Cards.plunder);
    			currentPlayer.reveal(c, this.controlCard, context);
    			revealedCard = true;
    		}
    	}
    	
    	if (!revealedCard) {
    		currentPlayer.playedCards.remove(currentPlayer.playedCards.lastIndexOf(this.controlCard));
    		currentPlayer.encampment.add(this.controlCard);
    	}
    }
    
    private void engineer(Game game, MoveContext context, Player currentPlayer) {
    	Card card = currentPlayer.controlPlayer.engineer_cardToObtain(context);
        if (card != null) {
            if (card.getCost(context) <= 4 && card.getDebtCost(context) == 0 && !card.costPotion()) {
                currentPlayer.gainNewCard(card, this.controlCard, context);
            }
        }
        if (!this.controlCard.movedToNextTurnPile) {
        	if (currentPlayer.controlPlayer.engineer_shouldTrashEngineerPlayed(context)) {
        	    this.controlCard.movedToNextTurnPile = true;
                currentPlayer.playedCards.remove(currentPlayer.playedCards.lastIndexOf(this.controlCard));
                currentPlayer.trash(this.controlCard, this.controlCard, context);
                
                card = currentPlayer.controlPlayer.engineer_cardToObtain(context);
                if (card != null) {
                    if (card.getCost(context) <= 4 && card.getDebtCost(context) == 0 && !card.costPotion()) {
                        currentPlayer.gainNewCard(card, this.controlCard, context);
                    }
                }
        	}
        }
    }
    
    private void farmersMarket(Game game, MoveContext context, Player currentPlayer) {
    	Card c = Cards.farmersMarket;
    	if (game.getPileVpTokens(c) >= 4) {
    		int numTokens = game.getPileVpTokens(c);
    		game.removePileVpTokens(c, numTokens, context);
    		currentPlayer.addVictoryTokens(context, numTokens);
    		currentPlayer.trash(currentPlayer.playedCards.removeLastCard(), this.getControlCard(), context);
    	} else {
    		game.addPileVpTokens(c, 1, context);
    		context.addCoins(game.getPileVpTokens(c));
    	}
    }
    
    private void fortune(MoveContext context, Player player, Game game) {
    	if (!context.hasDoubledCoins) {
    		//TODO?: is doubling coins affected by -1 coin token?
    		context.addCoins(context.getCoins() * 2);
    		context.hasDoubledCoins = true;
    	}
    }
    
    private void gladiator(Game game, MoveContext context, Player currentPlayer) {
    	boolean revealedCopy = false;
    	if (currentPlayer.hand.size() > 0) {
    		Card card = currentPlayer.controlPlayer.gladiator_revealedCard(context);
            if (card == null) {
                card = Util.randomCard(currentPlayer.hand);
            } else if (!currentPlayer.hand.contains(card)) {
                Util.playerError(currentPlayer, "Gladiator revealed card error, picking random card.");
                card = Util.randomCard(currentPlayer.hand);
            }
            currentPlayer.reveal(card, this.controlCard, context);
            Player nextPlayer = game.getNextPlayer();
            if (nextPlayer.getHand().contains(card)) {
            	MoveContext nextPlayerContext = new MoveContext(game, nextPlayer);
            	revealedCopy = nextPlayer.controlPlayer.gladiator_revealCopy(nextPlayerContext, currentPlayer, card);
            }
    	}
    	if (!revealedCopy) {
    		context.addCoins(1);
    		 AbstractCardPile pile = game.getPile(Cards.gladiator);
    		 if (pile != null && pile.getCount() > 0 && pile.card() == Cards.gladiator) {
    			 Card gladiator = pile.removeCard();
    			 currentPlayer.trash(gladiator, this.controlCard, context);
    		 }
    	}
    }
    
    private void legionary(Game game, MoveContext context, Player currentPlayer) {
    	ArrayList<Player> attackedPlayers = new ArrayList<Player>();
    	for (Player player : context.game.getPlayersInTurnOrder()) {
            if (player != currentPlayer && !Util.isDefendedFromAttack(context.game, player, this)) {
            	attackedPlayers.add(player);
            }
    	}
    	if (currentPlayer.hand.size() == 0) {
    		return;
    	}
    	if (currentPlayer.hand.contains(Cards.gold) && currentPlayer.controlPlayer.legionary_revealGold(context)) {
    		for (Player player : attackedPlayers) {
				player.attacked(this.controlCard, context);
	            MoveContext playerContext = new MoveContext(game, player);
	            playerContext.attackedPlayer = player;
	            int keepCardCount = 2;
                if (player.hand.size() > keepCardCount) {
                    Card[] cardsToKeep = player.controlPlayer.legionary_attack_cardsToKeep(playerContext);
                    player.discardRemainingCardsFromHand(playerContext, cardsToKeep, this.controlCard, keepCardCount);
                    game.drawToHand(playerContext, this, 1);
                }
	        }
    	}
    }
    
    private void opulentCastle(Game game, MoveContext context, Player currentPlayer) {
        Card[] cards = currentPlayer.controlPlayer.opulentCastle_cardsToDiscard(context);
        for(Card card : cards) {
        	if (!card.is(Type.Victory, currentPlayer)) {
        		Util.playerError(currentPlayer, "Opulent Castle choice error, trying to discard non-victory cards, ignoring.");
        		cards = null;
        	}
        }
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
                Util.playerError(currentPlayer, "Opulent Castle discard error, trying to discard cards not in hand, ignoring extra.");
            }
            
            context.addCoins(2 * numberOfCards);
        }
    }
    
    private void overlord(Game game, MoveContext context, Player currentPlayer) {
        // Already impersonating another card?
        if (!this.isImpersonatingAnotherCard()) {
            // Get card to impersonate
        	Card cardToImpersonate = currentPlayer.controlPlayer.overlord_actionCardToImpersonate(context);
            if (cardToImpersonate != null 
                && !game.isPileEmpty(cardToImpersonate)
                && Cards.isSupplyCard(cardToImpersonate)
                && cardToImpersonate.is(Type.Action, null)
                && cardToImpersonate.getCost(context) < this.controlCard.getCost(context)
                && cardToImpersonate.getDebtCost(context) == 0
            	&& !cardToImpersonate.costPotion()
                && (context.golemInEffect == 0 || cardToImpersonate != Cards.golem)) {
                GameEvent event = new GameEvent(GameEvent.EventType.CardNamed, (MoveContext) context);
                event.card = cardToImpersonate;
                event.responsible = this;
                game.broadcastEvent(event);
                this.startImpersonatingCard(cardToImpersonate.getTemplateCard().instantiate());
            } else {
                Card[] cards = game.getActionsInGame();
                if (cards.length != 0 && cardToImpersonate != null) {
                    Util.playerError(currentPlayer, "Overlord returned invalid card (" + cardToImpersonate.getName() + "), ignoring.");
                }
                return;
            }
        }

        // Play the impersonated card
        CardImpl cardToPlay = (CardImpl) this.impersonatingCard;
        context.freeActionInEffect++;
        cardToPlay.play(game, context, false);
        context.freeActionInEffect--;

        // impersonated card stays in play until next turn?
        if (cardToPlay.trashOnUse) {
            int idx = currentPlayer.playedCards.lastIndexOf(this);
            if (idx >= 0) currentPlayer.playedCards.remove(idx);
            currentPlayer.trash(this, null, context);
        } else if (cardToPlay.is(Type.Duration, currentPlayer) && !cardToPlay.equals(Cards.outpost)) {
            if (!this.controlCard.movedToNextTurnPile) {
                this.controlCard.movedToNextTurnPile = true;
                int idx = currentPlayer.playedCards.lastIndexOf(this);
                if (idx >= 0) {
                    currentPlayer.playedCards.remove(idx);
                    currentPlayer.nextTurnCards.add(this);
                }
            }
        }
    }
    
    private void patrician(Game game, MoveContext context, Player currentPlayer) {
    	Card c = game.draw(context, Cards.patrician, 1);
        if (c != null) {
            currentPlayer.reveal(c, this.controlCard, context);
            if (c.getCost(context) >= 5) {
                currentPlayer.hand.add(c);
            } else {
                currentPlayer.putOnTopOfDeck(c, context, true);
            }
        }
    }
    
    private void royalBlacksmith(Game game, MoveContext context, Player currentPlayer) {
    	int numCoppers = 0;
    	for (int i = 0; i < currentPlayer.hand.size(); i++) {
            Card card = currentPlayer.hand.get(i);
            currentPlayer.reveal(card, this.controlCard, context);
            if (card.equals(Cards.copper)) {
            	numCoppers++;
            }
        }
    	
    	for (int i = 0; i < numCoppers; i++) {
    		for (int j = 0; j < currentPlayer.hand.size(); j++) {
    			Card card = currentPlayer.hand.get(j);
    			if (card.equals(Cards.copper)) {
                    currentPlayer.discard(currentPlayer.hand.remove(j), this.controlCard, context);
                    break;
    			}
    		}
        }
    }
    
    private void sacrifice(Game game, MoveContext context, Player currentPlayer) {
    	if(currentPlayer.getHand().size() == 0) return;
        Card cardToTrash = currentPlayer.controlPlayer.sacrifice_cardToTrash(context);
        if (cardToTrash == null) {
            Util.playerError(currentPlayer, "Sacrifice did not return a card to trash, trashing random card.");
            cardToTrash = Util.randomCard(currentPlayer.getHand());
        }
        
        currentPlayer.hand.remove(cardToTrash);
        currentPlayer.trash(cardToTrash, this.controlCard, context);
        boolean isAction = cardToTrash.is(Type.Action, cardToTrash.behaveAsCard().getKind() == Kind.Fortress ? currentPlayer : null);
        boolean isTreasure = cardToTrash.is(Type.Treasure);
        boolean isVictory = cardToTrash.is(Type.Victory);
        
        if (isAction) {
        	game.drawToHand(context, this, 2);
        	game.drawToHand(context, this, 1);
        	context.actions += 2;
        }
        if (isTreasure) {
        	context.addCoins(2);
        }
        if (isVictory) {
        	currentPlayer.addVictoryTokens(context, 2);
        }
    }
    
    private void settlers(Game game, MoveContext context, Player currentPlayer) {
        if (currentPlayer.discard.isEmpty()) return;
        int coppers = 0;
        int settlers = 0;
        for (Iterator<Card> it = currentPlayer.discard.iterator(); it.hasNext();) {
            Card card = it.next();
            if (Cards.copper.equals(card)) {
                coppers++;
            }
            if (Cards.settlers.equals(card)) {
                settlers++;
            }
        }
        if (currentPlayer.controlPlayer.settlers_copperIntoHand(context, coppers, settlers)) {
        	for (Iterator<Card> it = currentPlayer.discard.iterator(); it.hasNext();) {
                Card card = it.next();
                if (Cards.copper.equals(card)) {
                    currentPlayer.reveal(card, this.controlCard, context);
                    it.remove();
                    currentPlayer.hand.add(card);
                    break;
                }
            }
        }
    }
    
    private void smallCastle(Game game, MoveContext context, Player currentPlayer) {
    	boolean didTrash = false;
    	if (currentPlayer.controlPlayer.smallCastle_shouldTrashSmallCastlePlayed(context)) {
    		if (!this.controlCard.movedToNextTurnPile) {
                this.controlCard.movedToNextTurnPile = true;
                currentPlayer.playedCards.remove(currentPlayer.playedCards.lastIndexOf(this.controlCard));
                currentPlayer.trash(this.controlCard, this.controlCard, context);
                didTrash = true;
            }
        } else if (currentPlayer.getHand().size() > 0) {
        	int numCastles = 0;
        	Card handCastle = null;
        	for (Card c : currentPlayer.getHand()) {
        		if (c.is(Type.Castle, currentPlayer)) {
        			numCastles++;
        			handCastle = c;
        		}
        	}
        	if (numCastles == 1) {
        		currentPlayer.getHand().remove(handCastle);
        		currentPlayer.trash(handCastle, this.controlCard, context);
        		didTrash = true;
        	} else if (numCastles > 1) {
        		Card cardToTrash = currentPlayer.controlPlayer.smallCastle_castleToTrash(context);
            	if (cardToTrash == null || !currentPlayer.getHand().contains(cardToTrash) || !cardToTrash.is(Type.Castle, currentPlayer)) {
                    Util.playerError(currentPlayer, "Small Castle trash error, trashing last castle.");
                    cardToTrash = handCastle;
                }
            	currentPlayer.getHand().remove(cardToTrash);
        		currentPlayer.trash(cardToTrash, this.controlCard, context);
        		didTrash = true;
        	}
        }
    	if (!didTrash)
    		return;
    	//Assuming gaining a gained castle can only from the castle pile
    	currentPlayer.gainNewCard(Cards.virtualCastle, this.controlCard, context);
    }
    
    private void temple(Game game, MoveContext context, Player currentPlayer) {
    	if (currentPlayer.getHand().size() > 0) {
    		Card[] cards = currentPlayer.controlPlayer.temple_cardsToTrash(context);
    		if (cards == null || cards.length == 0) {
                Util.playerError(currentPlayer, "Temple trash error, not trashing enough cards, trashing first.");
                cards = new Card[]{currentPlayer.getHand().get(0)};
    		}
    		if (cards.length > 3) {
    			Util.playerError(currentPlayer, "Temple trash error, trashing too many cards, trashing first.");
                cards = new Card[]{currentPlayer.getHand().get(0)};
    		}
    		Set<Card> differentCards = new HashSet<Card>();
    		for (Card c : cards) {
    			if (differentCards.contains(c)) {
    				Util.playerError(currentPlayer, "Temple trash error, trashing duplicate cards, trashing one.");
                    cards = new Card[]{c};
                    break;
    			}
    			differentCards.add(c);
    		}
    		
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
    	game.addPileVpTokens(Cards.temple, 1, context);
    }
    
    private void wildHunt(Game game, MoveContext context, Player currentPlayer) {
    	if (currentPlayer.controlPlayer.wildHunt_chooseOption(context) == WildHuntOption.Draw3AndPlaceToken) {
    		context.game.drawToHand(context, this.controlCard, 3);
    		context.game.drawToHand(context, this.controlCard, 2);
    		context.game.drawToHand(context, this.controlCard, 1);
    		context.game.addPileVpTokens(Cards.wildHunt, 1, context);
    	} else {
    		if (Cards.estate.equals(currentPlayer.gainNewCard(Cards.estate, this.controlCard, context))) {
    			context.game.removePileVpTokens(Cards.wildHunt, context.game.getPileVpTokens(Cards.wildHunt), context);
    		}
    	}
    }
	
    //Events
    
    private void advance(MoveContext context) {
    	Player player = context.getPlayer();
    	boolean hasAction = false;
    	for(Card c : player.getHand()) {
    		if (c.is(Type.Action, player)) {
    			hasAction = true;
    			break;
    		}
    	}
    	if (!hasAction) return;
    	
    	Card cardToTrash = player.controlPlayer.advance_actionToTrash(context);
    	if(cardToTrash != null) {
            if(!player.getHand().contains(cardToTrash) || !cardToTrash.is(Type.Action, player)) {
                Util.playerError(player, "Advance returned invalid card to trash from hand, ignoring.");
            } else {
                player.hand.remove(cardToTrash);
                player.trash(cardToTrash, this.controlCard, context);
                //TODO: check if there is an action card to gain
                Card cardToGain = player.controlPlayer.advance_cardToObtain(context);
                if (cardToGain == null || 
                		!cardToGain.is(Type.Action) || 
                		cardToGain.getCost(context) > 6 || 
                		cardToGain.getDebtCost(context) > 0 || 
                		cardToGain.costPotion()) {
                	Util.playerError(player, "Advance returned invalid card to gain, ignoring.");
                	//TODO: pick some valid action card and gain that instead
                } else {
                	player.gainNewCard(cardToGain, this.controlCard, context);
                }
            }
        }
    }
    
    private void annex(MoveContext context) {
    	Player player = context.getPlayer();
    	ArrayList<Card> toKeepInDiscard = new ArrayList<Card>();
    	Card card = null;
    	final int MAX_CARDS_TO_KEEP = 5;
    	do {
    		if (player.discard.size() == 0)
    			break;
    		Card[] sortedCards = player.discard.toArrayListClone().toArray(new Card[0]);
    		Arrays.sort(sortedCards, new Util.CardCostComparator());
    		card = player.controlPlayer.annex_cardToKeepInDiscard(context, sortedCards, MAX_CARDS_TO_KEEP - toKeepInDiscard.size());
    		if (!player.discard.contains(card)) card = null;
    		if (card != null) {
    			player.discard.remove(card);
    			toKeepInDiscard.add(card);
    		}
    		
    	} while (card != null && toKeepInDiscard.size() < MAX_CARDS_TO_KEEP);
    	
    	while(!player.discard.isEmpty()) {
    		player.deck.add(player.discard.removeLastCard());
    	}
    	while(!toKeepInDiscard.isEmpty()) {
    		player.discard.add(toKeepInDiscard.remove(0));
    	}
    	player.shuffleDeck(context, this.controlCard);
    	player.gainNewCard(Cards.duchy, this.controlCard, context);
    }
    
    private void banquet(MoveContext context) {
    	context.player.gainNewCard(Cards.silver, this.controlCard, context);
    	context.player.gainNewCard(Cards.silver, this.controlCard, context);
    	Card toGain = context.player.controlPlayer.banquet_cardToObtain(context);
    	if (toGain == null || toGain.getCost(context) > 5 || toGain.getDebtCost(context) > 0 || 
    			toGain.costPotion() || toGain.is(Type.Victory) ||
    			!context.game.isCardInGame(toGain) || context.game.isPileEmpty(toGain) || !Cards.isSupplyCard(toGain)) {
    		Util.playerError(context.player, "Annex - selected invalid card");
    		return;
    	}
    	context.player.gainNewCard(toGain, this.controlCard, context);
    }
    
    private void conquest(MoveContext context) {
    	context.player.gainNewCard(Cards.silver, this.controlCard, context);
    	context.player.gainNewCard(Cards.silver, this.controlCard, context);
    	int silversGained = context.getNumCardsGainedThisTurn(Kind.Silver);
    	if (silversGained > 0)
    		context.player.addVictoryTokens(context, silversGained);
    }
    
    private void delve(MoveContext context) {
    	context.player.gainNewCard(Cards.silver, this.controlCard, context);
    }
    
    private void dominate(MoveContext context) {
    	Card gainedCard = context.player.gainNewCard(Cards.province, this.controlCard, context);
    	if (Cards.province.equals(gainedCard)) {
    		context.getPlayer().controlPlayer.addVictoryTokens(context, 9);
    	}
    }
    
    private void donate(MoveContext context) {
    	++context.donatesBought;
    }
    
    private void ritual(MoveContext context) {
    	Player p = context.getPlayer();
    	CardList hand = p.getHand();
    	if (Cards.curse.equals(p.gainNewCard(Cards.curse, Cards.ritual, context))) {
    		if (hand.size() == 0) return;
    		Card toTrash = p.controlPlayer.ritual_cardToTrash(context);
    		if (toTrash == null || !hand.contains(toTrash)) {
    			Util.playerError(p, "Invalid card selected for Ritual, selecting first card");
    			toTrash = hand.get(0);
    		}
    		int trashCost = toTrash.getCost(context);
			toTrash = hand.get(toTrash);
			hand.remove(toTrash);
			p.trash(toTrash, this.controlCard, context);
			p.addVictoryTokens(context, trashCost);
    	}
    }
    
    private void saltTheEarth(MoveContext context) {
    	context.getPlayer().addVictoryTokens(context, 1);
    	Card toTrash = context.getPlayer().controlPlayer.saltTheEarth_cardToTrash(context);
    	AbstractCardPile pile = context.game.getPile(toTrash);
    	if (toTrash == null || !toTrash.is(Type.Victory) || pile.isEmpty() || !pile.card().equals(toTrash)) {
    		Util.playerError(context.getPlayer(), "Salt the Earth picked invalid card, picking province");
    		toTrash = Cards.province;
    	}
    	context.getPlayer().trash(pile.removeCard(), this.controlCard, context);
    }
    
    private void tax(MoveContext context) {
    	Card card = context.getPlayer().controlPlayer.tax_supplyToTax(context);
        if (card == null || !context.game.isCardInGame(card)) {
            Util.playerError(context.getPlayer(), "Tax error, choosing arbitrary Supply.");
            for (Card c : context.game.getCardsInGame()) {
            	if (Cards.isSupplyCard(c)) {
            		card = c;
            		break;
            	}
            }
        }
        context.game.addPileDebtTokens(card, 2, context);
    }

    private void triumph(MoveContext context) {
    	Card gainedCard = context.player.gainNewCard(Cards.estate, this.controlCard, context);
    	if (Cards.estate.equals(gainedCard)) {
    		context.getPlayer().addVictoryTokens(context, context.getNumCardsGainedThisTurn());
    	}
    }
    
    private void wedding(MoveContext context) {
    	context.player.gainNewCard(Cards.gold, this.controlCard, context);
    }

    private void windfall(MoveContext context) {
    	if (context.getPlayer().getDeckSize() == 0 && context.getPlayer().getDiscardSize() == 0) {
    		for (int i = 0; i < 3; ++i) {
    			context.player.gainNewCard(Cards.gold, this.controlCard, context);
    		}
    	}
    }
}
