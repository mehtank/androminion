package com.vdom.core;

import com.vdom.api.DurationCard;


public class ActionDurationCardImpl extends ActionCardImpl implements DurationCard {
    protected int addActionsNextTurn;
    protected int addBuysNextTurn;
    protected int addCardsNextTurn;
    protected int addGoldNextTurn;
    protected boolean takeAnotherTurn;
    protected int takeAnotherTurnCardCount;

    protected ActionDurationCardImpl(Builder builder) {
        super(builder);
        addActionsNextTurn = builder.addActionsNextTurn;
        addBuysNextTurn = builder.addBuysNextTurn;
        addCardsNextTurn = builder.addCardsNextTurn;
        addGoldNextTurn = builder.addGoldNextTurn;
        takeAnotherTurn = builder.takeAnotherTurn;
        takeAnotherTurnCardCount = builder.takeAnotherTurnCardCount;
    }

    public int getAddActionsNextTurn() {
        return addActionsNextTurn;
    }

    public int getAddBuysNextTurn() {
        return addBuysNextTurn;
    }

    public int getAddCardsNextTurn() {
        return addCardsNextTurn;
    }

    public int getAddGoldNextTurn() {
        return addGoldNextTurn;
    }

    public boolean takeAnotherTurn() {
        return takeAnotherTurn;
    }

    public int takeAnotherTurnCardCount() {
        return takeAnotherTurnCardCount;
    }

    public static class Builder extends ActionCardImpl.Builder {
        protected int addActionsNextTurn;
        protected int addBuysNextTurn;
        protected int addCardsNextTurn;
        protected int addGoldNextTurn;
        protected boolean takeAnotherTurn;
        protected int takeAnotherTurnCardCount;

        public Builder(String name, int cost) {
            super(name, cost);
        }

        public Builder addActionsNextTurn(int val) {
            addActionsNextTurn = val;
            return this;
        }

        public Builder addBuysNextTurn(int val) {
            addBuysNextTurn = val;
            return this;
        }

        public Builder addCardsNextTurn(int val) {
            addCardsNextTurn = val;
            return this;
        }

        public Builder addGoldNextTurn(int val) {
            addGoldNextTurn = val;
            return this;
        }

        public Builder takeAnotherTurn(int val) {
            takeAnotherTurn = true;
            takeAnotherTurnCardCount = val;
            return this;
        }

        public ActionCardImpl build() {
            return new ActionDurationCardImpl(this);
        }
    }

    @Override
    public CardImpl instantiate() {
        checkInstantiateOK();
        ActionDurationCardImpl c = new ActionDurationCardImpl();
        copyValues(c);
        return c;
    }

    protected void copyValues(ActionDurationCardImpl c) {
        super.copyValues(c);
        c.addActionsNextTurn = addActionsNextTurn;
        c.addBuysNextTurn = addBuysNextTurn;
        c.addCardsNextTurn = addCardsNextTurn;
        c.addGoldNextTurn = addGoldNextTurn;
        c.takeAnotherTurn = takeAnotherTurn;
        c.takeAnotherTurnCardCount = takeAnotherTurnCardCount;
    }

    protected ActionDurationCardImpl() {
    }

    @Override
    public String getStats() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.getStats());
        if (addActionsNextTurn > 0 || addBuysNextTurn > 0 || addGoldNextTurn > 0 || addCardsNextTurn > 0) {
            sb.append(" (");

            boolean start = true;
            if (addActionsNextTurn > 0) {
                if (start) {
                    start = false;
                } else {
                    sb.append(", ");
                }
                sb.append("+" + addActionsNextTurn + " Action");
                if (addActionsNextTurn > 1) {
                    sb.append("s");
                }
            }
            if (addBuysNextTurn > 0) {
                if (start) {
                    start = false;
                } else {
                    sb.append(", ");
                }
                sb.append("+" + addBuysNextTurn + " Buy");
                if (addBuysNextTurn > 1) {
                    sb.append("s");
                }
            }
            if (addGoldNextTurn > 0) {
                if (start) {
                    start = false;
                } else {
                    sb.append(", ");
                }
                sb.append("+" + addGoldNextTurn + " Gold");
            }
            if (addCardsNextTurn > 0) {
                if (start) {
                    start = false;
                } else {
                    sb.append(", ");
                }
                sb.append("+" + addCardsNextTurn + " Card");
                if (addCardsNextTurn > 1) {
                    sb.append("s");
                }
            }

            sb.append(" next turn)");
        }

        return sb.toString();
    }

}
