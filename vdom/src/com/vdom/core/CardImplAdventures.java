package com.vdom.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.vdom.api.Card;
import com.vdom.api.GameEvent;
import com.vdom.core.Player.AmuletOption;
import com.vdom.core.Player.QuestOption;

public class CardImplAdventures extends CardImpl {
	private static final long serialVersionUID = 1L;

	public CardImplAdventures(CardImpl.Builder builder) {
		super(builder);
	}
	
	protected CardImplAdventures() { }

	@Override
	protected void additionalCardActions(Game game, MoveContext context, Player currentPlayer) {
		switch(getKind()) {
		case Amulet:
            amulet(game, context, currentPlayer);
            break;
        case Artificer:
        	artificer(game, context, currentPlayer);
            break;
        case BridgeTroll:
        	bridgeTroll(game, context, currentPlayer);
            break;
        case CoinOfTheRealm:
        	putOnTavern(game, context, currentPlayer);
        	break;
        case Disciple:
        	disciple(game, context, currentPlayer);
            break;
		case DistantLands:
        case Duplicate:
        case Guide:
        case Ratcatcher:
        case RoyalCarriage:
        case Teacher:
        case Transmogrify:
        case WineMerchant:
        	putOnTavern(game, context, currentPlayer);
            break;
        case Dungeon:
            discardMultiple(context, currentPlayer, 2);
            break;
        case Fugitive:
            fugitive(context, currentPlayer);
            break;
        case Gear:
            gear(context, currentPlayer);
            break;
        case Giant:
            giant(game, context, currentPlayer);
            break;
        case HauntedWoods:
        	durationAttack(game, context, currentPlayer);
        	break;
        case Hero:
            hero(game, context, currentPlayer);
            break;
        case Magpie:
            magpie(game, context, currentPlayer);
            break;
        case Messenger:
            messenger(game, context, currentPlayer);
            break;
        case Miser:
            miser(game, context, currentPlayer);
            break;
        case Ranger:
            ranger(game, context, currentPlayer);
            break;
        case Raze:
        	raze(game, context, currentPlayer);
        	break;
        case Relic:
        	relic(context, currentPlayer, game);
        	break;
        case Soldier:
            soldier(game, context, currentPlayer);
            break;
        case Storyteller:
        	storyteller(game, context, currentPlayer);
            break;
        case SwampHag:
        	durationAttack(game, context, currentPlayer);
        	break;
        case TreasureHunter:
        	treasureHunter(game, context, currentPlayer);
            break;
        case TreasureTrove:
        	treasureTrove(context, currentPlayer, game);
        	break;
        case Warrior:
        	warrior(game, context, currentPlayer);
            break;
		default:
			break;
		}
	}
	
	@Override
    public void isBuying(MoveContext context) {
		super.isBuying(context);
        switch (this.getControlCard().getKind()) {
        case Messenger:
        	/* This buy is already in totalCardsBoughtThisTurn */
        	if(context.totalCardsBoughtThisTurn + context.totalEventsBoughtThisTurn == 1) {
                Card card = context.getPlayer().controlPlayer.messenger_cardToObtain(context);
                if (card != null && card.getCost(context) <= 4 && card.getDebtCost(context) == 0 && !card.costPotion()) {
                	Card cardGained = context.getPlayer().gainNewCard(card, this.getControlCard(), context);
                	if (cardGained != null && cardGained.equals(card)) {
	                    for (Player player : context.game.getPlayersInTurnOrder()) {
	                        if (player != context.getPlayer()) {
	                            player.gainNewCard(card, this.getControlCard(), new MoveContext(context.game, player));
	                        }
	                    }
                	}
                }
        	}
            break;
        case Port:
            context.player.gainNewCard(Cards.port, this, context);
            break;
        //Events
        case Alms:
        	alms(context);
            break;
        case Ball:
        	ball(context);
        	break;
        case Bonfire:
        	bonfire(context);
            break;
        case Borrow:
        	borrow(context);
            break;
        case Expedition:
        	context.totalExpeditionBoughtThisTurn += 2;
            break;
        case Ferry:
        	ferry(context);
    		break;
        case Inheritance:
        	inheritance(context);
        	break;
        case LostArts:
        	lostArts(context);
        	break;
        case Mission:
        	mission(context);
        	break;
        case Pathfinding:
        	pathfinding(context);
        	break;
        case Pilgrimage:
        	pilgrimage(context);
        	break;
        case Plan:
        	plan(context);
        	break;
        case Quest:
        	quest(context);
        	break;
        case Raid:
        	raid(context);
            break;
        case Save:
        	save(context);
            break;
        case ScoutingParty:
        	scoutingParty(context);
            break;
        case Seaway:
        	seaway(context);
        	break;
        case Trade:
        	trade(context);
        	break;
        case Training:
        	training(context);
        	break;
        case TravellingFair:
        	context.travellingFairBought = true;
        default:
            break;
        }
        
        // test if prince lost track of any cards
        context.player.princeCardLeftThePlay(context.player);
    }
	
    private boolean call(MoveContext context) {
    	Player currentPlayer = context.getPlayer();
		if (!currentPlayer.tavern.remove(this.getControlCard())) {
			return false;
		}
        currentPlayer.playedCards.add(this.getControlCard());
       	GameEvent event = new GameEvent(GameEvent.EventType.CallingCard, (MoveContext) context);
        event.card = this.getControlCard();
        event.newCard = true;
        context.game.broadcastEvent(event);
        return true;
	}
    
