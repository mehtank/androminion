package com.vdom.core;

import com.vdom.api.ActionCard;
import com.vdom.api.Card;
import com.vdom.api.GameEvent;

public class CallableActionCardImpl extends ActionCardImpl implements CallableCard {

	private static final long serialVersionUID = 1L;
	protected boolean callableWhenCardGained;
    protected boolean callableWhenActionResolved;
    protected boolean actionStillNeedsToBeInPlay;
    protected boolean callableWhenTurnStarts;
    protected int callableWhenCardGainedMaxCost = -1;
	
	protected CallableActionCardImpl(Builder builder) {
        super(builder);
        callableWhenCardGained = builder.callableWhenCardGained;
        callableWhenActionResolved = builder.callableWhenActionResolved;
        actionStillNeedsToBeInPlay = builder.actionStillNeedsToBeInPlay;
        callableWhenTurnStarts = builder.callableWhenTurnStarts;
        callableWhenCardGainedMaxCost = builder.callableWhenCardGainedMaxCost;
    }
	
	public boolean isCallableWhenCardGained() {
		return callableWhenCardGained;
	}
	
	public int getCallableWhenGainedMaxCost() {
		return callableWhenCardGainedMaxCost;
	}
	
	public boolean isCallableWhenActionResolved() {
		return callableWhenActionResolved;
	}
	
	public boolean doesActionStillNeedToBeInPlayToCall() {
		return actionStillNeedsToBeInPlay;
	}
	
	public boolean isCallableWhenTurnStarts() {
		return callableWhenTurnStarts;
	}
	
	public static class Builder extends ActionCardImpl.Builder {
        protected boolean callableWhenCardGained;
        protected boolean callableWhenActionResolved;
        protected boolean actionStillNeedsToBeInPlay;
        protected boolean callableWhenTurnStarts;
        protected int callableWhenCardGainedMaxCost;
        
        public Builder(Cards.Type type, int cost) {
            super(type, cost);
        }

        public Builder callWhenTurnStarts() {
        	callableWhenTurnStarts = true;
            return this;
        }

        public Builder callWhenActionResolved() {
        	callableWhenActionResolved = true;
            return this;
        }
        
        public Builder callWhenActionResolved(boolean mustBeInPlay) {
        	callableWhenActionResolved = true;
        	actionStillNeedsToBeInPlay = mustBeInPlay;
            return this;
        }

        public Builder callWhenGainCard(int maxCost) {
            callableWhenCardGained = true;
            callableWhenCardGainedMaxCost = maxCost;
            return this;
        }

        public CallableActionCardImpl build() {
            return new CallableActionCardImpl(this);
        }
    }

	@Override
    public CardImpl instantiate() {
        checkInstantiateOK();
        CallableActionCardImpl c = new CallableActionCardImpl();
        copyValues(c);
        return c;
    }

    protected void copyValues(CallableActionCardImpl c) {
        super.copyValues(c);
        c.callableWhenCardGained = callableWhenCardGained;
        c.callableWhenActionResolved = callableWhenActionResolved;
        c.callableWhenTurnStarts = callableWhenTurnStarts;
        c.callableWhenCardGainedMaxCost = callableWhenCardGainedMaxCost;
        c.actionStillNeedsToBeInPlay = actionStillNeedsToBeInPlay;
    }

    protected CallableActionCardImpl() {
    }
    
    private boolean call(MoveContext context) {
    	Player currentPlayer = context.getPlayer();
		if (!currentPlayer.tavern.remove(this.controlCard)) {
			return false;
		}
        currentPlayer.playedCards.add(this.controlCard);
       	GameEvent event = new GameEvent(GameEvent.Type.CallingCard, (MoveContext) context);
        event.card = this.controlCard;
        event.newCard = true;
        context.game.broadcastEvent(event);
        return true;
	}
    
    private void finishCall(MoveContext context) {
    	GameEvent event = new GameEvent(GameEvent.Type.CalledCard, (MoveContext) context);
        event.card = this.controlCard;
        context.game.broadcastEvent(event);
	}
    
    public void callWhenCardGained(MoveContext context, Card cardToGain) {
    	if (!callableWhenCardGained) return;
    	if (!call(context)) return;
    	Game game = context.game;
    	Player currentPlayer = context.getPlayer();
    	switch (this.getType()) {
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
    	switch (this.getType()) {
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
    	switch (this.getType()) {
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
	
	private void duplicate(Card cardToGain, MoveContext context, Game game, Player currentPlayer) {
		currentPlayer.gainNewCard(cardToGain, this.controlCard, context);
	}
	
	private void guide(MoveContext context, Game game, Player currentPlayer) {
		if (currentPlayer.getHand().size() > 0) {
            while (!currentPlayer.getHand().isEmpty()) {
                currentPlayer.discard(currentPlayer.getHand().remove(0), this.controlCard, context);
            }
        }
		for (int i = 0; i < 5; ++i) {
			game.drawToHand(context, this.controlCard, 5 - i);
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
        		currentPlayer.trash(cardToTrash, this.controlCard, context);
        	}
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
        if (cardToPlay.isKnight(currentPlayer) && !currentPlayer.playedCards.contains(cardToPlay) && game.trashPile.contains(cardToPlay)) {
            cardToPlay.getControlCard().cloneCount = 1;
        }

        if (cardToPlay.isDuration(currentPlayer) && !cardToPlay.equals(Cards.tactician)) {
            // Need to move royal carriage card to NextTurnCards first
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

	private void teacher(MoveContext context, Game game, Player currentPlayer) {
		//look to see if we have a free pile
		int numFreePiles = 0;
		Card lastFreePile = null;
		for (Card c : game.getCardsInGame()) {
			if (game.getPile(c).isSupply() 
					&& c.isAction(null)
					&& game.getPlayerSupplyTokens(c, currentPlayer).size() == 0) {
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
        boolean potion = cardToTrash.costPotion();
        
        Card card = currentPlayer.controlPlayer.transmogrify_cardToObtain(context, value, potion);
        if (card != null) {
            if (card.getCost(context) > value || (card.costPotion() && !potion)) {
                Util.playerError(currentPlayer, "Transmogrify error, new card does not cost value of the old card +1.");
            } else if (game.isPileEmpty(card)) {
            	Util.playerError(currentPlayer, "Transmogrify error, new card pile is empty.");
            } else if (!game.isCardInGame(card)) {
            	Util.playerError(currentPlayer, "Transmogrify error, new card not in game.");
            } else {
            	currentPlayer.gainNewCard(card, this.controlCard, context);
            }
        }
	}
}
