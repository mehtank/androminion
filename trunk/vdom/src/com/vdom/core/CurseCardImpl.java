package com.vdom.core;

import com.vdom.api.CurseCard;

public class CurseCardImpl extends CardImpl implements CurseCard {
    public CurseCardImpl(String name, int cost, int vp, boolean costPotion) {
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
        CurseCardImpl c = new CurseCardImpl();
        copyValues(c);
        return c;
    }

    protected void copyValues(CurseCardImpl c) {
        super.copyValues(c);
        c.vp = vp;
    }

    protected CurseCardImpl() {
    }

    int vp;
}