    private void finishCall(MoveContext context) {
    	GameEvent event = new GameEvent(GameEvent.EventType.CalledCard, (MoveContext) context);
        event.card = this.getControlCard();
        context.game.broadcastEvent(event);
	}
    
    public void callWhenCardGained(MoveContext context, Card cardToGain) {
    	if (!callableWhenCardGained) return;
    	if (!call(context)) return;
    	Game game = context.game;
    	Player currentPlayer = context.getPlayer();
    	switch (this.getKind()) {
    	case Duplicate:
    		duplicate(cardToGain, context, game, currentPlayer);
    		break;
    	default:
    		break;
    	}
    	finishCall(context);
    }
    
	public void callWhenActionResolved(MoveContext context, Card resolvedAction) {
    	if (!callableWhenActionResolved) return;
    	if (!call(context)) return;
    	Game game = context.game;
    	Player currentPlayer = context.getPlayer();
    	switch (this.getKind()) {
    	case CoinOfTheRealm:
    		coinOfTheRealm(context, game, currentPlayer);
    		break;
    	case RoyalCarriage:
    		royalCarriage(resolvedAction, context, game, currentPlayer);
    	default:
    		break;
    	}
    	finishCall(context);
    }
	    
	public void callAtStartOfTurn(MoveContext context) {
    	if (!callableWhenTurnStarts) return;
    	if (!call(context)) return;
    	Game game = context.game;
    	Player currentPlayer = context.getPlayer();
    	switch (this.getKind()) {
    	case Guide:
    		guide(context, game, currentPlayer);
    		break;
    	case Ratcatcher:
    		ratcatcher(context, game, currentPlayer);
    		break;
    	case Teacher:
    		teacher(context, game, currentPlayer);
    		break;
    	case Transmogrify:
    		transmogrify(context, game, currentPlayer);
    		break;
    	default:
    		break;
    	}
    	finishCall(context);
    }
	
	
	private void amulet(Game game, MoveContext context, Player currentPlayer) {
        Player.AmuletOption option = currentPlayer.controlPlayer.amulet_chooseOption(context);

        if (option == null) {
            Util.playerError(currentPlayer, "Amulet option error, choosing +(1) Coin.");
            option = AmuletOption.AddGold;
        }
        if (option == Player.AmuletOption.AddGold) {
            context.addCoins(1);
        } else if (option == Player.AmuletOption.GainSilver) {
            currentPlayer.gainNewCard(Cards.silver, this.getControlCard(), context);
        } else if (option == Player.AmuletOption.TrashCard) {
            CardList hand = currentPlayer.getHand();
            if (hand.size() == 0) {
                return;
            }

            Card cardToTrash = currentPlayer.controlPlayer.amulet_cardToTrash(context);
            if (cardToTrash == null || !currentPlayer.hand.contains(cardToTrash)) {
                Util.playerError(currentPlayer, "Amulet card to trash error, trashing random card.");
                cardToTrash = Util.randomCard(currentPlayer.getHand());
            }
            currentPlayer.hand.remove(cardToTrash);
            currentPlayer.trash(cardToTrash, this.getControlCard(), context);
        }
    }

    private void artificer(Game game, MoveContext context, Player currentPlayer) {
    	int numberOfCards = 0;
        Card[] cards = currentPlayer.getHand().size() == 0 ? null : currentPlayer.controlPlayer.artificer_cardsToDiscard(context);
        if (cards != null) {
            for (Card card : cards) {
                if (card != null) {
                	if (currentPlayer.getHand().remove(card)) {
	                    currentPlayer.discard(card, this.getControlCard(), context);
	                    numberOfCards++;
                	}
                }
            }
            if (numberOfCards != cards.length) {
                Util.playerError(currentPlayer, "Artificer discard error, trying to discard cards not in hand, ignoring extra.");
            }
        }
        Card toObtain = currentPlayer.controlPlayer.artificer_cardToObtain(context, numberOfCards);
        if (toObtain != null) {
            // check cost
            if (toObtain.getCost(context) == numberOfCards && toObtain.getDebtCost(context) == 0 && !toObtain.costPotion()) {
            	currentPlayer.gainNewCard(toObtain, this.getControlCard(), context);
            }
        }
    }
    
    private void bridgeTroll(Game game, MoveContext context, Player currentPlayer)
    {
        for (Player targetPlayer : game.getPlayersInTurnOrder()) {
            if (targetPlayer != currentPlayer && !Util.isDefendedFromAttack(game, targetPlayer, this)) {
                MoveContext targetContext = new MoveContext(context.game, targetPlayer);
                targetContext.attackedPlayer = targetPlayer;
                targetPlayer.attacked(this.getControlCard(), context);
                targetPlayer.setMinusOneCoinToken(true, targetContext);
            }
        }
    }
    
    private void coinOfTheRealm(MoveContext context, Game game,
			Player currentPlayer) {
		context.actions += 2;
	}
    
    private void disciple(Game game, MoveContext context, Player currentPlayer) {    	
    	Card card = throneRoomKingsCourt(game, context, currentPlayer);
    	if(card != null && Cards.isSupplyCard(card))
            currentPlayer.gainNewCard(card, this.getControlCard(), context);
    }
    
    private void duplicate(Card cardToGain, MoveContext context, Game game, Player currentPlayer) {
		currentPlayer.gainNewCard(cardToGain, this.getControlCard(), context);
	}
	    
