package com.vdom.api;

import java.io.Serializable;

import com.vdom.core.CardImpl;
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
    
    public boolean isPrize();
    
    public boolean isShelter();
    
    public boolean isLooter();
    
    public boolean isRuins();
    
    public boolean isKnight();
    
    public boolean isOverpay();
    
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
