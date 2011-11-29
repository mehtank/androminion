package com.vdom.api;

import com.vdom.core.MoveContext;

public interface TreasureCard extends Card {
    public boolean providesPotion = false;

    public int getValue();

    public boolean providesPotion();
    
    public void playTreasure(MoveContext context);
}