    private void fugitive(MoveContext context, Player currentPlayer) {
        if(currentPlayer.hand.size() > 0) {
            Card cardToDiscard = currentPlayer.controlPlayer.fugitive_cardToDiscard(context);
            if(cardToDiscard == null || !currentPlayer.hand.contains(cardToDiscard)) {
                Util.playerError(currentPlayer, "Returned an invalid card to discard with Fugitive, picking one for you.");
                cardToDiscard = currentPlayer.hand.get(0);
            }

            currentPlayer.hand.remove(cardToDiscard);
            currentPlayer.discard(cardToDiscard, this.getControlCard(), context);
        }
    }
    
    private void gear(MoveContext context, Player currentPlayer) {
    	boolean cardSetAside = false;
        Card[] cards = currentPlayer.getHand().size() == 0 ? null : currentPlayer.controlPlayer.gear_cardsToSetAside(context);
        if (cards != null && cards.length > 2) {
        	Util.playerError(currentPlayer, "Gear: Tried to set aside too many cards. Setting aside zero.");
        	cards = null;
        }
        if (cards != null && !Util.areCardsInHand(cards, context)) {
        	Util.playerError(currentPlayer, "Gear: Tried to set aside cards not in hand. Setting aside zero.");
        	cards = null;
        }
        if (cards != null) {
        	ArrayList<Card> gearCards = new ArrayList<Card>();
            for (Card card : cards) {
                if (card != null) {
                	cardSetAside = true;
                    currentPlayer.getHand().remove(card);
                    gearCards.add(card);
                    GameEvent event = new GameEvent(GameEvent.EventType.CardSetAsideGear, (MoveContext) context);
                    event.card = card;
                    event.setPrivate(true);
                    context.game.broadcastEvent(event);
                }
            }
            if (!gearCards.isEmpty()) {
            	currentPlayer.gear.add(gearCards);
            }
        }
        if (!cardSetAside && this.getControlCard().cloneCount == 1) {
            currentPlayer.nextTurnCards.remove(this.getControlCard());
            currentPlayer.playedCards.add(this.getControlCard());
        }
    }
    
    private void giant(Game game, MoveContext context, Player currentPlayer)
    {    	
        ArrayList<Player> playersToAttack = new ArrayList<Player>();
        for (Player targetPlayer : game.getPlayersInTurnOrder()) {
            if (targetPlayer != currentPlayer && !Util.isDefendedFromAttack(game, targetPlayer, this)) {
                playersToAttack.add(targetPlayer);
            }
        }
        if(currentPlayer.flipJourneyToken(context)) {
            context.addCoins(5);
            
            for (Player targetPlayer : playersToAttack) {
            	targetPlayer.attacked(this.getControlCard(), context);
                MoveContext targetContext = new MoveContext(game, targetPlayer);
                targetContext.attackedPlayer = targetPlayer;

                Card card = game.draw(targetContext, Cards.giant, 1);
                if (card != null) {
                    targetPlayer.reveal(card, this.getControlCard(), targetContext);
                    int cardCost = card.getCost(context);

                    if (!card.costPotion() && cardCost >= 3 && cardCost <= 6) {
                        targetPlayer.trash(card, this.getControlCard(), targetContext);
                    } else {
                        targetPlayer.discard(card, this.getControlCard(), targetContext);
                        targetPlayer.gainNewCard(Cards.curse, this.getControlCard(), targetContext);
                    }
                }
            }
        } else {
        	context.addCoins(1);
        }
    }
    
    private void guide(MoveContext context, Game game, Player currentPlayer) {
		if (currentPlayer.getHand().size() > 0) {
            while (!currentPlayer.getHand().isEmpty()) {
                currentPlayer.discard(currentPlayer.getHand().remove(0), this.getControlCard(), context);
            }
        }
		for (int i = 0; i < 5; ++i) {
			game.drawToHand(context, this.getControlCard(), 5 - i);
		}
		
	}

    private void hero(Game game, MoveContext context, Player currentPlayer)
    {
    	int numTreasuresAvailable = 0;
    	for (Card treasureCard : context.getCardsInGame(GetCardsInGameOptions.TopOfPiles, true, Type.Treasure)) {
    		if (Cards.isSupplyCard(treasureCard) && context.isCardOnTop(treasureCard)) {
    			numTreasuresAvailable++;
    		}
    	}
    	if (numTreasuresAvailable == 0)
    		return;
    	
    	Card newCard = currentPlayer.controlPlayer.hero_treasureToObtain(context);
    	
        if (!(newCard != null && newCard.is(Type.Treasure, null) && Cards.isSupplyCard(newCard) && context.isCardOnTop(newCard))) {
            Util.playerError(currentPlayer, "Hero treasure to obtain was invalid, picking random treasure from table.");
            for (Card treasureCard : context.getCardsInGame(GetCardsInGameOptions.TopOfPiles, true, Type.Treasure)) {
                if (Cards.isSupplyCard(treasureCard) && context.getCardsLeftInPile(treasureCard) > 0) {
                    newCard = treasureCard;
                    break;
                }
            }
        }
        
        currentPlayer.gainNewCard(newCard, this.getControlCard(), context);
    }
    
    private void magpie(Game game, MoveContext context, Player currentPlayer)
    {
        Card c = game.draw(context, Cards.magpie, 1);
        if (c != null) {
            currentPlayer.reveal(c, this.getControlCard(), context);
            if (c.is(Type.Treasure, currentPlayer)) {
                currentPlayer.hand.add(c);
            } else {
                currentPlayer.putOnTopOfDeck(c, context, true);
            }
            if ((c.is(Type.Victory, currentPlayer)) || (c.is(Type.Action, currentPlayer))) {
                currentPlayer.gainNewCard(Cards.magpie, this.getControlCard(), context);
            }
        }
    }
    
