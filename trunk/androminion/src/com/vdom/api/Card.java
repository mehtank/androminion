package com.vdom.api;


public interface Card {
    public String getName();

    public String getSafeName();
    
    public String getExpansion();

    public String getStats();

    public String getDescription();

    public int getCost();

    public boolean costPotion();
    
    public boolean isPrize();

    public Integer getId();
}
