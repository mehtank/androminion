package com.vdom.core;

import com.vdom.api.ActionCard;
import com.vdom.api.Card;
import com.vdom.api.GameEvent;

public class CallableTreasureCardImpl extends TreasureCardImpl implements CallableCard {

	private static final long serialVersionUID = 1L;
	protected boolean callableWhenCardGained;
    protected boolean callableWhenActionResolved;
    protected boolean actionStillNeedsToBeInPlay;
    protected boolean callableWhenTurnStarts;
    protected int callableWhenCardGainedMaxCost = -1;
	
	protected CallableTreasureCardImpl(Builder builder) {
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

	public static class Builder extends TreasureCardImpl.Builder {

        protected boolean callableWhenCardGained;
        protected boolean callableWhenActionResolved;
        protected boolean actionStillNeedsToBeInPlay;
        protected boolean callableWhenTurnStarts;
        protected int callableWhenCardGainedMaxCost;
        
        public Builder(Cards.Type type, int cost, int value) {
            super(type, cost, value);
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

        public CallableTreasureCardImpl build() {
            return new CallableTreasureCardImpl(this);
        }
    }
	
	@Override
    public CardImpl instantiate() {
        checkInstantiateOK();
        CallableTreasureCardImpl c = new CallableTreasureCardImpl();
        copyValues(c);
        return c;
    }

    protected void copyValues(CallableTreasureCardImpl c) {
        super.copyValues(c);
        c.callableWhenCardGained = callableWhenCardGained;
        c.callableWhenActionResolved = callableWhenActionResolved;
        c.callableWhenTurnStarts = callableWhenTurnStarts;
        c.callableWhenCardGainedMaxCost = callableWhenCardGainedMaxCost;
        c.actionStillNeedsToBeInPlay = actionStillNeedsToBeInPlay;
    }

    protected CallableTreasureCardImpl() {
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
    	switch (this.getType()) {
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
    	case CoinOfTheRealm:
    		coinOfTheRealm(context, game, currentPlayer);
    		break;
    	default:
    		break;
    	}
    	finishCall(context);
    }
    
	public void callAtStartOfTurn(MoveContext context) {
    	if (!callableWhenTurnStarts) return;
    	if (!call(context)) return;
    	switch (this.getType()) {
    	default:
    		break;
    	}
    	finishCall(context);
    }
	
	private void coinOfTheRealm(MoveContext context, Game game,
			Player currentPlayer) {
		context.actions += 2;
	}
}