    private void messenger(Game game, MoveContext context, Player currentPlayer) {
    	if (currentPlayer.getDeckSize() == 0)
    		return;
        boolean discard = currentPlayer.controlPlayer.messenger_shouldDiscardDeck(context);
        if (discard) {
            GameEvent event = new GameEvent(GameEvent.EventType.DeckPutIntoDiscardPile, (MoveContext) context);
            game.broadcastEvent(event);
            while (currentPlayer.getDeckSize() > 0) {
                currentPlayer.discard(game.draw(context, Cards.messenger, 0), this.getControlCard(), null, false, false);
            }
        }
    }

    private void miser(Game game, MoveContext context, Player currentPlayer) {
        boolean takeTreasure = currentPlayer.controlPlayer.miser_shouldTakeTreasure(context);
        if (takeTreasure) {
        	context.addCoins(currentPlayer.getMiserTreasure());
        }
        else {
            for (int i = 0; i < currentPlayer.hand.size(); i++) {
                Card card = currentPlayer.hand.get(i);
                if (card.equals(Cards.copper)) {
                    Card thisCard = currentPlayer.hand.remove(i);
                    currentPlayer.tavern.add(thisCard);
                    GameEvent event = new GameEvent(GameEvent.EventType.CardSetAsideOnTavernMat, (MoveContext) context);
                    event.card = thisCard;
                    game.broadcastEvent(event);
                    break;
                }
            }    
        }        	
    }

    private void ranger(Game game, MoveContext context, Player currentPlayer) {
        if(currentPlayer.flipJourneyToken(context)) {
        	for (int i=0; i < 5; i++) {
                context.game.drawToHand(context, this, 5 - i);
            }
        }
    }
    
    private void ratcatcher(MoveContext context, Game game, Player currentPlayer) {
		if (currentPlayer.getHand().size() == 0) {
			return;
		}
        Card cardToTrash = currentPlayer.getHand().size() == 1 ? currentPlayer.getHand().get(0) : currentPlayer.controlPlayer.ratcatcher_cardToTrash((MoveContext) context);
        if (cardToTrash != null) {
        	if (!currentPlayer.getHand().contains(cardToTrash)) {
        		Util.playerError(currentPlayer, "Ratcatcher error, invalid card to trash, trashing random card.");
        		cardToTrash = Util.randomCard(currentPlayer.getHand());
        	} else {
        		currentPlayer.hand.remove(cardToTrash);
        		currentPlayer.trash(cardToTrash, this.getControlCard(), context);
        	}
        }
	}
    
    private void raze(Game game, MoveContext context, Player currentPlayer) {
    	int trashCost = 0;
    	if (currentPlayer.controlPlayer.raze_shouldTrashRazePlayed(context, this.getControlCard())) {
    		if (!this.getControlCard().movedToNextTurnPile) {
                this.getControlCard().movedToNextTurnPile = true;
                trashCost = this.getControlCard().getCost(context);
                currentPlayer.playedCards.remove(currentPlayer.playedCards.lastIndexOf(this.getControlCard()));
                currentPlayer.trash(this.getControlCard(), this.getControlCard(), context);
            }
        } else if (currentPlayer.getHand().size() > 0) {
        	Card cardToTrash = currentPlayer.controlPlayer.raze_cardToTrash(context);
        	if (cardToTrash == null || !currentPlayer.getHand().contains(cardToTrash)) {
                Util.playerError(currentPlayer, "Raze trash error, trashing a random card.");
                cardToTrash = Util.randomCard(currentPlayer.getHand());
            }
        	trashCost = cardToTrash.getCost(context);
        	currentPlayer.getHand().remove(cardToTrash);
            currentPlayer.trash(cardToTrash, this.getControlCard(), context);
        }
    	if (trashCost == 0)
    		return;
    	else if (trashCost == 1) {
    		Card c = game.draw(context, Cards.raze, 1);
    		if (c != null)
    			currentPlayer.hand.add(c);
    	} else {
    		ArrayList<Card> lookAtCards = new ArrayList<Card>(trashCost);
    		for (int i = 0; i < trashCost; ++i) {
    			Card c = game.draw(context, Cards.raze, trashCost - i);
    			if (c == null)
    				break;
    			lookAtCards.add(c);
    		}
    		if (lookAtCards.size() > 0) {
	    		Card cardToKeep = lookAtCards.size() == 1 ? lookAtCards.get(0) : currentPlayer.controlPlayer.raze_cardToKeep(context, lookAtCards.toArray(new Card[0]));
	    		if (cardToKeep == null || !lookAtCards.contains(cardToKeep)) {
	    			Util.playerError(currentPlayer, "Raze keep error. Keeping random card.");
	    			cardToKeep = Util.randomCard(lookAtCards);
	    		}
	    		lookAtCards.remove(cardToKeep);
	    		currentPlayer.getHand().add(cardToKeep);
	    		for (Card c : lookAtCards) {
	                currentPlayer.discard(c, this.getControlCard(), context);
	            }
    		}
    	}
    }
    
    protected void relic(MoveContext context, Player player, Game game) {
        ArrayList<Player> playersToAttack = new ArrayList<Player>();
        for (Player targetPlayer : game.getPlayersInTurnOrder()) {
            if (targetPlayer != player && !Util.isDefendedFromAttack(game, targetPlayer, this)) {
                playersToAttack.add(targetPlayer);
            }
        }

        for (Player targetPlayer : playersToAttack) {
            targetPlayer.attacked(this.getControlCard(), context);
            MoveContext targetContext = new MoveContext(context.game, targetPlayer);
            targetContext.attackedPlayer = targetPlayer;
        	targetPlayer.setMinusOneCardToken(true, targetContext);
        }
    }
    
