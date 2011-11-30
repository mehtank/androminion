package com.vdom.api;

public interface ActionCard extends Card {
    public int getAddActions();

    public int getAddBuys();

    public int getAddCards();

    public int getAddGold();
    
    public int getAddVictoryTokens();

    public boolean isAttack();
}
