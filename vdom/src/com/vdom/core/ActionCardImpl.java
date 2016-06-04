package com.vdom.core;

import java.util.ArrayList;
import java.util.Collections;

import com.vdom.api.ActionCard;
import com.vdom.api.Card;
import com.vdom.api.GameEvent;

public class ActionCardImpl extends CardImpl implements ActionCard {
    private static final long serialVersionUID = 1L;
    // template (immutable)
    protected int addActions;
    protected int addBuys;
    protected int addCards;
    protected int addGold;
    protected int addVictoryTokens;
    boolean trashForced = false;

    public ActionCardImpl(com.vdom.core.CardImpl.Builder builder) {
        super(builder);
        addActions       = builder.addActions;
        addBuys          = builder.addBuys;
        addCards         = builder.addCards;
        addGold          = builder.addGold;
        addVictoryTokens = builder.addVictoryTokens;
        trashForced      = builder.trashForced;
    }

    public int getAddActions() {
        return addActions;
    }

    public int getAddBuys() {
        return addBuys;
    }

    public int getAddCards() {
        return addCards;
    }

    public int getAddGold() {
        return addGold;
    }

    public int getAddVictoryTokens() {
        return addVictoryTokens;
    }

    public boolean trashForced() {
        return trashForced;
    }

    @Override
    public CardImpl instantiate() {
        checkInstantiateOK();
        ActionCardImpl c = new ActionCardImpl();
        copyValues(c);
        return c;
    }

    protected void copyValues(ActionCardImpl c) {
        super.copyValues(c);
        c.addActions = addActions;
        c.addBuys = addBuys;
        c.addCards = addCards;
        c.addGold = addGold;
        c.addVictoryTokens = addVictoryTokens;
        c.trashForced = trashForced;
    }

    protected ActionCardImpl() {
    }

    public void play(Game game, MoveContext context) {
        play(game, context, true);
    }

