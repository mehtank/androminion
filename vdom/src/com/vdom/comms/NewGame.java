package com.vdom.comms;

import java.io.Serializable;
import java.util.List;

import com.vdom.api.Card;

public class NewGame implements Serializable {
    private static final long serialVersionUID = 229362050690595201L;

    public MyCard[] cards = null;
    public String[] players = null;
    public List<Card> druidBoons = null;

    public NewGame(MyCard[] cards, String[] players, List<Card> druidBoons) {
        this.cards = cards;
        this.players = players;
        this.druidBoons = druidBoons;
    }
}
