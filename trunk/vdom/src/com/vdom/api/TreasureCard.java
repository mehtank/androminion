package com.vdom.api;

import com.vdom.core.MoveContext;

public interface TreasureCard extends Card {
    public boolean providePotion = false;

    public int getValue();

    public boolean providePotion();
    
    public boolean playTreasure(MoveContext context);
}