    public void play(Game game, MoveContext context, boolean fromHand) {
        super.play(game, context);

        Player currentPlayer = context.getPlayer();
        boolean newCard = false;
        Card actualCard = (this.getControlCard() != null ? this.getControlCard() : this);
        boolean isInheritedAbility = actualCard.equals(Cards.estate);
        boolean enchantressEffect = !context.enchantressAlreadyAffected && game.enchantressAttacks(currentPlayer);
        if (enchantressEffect) context.enchantressAlreadyAffected = true;
                
        context.actions += game.countChampionsInPlay(currentPlayer);
        
        if (isAttack(currentPlayer))
            attackPlayed(context, game, currentPlayer);
        
        if (this.numberTimesAlreadyPlayed == 0 && this == actualCard) {
            newCard = true;
            this.movedToNextTurnPile = false;
            if (fromHand)
                currentPlayer.hand.remove(this);
            if (!enchantressEffect && trashOnUse) {
                currentPlayer.trash(this, null, context);
            } else if (!enchantressEffect && this.isDuration(currentPlayer)) {
                currentPlayer.nextTurnCards.add(this);
            } else {
                currentPlayer.playedCards.add(this);
            }
        }
        
        GameEvent event;
        if (!isInheritedAbility) {
	        event = new GameEvent(GameEvent.EventType.PlayingAction, (MoveContext) context);
	        event.card = this;
	        event.newCard = newCard;
	        game.broadcastEvent(event);
        }
      
        // playing an action
        if (this == actualCard) 
            context.actionsPlayedSoFar++;
        if (context.freeActionInEffect == 0) {
            context.actions--;
        }
        
        Card tokenPile = isInheritedAbility ? actualCard : this;
        if (game.isPlayerSupplyTokenOnPile(tokenPile, currentPlayer, PlayerSupplyToken.PlusOneAction))
        	context.actions += 1;
        if (game.isPlayerSupplyTokenOnPile(tokenPile, currentPlayer, PlayerSupplyToken.PlusOneBuy))
        	context.buys += 1;
        if (game.isPlayerSupplyTokenOnPile(tokenPile, currentPlayer, PlayerSupplyToken.PlusOneCoin))
        	context.addCoins(1);
        if (game.isPlayerSupplyTokenOnPile(tokenPile, currentPlayer, PlayerSupplyToken.PlusOneCard))
        	game.drawToHand(context, actualCard, 1 + addCards);
        
        if (enchantressEffect) {
        	//allow reaction to playing an attack card with Enchantress effect
        	if (isAttack(currentPlayer)) {
        		 for (Player player : game.getPlayersInTurnOrder()) {
    	            if (player != currentPlayer) Util.isDefendedFromAttack(game, player, this);
	            }
        	}
        	context.actions += 1;
        	game.drawToHand(context, this, 1);
        } else {
        	context.actions += addActions;
            context.buys += addBuys;
            context.addCoins(addGold);
            currentPlayer.addVictoryTokens(context, addVictoryTokens);

            for (int i = 0; i < addCards; ++i) {
            	game.drawToHand(context, this, addCards - i);
            }

            additionalCardActions(game, context, currentPlayer);
        }
        
        if (!isInheritedAbility) {
	        event = new GameEvent(GameEvent.EventType.PlayedAction, (MoveContext) context);
	        event.card = this;
	        game.broadcastEvent(event);
        } else {
        	return;
        }

        // test if any prince card left the play
        currentPlayer.princeCardLeftThePlay(currentPlayer);
        
     // check for cards to call after resolving action
        boolean isActionInPlay = isInPlay(currentPlayer);
        ArrayList<Card> callableCards = new ArrayList<Card>();
        Card toCall = null;
        for (Card c : currentPlayer.tavern) {
        	if (c.behaveAsCard().isCallableWhenActionResolved()) {
        		if (c.behaveAsCard().doesActionStillNeedToBeInPlayToCall() && !isActionInPlay) {
        			continue;
        		}
        		callableCards.add(c);
        	}
        }
        if (!callableCards.isEmpty()) {
        	Collections.sort(callableCards, new Util.CardCostComparator());
	        do {
	        	toCall = null;
	        	// we want null entry at the end for None
	        	Card[] cardsAsArray = callableCards.toArray(new Card[callableCards.size() + 1]);
	        	//ask player which card to call
	        	toCall = currentPlayer.controlPlayer.call_whenActionResolveCardToCall(context, this, cardsAsArray);
	        	if (toCall != null && callableCards.contains(toCall)) {
	        		callableCards.remove(toCall);
	        		toCall.behaveAsCard().callWhenActionResolved(context, this);
	        	}
		        // loop while we still have cards to call
		        // NOTE: we have a hack here to prevent asking for duplicate calls on an unused Royal Carriage
		        //   since technically you can ask for more and action re-played by royal carriage will ask as well
	        } while (toCall != null && toCall.equals(Cards.coinOfTheRealm) && !callableCards.isEmpty());
        }
    }

    protected void witchFamiliar(Game game, MoveContext context, Player currentPlayer) {
        for (Player player : game.getPlayersInTurnOrder()) {
            if (player != currentPlayer && !isDefendedFromAttack(game, player, this)) {
                player.attacked(this.controlCard, context);
                MoveContext targetContext = new MoveContext(game, player);
                targetContext.attackedPlayer = player;
                player.gainNewCard(Cards.curse, this.controlCard, targetContext);
            }
        }
    }

