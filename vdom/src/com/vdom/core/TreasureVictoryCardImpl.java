package com.vdom.core;

import com.vdom.api.VictoryCard;

public class TreasureVictoryCardImpl extends TreasureCardImpl implements VictoryCard {
    protected TreasureVictoryCardImpl(Builder builder) {
        super(builder);
    }

    public static class Builder extends TreasureCardImpl.Builder {
        public Builder(Cards.Type type, int cost, int value, int vp) {
            super(type, cost, value);
            this.vp = vp;
        }

        public TreasureCardImpl build() {
            return new TreasureVictoryCardImpl(this);
        }
    }

    public int getVictoryPoints() {
        return vp;
    }
    
    @Override
    public CardImpl instantiate() {
        checkInstantiateOK();
        TreasureVictoryCardImpl c = new TreasureVictoryCardImpl();
        copyValues(c);
        return c;
    }

    protected void copyValues(TreasureVictoryCardImpl c) {
        super.copyValues(c);
        c.vp = vp;
    }

    protected TreasureVictoryCardImpl() {
    }

    @Override
    public String getStats() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.getStats());
        if (vp > 0 || value > 0) {
            sb.append(" ");

            boolean start = true;
            if (value > 0) {
                if (start) {
                    start = false;
                } else {
                    sb.append(", ");
                }
                sb.append("" + value + " Gold");
            }
            if (vp > 0) {
                if (start) {
                    start = false;
                } else {
                    sb.append(", ");
                }
                sb.append("" + vp + " Victory Point");
                if (vp > 1) {
                    sb.append("s");
                }
            }
        }

        return sb.toString();
    }
}
