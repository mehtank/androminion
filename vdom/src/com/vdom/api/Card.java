package com.vdom.api;

import java.io.Serializable;

import com.vdom.core.CardImpl;
import com.vdom.core.Game;
import com.vdom.core.Cards.Type;
import com.vdom.core.MoveContext;


public interface Card extends Serializable {
    public Type getType();

    public String getName();

    public String getSafeName();
    
    public String getExpansion();

    public String getStats();

    public String getDescription();

    public int getCost(MoveContext context);

    public int getCost(MoveContext context, boolean buyPhase);

    public boolean isVictory(MoveContext context);
    
    public boolean costPotion();
    
    public boolean isAction();
    
    public boolean isAttack();
    
    public boolean isPrize();
    
    public boolean isShelter();
    
    public boolean isLooter();
    
    public boolean isRuins();
    
    public boolean isKnight();
    
    public boolean isOverpay();
    
    public boolean isEvent();
    
    public boolean isReserve();
    
    public boolean isTraveller();
    
    public int getAddCards();
    
    public int getAddActions();
    
    public int getAddGold();
    
    public int getAddBuys();
    
    public int getAddVictoryTokens();
    
    /**
     * Does this card force you to trash a card when played? (Used for AI)
     * @return Whether this card forces you to trash a card when played
     */
    public boolean trashForced();
    
    public void play(Game game, MoveContext context);
    
    public void play(Game game, MoveContext context, boolean fromHand);
    
    public Integer getId();
    
    public void isBuying(MoveContext context);
    public void isBought(MoveContext context);
    
    public void isTrashed(MoveContext context);
    
    public boolean isImpersonatingAnotherCard();
    public Card behaveAsCard();
    public CardImpl getControlCard();

    public boolean isTemplateCard();
    public CardImpl getTemplateCard();

    public CardImpl instantiate();
    
    //public void isGained(MoveContext context);
}
