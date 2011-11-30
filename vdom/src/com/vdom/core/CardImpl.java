package com.vdom.core;

import com.vdom.api.Card;

public class CardImpl implements Card {
    static int maxNameLen;
    public boolean templateCard = true;
    String safeName;

    CardImpl(String name, int cost) {
        this.name = name;
        this.cost = cost;
        if (maxNameLen < name.length()) {
            maxNameLen = name.length();
        }
    }
    
    public String getSafeName() {
        if(safeName == null) {
            StringBuilder sb = new StringBuilder();
            for(char c : getName().toCharArray()) {
                if(Character.isLetterOrDigit(c)) {
                    sb.append(c);
                }
            }
            safeName = sb.toString();
        }
        
        return safeName;
    }

    protected void checkInstantiateOK() {
        if (!templateCard) {
            Thread.dumpStack();
            System.out.println("Trying to create a real card from a real card instead of a template");
        }
    }

    public CardImpl instantiate() {
        checkInstantiateOK();
        CardImpl c = new CardImpl();
        copyValues(c);
        return c;
    }

    protected void copyValues(CardImpl c) {
        c.templateCard = false;
        c.id = Game.cardSequence++;

        c.name = name;
        c.cost = cost;
        c.description = description;
        c.expansion = expansion;
        c.dontAutoRecycleOnUse = dontAutoRecycleOnUse;
        c.costPotion = costPotion;
        c.isPrize = isPrize;
    }

    public String getName() {
        return name;
    }

    public int getCost() {
        return Math.max(0, cost - Game.bridgesInEffect);
    }

    String name;
    int cost;
    String expansion;
    boolean costPotion;
    boolean isPrize;
    String description = "";
    boolean dontAutoRecycleOnUse = false;
    private Integer id;

    protected CardImpl() {
    }

    /**
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    void play(Game game, MoveContext context) {
    }

    public String getStats() {
        // return ("" + cost + " Cost");
        return ("(" + cost + (costPotion ? "p)" : ") "));
    }
    
    @Override
    public String getExpansion() {
        return expansion;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return name + " (id=" + id + ")";
    }

    @Override
    public boolean equals(Object object) {
        return (object != null && (object instanceof Card) && name.equals(((Card) object).getName()));
    }
    
    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public boolean costPotion() {
        return costPotion;
    }
    
    public boolean isPrize() {
        return isPrize;
    }
}
