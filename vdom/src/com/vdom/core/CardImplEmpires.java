package com.vdom.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.vdom.api.Card;
import com.vdom.core.Cards.Kind;
import com.vdom.core.Player.EncampmentOption;

public class CardImplEmpires extends CardImpl {
	private static final long serialVersionUID = 1L;

	public CardImplEmpires(CardImpl.Builder builder) {
		super(builder);
	}

	protected CardImplEmpires() { }

	@Override
	protected void additionalCardActions(Game game, MoveContext context, Player currentPlayer) {
		switch(getKind()) {
		case BustlingVillage:
        	bustlingVillage(game, context, currentPlayer);
        	break;
        case Catapult:
        	catapult(game, context, currentPlayer);
        	break;
        case ChariotRace:
        	chariotRace(game, context, currentPlayer);
        	break;
        case CityQuarter:
        	cityQuarter(game, context, currentPlayer);
        	break;
        case Encampment:
        	encampment(game, context, currentPlayer);
        	break;
        case Enchantress:
        	durationAttack(game, context, currentPlayer);
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
        case OpulentCastle:
        	opulentCastle(game, context, currentPlayer);
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
        case Conquest:
        	conquest(context);
        	break;
        case Delve:
        	delve(context);
        	break;
        case Dominate:
        	dominate(context);
        	break;
        case Triumph:
        	triumph(context);
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
	
    //Events
    
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

    private void triumph(MoveContext context) {
    	Card gainedCard = context.player.gainNewCard(Cards.estate, this.controlCard, context);
    	if (Cards.estate.equals(gainedCard)) {
    		context.getPlayer().addVictoryTokens(context, context.getNumCardsGainedThisTurn());
    	}
    }

    private void windfall(MoveContext context) {
    	if (context.getPlayer().getDeckSize() == 0 && context.getPlayer().getDiscardSize() == 0) {
    		for (int i = 0; i < 3; ++i) {
    			context.player.gainNewCard(Cards.gold, this.controlCard, context);
    		}
    	}
    }
}
