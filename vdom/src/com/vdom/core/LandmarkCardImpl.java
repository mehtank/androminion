package com.vdom.core;

import com.vdom.core.EventCardImpl.Builder;

public class LandmarkCardImpl extends CardImpl {
	protected LandmarkCardImpl(Builder builder) {
        super(builder);
    }

    public static class Builder extends CardImpl.Builder {
        public Builder(Cards.Type type) {
            super(type, 0);
        }

        public CardImpl build() {
            return new LandmarkCardImpl(this);
        }
    }
}
