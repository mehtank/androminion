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

public class CardImplRenaissance extends CardImpl {

	private static final long serialVersionUID = 1L;

	public CardImplRenaissance(CardImpl.Builder builder) {
		super(builder);
	}

	protected CardImplRenaissance() { }
	
	protected void additionalCardActions(Game game, MoveContext context, Player currentPlayer, boolean isThronedEffect) {
		switch(getKind()) {
		
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
        
        default:
        	break;
    	}
    	
    	// card left play - stop any impersonations
	    this.getControlCard().stopImpersonatingCard();
	    this.getControlCard().stopInheritingCardAbilities();
	}
	
}
