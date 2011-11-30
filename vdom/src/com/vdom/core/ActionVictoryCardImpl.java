package com.vdom.core;

import com.vdom.api.VictoryCard;

public class ActionVictoryCardImpl extends ActionCardImpl implements VictoryCard {
    ActionVictoryCardImpl(Builder builder) {
        super(new ActionCardImpl.Builder(builder.name, builder.cost));
        addActions = builder.addActions;
        addBuys = builder.addBuys;
        addCards = builder.addCards;
        addGold = builder.addGold;
        attack = builder.attack;
        dontAutoRecycleOnUse = builder.dontAutoRecycleOnUse;
        vp = builder.vp;
    }

    public static class Builder {
        private String name;
        private int cost;

        private int addActions;
        private int addBuys;
        private int addCards;
        private int addGold;
        private boolean dontAutoRecycleOnUse;
        private boolean attack;
        private int vp;

        public Builder(String name, int cost) {
            this.name = name;
            this.cost = cost;
        }

        public Builder addActions(int val) {
            addActions = val;
            return this;
        }

        public Builder addBuys(int val) {
            addBuys = val;
            return this;
        }

        public Builder addCards(int val) {
            addCards = val;
            return this;
        }

        public Builder addGold(int val) {
            addGold = val;
            return this;
        }

        public Builder attack(boolean val) {
            attack = val;
            return this;
        }

        public Builder vp(int val) {
            vp = val;
            return this;
        }

        public Builder dontAutoRecycleOnUse(boolean val) {
            dontAutoRecycleOnUse = val;
            return this;
        }

        public ActionCardImpl build() {
            return new ActionVictoryCardImpl(this);
        }
    }

    public int getVictoryPoints() {
        return vp;
    }

    @Override
    public CardImpl instantiate() {
        checkInstantiateOK();
        ActionVictoryCardImpl c = new ActionVictoryCardImpl();
        copyValues(c);
        return c;
    }

    protected void copyValues(ActionVictoryCardImpl c) {
        super.copyValues(c);
        c.vp = vp;
    }

    protected ActionVictoryCardImpl() {
    }

    int vp;

    @Override
    public String getStats() {
        StringBuilder sb = new StringBuilder();
        String superStats = super.getStats();
        sb.append(superStats);
        if (vp > 0) {
            if (!superStats.endsWith(")")) {
                sb.append(",");
            }
            sb.append(" " + vp + " Victory Point");
            if (vp > 1) {
                sb.append("s");
            }
        }
        return sb.toString();
    }
}
