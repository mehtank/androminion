package com.vdom.api;

import com.vdom.core.Game;
import com.vdom.core.MoveContext;

public interface EventCard extends Card {
    public int getAddBuys();

    public void play(Game game, MoveContext context);

}
