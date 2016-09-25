package com.vdom.api;

import java.io.Serializable;

import com.vdom.core.CardImpl;
import com.vdom.core.Expansion;
import com.vdom.core.Game;
import com.vdom.core.Cards.Kind;
import com.vdom.core.MoveContext;
import com.vdom.core.PileCreator;
import com.vdom.core.Player;
import com.vdom.core.Type;


public interface Card extends Serializable {
    public Kind getKind();

    public String getName();

    public String getSafeName();
    
    public Expansion getExpansion();

    public boolean is(Type t, Player player);
    public boolean is(Type t);
    public int getNumberOfTypes(Player player);

    public String getStats();

    public String getDescription();

    public int getCost(MoveContext context);

    public int getCost(MoveContext context, boolean buyPhase);
    
    public boolean costPotion();
    
    public int getDebtCost(MoveContext context);
    
    public int getVictoryPoints();

    public boolean isOverpay(Player player);
    
    public int getAddCards();
    
    public int getAddActions();
    
    public int getAddGold();
    
    public boolean providePotion();
    
    public int getAddBuys();
    
    public int getAddVictoryTokens();
    
    public int getAddCardsNextTurn();
    
    public int getAddActionsNextTurn();
    
    public int getAddGoldNextTurn();
    
    public int getAddBuysNextTurn();
    
    public boolean takeAnotherTurn();
    
    public int takeAnotherTurnCardCount();
    
    /**
     * Does this card force you to trash a card when played? (Used for AI)
     * @return Whether this card forces you to trash a card when played
     */
    public boolean trashForced();
    
	public boolean isCallableWhenCardGained();
	
	public int getCallableWhenGainedMaxCost();
	
	public boolean isCallableWhenActionResolved();
	
	public boolean doesActionStillNeedToBeInPlayToCall();
	
	public boolean isCallableWhenTurnStarts();
	
	public void callWhenCardGained(MoveContext context, Card cardToGain);
    
	public void callWhenActionResolved(MoveContext context, Card resolvedAction);
    
	public void callAtStartOfTurn(MoveContext context);
    
    public void play(Game game, MoveContext context);
    
    public void play(Game game, MoveContext context, boolean fromHand);
    
    public void play(Game game, MoveContext context, boolean fromHand, boolean treasurePlay);
    
    public Integer getId();
    
    public void isBuying(MoveContext context);
    public void isBought(MoveContext context);
    
    public void isTrashed(MoveContext context);
    
    public boolean isImpersonatingAnotherCard();
    public Card behaveAsCard();
    public CardImpl getControlCard();

    public boolean isTemplateCard();
    public CardImpl getTemplateCard();

    public boolean isPlaceholderCard();
    public void setPlaceholderCard();

    public CardImpl instantiate();

    public PileCreator getPileCreator();

    //public void isGained(MoveContext context);
}