    protected Card throneRoomKingsCourt(Game game, MoveContext context, Player currentPlayer) {
        ArrayList<Card> actionCards = new ArrayList<Card>();
        CardImpl cardToPlay = null;
        for (Card card : currentPlayer.hand) {
            if (card.isAction(currentPlayer)) {
                actionCards.add(card);
            }
        }

        if (!actionCards.isEmpty()) {
            switch (this.kind) {
                case ThroneRoom:
                    cardToPlay = (CardImpl) currentPlayer.controlPlayer.throneRoom_cardToPlay(context);
                    break;
                case Disciple:
                    cardToPlay = (CardImpl) currentPlayer.controlPlayer.disciple_cardToPlay(context);
                    break;
                case KingsCourt:
                    cardToPlay = (CardImpl) currentPlayer.controlPlayer.kingsCourt_cardToPlay(context);
                    break;
                case Procession:
                    cardToPlay = (CardImpl) currentPlayer.controlPlayer.procession_cardToPlay(context);
                    break;
                default:
                    break;
            }

            if (cardToPlay != null) {
                if(!actionCards.contains(cardToPlay)) {
                    Util.playerError(currentPlayer, this.controlCard.name.toString() + " card selection error, ignoring");
                } else {
                    context.freeActionInEffect++;

                    cardToPlay.cloneCount = (equals(Cards.kingsCourt) ? 3 : 2);
                    int cloneCount = cardToPlay.cloneCount;
                    cardToPlay.numberTimesAlreadyPlayed = -1;
                    for (int i = 0; i < cloneCount; ++i) {
                        cardToPlay.numberTimesAlreadyPlayed++;
                        cardToPlay.play(game, context, cardToPlay.numberTimesAlreadyPlayed == 0 ? true : false);
                    }

                    cardToPlay.numberTimesAlreadyPlayed = 0;
                    context.freeActionInEffect--;
                    // If the cardToPlay was a knight, and was trashed, reset clonecount
                    if (cardToPlay.isKnight(currentPlayer) && !currentPlayer.playedCards.contains(cardToPlay) && game.trashPile.contains(cardToPlay)) {
                        cardToPlay.cloneCount = 1;
                    }

                    if (cardToPlay.isDuration(currentPlayer) && !cardToPlay.equals(Cards.tactician)) {
                        // Need to move throning card to NextTurnCards first
                        // (but does not play)
                        if (!this.controlCard.movedToNextTurnPile) {
                            this.controlCard.movedToNextTurnPile = true;
                            int idx = currentPlayer.playedCards.lastIndexOf(this.controlCard);
                            int ntidx = currentPlayer.nextTurnCards.size() - 1;
                            if (idx >= 0 && ntidx >= 0) {
                                currentPlayer.playedCards.remove(idx);
                                currentPlayer.nextTurnCards.add(ntidx, this.controlCard);
                            }
                        }
                    }
                }

                if (this.kind == Cards.Kind.Procession) {
                    if (!cardToPlay.trashOnUse) {
                        currentPlayer.trash(cardToPlay, this.controlCard, context);
                        if (currentPlayer.playedCards.getLastCard() == cardToPlay) { 
                            currentPlayer.playedCards.remove(cardToPlay);
                        } 
                        if (currentPlayer.nextTurnCards.contains(cardToPlay)) { 
                            ((CardImpl) cardToPlay).trashAfterPlay = true;
                        }
                    }

                    Card cardToGain = currentPlayer.controlPlayer.procession_cardToGain(context, 1 + cardToPlay.getCost(context), cardToPlay.costPotion());
                    if ((cardToGain != null) && (cardToPlay.getCost(context) + 1) == cardToGain.getCost(context)) {
                        currentPlayer.gainNewCard(cardToGain, this.controlCard, context);
                    }
                }
            }
        }
        return cardToPlay;
    }

    protected void horseTradersDungeon(MoveContext context, Player currentPlayer) {
    	CardList hand = currentPlayer.getHand();
    	if (hand.size() == 0)
    		return;
    	Card[] cardsToDiscard;
    	if (hand.size() <= 2) {
    		cardsToDiscard = new Card[currentPlayer.getHand().size()];
    		for (int i = 0; i < cardsToDiscard.length; ++i) {
    			cardsToDiscard[i] = hand.get(i);
    		}
    	} else {
    		cardsToDiscard = currentPlayer.controlPlayer.horseTradersDungeon_cardsToDiscard(context, this);
            if (cardsToDiscard == null || cardsToDiscard.length != 2 || !Util.areCardsInHand(cardsToDiscard, context)) {
                if (currentPlayer.hand.size() >= 2) {
                    Util.playerError(currentPlayer, "Horse Traders discard error, just discarding the first 2.");
                }
                cardsToDiscard = new Card[Math.min(2, currentPlayer.hand.size())];
                for (int i = 0; i < cardsToDiscard.length; i++) {
                    cardsToDiscard[i] = currentPlayer.hand.get(i);
                }
            }
    	}
        
        for (Card card : cardsToDiscard) {
            currentPlayer.hand.remove(card);
            currentPlayer.discard(card, this.controlCard, context);
        }
    }

