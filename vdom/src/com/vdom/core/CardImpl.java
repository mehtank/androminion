package com.vdom.core;

import com.vdom.api.Card;

public class CardImpl implements Card {
    String name;
    int cost;
    boolean trashed = false;
    boolean costPotion = false;

    String description = "";
    String expansion = "";
    protected int vp;
    protected int cloneCount = 1;

    boolean isPrize = false;

    private Integer id;

    static int maxNameLen;
    public boolean templateCard = true;
    String safeName;

    protected CardImpl(String name, int cost) {
        this.name = name;
        if (maxNameLen < name.length()) {
            maxNameLen = name.length();
        }
        this.cost = cost;
    }
    
    public CardImpl(Builder builder) {
        this(builder.name, builder.cost);
		    vp = builder.vp;
        description = builder.description;
        expansion = builder.expansion;
        isPrize = builder.isPrize;
        costPotion = builder.costPotion;
    }

    public static class Builder {
        protected String name;
        protected int cost;

        protected boolean costPotion = false;
        protected String description = "";
        protected String expansion = "";

	    protected boolean isPrize = false;

        protected int vp = 0;

        public Builder(String name, int cost) {
            this.name = name;
            this.cost = cost;
        }

        public Builder description(String val) {
            description = val;
            return this;
        }

        public Builder expansion(String val) {
            expansion = val;
            return this;
        }

        public Builder costPotion() {
            costPotion = true;
            return this;
        }

        public Builder vp(int val) {
            vp = val;
            return this;
        }

        public Builder isPrize() {
            isPrize = true;
            return this;
        }

        public CardImpl build() {
            return new CardImpl(this);
        }

    }
    protected CardImpl() {
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
        c.costPotion = costPotion;
        c.description = description;
        c.expansion = expansion;
        c.isPrize = isPrize;
        c.vp = vp;
    }

    public String getName() {
        return name;
    }

    public int getCost(MoveContext context) {
        if(context == null)
            return cost;
        return Math.max(0, cost + context.cardCostModifier);
    }

    /**
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    public void play(Game game, MoveContext context) {
    }

    public String getStats() {
        StringBuilder sb = new StringBuilder();
		    sb.append ("(" + cost + (costPotion ? "p)" : ") "));
        if (vp > 0) {
            sb.append(", " + vp + " victory points");
            if (vp > 1) {
                sb.append("s");
            }
        }
        return sb.toString();
    }

    public String getExpansion() {
        return expansion;
    }

    public String getDescription() {
        return description;
    }

    public String toString() {
        return name + " (id=" + id + ")";
    }

    public boolean equals(Object object) {
        return (object != null && (object instanceof Card) && name.equals(((Card) object).getName()));
    }
    
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
