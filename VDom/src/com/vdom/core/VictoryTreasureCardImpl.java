package com.vdom.core;

import com.vdom.api.VictoryCard;

public class VictoryTreasureCardImpl extends TreasureCardImpl implements VictoryCard {
    int vp;
    
    public VictoryTreasureCardImpl(String name, int cost, int vp, int value, boolean costPotion) {
        super(name, cost, value, costPotion, false);
        this.vp = vp;
    }

    @Override
    public int getVictoryPoints() {
        return vp;
    }
    
    @Override
    public CardImpl instantiate() {
        checkInstantiateOK();
        VictoryTreasureCardImpl c = new VictoryTreasureCardImpl();
        copyValues(c);
        return c;
    }

    protected void copyValues(VictoryTreasureCardImpl c) {
        super.copyValues(c);
        c.vp = vp;
    }

    protected VictoryTreasureCardImpl() {
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