    private void royalCarriage(Card resolvedAction, MoveContext context, Game game, Player currentPlayer) {
		CardImpl cardToPlay = (CardImpl) resolvedAction; 
		
		context.freeActionInEffect++;

        cardToPlay.getControlCard().cloneCount += 1;
        boolean beingThroned = cardToPlay.numberTimesAlreadyPlayed > 0;
        cardToPlay.numberTimesAlreadyPlayed = beingThroned ? cardToPlay.numberTimesAlreadyPlayed + 1 : 1;
        cardToPlay.play(game, context, false);
        if (!beingThroned)
        	cardToPlay.numberTimesAlreadyPlayed = 0; //TODO: not sure if this is always right
        context.freeActionInEffect--;
        // If the cardToPlay was a knight, and was trashed, reset clonecount
        if (cardToPlay.is(Type.Knight, currentPlayer) && !currentPlayer.playedCards.contains(cardToPlay) && game.trashPile.contains(cardToPlay)) {
            cardToPlay.getControlCard().cloneCount = 1;
        }

        if (cardToPlay.is(Type.Duration, currentPlayer) && !cardToPlay.equals(Cards.tactician)) {
            // Need to move royal carriage card to NextTurnCards first
            // (but does not play)
            if (!this.getControlCard().movedToNextTurnPile) {
                this.getControlCard().movedToNextTurnPile = true;
                int idx = currentPlayer.playedCards.lastIndexOf(this.getControlCard());
                int ntidx = currentPlayer.nextTurnCards.size() - 1;
                if (idx >= 0 && ntidx >= 0) {
                    currentPlayer.playedCards.remove(idx);
                    currentPlayer.nextTurnCards.add(ntidx, this.getControlCard());
                }
            }
        }
	}

    private void soldier(Game game, MoveContext context, Player currentPlayer) {       
    	context.addCoins(context.countAttackCardsInPlay() - 1); //without this soldier
    	for (Player player : context.game.getPlayersInTurnOrder()) {
            if (player != currentPlayer && !Util.isDefendedFromAttack(context.game, player, this)) {
                player.attacked(this.getControlCard(), context);
                MoveContext playerContext = new MoveContext(game, player);
                playerContext.attackedPlayer = player;

                if (player.hand.size() >= 4) {
                    Card cardToDiscard = player.controlPlayer.soldier_cardToDiscard(playerContext);
                    if(cardToDiscard == null || !player.hand.contains(cardToDiscard)) {
                        Util.playerError(player, "Returned an invalid card to discard with Soldier, picking one for you.");
                        cardToDiscard = player.hand.get(0);
                    }

                    player.hand.remove(cardToDiscard);
                    player.discard(cardToDiscard, this.getControlCard(), playerContext);
                }
            }
        }
    }

    private void storyteller(Game game, MoveContext context, Player currentPlayer) {
        // play up to 3 treasures
        context.game.playTreasures(currentPlayer, context, 3, this.getControlCard());
        
        int coins = context.getCoins();
        context.spendCoins(coins);
        
        for (int i = 0; i < coins; i++) {
        	game.drawToHand(context, this, coins - i);
        }
    }
    
    private void teacher(MoveContext context, Game game, Player currentPlayer) {
		//look to see if we have a free pile
		int numFreePiles = 0;
		Card lastFreePile = null;
		for (Card c : game.getCardsInGame(GetCardsInGameOptions.TopOfPiles.Placeholders, true, Type.Action)) {
			if (game.getPlayerSupplyTokens(c, currentPlayer).size() == 0) {
				numFreePiles++;
				lastFreePile = c;
			}
		}
		if (numFreePiles == 0)
			return;
		
		PlayerSupplyToken token = currentPlayer.controlPlayer.teacher_tokenTypeToMove(context);
		
		if (token == null || !(token == PlayerSupplyToken.PlusOneCard ||
				token == PlayerSupplyToken.PlusOneAction ||
				token == PlayerSupplyToken.PlusOneBuy ||
				token == PlayerSupplyToken.PlusOneCoin)) {
			Util.playerError(currentPlayer, "Teacher error: didn't select a valid token to move, selecting +Buy");
			token = PlayerSupplyToken.PlusOneBuy;
		}
		
		Card card;
		if (numFreePiles == 1) {
			card = lastFreePile;
		} else {
			card = context.getPlayer().controlPlayer.teacher_actionCardPileToHaveToken(context, token);
		}
		
    	placeToken(context, card, token);
	}
    
    private void transmogrify(MoveContext context, Game game, Player currentPlayer) {
		if (currentPlayer.getHand().size() == 0) {
			return;
		}
        Card cardToTrash = currentPlayer.controlPlayer.transmogrify_cardToTrash(context);
        if (cardToTrash != null) {
        	if (!currentPlayer.getHand().contains(cardToTrash)) {
        		Util.playerError(currentPlayer, "Transmogrify error, invalid card to trash, trashing random card.");
        		cardToTrash = Util.randomCard(currentPlayer.getHand());
        	} else {
        		currentPlayer.hand.remove(cardToTrash);
        		currentPlayer.trash(cardToTrash, null, context);
        	}
        }
        int value = cardToTrash.getCost(context) + 1;
        int debtValue = cardToTrash.getDebtCost(context);
        boolean potion = cardToTrash.costPotion();
        
        Card card = currentPlayer.controlPlayer.transmogrify_cardToObtain(context, value, debtValue, potion);
        if (card != null) {
            if (card.getCost(context) > value || card.getDebtCost(context) > debtValue || (card.costPotion() && !potion)) {
                Util.playerError(currentPlayer, "Transmogrify error, new card does not cost value of the old card +1 or less.");
            } else if (game.isPileEmpty(card)) {
            	Util.playerError(currentPlayer, "Transmogrify error, new card pile is empty.");
            } else if (!game.isCardOnTop(card)) {
            	Util.playerError(currentPlayer, "Transmogrify error, new card not in game.");
            } else {
            	currentPlayer.gainNewCard(card, this.getControlCard(), context);
            }
        }
	}
    
