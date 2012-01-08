package com.vdom.core;

import com.vdom.api.VictoryCard;

public class ActionVictoryCardImpl extends ActionCardImpl implements VictoryCard {
    protected ActionVictoryCardImpl(Builder builder) {
        super(builder);
    }

    public static class Builder extends ActionCardImpl.Builder {
        public Builder(String name, int cost) {
            super(name, cost);
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
    }

    protected ActionVictoryCardImpl() {
    }

}
