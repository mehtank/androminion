package com.vdom.core;

import com.vdom.api.VictoryCard;

public class VictoryCardImpl extends CardImpl implements VictoryCard {
    public VictoryCardImpl(String name, int cost, int vp, boolean costPotion) {
        super(name, cost);
        this.vp = vp;
        this.costPotion = costPotion;
    }

    public int getVictoryPoints() {
        return vp;
    }

    @Override
    public CardImpl instantiate() {
        checkInstantiateOK();
        VictoryCardImpl c = new VictoryCardImpl();
        copyValues(c);
        return c;
    }

    protected void copyValues(VictoryCardImpl c) {
        super.copyValues(c);
        c.vp = vp;
    }

    protected VictoryCardImpl() {
    }

    int vp;
}
