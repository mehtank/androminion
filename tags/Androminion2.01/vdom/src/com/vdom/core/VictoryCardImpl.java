package com.vdom.core;

import com.vdom.api.VictoryCard;

public class VictoryCardImpl extends CardImpl implements VictoryCard {
    public VictoryCardImpl(String name, int cost, int vp) {
        super(name, cost);
        this.vp = vp;
    }

    protected VictoryCardImpl(Builder builder) {
        super(builder);
    }

    public static class Builder extends CardImpl.Builder {
        public Builder(String name, int cost, int vp) {
            super(name, cost);
            this.vp = vp;
        }

        public VictoryCardImpl build() {
            return new VictoryCardImpl(this);
        }

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
    }

    protected VictoryCardImpl() {
    }

}