    private void treasureHunter(Game game, MoveContext context, Player currentPlayer) {
        for (int i = 0; i < context.getCardsObtainedByLastPlayer().size(); i++) {
            currentPlayer.gainNewCard(Cards.silver, this, context);
        }
    }
    
    protected void treasureTrove(MoveContext context, Player player, Game game) {
        context.getPlayer().gainNewCard(Cards.gold, this.getControlCard(), context);
        context.getPlayer().gainNewCard(Cards.copper, this.getControlCard(), context);
    }  
    
    private void warrior(Game game, MoveContext context, Player currentPlayer) {
    	ArrayList<Player> attackedPlayers = new ArrayList<Player>();
    	for (Player player : context.game.getPlayersInTurnOrder()) {
            if (player != currentPlayer && !Util.isDefendedFromAttack(context.game, player, this)) {
            	attackedPlayers.add(player);
            }
    	}
    	int numTravellers = context.countTravellerCardsInPlay();
    	for (int i = 0; i < numTravellers; i++) {
    		for (Player player : attackedPlayers) {
    			player.attacked(this.getControlCard(), context);
                MoveContext playerContext = new MoveContext(game, player);
                playerContext.attackedPlayer = player;
                Card draw = game.draw(playerContext, Cards.warrior, numTravellers - i);
                if (draw != null) {
                	int cost = draw.getCost(context);
                	if (draw.costPotion()) 
                		cost = 0;
                	player.discard(draw, this.getControlCard(), playerContext);
                	int discardIndex = player.discard.size() - 1;
                	if (player.discard.getLastCard() == draw && (cost == 3 || cost == 4)) {
                		player.discard.remove(discardIndex);
                		player.trash(draw, this.getControlCard(), playerContext);
                	}
                }
    		}
    	}
    }
    
    private void putOnTavern(Game game, MoveContext context, Player currentPlayer) {
        // throneroom has here no effect since card is already put on tavern
        // Move to tavern mat
        if (this.getControlCard().numberTimesAlreadyPlayed == 0) {
            currentPlayer.playedCards.remove(currentPlayer.playedCards.lastIndexOf((Card) this.getControlCard()));
            currentPlayer.tavern.add(this.getControlCard());
            this.getControlCard().stopImpersonatingCard();

            GameEvent event = new GameEvent(GameEvent.EventType.CardSetAsideOnTavernMat, (MoveContext) context);
            event.card = this.getControlCard();
            game.broadcastEvent(event);
        } else {
            // reset clone count
            this.getControlCard().cloneCount = 1;
        }
    }
    
    
    //Events
    
    public void alms(MoveContext context) {
    	boolean noTreasureCard = true;
        for(Card card : context.player.playedCards) {
            if (card.is(Type.Treasure, context.getPlayer())) {
            	noTreasureCard = false;
            	break;
            }
        }
        for(Card card : context.player.nextTurnCards) {
            if (card.is(Type.Treasure, context.getPlayer())) {
            	noTreasureCard = false;
            	break;
            }
        }
        if (noTreasureCard) {
	        Card card = context.player.controlPlayer.alms_cardToObtain(context);
	        if (card != null) {
	            if (card.getCost(context) <= 4 && card.getDebtCost(context) == 0 && !card.costPotion()) {
	            	context.player.gainNewCard(card, this.getControlCard(), context);
	            }
	        }
        }
        context.cantBuy.add(this); //once per turn
    }
    
    public void ball(MoveContext context) {
    	Player player = context.getPlayer();
    	player.setMinusOneCoinToken(true, context);
    	for (int i = 0; i < 2; ++i) {
			Card card = player.controlPlayer.ball_cardToObtain(context);
			if (card != null) {
	            // check cost
	            if (card.getCost(context) <= 4 && card.getDebtCost(context) == 0 && !card.costPotion()) {
	            	player.gainNewCard(card, this, context);
	            }
	        }
		}
    }
    
    private void bonfire(MoveContext context) {
        Card[] cards = context.player.controlPlayer.bonfire_cardsToTrash(context);
        if (cards != null) {
            if (cards.length > 2) {
                Util.playerError(context.player, "Bonfire trash error, trying to trash too many cards, ignoring.");
            } else {
            	cardLoop:
                for (Card card : cards) {
                    for (int i = 0; i < context.player.playedCards.size(); i++) {
                        Card playedCard = context.player.playedCards.get(i);
                        if (playedCard.equals(card)) {
                            context.player.trash(context.player.playedCards.remove(i, false), this.getControlCard(), context);
                            continue cardLoop;
                        }
                    }
                    for (int i = 0; i < context.player.nextTurnCards.size(); i++) {
                        Card nextTurnCard = context.player.nextTurnCards.get(i);
                        if (nextTurnCard.equals(card)) {
                        	if (nextTurnCard.is(Type.Duration, context.player)) {
                        		((CardImpl)nextTurnCard).trashAfterPlay = true;
                                context.player.trash(nextTurnCard, this.getControlCard(), context);
                        	} else {
                        		context.player.trash(context.player.nextTurnCards.remove(i, false), this.getControlCard(), context);
                        	}
                        	continue cardLoop;
                        }
                    }
                }
            }
        }
    }
    
