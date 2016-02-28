package com.vdom.core;

import com.vdom.api.ReactionCard;

public class ReactionCardImpl extends CardImpl implements ReactionCard 
{
    public ReactionCardImpl(Cards.Type type, int cost) 
    {
        super(type, cost);
    }

    protected ReactionCardImpl(Builder builder) 
    {
        super(builder);
    }

    public static class Builder extends CardImpl.Builder {
        public Builder(Cards.Type type, int cost) 
        {
            super(type, cost);
        }

        public ReactionCardImpl build() 
        {
            return new ReactionCardImpl(this);
        }

    }

    @Override
    public CardImpl instantiate() 
    {
        checkInstantiateOK();
        ReactionCardImpl c = new ReactionCardImpl();
        copyValues(c);
        return c;
    }

    protected void copyValues(ReactionCardImpl c) 
    {
        super.copyValues(c);
    }

    protected ReactionCardImpl() 
    {
    }
}
