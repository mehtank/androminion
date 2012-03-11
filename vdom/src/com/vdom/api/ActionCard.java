package com.vdom.api;

import com.vdom.core.Game;
import com.vdom.core.MoveContext;

public interface ActionCard extends Card {
    public int getAddActions();

    public int getAddBuys();

    public int getAddCards();

    public int getAddGold();
    
    public int getAddVictoryTokens();

    public boolean isAttack();

    public boolean trashForced();

    public void play(Game game, MoveContext context);

}
