package com.vdom.players;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import com.vdom.api.ActionCard;
import com.vdom.api.Card;
import com.vdom.api.GameType;
import com.vdom.api.TreasureCard;
import com.vdom.api.VictoryCard;
import com.vdom.core.BasePlayer;
import com.vdom.core.Cards;
import com.vdom.core.MoveContext;

public class VDomPlayerDumbo extends BasePlayer {
    protected Random rand = new Random(System.currentTimeMillis());
    protected static final int earlyGame = 5;
    protected static final int MAX_OF_ONE_ACTION_CARD = 4;
    protected int earlyCardBuyCount;

    
    protected boolean alwaysBuyProvince;
    protected boolean buyEstates;
    protected boolean favorSilverGoldPlat;
    protected int silverMax;
    protected Card[] valuedCards;
    protected int actionCardMax;
    protected Card[] earlyCardBuys;
    protected int earlyCardBuyMax;
    protected boolean onlyBuyEarlySingle;
    protected int throneRoomsAndKingsCourtsMax = 2;
    protected boolean improvise = false;
    protected Card[] trashCards;
    
    @Override
    public boolean isAi() {
        return true;
    }

    @Override
    public String getPlayerName() {
        return "Dumbo";
    }

    @Override
    public Card doAction(MoveContext context) {
        ArrayList<ActionCard> actionCards = context.getPlayer().getActionsInHand();
        // return card to play

        return null;
    }
    
 

    @Override
    public Card doBuy(MoveContext context) {
        final int coinAvailableForBuy = context.getCoinAvailableForBuy();

        
        if (context.canBuy(Cards.colony)) {
            return Cards.colony;
        }
        
        if (context.canBuy(Cards.platinum)) {
        	return Cards.platinum;
        }
        
        if (context.canBuy(Cards.province)) {
        	return Cards.province;
        }
        
        if (context.canBuy(Cards.gold)) {
        	return Cards.gold;
        }
        
        if (context.canBuy(Cards.silver)) {
        	return Cards.silver;
        }
        
        if (context.canBuy(Cards.copper)) {
        	return Cards.copper;
        }
        
        return null;
    }
    
    @Override
    public ArrayList<TreasureCard> treasureCardsToPlayInOrder(MoveContext context) {
        
        return super.treasureCardsToPlayInOrder(context);
    }
    

    @Override
    public void newGame(MoveContext context) {
        super.newGame(context);
    }        
        
    @Override
    public Card[] getTrashCards() {
        return trashCards;
    }
    
 	@Override
	public Card getAttackReaction(MoveContext context, Card responsible, boolean defended) {
        Card[] reactionCards = getReactionCards(defended);
    	for (Card c : reactionCards) {
    		if (c.equals(Cards.moat) && !reactedMoat) {
    			reactedMoat = true;
    			return c;
    		}
    		if (c.equals(Cards.secretChamber) && !reactedSecretChamber) {
    			reactedSecretChamber = true;
    			return c;
    		}
    		if (c.equals(Cards.horseTraders))
    			return c;
    	}
    	return null;
	}
    
}