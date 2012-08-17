package com.vdom.core;

import com.vdom.api.CurseCard;

public class CurseCardImpl extends CardImpl implements CurseCard {
    public CurseCardImpl(Cards.Type type, int cost, int vp) {
        super(type, cost);
        this.vp = vp;
    }

    @Override
    public CardImpl instantiate() {
        checkInstantiateOK();
        CurseCardImpl c = new CurseCardImpl();
        copyValues(c);
        return c;
    }

    protected void copyValues(CurseCardImpl c) {
        super.copyValues(c);
    }

    protected CurseCardImpl() {
    }

    public int getVictoryPoints() {
        return vp;
    }

}
