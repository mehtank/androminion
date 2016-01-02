package com.vdom.core;

import com.vdom.api.Card;

public interface CallableCard extends Card {
	public boolean isCallableWhenCardGained();
	
	public int getCallableWhenGainedMaxCost();
	
	public boolean isCallableWhenActionResolved();
	
	public boolean isCallableWhenTurnStarts();
	
	public void callWhenCardGained(MoveContext context, Card cardToGain);
    
	public void callWhenActionResolved(MoveContext context, Card resolvedAction);
    
	public void callAtStartOfTurn(MoveContext context);
}