    protected void borrow(MoveContext context) {
    	if (!context.player.getMinusOneCardToken()) {
    		context.player.setMinusOneCardToken(true, context);
    		context.addCoins(1);
    	}
        context.cantBuy.add(this); //once per turn
    }
    
    private void ferry(MoveContext context) {
    	Card card = context.getPlayer().controlPlayer.ferry_actionCardPileToHaveToken(context);
    	if (card.is(Type.Action, null))
    		placeToken(context, card, PlayerSupplyToken.MinusTwoCost);
    }
    
    private void inheritance(MoveContext context) {
    	Card card = context.getPlayer().controlPlayer.inheritance_actionCardTosetAside(context);
    	if (card != null && card.is(Type.Action, null)) {
            if (card.getCost(context) <= 4 && card.getDebtCost(context) == 0 && !card.costPotion() && 
            		!context.game.isPileEmpty(card) && !card.is(Type.Victory, null)) {
            	context.player.inheritance = context.game.takeFromPile(card, context);
            	GameEvent event = new GameEvent(GameEvent.EventType.CardSetAsideInheritance, context);
                event.card = card;
                context.game.broadcastEvent(event);
            }

            //Start inheriting estates the player already owns
            for (Card c : context.player.getAllCards()) {
                if (c.equals(Cards.estate)) {
                    ((CardImpl)c).startInheritingCardAbilities(context.player.inheritance.getTemplateCard().instantiate());
                }
            }
        }
    }
    
    private void lostArts(MoveContext context) {
    	Card card = context.getPlayer().controlPlayer.lostArts_actionCardPileToHaveToken(context);
    	if (card.is(Type.Action, null))
    		placeToken(context, card, PlayerSupplyToken.PlusOneAction);
    }
    
    private void mission(MoveContext context) {
    	context.missionBought = true;
    	context.cantBuy.add(this);
    }
    
    private void pathfinding(MoveContext context) {
    	Card card = context.getPlayer().controlPlayer.pathfinding_actionCardPileToHaveToken(context);
    	if (card.is(Type.Action, null))
    		placeToken(context, card, PlayerSupplyToken.PlusOneCard);
    }
    
    private void pilgrimage(MoveContext context) {
    	if(context.player.flipJourneyToken(context)) {
    		Card[] cards = context.player.controlPlayer.pilgrimage_cardsToGain(context);
    		if (cards != null) {
    			if (cards.length > 3) {
    				Util.playerError(context.player, "Pilgrimage gain error, trying to gain too many cards, ignoring.");
    			} else {
    				HashSet<Card> differentCards = new HashSet<Card>();
    				for (Card card : cards) {
    					differentCards.add(card);
    				}
    				for (Card card : differentCards) {
    					if(context.player.playedCards.contains(card) || 
    							context.player.nextTurnCards.contains(card)) {
    						context.player.gainNewCard(card, this.getControlCard(), context);
    					} else {
    						Util.playerError(context.player, "Pilgrimage gain error, card not in play, ignoring.");
    					}
    				}
    			}
    		}
    	}
    	context.cantBuy.add(this); //once per turn
	}
    
    private void plan(MoveContext context) {
    	Card card = context.getPlayer().controlPlayer.plan_actionCardPileToHaveToken(context);
    	if (card.is(Type.Action, null))
    		placeToken(context, card, PlayerSupplyToken.Trashing);
    }
    
    private void quest(MoveContext context) {
    	Player player = context.getPlayer();
    	CardList hand = player.getHand();
    	if (hand.size() == 0)
    		return;
    	QuestOption option = player.controlPlayer.quest_chooseOption(context);
    	if (option == null) {
    		return;
    	}
    	if (option == QuestOption.DiscardAttack) {
    		Set<Card> attackSet = new HashSet<Card>();
    		for (Card card : hand) {
    			if (card.behaveAsCard().is(Type.Attack, player)) {
    				attackSet.add(card);
    			}
    		}
    		if (attackSet.size() == 0) {
    			return;
    		} else if (attackSet.size() == 1) {
    			Card toDiscard = attackSet.toArray(new Card[0])[0];
    			hand.remove(toDiscard);
    			context.getPlayer().discard(toDiscard, this, context);
    		} else {
    			Card[] attacks = attackSet.toArray(new Card[0]);
    			Card toDiscard = player.controlPlayer.quest_attackCardToDiscard(context, attacks);
    			if (toDiscard == null || !attackSet.contains(toDiscard)) {
    				Util.playerError(player, "Quest error, picked attack didn't have. Choosing first attack.");
    				toDiscard = attacks[0];
    			}
    			hand.remove(toDiscard);
    			context.getPlayer().discard(toDiscard, this, context);
    		}
    	} else if (option == QuestOption.DiscardTwoCurses) {
    		int numCurses = 0;
    		for(int n = 0; n < 2; ++n) {
    			for (int i = 0; i < hand.size(); ++i) {
    				if (hand.get(i).equals(Cards.curse)) {
    					numCurses++;
    					player.discard(hand.remove(i), this, context);
    					break;
    				}
    			}
    		}
    		if (numCurses != 2)
    			return;
    	} else if (option == QuestOption.DiscardSixCards) {
    		if (hand.size() <= 6) {
    			int numCards = hand.size();
    			while (!hand.isEmpty()) {
    				player.discard(hand.remove(0), this, context);
    			}
    			if (numCards < 6)
    				return;
    		} else {
    			Card[] toDiscard = player.controlPlayer.quest_cardsToDiscard(context);
    			if (toDiscard.length != 6 || !Util.areCardsInHand(toDiscard, context)) {
    				Util.playerError(player, "Quest error, picked cards to discard player didn't have. Choosing first siz.");
    				for (int i = 0; i < 6; ++i) {
    					player.discard(hand.remove(0), Cards.quest, context);
    				}
    			} else {
    				for (Card card : toDiscard) {
    		            hand.remove(card);
    		            player.discard(card, this, context);
    		        }
    			}
    		}
    	}
    	player.gainNewCard(Cards.gold, this, context);
    }
    