    protected void spyAndScryingPool(Game game, MoveContext context, Player currentPlayer) {
        for (Player player : game.getPlayersInTurnOrder()) {
            // Note that this.controlCard is the opposite check of other attacks, the spy/scrying pool lets
            // the current player look at their own deck which is a good thing, so always
            // allow that
            if (player == currentPlayer || (!Util.isDefendedFromAttack(game, player, this))) {
                if (player != currentPlayer) {
                    player.attacked(this.controlCard, context);
                }

                MoveContext playerContext = new MoveContext(game, player);
                playerContext.attackedPlayer = player;
                Card card = game.draw(playerContext, this, 1);

                if (card != null) {
                    player.reveal(card, this.controlCard, playerContext);

                    boolean discard = false;

                    if(equals(Cards.spy)) {
                        discard = currentPlayer.controlPlayer.spy_shouldDiscard(context, player, card);
                    } else if (equals(Cards.scryingPool)) {
                        discard = currentPlayer.controlPlayer.scryingPool_shouldDiscard(context, player, card);
                    }

                    if (discard) {
                        player.discard(card, this.controlCard, playerContext);
                    } else {
                        // put it back
                        player.putOnTopOfDeck(card, playerContext, true);
                    }
                }
            }
        }

        if(equals(Cards.scryingPool)) {
            ArrayList<Card> cardsToPutInHand = new ArrayList<Card>();

            Card draw = null;
            while ((draw = game.draw(context, Cards.scryingPool, -1)) != null) {
                currentPlayer.reveal(draw, this.controlCard, new MoveContext(context, game, currentPlayer));
                cardsToPutInHand.add(draw);
                if(!(draw.isAction(currentPlayer))) {
                    break;
                }
            }

            for(Card card : cardsToPutInHand) {
                currentPlayer.hand.add(card);
            }
        }
    }

    Player getNextPlayer(Player player) {
        int next = -1;
        for (int i = 0; i < Game.players.length; i++) {
            if (player == Game.players[i]) {
                next = i + 1;
                if (next >= Game.players.length) {
                    next = 0;
                }
                break;
            }
        }

        if (next == -1) {
            Util.log("ERROR:getNextPlayer() could not find current player:" + player.getPlayerName());
            return null;
        }

        return Game.players[next];
    }

    protected boolean isDefendedFromAttack(Game game, Player player, Card responsible) {
        return Util.isDefendedFromAttack(game, player, responsible);
    }

    // TODO better way to do, possible security hole
    protected CardList hand(Player player) {
        return player.hand;
    }

    protected void drawToHand(MoveContext context, Card responsible, int count) {
        for (int i=0; i < count; i++) {
            context.game.drawToHand(context, responsible, count - i);
        }
    }

    protected void addToHand(Player player, Card card) {
        player.hand.add(card);
    }

    protected Card draw(MoveContext context, int cardsLeftToDraw) {
        return context.game.draw(context, this, cardsLeftToDraw);
    }

    protected Player[] getAllPlayers() {
        return Game.players;
    }

    protected Card removeFromHand(Player player, int i) {
        return player.hand.remove(i);
    }
    
    protected void durationAttack(Game game, MoveContext context, Player currentPlayer) {
        for (Player targetPlayer : context.game.getPlayersInTurnOrder()) {
        	if (targetPlayer != currentPlayer) {
        		if (!Util.isDefendedFromAttack(game, targetPlayer, this)) {
        			targetPlayer.attacked(this.controlCard, context);
        			currentPlayer.addDurationEffectOnOtherPlayer(targetPlayer, this.kind);
        		}
        	}
        }
    }
    
    
}