	protected void raid(MoveContext context) {
        for(Card card : context.player.playedCards) {
            if (card.equals(Cards.silver)) {
                context.player.gainNewCard(Cards.silver, this, context);
            }
        }
        for (Player targetPlayer : context.game.getPlayersInTurnOrder()) {
            if (targetPlayer != context.player) {
                MoveContext targetContext = new MoveContext(context.game, targetPlayer);
            	targetPlayer.setMinusOneCardToken(true, targetContext);
            }
        }
    }
    
    private void save(MoveContext context) {
    	context.cantBuy.add(this); //once per turn
    	CardList hand = context.getPlayer().getHand();
    	if (hand.size() == 0)
    		return;
    	
        Card card = (hand.size() == 1) ? hand.get(0) : context.player.controlPlayer.save_cardToSetAside(context);
        if (card == null || !context.player.hand.contains(card)) {
            Util.playerError(context.player, "Save set aside card error, setting aside the first card in hand.");
            card = context.player.hand.get(0);
        }

    	context.player.hand.remove(card);
    	context.player.save = card;
    	GameEvent event = new GameEvent(GameEvent.EventType.CardSetAsideSave, context);
        event.card = card;
        event.setPrivate(true);
        context.game.broadcastEvent(event);
    }
    
    private void scoutingParty(MoveContext context) {
        ArrayList<Card> cards = new ArrayList<Card>();
        for (int i = 0; i < 5; i++) {
            Card card = context.game.draw(context, Cards.scoutingParty, 5 - i);
            if (card != null) {
                cards.add(card);
            }
        }

        if (cards.size() == 0) {
            return;
        }

        for (int i = 0; i < 3; i++) {
        	if(cards.size() > 0) {
	        	Card toDiscard = null;
	        	if(cards.size() > 3-i) {
	        		toDiscard = context.player.scoutingParty_cardToDiscard(context, cards.toArray(new Card[cards.size()]));
		        } else {
		            toDiscard = cards.get(0);
		        }
	        	
		        if (toDiscard == null || !cards.contains(toDiscard)) {
		            Util.playerError(context.player, "ScoutingParty discard error, just picking the first card.");
		            toDiscard = cards.get(0);
		        }
		
		        context.player.discard(toDiscard, this.getControlCard(), context);
		
		        cards.remove(toDiscard);
        	}
        }

        if (cards.size() > 0) {
        	Card[] order = context.player.controlPlayer.survivors_cardOrder(context, cards.toArray(new Card[cards.size()]));

	        // Check that they returned the right cards
	        boolean bad = false;
	
	        if (order == null) {
	            bad = true;
	        } else {
	            ArrayList<Card> copy = new ArrayList<Card>();
	            for (Card card : cards) {
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
	            Util.playerError(context.player, "Scouting Party order cards error, ignoring.");
	            order = cards.toArray(new Card[cards.size()]);
	        }
	
	        // Put the cards back on the deck
	        for (int i = order.length - 1; i >= 0; i--) {
	        	context.player.putOnTopOfDeck(order[i]);
	        }
        }        
    }
    
    private void seaway(MoveContext context) {
    	Card card = context.player.controlPlayer.seaway_cardToObtain(context);
        if (card != null && card.is(Type.Action, null)) {
            if (card.getCost(context) <= 4 && card.getDebtCost(context) == 0 && !card.costPotion() && !context.game.isPileEmpty(card)) {
            	Card gainedCard = context.player.gainNewCard(card, this.getControlCard(), context);
                if (context.game.getPile(card).equals(context.game.getPile(gainedCard))) //check that the placeholdercard is from the same pile as the gained card.
            		placeToken(context, card, PlayerSupplyToken.PlusOneBuy);
            }
        }
    	
    }
    
    private void training(MoveContext context) {
    	Card card = context.getPlayer().controlPlayer.training_actionCardPileToHaveToken(context);
    	if (card.is(Type.Action, null))
    		placeToken(context, card, PlayerSupplyToken.PlusOneCoin);
    }
    
    private void trade(MoveContext context) {
    	Card[] cards = context.player.controlPlayer.trade_cardsToTrash(context);
    	if (cards != null) {
    		if (cards.length > 2) {
    			Util.playerError(context.player, "Trade trash error, trying to trash too many cards, ignoring.");
    		} else {
    			for (Card card : cards) {
    				for (int i = 0; i < context.player.hand.size(); i++) {
    					Card inHand = context.player.hand.get(i);
    					if (inHand.equals(card)) {
    						context.player.trash(context.player.hand.remove(i, false), this.getControlCard(), context);
    						context.player.gainNewCard(Cards.silver, this, context);
    						break;
    					}
    				}
    			}
    		}
    	}
	}
    
}
